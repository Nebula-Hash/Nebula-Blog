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
import com.nebula.vo.ArticleListVO;
import com.nebula.vo.ArticleVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
public class BlogArticleServiceImpl implements BlogArticleService {

    private final BlogArticleMapper articleMapper;
    private final BlogArticleLikeMapper articleLikeMapper;
    private final BlogArticleCollectMapper articleCollectMapper;
    private final BlogCategoryMapper categoryMapper;
    private final BlogTagMapper tagMapper;
    private final RelevancyArticleTagMapper articleTagMapper;

    // Helper组件
    private final ArticleQueryHelper queryHelper;
    private final ArticleConverter converter;
    private final ArticleInteractionHelper interactionHelper;
    private final MarkdownHelper markdownHelper;
    private final HotArticleScoreHelper hotArticleScoreHelper;

    // 文件上传工具
    private final com.nebula.upload.FileUploadUtil fileUploadUtil;

    // ==================== 文章CRUD ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long publishArticle(ArticleDTO articleDTO) {
        Long userId = StpUtil.getLoginIdAsLong();

        // 校验分类是否存在
        validateCategory(articleDTO.getCategoryId());

        // 创建文章
        BlogArticle article = new BlogArticle();
        BeanUtils.copyProperties(articleDTO, article);
        article.setAuthorId(userId);
        
        // 处理封面图：如果是临时文件，转为正式文件
        String coverImage = articleDTO.getCoverImage();
        if (org.springframework.util.StringUtils.hasText(coverImage) && fileUploadUtil.isTempFile(coverImage)) {
            coverImage = fileUploadUtil.moveToFormal(coverImage);
            article.setCoverImage(coverImage);
        }
        
        // 生成 HTML 内容
        article.setHtmlContent(markdownHelper.toHtml(articleDTO.getContent()));
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

        // 校验分类是否存在
        validateCategory(articleDTO.getCategoryId());

        // 保存旧的封面图URL
        String oldCoverImage = article.getCoverImage();
        String newCoverImage = articleDTO.getCoverImage();

        // 更新文章
        BeanUtils.copyProperties(articleDTO, article);
        
        // 处理封面图变更
        if (org.springframework.util.StringUtils.hasText(newCoverImage) && !newCoverImage.equals(oldCoverImage)) {
            // 如果新封面图是临时文件，说明用户更换了封面图
            if (fileUploadUtil.isTempFile(newCoverImage)) {
                // 新封面图转正
                newCoverImage = fileUploadUtil.moveToFormal(newCoverImage);
                article.setCoverImage(newCoverImage);

                // 旧封面图移至临时目录等待清理
                if (org.springframework.util.StringUtils.hasText(oldCoverImage)) {
                    try {
                        fileUploadUtil.moveToTemp(oldCoverImage);
                    } catch (Exception e) {
                        // 移动失败不影响更新操作
                    }
                }
            }
        }
        
        // 重新生成 HTML 内容
        article.setHtmlContent(markdownHelper.toHtml(articleDTO.getContent()));
        articleMapper.updateById(article);

