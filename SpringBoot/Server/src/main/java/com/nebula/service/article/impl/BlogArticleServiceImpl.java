package com.nebula.service.article.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nebula.constant.ArticleConstants;
import com.nebula.constant.CountConstants;
import com.nebula.dto.ArticleDTO;
import com.nebula.entity.BlogArticle;
import com.nebula.entity.RelevancyArticleTag;
import com.nebula.enumeration.DraftStatusEnum;
import com.nebula.enumeration.TopStatusEnum;
import com.nebula.exception.BusinessException;
import com.nebula.mapper.BlogArticleMapper;
import com.nebula.mapper.RelevancyArticleTagMapper;
import com.nebula.service.article.BlogArticleService;
import com.nebula.service.article.converter.ArticleConverter;
import com.nebula.service.article.helper.ArticleInteractionHelper;
import com.nebula.service.article.helper.ArticleQueryHelper;
import com.nebula.vo.ArticleListVO;
import com.nebula.vo.ArticleVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 文章服务实现类
 *
 * @author Nebula-Hash
 * @date 2026/1/22
 */
@Service
@RequiredArgsConstructor
public class BlogArticleServiceImpl implements BlogArticleService {

    private final BlogArticleMapper articleMapper;
    private final RelevancyArticleTagMapper articleTagMapper;

    // Helper组件
    private final ArticleQueryHelper queryHelper;
    private final ArticleConverter converter;
    private final ArticleInteractionHelper interactionHelper;

    // ==================== 文章CRUD ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long publishArticle(ArticleDTO articleDTO) {
        Long userId = StpUtil.getLoginIdAsLong();

        // 创建文章
        BlogArticle article = new BlogArticle();
        BeanUtils.copyProperties(articleDTO, article);
        article.setAuthorId(userId);
        article.setViewCount(CountConstants.INIT_VALUE);
        article.setLikeCount(CountConstants.INIT_VALUE);
        article.setCommentCount(CountConstants.INIT_VALUE);
        article.setCollectCount(CountConstants.INIT_VALUE);

        articleMapper.insert(article);

        // 保存文章标签关联
        saveArticleTags(article.getId(), articleDTO.getTagIds());

        return article.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateArticle(ArticleDTO articleDTO) {
        BlogArticle article = articleMapper.selectById(articleDTO.getId());
        if (article == null) {
            throw new BusinessException(ArticleConstants.ERROR_ARTICLE_NOT_FOUND);
        }

        // 更新文章
        BeanUtils.copyProperties(articleDTO, article);
        articleMapper.updateById(article);

        // 删除旧的标签关联
        LambdaQueryWrapper<RelevancyArticleTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RelevancyArticleTag::getArticleId, article.getId());
        articleTagMapper.delete(wrapper);

