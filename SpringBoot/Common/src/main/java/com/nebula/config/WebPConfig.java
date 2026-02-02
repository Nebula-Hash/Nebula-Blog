package com.nebula.config;

import lombok.Data;
import org.springframework.context.annotation.Configuration;

/**
 * WebP图像优化配置
 * @author Nebula-Hash
 * @date 2026/02/02
 */
@Data
@Configuration
public class WebPConfig {

    /**
     * 是否启用WebP自动转换
     * 默认: true (启用)
     * 说明: 上传图片时自动转换为WebP格式，可减少30-50%的文件大小
     */
    private boolean enabled = true;

    /**
     * WebP压缩质量 (0.0-1.0)
     * 默认: 0.85 (推荐值)
     * 说明:
     *   - 0.75: 高压缩，适合缩略图
     *   - 0.85: 平衡模式（推荐）
     *   - 0.95: 高质量，适合重要图片
     */
    private float quality = 0.85f;

    /**
     * 图片最大宽度（像素）
     * 默认: 2048
     * 说明: 超过此尺寸会自动等比缩放
     */
    private int maxWidth = 2048;

    /**
     * 图片最大高度（像素）
     * 默认: 2048
     * 说明: 超过此尺寸会自动等比缩放
     */
    private int maxHeight = 2048;

    /**
     * 是否保留原图
     * 默认: false (不保留)
     * 说明:
     *   - true: 同时保存原图和WebP（占用双倍存储空间）
     *   - false: 只保存WebP（推荐，节省存储）
     */
    private boolean keepOriginal = false;

    /**
     * 转换失败时的处理策略
     * 默认: KEEP_ORIGINAL (保留原图)
     * 说明:
     *   - KEEP_ORIGINAL: 转换失败时保留原图格式（推荐）
     *   - THROW_EXCEPTION: 转换失败时抛出异常
     */
    private FailureStrategy failureStrategy = FailureStrategy.KEEP_ORIGINAL;

    /**
     * 是否记录转换统计信息
     * 默认: true (记录)
     * 说明: 在日志中输出转换前后的文件大小和压缩率
     */
    private boolean enableStats = true;

    /**
     * 转换失败处理策略枚举
     */
    public enum FailureStrategy {
        /**
         * 保留原图格式
         */
        KEEP_ORIGINAL,

        /**
         * 抛出异常
         */
        THROW_EXCEPTION
    }
}
