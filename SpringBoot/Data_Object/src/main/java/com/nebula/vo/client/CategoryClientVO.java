package com.nebula.vo.client;

import lombok.Data;

/**
 * 分类VO（客户端）
 *
 * @author Nebula-Hash
 * @date 2026/1/25
 */
@Data
public class CategoryClientVO {

    /**
     * 分类ID
     */
    private Long id;

    /**
     * 分类名称
     */
    private String categoryName;

    /**
     * 文章数量
     */
    private Long articleCount;
}
