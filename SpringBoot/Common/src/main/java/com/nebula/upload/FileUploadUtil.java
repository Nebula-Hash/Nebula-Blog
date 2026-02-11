package com.nebula.upload;

import com.nebula.enumeration.FileTypeEnum;
import com.nebula.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
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
@Component
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
        // 提取正式路径：将temp/前缀去掉
        String formalPath = extractFormalPath(tempUrl);
        return move(tempUrl, formalPath);
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
        // 提取路径并添加temp前缀
        String tempPath = TEMP_PREFIX + extractPath(formalUrl);
        return move(formalUrl, tempPath);
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

    /**
     * 从URL中提取文件路径（不含文件名）
     * 例如: <a href="https://cdn.example.com/images/banners/2026-01-24xxx.jpg">...</a> -> images/banners/2026-01-24
     */
    private String extractPath(String fileUrl) {
        // 查找协议后的路径部分
        int protocolEnd = fileUrl.indexOf("://");
        if (protocolEnd == -1) {
            throw new BusinessException("无效的文件URL");
        }
        // 查找域名后的路径
        int pathStart = fileUrl.indexOf("/", protocolEnd + 3);
        if (pathStart == -1) {
            throw new BusinessException("无效的文件URL");
        }
        String fullPath = fileUrl.substring(pathStart + 1);
        // 去掉文件名，只保留目录
        int lastSlash = fullPath.lastIndexOf("/");
        if (lastSlash == -1) {
            return "";
        }
        return fullPath.substring(0, lastSlash);
    }

    /**
     * 从临时URL中提取正式路径
     * 例如: temp/images/banners/2026-01-24 -> images/banners/2026-01-24
     */
    private String extractFormalPath(String tempUrl) {
        String path = extractPath(tempUrl);
        // 去除temp/前缀
        if (path.startsWith(TEMP_PREFIX)) {
            return path.substring(TEMP_PREFIX.length());
        }
        return path;
    }
}
