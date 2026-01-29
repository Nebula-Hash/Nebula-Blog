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
    const message = error?.response?.data?.message || error?.message || defaultMessage
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
 * 创建分页配置对象
 * @param {number} pageSize - 每页大小，默认10
 * @returns {Object} 分页配置对象
 */
export const createPagination = (pageSize = 10) => {
    return {
        page: 1,
        pageSize,
        pageCount: 1,
        itemCount: 0,
        showSizePicker: true,
        pageSizes: [10, 20, 50, 100]
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
 * 获取用户名首字母（用于头像占位符）
 * @param {string} username - 用户名
 * @returns {string} 首字母大写
 */
export const getUserInitial = (username) => {
    return username?.charAt(0).toUpperCase() || 'U'
}

/**
 * 验证邮箱格式
 * @param {string} email - 邮箱地址
 * @returns {boolean} 是否有效
 */
export const isValidEmail = (email) => {
    if (!email) return true // 空值视为有效（选填字段）
    return /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/.test(email)
}

/**
 * 验证URL格式
 * @param {string} url - URL地址
 * @returns {boolean} 是否有效
 */
export const isValidUrl = (url) => {
    if (!url) return true // 空值视为有效（选填字段）
    try {
        new URL(url)
        return true
    } catch {
        return false
    }
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
 * 深拷贝对象
 * @param {any} obj - 要拷贝的对象
 * @returns {any} 拷贝后的对象
 */
export const deepClone = (obj) => {
    if (obj === null || typeof obj !== 'object') return obj
    if (obj instanceof Date) return new Date(obj)
    if (obj instanceof Array) return obj.map(item => deepClone(item))

    const clonedObj = {}
    for (const key in obj) {
        if (obj.hasOwnProperty(key)) {
            clonedObj[key] = deepClone(obj[key])
        }
    }
    return clonedObj
}
