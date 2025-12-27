package com.nebula.boot4.controller;

import com.nebula.boot4.common.Result;
import com.nebula.boot4.service.BlogBannerService;
import com.nebula.boot4.vo.BannerVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 轮播图控制器
 *
 * @author Nebula-Hash
 * @date 2025/11/25
 */
@RestController
@RequestMapping("/banner")
@RequiredArgsConstructor
public class BannerController {

    private final BlogBannerService bannerService;

    /**
     * 获取轮播图列表
     */
    @GetMapping("/list")
    public Result<List<BannerVO>> getActiveBanners() {
        List<BannerVO> banners = bannerService.getActiveBanners();
        return Result.success(banners);
    }
}
