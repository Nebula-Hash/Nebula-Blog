package com.nebula.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 文件类型枚举
 *
 * @author Nebula-Hash
 * @date 2026/1/22
 */
@Getter
@AllArgsConstructor
public enum FileTypeEnum {

    /**
     * 图片类型
     */
    IMAGE("image/", "图片", 5L),

    /**
     * WebP图片
     */
    WEBP("image/webp", "WebP图片", 5L),

    /**
     * 纯文本类型
     */
    TEXT("text/", "文本", 2L),

    /**
     * Markdown 文件
     */
    MARKDOWN("text/markdown", "Markdown", 2L),

    /**
     * PDF 文档
     */
    PDF("application/pdf", "PDF文档", 10L),

    /**
     * 视频类型
     */
    VIDEO("video/", "视频", 100L),

    /**
     * 任意类型（不校验格式）
     */
    ANY(null, "任意类型", 10L);


    /**
     * Content-Type 前缀或完整值
     */
    private final String contentTypePattern;

    /**
     * 类型描述
     */
    private final String description;

    /**
     * 默认最大文件大小（MB）
     */
    private final Long defaultMaxSizeMB;

}