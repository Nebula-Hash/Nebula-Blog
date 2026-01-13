package com.nebula.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.nebula.result.Result;
import com.nebula.strategy.UploadStrategy;
import com.nebula.strategy.UploadStrategyFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传控制器
 *
 * @author Nebula-Hash
 * @date 2025/11/27
 */
@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
public class FileController {

    private final UploadStrategyFactory uploadStrategyFactory;

    /**
     * 上传图片
     *
     * @param file 文件
     * @return 文件访问地址
     */
    @SaCheckLogin
    @PostMapping("/upload/image")
    public Result<String> uploadImage(@RequestParam("file") MultipartFile file) {
        // 校验文件
        if (file.isEmpty()) {
            return Result.error("文件不能为空");
        }

        // 校验文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return Result.error("只支持图片格式");
        }

        // 校验文件大小（5MB）
        long maxSize = 5 * 1024 * 1024;
        if (file.getSize() > maxSize) {
            return Result.error("文件大小不能超过5MB");
        }

        // 获取上传策略
        UploadStrategy uploadStrategy = uploadStrategyFactory.getStrategy();
        
        // 上传文件
        String fileUrl = uploadStrategy.uploadFile(file, "images");

        return Result.success("上传成功", fileUrl);
    }

    /**
     * 上传文章图片
     *
     * @param file 文件
     * @return 文件访问地址
     */
    @SaCheckLogin
    @PostMapping("/upload/article")
    public Result<String> uploadArticleImage(@RequestParam("file") MultipartFile file) {
        // 校验文件
        if (file.isEmpty()) {
            return Result.error("文件不能为空");
        }

        // 校验文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return Result.error("只支持图片格式");
        }

        // 校验文件大小（10MB）
        long maxSize = 10 * 1024 * 1024;
        if (file.getSize() > maxSize) {
            return Result.error("文件大小不能超过10MB");
        }

        // 获取上传策略
        UploadStrategy uploadStrategy = uploadStrategyFactory.getStrategy();
        
        // 上传文件
        String fileUrl = uploadStrategy.uploadFile(file, "articles");

        return Result.success("上传成功", fileUrl);
    }

    /**
     * 删除文件
     *
     * @param fileUrl 文件地址
     * @return 删除结果
     */
    @SaCheckLogin
    @DeleteMapping("/delete")
    public Result<String> deleteFile(@RequestParam String fileUrl) {
        // 获取上传策略
        UploadStrategy uploadStrategy = uploadStrategyFactory.getStrategy();
        
        // 删除文件
        uploadStrategy.deleteFile(fileUrl);

        return Result.success("删除成功");
    }
}
