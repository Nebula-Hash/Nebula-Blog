package com.nebula.controller.client;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nebula.constant.CommonConstants;
import com.nebula.controller.config.ClientController;
import com.nebula.dto.CommentDTO;
import com.nebula.result.Result;
import com.nebula.service.auxiliary.BlogCommentService;
import com.nebula.vo.CommentClientVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 评论控制器（客户端）
 *
 * @author Nebula-Hash
 * @date 2026/1/22
 */
@ClientController
@RequestMapping("/comment")
@RequiredArgsConstructor
public class ClientCommentController {

    private final BlogCommentService commentService;

    /**
     * 获取文章评论列表
     *
     * @param articleId 文章ID
     * @param current   当前页
     * @param size      每页大小
     * @return 评论分页列表
     */
    @GetMapping("/list/{articleId}")
    public Result<Page<CommentClientVO>> getArticleComments(
            @PathVariable Long articleId,
            @RequestParam(defaultValue = CommonConstants.DEFAULT_PAGE_CURRENT) Long current,
            @RequestParam(defaultValue = CommonConstants.DEFAULT_PAGE_SIZE) Long size) {
        Page<CommentClientVO> page = commentService.getArticleComments(articleId, current, size);
        return Result.success(page);
    }

    /**
     * 发表评论
     * TODO: 后续添加评论审核功能，目前默认发布即审核通过
     *
     * @param commentDTO 评论信息
     * @return 评论ID
     */
    @PostMapping("/publish")
    public Result<Long> publishComment(@Valid @RequestBody CommentDTO commentDTO) {
        Long commentId = commentService.publishComment(commentDTO);
        return Result.success("评论成功", commentId);
    }

    /**
     * 点赞评论
     *
     * @param id 评论ID
     * @return 操作结果
     */
    @PostMapping("/like/{id}")
    public Result<String> likeComment(@PathVariable Long id) {
        commentService.likeComment(id);
        return Result.success(CommonConstants.MSG_SUCCESS);
    }
}
