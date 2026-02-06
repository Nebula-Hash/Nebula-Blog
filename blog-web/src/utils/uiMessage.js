/**
 * 公共工具函数
 */
import { createDiscreteApi } from 'naive-ui'
import { MESSAGE_CONFIG, DEFAULTS } from '@/config/constants'

// 创建独立的 message API 实例
const { message } = createDiscreteApi(['message'])

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

export default {
    showSuccess,
    showError,
    showInfo,
    showWarning
}
