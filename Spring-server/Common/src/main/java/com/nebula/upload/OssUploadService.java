package com.nebula.upload;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.aliyun.oss.OSS;
import com.aliyun.oss.model.CopyObjectResult;
import com.aliyun.oss.model.ListObjectsRequest;
import com.aliyun.oss.model.ObjectListing;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.OSSObjectSummary;
import com.aliyun.oss.model.PutObjectRequest;
import com.nebula.exception.BusinessException;
import com.nebula.properties.UploadProperties;
import com.nebula.utils.WebPImageConversion;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 阿里云 OSS 上传服务
 *
 * @author Nebula-Hash
 * @date 2026/1/22
 */
@Slf4j
public class OssUploadService {

    private static final DateTimeFormatter DATE_DIR_FMT = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    /**
     * 扩展名到 Content-Type 的映射，确保上传后响应头准确
     */
    private static final Map<String, String> EXT_CONTENT_TYPE_MAP = Map.of(
            ".jpg", "image/jpeg",
            ".jpeg", "image/jpeg",
            ".png", "image/png",
            ".gif", "image/gif",
            ".webp", "image/webp",
            ".md", "text/markdown"
    );

    private final OSS ossClient;
    private final UploadProperties uploadProperties;
    private final WebPImageConversion imageConversionService;
    private final boolean uploadEnabled;

    /**
     * 构造启用态 OSS 上传服务
     *
     * @param ossClient OSS 客户端
     * @param uploadProperties 上传配置
     * @param imageConversionService WebP 图像转换服务
     */
    public OssUploadService(OSS ossClient,
                            UploadProperties uploadProperties,
                            WebPImageConversion imageConversionService) {
        this(ossClient, uploadProperties, imageConversionService, true);
    }

    /**
     * 构造 OSS 上传服务
     *
     * @param ossClient OSS 客户端
     * @param uploadProperties 上传配置
     * @param imageConversionService WebP 图像转换服务
     * @param uploadEnabled 上传模块是否启用
     */
    private OssUploadService(OSS ossClient,
                             UploadProperties uploadProperties,
                             WebPImageConversion imageConversionService,
                             boolean uploadEnabled) {
        this.ossClient = ossClient;
        this.uploadProperties = uploadProperties;
        this.imageConversionService = imageConversionService;
        this.uploadEnabled = uploadEnabled;
    }

    /**
     * 创建禁用态 OSS 上传服务
     *
     * @param uploadProperties 上传配置
     * @param imageConversionService WebP 图像转换服务
     * @return 禁用态 OSS 上传服务
     */
    public static OssUploadService disabled(UploadProperties uploadProperties,
                                            WebPImageConversion imageConversionService) {
        return new OssUploadService(null, uploadProperties, imageConversionService, false);
    }

    /**
     * 上传文件到 OSS
     *
     * @param file 文件对象
     * @param path 目标路径前缀
     * @return 文件访问 URL
     */
    public String uploadFile(MultipartFile file, String path) {
        ensureUploadEnabled();
        try {
            String dateDir = LocalDate.now().format(DATE_DIR_FMT);
            String fileBaseName = IdUtil.fastSimpleUUID();
            String originalSuffix = getFileSuffix(file.getOriginalFilename());
            String originalObjectName = buildObjectName(path, dateDir, fileBaseName, originalSuffix);

            // 根据配置判断是否进行 WebP 转换
            boolean shouldConvert = imageConversionService.shouldConvertToWebP(file);
            if (shouldConvert) {
                log.info("开始将图片转换为 WebP 格式: {}", file.getOriginalFilename());
                byte[] webpBytes = imageConversionService.convertToWebP(file);

                if (webpBytes != null) {
                    String webpObjectName = buildObjectName(
                            path,
                            dateDir,
                            fileBaseName,
                            imageConversionService.getWebPExtension()
                    );

                    if (imageConversionService.shouldKeepOriginal()) {
                        // 先上传原图，再上传 WebP，保证 WebP 失败时仍有可用文件
                        try (InputStream originalInputStream = file.getInputStream()) {
                            putObject(originalObjectName, originalInputStream, file.getSize(), originalSuffix);
                        }
                        try (InputStream webpInputStream = new ByteArrayInputStream(webpBytes)) {
                            putObject(
                                    webpObjectName,
                                    webpInputStream,
                                    webpBytes.length,
                                    imageConversionService.getWebPExtension()
                            );
                            return buildUrl(webpObjectName);
                        } catch (Exception webpUploadEx) {
                            log.warn("WebP 上传失败，回退使用原图: {}", file.getOriginalFilename(), webpUploadEx);
                            return buildUrl(originalObjectName);
                        }
                    }

                    try (InputStream webpInputStream = new ByteArrayInputStream(webpBytes)) {
                        putObject(
                                webpObjectName,
                                webpInputStream,
                                webpBytes.length,
                                imageConversionService.getWebPExtension()
                        );
                        return buildUrl(webpObjectName);
                    }
                }

                log.warn("WebP 转换失败，保留原图格式: {}", file.getOriginalFilename());
            }

            try (InputStream originalInputStream = file.getInputStream()) {
                putObject(originalObjectName, originalInputStream, file.getSize(), originalSuffix);
            }
            return buildUrl(originalObjectName);
        } catch (Exception e) {
            log.error("OSS 文件上传失败", e);
            throw new BusinessException("文件上传失败");
        }
    }

