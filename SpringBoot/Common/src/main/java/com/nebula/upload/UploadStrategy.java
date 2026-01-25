package com.nebula.upload;

import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 文件上传策略接口
 *
 * @author Nebula-Hash
 * @date 2026/1/22
 */
public interface UploadStrategy {

    /**
     * 上传文件
     *
     * @param file 文件
     * @param path 路径
     * @return 文件访问地址
     */
    String uploadFile(MultipartFile file, String path);

    /**
     * 删除文件
     *
     * @param fileUrl 文件地址
     */
    void deleteFile(String fileUrl);

    /**
     * 移动文件
     *
     * @param sourceUrl  源文件URL
     * @param targetPath 目标路径（不含文件名）
     * @return 移动后的文件访问URL
     */
    String moveFile(String sourceUrl, String targetPath);

    /**
     * 列出指定前缀下的文件
     *
     * @param prefix 路径前缀
     * @return 文件信息列表
     */
    List<FileInfo> listFiles(String prefix);

    /**
     * 文件信息
     */
    record FileInfo(String objectName, String url, LocalDateTime lastModified) {}
}
