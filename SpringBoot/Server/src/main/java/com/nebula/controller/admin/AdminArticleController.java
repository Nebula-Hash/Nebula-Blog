package com.nebula.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nebula.controller.config.AdminController;
import com.nebula.dto.ArticleDTO;
import com.nebula.result.Result;
import com.nebula.service.BlogArticleService;
import com.nebula.vo.ArticleListVO;
import com.nebula.vo.ArticleVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 文章控制器（管理端）
 *
 * @author Nebula-Hash
 * @date 2026/1/22
 */
@AdminController
@RequestMapping("/article")
@RequiredArgsConstructor
public class AdminArticleController {

    private final BlogArticleService articleService;

    /**
     * 发布文章
     */
    @PostMapping("/publish")
    public Result<Long> publishArticle(@Valid @RequestBody ArticleDTO articleDTO) {
        Long articleId = articleService.publishArticle(articleDTO);
        return Result.success("发布成功", articleId);
    }

    /**
     * 编辑文章
     */
    @PutMapping("/update")
    public Result<String> updateArticle(@Valid @RequestBody ArticleDTO articleDTO) {
        articleService.updateArticle(articleDTO);
        return Result.success("更新成功");
    }

    /**
     * 删除文章
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteArticle(@PathVariable Long id) {
        articleService.deleteArticle(id);
        return Result.success("删除成功");
    }

    /**
     * 获取文章详情（可查看草稿）
     */
    @GetMapping("/detail/{id}")
    public Result<ArticleVO> getArticleDetail(@PathVariable Long id) {
        ArticleVO articleVO = articleService.getAdminArticleDetail(id);
        return Result.success(articleVO);
    }

    /**
     * 分页查询文章列表（包含草稿）
     */
    @GetMapping("/list")
    public Result<Page<ArticleListVO>> getArticleList(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long tagId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer isDraft) {
        Page<ArticleListVO> page = articleService.getAdminArticleList(current, size, categoryId, tagId, keyword, isDraft);
        return Result.success(page);
    }
}
