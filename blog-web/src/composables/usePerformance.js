/**
 * usePerformance 组合式函数
 * 提供性能监控功能
 */
import { onMounted, onUnmounted } from 'vue'
import { createPerformanceMonitor } from '@/utils/performance'

/**
 * 使用性能监控
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

    onMounted(() => {
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
    })

    onUnmounted(() => {
        monitor.cleanup()
    })

    return {
        monitor,
        mark: monitor.mark.bind(monitor),
        measure: monitor.measure.bind(monitor),
        getMetrics: monitor.getAllMetrics.bind(monitor)
    }
}

export default usePerformance
