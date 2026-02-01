import { ref, onUnmounted } from 'vue'

/**
 * 节流组合式函数
 * @param {Function} fn - 要节流的函数
 * @param {number} delay - 延迟时间（毫秒），默认300ms
 * @returns {Function} 节流后的函数
 */
export function useThrottle(fn, delay = 300) {
    const lastRun = ref(0)
    const timeoutId = ref(null)

    const throttledFn = (...args) => {
        const now = Date.now()

        if (now - lastRun.value >= delay) {
            fn(...args)
            lastRun.value = now
        } else {
            // 确保最后一次调用也会执行
            if (timeoutId.value) {
                clearTimeout(timeoutId.value)
            }

            timeoutId.value = setTimeout(() => {
                fn(...args)
                lastRun.value = Date.now()
                timeoutId.value = null
            }, delay - (now - lastRun.value))
        }
    }

    // 清理函数
    onUnmounted(() => {
        if (timeoutId.value) {
            clearTimeout(timeoutId.value)
        }
    })

    return throttledFn
}
