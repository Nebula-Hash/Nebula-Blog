package com.nebula.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 分类VO（管理端）
 *
 * @author Nebula-Hash
 * @date 2026/1/22
 */
@Data
public class CategoryAdminVO {

    /**
     * 分类ID
     */
    private Long id;

    /**
     * 分类名称
     */
    private String categoryName;

    /**
     * 分类描述
     */
    private String categoryDesc;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 文章数量
     */
    private Long articleCount;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
