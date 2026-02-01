package com.nebula.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nebula.constant.CommentConstants;
import com.nebula.constant.CommonConstants;
import com.nebula.controller.config.AdminController;
import com.nebula.result.Result;
import com.nebula.service.comment.BlogCommentService;
import com.nebula.vo.CommentAdminVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 评论控制器（管理端）
 *
 * @author Nebula-Hash
 * @date 2026/1/22
 */
@AdminController
@RequestMapping("/comment")
@RequiredArgsConstructor
public class AdminCommentController {

    private final BlogCommentService commentService;

    /**
     * 分页查询评论列表
     *
     * @param current     当前页
     * @param size        每页大小
     * @param articleId   文章ID（可选）
     * @param userId      用户ID（可选）
     * @param auditStatus 审核状态（可选）0-待审核 1-审核通过 2-审核拒绝
     * @param keyword     评论内容关键词（可选）
     * @return 评论分页列表
     */
    @GetMapping("/list")
    public Result<Page<CommentAdminVO>> getAdminCommentList(
            @RequestParam(defaultValue = CommonConstants.DEFAULT_PAGE_CURRENT) Long current,
            @RequestParam(defaultValue = CommonConstants.DEFAULT_PAGE_SIZE) Long size,
            @RequestParam(required = false) Long articleId,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Integer auditStatus,
            @RequestParam(required = false) String keyword) {
        Page<CommentAdminVO> page = commentService.getAdminCommentList(
                current, size, articleId, userId, auditStatus, keyword);
        return Result.success(page);
    }

    /**
     * 审核评论
     *
     * @param id          评论ID
     * @param auditStatus 审核状态 1-通过 2-拒绝
     * @return 操作结果
     */
    @PutMapping("/audit/{id}")
    public Result<String> auditComment(
            @PathVariable Long id,
            @RequestParam Integer auditStatus) {
        commentService.auditComment(id, auditStatus);
        return Result.success(CommentConstants.MSG_AUDIT_SUCCESS);
    }

    /**
     * 删除评论（级联删除子评论）
     *
     * @param id 评论ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteComment(@PathVariable Long id) {
        commentService.deleteComment(id);
        return Result.success(CommentConstants.MSG_DELETE_SUCCESS);
    }

    /**
     * 批量审核评论
     *
     * @param ids         评论ID列表
     * @param auditStatus 审核状态 1-通过 2-拒绝
     * @return 操作结果
     */
    @PutMapping("/audit/batch")
    public Result<String> batchAuditComments(
            @RequestBody List<Long> ids,
            @RequestParam Integer auditStatus) {
        commentService.batchAuditComments(ids, auditStatus);
        return Result.success(CommentConstants.MSG_BATCH_AUDIT_SUCCESS);
    }

    /**
     * 批量删除评论
     *
     * @param ids 评论ID列表
     * @return 操作结果
     */
    @DeleteMapping("/batch")
    public Result<String> batchDeleteComments(@RequestBody List<Long> ids) {
        commentService.batchDeleteComments(ids);
        return Result.success(CommentConstants.MSG_BATCH_DELETE_SUCCESS);
    }

    /**
     * 获取待审核评论数量
     *
     * @return 待审核数量
     */
    @GetMapping("/pending/count")
    public Result<Long> getPendingAuditCount() {
        Long count = commentService.getPendingAuditCount();
        return Result.success(count);
    }
}
