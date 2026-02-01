/**
 * useDebounce 组合式函数
 * 提供防抖功能的简化封装
 */
import { debounce } from '@/utils/performance'

/**
 * 创建防抖函数
 * @param {Function} fn - 要防抖的函数
 * @param {number} delay - 延迟时间（毫秒）
 * @returns {Function} 防抖后的函数
 */
export function useDebounce(fn, delay = 300) {
    return debounce(fn, delay)
}

export default useDebounce
