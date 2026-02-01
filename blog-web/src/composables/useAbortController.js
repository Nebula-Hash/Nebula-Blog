/**
 * useAbortController 组合式函数
 * 统一管理请求取消逻辑，自动在组件卸载时取消所有请求
 */
import { onUnmounted } from 'vue'

/**
 * 创建可取消的请求控制器
 * @returns {Object} { signal, abort, createSignal }
 */
export function useAbortController() {
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
     * 取消所有请求
     * @param {string} reason - 取消原因
     */
    const abort = (reason = 'Request canceled') => {
        controllers.forEach(controller => {
            if (!controller.signal.aborted) {
                controller.abort(reason)
            }
        })
        controllers.length = 0
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
     * 创建单个 AbortController（不自动管理）
     * @returns {AbortController}
     */
    const createController = () => {
        const controller = new AbortController()
        controllers.push(controller)
        return controller
    }

    // 组件卸载时自动取消所有请求
    onUnmounted(() => {
        abort('Component unmounted')
    })

    return {
        createSignal,
        abort,
        abortSignal,
        createController
    }
}

/**
 * 创建单个可取消的请求
 * 适用于需要手动控制取消的场景
 * @returns {Object} { signal, abort, isAborted }
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

export default useAbortController
