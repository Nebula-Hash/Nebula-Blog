package com.nebula.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nebula.dto.CategoryDTO;
import com.nebula.entity.BlogArticle;
import com.nebula.entity.BlogCategory;
import com.nebula.exception.BusinessException;
import com.nebula.mapper.BlogArticleMapper;
import com.nebula.mapper.BlogCategoryMapper;
import com.nebula.service.BlogCategoryService;
import com.nebula.vo.CategoryAdminVO;
import com.nebula.vo.CategoryClientVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
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

        // 检查名称是否与其他分类重复
        if (categoryDTO.getCategoryName() != null 
                && !categoryDTO.getCategoryName().equals(category.getCategoryName())) {
            LambdaQueryWrapper<BlogCategory> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(BlogCategory::getCategoryName, categoryDTO.getCategoryName());
            wrapper.ne(BlogCategory::getId, categoryDTO.getId());
            Long count = categoryMapper.selectCount(wrapper);
            if (count > 0) {
                throw new BusinessException("分类名称已存在");
            }
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
    public List<CategoryAdminVO> getAllCategoriesForAdmin() {
        LambdaQueryWrapper<BlogCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(BlogCategory::getSort);
        List<BlogCategory> categories = categoryMapper.selectList(wrapper);

        // 一次性查询所有分类的文章数量，解决N+1查询问题
        Map<Long, Long> articleCountMap = getArticleCountByCategoryIds(
                categories.stream().map(BlogCategory::getId).collect(Collectors.toList())
        );

        return categories.stream().map(category -> {
            CategoryAdminVO vo = new CategoryAdminVO();
            BeanUtils.copyProperties(category, vo);
            vo.setArticleCount(articleCountMap.getOrDefault(category.getId(), 0L));
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public List<CategoryClientVO> getAllCategoriesForClient() {
        LambdaQueryWrapper<BlogCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(BlogCategory::getSort);
        List<BlogCategory> categories = categoryMapper.selectList(wrapper);

        // 一次性查询所有分类的文章数量，解决N+1查询问题
        Map<Long, Long> articleCountMap = getArticleCountByCategoryIds(
                categories.stream().map(BlogCategory::getId).collect(Collectors.toList())
        );

        return categories.stream().map(category -> {
            CategoryClientVO vo = new CategoryClientVO();
            vo.setId(category.getId());
            vo.setCategoryName(category.getCategoryName());
            vo.setArticleCount(articleCountMap.getOrDefault(category.getId(), 0L));
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public CategoryClientVO getCategoryById(Long id) {
        BlogCategory category = categoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }
        CategoryClientVO vo = new CategoryClientVO();
        vo.setId(category.getId());
        vo.setCategoryName(category.getCategoryName());
        // 获取该分类下的文章数量
        Map<Long, Long> articleCountMap = getArticleCountByCategoryIds(List.of(id));
        vo.setArticleCount(articleCountMap.getOrDefault(id, 0L));
        return vo;
    }

    /**
     * 批量查询分类下的文章数量
     *
     * @param categoryIds 分类ID列表
     * @return Map<分类ID, 文章数量>
     */
    private Map<Long, Long> getArticleCountByCategoryIds(List<Long> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return Map.of();
        }

        // 查询已发布文章
        LambdaQueryWrapper<BlogArticle> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(BlogArticle::getCategoryId, categoryIds);
        wrapper.eq(BlogArticle::getIsDraft, 0);
        wrapper.select(BlogArticle::getCategoryId);

        List<BlogArticle> articles = articleMapper.selectList(wrapper);

        // 按分类ID分组统计数量
        return articles.stream()
                .collect(Collectors.groupingBy(
                        BlogArticle::getCategoryId,
                        Collectors.counting()
                ));
    }
}
