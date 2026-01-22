package com.nebula.controller;

import com.nebula.result.Result;
import com.nebula.dto.CategoryDTO;
import com.nebula.service.BlogCategoryService;
import com.nebula.vo.CategoryVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 分类控制器
 *
 * @author Nebula-Hash
 * @date 2026/1/22
 */
@RestController
@RequestMapping("/category")
@RequiredArgsConstructor
public class CategoryController {

    private final BlogCategoryService categoryService;

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

    /**
     * 获取所有分类
     */
    @GetMapping("/list")
    public Result<List<CategoryVO>> getAllCategories() {
        List<CategoryVO> categories = categoryService.getAllCategories();
        return Result.success(categories);
    }
}
