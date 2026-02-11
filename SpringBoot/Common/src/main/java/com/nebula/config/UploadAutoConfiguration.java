package com.nebula.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.nebula.properties.UploadProperties;
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
     * OSS客户端
     */
    @Bean
    @ConditionalOnMissingBean
    public OSS ossClient(UploadProperties uploadProperties) {
        return new OSSClientBuilder().build(
                uploadProperties.getOss().getEndpoint(),
                uploadProperties.getOss().getAccessKeyId(),
                uploadProperties.getOss().getAccessKeySecret()
        );
    }
}
