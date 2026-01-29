package com.nebula.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nebula.dto.BannerDTO;
import com.nebula.entity.BlogBanner;
import com.nebula.enumeration.StatusEnum;
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
        wrapper.eq(BlogBanner::getStatus, StatusEnum.ENABLED.getCode());
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

        // 如果图片是临时文件，转为正式文件
        String imageUrl = bannerDTO.getImageUrl();
        if (fileUploadUtil.isTempFile(imageUrl)) {
            imageUrl = fileUploadUtil.moveToFormal(imageUrl);
            banner.setImageUrl(imageUrl);
        }

        // 设置默认值
        if (banner.getSort() == null) {
            banner.setSort(0);
        }
        if (banner.getStatus() == null) {
            banner.setStatus(StatusEnum.ENABLED.getCode());
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

        String newImageUrl = bannerDTO.getImageUrl();
        String oldImageUrl = existBanner.getImageUrl();

        // 判断图片是否变更
        if (StringUtils.hasText(newImageUrl) && !newImageUrl.equals(oldImageUrl)) {
            // 如果新图片是临时文件，说明用户更换了图片
            if (fileUploadUtil.isTempFile(newImageUrl)) {
                // 新图片转正
                newImageUrl = fileUploadUtil.moveToFormal(newImageUrl);
                bannerDTO.setImageUrl(newImageUrl);

                // 旧图片移至临时目录等待清理
                if (StringUtils.hasText(oldImageUrl)) {
                    try {
                        fileUploadUtil.moveToTemp(oldImageUrl);
                    } catch (Exception e) {
                        // 移动失败不影响更新操作
                    }
                }
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
        // 将正式图片移至临时目录，等待定时任务清理
        try {
            if (StringUtils.hasText(banner.getImageUrl())) {
                fileUploadUtil.moveToTemp(banner.getImageUrl());
            }
        } catch (Exception e) {
            // 移动文件失败不影响删除操作
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
