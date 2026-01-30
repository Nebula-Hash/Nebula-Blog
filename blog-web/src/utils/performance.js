/**
 * 性能优化工具
 * 提供防抖、节流等性能优化函数
 */
import { ref, watch } from 'vue'

/**
 * 防抖函数
 * 在事件被触发n秒后再执行回调，如果在这n秒内又被触发，则重新计时
 * 
 * @param {Function} fn - 要防抖的函数
 * @param {number} delay - 延迟时间（毫秒）
 * @returns {Function} 防抖后的函数
 * 
 * @example
 * const debouncedSearch = debounce(() => {
 *   console.log('搜索')
 * }, 500)
 * 
 * input.addEventListener('input', debouncedSearch)
 */
export const debounce = (fn, delay = 300) => {
    let timer = null

    return function (...args) {
        if (timer) {
            clearTimeout(timer)
        }

        timer = setTimeout(() => {
            fn.apply(this, args)
            timer = null
        }, delay)
    }
}

/**
 * 节流函数
 * 规定在一个单位时间内，只能触发一次函数。如果这个单位时间内触发多次函数，只有一次生效
 * 
 * @param {Function} fn - 要节流的函数
 * @param {number} delay - 延迟时间（毫秒）
 * @returns {Function} 节流后的函数
 * 
 * @example
 * const throttledScroll = throttle(() => {
 *   console.log('滚动')
 * }, 300)
 * 
 * window.addEventListener('scroll', throttledScroll)
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
 * Vue 3 组合式 API 专用防抖函数
 * 用于监听 ref 变化并执行防抖回调
 * 
 * @param {Function} callback - 回调函数
 * @param {number} delay - 延迟时间（毫秒）
 * @returns {Function} 防抖后的函数
 * 
 * @example
 * const searchKeyword = ref('')
 * const debouncedSearch = useDebouncedRef(() => {
 *   console.log('搜索:', searchKeyword.value)
 * }, 500)
 * 
 * watch(searchKeyword, debouncedSearch)
 */
export const useDebouncedRef = (callback, delay = 300) => {
    return debounce(callback, delay)
}

/**
 * 创建防抖的搜索函数
 * 专门用于搜索场景，提供更好的用户体验
 * 
 * @param {Function} searchFn - 搜索函数
 * @param {Object} options - 配置选项
 * @param {number} options.delay - 延迟时间（毫秒），默认 500
 * @param {number} options.minLength - 最小搜索长度，默认 2
 * @returns {Function} 防抖后的搜索函数
 * 
 * @example
 * const search = createDebouncedSearch(async (keyword) => {
 *   const results = await searchApi(keyword)
 *   return results
 * }, { delay: 500, minLength: 2 })
 * 
 * search('vue')
 */
export const createDebouncedSearch = (searchFn, options = {}) => {
    const { delay = 500, minLength = 2 } = options

    const debouncedFn = debounce(async (keyword) => {
        if (!keyword || keyword.trim().length < minLength) {
            return null
        }

        return await searchFn(keyword.trim())
    }, delay)

    return debouncedFn
}

/**
 * 创建节流的滚动处理函数
 * 专门用于滚动事件，提供更好的性能
 * 
 * @param {Function} scrollFn - 滚动处理函数
 * @param {number} delay - 延迟时间（毫秒），默认 300
 * @returns {Function} 节流后的滚动处理函数
 * 
 * @example
 * const handleScroll = createThrottledScroll(() => {
 *   console.log('滚动位置:', window.scrollY)
 * }, 300)
 * 
 * window.addEventListener('scroll', handleScroll)
 */
export const createThrottledScroll = (scrollFn, delay = 300) => {
    return throttle(scrollFn, delay)
}

/**
 * 请求动画帧节流
 * 使用 requestAnimationFrame 实现的节流，适用于动画和滚动场景
 * 
 * @param {Function} fn - 要节流的函数
 * @returns {Function} 节流后的函数
 * 
 * @example
 * const rafThrottledFn = rafThrottle(() => {
 *   console.log('RAF 节流')
 * })
 * 
 * window.addEventListener('scroll', rafThrottledFn)
 */
export const rafThrottle = (fn) => {
    let rafId = null

    return function (...args) {
        if (rafId) {
            return
        }

        rafId = requestAnimationFrame(() => {
            fn.apply(this, args)
            rafId = null
        })
    }
}

/**
 * 取消防抖/节流
 * 用于清理防抖或节流函数的定时器
 * 
 * @param {Function} debouncedOrThrottledFn - 防抖或节流后的函数
 * 
 * @example
 * const debouncedFn = debounce(() => {}, 500)
 * // ... 使用 debouncedFn
 * cancel(debouncedFn) // 取消待执行的函数
 */
export const cancel = (debouncedOrThrottledFn) => {
    if (debouncedOrThrottledFn && typeof debouncedOrThrottledFn.cancel === 'function') {
        debouncedOrThrottledFn.cancel()
    }
}

export default {
    debounce,
    throttle,
    useDebouncedRef,
    createDebouncedSearch,
    createThrottledScroll,
    rafThrottle,
    cancel
}
