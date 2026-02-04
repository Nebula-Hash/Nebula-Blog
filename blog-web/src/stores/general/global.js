import { defineStore } from 'pinia'
import { ref } from 'vue'

/**
 * 全局状态管理
 * 管理全局 loading、错误、通知等状态
 */
export const useGlobalStore = defineStore('global', () => {
    // 全局加载状态
    const globalLoading = ref(false)
    const loadingCount = ref(0)

    // 错误状态
    const lastError = ref(null)

    /**
     * 显示全局加载
     */
    function showLoading() {
        loadingCount.value++
        globalLoading.value = true
    }

    /**
     * 隐藏全局加载
     */
    function hideLoading() {
        loadingCount.value = Math.max(0, loadingCount.value - 1)
        if (loadingCount.value === 0) {
            globalLoading.value = false
        }
    }

    /**
     * 重置加载状态
     */
    function resetLoading() {
        loadingCount.value = 0
        globalLoading.value = false
    }

    /**
     * 设置错误
     */
    function setError(error) {
        lastError.value = {
            message: error.message || '未知错误',
            code: error.code,
            timestamp: Date.now()
        }
    }

    /**
     * 清除错误
     */
    function clearError() {
        lastError.value = null
    }

    return {
        // State
        globalLoading,
        loadingCount,
        lastError,

        // Actions
        showLoading,
        hideLoading,
        resetLoading,
        setError,
        clearError
    }
})
