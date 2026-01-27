package com.nebula.controller.client;

import com.nebula.controller.config.ClientController;
import com.nebula.result.Result;
import com.nebula.service.BlogCategoryService;
import com.nebula.vo.CategoryClientVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * 分类控制器（客户端）
 *
 * @author Nebula-Hash
 * @date 2026/1/25
 */
@ClientController
@RequestMapping("/category")
@RequiredArgsConstructor
public class ClientCategoryController {

    private final BlogCategoryService categoryService;

    /**
     * 获取所有分类
     */
    @GetMapping("/list")
    public Result<List<CategoryClientVO>> getAllCategories() {
        List<CategoryClientVO> categories = categoryService.getAllCategoriesForClient();
        return Result.success(categories);
    }

    /**
     * 根据ID获取分类详情
     */
    @GetMapping("/detail/{id}")
    public Result<CategoryClientVO> getCategoryById(@PathVariable Long id) {
        CategoryClientVO category = categoryService.getCategoryById(id);
        return Result.success(category);
    }
}
