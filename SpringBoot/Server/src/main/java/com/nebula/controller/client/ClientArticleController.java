package com.nebula.controller.client;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nebula.constant.ArticleConstants;
import com.nebula.constant.CommonConstants;
import com.nebula.controller.config.ClientController;
import com.nebula.result.Result;
import com.nebula.service.article.BlogArticleService;
import com.nebula.vo.ArticleListVO;
import com.nebula.vo.ArticleVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 文章控制器（客户端）
 *
 * @author Nebula-Hash
 * @date 2026/1/22
 */
@ClientController
@RequestMapping("/article")
@RequiredArgsConstructor
public class ClientArticleController {

    private final BlogArticleService articleService;

    /**
     * 分页查询/搜索文章列表（仅已发布文章）
     * <p>
     * 不传搜索参数时返回所有非草稿文章；传入搜索参数时按条件筛选
     *
     * @param current      当前页
     * @param size         每页大小
     * @param authorName   作者名称（可选，模糊搜索）
     * @param title        文章标题（可选，模糊搜索）
     * @param categoryName 分类名称（可选，模糊搜索）
     * @param tagName      标签名称（可选，模糊搜索）
     * @return 文章分页列表
     */
    @GetMapping("/list")
    public Result<Page<ArticleListVO>> getClientArticleList(
            @RequestParam(defaultValue = CommonConstants.DEFAULT_PAGE_CURRENT) Long current,
            @RequestParam(defaultValue = CommonConstants.DEFAULT_PAGE_SIZE) Long size,
            @RequestParam(required = false) String authorName,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String categoryName,
            @RequestParam(required = false) String tagName) {
        Page<ArticleListVO> page = articleService.getClientArticleList(
                current, size, authorName, title, categoryName, tagName);
        return Result.success(page);
    }

    /**
     * 获取文章详情
     */
    @GetMapping("/detail/{id}")
    public Result<ArticleVO> getArticleDetail(@PathVariable Long id) {
        ArticleVO articleVO = articleService.getClientArticleDetail(id);
        articleService.incrementViewCount(id);
        return Result.success(articleVO);
    }

    /**
     * 获取热门文章
     */
    @GetMapping("/hot")
    public Result<List<ArticleListVO>> getHotArticles(
            @RequestParam(defaultValue = ArticleConstants.DEFAULT_HOT_LIMIT_STR) Integer limit) {
        List<ArticleListVO> articles = articleService.getHotArticles(limit);
        return Result.success(articles);
    }

    /**
     * 获取推荐文章
     */
    @GetMapping("/recommend")
    public Result<List<ArticleListVO>> getRecommendArticles(
            @RequestParam(defaultValue = ArticleConstants.DEFAULT_RECOMMEND_LIMIT_STR) Integer limit) {
        List<ArticleListVO> articles = articleService.getRecommendArticles(limit);
        return Result.success(articles);
    }

    /**
     * 点赞文章
     */
    @PostMapping("/like/{id}")
    public Result<String> likeArticle(@PathVariable Long id) {
        articleService.likeArticle(id);
        return Result.success(CommonConstants.MSG_SUCCESS);
    }

    /**
     * 收藏文章
     */
    @PostMapping("/collect/{id}")
    public Result<String> collectArticle(@PathVariable Long id) {
        articleService.collectArticle(id);
        return Result.success(CommonConstants.MSG_SUCCESS);
    }
}
