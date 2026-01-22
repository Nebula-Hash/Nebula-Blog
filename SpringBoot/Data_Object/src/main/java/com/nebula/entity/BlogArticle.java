package com.nebula.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 文章实体类
 *
 * @author Nebula-Hash
 * @date 2026/1/22
 */
@Data
@TableName("blog_article")
public class BlogArticle implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 文章ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 作者ID
     */
    private Long userId;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 文章标题
     */
    private String title;

    /**
     * 文章摘要
     */
    private String summary;

    /**
     * 封面图
     */
    private String coverImage;

    /**
     * 文章内容(Markdown)
     */
    private String content;

    /**
     * HTML内容
     */
    private String htmlContent;

    /**
     * 是否置顶 0-否 1-是
     */
    private Integer isTop;

    /**
     * 是否草稿 0-否 1-是
     */
    private Integer isDraft;

    /**
     * 审核状态 0-待审核 1-审核通过 2-审核拒绝
     */
    private Integer auditStatus;

    /**
     * 浏览量
     */
    private Integer viewCount;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 评论数
     */
    private Integer commentCount;

    /**
     * 收藏数
     */
    private Integer collectCount;

    /**
     * 文章版本号
     */
    private Integer version;

    /**
     * 逻辑删除 0-未删除 1-已删除
     */
    @TableLogic
    private Integer deleted;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
