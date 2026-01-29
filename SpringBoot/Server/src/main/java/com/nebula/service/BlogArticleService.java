package com.nebula.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nebula.dto.ArticleDTO;
import com.nebula.vo.ArticleListVO;
import com.nebula.vo.ArticleVO;

import java.util.List;

/**
 * 文章服务接口
 *
 * @author Nebula-Hash
 * @date 2026/1/22
 */
public interface BlogArticleService {

    /**
     * 发布文章
     *
     * @param articleDTO 文章信息
     * @return 文章ID
     */
    Long publishArticle(ArticleDTO articleDTO);

    /**
     * 编辑文章
     *
     * @param articleDTO 文章信息
     */
    void updateArticle(ArticleDTO articleDTO);

    /**
     * 删除文章
     *
     * @param id 文章ID
     */
    void deleteArticle(Long id);

    /**
     * 获取文章详情
     *
     * @param id 文章ID
     * @return 文章详情
     */
    ArticleVO getArticleDetail(Long id);

    /**
     * 客户端分页查询文章列表（仅已发布文章）
     *
     * @param current    当前页
     * @param size       每页大小
     * @param categoryId 分类ID
     * @param tagId      标签ID
     * @param keyword    关键词
     * @return 文章列表
     */
    Page<ArticleListVO> getArticleList(Long current, Long size, Long categoryId, Long tagId, String keyword);

    /**
     * 管理端分页查询文章列表（包含草稿）
     *
     * @param current    当前页
     * @param size       每页大小
     * @param categoryId 分类ID
     * @param tagId      标签ID
     * @param keyword    关键词
     * @param isDraft    草稿状态（null-全部，0-已发布，1-草稿）
     * @return 文章列表
     */
    Page<ArticleListVO> getAdminArticleList(Long current, Long size, Long categoryId, Long tagId, String keyword, Integer isDraft);

    /**
     * 管理端获取文章详情（可查看草稿）
     *
     * @param id 文章ID
     * @return 文章详情
     */
    ArticleVO getAdminArticleDetail(Long id);

    /**
     * 获取热门文章
     *
     * @param limit 数量
     * @return 文章列表
     */
    List<ArticleListVO> getHotArticles(Integer limit);

    /**
     * 获取推荐文章
     *
     * @param limit 数量
     * @return 文章列表
     */
    List<ArticleListVO> getRecommendArticles(Integer limit);

    /**
     * 文章点赞/取消点赞
     *
     * @param articleId 文章ID
     */
    void likeArticle(Long articleId);

    /**
     * 文章收藏/取消收藏
     *
     * @param articleId 文章ID
     */
    void collectArticle(Long articleId);

    /**
     * 记录浏览历史
     *
     * @param articleId 文章ID
     */
    void recordView(Long articleId);
}
