/**
 * 公共工具函数
 */

/**
 * 格式化日期时间
 * @param {string|Date} dateTime - 日期时间
 * @param {string} format - 格式类型 'datetime' | 'date' | 'time'
 * @returns {string} 格式化后的日期时间字符串
 */
export const formatDateTime = (dateTime, format = 'datetime') => {
    if (!dateTime) return '-'

    const date = new Date(dateTime)

    if (isNaN(date.getTime())) return '-'

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
 * @param {string} message - 消息内容
 */
export const showSuccess = (message) => {
    window.$message?.success(message)
}

/**
 * 显示错误消息
 * @param {string|Error} error - 错误信息或错误对象
 * @param {string} defaultMessage - 默认错误消息
 */
export const showError = (error, defaultMessage = '操作失败，请稍后重试') => {
    const message = error?.response?.data?.message || error?.message || error?.toString() || defaultMessage
    window.$message?.error(message)
}

/**
 * 显示信息消息
 * @param {string} message - 消息内容
 */
export const showInfo = (message) => {
    window.$message?.info(message)
}

/**
 * 显示警告消息
 * @param {string} message - 消息内容
 */
export const showWarning = (message) => {
    window.$message?.warning(message)
}

/**
 * 检查用户是否登录，未登录则提示
 * @param {Object} userStore - 用户状态store
 * @param {string} message - 提示消息，默认"请先登录"
 * @returns {boolean} 是否已登录
 */
export const checkLogin = (userStore, message = '请先登录') => {
    if (!userStore.token) {
        showWarning(message)
        return false
    }
    return true
}

/**
 * 验证输入内容是否为空
 * @param {string} value - 输入值
 * @param {string} message - 提示消息
 * @returns {boolean} 是否有效（非空）
 */
export const validateNotEmpty = (value, message = '内容不能为空') => {
    if (!value || !value.trim()) {
        showWarning(message)
        return false
    }
    return true
}

/**
 * 处理异步操作的错误
 * @param {Function} asyncFn - 异步函数
 * @param {string} errorMessage - 错误提示消息
 * @returns {Promise<any>}
 */
export const handleAsyncError = async (asyncFn, errorMessage) => {
    try {
        return await asyncFn()
    } catch (error) {
        console.error(errorMessage, error)
        showError(error, errorMessage)
        throw error
    }
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

/**
 * 防抖函数
 * @param {Function} fn - 要防抖的函数
 * @param {number} delay - 延迟时间（毫秒）
 * @returns {Function} 防抖后的函数
 */
export const debounce = (fn, delay = 300) => {
    let timer = null
    return function (...args) {
        if (timer) clearTimeout(timer)
        timer = setTimeout(() => {
            fn.apply(this, args)
        }, delay)
    }
}

/**
 * 节流函数
 * @param {Function} fn - 要节流的函数
 * @param {number} delay - 延迟时间（毫秒）
 * @returns {Function} 节流后的函数
 */
export const throttle = (fn, delay = 300) => {
    let lastTime = 0
    return function (...args) {
        const now = Date.now()
        if (now - lastTime >= delay) {
            lastTime = now
            fn.apply(this, args)
        }
    }
}

/**
 * 获取相对时间描述（如：刚刚、5分钟前、3小时前）
 * @param {string|Date} dateTime - 日期时间
 * @returns {string} 相对时间描述
 */
export const getRelativeTime = (dateTime) => {
    if (!dateTime) return '-'

    const date = new Date(dateTime)
    if (isNaN(date.getTime())) return '-'

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
