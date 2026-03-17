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
import org.springframework.context.annotation.PropertySource;

/**
 * 文件上传自动配置类
 * 所有上传相关 Bean 均在此处注册，并由 upload.enabled 控制启用状态
 *
 * @author Nebula-Hash
 * @date 2026/1/22
 */
@Configuration
@PropertySource(value = "classpath:upload.properties", encoding = "UTF-8")
@EnableConfigurationProperties({UploadProperties.class, WebPProperties.class})
public class UploadAutoConfiguration {

    /**
     * 创建 OSS 客户端
     *
     * @param uploadProperties 上传配置
     * @return OSS 客户端实例
     */
    @Bean(destroyMethod = "shutdown")
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "upload", name = "enabled", havingValue = "true", matchIfMissing = true)
    public OSS ossClient(UploadProperties uploadProperties) {
        return new OSSClientBuilder().build(
                uploadProperties.getOss().getEndpoint(),
                uploadProperties.getOss().getAccessKeyId(),
                uploadProperties.getOss().getAccessKeySecret()
        );
    }

    /**
     * 创建 WebP 图像转换服务
     *
     * @param webPProperties WebP 配置
     * @return WebP 图像转换服务
     */
    @Bean
    @ConditionalOnMissingBean
    public WebPImageConversion webPImageConversion(WebPProperties webPProperties) {
        return new WebPImageConversion(webPProperties);
    }

    /**
     * 创建 OSS 上传服务
     *
     * @param ossClient OSS 客户端
     * @param uploadProperties 上传配置
     * @param webPImageConversion WebP 图像转换服务
     * @return OSS 上传服务
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "upload", name = "enabled", havingValue = "true", matchIfMissing = true)
    public OssUploadService ossUploadService(OSS ossClient,
                                             UploadProperties uploadProperties,
                                             WebPImageConversion webPImageConversion) {
        return new OssUploadService(ossClient, uploadProperties, webPImageConversion);
    }

    /**
     * 创建禁用态 OSS 上传服务占位实现
     * upload.enabled=false 时注入该 Bean，确保应用可以正常启动
     *
     * @param uploadProperties 上传配置
     * @param webPImageConversion WebP 图像转换服务
     * @return 禁用态 OSS 上传服务
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "upload", name = "enabled", havingValue = "false")
    public OssUploadService disabledOssUploadService(UploadProperties uploadProperties,
                                                     WebPImageConversion webPImageConversion) {
        return OssUploadService.disabled(uploadProperties, webPImageConversion);
    }

    /**
     * 创建文件上传工具类
     *
     * @param ossUploadService OSS 上传服务
     * @return 文件上传工具实例
     */
    @Bean
    @ConditionalOnMissingBean
    public FileUploadUtil fileUploadUtil(OssUploadService ossUploadService) {
        return new FileUploadUtil(ossUploadService);
    }
}
