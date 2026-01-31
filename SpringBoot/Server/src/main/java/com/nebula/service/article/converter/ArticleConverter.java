package com.nebula.service.article.converter;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nebula.entity.*;
import com.nebula.mapper.*;
import com.nebula.vo.ArticleListVO;
import com.nebula.vo.ArticleVO;
import com.nebula.vo.TagClientVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 文章VO转换器
 * <p>
 * 封装文章实体与VO对象之间的转换逻辑
 *
 * @author Nebula-Hash
 * @date 2026/1/31
 */
@Component
@RequiredArgsConstructor
public class ArticleConverter {

    private final SysUserMapper userMapper;
    private final BlogCategoryMapper categoryMapper;
    private final BlogTagMapper tagMapper;
    private final RelevancyArticleTagMapper articleTagMapper;
    private final BlogArticleLikeMapper articleLikeMapper;
    private final BlogArticleCollectMapper articleCollectMapper;

    /**
     * 批量转换文章列表（优化N+1查询）
     *
     * @param articles 文章实体列表
     * @return 文章列表VO
     */
    public List<ArticleListVO> batchToListVO(List<BlogArticle> articles) {
        if (articles.isEmpty()) {
            return new ArrayList<>();
        }

        // 批量查询用户信息
        Set<Long> userIds = articles.stream()
                .map(BlogArticle::getAuthorId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, SysUser> userMap = userIds.isEmpty() ? Map.of() :
                userMapper.selectBatchIds(userIds).stream()
                        .collect(Collectors.toMap(SysUser::getId, Function.identity()));

        // 批量查询分类信息
        Set<Long> categoryIds = articles.stream()
                .map(BlogArticle::getCategoryId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, BlogCategory> categoryMap = categoryIds.isEmpty() ? Map.of() :
                categoryMapper.selectBatchIds(categoryIds).stream()
                        .collect(Collectors.toMap(BlogCategory::getId, Function.identity()));

        // 转换为VO
        return articles.stream().map(article -> {
            ArticleListVO vo = new ArticleListVO();
            BeanUtils.copyProperties(article, vo);

            // 设置作者信息
            SysUser author = userMap.get(article.getAuthorId());
            if (author != null) {
                vo.setAuthorNickname(author.getNickname());
                vo.setAuthorAvatar(author.getAvatar());
            }

            // 设置分类信息
            BlogCategory category = categoryMap.get(article.getCategoryId());
            if (category != null) {
                vo.setCategoryName(category.getCategoryName());
            }

            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 转换文章分页结果
     *
     * @param articlePage 文章分页数据
     * @param current     当前页
     * @param size        每页大小
     * @return 文章列表VO分页
     */
    public Page<ArticleListVO> toVOPage(Page<BlogArticle> articlePage, Long current, Long size) {
        Page<ArticleListVO> voPage = new Page<>(current, size);
        voPage.setTotal(articlePage.getTotal());
        voPage.setRecords(batchToListVO(articlePage.getRecords()));
        return voPage;
    }

    /**
     * 构建文章详情VO
     *
     * @param article 文章实体
     * @return 文章详情VO
     */
    public ArticleVO toDetailVO(BlogArticle article) {
        ArticleVO articleVO = new ArticleVO();
        BeanUtils.copyProperties(article, articleVO);

        // 查询作者信息
        fillAuthorInfo(articleVO, article.getAuthorId());

        // 查询分类信息
        fillCategoryInfo(articleVO, article.getCategoryId());

        // 查询标签列表
        fillTagsInfo(articleVO, article.getId());

        // 查询当前用户交互状态
        fillUserInteractionStatus(articleVO, article.getId());

        return articleVO;
    }

    /**
     * 填充作者信息
     */
    private void fillAuthorInfo(ArticleVO articleVO, Long authorId) {
        if (authorId == null) {
            return;
        }
        SysUser author = userMapper.selectById(authorId);
        if (author != null) {
            articleVO.setAuthorNickname(author.getNickname());
            articleVO.setAuthorAvatar(author.getAvatar());
        }
    }

    /**
     * 填充分类信息
     */
    private void fillCategoryInfo(ArticleVO articleVO, Long categoryId) {
        if (categoryId == null) {
            return;
        }
        BlogCategory category = categoryMapper.selectById(categoryId);
        if (category != null) {
            articleVO.setCategoryName(category.getCategoryName());
        }
    }

    /**
     * 填充标签信息
     */
    private void fillTagsInfo(ArticleVO articleVO, Long articleId) {
        LambdaQueryWrapper<RelevancyArticleTag> tagWrapper = new LambdaQueryWrapper<>();
        tagWrapper.eq(RelevancyArticleTag::getArticleId, articleId);
        List<RelevancyArticleTag> articleTags = articleTagMapper.selectList(tagWrapper);

        if (articleTags.isEmpty()) {
            articleVO.setTags(new ArrayList<>());
            return;
        }

        List<Long> tagIds = articleTags.stream()
                .map(RelevancyArticleTag::getTagId)
                .collect(Collectors.toList());
        List<BlogTag> tags = tagMapper.selectBatchIds(tagIds);

        List<TagClientVO> tagVOs = tags.stream().map(tag -> {
            TagClientVO tagVO = new TagClientVO();
            BeanUtils.copyProperties(tag, tagVO);
            return tagVO;
        }).collect(Collectors.toList());

        articleVO.setTags(tagVOs);
    }

    /**
     * 填充用户交互状态（点赞/收藏）
     */
    private void fillUserInteractionStatus(ArticleVO articleVO, Long articleId) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();

            // 检查是否点赞
            LambdaQueryWrapper<BlogArticleLike> likeWrapper = new LambdaQueryWrapper<>();
            likeWrapper.eq(BlogArticleLike::getArticleId, articleId)
                    .eq(BlogArticleLike::getUserId, userId);
            articleVO.setIsLiked(articleLikeMapper.selectCount(likeWrapper) > 0);

            // 检查是否收藏
            LambdaQueryWrapper<BlogArticleCollect> collectWrapper = new LambdaQueryWrapper<>();
            collectWrapper.eq(BlogArticleCollect::getArticleId, articleId)
                    .eq(BlogArticleCollect::getUserId, userId);
            articleVO.setIsCollected(articleCollectMapper.selectCount(collectWrapper) > 0);
        } catch (Exception e) {
            // 未登录
            articleVO.setIsLiked(false);
            articleVO.setIsCollected(false);
        }
    }
}
