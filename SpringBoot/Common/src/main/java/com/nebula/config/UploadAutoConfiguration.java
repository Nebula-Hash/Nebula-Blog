package com.nebula.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.nebula.properties.UploadProperties;
import com.nebula.properties.WebPProperties;
import com.nebula.upload.FileUploadUtil;
import com.nebula.upload.OssUploadService;
import com.nebula.utils.WebPImageConversion;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 文件上传自动配置类
 * 所有上传相关 bean 均在此注册，受 upload.enabled 条件控制
 *
 * @author Nebula-Hash
 * @date 2026/1/22
 */
@Configuration
@EnableConfigurationProperties({UploadProperties.class, WebPProperties.class})
@ConditionalOnProperty(prefix = "upload", name = "enabled", havingValue = "true", matchIfMissing = true)
public class UploadAutoConfiguration {

    /**
     * OSS客户端
     */
    @Bean(destroyMethod = "shutdown")
    @ConditionalOnMissingBean
    public OSS ossClient(UploadProperties uploadProperties) {
        return new OSSClientBuilder().build(
                uploadProperties.getOss().getEndpoint(),
                uploadProperties.getOss().getAccessKeyId(),
                uploadProperties.getOss().getAccessKeySecret()
        );
    }

    /**
     * WebP 图像转换服务
     */
    @Bean
    @ConditionalOnMissingBean
    public WebPImageConversion webPImageConversion(WebPProperties webPProperties) {
        return new WebPImageConversion(webPProperties);
    }

    /**
     * OSS 上传服务
     */
    @Bean
    @ConditionalOnMissingBean
    public OssUploadService ossUploadService(OSS ossClient, UploadProperties uploadProperties,
                                             WebPImageConversion webPImageConversion) {
        return new OssUploadService(ossClient, uploadProperties, webPImageConversion);
    }

    /**
     * 文件上传工具
     */
    @Bean
    @ConditionalOnMissingBean
    public FileUploadUtil fileUploadUtil(OssUploadService ossUploadService) {
        return new FileUploadUtil(ossUploadService);
    }
}
