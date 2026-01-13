package com.nebula.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 评论发布DTO
 *
 * @author Nebula-Hash
 * @date 2025/11/25
 */
@Data
public class CommentDTO {

    /**
     * 文章ID
     */
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
