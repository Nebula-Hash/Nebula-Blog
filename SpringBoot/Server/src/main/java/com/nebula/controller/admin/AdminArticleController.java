package com.nebula.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nebula.constant.ArticleConstants;
import com.nebula.constant.CommonConstants;
import com.nebula.controller.config.AdminController;
import com.nebula.dto.ArticleDTO;
import com.nebula.result.Result;
import com.nebula.service.article.BlogArticleService;
import com.nebula.upload.FileUploadUtil;
import com.nebula.vo.ArticleListVO;
import com.nebula.vo.ArticleVO;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文章控制器（管理端）
 *
 * @author Nebula-Hash
 * @date 2026/1/22
 */
@AdminController
@RequestMapping("/article")
@RequiredArgsConstructor
public class AdminArticleController {

    private final BlogArticleService articleService;
    private final FileUploadUtil fileUploadUtil;

    /**
     * 分页查询/搜索文章列表（包含草稿）
     * <p>
     * 不传搜索参数时返回所有文章；传入搜索参数时按条件筛选
     *
     * @param current      当前页
     * @param size         每页大小
     * @param authorName   作者名称（可选，模糊搜索）
     * @param title        文章标题（可选，模糊搜索）
     * @param categoryName 分类名称（可选，模糊搜索）
     * @param tagName      标签名称（可选，模糊搜索）
     * @param isDraft      草稿状态（可选，0-已发布，1-草稿）
     * @param isTop        置顶状态（可选，0-未置顶，1-置顶）
     * @return 文章分页列表
     */
    @GetMapping("/list")
    public Result<Page<ArticleListVO>> getAdminArticleList(
            @RequestParam(defaultValue = CommonConstants.DEFAULT_PAGE_CURRENT) Long current,
            @RequestParam(defaultValue = CommonConstants.DEFAULT_PAGE_SIZE) Long size,
            @RequestParam(required = false) String authorName,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String categoryName,
            @RequestParam(required = false) String tagName,
            @RequestParam(required = false) Integer isDraft,
            @RequestParam(required = false) Integer isTop) {
        Page<ArticleListVO> page = articleService.getAdminArticleList(
                current, size, authorName, title, categoryName, tagName, isDraft, isTop);
        return Result.success(page);
    }

    /**
     * 获取文章详情（可查看草稿）
     */
    @GetMapping("/detail/{id}")
    public Result<ArticleVO> getArticleDetail(@PathVariable Long id) {
        ArticleVO articleVO = articleService.getAdminArticleDetail(id);
        return Result.success(articleVO);
    }

    /**
     * 上传文章封面图（上传到临时目录）
     * <p>
     * 设计思路：
     * - 选择图片后立即上传并显示预览
     * - 单独显示图片上传进度条
     * - 图片上传和表单提交分离，最终提交时更快
     * - 前端拿到临时 coverImage URL 后提交表单，后端业务层将临时文件转正
     *
     * @param file 封面图文件
     * @return 临时封面图的访问URL
     */
    @PostMapping("/upload/cover")
    public Result<String> uploadCoverImage(@RequestParam("file") MultipartFile file) {
        // 上传到临时目录
        String tempCoverUrl = fileUploadUtil.uploadImageToTemp(file, "images/articles/covers");
        return Result.success("上传成功", tempCoverUrl);
    }

    /**
     * 发布文章
     */
    @PostMapping("/publish")
    public Result<Long> publishArticle(@Validated(ArticleDTO.Publish.class) @RequestBody ArticleDTO articleDTO) {
        Long articleId = articleService.publishArticle(articleDTO);
        return Result.success(ArticleConstants.MSG_PUBLISH_SUCCESS, articleId);
    }

    /**
     * 编辑文章
     */
    @PutMapping("/update")
    public Result<String> updateArticle(@Validated(ArticleDTO.Update.class) @RequestBody ArticleDTO articleDTO) {
        articleService.updateArticle(articleDTO);
        return Result.success(ArticleConstants.MSG_UPDATE_SUCCESS);
    }

    /**
     * 删除文章
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteArticle(@PathVariable Long id) {
        articleService.deleteArticle(id);
        return Result.success(ArticleConstants.MSG_DELETE_SUCCESS);
    }

}
