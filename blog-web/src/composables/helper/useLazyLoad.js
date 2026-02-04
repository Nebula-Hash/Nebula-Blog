import { ref, onMounted, onUnmounted, getCurrentInstance } from 'vue'

/**
 * 懒加载组合式函数
 * 使用 Intersection Observer API 实现元素懒加载
 * @param {Object} options - 配置选项
 * @param {string} options.rootMargin - 根边距，默认 '50px'
 * @param {number} options.threshold - 触发阈值，默认 0.01
 * @param {Function} options.onVisible - 元素可见时的回调
 * @returns {Object} { targetRef, isLoaded, isVisible }
 */
export function useLazyLoad(options = {}) {
    const {
        rootMargin = '50px',
        threshold = 0.01,
        onVisible = null
    } = options

    const targetRef = ref(null)
    const isLoaded = ref(false)
    const isVisible = ref(false)
    let observer = null

    // 检查是否在组件实例中调用
    const instance = getCurrentInstance()

    const setupObserver = () => {
        if (!targetRef.value) return

        // 检查浏览器是否支持 Intersection Observer
        if (!('IntersectionObserver' in window)) {
            // 不支持则直接加载
            isVisible.value = true
            isLoaded.value = true
            if (onVisible) onVisible()
            return
        }

        observer = new IntersectionObserver(
            (entries) => {
                entries.forEach((entry) => {
                    if (entry.isIntersecting) {
                        isVisible.value = true

                        if (!isLoaded.value) {
                            isLoaded.value = true
                            if (onVisible) onVisible()
                        }

                        // 一旦加载完成，停止观察
                        if (observer && targetRef.value) {
                            observer.unobserve(targetRef.value)
                        }
                    }
                })
            },
            {
                rootMargin,
                threshold
            }
        )

        observer.observe(targetRef.value)
    }

    const cleanupObserver = () => {
        if (observer && targetRef.value) {
            observer.unobserve(targetRef.value)
            observer.disconnect()
        }
    }

    // 只在有组件实例时注册生命周期钩子
    if (instance) {
        onMounted(setupObserver)
        onUnmounted(cleanupObserver)
    }

    return {
        targetRef,
        isLoaded,
        isVisible,
        setupObserver,
        cleanupObserver
    }
}
