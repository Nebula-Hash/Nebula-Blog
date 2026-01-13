package com.nebula.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nebula.dto.CommentDTO;
import com.nebula.vo.CommentVO;

/**
 * 评论服务接口
 *
 * @author Nebula-Hash
 * @date 2025/11/25
 */
public interface BlogCommentService {

    /**
     * 发布评论
     *
     * @param commentDTO 评论信息
     * @return 评论ID
     */
    Long publishComment(CommentDTO commentDTO);

    /**
     * 删除评论
     *
     * @param id 评论ID
     */
    void deleteComment(Long id);

    /**
     * 评论点赞/取消点赞
     *
     * @param commentId 评论ID
     */
    void likeComment(Long commentId);

    /**
     * 获取文章评论列表
     *
     * @param articleId 文章ID
     * @param current   当前页
     * @param size      每页大小
     * @return 评论列表
     */
    Page<CommentVO> getArticleComments(Long articleId, Long current, Long size);
}
