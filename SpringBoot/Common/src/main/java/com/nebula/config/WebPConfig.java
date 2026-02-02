package com.nebula.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * WebP图像优化配置
 *
 * @author Nebula-Hash
 * @date 2026/02/02
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "webp")
public class WebPConfig {

    /**
     * 是否启用WebP自动转换
     * true: 上传时自动转换为WebP
     * false: 保持原格式
     */
    private boolean enabled = true;

    /**
     * WebP压缩质量 (0.0-1.0)
     * 0.75: 高压缩，适合缩略图
     * 0.85: 平衡模式（推荐）
     * 0.95: 高质量，适合重要图片
     */
    private float quality = 0.85f;

    /**
     * 图片最大宽度（像素）
     * 超过此尺寸会自动缩放
     */
    private int maxWidth = 2048;

    /**
     * 图片最大高度（像素）
     * 超过此尺寸会自动缩放
     */
    private int maxHeight = 2048;

    /**
     * 是否保留原图
     * true: 同时保存原图和WebP（占用双倍存储）
     * false: 只保存WebP（推荐）
     */
    private boolean keepOriginal = false;

    /**
     * 转换失败时的处理策略
     * KEEP_ORIGINAL: 保留原图（推荐）
     * THROW_EXCEPTION: 抛出异常
     */
    private FailureStrategy failureStrategy = FailureStrategy.KEEP_ORIGINAL;

    /**
     * 是否记录转换统计信息
     */
    private boolean enableStats = true;

    /**
     * 转换失败处理策略枚举
     */
    public enum FailureStrategy {
        /**
         * 保留原图
         */
        KEEP_ORIGINAL,

        /**
         * 抛出异常
         */
        THROW_EXCEPTION
    }
}