        // 删除旧的标签关联
        LambdaQueryWrapper<RelevancyArticleTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RelevancyArticleTag::getArticleId, article.getId());
        articleTagMapper.delete(wrapper);

        // 保存新的标签关联
        saveArticleTags(article.getId(), articleDTO.getTagIds());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteArticle(Long id) {
        BlogArticle article = articleMapper.selectById(id);
        if (article == null) {
            throw new BusinessException(ArticleConstants.ERROR_ARTICLE_NOT_FOUND);
        }

        // 删除文章-标签关联
        LambdaQueryWrapper<RelevancyArticleTag> tagWrapper = new LambdaQueryWrapper<>();
        tagWrapper.eq(RelevancyArticleTag::getArticleId, id);
        articleTagMapper.delete(tagWrapper);

        // 删除点赞记录
        LambdaQueryWrapper<BlogArticleLike> likeWrapper = new LambdaQueryWrapper<>();
        likeWrapper.eq(BlogArticleLike::getArticleId, id);
        articleLikeMapper.delete(likeWrapper);

        // 删除收藏记录
        LambdaQueryWrapper<BlogArticleCollect> collectWrapper = new LambdaQueryWrapper<>();
        collectWrapper.eq(BlogArticleCollect::getArticleId, id);
        articleCollectMapper.delete(collectWrapper);

        // 将封面图移至临时目录，等待定时任务清理
        if (org.springframework.util.StringUtils.hasText(article.getCoverImage())) {
            try {
                fileUploadUtil.moveToTemp(article.getCoverImage());
            } catch (Exception e) {
                // 移动文件失败不影响删除操作
            }
        }

        // 删除文章（逻辑删除）
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
    public ArticleVO getClientArticleDetailWithView(Long id) {
        // 先查询文章确认存在
        BlogArticle article = articleMapper.selectById(id);
        if (article == null || DraftStatusEnum.isDraft(article.getIsDraft())) {
            throw new BusinessException(ArticleConstants.ERROR_ARTICLE_NOT_FOUND);
        }

        // 文章存在才更新浏览量
        interactionHelper.incrementViewCount(id);

        // 重新查询获取更新后的数据
        article = articleMapper.selectById(id);
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
        // 查询候选池（按基础互动指标初筛，确保候选池质量）
        Page<BlogArticle> page = new Page<>(1, ArticleConstants.HOT_ARTICLE_CANDIDATE_POOL_SIZE, false);
        LambdaQueryWrapper<BlogArticle> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BlogArticle::getIsDraft, DraftStatusEnum.PUBLISHED.getCode())
                // 按浏览量和点赞数预排序，确保候选池包含热门文章
                .orderByDesc(BlogArticle::getViewCount)
                .orderByDesc(BlogArticle::getLikeCount);

        Page<BlogArticle> articlePage = articleMapper.selectPage(page, wrapper);

        // 使用综合评分算法排序并取前N条
        List<BlogArticle> hotArticles = hotArticleScoreHelper.getTopHotArticles(articlePage.getRecords(), limit);
        return converter.batchToListVO(hotArticles);
    }

    @Override
    public List<ArticleListVO> getRecommendArticles(Integer limit) {
        // 使用分页查询，不查询总数
        Page<BlogArticle> page = new Page<>(1, limit, false);
        LambdaQueryWrapper<BlogArticle> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BlogArticle::getIsDraft, DraftStatusEnum.PUBLISHED.getCode())
                .eq(BlogArticle::getIsTop, TopStatusEnum.TOP.getCode())
                .orderByDesc(BlogArticle::getCreateTime);

        Page<BlogArticle> articlePage = articleMapper.selectPage(page, wrapper);
        return converter.batchToListVO(articlePage.getRecords());
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
     * 保存文章标签关联（带标签有效性校验 + 去重 + 批量插入）
     */
    private void saveArticleTags(Long articleId, List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return;
        }

        // 去重处理，避免重复标签
        List<Long> distinctTagIds = tagIds.stream().distinct().toList();

        // 校验标签是否存在
        List<BlogTag> existingTags = tagMapper.selectBatchIds(distinctTagIds);
        Set<Long> existingTagIds = existingTags.stream()
                .map(BlogTag::getId)
                .collect(Collectors.toSet());

        // 构建批量插入列表
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

        // 批量插入
        if (!articleTagList.isEmpty()) {
            articleTagMapper.batchInsert(articleTagList);
        }
    }

    /**
     * 应用默认排序（置顶优先 + 创建时间倒序）
     */
    private void applyDefaultSort(LambdaQueryWrapper<BlogArticle> wrapper) {
        wrapper.orderByDesc(BlogArticle::getIsTop);
        wrapper.orderByDesc(BlogArticle::getCreateTime);
    }

    /**
     * 校验分类是否存在
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
