package com.nebula.upload;

import com.nebula.enumeration.FileType;
import com.nebula.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传工具类
 * 提供统一的文件上传、删除、校验功能
 *
 * @author Nebula-Hash
 * @date 2026/1/22
 */
@Component
@RequiredArgsConstructor
public class FileUploadUtil {

    private final UploadStrategyFactory uploadStrategyFactory;

    /**
     * 上传文件（供内部调用）
     *
     * @param file 文件
     * @param path 存储路径
     * @return 文件访问URL
     */
    public String upload(MultipartFile file, String path) {
        return uploadStrategyFactory.getStrategy().uploadFile(file, path);
    }

    /**
     * 上传图片（带校验）
     *
     * @param file      文件
     * @param path      存储路径
     * @return 文件访问URL
     */
    public String uploadImage(MultipartFile file, String path) {
        validateFile(file, FileType.IMAGE);
        return upload(file, path);
    }

    /**
     * 上传文章（带校验）
     *
     * @param file      文件
     * @param path      存储路径
     * @return 文件访问URL
     */
    public String uploadArticle(MultipartFile file, String path) {
        validateFile(file,FileType.MARKDOWN);
        return upload(file, path);
    }



    /**
     * 删除文件
     *
     * @param fileUrl 文件访问URL
     */
    public void delete(String fileUrl) {
        uploadStrategyFactory.getStrategy().deleteFile(fileUrl);
    }



    /**
     * 校验文件
     *
     * @param file       文件
     * @param fileType   文件类型
     */
    public void validateFile(MultipartFile file, FileType fileType) {
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
