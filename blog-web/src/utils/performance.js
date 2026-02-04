/**
 * 性能优化工具
 * 提供防抖、节流等性能优化函数
 */

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
     * 使用 Navigation Timing API Level 2，降级到 Level 1
     * @returns {Object|null} 性能指标
     */
    getPageLoadMetrics() {
        if (!window.performance) {
            return null
        }

        // 优先使用 Navigation Timing API Level 2
        const navigationEntry = performance.getEntriesByType('navigation')[0]

        if (navigationEntry) {
            // Navigation Timing API Level 2
            return {
                // DNS 查询时间
                dns: navigationEntry.domainLookupEnd - navigationEntry.domainLookupStart,
                // TCP 连接时间
                tcp: navigationEntry.connectEnd - navigationEntry.connectStart,
                // SSL/TLS 握手时间
                ssl: navigationEntry.secureConnectionStart > 0
                    ? navigationEntry.connectEnd - navigationEntry.secureConnectionStart
                    : 0,
                // 请求时间
                request: navigationEntry.responseStart - navigationEntry.requestStart,
                // 响应时间
                response: navigationEntry.responseEnd - navigationEntry.responseStart,
                // DOM 解析时间
                domParse: navigationEntry.domInteractive - navigationEntry.domLoading,
                // DOM 内容加载完成时间
                domContentLoaded: navigationEntry.domContentLoadedEventEnd - navigationEntry.domContentLoadedEventStart,
                // 页面完全加载时间
                load: navigationEntry.loadEventEnd - navigationEntry.loadEventStart,
                // 总时间（从开始到页面完全加载）
                total: navigationEntry.loadEventEnd - navigationEntry.fetchStart,
                // 重定向时间
                redirect: navigationEntry.redirectEnd - navigationEntry.redirectStart,
                // 重定向次数
                redirectCount: navigationEntry.redirectCount || 0,
                // 导航类型
                navigationType: navigationEntry.type,
                // 传输大小
                transferSize: navigationEntry.transferSize || 0,
                // 编码大小
                encodedBodySize: navigationEntry.encodedBodySize || 0,
                // 解码大小
                decodedBodySize: navigationEntry.decodedBodySize || 0
            }
        }

        // 降级到 Navigation Timing API Level 1（已废弃但仍可用）
        if (window.performance.timing) {
            const timing = window.performance.timing
            const navigation = window.performance.navigation

            return {
                // DNS 查询时间
                dns: timing.domainLookupEnd - timing.domainLookupStart,
                // TCP 连接时间
                tcp: timing.connectEnd - timing.connectStart,
                // SSL/TLS 握手时间
                ssl: timing.secureConnectionStart > 0
                    ? timing.connectEnd - timing.secureConnectionStart
                    : 0,
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
                // 重定向时间
                redirect: timing.redirectEnd - timing.redirectStart,
                // 重定向次数
                redirectCount: navigation ? navigation.redirectCount : 0,
                // 导航类型（0: 正常导航, 1: 重新加载, 2: 前进/后退）
                navigationType: navigation ? navigation.type : 0,
                // Level 1 不支持传输大小
                transferSize: 0,
                encodedBodySize: 0,
                decodedBodySize: 0
            }
        }

        return null
    }

    /**
     * 获取资源加载性能
     * @param {Object} options - 筛选选项
     * @param {string} options.type - 资源类型（script, stylesheet, img等）
     * @param {number} options.minDuration - 最小持续时间（毫秒）
     * @returns {Array} 资源性能列表
     */
    getResourceMetrics(options = {}) {
        if (!window.performance || !window.performance.getEntriesByType) {
            return []
        }

        const { type = null, minDuration = 0 } = options

        const resources = window.performance.getEntriesByType('resource')

        return resources
            .filter(resource => {
                // 按类型筛选
                if (type && resource.initiatorType !== type) {
                    return false
                }
                // 按最小持续时间筛选
                if (minDuration > 0 && resource.duration < minDuration) {
                    return false
                }
                return true
            })
            .map(resource => ({
                name: resource.name,
                type: resource.initiatorType,
                duration: resource.duration,
                // 传输大小（字节）
                transferSize: resource.transferSize || 0,
                // 编码大小（字节）
                encodedBodySize: resource.encodedBodySize || 0,
                // 解码大小（字节）
                decodedBodySize: resource.decodedBodySize || 0,
                startTime: resource.startTime,
                // 是否使用缓存
                cached: resource.transferSize === 0 && resource.decodedBodySize > 0
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

    /**
     * 获取核心 Web Vitals 指标
     * 返回一个 Promise，在所有指标收集完成后 resolve
     * @param {number} timeout - 超时时间（毫秒），默认 10000
     * @returns {Promise<Object>} 核心指标对象
     */
    getCoreWebVitals(timeout = 10000) {
        return new Promise((resolve) => {
            const metrics = {
                FCP: null,
                LCP: null,
                FID: null,
                CLS: null
            }

            let resolveTimer = null

            const checkComplete = () => {
                // 如果所有指标都已收集，立即 resolve
                if (metrics.FCP !== null && metrics.LCP !== null &&
                    metrics.FID !== null && metrics.CLS !== null) {
                    if (resolveTimer) clearTimeout(resolveTimer)
                    resolve(metrics)
                }
            }

            // FCP
            this.observeFCP((value) => {
                metrics.FCP = value
                checkComplete()
            })

            // LCP
            this.observeLCP((value) => {
                metrics.LCP = value
                checkComplete()
            })

            // FID
            this.observeFID((value) => {
                metrics.FID = value
                checkComplete()
            })

            // CLS
            this.observeCLS((value) => {
                metrics.CLS = value
                // CLS 会持续更新，不触发 checkComplete
            })

            // 超时后返回已收集的指标
            resolveTimer = setTimeout(() => {
                resolve(metrics)
            }, timeout)
        })
    }

    /**
     * 获取性能评分
     * 基于 Lighthouse 的评分标准
     * @returns {Object} 性能评分对象
     */
    getPerformanceScore() {
        const pageLoad = this.getPageLoadMetrics()
        if (!pageLoad) return null

        // Lighthouse 评分标准
        const scores = {
            FCP: this.calculateScore(pageLoad.total, 1800, 3000), // FCP < 1.8s 为好
            LCP: this.calculateScore(pageLoad.total, 2500, 4000), // LCP < 2.5s 为好
            TTI: this.calculateScore(pageLoad.total, 3800, 7300), // TTI < 3.8s 为好
            overall: 0
        }

        // 计算总分（简化版）
        scores.overall = Math.round(
            (scores.FCP + scores.LCP + scores.TTI) / 3
        )

        return scores
    }

    /**
     * 计算性能评分
     * @param {number} value - 实际值
     * @param {number} good - 良好阈值
     * @param {number} poor - 较差阈值
     * @returns {number} 评分（0-100）
     */
    calculateScore(value, good, poor) {
        if (value <= good) return 100
        if (value >= poor) return 0

        // 线性插值
        const ratio = (value - good) / (poor - good)
        return Math.round(100 - (ratio * 100))
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
    createDebouncedSearch,
    PerformanceMonitor,
    createPerformanceMonitor
}
