package com.nebula.utils;

import com.nebula.exception.BusinessException;
import com.nebula.properties.WebPProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
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
 * 支持将图片转换为 WebP 格式
 *
 * @author Nebula-Hash
 * @date 2026/2/2
 */
@Slf4j
@RequiredArgsConstructor
public class WebPImageConversion {

    private final WebPProperties webPProperties;

    /**
     * 将图片转换为 WebP 字节数组
     *
     * @param file 原始图片文件
     * @return WebP 字节数组，转换失败时返回 null
     */
    public byte[] convertToWebP(MultipartFile file) {
        if (!webPProperties.isEnabled()) {
            log.debug("WebP 转换已禁用");
            return null;
        }

        try {
            // 读取原始图片
            BufferedImage image = ImageIO.read(file.getInputStream());
            if (image == null) {
                log.warn("无法读取图片文件: {}", file.getOriginalFilename());
                return null;
            }

            log.debug(
                    "原始图片尺寸: {}x{}, 内容类型: {}",
                    image.getWidth(),
                    image.getHeight(),
                    file.getContentType()
            );

            // 大图先按配置缩放，避免后续编码耗时过高
            if (image.getWidth() > webPProperties.getMaxWidth() || image.getHeight() > webPProperties.getMaxHeight()) {
                log.info(
                        "图片尺寸超过限制，执行缩放: {}x{} -> 最大 {}x{}",
                        image.getWidth(),
                        image.getHeight(),
                        webPProperties.getMaxWidth(),
                        webPProperties.getMaxHeight()
                );
                image = resizeImage(image);
            }

            byte[] webpBytes = encodeToWebP(image);

            if (webPProperties.isEnableStats()) {
                long originalSize = Math.max(file.getSize(), 1L);
                double compressionRatio = (1 - (double) webpBytes.length / originalSize) * 100;
                log.info(
                        "WebP 转换成功: {} | 原始: {}KB -> WebP: {}KB | 压缩率: {}%",
                        file.getOriginalFilename(),
                        file.getSize() / 1024,
                        webpBytes.length / 1024,
                        String.format("%.1f", compressionRatio)
                );
            }

            return webpBytes;
        } catch (Exception e) {
            log.error("WebP 转换失败: {}, 错误: {}", file.getOriginalFilename(), e.getMessage());

            // 按配置决定转换失败后的处理策略
            if (webPProperties.getFailureStrategy() == WebPProperties.FailureStrategy.THROW_EXCEPTION) {
                throw new BusinessException("图片转换失败: " + e.getMessage());
            }

            log.warn("WebP 转换失败，将使用原图格式");
            return null;
        }
    }

    /**
     * 将图片转换为 WebP 输入流
     *
     * @param file 原始图片文件
     * @return WebP 输入流，转换失败时返回 null
     */
    public InputStream convertToWebPStream(MultipartFile file) {
        byte[] webpBytes = convertToWebP(file);
        return webpBytes != null ? new ByteArrayInputStream(webpBytes) : null;
    }

    /**
     * 按最大宽高缩放图片并保持比例
     *
     * @param original 原始图片
     * @return 缩放后的图片
     * @throws IOException IO 异常
     */
    private BufferedImage resizeImage(BufferedImage original) throws IOException {
        int width = original.getWidth();
        int height = original.getHeight();

        double scale = Math.min(
                (double) webPProperties.getMaxWidth() / width,
                (double) webPProperties.getMaxHeight() / height
        );

        int newWidth = (int) (width * scale);
        int newHeight = (int) (height * scale);

        log.debug("缩放图片: {}x{} -> {}x{}", width, height, newWidth, newHeight);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Thumbnails.of(original)
                .size(newWidth, newHeight)
                // 先输出为 PNG，再统一走 WebP 编码流程
                .outputFormat("png")
                // 缩放过程中保持高质量，压缩在 WebP 编码时处理
                .outputQuality(1.0)
                .toOutputStream(outputStream);

        return ImageIO.read(new ByteArrayInputStream(outputStream.toByteArray()));
    }

    /**
     * 将 BufferedImage 编码为 WebP
     *
     * @param image 待编码图片
     * @return WebP 字节数组
     * @throws IOException IO 异常
     */
    private byte[] encodeToWebP(BufferedImage image) throws IOException {
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("webp");
        if (!writers.hasNext()) {
            writers = ImageIO.getImageWritersByMIMEType("image/webp");
            if (!writers.hasNext()) {
                String[] availableFormats = ImageIO.getWriterFormatNames();
                log.error("系统不支持 WebP 格式，可用写入格式: {}", String.join(", ", availableFormats));
                throw new BusinessException("系统不支持 WebP 格式，请检查 webp-imageio 依赖是否正确加载");
            }
        }

        ImageWriter writer = writers.next();
        log.debug("使用 WebP 编码器: {}", writer.getClass().getName());

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (ImageOutputStream ios = ImageIO.createImageOutputStream(outputStream)) {
            writer.setOutput(ios);

            ImageWriteParam writeParam = writer.getDefaultWriteParam();
            if (writeParam.canWriteCompressed()) {
                writeParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);

                String[] compressionTypes = writeParam.getCompressionTypes();
                if (compressionTypes != null && compressionTypes.length > 0) {
                    writeParam.setCompressionType(compressionTypes[0]);
                    log.debug("WebP 压缩类型: {}", compressionTypes[0]);
                }

                writeParam.setCompressionQuality(webPProperties.getQuality());
                log.debug("WebP 压缩质量: {}", webPProperties.getQuality());
            } else {
                log.warn("WebP 编码器不支持压缩参数设置");
            }

            writer.write(null, new IIOImage(image, null, null), writeParam);
            ios.flush();
        } finally {
            writer.dispose();
        }

        return outputStream.toByteArray();
    }

    /**
     * 判断文件是否为图片
     *
     * @param file 待判断文件
     * @return true 表示图片文件
     */
    public boolean isImage(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && contentType.startsWith("image/");
    }

    /**
     * 判断文件是否已经是 WebP
     *
     * @param file 待判断文件
     * @return true 表示 WebP 文件
     */
    public boolean isWebP(MultipartFile file) {
        String contentType = file.getContentType();
        String filename = file.getOriginalFilename();
        return "image/webp".equals(contentType)
                || (filename != null && filename.toLowerCase().endsWith(".webp"));
    }

    /**
     * 判断文件是否应转换为 WebP
     *
     * @param file 待判断文件
     * @return true 表示应执行转换
     */
    public boolean shouldConvertToWebP(MultipartFile file) {
        return webPProperties.isEnabled() && isImage(file) && !isWebP(file);
    }

    /**
     * 判断转换成功后是否保留原图
     *
     * @return true 表示保留原图
     */
    public boolean shouldKeepOriginal() {
        return webPProperties.isKeepOriginal();
    }

    /**
     * 获取 WebP 文件后缀
     *
     * @return WebP 后缀名
     */
    public String getWebPExtension() {
        return ".webp";
    }

    /**
     * 估算 WebP 转换后的文件大小
     *
     * @param originalSize 原始文件大小（字节）
     * @return 预估转换后大小（字节）
     */
    public long estimateWebPSize(long originalSize) {
        // 经验值：WebP 通常可压缩至原始大小的 40% 左右
        return (long) (originalSize * 0.4);
    }
}
