package com.nebula.controller.admin;

import com.nebula.controller.config.AdminController;
import com.nebula.service.auxiliary.BlogCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 评论控制器（管理端）
 * TODO: 后续实现评论管理功能（审核、删除、列表查询等）
 *
 * @author Nebula-Hash
 * @date 2026/1/22
 */
@AdminController
@RequestMapping("/comment")
@RequiredArgsConstructor
public class AdminCommentController {

    private final BlogCommentService commentService;

    // TODO: 评论列表查询（支持审核状态筛选）

    // TODO: 审核评论（通过/拒绝）

    // TODO: 删除评论
}
