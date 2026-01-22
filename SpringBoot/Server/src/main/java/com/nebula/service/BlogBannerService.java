package com.nebula.service;

import com.nebula.vo.BannerVO;

import java.util.List;

/**
 * 轮播图服务接口
 *
 * @author Nebula-Hash
 * @date 2026/1/22
 */
public interface BlogBannerService {

    /**
     * 获取所有启用的轮播图
     *
     * @return 轮播图列表
     */
    List<BannerVO> getActiveBanners();
}
