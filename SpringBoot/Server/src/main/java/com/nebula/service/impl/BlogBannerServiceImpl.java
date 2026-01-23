package com.nebula.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nebula.dto.BannerDTO;
import com.nebula.entity.BlogBanner;
import com.nebula.exception.BusinessException;
import com.nebula.mapper.BlogBannerMapper;
import com.nebula.service.BlogBannerService;
import com.nebula.upload.FileUploadUtil;
import com.nebula.vo.BannerAdminVO;
import com.nebula.vo.BannerClientVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 轮播图服务实现类
 *
 * @author Nebula-Hash
 * @date 2026/1/22
 */
@Service
@RequiredArgsConstructor
public class BlogBannerServiceImpl implements BlogBannerService {

    private final BlogBannerMapper bannerMapper;
    private final FileUploadUtil fileUploadUtil;

    @Override
    public List<BannerClientVO> getActiveBanners() {
        LambdaQueryWrapper<BlogBanner> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BlogBanner::getStatus, 1);
        wrapper.orderByAsc(BlogBanner::getSort);
        List<BlogBanner> banners = bannerMapper.selectList(wrapper);
        return banners.stream().map(banner -> {
            BannerClientVO vo = new BannerClientVO();
            BeanUtils.copyProperties(banner, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public Page<BannerAdminVO> getBannerList(Long current, Long size) {
        Page<BlogBanner> page = new Page<>(current, size);
        LambdaQueryWrapper<BlogBanner> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(BlogBanner::getSort);
        wrapper.orderByDesc(BlogBanner::getCreateTime);
        Page<BlogBanner> bannerPage = bannerMapper.selectPage(page, wrapper);

        Page<BannerAdminVO> resultPage = new Page<>(bannerPage.getCurrent(), bannerPage.getSize(), bannerPage.getTotal());
        List<BannerAdminVO> records = bannerPage.getRecords().stream().map(banner -> {
            BannerAdminVO vo = new BannerAdminVO();
            BeanUtils.copyProperties(banner, vo);
            return vo;
        }).collect(Collectors.toList());
        resultPage.setRecords(records);
        return resultPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addBanner(BannerDTO bannerDTO) {
        BlogBanner banner = new BlogBanner();
        BeanUtils.copyProperties(bannerDTO, banner);
        // 设置默认值
        if (banner.getSort() == null) {
            banner.setSort(0);
        }
        if (banner.getStatus() == null) {
            banner.setStatus(1);
        }
        bannerMapper.insert(banner);
        return banner.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateBanner(BannerDTO bannerDTO) {
        if (bannerDTO.getId() == null) {
            throw new BusinessException("轮播图ID不能为空");
        }
        BlogBanner existBanner = bannerMapper.selectById(bannerDTO.getId());
        if (existBanner == null) {
            throw new BusinessException("轮播图不存在");
        }

        // 如果图片URL变更，删除旧图片
        if (StringUtils.hasText(bannerDTO.getImageUrl())
                && !bannerDTO.getImageUrl().equals(existBanner.getImageUrl())) {
            try {
                fileUploadUtil.delete(existBanner.getImageUrl());
            } catch (Exception e) {
                // 删除旧文件失败不影响更新操作
            }
        }

        BlogBanner banner = new BlogBanner();
        BeanUtils.copyProperties(bannerDTO, banner);
        bannerMapper.updateById(banner);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBanner(Long id) {
        BlogBanner banner = bannerMapper.selectById(id);
        if (banner == null) {
            throw new BusinessException("轮播图不存在");
        }
        // 删除轮播图记录（逻辑删除）
        bannerMapper.deleteById(id);
        // 删除对应的图片文件
        try {
            fileUploadUtil.delete(banner.getImageUrl());
        } catch (Exception e) {
            // 删除文件失败不影响删除操作
        }
    }

    @Override
    public BannerAdminVO getBannerDetail(Long id) {
        BlogBanner banner = bannerMapper.selectById(id);
        if (banner == null) {
            throw new BusinessException("轮播图不存在");
        }
        BannerAdminVO vo = new BannerAdminVO();
        BeanUtils.copyProperties(banner, vo);
        return vo;
    }
}
