package com.nebula.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nebula.entity.BlogBanner;
import com.nebula.mapper.BlogBannerMapper;
import com.nebula.service.BlogBannerService;
import com.nebula.vo.BannerVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BlogBannerServiceImpl implements BlogBannerService {

    private final BlogBannerMapper bannerMapper;

    @Override
    public List<BannerVO> getActiveBanners() {
        LambdaQueryWrapper<BlogBanner> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BlogBanner::getStatus, 1);
        wrapper.orderByAsc(BlogBanner::getSort);
        List<BlogBanner> banners = bannerMapper.selectList(wrapper);
        return banners.stream().map(banner -> {
            BannerVO vo = new BannerVO();
            BeanUtils.copyProperties(banner, vo);
            return vo;
        }).collect(Collectors.toList());
    }
}
