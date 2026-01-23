package com.nebula.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 轮播图DTO
 *
 * @author Nebula-Hash
 * @date 2026/1/23
 */
@Data
public class BannerDTO {

    /**
     * 轮播图ID（编辑时必填）
     */
    private Long id;

    /**
     * 标题
     */
    private String title;

    /**
     * 图片URL
     */
    @NotBlank(message = "图片不能为空")
    private String imageUrl;

    /**
     * 链接URL
     */
    private String linkUrl;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 状态 0-禁用 1-启用
     */
    private Integer status;
}
