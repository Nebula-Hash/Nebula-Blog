package com.nebula.controller.admin;

import com.nebula.controller.config.AdminController;
import com.nebula.dto.TagDTO;
import com.nebula.result.Result;
import com.nebula.service.tag.BlogTagService;
import com.nebula.vo.TagAdminVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 标签控制器（管理端）
 *
 * @author Nebula-Hash
 * @date 2026/1/26
 */
@AdminController
@RequestMapping("/tag")
@RequiredArgsConstructor
public class AdminTagController {

    private final BlogTagService tagService;

    /**
     * 获取所有标签
     */
    @GetMapping("/list")
    public Result<List<TagAdminVO>> getAllTags() {
        List<TagAdminVO> tags = tagService.getAllTagsForAdmin();
        return Result.success(tags);
    }

    /**
     * 创建标签
     */
    @PostMapping("/create")
    public Result<Long> createTag(@Valid @RequestBody TagDTO tagDTO) {
        Long tagId = tagService.createTag(tagDTO);
        return Result.success("创建成功", tagId);
    }

    /**
     * 更新标签
     */
    @PutMapping("/update")
    public Result<String> updateTag(@Valid @RequestBody TagDTO tagDTO) {
        tagService.updateTag(tagDTO);
        return Result.success("更新成功");
    }

    /**
     * 删除标签
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteTag(@PathVariable Long id) {
        tagService.deleteTag(id);
        return Result.success("删除成功");
    }
}
