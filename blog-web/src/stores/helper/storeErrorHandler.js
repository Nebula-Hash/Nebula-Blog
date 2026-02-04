/**
 * Store 统一错误处理工具
 * 提供统一的错误处理和用户提示
 */

/**
 * 创建 Store 错误处理器
 * @param {string} storeName - Store 名称
 * @param {Object} options - 配置选项
 * @returns {Object}
 */
export function createStoreErrorHandler(storeName, options = {}) {
    const {
        showNotification = true,
        logToConsole = true
    } = options

    /**
     * 处理错误
     * @param {Error} error - 错误对象
     * @param {string} operation - 操作名称
     * @param {Function} globalStoreSetError - 全局 store 的 setError 方法
     */
    function handleError(error, operation, globalStoreSetError) {
        const errorMessage = error.message || '操作失败'
        const fullMessage = `[${storeName}] ${operation}失败: ${errorMessage}`

        // 记录到控制台
        if (logToConsole) {
            console.error(fullMessage, error)
        }

        // 设置到全局错误状态
        if (globalStoreSetError) {
            globalStoreSetError({
                message: errorMessage,
                code: error.code,
                operation,
                storeName
            })
        }

        return {
            success: false,
            error: errorMessage
        }
    }

    /**
     * 包装异步操作，自动处理错误
     * @param {Function} asyncFn - 异步函数
     * @param {string} operation - 操作名称
     * @param {Function} globalStoreSetError - 全局 store 的 setError 方法
     */
    async function wrapAsync(asyncFn, operation, globalStoreSetError) {
        try {
            const result = await asyncFn()
            return {
                success: true,
                data: result
            }
        } catch (error) {
            return handleError(error, operation, globalStoreSetError)
        }
    }

    return {
        handleError,
        wrapAsync
    }
}

/**
 * 错误类型枚举
 */
export const ErrorType = {
    NETWORK: 'NETWORK_ERROR',
    VALIDATION: 'VALIDATION_ERROR',
    AUTH: 'AUTH_ERROR',
    NOT_FOUND: 'NOT_FOUND',
    SERVER: 'SERVER_ERROR',
    UNKNOWN: 'UNKNOWN_ERROR'
}

/**
 * 根据错误对象判断错误类型
 */
export function getErrorType(error) {
    if (!error) return ErrorType.UNKNOWN

    if (error.code === 'ECONNABORTED' || error.message?.includes('timeout')) {
        return ErrorType.NETWORK
    }

    if (error.response) {
        const status = error.response.status
        if (status === 401 || status === 403) return ErrorType.AUTH
        if (status === 404) return ErrorType.NOT_FOUND
        if (status >= 500) return ErrorType.SERVER
        if (status >= 400) return ErrorType.VALIDATION
    }

    return ErrorType.UNKNOWN
}

/**
 * 获取用户友好的错误消息
 */
export function getUserFriendlyMessage(error) {
    const errorType = getErrorType(error)

    const messages = {
        [ErrorType.NETWORK]: '网络连接失败，请检查网络设置',
        [ErrorType.VALIDATION]: '输入数据有误，请检查后重试',
        [ErrorType.AUTH]: '您没有权限执行此操作',
        [ErrorType.NOT_FOUND]: '请求的资源不存在',
        [ErrorType.SERVER]: '服务器错误，请稍后重试',
        [ErrorType.UNKNOWN]: '操作失败，请重试'
    }

    return messages[errorType] || messages[ErrorType.UNKNOWN]
}
