package com.nebula.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 文章发布/编辑DTO
 *
 * @author Nebula-Hash
 * @date 2026/1/22
 */
@Data
public class ArticleDTO {

    /**
     * 文章ID (编辑时传)
     */
    private Long id;

    /**
     * 文章标题
     */
    @NotBlank(message = "文章标题不能为空")
    private String title;

    /**
     * 文章摘要
     */
    private String summary;

    /**
     * 封面图
     */
    private String coverImage;

    /**
     * 文章内容(Markdown)
     */
    @NotBlank(message = "文章内容不能为空")
    private String content;

    /**
     * 分类ID
     */
    @NotNull(message = "文章分类不能为空")
    private Long categoryId;

    /**
     * 标签ID列表
     */
    private List<Long> tagIds;

    /**
     * 是否置顶 0-否 1-是
     */
    private Integer isTop;

    /**
     * 是否草稿 0-否 1-是
     */
    private Integer isDraft;
}
