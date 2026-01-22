package com.nebula.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nebula.result.Result;
import com.nebula.dto.CommentDTO;
import com.nebula.service.BlogCommentService;
import com.nebula.vo.CommentVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 评论控制器
 *
 * @author Nebula-Hash
 * @date 2026/1/22
 */
@RestController
@RequestMapping("/comment")
@RequiredArgsConstructor
public class CommentController {

    private final BlogCommentService commentService;

    /**
     * 发布评论
     */
    @PostMapping("/publish")
    public Result<Long> publishComment(@Valid @RequestBody CommentDTO commentDTO) {
        Long commentId = commentService.publishComment(commentDTO);
        return Result.success("评论成功", commentId);
    }

    /**
     * 删除评论
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteComment(@PathVariable Long id) {
        commentService.deleteComment(id);
        return Result.success("删除成功");
    }

    /**
     * 点赞评论
     */
    @PostMapping("/like/{id}")
    public Result<String> likeComment(@PathVariable Long id) {
        commentService.likeComment(id);
        return Result.success("操作成功");
    }

    /**
     * 获取文章评论列表
     */
    @GetMapping("/list/{articleId}")
    public Result<Page<CommentVO>> getArticleComments(
            @PathVariable Long articleId,
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size) {
        Page<CommentVO> page = commentService.getArticleComments(articleId, current, size);
        return Result.success(page);
    }
}
