package com.nebula.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nebula.result.Result;
import com.nebula.dto.ArticleDTO;
import com.nebula.service.BlogArticleService;
import com.nebula.vo.ArticleListVO;
import com.nebula.vo.ArticleVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 文章控制器
 *
 * @author Nebula-Hash
 * @date 2025/11/25
 */
@RestController
@RequestMapping("/article")
@RequiredArgsConstructor
public class ArticleController {

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
     * 获取文章详情
     */
    @GetMapping("/{id}")
    public Result<ArticleVO> getArticleDetail(@PathVariable Long id) {
        ArticleVO articleVO = articleService.getArticleDetail(id);
        articleService.recordView(id);
        return Result.success(articleVO);
    }

    /**
     * 分页查询文章列表
     */
    @GetMapping("/list")
    public Result<Page<ArticleListVO>> getArticleList(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long tagId,
            @RequestParam(required = false) String keyword) {
        Page<ArticleListVO> page = articleService.getArticleList(current, size, categoryId, tagId, keyword);
        return Result.success(page);
    }

    /**
     * 获取热门文章
     */
    @GetMapping("/hot")
    public Result<List<ArticleListVO>> getHotArticles(@RequestParam(defaultValue = "5") Integer limit) {
        List<ArticleListVO> articles = articleService.getHotArticles(limit);
        return Result.success(articles);
    }

    /**
     * 获取推荐文章
     */
    @GetMapping("/recommend")
    public Result<List<ArticleListVO>> getRecommendArticles(@RequestParam(defaultValue = "5") Integer limit) {
        List<ArticleListVO> articles = articleService.getRecommendArticles(limit);
        return Result.success(articles);
    }

    /**
     * 获取我的文章
     */
    @GetMapping("/my")
    public Result<Page<ArticleListVO>> getMyArticles(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size) {
        Page<ArticleListVO> page = articleService.getMyArticles(current, size);
        return Result.success(page);
    }

    /**
     * 点赞文章
     */
    @PostMapping("/like/{id}")
    public Result<String> likeArticle(@PathVariable Long id) {
        articleService.likeArticle(id);
        return Result.success("操作成功");
    }

    /**
     * 收藏文章
     */
    @PostMapping("/collect/{id}")
    public Result<String> collectArticle(@PathVariable Long id) {
        articleService.collectArticle(id);
        return Result.success("操作成功");
    }
}
