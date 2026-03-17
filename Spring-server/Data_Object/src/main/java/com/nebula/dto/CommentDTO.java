package com.nebula.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 评论发布DTO
 *
 * @author Nebula-Hash
 * @date 2026/1/22
 */
@Data
public class CommentDTO {

    /**
     * 文章ID
     */
    @NotNull(message = "文章ID不能为空")
    private Long articleId;

    /**
     * 父评论ID
     */
    private Long parentId;

    /**
     * 回复用户ID
     */
    private Long replyUserId;

    /**
     * 评论内容
     */
    @NotBlank(message = "评论内容不能为空")
    private String content;
}
