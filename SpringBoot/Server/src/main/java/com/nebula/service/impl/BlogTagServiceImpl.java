package com.nebula.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nebula.dto.TagDTO;
import com.nebula.entity.BlogArticleTag;
import com.nebula.entity.BlogTag;
import com.nebula.exception.BusinessException;
import com.nebula.mapper.BlogArticleTagMapper;
import com.nebula.mapper.BlogTagMapper;
import com.nebula.service.BlogTagService;
import com.nebula.vo.TagVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BlogTagServiceImpl implements BlogTagService {

    private final BlogTagMapper tagMapper;
    private final BlogArticleTagMapper articleTagMapper;

    @Override
    public Long createTag(TagDTO tagDTO) {
        LambdaQueryWrapper<BlogTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BlogTag::getTagName, tagDTO.getTagName());
        Long count = tagMapper.selectCount(wrapper);
        if (count > 0) {
            throw new BusinessException("标签名称已存在");
        }
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
        BeanUtils.copyProperties(tagDTO, tag);
        tagMapper.updateById(tag);
    }

    @Override
    public void deleteTag(Long id) {
        LambdaQueryWrapper<BlogArticleTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BlogArticleTag::getTagId, id);
        articleTagMapper.delete(wrapper);
        tagMapper.deleteById(id);
    }

    @Override
    public List<TagVO> getAllTags() {
        List<BlogTag> tags = tagMapper.selectList(null);
        return tags.stream().map(tag -> {
            TagVO vo = new TagVO();
            BeanUtils.copyProperties(tag, vo);
            LambdaQueryWrapper<BlogArticleTag> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(BlogArticleTag::getTagId, tag.getId());
            Long count = articleTagMapper.selectCount(wrapper);
            vo.setArticleCount(count);
            return vo;
        }).collect(Collectors.toList());
    }
}
