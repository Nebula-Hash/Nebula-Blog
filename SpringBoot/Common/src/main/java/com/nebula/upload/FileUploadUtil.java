package com.nebula.upload;

import com.nebula.enumeration.FileTypeEnum;
import com.nebula.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 文件上传工具类
 * 提供统一的文件上传、删除、校验、临时文件管理功能
 *
 * @author Nebula-Hash
 * @date 2026/1/22
 */
@RequiredArgsConstructor
public class FileUploadUtil {

    private final OssUploadService ossUploadService;

    /**
     * 临时文件路径前缀
     */
    private static final String TEMP_PREFIX = "temp/";

    /**
     * 上传文件（供内部调用）
     *
     * @param file 文件
     * @param path 存储路径
     * @return 文件访问URL
     */
    public String upload(MultipartFile file, String path) {
        return ossUploadService.uploadFile(file, path);
    }

    /**
     * 上传图片（带校验）
     *
     * @param file      文件
     * @param path      存储路径
     * @return 文件访问URL
     */
    public String uploadImage(MultipartFile file, String path) {
        validateFile(file, FileTypeEnum.IMAGE);
        return upload(file, path);
    }

    /**
     * 上传图片到临时目录（带校验）
     *
     * @param file     文件
     * @param basePath 基础存储路径（不含temp前缀）
     * @return 临时文件访问URL
     */
    public String uploadImageToTemp(MultipartFile file, String basePath) {
        validateFile(file, FileTypeEnum.IMAGE);
        return upload(file, TEMP_PREFIX + basePath);
    }

    /**
     * 上传文章（带校验）
     *
     * @param file      文件
     * @param path      存储路径
     * @return 文件访问URL
     */
    public String uploadArticle(MultipartFile file, String path) {
        validateFile(file, FileTypeEnum.MARKDOWN);
        return upload(file, path);
    }

    /**
     * 删除文件
     *
     * @param fileUrl 文件访问URL
     */
    public void delete(String fileUrl) {
        ossUploadService.deleteFile(fileUrl);
    }

    /**
     * 移动文件
     *
     * @param sourceUrl  源文件URL
     * @param targetPath 目标路径
     * @return 移动后的文件URL
     */
    public String move(String sourceUrl, String targetPath) {
        return ossUploadService.moveFile(sourceUrl, targetPath);
    }

    /**
     * 将临时文件转为正式文件
     * temp/images/banners/xxx.jpg -> images/banners/xxx.jpg
     *
     * @param tempUrl 临时文件URL
     * @return 正式文件URL
     */
    public String moveToFormal(String tempUrl) {
        if (!isTempFile(tempUrl)) {
            throw new BusinessException("不是临时文件");
        }
        // 通过 OSS 对象名提取正式目录：去掉 temp/ 前缀和文件名
        String objectName = ossUploadService.extractObjectName(tempUrl);
        String formalObjectName = objectName.substring(TEMP_PREFIX.length());
        int lastSlash = formalObjectName.lastIndexOf("/");
        String formalDir = lastSlash > 0 ? formalObjectName.substring(0, lastSlash) : "";
        return move(tempUrl, formalDir);
    }

    /**
     * 将正式文件移至临时目录
     * images/banners/xxx.jpg -> temp/images/banners/xxx.jpg
     *
     * @param formalUrl 正式文件URL
     * @return 临时文件URL
     */
    public String moveToTemp(String formalUrl) {
        if (isTempFile(formalUrl)) {
            throw new BusinessException("已经是临时文件");
        }
        // 通过 OSS 对象名提取目录并添加 temp/ 前缀
        String objectName = ossUploadService.extractObjectName(formalUrl);
        int lastSlash = objectName.lastIndexOf("/");
        String dir = lastSlash > 0 ? objectName.substring(0, lastSlash) : "";
        return move(formalUrl, TEMP_PREFIX + dir);
    }

    /**
     * 判断是否为临时文件
     *
     * @param fileUrl 文件URL
     * @return 是否为临时文件
     */
    public boolean isTempFile(String fileUrl) {
        return fileUrl != null && fileUrl.contains("/" + TEMP_PREFIX);
    }

    /**
     * 列出临时目录下的文件
     *
     * @return 文件信息列表
     */
    public List<OssUploadService.FileInfo> listTempFiles() {
        return ossUploadService.listFiles(TEMP_PREFIX);
    }

    /**
     * 清理过期的临时文件
     *
     * @param expiredHours 过期时间（小时）
     * @return 清理的文件数量
     */
    public int cleanExpiredTempFiles(int expiredHours) {
        List<OssUploadService.FileInfo> tempFiles = listTempFiles();
        LocalDateTime expiredTime = LocalDateTime.now().minusHours(expiredHours);
        int count = 0;

        for (OssUploadService.FileInfo fileInfo : tempFiles) {
            if (fileInfo.lastModified().isBefore(expiredTime)) {
                try {
                    delete(fileInfo.url());
                    count++;
                } catch (Exception e) {
                    // 删除失败不影响继续清理
                }
            }
        }
        return count;
    }

    /**
     * 校验文件
     *
     * @param file       文件
     * @param fileType   文件类型
     */
    public void validateFile(MultipartFile file, FileTypeEnum fileType) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("文件不能为空");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith(fileType.getContentTypePattern())) {
            throw new BusinessException("文件格式需为：" + fileType.getDescription());
        }
        if (file.getSize() > fileType.getDefaultMaxSizeMB() * 1024 * 1024) {
            throw new BusinessException("文件大小不能超过" + fileType.getDefaultMaxSizeMB() + "MB");
        }
    }

}