    /**
     * 删除 OSS 文件
     *
     * @param fileUrl 文件访问 URL
     */
    public void deleteFile(String fileUrl) {
        ensureUploadEnabled();
        try {
            String bucketName = uploadProperties.getOss().getBucketName();
            String objectName = extractObjectName(fileUrl);
            ossClient.deleteObject(bucketName, objectName);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("OSS 文件删除失败: {}", e.getMessage(), e);
            throw new BusinessException("文件删除失败");
        }
    }

    /**
     * 在 OSS 中移动文件（复制后删除源文件）
     *
     * @param sourceUrl 源文件 URL
     * @param targetPath 目标目录（不含文件名）
     * @return 移动后的文件 URL
     */
    public String moveFile(String sourceUrl, String targetPath) {
        ensureUploadEnabled();
        try {
            String bucketName = uploadProperties.getOss().getBucketName();
            String sourceObjectName = extractObjectName(sourceUrl);

            String fileName = sourceObjectName.substring(sourceObjectName.lastIndexOf("/") + 1);
            String targetObjectName = StrUtil.isBlank(targetPath)
                    ? fileName
                    : StrUtil.removeSuffix(targetPath, "/") + "/" + fileName;

            CopyObjectResult copyResult = ossClient.copyObject(bucketName, sourceObjectName, bucketName, targetObjectName);
            if (copyResult == null || StrUtil.isBlank(copyResult.getETag())) {
                throw new BusinessException("文件复制到目标位置失败");
            }

            try {
                ossClient.deleteObject(bucketName, sourceObjectName);
            } catch (Exception deleteEx) {
                // 删除源文件失败时，仅记录告警，避免影响主流程返回
                log.warn("文件移动后删除源文件失败（可能产生冗余文件）: {}", sourceObjectName, deleteEx);
            }

            return buildUrl(targetObjectName);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("OSS 文件移动失败", e);
            throw new BusinessException("文件移动失败");
        }
    }

    /**
     * 列出指定前缀下的 OSS 文件
     *
     * @param prefix 对象前缀
     * @return 文件信息列表
     */
    public List<FileInfo> listFiles(String prefix) {
        ensureUploadEnabled();
        try {
            String bucketName = uploadProperties.getOss().getBucketName();
            List<FileInfo> fileInfoList = new ArrayList<>();

            ListObjectsRequest request = new ListObjectsRequest(bucketName);
            request.setPrefix(prefix);
            request.setMaxKeys(1000);

            ObjectListing listing;
            do {
                listing = ossClient.listObjects(request);
                for (OSSObjectSummary summary : listing.getObjectSummaries()) {
                    String url = buildUrl(summary.getKey());
                    LocalDateTime lastModified = summary.getLastModified()
                            .toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDateTime();
                    fileInfoList.add(new FileInfo(summary.getKey(), url, lastModified));
                }
                request.setMarker(listing.getNextMarker());
            } while (listing.isTruncated());

            return fileInfoList;
        } catch (Exception e) {
            log.error("OSS 文件列表获取失败: {}", e.getMessage(), e);
            throw new BusinessException("获取文件列表失败");
        }
    }

