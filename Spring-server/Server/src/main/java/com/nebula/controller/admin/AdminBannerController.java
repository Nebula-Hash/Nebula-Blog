package com.nebula.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nebula.config.UploadRateLimiter;
import com.nebula.controller.config.AdminController;
import com.nebula.dto.BannerDTO;
import com.nebula.properties.UploadProperties;
import com.nebula.result.Result;
import com.nebula.service.banner.BlogBannerService;
import com.nebula.upload.FileUploadUtil;
import com.nebula.vo.admin.BannerAdminVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 轮播图管理控制器
 *
 * @author Nebula-Hash
 * @date 2026/1/23
 */
@AdminController
@RequestMapping("/banner")
@RequiredArgsConstructor
public class AdminBannerController {

    private final BlogBannerService bannerService;
    private final FileUploadUtil fileUploadUtil;
    private final UploadRateLimiter uploadRateLimiter;
    private final UploadProperties uploadProperties;

    /**
     * 分页查询轮播图列表
     *
     * @param current 当前页
     * @param size 每页大小
     * @return 轮播图分页结果
     */
    @GetMapping("/list")
    public Result<Page<BannerAdminVO>> getBannerList(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "5") Long size) {
        Page<BannerAdminVO> page = bannerService.getBannerList(current, size);
        return Result.success(page);
    }

    /**
     * 获取轮播图详情
     *
     * @param id 轮播图 ID
     * @return 轮播图详情
     */
    @GetMapping("/detail/{id}")
    public Result<BannerAdminVO> getBannerDetail(@PathVariable Long id) {
        BannerAdminVO bannerVO = bannerService.getBannerDetail(id);
        return Result.success(bannerVO);
    }

    /**
     * 获取已发布文章列表（用于轮播图关联选择）
     *
     * @return 文章简要列表
     */
    @GetMapping("/articles")
    public Result<List<Map<String, Object>>> getPublishedArticles() {
        List<Map<String, Object>> articles = bannerService.getPublishedArticles();
        return Result.success(articles);
    }

    /**
     * 上传轮播图到临时目录
     *
     * @param file 图片文件
     * @param request HTTP 请求
     * @return 临时图片 URL
     */
    @PostMapping("/upload")
    public Result<String> uploadBannerImage(@RequestParam("file") MultipartFile file,
                                            HttpServletRequest request) {
        // 上传前执行限流，控制高频访问
        uploadRateLimiter.checkLimit(request);
        String tempImageUrl = fileUploadUtil.uploadImageToTemp(
                file,
                uploadProperties.getDirectory().getImages().getBanners()
        );
        return Result.success("上传成功", tempImageUrl);
    }

    /**
     * 新增轮播图
     *
     * @param bannerDTO 轮播图参数
     * @return 新增轮播图 ID
     */
    @PostMapping("/add")
    public Result<Long> addBanner(@Valid @RequestBody BannerDTO bannerDTO) {
        Long bannerId = bannerService.addBanner(bannerDTO);
        return Result.success("添加成功", bannerId);
    }

    /**
     * 更新轮播图
     *
     * @param bannerDTO 轮播图参数
     * @return 操作结果
     */
    @PutMapping("/update")
    public Result<String> updateBanner(@Valid @RequestBody BannerDTO bannerDTO) {
        bannerService.updateBanner(bannerDTO);
        return Result.success("更新成功");
    }

    /**
     * 删除轮播图
     *
     * @param id 轮播图 ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteBanner(@PathVariable Long id) {
        bannerService.deleteBanner(id);
        return Result.success("删除成功");
    }
}
