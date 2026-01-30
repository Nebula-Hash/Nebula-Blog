/**
 * 性能优化工具函数
 */

/**
 * 防抖函数
 * @param {Function} fn - 要防抖的函数
 * @param {number} delay - 延迟时间（毫秒），默认 300ms
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
 * @param {number} delay - 延迟时间（毫秒），默认 300ms
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
 * 创建防抖的 ref 值更新函数
 * 用于 Vue 3 组合式 API
 * @param {Function} callback - 回调函数
 * @param {number} delay - 延迟时间（毫秒）
 * @returns {Function} 防抖后的函数
 */
export const useDebouncedRef = (callback, delay = 300) => {
    return debounce(callback, delay)
}

export default {
    debounce,
    throttle,
    useDebouncedRef
}