    /**
     * 从文件 URL 中提取对象名称
     *
     * @param fileUrl 文件访问 URL
     * @return OSS 对象名称
     */
    public String extractObjectName(String fileUrl) {
        if (StrUtil.isBlank(fileUrl)) {
            throw new BusinessException("文件 URL 不能为空");
        }

        String host = uploadProperties.getOss().getCustomDomain();
        int hostIndex = fileUrl.indexOf(host);
        if (hostIndex == -1) {
            throw new BusinessException("无效的文件 URL：域名不匹配");
        }

        int pathStart = hostIndex + host.length() + 1;
        if (pathStart >= fileUrl.length()) {
            throw new BusinessException("无效的文件 URL：缺少对象路径");
        }

        String objectName = fileUrl.substring(pathStart);
        int queryIdx = objectName.indexOf('?');
        if (queryIdx > 0) {
            objectName = objectName.substring(0, queryIdx);
        }
        return objectName;
    }

    /**
     * 上传对象到 OSS 并设置基础元数据
     *
     * @param objectName 对象名称
     * @param inputStream 文件输入流
     * @param fileSize 文件大小（字节）
     * @param suffix 文件后缀
     */
    private void putObject(String objectName, InputStream inputStream, long fileSize, String suffix) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(fileSize);

        String resolvedContentType = EXT_CONTENT_TYPE_MAP.getOrDefault(
                suffix == null ? "" : suffix.toLowerCase(),
                "application/octet-stream"
        );
        metadata.setContentType(resolvedContentType);

        PutObjectRequest putObjectRequest = new PutObjectRequest(
                uploadProperties.getOss().getBucketName(),
                objectName,
                inputStream,
                metadata
        );

        ossClient.putObject(putObjectRequest);
        log.info("文件上传成功: {} ({}KB)", objectName, fileSize / 1024);
    }

    /**
     * 组装对象名称
     *
     * @param path 路径前缀
     * @param dateDir 日期目录
     * @param fileBaseName 文件基础名
     * @param suffix 文件后缀
     * @return 对象名称
     */
    private String buildObjectName(String path, String dateDir, String fileBaseName, String suffix) {
        String normalizedPath = StrUtil.removeSuffix(StrUtil.removePrefix(StrUtil.blankToDefault(path, ""), "/"), "/");
        String normalizedSuffix = suffix == null ? "" : suffix;
        if (StrUtil.isBlank(normalizedPath)) {
            return dateDir + "/" + fileBaseName + normalizedSuffix;
        }
        return normalizedPath + "/" + dateDir + "/" + fileBaseName + normalizedSuffix;
    }

    /**
     * 组装对象访问地址
     *
     * @param objectName 对象名称
     * @return 对象访问 URL
     */
    private String buildUrl(String objectName) {
        return "https://" + uploadProperties.getOss().getCustomDomain() + "/" + objectName;
    }

    /**
     * 校验上传模块开关
     */
    private void ensureUploadEnabled() {
        if (!uploadEnabled) {
            throw new BusinessException("文件上传功能已关闭");
        }
    }

    /**
     * 安全获取文件后缀
     *
     * @param originalFilename 原始文件名
     * @return 包含点号的后缀，无后缀时返回空字符串
     */
    private String getFileSuffix(String originalFilename) {
        if (StrUtil.isBlank(originalFilename)) {
            return "";
        }
        int lastDotIndex = originalFilename.lastIndexOf(".");
        if (lastDotIndex > 0 && lastDotIndex < originalFilename.length() - 1) {
            return originalFilename.substring(lastDotIndex);
        }
        return "";
    }

    /**
     * 文件信息
     *
     * @param objectName 对象名称
     * @param url 文件访问地址
     * @param lastModified 最后修改时间
     */
    public record FileInfo(String objectName, String url, LocalDateTime lastModified) {
    }
}
