package com.nebula.controller.client;

import com.nebula.controller.config.ClientController;
import com.nebula.result.Result;
import com.nebula.service.banner.BlogBannerService;
import com.nebula.vo.client.BannerClientVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * 轮播图控制器（客户端）
 *
 * @author Nebula-Hash
 * @date 2026/1/23
 */
@ClientController
@RequestMapping("/banner")
@RequiredArgsConstructor
public class ClientBannerController {

    private final BlogBannerService bannerService;

    /**
     * 获取当前可用轮播图列表
     */
    @GetMapping("/list")
    public Result<List<BannerClientVO>> getActiveBanners() {
        List<BannerClientVO> banners = bannerService.getActiveBanners();
        return Result.success(banners);
    }
}
