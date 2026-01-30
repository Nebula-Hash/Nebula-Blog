/**
 * 统一错误处理工具
 * 提供一致的错误处理和用户反馈
 */
import { showError } from '@/utils/common'

/**
 * 处理 API 错误
 * @param {Error} error - 错误对象
 * @param {string} operation - 操作名称
 * @param {boolean} silent - 是否静默（不显示错误提示）
 * @returns {boolean} 是否成功处理
 */
export const handleApiError = (error, operation = '操作', silent = false) => {
    console.error(`[ErrorHandler] ${operation}失败:`, error)

    if (!silent) {
        const message = error?.response?.data?.message || error?.message || `${operation}失败，请稍后重试`
        showError(message)
    }

    return false
}

/**
 * 处理加载数据错误
 * @param {Error} error - 错误对象
 * @param {string} resourceName - 资源名称
 * @param {boolean} silent - 是否静默
 * @returns {boolean} 是否成功处理
 */
export const handleLoadError = (error, resourceName = '数据', silent = false) => {
    return handleApiError(error, `加载${resourceName}`, silent)
}

/**
 * 处理保存数据错误
 * @param {Error} error - 错误对象
 * @param {string} operation - 操作名称
 * @param {boolean} silent - 是否静默
 * @returns {boolean} 是否成功处理
 */
export const handleSaveError = (error, operation = '保存', silent = false) => {
    return handleApiError(error, operation, silent)
}

/**
 * 处理删除数据错误
 * @param {Error} error - 错误对象
 * @param {string} resourceName - 资源名称
 * @param {boolean} silent - 是否静默
 * @returns {boolean} 是否成功处理
 */
export const handleDeleteError = (error, resourceName = '数据', silent = false) => {
    return handleApiError(error, `删除${resourceName}`, silent)
}

/**
 * 异步操作包装器 - 自动处理错误
 * @param {Function} asyncFn - 异步函数
 * @param {Object} options - 配置选项
 * @param {string} options.operation - 操作名称
 * @param {boolean} options.silent - 是否静默
 * @param {Function} options.onError - 自定义错误处理函数
 * @returns {Promise<any>} 返回异步函数的结果，错误时返回 null
 */
export const asyncWrapper = async (asyncFn, options = {}) => {
    const { operation = '操作', silent = false, onError } = options

    try {
        return await asyncFn()
    } catch (error) {
        if (onError) {
            onError(error)
        } else {
            handleApiError(error, operation, silent)
        }
        return null
    }
}

/**
 * 创建组件级错误处理器
 * @param {string} componentName - 组件名称
 * @returns {Object} 错误处理器对象
 */
export const createErrorHandler = (componentName) => {
    return {
        /**
         * 处理加载错误
         */
        handleLoad: (error, resourceName, silent = false) => {
            console.error(`[${componentName}] 加载${resourceName}失败:`, error)
            return handleLoadError(error, resourceName, silent)
        },

        /**
         * 处理保存错误
         */
        handleSave: (error, operation, silent = false) => {
            console.error(`[${componentName}] ${operation}失败:`, error)
            return handleSaveError(error, operation, silent)
        },

        /**
         * 处理删除错误
         */
        handleDelete: (error, resourceName, silent = false) => {
            console.error(`[${componentName}] 删除${resourceName}失败:`, error)
            return handleDeleteError(error, resourceName, silent)
        },

        /**
         * 处理通用错误
         */
        handle: (error, operation, silent = false) => {
            console.error(`[${componentName}] ${operation}失败:`, error)
            return handleApiError(error, operation, silent)
        },

        /**
         * 异步操作包装器
         */
        wrap: (asyncFn, options = {}) => {
            return asyncWrapper(asyncFn, {
                ...options,
                operation: options.operation || componentName
            })
        }
    }
}

export default {
    handleApiError,
    handleLoadError,
    handleSaveError,
    handleDeleteError,
    asyncWrapper,
    createErrorHandler
}
