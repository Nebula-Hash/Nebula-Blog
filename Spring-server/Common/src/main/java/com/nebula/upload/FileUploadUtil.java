package com.nebula.upload;

import com.nebula.enumeration.FileTypeEnum;
import com.nebula.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 文件上传工具类
 * 提供统一的文件上传、删除、校验、临时文件管理功能
 *
 * @author Nebula-Hash
 * @date 2026/1/22
 */
@RequiredArgsConstructor
@Slf4j
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
                    // 删除失败不影响继续清理，但需要记录日志方便追踪
                    log.warn("删除临时文件失败: {}", fileInfo.url(), e);
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

        long maxBytes = fileType.getDefaultMaxSizeMB() * 1024 * 1024;
        if (file.getSize() > maxBytes) {
            throw new BusinessException("文件大小不能超过" + fileType.getDefaultMaxSizeMB() + "MB");
        }

        String filename = file.getOriginalFilename();
        String lowerExt = getLowerExt(filename); // e.g. ".png" 或 空字符串
        String contentType = file.getContentType();

        switch (fileType) {
            case IMAGE:
                // 扩展名白名单
                if (!isAllowedImageExt(lowerExt)) {
                    throw new BusinessException("不支持的图片格式，仅支持 JPG/PNG/GIF/WebP");
                }
                // 魔数检测 + 可解码性校验
                if (!hasImageMagic(file) || !canDecodeImage(file)) {
                    throw new BusinessException("图片文件内容不合法或已损坏");
                }
                break;

            case MARKDOWN:
                if (!".md".equals(lowerExt)) {
                    throw new BusinessException("文件格式需为：Markdown(.md)");
                }
                // 放宽 Content-Type：接受 text/markdown、text/plain、application/octet-stream
                if (contentType != null && !(contentType.startsWith("text/") || "application/octet-stream".equals(contentType))) {
                    // 仅记录而不阻断（以扩展名为准），避免前端浏览器差异导致上传失败
                    log.debug("Markdown Content-Type 异常但忽略: {}", contentType);
                }
                break;

            case WEBP:
                if (!".webp".equals(lowerExt)) {
                    throw new BusinessException("文件格式需为：WebP(.webp)");
                }
                if (!hasImageMagic(file)) {
                    throw new BusinessException("WebP 文件内容不合法");
                }
                break;

            default:
                // 其余类型保持原有基于 Content-Type 的校验
                if (fileType.getContentTypePattern() != null) {
                    if (contentType == null || !contentType.startsWith(fileType.getContentTypePattern())) {
                        throw new BusinessException("文件格式需为：" + fileType.getDescription());
                    }
                }
        }
    }

    // ==================== 私有辅助方法 ====================

    private String getLowerExt(String filename) {
        if (filename == null) return "";
        int i = filename.lastIndexOf('.');
        return (i >= 0 && i < filename.length() - 1) ? filename.substring(i).toLowerCase() : "";
    }

    private boolean isAllowedImageExt(String ext) {
        // 允许的图片扩展名
        Set<String> exts = new HashSet<>(Arrays.asList(".jpg", ".jpeg", ".png", ".gif", ".webp"));
        return exts.contains(ext);
    }

    private boolean canDecodeImage(MultipartFile file) {
        try (InputStream is = file.getInputStream()) {
            BufferedImage bi = ImageIO.read(is);
            return bi != null;
        } catch (IOException e) {
            return false;
        }
    }

    private boolean hasImageMagic(MultipartFile file) {
        byte[] header = new byte[12];
        int read = 0;
        try (InputStream is = file.getInputStream()) {
            read = is.read(header);
        } catch (IOException e) {
            return false;
        }
        if (read < 3) return false;

        // JPEG FF D8 FF
        if ((header[0] & 0xFF) == 0xFF && (header[1] & 0xFF) == 0xD8 && (header[2] & 0xFF) == 0xFF) {
            return true;
        }
        // PNG 89 50 4E 47 0D 0A 1A 0A
        if (read >= 8 && (header[0] & 0xFF) == 0x89 && header[1] == 0x50 && header[2] == 0x4E && header[3] == 0x47 &&
                header[4] == 0x0D && header[5] == 0x0A && header[6] == 0x1A && header[7] == 0x0A) {
            return true;
        }
        // GIF "GIF87a" or "GIF89a"
        if (read >= 6 && header[0] == 'G' && header[1] == 'I' && header[2] == 'F' && header[3] == '8' &&
                (header[4] == '7' || header[4] == '9') && header[5] == 'a') {
            return true;
        }
        // WebP RIFF....WEBP
        if (read >= 12 && header[0] == 'R' && header[1] == 'I' && header[2] == 'F' && header[3] == 'F' &&
                header[8] == 'W' && header[9] == 'E' && header[10] == 'B' && header[11] == 'P') {
            return true;
        }
        return false;
    }

}
