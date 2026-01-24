package com.nebula.upload.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.aliyun.oss.OSS;
import com.aliyun.oss.model.PutObjectRequest;
import com.nebula.exception.BusinessException;
import com.nebula.properties.UploadProperties;
import com.nebula.upload.UploadStrategy;
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
 * @date 2026/1/22
 */
@Slf4j
@Service("ossUploadStrategy")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "upload.mode", havingValue = "oss")
public class OssUploadStrategyImpl implements UploadStrategy {

    private final OSS ossClient;
    private final UploadProperties uploadProperties;

    @Override
    public String uploadFile(MultipartFile file, String path) {
        try {
            // 安全获取文件后缀
            String suffix = getFileSuffix(file.getOriginalFilename());
            // 生成新文件名：日期/UUID.后缀
            String fileName = DateUtil.today() + IdUtil.fastSimpleUUID() + suffix;
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

            /* 返回文件访问路径 */

            // 使用OSS提供的默认域名格式拼接文件访问路径
//            return "https://" + uploadProperties.getOss().getBucketName() + "." +
//                    uploadProperties.getOss().getEndpoint() + "/" + objectName;

            // 使用自定义域名拼接文件访问路径
            return "https://" + uploadProperties.getOss().getCustomDomain() + "/" + objectName;

        } catch (Exception e) {
            log.error("OSS文件上传失败：{}", e.getMessage(), e);
            throw new BusinessException("文件上传失败");
        }
    }

    @Override
    public void deleteFile(String fileUrl) {
        try {
            // 从URL中提取对象名称
            String bucketName = uploadProperties.getOss().getBucketName();

            // String host = bucketName + "." + uploadProperties.getOss().getEndpoint(); // 使用默认域名
            String host = uploadProperties.getOss().getCustomDomain(); // 使用自定义域名

            int hostIndex = fileUrl.indexOf(host);
            if (hostIndex == -1) {
                throw new BusinessException("无效的文件URL");
            }
            String objectName = fileUrl.substring(hostIndex + host.length() + 1);

            ossClient.deleteObject(bucketName, objectName);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("OSS文件删除失败：{}", e.getMessage(), e);
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
