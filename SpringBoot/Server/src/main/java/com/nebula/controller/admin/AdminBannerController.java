package com.nebula.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nebula.controller.config.AdminController;
import com.nebula.dto.BannerDTO;
import com.nebula.result.Result;
import com.nebula.service.banner.BlogBannerService;
import com.nebula.upload.FileUploadUtil;
import com.nebula.vo.BannerAdminVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 轮播图控制器（管理端）
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

    /**
     * 分页查询轮播图列表
     */
    @GetMapping("/list")
    public Result<Page<BannerAdminVO>> getBannerList(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "5") Long size)
    {
        Page<BannerAdminVO> page = bannerService.getBannerList(current, size);
        return Result.success(page);
    }

    /**
     * 获取轮播图详情
     */
    @GetMapping("/detail/{id}")
    public Result<BannerAdminVO> getBannerDetail(@PathVariable Long id) {
        BannerAdminVO bannerVO = bannerService.getBannerDetail(id);
        return Result.success(bannerVO);
    }

    /**
     * 上传轮播图图片（上传到临时目录）
     * /选择图片后立即上传并显示预览
     * /单独显示图片上传进度条
     * /图片上传和表单提交分离，最终提交时更快
     * /前端拿到临时ImageURL后提交表单，后端业务层将临时文件转正
     *
     * @param file 图片文件
     * @return 临时图片的访问URL
     */
    @PostMapping("/upload")
    public Result<String> uploadBannerImage(@RequestParam("file") MultipartFile file) {
        // 上传到临时目录
        String tempImageUrl = fileUploadUtil.uploadImageToTemp(file, "images/banners");
        return Result.success("上传成功", tempImageUrl);
    }

    /**
     * 添加轮播图
     */
    @PostMapping("/add")
    public Result<Long> addBanner(@Valid @RequestBody BannerDTO bannerDTO) {
        Long bannerId = bannerService.addBanner(bannerDTO);
        return Result.success("添加成功", bannerId);
    }

    /**
     * 编辑轮播图
     */
    @PutMapping("/update")
    public Result<String> updateBanner(@Valid @RequestBody BannerDTO bannerDTO) {
        bannerService.updateBanner(bannerDTO);
        return Result.success("更新成功");
    }

    /**
     * 删除轮播图
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteBanner(@PathVariable Long id) {
        bannerService.deleteBanner(id);
        return Result.success("删除成功");
    }
}
