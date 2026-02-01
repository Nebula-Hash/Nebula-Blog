package com.nebula.service.tag.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nebula.dto.TagDTO;
import com.nebula.entity.BlogArticle;
import com.nebula.entity.RelevancyArticleTag;
import com.nebula.entity.BlogTag;
import com.nebula.exception.BusinessException;
import com.nebula.mapper.BlogArticleMapper;
import com.nebula.mapper.RelevancyArticleTagMapper;
import com.nebula.mapper.BlogTagMapper;
import com.nebula.service.tag.BlogTagService;
import com.nebula.vo.TagAdminVO;
import com.nebula.vo.TagClientVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 标签服务实现类
 *
 * @author Nebula-Hash
 * @date 2026/1/22
 */
@Service
@RequiredArgsConstructor
public class BlogTagServiceImpl implements BlogTagService {

    private final BlogTagMapper tagMapper;
    private final RelevancyArticleTagMapper articleTagMapper;
    private final BlogArticleMapper articleMapper;

    @Override
    public Long createTag(TagDTO tagDTO) {
        checkTagNameExists(tagDTO.getTagName(), null);
        BlogTag tag = new BlogTag();
        BeanUtils.copyProperties(tagDTO, tag);
        tagMapper.insert(tag);
        return tag.getId();
    }

    @Override
    public void updateTag(TagDTO tagDTO) {
        BlogTag tag = tagMapper.selectById(tagDTO.getId());
        if (tag == null) {
            throw new BusinessException("标签不存在");
        }
        // 如果标签名发生变更，检查新名称是否已存在
        if (!tag.getTagName().equals(tagDTO.getTagName())) {
            checkTagNameExists(tagDTO.getTagName(), tagDTO.getId());
        }
        BeanUtils.copyProperties(tagDTO, tag);
        tagMapper.updateById(tag);
    }

    @Override
    public void deleteTag(Long id) {
        LambdaQueryWrapper<RelevancyArticleTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RelevancyArticleTag::getTagId, id);
        articleTagMapper.delete(wrapper);
        tagMapper.deleteById(id);
    }

    @Override
    public List<TagAdminVO> getAllTagsForAdmin() {
        LambdaQueryWrapper<BlogTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(BlogTag::getSort);
        List<BlogTag> tags = tagMapper.selectList(wrapper);
        Map<Long, Long> articleCountMap = getArticleCountMap();
        return tags.stream().map(tag -> {
            TagAdminVO vo = new TagAdminVO();
            BeanUtils.copyProperties(tag, vo);
            vo.setArticleCount(articleCountMap.getOrDefault(tag.getId(), 0L));
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public List<TagClientVO> getAllTagsForClient() {
        LambdaQueryWrapper<BlogTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(BlogTag::getSort);
        List<BlogTag> tags = tagMapper.selectList(wrapper);
        Map<Long, Long> articleCountMap = getArticleCountMap();
        return tags.stream().map(tag -> {
            TagClientVO vo = new TagClientVO();
            BeanUtils.copyProperties(tag, vo);
            vo.setArticleCount(articleCountMap.getOrDefault(tag.getId(), 0L));
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public TagClientVO getTagById(Long id) {
        BlogTag tag = tagMapper.selectById(id);
        if (tag == null) {
            throw new BusinessException("标签不存在");
        }
        TagClientVO vo = new TagClientVO();
        BeanUtils.copyProperties(tag, vo);
        // 获取该标签下的文章数量
        Map<Long, Long> articleCountMap = getArticleCountMap();
        vo.setArticleCount(articleCountMap.getOrDefault(id, 0L));
        return vo;
    }

    /**
     * 检查标签名称是否已存在
     *
     * @param tagName   标签名称
     * @param excludeId 排除的标签ID（更新时排除自身）
     */
    private void checkTagNameExists(String tagName, Long excludeId) {
        LambdaQueryWrapper<BlogTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BlogTag::getTagName, tagName);
        if (excludeId != null) {
            wrapper.ne(BlogTag::getId, excludeId);
        }
        Long count = tagMapper.selectCount(wrapper);
        if (count > 0) {
            throw new BusinessException("标签名称已存在");
        }
    }

    /**
     * 获取所有标签的文章数量映射（解决N+1查询问题）
     * 只统计已发布的文章（过滤草稿）
     *
     * @return Map<tagId, articleCount>
     */
    private Map<Long, Long> getArticleCountMap() {
        // 查询所有已发布文章的ID
        LambdaQueryWrapper<BlogArticle> articleWrapper = new LambdaQueryWrapper<>();
        articleWrapper.eq(BlogArticle::getIsDraft, 0);
        articleWrapper.select(BlogArticle::getId);
        List<BlogArticle> publishedArticles = articleMapper.selectList(articleWrapper);
        
        if (publishedArticles.isEmpty()) {
            return Map.of();
        }
        
        Set<Long> publishedArticleIds = publishedArticles.stream()
                .map(BlogArticle::getId)
                .collect(Collectors.toSet());
        
        // 查询文章-标签关联，过滤出已发布文章的关联
        List<RelevancyArticleTag> articleTags = articleTagMapper.selectList(null);
        return articleTags.stream()
                .filter(at -> publishedArticleIds.contains(at.getArticleId()))
                .collect(Collectors.groupingBy(
                        RelevancyArticleTag::getTagId,
                        Collectors.counting()
                ));
    }
}
