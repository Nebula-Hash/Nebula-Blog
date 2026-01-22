package com.nebula.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nebula.dto.ArticleDTO;
import com.nebula.entity.*;
import com.nebula.exception.BusinessException;
import com.nebula.mapper.*;
import com.nebula.service.BlogArticleService;
import com.nebula.vo.ArticleListVO;
import com.nebula.vo.ArticleVO;
import com.nebula.vo.TagVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
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
    private final BlogArticleTagMapper articleTagMapper;
    private final BlogArticleLikeMapper articleLikeMapper;
    private final BlogArticleCollectMapper articleCollectMapper;
    private final BlogViewHistoryMapper viewHistoryMapper;
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
        article.setUserId(userId);
        article.setViewCount(0);
        article.setLikeCount(0);
        article.setCommentCount(0);
        article.setCollectCount(0);
        article.setVersion(1);
        article.setAuditStatus(1); // 默认审核通过
        
        articleMapper.insert(article);

        // 保存文章标签关联
        if (articleDTO.getTagIds() != null && !articleDTO.getTagIds().isEmpty()) {
            for (Long tagId : articleDTO.getTagIds()) {
                BlogArticleTag articleTag = new BlogArticleTag();
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
        Long userId = StpUtil.getLoginIdAsLong();

        // 查询文章
        BlogArticle article = articleMapper.selectById(articleDTO.getId());
        if (article == null) {
            throw new BusinessException("文章不存在");
        }

        // 校验权限
        if (!article.getUserId().equals(userId)) {
            throw new BusinessException("无权修改此文章");
        }

        // 更新文章
        BeanUtils.copyProperties(articleDTO, article);
        article.setVersion(article.getVersion() + 1);
        articleMapper.updateById(article);

        // 删除旧的标签关联
        LambdaQueryWrapper<BlogArticleTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BlogArticleTag::getArticleId, article.getId());
        articleTagMapper.delete(wrapper);

        // 保存新的标签关联
        if (articleDTO.getTagIds() != null && !articleDTO.getTagIds().isEmpty()) {
            for (Long tagId : articleDTO.getTagIds()) {
                BlogArticleTag articleTag = new BlogArticleTag();
                articleTag.setArticleId(article.getId());
                articleTag.setTagId(tagId);
                articleTagMapper.insert(articleTag);
            }
        }
    }

    @Override
    public void deleteArticle(Long id) {
        Long userId = StpUtil.getLoginIdAsLong();

        // 查询文章
        BlogArticle article = articleMapper.selectById(id);
        if (article == null) {
            throw new BusinessException("文章不存在");
        }

        // 校验权限
        if (!article.getUserId().equals(userId)) {
            throw new BusinessException("无权删除此文章");
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
        SysUser author = userMapper.selectById(article.getUserId());
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
        LambdaQueryWrapper<BlogArticleTag> tagWrapper = new LambdaQueryWrapper<>();
        tagWrapper.eq(BlogArticleTag::getArticleId, id);
        List<BlogArticleTag> articleTags = articleTagMapper.selectList(tagWrapper);
        
        List<TagVO> tags = new ArrayList<>();
        for (BlogArticleTag articleTag : articleTags) {
            BlogTag tag = tagMapper.selectById(articleTag.getTagId());
            if (tag != null) {
                TagVO tagVO = new TagVO();
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
        wrapper.eq(BlogArticle::getAuditStatus, 1);
        
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
            LambdaQueryWrapper<BlogArticleTag> tagWrapper = new LambdaQueryWrapper<>();
            tagWrapper.eq(BlogArticleTag::getTagId, tagId);
            List<BlogArticleTag> articleTags = articleTagMapper.selectList(tagWrapper);
            List<Long> articleIds = articleTags.stream()
                    .map(BlogArticleTag::getArticleId)
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
        
        // 转换为VO
        Page<ArticleListVO> voPage = new Page<>(current, size);
        voPage.setTotal(articlePage.getTotal());
        List<ArticleListVO> voList = articlePage.getRecords().stream().map(this::convertToListVO).collect(Collectors.toList());
        voPage.setRecords(voList);
        
        return voPage;
    }

    @Override
    public List<ArticleListVO> getHotArticles(Integer limit) {
        LambdaQueryWrapper<BlogArticle> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BlogArticle::getIsDraft, 0);
        wrapper.eq(BlogArticle::getAuditStatus, 1);
        wrapper.orderByDesc(BlogArticle::getViewCount);
        wrapper.last("LIMIT " + limit);
        
        List<BlogArticle> articles = articleMapper.selectList(wrapper);
        return articles.stream().map(this::convertToListVO).collect(Collectors.toList());
    }

    @Override
    public List<ArticleListVO> getRecommendArticles(Integer limit) {
        LambdaQueryWrapper<BlogArticle> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BlogArticle::getIsDraft, 0);
        wrapper.eq(BlogArticle::getAuditStatus, 1);
        wrapper.eq(BlogArticle::getIsTop, 1);
        wrapper.orderByDesc(BlogArticle::getCreateTime);
        wrapper.last("LIMIT " + limit);
        
        List<BlogArticle> articles = articleMapper.selectList(wrapper);
        return articles.stream().map(this::convertToListVO).collect(Collectors.toList());
    }

    @Override
    public Page<ArticleListVO> getMyArticles(Long current, Long size) {
        Long userId = StpUtil.getLoginIdAsLong();
        
        Page<BlogArticle> page = new Page<>(current, size);
        LambdaQueryWrapper<BlogArticle> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BlogArticle::getUserId, userId);
        wrapper.orderByDesc(BlogArticle::getCreateTime);
        
        Page<BlogArticle> articlePage = articleMapper.selectPage(page, wrapper);
        
        // 转换为VO
        Page<ArticleListVO> voPage = new Page<>(current, size);
        voPage.setTotal(articlePage.getTotal());
        List<ArticleListVO> voList = articlePage.getRecords().stream().map(this::convertToListVO).collect(Collectors.toList());
        voPage.setRecords(voList);
        
        return voPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void likeArticle(Long articleId) {
        Long userId = StpUtil.getLoginIdAsLong();

        // 查询是否已点赞
        LambdaQueryWrapper<BlogArticleLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BlogArticleLike::getArticleId, articleId)
                .eq(BlogArticleLike::getUserId, userId);
        BlogArticleLike existLike = articleLikeMapper.selectOne(wrapper);

        if (existLike != null) {
            // 取消点赞
            articleLikeMapper.deleteById(existLike.getId());
            // 更新文章点赞数
            BlogArticle article = articleMapper.selectById(articleId);
            article.setLikeCount(Math.max(0, article.getLikeCount() - 1));
            articleMapper.updateById(article);
        } else {
            // 点赞
            BlogArticleLike like = new BlogArticleLike();
            like.setArticleId(articleId);
            like.setUserId(userId);
            articleLikeMapper.insert(like);
            // 更新文章点赞数
            BlogArticle article = articleMapper.selectById(articleId);
            article.setLikeCount(article.getLikeCount() + 1);
            articleMapper.updateById(article);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void collectArticle(Long articleId) {
        Long userId = StpUtil.getLoginIdAsLong();

        // 查询是否已收藏
        LambdaQueryWrapper<BlogArticleCollect> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BlogArticleCollect::getArticleId, articleId)
                .eq(BlogArticleCollect::getUserId, userId);
        BlogArticleCollect existCollect = articleCollectMapper.selectOne(wrapper);

        if (existCollect != null) {
            // 取消收藏
            articleCollectMapper.deleteById(existCollect.getId());
            // 更新文章收藏数
            BlogArticle article = articleMapper.selectById(articleId);
            article.setCollectCount(Math.max(0, article.getCollectCount() - 1));
            articleMapper.updateById(article);
        } else {
            // 收藏
            BlogArticleCollect collect = new BlogArticleCollect();
            collect.setArticleId(articleId);
            collect.setUserId(userId);
            articleCollectMapper.insert(collect);
            // 更新文章收藏数
            BlogArticle article = articleMapper.selectById(articleId);
            article.setCollectCount(article.getCollectCount() + 1);
            articleMapper.updateById(article);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordView(Long articleId) {
        // 记录浏览历史
        BlogViewHistory viewHistory = new BlogViewHistory();
        viewHistory.setArticleId(articleId);
        
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            viewHistory.setUserId(userId);
        } catch (Exception e) {
            // 未登录用户
            viewHistory.setUserId(null);
        }
        
        viewHistoryMapper.insert(viewHistory);

        // 更新文章浏览量
        BlogArticle article = articleMapper.selectById(articleId);
        if (article != null) {
            article.setViewCount(article.getViewCount() + 1);
            articleMapper.updateById(article);
        }
    }

    /**
     * 转换为列表VO
     */
    private ArticleListVO convertToListVO(BlogArticle article) {
        ArticleListVO vo = new ArticleListVO();
        BeanUtils.copyProperties(article, vo);

        // 查询作者信息
        SysUser author = userMapper.selectById(article.getUserId());
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
}
