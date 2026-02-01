package com.nebula.service.category;

import com.nebula.dto.CategoryDTO;
import com.nebula.vo.CategoryAdminVO;
import com.nebula.vo.CategoryClientVO;

import java.util.List;

/**
 * 分类服务接口
 *
 * @author Nebula-Hash
 * @date 2026/1/25
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
     * 获取所有分类（管理端）
     *
     * @return 分类列表
     */
    List<CategoryAdminVO> getAllCategoriesForAdmin();

    /**
     * 获取所有分类（客户端）
     *
     * @return 分类列表
     */
    List<CategoryClientVO> getAllCategoriesForClient();

    /**
     * 根据ID获取分类详情（客户端）
     *
     * @param id 分类ID
     * @return 分类详情
     */
    CategoryClientVO getCategoryById(Long id);
}
