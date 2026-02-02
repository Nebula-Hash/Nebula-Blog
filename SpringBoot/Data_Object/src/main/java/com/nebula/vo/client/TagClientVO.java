package com.nebula.vo.client;

import lombok.Data;

/**
 * 标签VO（客户端）
 *
 * @author Nebula-Hash
 * @date 2026/1/26
 */
@Data
public class TagClientVO {

    /**
     * 标签ID
     */
    private Long id;

    /**
     * 标签名称
     */
    private String tagName;

    /**
     * 文章数量
     */
    private Long articleCount;
}
