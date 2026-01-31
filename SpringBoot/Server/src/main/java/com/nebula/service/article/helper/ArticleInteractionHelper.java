package com.nebula.service.article.helper;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.nebula.constant.ArticleConstants;
import com.nebula.constant.CountConstants;
import com.nebula.entity.BlogArticle;
import com.nebula.entity.BlogArticleCollect;
import com.nebula.entity.BlogArticleLike;
import com.nebula.exception.BusinessException;
import com.nebula.mapper.BlogArticleCollectMapper;
import com.nebula.mapper.BlogArticleLikeMapper;
import com.nebula.mapper.BlogArticleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 文章互动操作Helper
 * <p>
 * 封装文章的点赞、收藏、浏览等互动逻辑
 *
 * @author Nebula-Hash
 * @date 2026/1/31
 */
@Component
@RequiredArgsConstructor
public class ArticleInteractionHelper {

    private final BlogArticleMapper articleMapper;
    private final BlogArticleLikeMapper articleLikeMapper;
    private final BlogArticleCollectMapper articleCollectMapper;

    /**
     * 切换点赞状态
     *
     * @param articleId 文章ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void toggleLike(Long articleId) {
        toggleInteraction(
                articleId,
                articleLikeMapper,
                BlogArticleLike::getArticleId,
                BlogArticleLike::getUserId,
                BlogArticleLike::getId,
                () -> {
                    BlogArticleLike like = new BlogArticleLike();
                    like.setArticleId(articleId);
                    like.setUserId(StpUtil.getLoginIdAsLong());
                    return like;
                },
                BlogArticle::getLikeCount,
                BlogArticle::setLikeCount
        );
    }

    /**
     * 切换收藏状态
     *
     * @param articleId 文章ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void toggleCollect(Long articleId) {
        toggleInteraction(
                articleId,
                articleCollectMapper,
                BlogArticleCollect::getArticleId,
                BlogArticleCollect::getUserId,
                BlogArticleCollect::getId,
                () -> {
                    BlogArticleCollect collect = new BlogArticleCollect();
                    collect.setArticleId(articleId);
                    collect.setUserId(StpUtil.getLoginIdAsLong());
                    return collect;
                },
                BlogArticle::getCollectCount,
                BlogArticle::setCollectCount
        );
    }

    /**
     * 记录文章浏览
     *
     * @param articleId 文章ID
     */
    public void incrementViewCount(Long articleId) {
        BlogArticle article = articleMapper.selectById(articleId);
        if (article != null) {
            article.setViewCount(article.getViewCount() + CountConstants.INCREMENT);
            articleMapper.updateById(article);
        }
    }

    /**
     * 通用的文章交互操作（点赞/收藏）
     *
     * @param articleId       文章ID
     * @param mapper          Mapper
     * @param articleIdGetter 获取文章ID的方法引用
     * @param userIdGetter    获取用户ID的方法引用
     * @param idGetter        获取主键ID的方法引用
     * @param entitySupplier  创建新实体的方法
     * @param countGetter     获取计数的方法引用
     * @param countSetter     设置计数的方法引用
     */
    private <T> void toggleInteraction(
            Long articleId,
            BaseMapper<T> mapper,
            SFunction<T, Long> articleIdGetter,
            SFunction<T, Long> userIdGetter,
            SFunction<T, Long> idGetter,
            Supplier<T> entitySupplier,
            Function<BlogArticle, Integer> countGetter,
            BiConsumer<BlogArticle, Integer> countSetter) {

        Long userId = StpUtil.getLoginIdAsLong();

        // 查询是否已存在
        LambdaQueryWrapper<T> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(articleIdGetter, articleId)
                .eq(userIdGetter, userId);
        T existing = mapper.selectOne(wrapper);

        BlogArticle article = articleMapper.selectById(articleId);
        if (article == null) {
            throw new BusinessException(ArticleConstants.ERROR_ARTICLE_NOT_FOUND);
        }

        if (existing != null) {
            // 取消操作
            LambdaQueryWrapper<T> deleteWrapper = new LambdaQueryWrapper<>();
            deleteWrapper.eq(idGetter, idGetter.apply(existing));
            mapper.delete(deleteWrapper);
            countSetter.accept(article, Math.max(CountConstants.INIT_VALUE, countGetter.apply(article) - CountConstants.INCREMENT));
        } else {
            // 执行操作
            mapper.insert(entitySupplier.get());
            countSetter.accept(article, countGetter.apply(article) + CountConstants.INCREMENT);
        }

        articleMapper.updateById(article);
    }
}
