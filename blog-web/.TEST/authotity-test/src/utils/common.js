/**
 * 公共工具函数（认证相关）
 */
import { createDiscreteApi } from 'naive-ui'

// 创建独立的 message API 实例
const { message } = createDiscreteApi(['message'])

// 消息配置
const MESSAGE_CONFIG = {
    SUCCESS_DURATION: 3000,
    ERROR_DURATION: 5000,
    WARNING_DURATION: 4000,
    INFO_DURATION: 3000
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
export const showError = (error, defaultMessage = '操作失败，请稍后重试', duration = MESSAGE_CONFIG.ERROR_DURATION) => {
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
 * 检查用户是否登录，未登录则提示
 * @param {Object} userStore - 用户状态store
 * @param {string} content - 提示消息，默认"请先登录"
 * @returns {boolean} 是否已登录
 */
export const checkLogin = (userStore, content = '请先登录') => {
    if (!userStore.token) {
        showWarning(content)
        return false
    }
    return true
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
