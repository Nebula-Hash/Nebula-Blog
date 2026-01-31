package com.nebula.service.article.helper;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
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
                () -> {
                    BlogArticleLike like = new BlogArticleLike();
                    like.setArticleId(articleId);
                    like.setUserId(StpUtil.getLoginIdAsLong());
                    return like;
                },
                "like_count"
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
                () -> {
                    BlogArticleCollect collect = new BlogArticleCollect();
                    collect.setArticleId(articleId);
                    collect.setUserId(StpUtil.getLoginIdAsLong());
                    return collect;
                },
                "collect_count"
        );
    }

    /**
     * 记录文章浏览（使用数据库原子更新，避免并发问题）
     *
     * @param articleId 文章ID
     */
    public void incrementViewCount(Long articleId) {
        articleMapper.update(null, new LambdaUpdateWrapper<BlogArticle>()
                .eq(BlogArticle::getId, articleId)
                .setSql("view_count = view_count + " + CountConstants.INCREMENT));
    }

    /**
     * 通用的文章交互操作（点赞/收藏）
     * <p>
     * 使用数据库原子更新，避免并发竞态条件
     *
     * @param articleId       文章ID
     * @param mapper          Mapper
     * @param articleIdGetter 获取文章ID的方法引用
     * @param userIdGetter    获取用户ID的方法引用
     * @param entitySupplier  创建新实体的方法
     * @param countColumn     计数字段名（数据库列名）
     */
    private <T> void toggleInteraction(
            Long articleId,
            BaseMapper<T> mapper,
            SFunction<T, Long> articleIdGetter,
            SFunction<T, Long> userIdGetter,
            Supplier<T> entitySupplier,
            String countColumn) {

        Long userId = StpUtil.getLoginIdAsLong();

        // 校验文章是否存在
        BlogArticle article = articleMapper.selectById(articleId);
        if (article == null) {
            throw new BusinessException(ArticleConstants.ERROR_ARTICLE_NOT_FOUND);
        }

        // 查询是否已存在
        LambdaQueryWrapper<T> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(articleIdGetter, articleId)
                .eq(userIdGetter, userId);
        T existing = mapper.selectOne(wrapper);

        if (existing != null) {
            // 取消操作：删除记录并原子递减计数
            mapper.delete(wrapper);
            articleMapper.update(null, new LambdaUpdateWrapper<BlogArticle>()
                    .eq(BlogArticle::getId, articleId)
                    .setSql(countColumn + " = GREATEST(0, " + countColumn + " - " + CountConstants.INCREMENT + ")"));
        } else {
            // 执行操作：插入记录并原子递增计数
            mapper.insert(entitySupplier.get());
            articleMapper.update(null, new LambdaUpdateWrapper<BlogArticle>()
                    .eq(BlogArticle::getId, articleId)
                    .setSql(countColumn + " = " + countColumn + " + " + CountConstants.INCREMENT));
        }
    }
}
