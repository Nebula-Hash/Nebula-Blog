package com.nebula.service.auxiliary;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nebula.dto.CommentDTO;
import com.nebula.vo.CommentAdminVO;
import com.nebula.vo.CommentClientVO;

import java.util.List;

/**
 * 评论服务接口
 *
 * @author Nebula-Hash
 * @date 2026/1/22
 */
public interface BlogCommentService {

    // ==================== 客户端方法 ====================

    /**
     * 发布评论
     *
     * @param commentDTO 评论信息
     * @return 评论ID
     */
    Long publishComment(CommentDTO commentDTO);

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
    Page<CommentClientVO> getArticleComments(Long articleId, Long current, Long size);

    // ==================== 管理端方法 ====================

    /**
     * 管理端分页查询评论列表
     *
     * @param current     当前页
     * @param size        每页大小
     * @param articleId   文章ID（可选）
     * @param userId      用户ID（可选）
     * @param auditStatus 审核状态（可选）
     * @param keyword     评论内容关键词（可选）
     * @return 评论分页列表
     */
    Page<CommentAdminVO> getAdminCommentList(Long current, Long size, Long articleId,
                                              Long userId, Integer auditStatus, String keyword);

    /**
     * 审核评论
     *
     * @param commentId   评论ID
     * @param auditStatus 审核状态 1-通过 2-拒绝
     */
    void auditComment(Long commentId, Integer auditStatus);

    /**
     * 删除评论（级联删除子评论）
     *
     * @param commentId 评论ID
     */
    void deleteComment(Long commentId);

    /**
     * 批量审核评论
     *
     * @param commentIds  评论ID列表
     * @param auditStatus 审核状态 1-通过 2-拒绝
     */
    void batchAuditComments(List<Long> commentIds, Integer auditStatus);

    /**
     * 批量删除评论
     *
     * @param commentIds 评论ID列表
     */
    void batchDeleteComments(List<Long> commentIds);

    /**
     * 获取待审核评论数量
     *
     * @return 待审核数量
     */
    Long getPendingAuditCount();
}
