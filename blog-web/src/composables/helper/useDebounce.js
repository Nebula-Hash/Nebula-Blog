import { onUnmounted } from 'vue'
import { debounce } from '@/utils/performance'

/**
 * 防抖组合式函数（Vue 版本）
 * 内部使用 utils/performance.js 的 debounce 核心逻辑
 * 添加 Vue 特定的生命周期管理
 * 
 * @param {Function} fn - 要防抖的函数
 * @param {number} delay - 延迟时间（毫秒），默认300ms
 * @returns {Function} 防抖后的函数
 */
export function useDebounce(fn, delay = 300) {
    // 使用工具函数的核心逻辑
    const debouncedFn = debounce(fn, delay)

    // Vue 特定：组件卸载时的清理逻辑
    // 注意：debounce 返回的函数本身是无状态的，不需要特殊清理
    // 但我们保留这个钩子以便未来扩展
    onUnmounted(() => {
        // 如果未来需要清理逻辑，可以在这里添加
    })

    return debouncedFn
}
