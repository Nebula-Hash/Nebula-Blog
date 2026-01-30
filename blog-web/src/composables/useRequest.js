/**
 * useRequest 组合式函数
 * 提供请求管理功能，包括自动取消、加载状态等
 */
import { ref, onUnmounted } from 'vue'
import { showError } from '@/utils/common'

/**
 * 创建可取消的请求
 * @returns {Object} { loading, error, execute, cancel, abortController }
 */
export function useRequest() {
    const loading = ref(false)
    const error = ref(null)
    const abortController = ref(null)

    /**
     * 执行请求
     * @param {Function} requestFn - 请求函数
     * @param {Object} options - 配置选项
     * @param {boolean} options.showErrorMessage - 是否显示错误消息
     * @param {Function} options.onSuccess - 成功回调
     * @param {Function} options.onError - 错误回调
     * @returns {Promise<any>}
     */
    const execute = async (requestFn, options = {}) => {
        const {
            showErrorMessage = true,
            onSuccess,
            onError
        } = options

        // 取消之前的请求
        cancel()

        // 创建新的 AbortController
        abortController.value = new AbortController()

        loading.value = true
        error.value = null

        try {
            const result = await requestFn(abortController.value.signal)

            if (onSuccess) {
                onSuccess(result)
            }

            return result
        } catch (err) {
            // 如果是取消请求，不处理错误
            if (err.name === 'AbortError' || err.name === 'CanceledError') {
                console.log('[useRequest] 请求已取消')
                return null
            }

            error.value = err

            if (showErrorMessage) {
                showError(err)
            }

            if (onError) {
                onError(err)
            }

            throw err
        } finally {
            loading.value = false
        }
    }

    /**
     * 取消请求
     */
    const cancel = () => {
        if (abortController.value) {
            abortController.value.abort()
            abortController.value = null
        }
    }

    // 组件卸载时自动取消请求
    onUnmounted(() => {
        cancel()
    })

    return {
        loading,
        error,
        execute,
        cancel,
        abortController
    }
}

/**
 * 创建支持取消的请求包装器
 * @param {Function} apiFunction - API函数
 * @returns {Function} 包装后的函数
 */
export function createCancelableRequest(apiFunction) {
    return (signal, ...args) => {
        // 如果API函数支持config参数，添加signal
        const lastArg = args[args.length - 1]
        if (lastArg && typeof lastArg === 'object' && !Array.isArray(lastArg)) {
            lastArg.signal = signal
            return apiFunction(...args)
        } else {
            return apiFunction(...args, { signal })
        }
    }
}

export default {
    useRequest,
    createCancelableRequest
}
