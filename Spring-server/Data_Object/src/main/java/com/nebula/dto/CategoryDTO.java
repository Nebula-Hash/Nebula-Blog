package com.nebula.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 分类DTO
 *
 * @author Nebula-Hash
 * @date 2026/1/22
 */
@Data
public class CategoryDTO {

    /**
     * 分类ID (编辑时传)
     */
    private Long id;

    /**
     * 分类名称
     */
    @NotBlank(message = "分类名称不能为空")
    private String categoryName;

    /**
     * 分类描述
     */
    private String categoryDesc;

    /**
     * 排序
     */
    private Integer sort;
}
