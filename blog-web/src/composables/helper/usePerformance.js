/**
 * usePerformance 组合式函数
 * 提供性能监控功能
 */
import { onMounted, onUnmounted, getCurrentInstance } from 'vue'
import { createPerformanceMonitor } from '@/utils/performance'

/**
 * 使用性能监控（在组件中使用）
 * @param {Object} options - 配置选项
 * @returns {Object} 性能监控实例和方法
 */
export function usePerformance(options = {}) {
    const {
        enableFCP = true,
        enableLCP = true,
        enableFID = true,
        enableCLS = true,
        onMetric = null
    } = options

    const monitor = createPerformanceMonitor()
    const instance = getCurrentInstance()

    const setupMonitoring = () => {
        // 监控首次内容绘制（FCP）
        if (enableFCP) {
            monitor.observeFCP((value) => {
                console.log('[Performance] FCP:', value.toFixed(2), 'ms')
                if (onMetric) onMetric('FCP', value)
            })
        }

        // 监控最大内容绘制（LCP）
        if (enableLCP) {
            monitor.observeLCP((value) => {
                console.log('[Performance] LCP:', value.toFixed(2), 'ms')
                if (onMetric) onMetric('LCP', value)
            })
        }

        // 监控首次输入延迟（FID）
        if (enableFID) {
            monitor.observeFID((value) => {
                console.log('[Performance] FID:', value.toFixed(2), 'ms')
                if (onMetric) onMetric('FID', value)
            })
        }

        // 监控累积布局偏移（CLS）
        if (enableCLS) {
            monitor.observeCLS((value) => {
                console.log('[Performance] CLS:', value.toFixed(4))
                if (onMetric) onMetric('CLS', value)
            })
        }
    }

    // 只在组件实例中注册生命周期钩子
    if (instance) {
        onMounted(setupMonitoring)
        onUnmounted(() => {
            monitor.cleanup()
        })
    } else {
        // 如果不在组件中，立即执行
        setupMonitoring()
    }

    return {
        monitor,
        mark: monitor.mark.bind(monitor),
        measure: monitor.measure.bind(monitor),
        getMetrics: monitor.getAllMetrics.bind(monitor),
        cleanup: monitor.cleanup.bind(monitor)
    }
}

/**
 * 创建全局性能监控（不依赖组件生命周期）
 * @param {Object} options - 配置选项
 * @returns {Object} 性能监控实例和方法
 */
export function createGlobalPerformanceMonitor(options = {}) {
    const {
        enableFCP = true,
        enableLCP = true,
        enableFID = true,
        enableCLS = true,
        onMetric = null
    } = options

    const monitor = createPerformanceMonitor()

    // 立即开始监控
    if (enableFCP) {
        monitor.observeFCP((value) => {
            console.log('[Performance] FCP:', value.toFixed(2), 'ms')
            if (onMetric) onMetric('FCP', value)
        })
    }

    if (enableLCP) {
        monitor.observeLCP((value) => {
            console.log('[Performance] LCP:', value.toFixed(2), 'ms')
            if (onMetric) onMetric('LCP', value)
        })
    }

    if (enableFID) {
        monitor.observeFID((value) => {
            console.log('[Performance] FID:', value.toFixed(2), 'ms')
            if (onMetric) onMetric('FID', value)
        })
    }

    if (enableCLS) {
        monitor.observeCLS((value) => {
            console.log('[Performance] CLS:', value.toFixed(4))
            if (onMetric) onMetric('CLS', value)
        })
    }

    return {
        monitor,
        mark: monitor.mark.bind(monitor),
        measure: monitor.measure.bind(monitor),
        getMetrics: monitor.getAllMetrics.bind(monitor),
        cleanup: monitor.cleanup.bind(monitor)
    }
}
