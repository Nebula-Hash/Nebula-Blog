package com.nebula.service.article;

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
     * 管理端分页查询/搜索文章列表（包含草稿）
     * <p>
     * 不传搜索参数时返回所有文章；传入搜索参数时按条件筛选
     *
     * @param current      当前页
     * @param size         每页大小
     * @param authorName   作者名称（可选，模糊搜索）
     * @param title        文章标题（可选，模糊搜索）
     * @param categoryName 分类名称（可选，模糊搜索）
     * @param tagName      标签名称（可选，模糊搜索）
     * @param isDraft      草稿状态（null-全部，0-已发布，1-草稿）
     * @param isTop        置顶状态（null-全部，0-未置顶，1-置顶）
     * @return 文章列表
     */
    Page<ArticleListVO> getAdminArticleList(Long current, Long size, String authorName, String title,
                                            String categoryName, String tagName, Integer isDraft, Integer isTop);

    /**
     * 管理端获取文章详情（可查看草稿）
     *
     * @param id 文章ID
     * @return 文章详情
     */
    ArticleVO getAdminArticleDetail(Long id);

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
     * 客户端分页查询/搜索文章列表（仅已发布文章，过滤草稿）
     * <p>
     * 不传搜索参数时返回所有非草稿文章；传入搜索参数时按条件筛选
     *
     * @param current      当前页
     * @param size         每页大小
     * @param authorName   作者名称（可选，模糊搜索）
     * @param title        文章标题（可选，模糊搜索）
     * @param categoryName 分类名称（可选，模糊搜索）
     * @param tagName      标签名称（可选，模糊搜索）
     * @return 文章列表
     */
    Page<ArticleListVO> getClientArticleList(Long current, Long size, String authorName, String title,
                                             String categoryName, String tagName);

    /**
     * 客户端获取文章详情
     *
     * @param id 文章ID
     * @return 文章详情
     */
    ArticleVO getClientArticleDetail(Long id);

    /**
     * 客户端获取文章详情（并更新浏览量）
     * <p>
     * 先查询文章确认存在，再更新浏览量，最后返回详情
     *
     * @param id 文章ID
     * @return 文章详情（包含更新后的浏览量）
     */
    ArticleVO getClientArticleDetailWithView(Long id);

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
     * 增加文章浏览量
     *
     * @param articleId 文章ID
     */
    void incrementViewCount(Long articleId);
}
