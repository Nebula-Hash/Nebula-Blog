package com.nebula.service.article.helper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nebula.entity.*;
import com.nebula.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 文章查询条件构建器
 * <p>
 * 封装文章模块的搜索条件构建逻辑，支持作者、标题、分类、标签的模糊搜索
 *
 * @author Nebula-Hash
 * @date 2026/1/31
 */
@Component
@RequiredArgsConstructor
public class ArticleQueryHelper {

    /**
     * 查询结果数量限制，避免大IN查询影响性能
     */
    private static final int QUERY_LIMIT = 1000;

    private final SysUserMapper userMapper;
    private final BlogCategoryMapper categoryMapper;
    private final BlogTagMapper tagMapper;
    private final RelevancyArticleTagMapper articleTagMapper;

    /**
     * 应用搜索条件到查询包装器
     *
     * @param wrapper      查询包装器
     * @param authorName   作者名称（模糊搜索）
     * @param title        文章标题（模糊搜索）
     * @param categoryName 分类名称（模糊搜索）
     * @param tagName      标签名称（模糊搜索）
     * @return true-条件应用成功，false-无匹配结果应返回空页
     */
    public boolean applySearchConditions(LambdaQueryWrapper<BlogArticle> wrapper,
                                         String authorName, String title,
                                         String categoryName, String tagName) {
        // 作者名称模糊搜索
        if (!applyAuthorCondition(wrapper, authorName)) {
            return false;
        }

        // 标题模糊搜索
        applyTitleCondition(wrapper, title);

        // 分类名称模糊搜索
        if (!applyCategoryCondition(wrapper, categoryName)) {
            return false;
        }

        // 标签名称模糊搜索
        if (!applyTagCondition(wrapper, tagName)) {
            return false;
        }

        return true;
    }

    /**
     * 应用作者搜索条件
     *
     * @return true-成功，false-无匹配结果
     */
    private boolean applyAuthorCondition(LambdaQueryWrapper<BlogArticle> wrapper, String authorName) {
        if (authorName == null || authorName.isEmpty()) {
            return true;
        }

        LambdaQueryWrapper<SysUser> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.like(SysUser::getNickname, authorName)
                .last("LIMIT " + QUERY_LIMIT);
        List<SysUser> matchedUsers = userMapper.selectList(userWrapper);

        Set<Long> authorIds = matchedUsers.stream()
                .map(SysUser::getId)
                .collect(Collectors.toSet());

        if (authorIds.isEmpty()) {
            return false;
        }

        wrapper.in(BlogArticle::getAuthorId, authorIds);
        return true;
    }

    /**
     * 应用标题搜索条件
     */
    private void applyTitleCondition(LambdaQueryWrapper<BlogArticle> wrapper, String title) {
        if (title != null && !title.isEmpty()) {
            wrapper.like(BlogArticle::getTitle, title);
        }
    }

    /**
     * 应用分类搜索条件
     *
     * @return true-成功，false-无匹配结果
     */
    private boolean applyCategoryCondition(LambdaQueryWrapper<BlogArticle> wrapper, String categoryName) {
        if (categoryName == null || categoryName.isEmpty()) {
            return true;
        }

        LambdaQueryWrapper<BlogCategory> categoryWrapper = new LambdaQueryWrapper<>();
        categoryWrapper.like(BlogCategory::getCategoryName, categoryName)
                .last("LIMIT " + QUERY_LIMIT);
        List<BlogCategory> matchedCategories = categoryMapper.selectList(categoryWrapper);

        Set<Long> categoryIds = matchedCategories.stream()
                .map(BlogCategory::getId)
                .collect(Collectors.toSet());

        if (categoryIds.isEmpty()) {
            return false;
        }

        wrapper.in(BlogArticle::getCategoryId, categoryIds);
        return true;
    }

    /**
     * 应用标签搜索条件
     * <p>
     * 添加数量限制避免大IN查询影响性能
     *
     * @return true-成功，false-无匹配结果
     */
    private boolean applyTagCondition(LambdaQueryWrapper<BlogArticle> wrapper, String tagName) {
        if (tagName == null || tagName.isEmpty()) {
            return true;
        }

        // 查询匹配的标签（限制数量）
        LambdaQueryWrapper<BlogTag> tagQueryWrapper = new LambdaQueryWrapper<>();
        tagQueryWrapper.like(BlogTag::getTagName, tagName)
                .last("LIMIT " + QUERY_LIMIT);
        List<BlogTag> matchedTags = tagMapper.selectList(tagQueryWrapper);

        Set<Long> tagIds = matchedTags.stream()
                .map(BlogTag::getId)
                .collect(Collectors.toSet());

        if (tagIds.isEmpty()) {
            return false;
        }

        // 查询包含这些标签的文章（限制数量）
        LambdaQueryWrapper<RelevancyArticleTag> tagWrapper = new LambdaQueryWrapper<>();
        tagWrapper.in(RelevancyArticleTag::getTagId, tagIds)
                .last("LIMIT " + QUERY_LIMIT);
        List<RelevancyArticleTag> articleTags = articleTagMapper.selectList(tagWrapper);

        Set<Long> articleIds = articleTags.stream()
                .map(RelevancyArticleTag::getArticleId)
                .collect(Collectors.toSet());

        if (articleIds.isEmpty()) {
            return false;
        }

        wrapper.in(BlogArticle::getId, articleIds);
        return true;
    }
}
