package com.nebula.service.banner.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nebula.dto.BannerDTO;
import com.nebula.entity.BlogArticle;
import com.nebula.entity.BlogBanner;
import com.nebula.enumeration.StatusEnum;
import com.nebula.exception.BusinessException;
import com.nebula.mapper.BlogArticleMapper;
import com.nebula.mapper.BlogBannerMapper;
import com.nebula.service.banner.BlogBannerService;
import com.nebula.service.common.TransactionCompensationHelper;
import com.nebula.upload.FileUploadUtil;
import com.nebula.vo.admin.BannerAdminVO;
import com.nebula.vo.client.BannerClientVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 轮播图服务实现类
 *
 * @author Nebula-Hash
 * @date 2026/1/23
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BlogBannerServiceImpl implements BlogBannerService {

    private final BlogBannerMapper bannerMapper;
    private final BlogArticleMapper articleMapper;
    private final FileUploadUtil fileUploadUtil;

    /**
     * 获取前台可展示轮播图列表
     *
     * @return 已启用轮播图列表
     */
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

    /**
     * 分页查询轮播图列表（管理端）
     *
     * @param current 当前页
     * @param size 每页大小
     * @return 分页结果
     */
    @Override
    public Page<BannerAdminVO> getBannerList(Long current, Long size) {
        List<BannerAdminVO> allBanners = bannerMapper.selectBannerListWithArticle();
        int start = (int) ((current - 1) * size);
        int end = Math.min(start + size.intValue(), allBanners.size());
        List<BannerAdminVO> pageRecords = allBanners.subList(start, end);

        Page<BannerAdminVO> resultPage = new Page<>(current, size, allBanners.size());
        resultPage.setRecords(pageRecords);
        return resultPage;
    }

    /**
     * 获取轮播图详情
     *
     * @param id 轮播图 ID
     * @return 轮播图详情
     */
    @Override
    public BannerAdminVO getBannerDetail(Long id) {
        BannerAdminVO banner = bannerMapper.selectBannerDetailWithArticle(id);
        if (banner == null) {
            throw new BusinessException("轮播图不存在");
        }
        return banner;
    }

    /**
     * 获取可关联的已发布文章列表
     *
     * @return 文章列表（ID + 标题）
     */
    @Override
    public List<Map<String, Object>> getPublishedArticles() {
        LambdaQueryWrapper<BlogArticle> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BlogArticle::getIsDraft, 0);
        wrapper.select(BlogArticle::getId, BlogArticle::getTitle);
        wrapper.orderByDesc(BlogArticle::getCreateTime);

        List<BlogArticle> articles = articleMapper.selectList(wrapper);
        return articles.stream().map(article -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", article.getId());
            map.put("title", article.getTitle());
            return map;
        }).collect(Collectors.toList());
    }

    /**
     * 新增轮播图
     *
     * @param bannerDTO 轮播图参数
     * @return 新增轮播图 ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addBanner(BannerDTO bannerDTO) {
        BlogArticle article = articleMapper.selectById(bannerDTO.getArticleId());
        if (article == null) {
            throw new BusinessException("关联的文章不存在");
        }
        if (article.getIsDraft() == 1) {
            throw new BusinessException("不能关联草稿文章");
        }

        BlogBanner banner = new BlogBanner();
        BeanUtils.copyProperties(bannerDTO, banner);

        String imageUrl = bannerDTO.getImageUrl();
        if (fileUploadUtil.isTempFile(imageUrl)) {
            imageUrl = fileUploadUtil.moveToFormal(imageUrl);
            banner.setImageUrl(imageUrl);
            final String movedImageUrl = imageUrl;
            TransactionCompensationHelper.registerRollbackAction(
                    "addBanner-imageUrl",
                    () -> fileUploadUtil.moveToTemp(movedImageUrl)
            );
        }

        if (banner.getSort() == null) {
            banner.setSort(0);
        }
        if (banner.getStatus() == null) {
            banner.setStatus(StatusEnum.ENABLED.getCode());
        }

        bannerMapper.insert(banner);
        return banner.getId();
    }

    /**
     * 更新轮播图
     *
     * @param bannerDTO 轮播图参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateBanner(BannerDTO bannerDTO) {
        if (bannerDTO.getId() == null) {
            throw new BusinessException("轮播图 ID 不能为空");
        }

        BlogBanner existBanner = bannerMapper.selectById(bannerDTO.getId());
        if (existBanner == null) {
            throw new BusinessException("轮播图不存在");
        }

        BlogArticle article = articleMapper.selectById(bannerDTO.getArticleId());
        if (article == null) {
            throw new BusinessException("关联的文章不存在");
        }
        if (article.getIsDraft() == 1) {
            throw new BusinessException("不能关联草稿文章");
        }

        String newImageUrl = bannerDTO.getImageUrl();
        String oldImageUrl = existBanner.getImageUrl();
        if (StringUtils.hasText(newImageUrl) && !newImageUrl.equals(oldImageUrl)) {
            if (fileUploadUtil.isTempFile(newImageUrl)) {
                newImageUrl = fileUploadUtil.moveToFormal(newImageUrl);
                bannerDTO.setImageUrl(newImageUrl);
                final String movedNewImageUrl = newImageUrl;
                TransactionCompensationHelper.registerRollbackAction(
                        "updateBanner-newImageUrl",
                        () -> fileUploadUtil.moveToTemp(movedNewImageUrl)
                );

                if (StringUtils.hasText(oldImageUrl)) {
                    try {
                        String oldImageTempUrl = fileUploadUtil.moveToTemp(oldImageUrl);
                        TransactionCompensationHelper.registerRollbackAction(
                                "updateBanner-oldImageUrl",
                                () -> fileUploadUtil.moveToFormal(oldImageTempUrl)
                        );
                    } catch (Exception e) {
                        log.warn("更新轮播图时移动旧图片到临时目录失败: url={}", oldImageUrl, e);
                    }
                }
            }
        }

        BlogBanner banner = new BlogBanner();
        BeanUtils.copyProperties(bannerDTO, banner);
        bannerMapper.updateById(banner);
    }

    /**
     * 删除轮播图
     *
     * @param id 轮播图 ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBanner(Long id) {
        BlogBanner banner = bannerMapper.selectById(id);
        if (banner == null) {
            throw new BusinessException("轮播图不存在");
        }

        bannerMapper.deleteById(id);

        // 删除后将图片移入临时目录，便于后续统一生命周期处理
        try {
            if (StringUtils.hasText(banner.getImageUrl())) {
                String imageTempUrl = fileUploadUtil.moveToTemp(banner.getImageUrl());
                TransactionCompensationHelper.registerRollbackAction(
                        "deleteBanner-imageUrl",
                        () -> fileUploadUtil.moveToFormal(imageTempUrl)
                );
            }
        } catch (Exception e) {
            log.warn("删除轮播图后移动图片到临时目录失败: url={}", banner.getImageUrl(), e);
        }
    }
}
