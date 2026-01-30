package com.nebula.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 文章列表VO
 *
 * @author Nebula-Hash
 * @date 2026/1/22
 */
@Data
public class ArticleListVO {

    /**
     * 文章ID
     */
    private Long id;

    /**
     * 作者ID
     */
    private Long authorId;

    /**
     * 作者昵称
     */
    private String authorNickname;

    /**
     * 作者头像
     */
    private String authorAvatar;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 分类名称
     */
    private String categoryName;

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
     * 是否置顶
     */
    private Integer isTop;

    /**
     * 是否草稿
     */
    private Integer isDraft;

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
     * 标签列表
     */
    private List<TagClientVO> tags;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
