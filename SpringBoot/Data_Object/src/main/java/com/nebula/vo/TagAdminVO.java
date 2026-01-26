package com.nebula.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 标签VO（管理端）
 *
 * @author Nebula-Hash
 * @date 2026/1/26
 */
@Data
public class TagAdminVO {

    /**
     * 标签ID
     */
    private Long id;

    /**
     * 标签名称
     */
    private String tagName;

    /**
     * 文章数量
     */
    private Long articleCount;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
