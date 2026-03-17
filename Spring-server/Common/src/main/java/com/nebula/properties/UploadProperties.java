package com.nebula.properties;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * 文件上传配置属性
 *
 * @author Nebula-Hash
 * @date 2026/1/22
 */
@Data
@Validated
@ConfigurationProperties(prefix = "upload")
public class UploadProperties {

    /**
     * 上传模块总开关
     */
    private boolean enabled = true;

    /**
     * OSS 配置
     */
    @Valid
    private OssConfig oss = new OssConfig();

    /**
     * 文件存储目录配置
     */
    @Valid
    private DirectoryConfig directory = new DirectoryConfig();

    /**
     * OSS 连接配置
     *
     * @author Nebula-Hash
     * @date 2026/1/22
     */
    @Data
    public static class OssConfig {

        /**
         * OSS 访问端点
         */
        private String endpoint;

        /**
         * 访问密钥 ID
         */
        private String accessKeyId;

        /**
         * 访问密钥
         */
        private String accessKeySecret;

        /**
         * 存储桶名称
         */
        private String bucketName;

        /**
         * 自定义访问域名
         */
        private String customDomain;
    }

    /**
     * 目录配置
     */
    @Data
    public static class DirectoryConfig {

        /**
         * 图片目录配置
         */
        @Valid
        private ImageDirectoryConfig images = new ImageDirectoryConfig();
    }

    /**
     * 图片目录配置
     */
    @Data
    public static class ImageDirectoryConfig {

        /**
         * 文章封面目录
         */
        @NotBlank(message = "upload.directory.images.article-covers 不能为空")
        private String articleCovers = "images/articles/covers";

        /**
         * Banner 图片目录
         */
        @NotBlank(message = "upload.directory.images.banners 不能为空")
        private String banners = "images/banners";

        /**
         * 头像目录
         */
        @NotBlank(message = "upload.directory.images.avatars 不能为空")
        private String avatars = "images/avatars";
    }
}
