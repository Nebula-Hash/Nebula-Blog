package com.nebula.service.article.helper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
     * @param categoryId   分类ID（精确匹配）
     * @param categoryName 分类名称（模糊搜索）
     * @param tagId        标签ID（精确匹配）
     * @param tagName      标签名称（模糊搜索）
     * @return true-条件应用成功，false-无匹配结果应返回空页
     */
    public boolean applySearchConditions(LambdaQueryWrapper<BlogArticle> wrapper,
                                         String authorName, String title,
                                         Long categoryId, String categoryName,
                                         Long tagId, String tagName) {
        // 作者名称模糊搜索
        if (!applyAuthorCondition(wrapper, authorName)) {
            return false;
        }

        // 标题模糊搜索
        applyTitleCondition(wrapper, title);

        // 分类搜索（优先使用ID，其次使用名称）
        if (!applyCategoryCondition(wrapper, categoryId, categoryName)) {
            return false;
        }

        // 标签搜索（优先使用ID，其次使用名称）
        if (!applyTagCondition(wrapper, tagId, tagName)) {
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

        // 使用 MyBatis-Plus 分页替代 wrapper.last(LIMIT)
        Page<SysUser> page = new Page<>(1, QUERY_LIMIT, false);
        LambdaQueryWrapper<SysUser> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.like(SysUser::getNickname, authorName);
        Page<SysUser> userPage = userMapper.selectPage(page, userWrapper);

        Set<Long> authorIds = userPage.getRecords().stream()
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
     * 应用分类搜索条件（优先使用ID，其次使用名称）
     *
     * @param categoryId   分类ID（精确匹配）
     * @param categoryName 分类名称（模糊搜索）
     * @return true-成功，false-无匹配结果
     */
    private boolean applyCategoryCondition(LambdaQueryWrapper<BlogArticle> wrapper, Long categoryId, String categoryName) {
        // 优先使用分类ID进行精确匹配
        if (categoryId != null) {
            wrapper.eq(BlogArticle::getCategoryId, categoryId);
            return true;
        }
        
        // 如果没有ID，使用名称进行模糊搜索
        if (categoryName == null || categoryName.isEmpty()) {
            return true;
        }

        // 使用 MyBatis-Plus 分页替代 wrapper.last(LIMIT)
        Page<BlogCategory> page = new Page<>(1, QUERY_LIMIT, false);
        LambdaQueryWrapper<BlogCategory> categoryWrapper = new LambdaQueryWrapper<>();
        categoryWrapper.like(BlogCategory::getCategoryName, categoryName);
        Page<BlogCategory> categoryPage = categoryMapper.selectPage(page, categoryWrapper);

        Set<Long> categoryIds = categoryPage.getRecords().stream()
                .map(BlogCategory::getId)
                .collect(Collectors.toSet());

        if (categoryIds.isEmpty()) {
            return false;
        }

        wrapper.in(BlogArticle::getCategoryId, categoryIds);
        return true;
    }

    /**
     * 应用标签搜索条件（优先使用ID，其次使用名称）
     * <p>
     * 使用分页限制数量避免大IN查询影响性能
     *
     * @param tagId   标签ID（精确匹配）
     * @param tagName 标签名称（模糊搜索）
     * @return true-成功，false-无匹配结果
     */
    private boolean applyTagCondition(LambdaQueryWrapper<BlogArticle> wrapper, Long tagId, String tagName) {
        Set<Long> tagIds;
        
        // 优先使用标签ID进行精确匹配
        if (tagId != null) {
            tagIds = Set.of(tagId);
        } else if (tagName != null && !tagName.isEmpty()) {
            // 如果没有ID，使用名称进行模糊搜索
            // 使用 MyBatis-Plus 分页查询匹配的标签
            Page<BlogTag> tagPage = new Page<>(1, QUERY_LIMIT, false);
            LambdaQueryWrapper<BlogTag> tagQueryWrapper = new LambdaQueryWrapper<>();
            tagQueryWrapper.like(BlogTag::getTagName, tagName);
            tagPage = tagMapper.selectPage(tagPage, tagQueryWrapper);

            tagIds = tagPage.getRecords().stream()
                    .map(BlogTag::getId)
                    .collect(Collectors.toSet());

            if (tagIds.isEmpty()) {
                return false;
            }
        } else {
            // 既没有ID也没有名称，不应用标签过滤
            return true;
        }

        // 使用 MyBatis-Plus 分页查询包含这些标签的文章
        Page<RelevancyArticleTag> articleTagPage = new Page<>(1, QUERY_LIMIT, false);
        LambdaQueryWrapper<RelevancyArticleTag> articleTagWrapper = new LambdaQueryWrapper<>();
        articleTagWrapper.in(RelevancyArticleTag::getTagId, tagIds);
        articleTagPage = articleTagMapper.selectPage(articleTagPage, articleTagWrapper);

        Set<Long> articleIds = articleTagPage.getRecords().stream()
                .map(RelevancyArticleTag::getArticleId)
                .collect(Collectors.toSet());

        if (articleIds.isEmpty()) {
            return false;
        }

        wrapper.in(BlogArticle::getId, articleIds);
        return true;
    }
}
