package com.nebula.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 标签VO
 *
 * @author Nebula-Hash
 * @date 2026/1/22
 */
@Data
public class TagVO {

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

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
