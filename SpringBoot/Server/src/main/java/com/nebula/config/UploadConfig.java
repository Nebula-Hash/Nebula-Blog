package com.nebula.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.nebula.properties.UploadProperties;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 文件上传客户端配置
 *
 * @author Nebula-Hash
 * @date 2025/11/27
 */
@Configuration
@RequiredArgsConstructor
public class UploadConfig {

    private final UploadProperties uploadProperties;

    /**
     * MinIO客户端
     */
    @Bean
    @ConditionalOnProperty(name = "upload.mode", havingValue = "minio")
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(uploadProperties.getMinio().getEndpoint())
                .credentials(uploadProperties.getMinio().getAccessKey(), 
                           uploadProperties.getMinio().getSecretKey())
                .build();
    }

    /**
     * OSS客户端
     */
    @Bean
    @ConditionalOnProperty(name = "upload.mode", havingValue = "oss")
    public OSS ossClient() {
        return new OSSClientBuilder().build(
                uploadProperties.getOss().getEndpoint(),
                uploadProperties.getOss().getAccessKeyId(),
                uploadProperties.getOss().getAccessKeySecret()
        );
    }
}
