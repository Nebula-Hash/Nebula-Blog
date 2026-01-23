package com.nebula.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.nebula.properties.UploadProperties;
import io.minio.MinioClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * 文件上传自动配置类
 *
 * @author Nebula-Hash
 * @date 2026/1/22
 */
@Configuration
@EnableConfigurationProperties(UploadProperties.class)
@ComponentScan(basePackages = "com.nebula.upload")
@ConditionalOnProperty(prefix = "upload", name = "enabled", havingValue = "true", matchIfMissing = true)
public class UploadAutoConfiguration {

    /**
     * MinIO客户端
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(MinioClient.class)
    @ConditionalOnProperty(name = "upload.mode", havingValue = "minio")
    public MinioClient minioClient(UploadProperties uploadProperties) {
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
    @ConditionalOnMissingBean
    @ConditionalOnClass(OSS.class)
    @ConditionalOnProperty(name = "upload.mode", havingValue = "oss")
    public OSS ossClient(UploadProperties uploadProperties) {
        return new OSSClientBuilder().build(
                uploadProperties.getOss().getEndpoint(),
                uploadProperties.getOss().getAccessKeyId(),
                uploadProperties.getOss().getAccessKeySecret()
        );
    }
}
