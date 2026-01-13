package com.nebula.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nebula.dto.CategoryDTO;
import com.nebula.entity.BlogArticle;
import com.nebula.entity.BlogCategory;
import com.nebula.exception.BusinessException;
import com.nebula.mapper.BlogArticleMapper;
import com.nebula.mapper.BlogCategoryMapper;
import com.nebula.service.BlogCategoryService;
import com.nebula.vo.CategoryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BlogCategoryServiceImpl implements BlogCategoryService {

    private final BlogCategoryMapper categoryMapper;
    private final BlogArticleMapper articleMapper;

    @Override
    public Long createCategory(CategoryDTO categoryDTO) {
        LambdaQueryWrapper<BlogCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BlogCategory::getCategoryName, categoryDTO.getCategoryName());
        Long count = categoryMapper.selectCount(wrapper);
        if (count > 0) {
            throw new BusinessException("分类名称已存在");
        }
        BlogCategory category = new BlogCategory();
        BeanUtils.copyProperties(categoryDTO, category);
        categoryMapper.insert(category);
        return category.getId();
    }

    @Override
    public void updateCategory(CategoryDTO categoryDTO) {
        BlogCategory category = categoryMapper.selectById(categoryDTO.getId());
        if (category == null) {
            throw new BusinessException("分类不存在");
        }
        BeanUtils.copyProperties(categoryDTO, category);
        categoryMapper.updateById(category);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCategory(Long id) {
        LambdaQueryWrapper<BlogArticle> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BlogArticle::getCategoryId, id);
        Long count = articleMapper.selectCount(wrapper);
        if (count > 0) {
            throw new BusinessException("该分类下还有文章,无法删除");
        }
        categoryMapper.deleteById(id);
    }

    @Override
    public List<CategoryVO> getAllCategories() {
        LambdaQueryWrapper<BlogCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(BlogCategory::getSort);
        List<BlogCategory> categories = categoryMapper.selectList(wrapper);
        return categories.stream().map(category -> {
            CategoryVO vo = new CategoryVO();
            BeanUtils.copyProperties(category, vo);
            LambdaQueryWrapper<BlogArticle> articleWrapper = new LambdaQueryWrapper<>();
            articleWrapper.eq(BlogArticle::getCategoryId, category.getId());
            articleWrapper.eq(BlogArticle::getIsDraft, 0);
            Long count = articleMapper.selectCount(articleWrapper);
            vo.setArticleCount(count);
            return vo;
        }).collect(Collectors.toList());
    }
}
