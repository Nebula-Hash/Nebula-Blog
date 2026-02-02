/**
 * WebP 工具函数
 * 提供WebP格式检测和URL处理功能
 */

/**
 * 检测浏览器是否支持WebP
 * @returns {boolean} 是否支持WebP
 */
export const supportsWebP = () => {
    const canvas = document.createElement('canvas')
    if (canvas.getContext && canvas.getContext('2d')) {
        return canvas.toDataURL('image/webp').indexOf('data:image/webp') === 0
    }
    return false
}

/**
 * 判断URL是否为WebP格式
 * @param {string} url - 图片URL
 * @returns {boolean} 是否为WebP格式
 */
export const isWebPUrl = (url) => {
    if (!url) return false
    return url.toLowerCase().endsWith('.webp')
}

/**
 * 判断URL是否为新上传的图片（包含日期格式）
 * 新上传的图片已经由后端转换为WebP
 * @param {string} url - 图片URL
 * @returns {boolean} 是否为新上传的图片
 */
export const isNewUploadedImage = (url) => {
    if (!url) return false
    return /\/\d{4}-\d{2}-\d{2}/.test(url)
}

/**
 * 获取WebP版本的URL
 * 如果图片已经是WebP或是新上传的图片，直接返回原URL
 * 否则尝试将扩展名替换为.webp
 * @param {string} url - 原始图片URL
 * @returns {string|null} WebP版本的URL，如果不适用则返回null
 */
export const getWebPUrl = (url) => {
    if (!url) return null

    // 已经是WebP格式
    if (isWebPUrl(url)) return url

    // 新上传的图片，后端已处理为WebP
    if (isNewUploadedImage(url)) return url

    // 历史图片，尝试替换扩展名
    const ext = url.split('.').pop()
    if (ext && ext.length <= 4) {
        return url.replace(`.${ext}`, '.webp')
    }

    return null
}

/**
 * 创建picture标签的srcset属性
 * @param {string} url - 图片URL
 * @returns {object} 包含webpSrc和originalSrc的对象
 */
export const createPictureSources = (url) => {
    const webpUrl = getWebPUrl(url)

    return {
        webpSrc: webpUrl && webpUrl !== url ? webpUrl : null,
        originalSrc: url
    }
}

/**
 * 预加载WebP图片
 * @param {string} url - 图片URL
 * @returns {Promise<boolean>} 加载是否成功
 */
export const preloadWebPImage = (url) => {
    return new Promise((resolve) => {
        const img = new Image()
        img.onload = () => resolve(true)
        img.onerror = () => resolve(false)
        img.src = url
    })
}

/**
 * 批量预加载图片
 * @param {string[]} urls - 图片URL数组
 * @returns {Promise<boolean[]>} 每个图片的加载结果
 */
export const preloadImages = async (urls) => {
    const promises = urls.map(url => preloadWebPImage(url))
    return Promise.all(promises)
}

/**
 * 获取图片的实际格式（通过URL判断）
 * @param {string} url - 图片URL
 * @returns {string} 图片格式（webp, jpg, png等）
 */
export const getImageFormat = (url) => {
    if (!url) return 'unknown'

    const ext = url.split('.').pop().toLowerCase()
    const validFormats = ['webp', 'jpg', 'jpeg', 'png', 'gif', 'bmp', 'svg']

    return validFormats.includes(ext) ? ext : 'unknown'
}

/**
 * 估算WebP相比原格式的压缩率
 * @param {string} format - 原始格式
 * @returns {number} 预估压缩率（0-1之间）
 */
export const estimateWebPCompression = (format) => {
    const compressionRates = {
        png: 0.64,  // PNG通常能压缩64%
        jpg: 0.58,  // JPG通常能压缩58%
        jpeg: 0.58,
        bmp: 0.70,  // BMP压缩率最高
        gif: 0.60,
        default: 0.60
    }

    return compressionRates[format.toLowerCase()] || compressionRates.default
}

/**
 * 计算预估的WebP文件大小
 * @param {number} originalSize - 原始文件大小（字节）
 * @param {string} format - 原始格式
 * @returns {number} 预估的WebP文件大小（字节）
 */
export const estimateWebPSize = (originalSize, format) => {
    const compressionRate = estimateWebPCompression(format)
    return Math.round(originalSize * (1 - compressionRate))
}

/**
 * 格式化文件大小
 * @param {number} bytes - 字节数
 * @returns {string} 格式化后的大小（如 "1.5 MB"）
 */
export const formatFileSize = (bytes) => {
    if (bytes === 0) return '0 B'

    const k = 1024
    const sizes = ['B', 'KB', 'MB', 'GB']
    const i = Math.floor(Math.log(bytes) / Math.log(k))

    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

// 导出一个默认对象，包含所有工具函数
export default {
    supportsWebP,
    isWebPUrl,
    isNewUploadedImage,
    getWebPUrl,
    createPictureSources,
    preloadWebPImage,
    preloadImages,
    getImageFormat,
    estimateWebPCompression,
    estimateWebPSize,
    formatFileSize
}
