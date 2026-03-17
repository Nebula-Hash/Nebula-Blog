package com.nebula.vo.admin;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理端评论VO
 *
 * @author Nebula-Hash
 * @date 2026/1/31
 */
@Data
public class CommentAdminVO {

    /**
     * 评论ID
     */
    private Long id;

    /**
     * 文章ID
     */
    private Long articleId;

    /**
     * 文章标题
     */
    private String articleTitle;

    /**
     * 根评论ID(楼主评论ID，根评论时为NULL)
     */
    private Long rootId;

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
     * 被回复用户ID
     */
    private Long replyUserId;

    /**
     * 被回复用户昵称
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
     * 审核状态 0-待审核 1-审核通过 2-审核拒绝
     */
    private Integer auditStatus;

    /**
     * 审核状态描述
     */
    private String auditStatusDesc;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
