import { ref, onMounted, onUnmounted } from 'vue'

/**
 * 无限滚动组合式函数
 * 使用 Intersection Observer 监听触发元素，实现无限滚动加载
 * @param {Function} loadMore - 加载更多的回调函数
 * @param {Object} options - 配置选项
 * @param {string} options.rootMargin - 根边距，默认 '100px'
 * @param {number} options.threshold - 触发阈值，默认 0.01
 * @param {boolean} options.immediate - 是否立即执行一次加载，默认 false
 * @returns {Object} { targetRef, loading, hasMore, trigger }
 */
export function useInfiniteScroll(loadMore, options = {}) {
    const {
        rootMargin = '100px',
        threshold = 0.01,
        immediate = false
    } = options

    const targetRef = ref(null)
    const loading = ref(false)
    const hasMore = ref(true)
    let observer = null

    /**
     * 手动触发加载
     */
    const trigger = async () => {
        if (loading.value || !hasMore.value) return

        loading.value = true
        try {
            const result = await loadMore()
            // 如果回调返回 false，表示没有更多数据
            if (result === false) {
                hasMore.value = false
            }
        } catch (error) {
            console.error('[InfiniteScroll] 加载失败:', error)
            throw error
        } finally {
            loading.value = false
        }
    }

    onMounted(() => {
        // 立即执行一次加载
        if (immediate) {
            trigger()
        }

        if (!targetRef.value) return

        // 检查浏览器是否支持 Intersection Observer
        if (!('IntersectionObserver' in window)) {
            console.warn('[InfiniteScroll] 浏览器不支持 Intersection Observer')
            return
        }

        observer = new IntersectionObserver(
            (entries) => {
                entries.forEach((entry) => {
                    if (entry.isIntersecting && !loading.value && hasMore.value) {
                        trigger()
                    }
                })
            },
            {
                rootMargin,
                threshold
            }
        )

        observer.observe(targetRef.value)
    })

    onUnmounted(() => {
        if (observer && targetRef.value) {
            observer.unobserve(targetRef.value)
            observer.disconnect()
        }
    })

    return {
        targetRef,
        loading,
        hasMore,
        trigger
    }
}
