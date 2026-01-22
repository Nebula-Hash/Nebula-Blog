package com.nebula.service;

import com.nebula.dto.CategoryDTO;
import com.nebula.vo.CategoryVO;

import java.util.List;

/**
 * 分类服务接口
 *
 * @author Nebula-Hash
 * @date 2026/1/22
 */
public interface BlogCategoryService {

    /**
     * 创建分类
     *
     * @param categoryDTO 分类信息
     * @return 分类ID
     */
    Long createCategory(CategoryDTO categoryDTO);

    /**
     * 更新分类
     *
     * @param categoryDTO 分类信息
     */
    void updateCategory(CategoryDTO categoryDTO);

    /**
     * 删除分类
     *
     * @param id 分类ID
     */
    void deleteCategory(Long id);

    /**
     * 获取所有分类
     *
     * @return 分类列表
     */
    List<CategoryVO> getAllCategories();
}
