import { onUnmounted } from 'vue'
import { throttle } from '@/utils/performance'

/**
 * 节流组合式函数（Vue 版本）
 * 内部使用 utils/performance.js 的 throttle 核心逻辑
 * 添加 Vue 特定的生命周期管理
 * 
 * @param {Function} fn - 要节流的函数
 * @param {number} delay - 延迟时间（毫秒），默认300ms
 * @returns {Function} 节流后的函数
 */
export function useThrottle(fn, delay = 300) {
    // 使用工具函数的核心逻辑
    const throttledFn = throttle(fn, delay)

    // Vue 特定：组件卸载时的清理逻辑
    // 注意：throttle 返回的函数本身是无状态的，不需要特殊清理
    // 但我们保留这个钩子以便未来扩展
    onUnmounted(() => {
        // 如果未来需要清理逻辑，可以在这里添加
    })

    return throttledFn
}
