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

/**
 * 性能监控类
 * 用于监控页面性能指标
 */
export class PerformanceMonitor {
    constructor() {
        this.metrics = {}
        this.observers = []
    }

    /**
     * 开始性能标记
     * @param {string} name - 标记名称
     */
    mark(name) {
        if (window.performance && window.performance.mark) {
            window.performance.mark(name)
        }
    }

    /**
     * 测量性能
     * @param {string} name - 测量名称
     * @param {string} startMark - 开始标记
     * @param {string} endMark - 结束标记
     * @returns {number} 持续时间（毫秒）
     */
    measure(name, startMark, endMark) {
        if (window.performance && window.performance.measure) {
            try {
                window.performance.measure(name, startMark, endMark)
                const measure = window.performance.getEntriesByName(name)[0]
                this.metrics[name] = measure.duration
                return measure.duration
            } catch (error) {
                console.warn('[PerformanceMonitor] 测量失败:', error)
                return 0
            }
        }
        return 0
    }

    /**
     * 获取页面加载性能
     * @returns {Object} 性能指标
     */
    getPageLoadMetrics() {
        if (!window.performance || !window.performance.timing) {
            return null
        }

        const timing = window.performance.timing
        const navigation = window.performance.navigation

        return {
            // DNS 查询时间
            dns: timing.domainLookupEnd - timing.domainLookupStart,
            // TCP 连接时间
            tcp: timing.connectEnd - timing.connectStart,
            // 请求时间
            request: timing.responseStart - timing.requestStart,
            // 响应时间
            response: timing.responseEnd - timing.responseStart,
            // DOM 解析时间
            domParse: timing.domInteractive - timing.domLoading,
            // DOM 内容加载完成时间
            domContentLoaded: timing.domContentLoadedEventEnd - timing.domContentLoadedEventStart,
            // 页面完全加载时间
            load: timing.loadEventEnd - timing.loadEventStart,
            // 总时间（从开始到页面完全加载）
            total: timing.loadEventEnd - timing.navigationStart,
            // 重定向次数
            redirectCount: navigation.redirectCount,
            // 导航类型（0: 正常导航, 1: 重新加载, 2: 前进/后退）
            navigationType: navigation.type
        }
    }

    /**
     * 获取资源加载性能
     * @returns {Array} 资源性能列表
     */
    getResourceMetrics() {
        if (!window.performance || !window.performance.getEntriesByType) {
            return []
        }

        const resources = window.performance.getEntriesByType('resource')
        return resources.map(resource => ({
            name: resource.name,
            type: resource.initiatorType,
            duration: resource.duration,
            size: resource.transferSize || 0,
            startTime: resource.startTime
        }))
    }

    /**
     * 监控首次内容绘制（FCP）
     * @param {Function} callback - 回调函数
     */
    observeFCP(callback) {
        if (!window.PerformanceObserver) return

        try {
            const observer = new PerformanceObserver((list) => {
                for (const entry of list.getEntries()) {
                    if (entry.name === 'first-contentful-paint') {
                        callback(entry.startTime)
                        observer.disconnect()
                    }
                }
            })
            observer.observe({ entryTypes: ['paint'] })
            this.observers.push(observer)
        } catch (error) {
            console.warn('[PerformanceMonitor] FCP 监控失败:', error)
        }
    }

    /**
     * 监控最大内容绘制（LCP）
     * @param {Function} callback - 回调函数
     */
    observeLCP(callback) {
        if (!window.PerformanceObserver) return

        try {
            const observer = new PerformanceObserver((list) => {
                const entries = list.getEntries()
                const lastEntry = entries[entries.length - 1]
                callback(lastEntry.startTime)
            })
            observer.observe({ entryTypes: ['largest-contentful-paint'] })
            this.observers.push(observer)
        } catch (error) {
            console.warn('[PerformanceMonitor] LCP 监控失败:', error)
        }
    }

    /**
     * 监控首次输入延迟（FID）
     * @param {Function} callback - 回调函数
     */
    observeFID(callback) {
        if (!window.PerformanceObserver) return

        try {
            const observer = new PerformanceObserver((list) => {
                for (const entry of list.getEntries()) {
                    callback(entry.processingStart - entry.startTime)
                }
            })
            observer.observe({ entryTypes: ['first-input'] })
            this.observers.push(observer)
        } catch (error) {
            console.warn('[PerformanceMonitor] FID 监控失败:', error)
        }
    }

    /**
     * 监控累积布局偏移（CLS）
     * @param {Function} callback - 回调函数
     */
    observeCLS(callback) {
        if (!window.PerformanceObserver) return

        try {
            let clsValue = 0
            const observer = new PerformanceObserver((list) => {
                for (const entry of list.getEntries()) {
                    if (!entry.hadRecentInput) {
                        clsValue += entry.value
                        callback(clsValue)
                    }
                }
            })
            observer.observe({ entryTypes: ['layout-shift'] })
            this.observers.push(observer)
        } catch (error) {
            console.warn('[PerformanceMonitor] CLS 监控失败:', error)
        }
    }

    /**
     * 获取所有指标
     * @returns {Object} 所有性能指标
     */
    getAllMetrics() {
        return {
            custom: this.metrics,
            pageLoad: this.getPageLoadMetrics(),
            resources: this.getResourceMetrics()
        }
    }

    /**
     * 清理所有观察器
     */
    cleanup() {
        this.observers.forEach(observer => observer.disconnect())
        this.observers = []
    }
}

/**
 * 创建性能监控实例
 * @returns {PerformanceMonitor}
 */
export const createPerformanceMonitor = () => {
    return new PerformanceMonitor()
}

export default {
    debounce,
    throttle,
    useDebouncedRef,
    createDebouncedSearch,
    createThrottledScroll,
    rafThrottle,
    cancel,
    PerformanceMonitor,
    createPerformanceMonitor
}
