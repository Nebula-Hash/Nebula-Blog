package com.nebula.utils;

import com.nebula.config.WebPConfig;
import com.nebula.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

/**
 * 图像转换服务
 * 支持将各种图片格式转换为WebP
 *
 * @author Nebula-Hash
 * @date 2026/02/02
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WebPImageConversion {

    private final WebPConfig webPConfig;

    /**
     * 将图片转换为WebP格式
     *
     * @param file 原始图片文件
     * @return WebP格式的字节数组
     * @throws BusinessException 转换失败时抛出
     */
    public byte[] convertToWebP(MultipartFile file) {
        try {
            // 读取原始图片
            BufferedImage image = ImageIO.read(file.getInputStream());
            if (image == null) {
                throw new BusinessException("无法读取图片文件，可能不是有效的图片格式");
            }

            log.debug("原始图片尺寸: {}x{}", image.getWidth(), image.getHeight());

            // 如果图片过大，进行缩放
            if (image.getWidth() > webPConfig.getMaxWidth() || image.getHeight() > webPConfig.getMaxHeight()) {
                log.info("图片尺寸超过限制，进行缩放: {}x{} -> 最大{}x{}",
                        image.getWidth(), image.getHeight(), webPConfig.getMaxWidth(), webPConfig.getMaxHeight());
                image = resizeImage(image);
            }

            // 转换为WebP
            byte[] webpBytes = encodeToWebP(image);

            if (webPConfig.isEnableStats()) {
                log.info("图片转换成功: {} -> WebP, 原始大小: {}KB, WebP大小: {}KB, 压缩率: {}%",
                        file.getOriginalFilename(),
                        file.getSize() / 1024,
                        webpBytes.length / 1024,
                        String.format("%.1f", (1 - (double) webpBytes.length / file.getSize()) * 100)
                );
            }

            return webpBytes;

        } catch (IOException e) {
            log.error("图片转换失败: {}, 错误: {}", file.getOriginalFilename(), e.getMessage(), e);
            
            // 根据配置的失败策略处理
            if (webPConfig.getFailureStrategy() == WebPConfig.FailureStrategy.THROW_EXCEPTION) {
                throw new BusinessException("图片转换失败: " + e.getMessage());
            }
            
            // 返回null表示转换失败，调用方应保留原图
            return null;
        }
    }

    /**
     * 将图片转换为WebP格式（返回InputStream）
     * 适用于直接上传到OSS的场景
     *
     * @param file 原始图片文件
     * @return WebP格式的输入流，转换失败返回null
     */
    public InputStream convertToWebPStream(MultipartFile file) {
        byte[] webpBytes = convertToWebP(file);
        return webpBytes != null ? new ByteArrayInputStream(webpBytes) : null;
    }

    /**
     * 缩放图片到指定最大尺寸
     * 保持宽高比
     *
     * @param original 原始图片
     * @return 缩放后的图片
     */
    private BufferedImage resizeImage(BufferedImage original) throws IOException {
        int width = original.getWidth();
        int height = original.getHeight();

        // 计算缩放比例（保持宽高比）
        double scale = Math.min(
                (double) webPConfig.getMaxWidth() / width,
                (double) webPConfig.getMaxHeight() / height
        );

        int newWidth = (int) (width * scale);
        int newHeight = (int) (height * scale);

        log.debug("缩放图片: {}x{} -> {}x{}", width, height, newWidth, newHeight);

        // 使用Thumbnailator进行高质量缩放
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Thumbnails.of(original)
                .size(newWidth, newHeight)
                .outputFormat("png") // 临时格式，后续会转为WebP
                .outputQuality(1.0) // 缩放时保持最高质量
                .toOutputStream(outputStream);

        return ImageIO.read(new ByteArrayInputStream(outputStream.toByteArray()));
    }

    /**
     * 编码为WebP格式
     *
     * @param image 待编码的图片
     * @return WebP格式的字节数组
     */
    private byte[] encodeToWebP(BufferedImage image) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        // 获取WebP编码器
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByMIMEType("image/webp");
        if (!writers.hasNext()) {
            throw new BusinessException("系统不支持WebP格式，请检查是否添加了imageio-webp依赖");
        }

        ImageWriter writer = writers.next();
        ImageWriteParam writeParam = writer.getDefaultWriteParam();

        // 设置压缩参数
        if (writeParam.canWriteCompressed()) {
            writeParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            writeParam.setCompressionQuality(webPConfig.getQuality());
            log.debug("WebP压缩质量设置为: {}", webPConfig.getQuality());
        }

        try (ImageOutputStream ios = ImageIO.createImageOutputStream(outputStream)) {
            writer.setOutput(ios);
            writer.write(null, new IIOImage(image, null, null), writeParam);
        } finally {
            writer.dispose();
        }

        return outputStream.toByteArray();
    }

    /**
     * 检查文件是否为图片
     *
     * @param file 待检查的文件
     * @return 是否为图片
     */
    public boolean isImage(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && contentType.startsWith("image/");
    }

    /**
     * 检查文件是否已经是WebP格式
     *
     * @param file 待检查的文件
     * @return 是否为WebP格式
     */
    public boolean isWebP(MultipartFile file) {
        String contentType = file.getContentType();
        String filename = file.getOriginalFilename();
        return "image/webp".equals(contentType) ||
                (filename != null && filename.toLowerCase().endsWith(".webp"));
    }

    /**
     * 检查是否应该转换为WebP
     *
     * @param file 待检查的文件
     * @return 是否应该转换
     */
    public boolean shouldConvertToWebP(MultipartFile file) {
        return webPConfig.isEnabled() && isImage(file) && !isWebP(file);
    }

    /**
     * 获取WebP文件扩展名
     *
     * @return .webp
     */
    public String getWebPExtension() {
        return ".webp";
    }

    /**
     * 估算转换后的文件大小（用于预检查）
     *
     * @param originalSize 原始文件大小（字节）
     * @return 预估的WebP文件大小（字节）
     */
    public long estimateWebPSize(long originalSize) {
        // 根据经验，WebP通常能压缩到原始大小的30-50%
        return (long) (originalSize * 0.4);
    }
}
