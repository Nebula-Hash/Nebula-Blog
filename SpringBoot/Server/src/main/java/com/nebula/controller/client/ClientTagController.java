package com.nebula.controller.client;

import com.nebula.controller.config.ClientController;
import com.nebula.result.Result;
import com.nebula.service.auxiliary.BlogTagService;
import com.nebula.vo.TagClientVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * 标签控制器（客户端）
 *
 * @author Nebula-Hash
 * @date 2026/1/26
 */
@ClientController
@RequestMapping("/tag")
@RequiredArgsConstructor
public class ClientTagController {

    private final BlogTagService tagService;

    /**
     * 获取所有标签
     */
    @GetMapping("/list")
    public Result<List<TagClientVO>> getAllTags() {
        List<TagClientVO> tags = tagService.getAllTagsForClient();
        return Result.success(tags);
    }

    /**
     * 根据ID获取标签详情
     */
    @GetMapping("/detail/{id}")
    public Result<TagClientVO> getTagById(@PathVariable Long id) {
        TagClientVO tag = tagService.getTagById(id);
        return Result.success(tag);
    }
}
