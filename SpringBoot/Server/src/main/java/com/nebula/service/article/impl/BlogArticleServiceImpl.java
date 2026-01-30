package com.nebula.service.article.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nebula.dto.ArticleDTO;
import com.nebula.entity.*;
import com.nebula.exception.BusinessException;
import com.nebula.mapper.*;
import com.nebula.service.article.BlogArticleService;
import com.nebula.vo.ArticleListVO;
import com.nebula.vo.ArticleVO;
import com.nebula.vo.TagClientVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    private final BlogArticleLikeMapper articleLikeMapper;
    private final BlogArticleCollectMapper articleCollectMapper;
    private final BlogCategoryMapper categoryMapper;
    private final BlogTagMapper tagMapper;
    private final SysUserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long publishArticle(ArticleDTO articleDTO) {
        Long userId = StpUtil.getLoginIdAsLong();

        // 创建文章
        BlogArticle article = new BlogArticle();
        BeanUtils.copyProperties(articleDTO, article);
        article.setAuthorId(userId);
        article.setViewCount(0);
        article.setLikeCount(0);
        article.setCommentCount(0);
        article.setCollectCount(0);
        
        articleMapper.insert(article);

        // 保存文章标签关联
        if (articleDTO.getTagIds() != null && !articleDTO.getTagIds().isEmpty()) {
            for (Long tagId : articleDTO.getTagIds()) {
                RelevancyArticleTag articleTag = new RelevancyArticleTag();
                articleTag.setArticleId(article.getId());
                articleTag.setTagId(tagId);
                articleTagMapper.insert(articleTag);
            }
        }

        return article.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateArticle(ArticleDTO articleDTO) {
        // 查询文章
        BlogArticle article = articleMapper.selectById(articleDTO.getId());
        if (article == null) {
            throw new BusinessException("文章不存在");
        }

        // 更新文章
        BeanUtils.copyProperties(articleDTO, article);
        articleMapper.updateById(article);

        // 删除旧的标签关联
        LambdaQueryWrapper<RelevancyArticleTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RelevancyArticleTag::getArticleId, article.getId());
        articleTagMapper.delete(wrapper);

        // 保存新的标签关联
        if (articleDTO.getTagIds() != null && !articleDTO.getTagIds().isEmpty()) {
            for (Long tagId : articleDTO.getTagIds()) {
                RelevancyArticleTag articleTag = new RelevancyArticleTag();
                articleTag.setArticleId(article.getId());
                articleTag.setTagId(tagId);
                articleTagMapper.insert(articleTag);
            }
        }
    }

    @Override
    public void deleteArticle(Long id) {
        // 查询文章
        BlogArticle article = articleMapper.selectById(id);
        if (article == null) {
            throw new BusinessException("文章不存在");
        }

        // 逻辑删除
        articleMapper.deleteById(id);
    }

    @Override
    public ArticleVO getArticleDetail(Long id) {
        // 查询文章
        BlogArticle article = articleMapper.selectById(id);
        if (article == null || article.getIsDraft() == 1) {
            throw new BusinessException("文章不存在");
        }

        ArticleVO articleVO = new ArticleVO();
        BeanUtils.copyProperties(article, articleVO);

        // 查询作者信息
        SysUser author = userMapper.selectById(article.getAuthorId());
        if (author != null) {
            articleVO.setAuthorNickname(author.getNickname());
            articleVO.setAuthorAvatar(author.getAvatar());
        }

        // 查询分类信息
        if (article.getCategoryId() != null) {
            BlogCategory category = categoryMapper.selectById(article.getCategoryId());
            if (category != null) {
                articleVO.setCategoryName(category.getCategoryName());
            }
        }

        // 查询标签列表
        LambdaQueryWrapper<RelevancyArticleTag> tagWrapper = new LambdaQueryWrapper<>();
        tagWrapper.eq(RelevancyArticleTag::getArticleId, id);
        List<RelevancyArticleTag> articleTags = articleTagMapper.selectList(tagWrapper);
        
        List<TagClientVO> tags = new ArrayList<>();
        for (RelevancyArticleTag articleTag : articleTags) {
            BlogTag tag = tagMapper.selectById(articleTag.getTagId());
            if (tag != null) {
                TagClientVO tagVO = new TagClientVO();
                BeanUtils.copyProperties(tag, tagVO);
                tags.add(tagVO);
            }
        }
        articleVO.setTags(tags);

        // 查询当前用户是否点赞
        Long userId = null;
        try {
            userId = StpUtil.getLoginIdAsLong();
            
            LambdaQueryWrapper<BlogArticleLike> likeWrapper = new LambdaQueryWrapper<>();
            likeWrapper.eq(BlogArticleLike::getArticleId, id)
                    .eq(BlogArticleLike::getUserId, userId);
            Long likeCount = articleLikeMapper.selectCount(likeWrapper);
            articleVO.setIsLiked(likeCount > 0);

            // 查询当前用户是否收藏
            LambdaQueryWrapper<BlogArticleCollect> collectWrapper = new LambdaQueryWrapper<>();
            collectWrapper.eq(BlogArticleCollect::getArticleId, id)
                    .eq(BlogArticleCollect::getUserId, userId);
            Long collectCount = articleCollectMapper.selectCount(collectWrapper);
            articleVO.setIsCollected(collectCount > 0);
        } catch (Exception e) {
            // 未登录
            articleVO.setIsLiked(false);
            articleVO.setIsCollected(false);
        }

        return articleVO;
    }

    @Override
    public Page<ArticleListVO> getArticleList(Long current, Long size, Long categoryId, Long tagId, String keyword) {
        Page<BlogArticle> page = new Page<>(current, size);
        LambdaQueryWrapper<BlogArticle> wrapper = new LambdaQueryWrapper<>();
        
        // 只查询已发布的文章
        wrapper.eq(BlogArticle::getIsDraft, 0);
        
        // 分类筛选
        if (categoryId != null) {
            wrapper.eq(BlogArticle::getCategoryId, categoryId);
        }
        
        // 关键词搜索
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(BlogArticle::getTitle, keyword)
                    .or().like(BlogArticle::getSummary, keyword));
        }
        
        // 标签筛选
        if (tagId != null) {
            LambdaQueryWrapper<RelevancyArticleTag> tagWrapper = new LambdaQueryWrapper<>();
            tagWrapper.eq(RelevancyArticleTag::getTagId, tagId);
            List<RelevancyArticleTag> articleTags = articleTagMapper.selectList(tagWrapper);
            List<Long> articleIds = articleTags.stream()
                    .map(RelevancyArticleTag::getArticleId)
                    .collect(Collectors.toList());
            if (!articleIds.isEmpty()) {
                wrapper.in(BlogArticle::getId, articleIds);
            } else {
                // 没有文章匹配该标签
                return new Page<>(current, size);
            }
        }
        
        // 按置顶和创建时间排序
        wrapper.orderByDesc(BlogArticle::getIsTop);
        wrapper.orderByDesc(BlogArticle::getCreateTime);
        
        Page<BlogArticle> articlePage = articleMapper.selectPage(page, wrapper);
        
        // 转换为VO（使用批量查询优化）
        return convertToVOPage(articlePage, current, size);
    }

    @Override
    public List<ArticleListVO> getHotArticles(Integer limit) {
        LambdaQueryWrapper<BlogArticle> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BlogArticle::getIsDraft, 0);
        wrapper.orderByDesc(BlogArticle::getViewCount);
        wrapper.last("LIMIT " + limit);
        
        List<BlogArticle> articles = articleMapper.selectList(wrapper);
        return batchConvertToListVO(articles);
    }

    @Override
    public List<ArticleListVO> getRecommendArticles(Integer limit) {
        LambdaQueryWrapper<BlogArticle> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BlogArticle::getIsDraft, 0);
        wrapper.eq(BlogArticle::getIsTop, 1);
        wrapper.orderByDesc(BlogArticle::getCreateTime);
        wrapper.last("LIMIT " + limit);
        
        List<BlogArticle> articles = articleMapper.selectList(wrapper);
        return batchConvertToListVO(articles);
    }

    @Override
    public Page<ArticleListVO> getAdminArticleList(Long current, Long size, Long categoryId, Long tagId, String keyword, Integer isDraft) {
        Page<BlogArticle> page = new Page<>(current, size);
        LambdaQueryWrapper<BlogArticle> wrapper = new LambdaQueryWrapper<>();
        
        // 草稿状态筛选（null-全部，0-已发布，1-草稿）
        if (isDraft != null) {
            wrapper.eq(BlogArticle::getIsDraft, isDraft);
        }
        
        // 分类筛选
        if (categoryId != null) {
            wrapper.eq(BlogArticle::getCategoryId, categoryId);
        }
        
        // 关键词搜索
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(BlogArticle::getTitle, keyword)
                    .or().like(BlogArticle::getSummary, keyword));
        }
        
        // 标签筛选
        if (tagId != null) {
            LambdaQueryWrapper<RelevancyArticleTag> tagWrapper = new LambdaQueryWrapper<>();
            tagWrapper.eq(RelevancyArticleTag::getTagId, tagId);
            List<RelevancyArticleTag> articleTags = articleTagMapper.selectList(tagWrapper);
            List<Long> articleIds = articleTags.stream()
                    .map(RelevancyArticleTag::getArticleId)
                    .collect(Collectors.toList());
            if (!articleIds.isEmpty()) {
                wrapper.in(BlogArticle::getId, articleIds);
            } else {
                return new Page<>(current, size);
            }
        }
        
        // 按置顶和创建时间排序
        wrapper.orderByDesc(BlogArticle::getIsTop);
        wrapper.orderByDesc(BlogArticle::getCreateTime);
        
        Page<BlogArticle> articlePage = articleMapper.selectPage(page, wrapper);
        
        return convertToVOPage(articlePage, current, size);
    }

    @Override
    public ArticleVO getAdminArticleDetail(Long id) {
        // 查询文章（管理端可查看草稿）
        BlogArticle article = articleMapper.selectById(id);
        if (article == null) {
            throw new BusinessException("文章不存在");
        }
        return buildArticleVO(article);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void likeArticle(Long articleId) {
        toggleArticleInteraction(
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void collectArticle(Long articleId) {
        toggleArticleInteraction(
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
     * 通用的文章交互操作（点赞/收藏）
     *
     * @param articleId      文章ID
     * @param mapper         Mapper
     * @param articleIdGetter 获取文章ID的方法引用
     * @param userIdGetter   获取用户ID的方法引用
     * @param idGetter       获取主键ID的方法引用
     * @param entitySupplier 创建新实体的方法
     * @param countGetter    获取计数的方法引用
     * @param countSetter    设置计数的方法引用
     */
    private <T> void toggleArticleInteraction(
            Long articleId,
            com.baomidou.mybatisplus.core.mapper.BaseMapper<T> mapper,
            com.baomidou.mybatisplus.core.toolkit.support.SFunction<T, Long> articleIdGetter,
            com.baomidou.mybatisplus.core.toolkit.support.SFunction<T, Long> userIdGetter,
            com.baomidou.mybatisplus.core.toolkit.support.SFunction<T, Long> idGetter,
            java.util.function.Supplier<T> entitySupplier,
            Function<BlogArticle, Integer> countGetter,
            java.util.function.BiConsumer<BlogArticle, Integer> countSetter) {
        
        Long userId = StpUtil.getLoginIdAsLong();

        // 查询是否已存在
        LambdaQueryWrapper<T> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(articleIdGetter, articleId)
                .eq(userIdGetter, userId);
        T existing = mapper.selectOne(wrapper);

        BlogArticle article = articleMapper.selectById(articleId);
        if (article == null) {
            throw new BusinessException("文章不存在");
        }

        if (existing != null) {
            // 取消操作
            LambdaQueryWrapper<T> deleteWrapper = new LambdaQueryWrapper<>();
            deleteWrapper.eq(idGetter, idGetter.apply(existing));
            mapper.delete(deleteWrapper);
            countSetter.accept(article, Math.max(0, countGetter.apply(article) - 1));
        } else {
            // 执行操作
            mapper.insert(entitySupplier.get());
            countSetter.accept(article, countGetter.apply(article) + 1);
        }
        
        articleMapper.updateById(article);
    }

    @Override
    public void recordView(Long articleId) {
        // 更新文章浏览量
        BlogArticle article = articleMapper.selectById(articleId);
        if (article != null) {
            article.setViewCount(article.getViewCount() + 1);
            articleMapper.updateById(article);
        }
    }

    /**
     * 转换为列表VO（单条转换，仅用于少量数据）
     */
    private ArticleListVO convertToListVO(BlogArticle article) {
        ArticleListVO vo = new ArticleListVO();
        BeanUtils.copyProperties(article, vo);

        // 查询作者信息
        SysUser author = userMapper.selectById(article.getAuthorId());
        if (author != null) {
            vo.setAuthorNickname(author.getNickname());
            vo.setAuthorAvatar(author.getAvatar());
        }

        // 查询分类信息
        if (article.getCategoryId() != null) {
            BlogCategory category = categoryMapper.selectById(article.getCategoryId());
            if (category != null) {
                vo.setCategoryName(category.getCategoryName());
            }
        }

        return vo;
    }

    /**
     * 批量转换文章列表（优化N+1查询）
     */
    private List<ArticleListVO> batchConvertToListVO(List<BlogArticle> articles) {
        if (articles.isEmpty()) {
            return new ArrayList<>();
        }

        // 批量查询用户信息
        Set<Long> userIds = articles.stream()
                .map(BlogArticle::getAuthorId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        Map<Long, SysUser> userMap = userIds.isEmpty() ? Map.of() :
                userMapper.selectBatchIds(userIds).stream()
                        .collect(Collectors.toMap(SysUser::getId, Function.identity()));

        // 批量查询分类信息
        Set<Long> categoryIds = articles.stream()
                .map(BlogArticle::getCategoryId)
                .filter(id -> id != null)
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
     * 转换文章分页结果（使用批量查询优化）
     */
    private Page<ArticleListVO> convertToVOPage(Page<BlogArticle> articlePage, Long current, Long size) {
        Page<ArticleListVO> voPage = new Page<>(current, size);
        voPage.setTotal(articlePage.getTotal());
        voPage.setRecords(batchConvertToListVO(articlePage.getRecords()));
        return voPage;
    }

    /**
     * 构建文章详情VO
     */
    private ArticleVO buildArticleVO(BlogArticle article) {
        ArticleVO articleVO = new ArticleVO();
        BeanUtils.copyProperties(article, articleVO);

        // 查询作者信息
        SysUser author = userMapper.selectById(article.getAuthorId());
        if (author != null) {
            articleVO.setAuthorNickname(author.getNickname());
            articleVO.setAuthorAvatar(author.getAvatar());
        }

        // 查询分类信息
        if (article.getCategoryId() != null) {
            BlogCategory category = categoryMapper.selectById(article.getCategoryId());
            if (category != null) {
                articleVO.setCategoryName(category.getCategoryName());
            }
        }

        // 查询标签列表
        LambdaQueryWrapper<RelevancyArticleTag> tagWrapper = new LambdaQueryWrapper<>();
        tagWrapper.eq(RelevancyArticleTag::getArticleId, article.getId());
        List<RelevancyArticleTag> articleTags = articleTagMapper.selectList(tagWrapper);
        
        if (!articleTags.isEmpty()) {
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
        } else {
            articleVO.setTags(new ArrayList<>());
        }

        // 查询当前用户是否点赞/收藏
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            
            LambdaQueryWrapper<BlogArticleLike> likeWrapper = new LambdaQueryWrapper<>();
            likeWrapper.eq(BlogArticleLike::getArticleId, article.getId())
                    .eq(BlogArticleLike::getUserId, userId);
            articleVO.setIsLiked(articleLikeMapper.selectCount(likeWrapper) > 0);

            LambdaQueryWrapper<BlogArticleCollect> collectWrapper = new LambdaQueryWrapper<>();
            collectWrapper.eq(BlogArticleCollect::getArticleId, article.getId())
                    .eq(BlogArticleCollect::getUserId, userId);
            articleVO.setIsCollected(articleCollectMapper.selectCount(collectWrapper) > 0);
        } catch (Exception e) {
            articleVO.setIsLiked(false);
            articleVO.setIsCollected(false);
        }

        return articleVO;
    }
}
