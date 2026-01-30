/**
 * 统一错误处理工具
 * 确保所有用户操作都有一致的错误反馈
 */

import { showError, showWarning } from './common'
import { BUSINESS_CODE } from '@/config/constants'

/**
 * 处理API错误
 * @param {Error} error - 错误对象
 * @param {string} defaultMessage - 默认错误消息
 * @param {boolean} silent - 是否静默处理（不显示提示）
 * @returns {string} 错误消息
 */
export const handleApiError = (error, defaultMessage = '操作失败，请稍后重试', silent = false) => {
    let message = defaultMessage

    // 从响应中提取错误消息
    if (error?.response?.data?.message) {
        message = error.response.data.message
    } else if (error?.message) {
        message = error.message
    }

    // 根据错误码特殊处理
    const code = error?.response?.data?.code
    if (code) {
        message = getErrorMessageByCode(code, message)
    }

    // 显示错误提示
    if (!silent) {
        showError(message)
    }

    // 记录错误日志
    console.error('[ErrorHandler]', {
        message,
        code,
        error
    })

    return message
}

/**
 * 根据错误码获取错误消息
 * @param {number} code - 错误码
 * @param {string} defaultMessage - 默认消息
 * @returns {string} 错误消息
 */
const getErrorMessageByCode = (code, defaultMessage) => {
    const errorMessages = {
        [BUSINESS_CODE.INVALID_CREDENTIALS]: '用户名或密码错误',
        [BUSINESS_CODE.ACCOUNT_LOCKED]: defaultMessage.includes('分钟') ? defaultMessage : '账号已被锁定，请稍后再试',
        [BUSINESS_CODE.USERNAME_EXISTS]: '用户名已存在',
        [BUSINESS_CODE.REGISTER_FREQUENT]: '注册过于频繁，请稍后再试',
        [BUSINESS_CODE.FORBIDDEN]: '权限不足，无法访问',
        [BUSINESS_CODE.NOT_FOUND]: '请求的资源不存在',
        [BUSINESS_CODE.UNAUTHORIZED]: '登录已过期，请重新登录'
    }

    return errorMessages[code] || defaultMessage
}

/**
 * 处理加载数据错误
 * @param {Error} error - 错误对象
 * @param {string} resourceName - 资源名称（如：文章列表、用户信息）
 * @param {boolean} silent - 是否静默处理
 */
export const handleLoadError = (error, resourceName, silent = false) => {
    const message = `加载${resourceName}失败，请稍后重试`
    return handleApiError(error, message, silent)
}

/**
 * 处理保存数据错误
 * @param {Error} error - 错误对象
 * @param {string} action - 操作名称（如：保存、更新、新增）
 * @param {boolean} silent - 是否静默处理
 */
export const handleSaveError = (error, action = '保存', silent = false) => {
    const message = `${action}失败，请稍后重试`
    return handleApiError(error, message, silent)
}

/**
 * 处理删除数据错误
 * @param {Error} error - 错误对象
 * @param {string} resourceName - 资源名称
 * @param {boolean} silent - 是否静默处理
 */
export const handleDeleteError = (error, resourceName = '', silent = false) => {
    const message = resourceName ? `删除${resourceName}失败，请稍后重试` : '删除失败，请稍后重试'
    return handleApiError(error, message, silent)
}

/**
 * 处理表单验证错误
 * @param {Error} error - 验证错误对象
 * @param {boolean} silent - 是否静默处理
 */
export const handleValidationError = (error, silent = false) => {
    // 表单验证错误通常不需要显示提示，因为表单会自动显示错误
    if (!silent && error?.message) {
        showWarning(error.message)
    }
    console.warn('[ValidationError]', error)
}

/**
 * 处理网络错误
 * @param {Error} error - 错误对象
 * @param {boolean} silent - 是否静默处理
 */
export const handleNetworkError = (error, silent = false) => {
    let message = '网络连接失败，请检查网络'

    if (error.code === 'ECONNABORTED' || error.message?.includes('timeout')) {
        message = '请求超时，请检查网络连接'
    } else if (error.response) {
        const status = error.response.status
        switch (status) {
            case 403:
                message = '权限不足，无法访问'
                break
            case 404:
                message = '请求的资源不存在'
                break
            case 500:
            case 502:
            case 503:
            case 504:
                message = '服务器错误，请稍后重试'
                break
            default:
                message = error.message || '网络错误'
        }
    }

    if (!silent) {
        showError(message)
    }

    console.error('[NetworkError]', error)
    return message
}

/**
 * 异步操作包装器
 * 自动处理错误并提供统一的错误反馈
 * @param {Function} asyncFn - 异步函数
 * @param {Object} options - 配置选项
 * @param {string} options.loadingMessage - 加载提示消息
 * @param {string} options.successMessage - 成功提示消息
 * @param {string} options.errorMessage - 错误提示消息
 * @param {boolean} options.silent - 是否静默处理错误
 * @param {Function} options.onSuccess - 成功回调
 * @param {Function} options.onError - 错误回调
 * @returns {Promise<{success: boolean, data?: any, error?: string}>}
 */
export const asyncWrapper = async (asyncFn, options = {}) => {
    const {
        successMessage,
        errorMessage = '操作失败，请稍后重试',
        silent = false,
        onSuccess,
        onError
    } = options

    try {
        const result = await asyncFn()

        // 成功回调
        if (onSuccess) {
            onSuccess(result)
        }

        // 显示成功消息
        if (successMessage) {
            const { showSuccess } = await import('./common')
            showSuccess(successMessage)
        }

        return {
            success: true,
            data: result
        }
    } catch (error) {
        // 处理错误
        const message = handleApiError(error, errorMessage, silent)

        // 错误回调
        if (onError) {
            onError(error, message)
        }

        return {
            success: false,
            error: message
        }
    }
}

/**
 * 创建错误处理器
 * 用于组件中统一处理错误
 * @param {string} componentName - 组件名称（用于日志）
 * @returns {Object} 错误处理器对象
 */
export const createErrorHandler = (componentName) => {
    const logPrefix = `[${componentName}]`

    return {
        /**
         * 处理加载错误
         */
        handleLoad: (error, resourceName, silent = false) => {
            console.error(`${logPrefix} 加载${resourceName}失败:`, error)
            return handleLoadError(error, resourceName, silent)
        },

        /**
         * 处理保存错误
         */
        handleSave: (error, action = '保存', silent = false) => {
            console.error(`${logPrefix} ${action}失败:`, error)
            return handleSaveError(error, action, silent)
        },

        /**
         * 处理删除错误
         */
        handleDelete: (error, resourceName = '', silent = false) => {
            console.error(`${logPrefix} 删除失败:`, error)
            return handleDeleteError(error, resourceName, silent)
        },

        /**
         * 处理通用错误
         */
        handle: (error, message, silent = false) => {
            console.error(`${logPrefix}`, error)
            return handleApiError(error, message, silent)
        }
    }
}

export default {
    handleApiError,
    handleLoadError,
    handleSaveError,
    handleDeleteError,
    handleValidationError,
    handleNetworkError,
    asyncWrapper,
    createErrorHandler
}
