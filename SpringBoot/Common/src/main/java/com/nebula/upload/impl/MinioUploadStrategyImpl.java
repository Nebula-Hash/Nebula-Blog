package com.nebula.upload.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.nebula.exception.BusinessException;
import com.nebula.properties.UploadProperties;
import com.nebula.upload.UploadStrategy;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

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

    @Override
    public String uploadFile(MultipartFile file, String path) {
        try {
            // 安全获取文件后缀
            String suffix = getFileSuffix(file.getOriginalFilename());
            // 生成新文件名：日期/UUID.后缀
            String fileName = DateUtil.today() + "/" + IdUtil.fastSimpleUUID() + suffix;
            // 拼接完整路径
            String objectName = path + "/" + fileName;

            // 上传文件
            try (InputStream inputStream = file.getInputStream()) {
                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(uploadProperties.getMinio().getBucketName())
                                .object(objectName)
                                .stream(inputStream, file.getSize(), -1)
                                .contentType(file.getContentType())
                                .build()
                );
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
            // 从URL中提取对象名称
            String bucketName = uploadProperties.getMinio().getBucketName();
            int bucketIndex = fileUrl.indexOf(bucketName);
            if (bucketIndex == -1) {
                throw new BusinessException("无效的文件URL");
            }
            String objectName = fileUrl.substring(bucketIndex + bucketName.length() + 1);

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
