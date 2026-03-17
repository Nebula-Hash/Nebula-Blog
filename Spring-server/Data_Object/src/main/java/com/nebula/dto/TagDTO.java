package com.nebula.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 标签DTO
 *
 * @author Nebula-Hash
 * @date 2026/1/22
 */
@Data
public class TagDTO {

    /**
     * 标签ID (编辑时传)
     */
    private Long id;

    /**
     * 标签名称
     */
    @NotBlank(message = "标签名称不能为空")
    private String tagName;

    /**
     * 排序
     */
    private Integer sort;
}
