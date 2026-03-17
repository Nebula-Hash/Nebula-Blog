package com.nebula.service.comment;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nebula.dto.CommentDTO;
import com.nebula.vo.admin.BatchAuditResultVO;
import com.nebula.vo.admin.BatchDeleteResultVO;
import com.nebula.vo.admin.CommentAdminVO;
import com.nebula.vo.client.CommentClientVO;

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
     * 获取文章评论列表
     *
     * @param articleId 文章ID
     * @param current   当前页
     * @param size      每页大小
     * @return 评论列表
     */
    Page<CommentClientVO> getArticleComments(Long articleId, Long current, Long size);

    /**
     * 发布评论
     *
     * @param commentDTO 评论信息
     * @return 评论ID
     */
    Long publishComment(CommentDTO commentDTO);

    /**
     * 用户删除自己的评论
     *
     * @param commentId 评论ID
     */
    void deleteMyComment(Long commentId);

    /**
     * 评论点赞/取消点赞
     *
     * @param commentId 评论ID
     */
    void likeComment(Long commentId);

    /**
     * 获取根评论下的更多回复（分页）
     *
     * @param rootId  根评论ID
     * @param current 当前页
     * @param size    每页大小
     * @return 回复列表
     */
    Page<CommentClientVO> getReplies(Long rootId, Long current, Long size);


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
     * @return 批量审核结果
     */
    BatchAuditResultVO batchAuditComments(List<Long> commentIds, Integer auditStatus);

    /**
     * 批量删除评论
     *
     * @param commentIds 评论ID列表
     * @return 批量删除结果
     */
    BatchDeleteResultVO batchDeleteComments(List<Long> commentIds);

    /**
     * 获取待审核评论数量
     *
     * @return 待审核数量
     */
    Long getPendingAuditCount();
}
