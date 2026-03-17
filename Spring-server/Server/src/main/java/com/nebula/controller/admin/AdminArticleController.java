package com.nebula.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nebula.config.UploadRateLimiter;
import com.nebula.constant.ArticleConstants;
import com.nebula.constant.CommonConstants;
import com.nebula.controller.config.AdminController;
import com.nebula.dto.ArticleDTO;
import com.nebula.properties.UploadProperties;
import com.nebula.result.Result;
import com.nebula.service.article.BlogArticleService;
import com.nebula.upload.FileUploadUtil;
import com.nebula.vo.ArticleListVO;
import com.nebula.vo.ArticleVO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文章管理控制器
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
    private final UploadRateLimiter uploadRateLimiter;
    private final UploadProperties uploadProperties;

    /**
     * 分页查询文章列表（管理端，包含草稿）
     *
     * @param current 当前页
     * @param size 每页大小
     * @param authorName 作者名称（模糊）
     * @param title 文章标题（模糊）
     * @param categoryName 分类名称（模糊）
     * @param tagName 标签名称（模糊）
     * @param isDraft 草稿状态
     * @param isTop 置顶状态
     * @return 文章分页结果
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
     * 获取文章详情（管理端可查看草稿）
     *
     * @param id 文章 ID
     * @return 文章详情
     */
    @GetMapping("/detail/{id}")
    public Result<ArticleVO> getArticleDetail(@PathVariable Long id) {
        ArticleVO articleVO = articleService.getAdminArticleDetail(id);
        return Result.success(articleVO);
    }

    /**
     * 上传文章封面到临时目录
     *
     * @param file 封面文件
     * @param request HTTP 请求
     * @return 临时封面 URL
     */
    @PostMapping("/upload/cover")
    public Result<String> uploadCoverImage(@RequestParam("file") MultipartFile file,
                                           HttpServletRequest request) {
        // 上传前执行限流，避免恶意高频上传
        uploadRateLimiter.checkLimit(request);
        String tempCoverUrl = fileUploadUtil.uploadImageToTemp(
                file,
                uploadProperties.getDirectory().getImages().getArticleCovers()
        );
        return Result.success("上传成功", tempCoverUrl);
    }

    /**
     * 发布文章
     *
     * @param articleDTO 文章发布参数
     * @return 新文章 ID
     */
    @PostMapping("/publish")
    public Result<Long> publishArticle(@Validated(ArticleDTO.Publish.class) @RequestBody ArticleDTO articleDTO) {
        Long articleId = articleService.publishArticle(articleDTO);
        return Result.success(ArticleConstants.MSG_PUBLISH_SUCCESS, articleId);
    }

    /**
     * 更新文章
     *
     * @param articleDTO 文章更新参数
     * @return 操作结果
     */
    @PutMapping("/update")
    public Result<String> updateArticle(@Validated(ArticleDTO.Update.class) @RequestBody ArticleDTO articleDTO) {
        articleService.updateArticle(articleDTO);
        return Result.success(ArticleConstants.MSG_UPDATE_SUCCESS);
    }

    /**
     * 删除文章
     *
     * @param id 文章 ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteArticle(@PathVariable Long id) {
        articleService.deleteArticle(id);
        return Result.success(ArticleConstants.MSG_DELETE_SUCCESS);
    }
}