        // 保存新的标签关联
        saveArticleTags(article.getId(), articleDTO.getTagIds());
    }

    @Override
    public void deleteArticle(Long id) {
        BlogArticle article = articleMapper.selectById(id);
        if (article == null) {
            throw new BusinessException(ArticleConstants.ERROR_ARTICLE_NOT_FOUND);
        }
        articleMapper.deleteById(id);
    }

    // ==================== 文章查询 ====================

    @Override
    public ArticleVO getClientArticleDetail(Long id) {
        BlogArticle article = articleMapper.selectById(id);
        if (article == null || DraftStatusEnum.isDraft(article.getIsDraft())) {
            throw new BusinessException(ArticleConstants.ERROR_ARTICLE_NOT_FOUND);
        }
        return converter.toDetailVO(article);
    }

    @Override
    public ArticleVO getAdminArticleDetail(Long id) {
        BlogArticle article = articleMapper.selectById(id);
        if (article == null) {
            throw new BusinessException(ArticleConstants.ERROR_ARTICLE_NOT_FOUND);
        }
        return converter.toDetailVO(article);
    }

    @Override
    public Page<ArticleListVO> getClientArticleList(Long current, Long size, String authorName, String title,
                                                    String categoryName, String tagName) {
        Page<BlogArticle> page = new Page<>(current, size);
        LambdaQueryWrapper<BlogArticle> wrapper = new LambdaQueryWrapper<>();

        // 只查询已发布的文章
        wrapper.eq(BlogArticle::getIsDraft, DraftStatusEnum.PUBLISHED.getCode());

        // 应用搜索条件
        if (!queryHelper.applySearchConditions(wrapper, authorName, title, categoryName, tagName)) {
            return new Page<>(current, size);
        }

        // 排序
        applyDefaultSort(wrapper);

        Page<BlogArticle> articlePage = articleMapper.selectPage(page, wrapper);
        return converter.toVOPage(articlePage, current, size);
    }

    @Override
    public Page<ArticleListVO> getAdminArticleList(Long current, Long size, String authorName, String title,
                                                   String categoryName, String tagName, Integer isDraft, Integer isTop) {
        Page<BlogArticle> page = new Page<>(current, size);
        LambdaQueryWrapper<BlogArticle> wrapper = new LambdaQueryWrapper<>();

        // 状态筛选
        if (isDraft != null) {
            wrapper.eq(BlogArticle::getIsDraft, isDraft);
        }
        if (isTop != null) {
            wrapper.eq(BlogArticle::getIsTop, isTop);
        }

        // 应用搜索条件
        if (!queryHelper.applySearchConditions(wrapper, authorName, title, categoryName, tagName)) {
            return new Page<>(current, size);
        }

        // 排序
        applyDefaultSort(wrapper);

        Page<BlogArticle> articlePage = articleMapper.selectPage(page, wrapper);
        return converter.toVOPage(articlePage, current, size);
    }

    @Override
    public List<ArticleListVO> getHotArticles(Integer limit) {
        LambdaQueryWrapper<BlogArticle> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BlogArticle::getIsDraft, DraftStatusEnum.PUBLISHED.getCode());
        wrapper.orderByDesc(BlogArticle::getViewCount);
        wrapper.last("LIMIT " + limit);

        List<BlogArticle> articles = articleMapper.selectList(wrapper);
        return converter.batchToListVO(articles);
    }

    @Override
    public List<ArticleListVO> getRecommendArticles(Integer limit) {
        LambdaQueryWrapper<BlogArticle> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BlogArticle::getIsDraft, DraftStatusEnum.PUBLISHED.getCode());
        wrapper.eq(BlogArticle::getIsTop, TopStatusEnum.TOP.getCode());
        wrapper.orderByDesc(BlogArticle::getCreateTime);
        wrapper.last("LIMIT " + limit);

        List<BlogArticle> articles = articleMapper.selectList(wrapper);
        return converter.batchToListVO(articles);
    }

    // ==================== 文章互动 ====================

    @Override
    public void likeArticle(Long articleId) {
        interactionHelper.toggleLike(articleId);
    }

    @Override
    public void collectArticle(Long articleId) {
        interactionHelper.toggleCollect(articleId);
    }

    @Override
    public void incrementViewCount(Long articleId) {
        interactionHelper.incrementViewCount(articleId);
    }

    // ==================== 私有方法 ====================

    /**
     * 保存文章标签关联
     */
    private void saveArticleTags(Long articleId, List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return;
        }
        for (Long tagId : tagIds) {
            RelevancyArticleTag articleTag = new RelevancyArticleTag();
            articleTag.setArticleId(articleId);
            articleTag.setTagId(tagId);
            articleTagMapper.insert(articleTag);
        }
    }

    /**
     * 应用默认排序（置顶优先 + 创建时间倒序）
     */
    private void applyDefaultSort(LambdaQueryWrapper<BlogArticle> wrapper) {
        wrapper.orderByDesc(BlogArticle::getIsTop);
        wrapper.orderByDesc(BlogArticle::getCreateTime);
    }
}
