package com.nebula.vo.admin;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理端轮播图VO
 *
 * @author Nebula-Hash
 * @date 2026/1/23
 */
@Data
public class BannerAdminVO {

    /**
     * 轮播图ID
     */
    private Long id;

    /**
     * 标题
     */
    private String title;

    /**
     * 图片URL
     */
    private String imageUrl;

    /**
     * 关联文章ID
     */
    private Long articleId;

    /**
     * 文章标题
     */
    private String articleTitle;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 状态 0-禁用 1-启用
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
