package com.nebula.upload;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.aliyun.oss.OSS;
import com.aliyun.oss.model.ListObjectsRequest;
import com.aliyun.oss.model.OSSObjectSummary;
import com.aliyun.oss.model.ObjectListing;
import com.aliyun.oss.model.PutObjectRequest;
import com.nebula.exception.BusinessException;
import com.nebula.properties.UploadProperties;
import com.nebula.utils.WebPImageConversion;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * 阿里云OSS上传服务
 *
 * @author Nebula-Hash
 * @date 2026/1/22
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OssUploadService {

    private final OSS ossClient;
    private final UploadProperties uploadProperties;
    private final WebPImageConversion imageConversionService;

    /**
     * 上传文件
     *
     * @param file 文件
     * @param path 路径
     * @return 文件访问地址
     */
    public String uploadFile(MultipartFile file, String path) {
        try {
            // 判断是否需要转换为WebP
            boolean shouldConvert = imageConversionService.shouldConvertToWebP(file);

            String suffix;
            InputStream inputStream;
            long fileSize;

            if (shouldConvert) {
                // 转换为WebP
                log.info("转换图片为WebP格式: {}", file.getOriginalFilename());
                InputStream webpStream = imageConversionService.convertToWebPStream(file);
                
                if (webpStream != null) {
                    // 转换成功，使用WebP
                    inputStream = webpStream;
                    suffix = imageConversionService.getWebPExtension();
                    fileSize = imageConversionService.estimateWebPSize(file.getSize());
                } else {
                    // 转换失败，使用原图
                    log.warn("WebP转换失败，保留原图格式: {}", file.getOriginalFilename());
                    suffix = getFileSuffix(file.getOriginalFilename());
                    inputStream = file.getInputStream();
                    fileSize = file.getSize();
                }
            } else {
                // 保持原格式
                suffix = getFileSuffix(file.getOriginalFilename());
                inputStream = file.getInputStream();
                fileSize = file.getSize();
            }

            // 生成新文件名：日期/UUID.后缀
            String fileName = DateUtil.today() + IdUtil.fastSimpleUUID() + suffix;
            // 拼接完整路径
            String objectName = path + "/" + fileName;

            // 上传文件
            try {
                PutObjectRequest putObjectRequest = new PutObjectRequest(
                        uploadProperties.getOss().getBucketName(),
                        objectName,
                        inputStream
                );
                ossClient.putObject(putObjectRequest);
                
                log.info("文件上传成功: {} -> {}, 大小: {}KB", 
                    file.getOriginalFilename(), objectName, fileSize / 1024);
            } finally {
                inputStream.close();
            }

            // 返回文件访问路径（使用自定义域名）
            return "https://" + uploadProperties.getOss().getCustomDomain() + "/" + objectName;

        } catch (Exception e) {
            log.error("OSS文件上传失败：{}", e.getMessage(), e);
            throw new BusinessException("文件上传失败");
        }
    }

    /**
     * 删除文件
     *
     * @param fileUrl 文件地址
     */
    public void deleteFile(String fileUrl) {
        try {
            String bucketName = uploadProperties.getOss().getBucketName();
            String objectName = extractObjectName(fileUrl);
            ossClient.deleteObject(bucketName, objectName);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("OSS文件删除失败：{}", e.getMessage(), e);
            throw new BusinessException("文件删除失败");
        }
    }

    /**
     * 移动文件
     *
     * @param sourceUrl  源文件URL
     * @param targetPath 目标路径（不含文件名）
     * @return 移动后的文件访问URL
     */
    public String moveFile(String sourceUrl, String targetPath) {
        try {
            String bucketName = uploadProperties.getOss().getBucketName();
            String sourceObjectName = extractObjectName(sourceUrl);

            // 提取文件名
            String fileName = sourceObjectName.substring(sourceObjectName.lastIndexOf("/") + 1);
            String targetObjectName = targetPath + "/" + fileName;

            // 复制文件到新位置
            ossClient.copyObject(bucketName, sourceObjectName, bucketName, targetObjectName);
            // 删除原文件
            ossClient.deleteObject(bucketName, sourceObjectName);

            // 返回新的访问URL
            return "https://" + uploadProperties.getOss().getCustomDomain() + "/" + targetObjectName;
        } catch (Exception e) {
            log.error("OSS文件移动失败：{}", e.getMessage(), e);
            throw new BusinessException("文件移动失败");
        }
    }

    /**
     * 列出指定前缀下的文件
     *
     * @param prefix 路径前缀
     * @return 文件信息列表
     */
    public List<FileInfo> listFiles(String prefix) {
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
                    String url = "https://" + uploadProperties.getOss().getCustomDomain() + "/" + summary.getKey();
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
            log.error("OSS文件列表获取失败：{}", e.getMessage(), e);
            throw new BusinessException("获取文件列表失败");
        }
    }

    /**
     * 从URL中提取对象名称
     */
    private String extractObjectName(String fileUrl) {
        String host = uploadProperties.getOss().getCustomDomain();
        int hostIndex = fileUrl.indexOf(host);
        if (hostIndex == -1) {
            throw new BusinessException("无效的文件URL");
        }
        return fileUrl.substring(hostIndex + host.length() + 1);
    }

    /**
     * 安全获取文件后缀
     *
     * @param originalFilename 原始文件名
     * @return 文件后缀（包含点号），如 ".jpg"；无后缀返回空字符串
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
     */
    public record FileInfo(String objectName, String url, LocalDateTime lastModified) {}
}
