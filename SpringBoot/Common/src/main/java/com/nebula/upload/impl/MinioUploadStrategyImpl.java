package com.nebula.upload.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.nebula.exception.BusinessException;
import com.nebula.properties.UploadProperties;
import com.nebula.utils.WebPImageConversion;
import com.nebula.upload.UploadStrategy;
import io.minio.*;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * MinIO上传策略实现
 *
 * @author Nebula-Hash
 * @date 2026/1/22
 */
@Slf4j
@Service("minioUploadStrategy")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "upload.mode", havingValue = "minio")
public class MinioUploadStrategyImpl implements UploadStrategy {

    private final MinioClient minioClient;
    private final UploadProperties uploadProperties;
    private final WebPImageConversion imageConversionService;

    @Override
    public String uploadFile(MultipartFile file, String path) {
        try {
            // 判断是否需要转换为WebP
            boolean shouldConvert = imageConversionService.shouldConvertToWebP(file);

            String suffix;
            InputStream inputStream;
            long fileSize;
            String contentType;

            if (shouldConvert) {
                // 转换为WebP
                log.info("转换图片为WebP格式: {}", file.getOriginalFilename());
                InputStream webpStream = imageConversionService.convertToWebPStream(file);
                
                if (webpStream != null) {
                    // 转换成功，使用WebP
                    inputStream = webpStream;
                    suffix = imageConversionService.getWebPExtension();
                    fileSize = imageConversionService.estimateWebPSize(file.getSize());
                    contentType = "image/webp";
                } else {
                    // 转换失败，使用原图
                    log.warn("WebP转换失败，保留原图格式: {}", file.getOriginalFilename());
                    suffix = getFileSuffix(file.getOriginalFilename());
                    inputStream = file.getInputStream();
                    fileSize = file.getSize();
                    contentType = file.getContentType();
                }
            } else {
                // 保持原格式
                suffix = getFileSuffix(file.getOriginalFilename());
                inputStream = file.getInputStream();
                fileSize = file.getSize();
                contentType = file.getContentType();
            }

            // 生成新文件名：日期/UUID.后缀
            String fileName = DateUtil.today() + "/" + IdUtil.fastSimpleUUID() + suffix;
            // 拼接完整路径
            String objectName = path + "/" + fileName;

            // 上传文件
            try {
                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(uploadProperties.getMinio().getBucketName())
                                .object(objectName)
                                .stream(inputStream, fileSize, -1)
                                .contentType(contentType)
                                .build()
                );
                
                log.info("文件上传成功: {} -> {}, 大小: {}KB", 
                    file.getOriginalFilename(), objectName, fileSize / 1024);
            } finally {
                inputStream.close();
            }

            // 返回文件访问路径
            return uploadProperties.getMinio().getEndpoint() + "/" +
                    uploadProperties.getMinio().getBucketName() + "/" + objectName;

        } catch (Exception e) {
            log.error("MinIO文件上传失败：{}", e.getMessage(), e);
            throw new BusinessException("文件上传失败");
        }
    }

    @Override
    public void deleteFile(String fileUrl) {
        try {
            String bucketName = uploadProperties.getMinio().getBucketName();
            String objectName = extractObjectName(fileUrl);

            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("MinIO文件删除失败：{}", e.getMessage(), e);
            throw new BusinessException("文件删除失败");
        }
    }

    @Override
    public String moveFile(String sourceUrl, String targetPath) {
        try {
            String bucketName = uploadProperties.getMinio().getBucketName();
            String sourceObjectName = extractObjectName(sourceUrl);

            // 提取文件名
            String fileName = sourceObjectName.substring(sourceObjectName.lastIndexOf("/") + 1);
            String targetObjectName = targetPath + "/" + fileName;

            // 复制文件到新位置
            minioClient.copyObject(
                    CopyObjectArgs.builder()
                            .bucket(bucketName)
                            .object(targetObjectName)
                            .source(CopySource.builder()
                                    .bucket(bucketName)
                                    .object(sourceObjectName)
                                    .build())
                            .build()
            );

            // 删除原文件
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(sourceObjectName)
                            .build()
            );

            // 返回新的访问URL
            return uploadProperties.getMinio().getEndpoint() + "/" + bucketName + "/" + targetObjectName;
        } catch (Exception e) {
            log.error("MinIO文件移动失败：{}", e.getMessage(), e);
            throw new BusinessException("文件移动失败");
        }
    }

    @Override
    public List<FileInfo> listFiles(String prefix) {
        try {
            String bucketName = uploadProperties.getMinio().getBucketName();
            List<FileInfo> fileInfoList = new ArrayList<>();

            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(bucketName)
                            .prefix(prefix)
                            .recursive(true)
                            .build()
            );

            for (Result<Item> result : results) {
                Item item = result.get();
                String url = uploadProperties.getMinio().getEndpoint() + "/" + bucketName + "/" + item.objectName();
                LocalDateTime lastModified = item.lastModified()
                        .toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime();
                fileInfoList.add(new FileInfo(item.objectName(), url, lastModified));
            }

            return fileInfoList;
        } catch (Exception e) {
            log.error("MinIO文件列表获取失败：{}", e.getMessage(), e);
            throw new BusinessException("获取文件列表失败");
        }
    }

    /**
     * 从URL中提取对象名称
     */
    private String extractObjectName(String fileUrl) {
        String bucketName = uploadProperties.getMinio().getBucketName();
        int bucketIndex = fileUrl.indexOf(bucketName);
        if (bucketIndex == -1) {
            throw new BusinessException("无效的文件URL");
        }
        return fileUrl.substring(bucketIndex + bucketName.length() + 1);
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
}
