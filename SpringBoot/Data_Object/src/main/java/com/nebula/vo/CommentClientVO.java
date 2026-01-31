package com.nebula.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 评论VO
 *
 * @author Nebula-Hash
 * @date 2026/1/22
 */
@Data
public class CommentClientVO {

    /**
     * 评论ID
     */
    private Long id;

    /**
     * 文章ID
     */
    private Long articleId;

    /**
     * 根评论ID(楼主评论ID，根评论时为NULL)
     */
    private Long rootId;

    /**
     * 父评论ID(直接回复对象)
     */
    private Long parentId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户昵称
     */
    private String nickname;

    /**
     * 用户头像
     */
    private String avatar;

    /**
     * 回复用户ID
     */
    private Long replyUserId;

    /**
     * 回复用户昵称
     */
    private String replyNickname;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 是否已点赞
     */
    private Boolean isLiked;

    /**
     * 子评论列表
     */
    private List<CommentClientVO> children;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
