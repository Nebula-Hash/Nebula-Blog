package com.nebula.service.article.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nebula.constant.ArticleConstants;
import com.nebula.constant.CountConstants;
import com.nebula.dto.ArticleDTO;
import com.nebula.entity.BlogArticle;
import com.nebula.entity.BlogArticleCollect;
import com.nebula.entity.BlogArticleLike;
import com.nebula.entity.BlogCategory;
import com.nebula.entity.BlogTag;
import com.nebula.entity.RelevancyArticleTag;
import com.nebula.enumeration.DraftStatusEnum;
import com.nebula.enumeration.TopStatusEnum;
import com.nebula.exception.BusinessException;
import com.nebula.mapper.BlogArticleCollectMapper;
import com.nebula.mapper.BlogArticleLikeMapper;
import com.nebula.mapper.BlogArticleMapper;
import com.nebula.mapper.BlogCategoryMapper;
import com.nebula.mapper.BlogTagMapper;
import com.nebula.mapper.RelevancyArticleTagMapper;
import com.nebula.service.article.BlogArticleService;
import com.nebula.service.article.converter.ArticleConverter;
import com.nebula.service.article.helper.ArticleInteractionHelper;
import com.nebula.service.article.helper.ArticleQueryHelper;
import com.nebula.service.article.helper.HotArticleScoreHelper;
import com.nebula.service.article.helper.MarkdownHelper;
import com.nebula.service.common.TransactionCompensationHelper;
import com.nebula.upload.FileUploadUtil;
import com.nebula.vo.ArticleListVO;
import com.nebula.vo.ArticleVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 文章服务实现类
 *
 * @author Nebula-Hash
 * @date 2026/1/22
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BlogArticleServiceImpl implements BlogArticleService {

    private final BlogArticleMapper articleMapper;
    private final BlogArticleLikeMapper articleLikeMapper;
    private final BlogArticleCollectMapper articleCollectMapper;
    private final BlogCategoryMapper categoryMapper;
    private final BlogTagMapper tagMapper;
    private final RelevancyArticleTagMapper articleTagMapper;

    /**
     * 查询相关辅助组件
     */
    private final ArticleQueryHelper queryHelper;
    private final ArticleConverter converter;
    private final ArticleInteractionHelper interactionHelper;
    private final MarkdownHelper markdownHelper;
    private final HotArticleScoreHelper hotArticleScoreHelper;

    /**
     * 文件上传工具
     */
    private final FileUploadUtil fileUploadUtil;

    /**
     * 发布文章
     *
     * @param articleDTO 文章参数
     * @return 新文章 ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long publishArticle(ArticleDTO articleDTO) {
        Long userId = StpUtil.getLoginIdAsLong();

        validateCategory(articleDTO.getCategoryId());

        BlogArticle article = new BlogArticle();
        BeanUtils.copyProperties(articleDTO, article);
        article.setAuthorId(userId);

        String coverImage = articleDTO.getCoverImage();
        if (StringUtils.hasText(coverImage) && fileUploadUtil.isTempFile(coverImage)) {
            coverImage = fileUploadUtil.moveToFormal(coverImage);
            article.setCoverImage(coverImage);
            final String movedCoverImage = coverImage;
            TransactionCompensationHelper.registerRollbackAction(
                    "publishArticle-coverImage",
                    () -> fileUploadUtil.moveToTemp(movedCoverImage)
            );
        }

        article.setHtmlContent(markdownHelper.toHtml(articleDTO.getContent()));
        article.setViewCount(CountConstants.INIT_VALUE);
        article.setLikeCount(CountConstants.INIT_VALUE);
        article.setCommentCount(CountConstants.INIT_VALUE);
        article.setCollectCount(CountConstants.INIT_VALUE);

        articleMapper.insert(article);
        saveArticleTags(article.getId(), articleDTO.getTagIds());

        return article.getId();
    }

    /**
     * 更新文章
     *
     * @param articleDTO 文章参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateArticle(ArticleDTO articleDTO) {
        BlogArticle article = articleMapper.selectById(articleDTO.getId());
        if (article == null) {
            throw new BusinessException(ArticleConstants.ERROR_ARTICLE_NOT_FOUND);
        }

        validateCategory(articleDTO.getCategoryId());

        String oldCoverImage = article.getCoverImage();
        String newCoverImage = articleDTO.getCoverImage();

        BeanUtils.copyProperties(articleDTO, article);

        // 封面发生变化时，执行临时文件转正和旧图回收
        if (StringUtils.hasText(newCoverImage) && !newCoverImage.equals(oldCoverImage)) {
            if (fileUploadUtil.isTempFile(newCoverImage)) {
                newCoverImage = fileUploadUtil.moveToFormal(newCoverImage);
                article.setCoverImage(newCoverImage);

                final String movedNewCoverImage = newCoverImage;
                TransactionCompensationHelper.registerRollbackAction(
                        "updateArticle-newCoverImage",
                        () -> fileUploadUtil.moveToTemp(movedNewCoverImage)
                );

                if (StringUtils.hasText(oldCoverImage)) {
                    try {
                        String oldCoverTempUrl = fileUploadUtil.moveToTemp(oldCoverImage);
                        TransactionCompensationHelper.registerRollbackAction(
                                "updateArticle-oldCoverImage",
                                () -> fileUploadUtil.moveToFormal(oldCoverTempUrl)
                        );
                    } catch (Exception e) {
                        log.warn("更新文章时移动旧封面到临时目录失败: url={}", oldCoverImage, e);
                    }
                }
            }
        }

        article.setHtmlContent(markdownHelper.toHtml(articleDTO.getContent()));
        articleMapper.updateById(article);

        LambdaQueryWrapper<RelevancyArticleTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RelevancyArticleTag::getArticleId, article.getId());
        articleTagMapper.delete(wrapper);

        saveArticleTags(article.getId(), articleDTO.getTagIds());
    }

    /**
     * 删除文章
     *
     * @param id 文章 ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteArticle(Long id) {
        BlogArticle article = articleMapper.selectById(id);
        if (article == null) {
            throw new BusinessException(ArticleConstants.ERROR_ARTICLE_NOT_FOUND);
        }

        LambdaQueryWrapper<RelevancyArticleTag> tagWrapper = new LambdaQueryWrapper<>();
        tagWrapper.eq(RelevancyArticleTag::getArticleId, id);
        articleTagMapper.delete(tagWrapper);

        LambdaQueryWrapper<BlogArticleLike> likeWrapper = new LambdaQueryWrapper<>();
        likeWrapper.eq(BlogArticleLike::getArticleId, id);
        articleLikeMapper.delete(likeWrapper);

        LambdaQueryWrapper<BlogArticleCollect> collectWrapper = new LambdaQueryWrapper<>();
        collectWrapper.eq(BlogArticleCollect::getArticleId, id);
        articleCollectMapper.delete(collectWrapper);

        if (StringUtils.hasText(article.getCoverImage())) {
            try {
                String coverTempUrl = fileUploadUtil.moveToTemp(article.getCoverImage());
                TransactionCompensationHelper.registerRollbackAction(
                        "deleteArticle-coverImage",
                        () -> fileUploadUtil.moveToFormal(coverTempUrl)
                );
            } catch (Exception e) {
                log.warn("删除文章后移动封面到临时目录失败: url={}", article.getCoverImage(), e);
            }
        }

        articleMapper.deleteById(id);
    }

    /**
     * 获取客户端文章详情
     *
     * @param id 文章 ID
     * @return 文章详情
     */
    @Override
    public ArticleVO getClientArticleDetail(Long id) {
        BlogArticle article = articleMapper.selectById(id);
        if (article == null || DraftStatusEnum.isDraft(article.getIsDraft())) {
            throw new BusinessException(ArticleConstants.ERROR_ARTICLE_NOT_FOUND);
        }
        return converter.toDetailVO(article);
    }

    /**
     * 获取客户端文章详情并累加浏览量
     *
     * @param id 文章 ID
     * @return 文章详情
     */
    @Override
    public ArticleVO getClientArticleDetailWithView(Long id) {
        BlogArticle article = articleMapper.selectById(id);
        if (article == null || DraftStatusEnum.isDraft(article.getIsDraft())) {
            throw new BusinessException(ArticleConstants.ERROR_ARTICLE_NOT_FOUND);
        }

        interactionHelper.incrementViewCount(id);
        article = articleMapper.selectById(id);
        return converter.toDetailVO(article);
    }

    /**
     * 获取管理端文章详情
     *
     * @param id 文章 ID
     * @return 文章详情
     */
    @Override
    public ArticleVO getAdminArticleDetail(Long id) {
        BlogArticle article = articleMapper.selectById(id);
        if (article == null) {
            throw new BusinessException(ArticleConstants.ERROR_ARTICLE_NOT_FOUND);
        }
        return converter.toDetailVO(article);
    }

    /**
     * 分页查询客户端文章列表
     *
     * @param current 当前页
     * @param size 每页大小
     * @param authorName 作者名称
     * @param title 标题
     * @param categoryId 分类 ID
     * @param categoryName 分类名称
     * @param tagId 标签 ID
     * @param tagName 标签名称
     * @return 文章分页结果
     */
    @Override
    public Page<ArticleListVO> getClientArticleList(Long current,
                                                    Long size,
                                                    String authorName,
                                                    String title,
                                                    Long categoryId,
                                                    String categoryName,
                                                    Long tagId,
                                                    String tagName) {
        Page<BlogArticle> page = new Page<>(current, size);
        LambdaQueryWrapper<BlogArticle> wrapper = new LambdaQueryWrapper<>();

        wrapper.eq(BlogArticle::getIsDraft, DraftStatusEnum.PUBLISHED.getCode());

        if (!queryHelper.applySearchConditions(wrapper, authorName, title, categoryId, categoryName, tagId, tagName)) {
            return new Page<>(current, size);
        }

        applyDefaultSort(wrapper);

        Page<BlogArticle> articlePage = articleMapper.selectPage(page, wrapper);
        return converter.toVOPage(articlePage, current, size);
    }

    /**
     * 分页查询管理端文章列表
     *
     * @param current 当前页
     * @param size 每页大小
     * @param authorName 作者名称
     * @param title 标题
     * @param categoryName 分类名称
     * @param tagName 标签名称
     * @param isDraft 草稿状态
     * @param isTop 置顶状态
     * @return 文章分页结果
     */
    @Override
    public Page<ArticleListVO> getAdminArticleList(Long current,
                                                   Long size,
                                                   String authorName,
                                                   String title,
                                                   String categoryName,
                                                   String tagName,
                                                   Integer isDraft,
                                                   Integer isTop) {
        Page<BlogArticle> page = new Page<>(current, size);
        LambdaQueryWrapper<BlogArticle> wrapper = new LambdaQueryWrapper<>();

        if (isDraft != null) {
            wrapper.eq(BlogArticle::getIsDraft, isDraft);
        }
        if (isTop != null) {
            wrapper.eq(BlogArticle::getIsTop, isTop);
        }

        if (!queryHelper.applySearchConditions(wrapper, authorName, title, null, categoryName, null, tagName)) {
            return new Page<>(current, size);
        }

        applyDefaultSort(wrapper);

        Page<BlogArticle> articlePage = articleMapper.selectPage(page, wrapper);
        return converter.toVOPage(articlePage, current, size);
    }

    /**
     * 获取热门文章列表
     *
     * @param limit 返回数量
     * @return 热门文章列表
     */
    @Override
    public List<ArticleListVO> getHotArticles(Integer limit) {
        Page<BlogArticle> page = new Page<>(1, ArticleConstants.HOT_ARTICLE_CANDIDATE_POOL_SIZE, false);
        LambdaQueryWrapper<BlogArticle> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BlogArticle::getIsDraft, DraftStatusEnum.PUBLISHED.getCode())
                .orderByDesc(BlogArticle::getViewCount)
                .orderByDesc(BlogArticle::getLikeCount);

        Page<BlogArticle> articlePage = articleMapper.selectPage(page, wrapper);

        List<BlogArticle> hotArticles = hotArticleScoreHelper.getTopHotArticles(articlePage.getRecords(), limit);
        return converter.batchToListVO(hotArticles);
    }

    /**
     * 获取推荐文章列表
     *
     * @param limit 返回数量
     * @return 推荐文章列表
     */
    @Override
    public List<ArticleListVO> getRecommendArticles(Integer limit) {
        Page<BlogArticle> page = new Page<>(1, limit, false);
        LambdaQueryWrapper<BlogArticle> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BlogArticle::getIsDraft, DraftStatusEnum.PUBLISHED.getCode())
                .eq(BlogArticle::getIsTop, TopStatusEnum.TOP.getCode())
                .orderByDesc(BlogArticle::getCreateTime);

        Page<BlogArticle> articlePage = articleMapper.selectPage(page, wrapper);
        return converter.batchToListVO(articlePage.getRecords());
    }

    /**
     * 点赞文章
     *
     * @param articleId 文章 ID
     */
    @Override
    public void likeArticle(Long articleId) {
        interactionHelper.toggleLike(articleId);
    }

    /**
     * 收藏文章
     *
     * @param articleId 文章 ID
     */
    @Override
    public void collectArticle(Long articleId) {
        interactionHelper.toggleCollect(articleId);
    }

    /**
     * 增加文章浏览量
     *
     * @param articleId 文章 ID
     */
    @Override
    public void incrementViewCount(Long articleId) {
        interactionHelper.incrementViewCount(articleId);
    }

    /**
     * 保存文章标签关联
     * 包含标签有效性校验、去重和批量插入
     *
     * @param articleId 文章 ID
     * @param tagIds 标签 ID 列表
     */
    private void saveArticleTags(Long articleId, List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return;
        }

        List<Long> distinctTagIds = tagIds.stream().distinct().toList();

        List<BlogTag> existingTags = tagMapper.selectBatchIds(distinctTagIds);
        Set<Long> existingTagIds = existingTags.stream()
                .map(BlogTag::getId)
                .collect(Collectors.toSet());

        List<RelevancyArticleTag> articleTagList = new ArrayList<>();
        for (Long tagId : distinctTagIds) {
            if (!existingTagIds.contains(tagId)) {
                throw new BusinessException(ArticleConstants.ERROR_TAG_NOT_FOUND + ": " + tagId);
            }
            RelevancyArticleTag articleTag = new RelevancyArticleTag();
            articleTag.setArticleId(articleId);
            articleTag.setTagId(tagId);
            articleTagList.add(articleTag);
        }

        if (!articleTagList.isEmpty()) {
            articleTagMapper.batchInsert(articleTagList);
        }
    }

    /**
     * 应用默认排序（置顶优先，其次按创建时间倒序）
     *
     * @param wrapper 查询包装器
     */
    private void applyDefaultSort(LambdaQueryWrapper<BlogArticle> wrapper) {
        wrapper.orderByDesc(BlogArticle::getIsTop);
        wrapper.orderByDesc(BlogArticle::getCreateTime);
    }

    /**
     * 校验分类是否存在
     *
     * @param categoryId 分类 ID
     */
    private void validateCategory(Long categoryId) {
        if (categoryId == null) {
            return;
        }
        BlogCategory category = categoryMapper.selectById(categoryId);
        if (category == null) {
            throw new BusinessException(ArticleConstants.ERROR_CATEGORY_NOT_FOUND);
        }
    }
}
