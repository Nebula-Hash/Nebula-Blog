package com.nebula.strategy.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import com.aliyun.oss.OSS;
import com.aliyun.oss.model.PutObjectRequest;
import com.nebula.properties.UploadProperties;
import com.nebula.exception.BusinessException;
import com.nebula.strategy.UploadStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

/**
 * 阿里云OSS上传策略实现
 *
 * @author Nebula-Hash
 * @date 2025/11/27
 */
@Slf4j
@Service("ossUploadStrategyImpl")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "upload.mode", havingValue = "oss")
public class OssUploadStrategyImpl implements UploadStrategy {

    private final OSS ossClient;
    private final UploadProperties uploadProperties;

    @Override
    public String uploadFile(MultipartFile file, String path) {
        try {
            // 获取文件原名
            String originalFilename = file.getOriginalFilename();
            // 获取文件后缀
            String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
            // 生成新文件名：日期/UUID.后缀
            String fileName = DateUtil.today() + "/" + IdUtil.fastSimpleUUID() + suffix;
            // 拼接完整路径
            String objectName = path + "/" + fileName;

            // 上传文件
            try (InputStream inputStream = file.getInputStream()) {
                PutObjectRequest putObjectRequest = new PutObjectRequest(
                        uploadProperties.getOss().getBucketName(),
                        objectName,
                        inputStream
                );
                ossClient.putObject(putObjectRequest);
            }

            // 返回文件访问路径
            return "https://" + uploadProperties.getOss().getBucketName() + "." +
                   uploadProperties.getOss().getEndpoint() + "/" + objectName;

        } catch (Exception e) {
            log.error("OSS文件上传失败：{}", e.getMessage(), e);
            throw new BusinessException("文件上传失败");
        }
    }

    @Override
    public void deleteFile(String fileUrl) {
        try {
            // 从URL中提取对象名称
            String host = uploadProperties.getOss().getBucketName() + "." + 
                         uploadProperties.getOss().getEndpoint();
            String objectName = fileUrl.substring(fileUrl.indexOf(host) + host.length() + 1);

            ossClient.deleteObject(uploadProperties.getOss().getBucketName(), objectName);
        } catch (Exception e) {
            log.error("OSS文件删除失败：{}", e.getMessage(), e);
            throw new BusinessException("文件删除失败");
        }
    }
}
