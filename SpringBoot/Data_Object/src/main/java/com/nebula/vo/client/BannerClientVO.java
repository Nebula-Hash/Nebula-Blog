package com.nebula.vo.client;

import lombok.Data;

/**
 * 轮播图VO
 *
 * @author Nebula-Hash
 * @date 2026/1/22
 */
@Data
public class BannerClientVO {

    /**
     * 轮播图ID
     */
    private Long id;

    /**
     * 标题
     */
    private String title;

    /**
     * 图片URL
     */
    private String imageUrl;

    /**
     * 链接URL
     */
    private String linkUrl;
}
