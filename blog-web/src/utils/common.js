/**
 * 公共工具函数
 */
import { createDiscreteApi } from 'naive-ui'
import { MESSAGE_CONFIG, DEFAULTS } from '@/config/constants'

// 创建独立的 message API 实例
const { message } = createDiscreteApi(['message'])

/**
 * 格式化日期时间
 * @param {string|Date} dateTime - 日期时间
 * @param {string} format - 格式类型 'datetime' | 'date' | 'time'
 * @returns {string} 格式化后的日期时间字符串
 */
export const formatDateTime = (dateTime, format = 'datetime') => {
    if (!dateTime) return DEFAULTS.EMPTY_TEXT

    const date = new Date(dateTime)

    if (isNaN(date.getTime())) return DEFAULTS.EMPTY_TEXT

    const options = {
        datetime: {
            year: 'numeric',
            month: '2-digit',
            day: '2-digit',
            hour: '2-digit',
            minute: '2-digit',
            second: '2-digit',
            hour12: false
        },
        date: {
            year: 'numeric',
            month: '2-digit',
            day: '2-digit'
        },
        time: {
            hour: '2-digit',
            minute: '2-digit',
            second: '2-digit',
            hour12: false
        }
    }

    return date.toLocaleString('zh-CN', options[format] || options.datetime)
}

/**
 * 显示成功消息
 * @param {string} content - 消息内容
 * @param {number} duration - 显示时长（毫秒）
 */
export const showSuccess = (content, duration = MESSAGE_CONFIG.SUCCESS_DURATION) => {
    message.success(content, { duration })
}

/**
 * 显示错误消息
 * @param {string|Error} error - 错误信息或错误对象
 * @param {string} defaultMessage - 默认错误消息
 * @param {number} duration - 显示时长（毫秒）
 */
export const showError = (error, defaultMessage = DEFAULTS.DEFAULT_ERROR_MESSAGE, duration = MESSAGE_CONFIG.ERROR_DURATION) => {
    const content = error?.response?.data?.message || error?.message || error?.toString() || defaultMessage
    message.error(content, { duration })
}

/**
 * 显示信息消息
 * @param {string} content - 消息内容
 * @param {number} duration - 显示时长（毫秒）
 */
export const showInfo = (content, duration = MESSAGE_CONFIG.INFO_DURATION) => {
    message.info(content, { duration })
}

/**
 * 显示警告消息
 * @param {string} content - 消息内容
 * @param {number} duration - 显示时长（毫秒）
 */
export const showWarning = (content, duration = MESSAGE_CONFIG.WARNING_DURATION) => {
    message.warning(content, { duration })
}

/**
 * 验证输入内容是否为空
 * @param {string} value - 输入值
 * @param {string} content - 提示消息
 * @returns {boolean} 是否有效（非空）
 */
export const validateNotEmpty = (value, content = '内容不能为空') => {
    if (!value || !value.trim()) {
        showWarning(content)
        return false
    }
    return true
}

/**
 * 格式化数字（添加千分位）
 * @param {number} num - 数字
 * @returns {string} 格式化后的字符串
 */
export const formatNumber = (num) => {
    if (num === null || num === undefined) return '0'
    return num.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',')
}

/**
 * 截断文本
 * @param {string} text - 文本内容
 * @param {number} maxLength - 最大长度
 * @param {string} suffix - 后缀，默认 '...'
 * @returns {string} 截断后的文本
 */
export const truncateText = (text, maxLength, suffix = '...') => {
    if (!text || text.length <= maxLength) return text || ''
    return text.substring(0, maxLength) + suffix
}

// 从 performance.js 导入防抖和节流函数，避免重复实现
export { debounce, throttle } from './performance'

/**
 * 获取相对时间描述（如：刚刚、5分钟前、3小时前）
 * @param {string|Date} dateTime - 日期时间
 * @returns {string} 相对时间描述
 */
export const getRelativeTime = (dateTime) => {
    if (!dateTime) return DEFAULTS.EMPTY_TEXT

    const date = new Date(dateTime)
    if (isNaN(date.getTime())) return DEFAULTS.EMPTY_TEXT

    const now = new Date()
    const diff = now - date
    const seconds = Math.floor(diff / 1000)
    const minutes = Math.floor(seconds / 60)
    const hours = Math.floor(minutes / 60)
    const days = Math.floor(hours / 24)
    const months = Math.floor(days / 30)
    const years = Math.floor(days / 365)

    if (seconds < 60) return '刚刚'
    if (minutes < 60) return `${minutes}分钟前`
    if (hours < 24) return `${hours}小时前`
    if (days < 30) return `${days}天前`
    if (months < 12) return `${months}个月前`
    return `${years}年前`
}
