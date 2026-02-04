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


