package com.nebula.controller.admin;

import com.nebula.controller.config.AdminController;
import com.nebula.dto.CategoryDTO;
import com.nebula.result.Result;
import com.nebula.service.category.BlogCategoryService;
import com.nebula.vo.CategoryAdminVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 分类控制器（管理端）
 *
 * @author Nebula-Hash
 * @date 2026/1/25
 */
@AdminController
@RequestMapping("/category")
@RequiredArgsConstructor
public class AdminCategoryController {

    private final BlogCategoryService categoryService;

    /**
     * 获取所有分类
     */
    @GetMapping("/list")
    public Result<List<CategoryAdminVO>> getAllCategories() {
        List<CategoryAdminVO> categories = categoryService.getAllCategoriesForAdmin();
        return Result.success(categories);
    }

    /**
     * 创建分类
     */
    @PostMapping("/create")
    public Result<Long> createCategory(@Valid @RequestBody CategoryDTO categoryDTO) {
        Long categoryId = categoryService.createCategory(categoryDTO);
        return Result.success("创建成功", categoryId);
    }

    /**
     * 更新分类
     */
    @PutMapping("/update")
    public Result<String> updateCategory(@Valid @RequestBody CategoryDTO categoryDTO) {
        categoryService.updateCategory(categoryDTO);
        return Result.success("更新成功");
    }

    /**
     * 删除分类
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return Result.success("删除成功");
    }
}
