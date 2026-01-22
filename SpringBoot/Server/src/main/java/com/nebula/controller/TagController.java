package com.nebula.controller;

import com.nebula.result.Result;
import com.nebula.dto.TagDTO;
import com.nebula.service.BlogTagService;
import com.nebula.vo.TagVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 标签控制器
 *
 * @author Nebula-Hash
 * @date 2026/1/22
 */
@RestController
@RequestMapping("/tag")
@RequiredArgsConstructor
public class TagController {

    private final BlogTagService tagService;

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

    /**
     * 获取所有标签
     */
    @GetMapping("/list")
    public Result<List<TagVO>> getAllTags() {
        List<TagVO> tags = tagService.getAllTags();
        return Result.success(tags);
    }
}
