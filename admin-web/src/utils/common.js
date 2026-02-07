/**
 * 公共工具函数
 */

import { createDiscreteApi } from 'naive-ui'
import { MESSAGE_CONFIG, PAGINATION_CONFIG, DEFAULTS } from '@/config/constants'

// 创建独立的 Naive UI API 实例（不依赖 Vue 上下文）
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
    let content = defaultMessage

    if (typeof error === 'string') {
        content = error
    } else if (error?.response?.data?.message) {
        content = error.response.data.message
    } else if (error?.message) {
        content = error.message
    }

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
 * 创建分页配置对象
 * @param {number} pageSize - 每页大小
 * @returns {Object} 分页配置对象
 */
export const createPagination = (pageSize = PAGINATION_CONFIG.DEFAULT_PAGE_SIZE) => {
    return {
        page: 1,
        pageSize,
        pageCount: 1,
        itemCount: 0,
        showSizePicker: PAGINATION_CONFIG.SHOW_SIZE_PICKER,
        pageSizes: PAGINATION_CONFIG.PAGE_SIZE_OPTIONS
    }
}

/**
 * 更新分页数据
 * @param {Object} pagination - 分页对象
 * @param {Object} responseData - 响应数据（包含 total 和 pages）
 */
export const updatePagination = (pagination, responseData) => {
    pagination.itemCount = responseData.total || 0
    pagination.pageCount = responseData.pages || 1
}

/**
 * 获取用户名首字符（用于头像占位符）
 * 中文用户名返回第一个汉字，英文用户名返回首字母大写
 * @param {string} username - 用户名
 * @returns {string} 首字符
 */
export const getUserInitial = (username) => {
    if (!username) return DEFAULTS.AVATAR_PLACEHOLDER

    const firstChar = username.charAt(0)

    // 判断是否为中文字符（Unicode 范围：\u4e00-\u9fa5）
    if (/[\u4e00-\u9fa5]/.test(firstChar)) {
        return firstChar
    }

    // 英文字符返回大写
    return firstChar.toUpperCase()
}

/**
 * 验证邮箱格式
 * @param {string} email - 邮箱地址
 * @param {boolean} required - 是否必填，默认 false（选填时空值返回 true）
 * @returns {boolean} 是否有效
 */
export const isValidEmail = (email, required = false) => {
    // 空值处理
    if (!email) {
        return !required // 必填时空值无效，选填时空值有效
    }

    // 邮箱格式验证
    return /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/.test(email)
}

/**
 * 验证URL格式
 * @param {string} url - URL地址
 * @param {boolean} required - 是否必填，默认 false（选填时空值返回 true）
 * @returns {boolean} 是否有效
 */
export const isValidUrl = (url, required = false) => {
    // 空值处理
    if (!url) {
        return !required // 必填时空值无效，选填时空值有效
    }

    // URL格式验证
    try {
        new URL(url)
        return true
    } catch {
        return false
    }
}


