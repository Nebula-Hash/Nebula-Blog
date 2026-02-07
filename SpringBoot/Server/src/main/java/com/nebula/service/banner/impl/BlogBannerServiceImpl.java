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
import com.nebula.upload.FileUploadUtil;
import com.nebula.vo.admin.BannerAdminVO;
import com.nebula.vo.client.BannerClientVO;
import lombok.RequiredArgsConstructor;
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
 * @date 2026/1/22
 */
@Service
@RequiredArgsConstructor
public class BlogBannerServiceImpl implements BlogBannerService {

    private final BlogBannerMapper bannerMapper;
    private final BlogArticleMapper articleMapper;
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
        // 使用自定义SQL查询，包含文章标题
        List<BannerAdminVO> allBanners = bannerMapper.selectBannerListWithArticle();
        
        // 手动分页
        int start = (int) ((current - 1) * size);
        int end = Math.min(start + size.intValue(), allBanners.size());
        List<BannerAdminVO> pageRecords = allBanners.subList(start, end);
        
        Page<BannerAdminVO> resultPage = new Page<>(current, size, allBanners.size());
        resultPage.setRecords(pageRecords);
        return resultPage;
    }

    @Override
    public BannerAdminVO getBannerDetail(Long id) {
        BannerAdminVO banner = bannerMapper.selectBannerDetailWithArticle(id);
        if (banner == null) {
            throw new BusinessException("轮播图不存在");
        }
        return banner;
    }

    @Override
    public List<Map<String, Object>> getPublishedArticles() {
        LambdaQueryWrapper<BlogArticle> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BlogArticle::getIsDraft, 0); // 非草稿
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addBanner(BannerDTO bannerDTO) {
        // 验证文章是否存在
        BlogArticle article = articleMapper.selectById(bannerDTO.getArticleId());
        if (article == null) {
            throw new BusinessException("关联的文章不存在");
        }
        if (article.getIsDraft() == 1) {
            throw new BusinessException("不能关联草稿文章");
        }

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

        // 验证文章是否存在
        BlogArticle article = articleMapper.selectById(bannerDTO.getArticleId());
        if (article == null) {
            throw new BusinessException("关联的文章不存在");
        }
        if (article.getIsDraft() == 1) {
            throw new BusinessException("不能关联草稿文章");
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
}
