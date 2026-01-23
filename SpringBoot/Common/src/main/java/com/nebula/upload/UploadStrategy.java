package com.nebula.upload;

import org.springframework.web.multipart.MultipartFile;

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
}
