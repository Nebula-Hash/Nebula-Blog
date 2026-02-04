/**
 * useRequest 组合式函数
 * 提供请求管理功能，包括自动取消、加载状态、多请求管理等
 * 内部使用 utils/request.js 的 axios 实例
 */
import { ref, onUnmounted } from 'vue'
import request from '@/utils/request'
import { showError } from '@/utils/common'

/**
 * 创建可取消的单个请求
 * 适用于单个请求的场景，自动管理加载状态和错误处理
 * 内部使用 utils/request.js 的 axios 实例
 * 
 * @returns {Object} { loading, error, data, execute, cancel, isAborted }
 * 
 * @example
 * const { loading, data, execute } = useRequest()
 * 
 * // 执行请求
 * await execute('/api/articles', { method: 'GET' })
 */
export function useRequest() {
    const loading = ref(false)
    const error = ref(null)
    const data = ref(null)
    const abortController = ref(null)

    /**
     * 执行请求
     * @param {string|Object} urlOrConfig - URL 字符串或完整的 axios 配置对象
     * @param {Object} options - 配置选项
     * @param {boolean} options.showErrorMessage - 是否显示错误消息，默认 true
     * @param {Function} options.onSuccess - 成功回调
     * @param {Function} options.onError - 错误回调
     * @param {boolean} options.throwError - 是否抛出错误，默认 true
     * @returns {Promise<any>}
     */
    const execute = async (urlOrConfig, options = {}) => {
        const {
            showErrorMessage = true,
            onSuccess,
            onError,
            throwError = true
        } = options

        // 取消之前的请求
        cancel()

        // 创建新的 AbortController
        abortController.value = new AbortController()

        loading.value = true
        error.value = null

        try {
            // 构建请求配置
            const config = typeof urlOrConfig === 'string'
                ? { url: urlOrConfig }
                : urlOrConfig

            // 使用 utils/request.js 的 axios 实例
            const result = await request({
                ...config,
                signal: abortController.value.signal
            })

            data.value = result

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

            if (throwError) {
                throw err
            }

            return null
        } finally {
            loading.value = false
        }
    }

    /**
     * 取消当前请求
     * @param {string} reason - 取消原因
     */
    const cancel = (reason = 'Request canceled') => {
        if (abortController.value && !abortController.value.signal.aborted) {
            abortController.value.abort(reason)
            abortController.value = null
        }
    }

    /**
     * 检查请求是否已取消
     * @returns {boolean}
     */
    const isAborted = () => {
        return abortController.value?.signal.aborted || false
    }

    // 组件卸载时自动取消请求
    onUnmounted(() => {
        cancel('Component unmounted')
    })

    return {
        loading,
        error,
        data,
        execute,
        cancel,
        isAborted
    }
}

/**
 * 创建多请求管理器
 * 适用于需要同时管理多个请求的场景
 * 
 * @returns {Object} { createSignal, abort, abortSignal, abortAll, createController, getActiveCount }
 * 
 * @example
 * const { createSignal, abortAll } = useMultipleRequests()
 * 
 * // 创建多个请求
 * const signal1 = createSignal()
 * const signal2 = createSignal()
 * 
 * // 取消所有请求
 * abortAll()
 */
export function useMultipleRequests() {
    const controllers = []

    /**
     * 创建新的 AbortController 并返回 signal
     * @returns {AbortSignal}
     */
    const createSignal = () => {
        const controller = new AbortController()
        controllers.push(controller)
        return controller.signal
    }

    /**
     * 创建新的 AbortController
     * @returns {AbortController}
     */
    const createController = () => {
        const controller = new AbortController()
        controllers.push(controller)
        return controller
    }

    /**
     * 取消指定的请求
     * @param {AbortSignal} signal - 要取消的信号
     * @param {string} reason - 取消原因
     */
    const abortSignal = (signal, reason = 'Request canceled') => {
        const index = controllers.findIndex(c => c.signal === signal)
        if (index !== -1) {
            const controller = controllers[index]
            if (!controller.signal.aborted) {
                controller.abort(reason)
            }
            controllers.splice(index, 1)
        }
    }

    /**
     * 取消所有请求
     * @param {string} reason - 取消原因
     */
    const abortAll = (reason = 'All requests canceled') => {
        controllers.forEach(controller => {
            if (!controller.signal.aborted) {
                controller.abort(reason)
            }
        })
        controllers.length = 0
    }

    /**
     * 别名：取消所有请求
     * @param {string} reason - 取消原因
     */
    const abort = abortAll

    /**
     * 获取活跃的请求数量
     * @returns {number}
     */
    const getActiveCount = () => {
        return controllers.filter(c => !c.signal.aborted).length
    }

    // 组件卸载时自动取消所有请求
    onUnmounted(() => {
        abortAll('Component unmounted')
    })

    return {
        createSignal,
        createController,
        abort,
        abortSignal,
        abortAll,
        getActiveCount
    }
}

/**
 * 创建单个可取消的请求（简化版）
 * 适用于需要手动控制取消的场景，不包含加载状态管理
 * 
 * @returns {Object} { signal, abort, isAborted }
 * 
 * @example
 * const { signal, abort } = useSingleAbortController()
 * 
 * // 使用 signal
 * fetch('/api/data', { signal })
 * 
 * // 取消请求
 * abort()
 */
export function useSingleAbortController() {
    const controller = new AbortController()

    const abort = (reason = 'Request canceled') => {
        if (!controller.signal.aborted) {
            controller.abort(reason)
        }
    }

    const isAborted = () => controller.signal.aborted

    // 组件卸载时自动取消
    onUnmounted(() => {
        abort('Component unmounted')
    })

    return {
        signal: controller.signal,
        abort,
        isAborted
    }
}

// 默认导出
export default {
    useRequest,
    useMultipleRequests,
    useSingleAbortController
}
