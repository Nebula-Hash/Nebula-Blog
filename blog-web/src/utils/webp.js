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
    if (url.toLowerCase().endsWith('.webp')) return url

    // 新上传的图片，后端已处理为WebP
    if (isNewUploadedImage(url)) return url

    // 历史图片，尝试替换扩展名
    const ext = url.split('.').pop()
    if (ext && ext.length <= 4) {
        return url.replace(`.${ext}`, '.webp')
    }

    return null
}

// 导出一个默认对象，包含核心工具函数
export default {
    supportsWebP,
    isNewUploadedImage,
    getWebPUrl
}
