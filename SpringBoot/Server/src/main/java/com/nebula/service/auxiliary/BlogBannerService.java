package com.nebula.service.auxiliary;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nebula.dto.BannerDTO;
import com.nebula.vo.BannerAdminVO;
import com.nebula.vo.BannerClientVO;

import java.util.List;

/**
 * 轮播图服务接口
 *
 * @author Nebula-Hash
 * @date 2026/1/22
 */
public interface BlogBannerService {

    /**
     * 获取所有启用的轮播图（客户端）
     *
     * @return 轮播图列表
     */
    List<BannerClientVO> getActiveBanners();

    /**
     * 分页查询轮播图列表（管理端）
     *
     * @param current 当前页
     * @param size    每页大小
     * @return 轮播图分页列表
     */
    Page<BannerAdminVO> getBannerList(Long current, Long size);

    /**
     * 获取轮播图详情（管理端）
     *
     * @param id 轮播图ID
     * @return 轮播图详情
     */
    BannerAdminVO getBannerDetail(Long id);


    /**
     * 添加轮播图
     *
     * @param bannerDTO 轮播图信息
     * @return 轮播图ID
     */
    Long addBanner(BannerDTO bannerDTO);

    /**
     * 编辑轮播图
     *
     * @param bannerDTO 轮播图信息
     */
    void updateBanner(BannerDTO bannerDTO);

    /**
     * 删除轮播图
     *
     * @param id 轮播图ID
     */
    void deleteBanner(Long id);
}
