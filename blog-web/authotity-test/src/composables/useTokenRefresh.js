/**
 * useTokenRefresh 组合式函数
 * 实现Token主动刷新机制
 */
import { onMounted, onUnmounted } from 'vue'
import * as tokenService from '@/services/tokenService'
import { useUserStore } from '@/stores/user'

export function useTokenRefresh() {
    const userStore = useUserStore()
    let refreshTimer = null

    /**
     * 检查并刷新Token
     */
    const checkAndRefresh = async () => {
        if (!userStore.isLoggedIn) {
            return
        }

        // Token即将过期（5分钟内）
        if (tokenService.isTokenExpiring()) {
            try {
                await tokenService.refreshToken()
                console.log('[TokenRefresh] Token已自动刷新')
            } catch (error) {
                console.error('[TokenRefresh] Token自动刷新失败:', error)
                // 不强制登出，等待下次请求时处理
            }
        }
    }

    /**
     * 启动定时检查（每分钟检查一次）
     */
    const startAutoRefresh = () => {
        stopAutoRefresh()
        refreshTimer = setInterval(checkAndRefresh, 60000)
        // 立即执行一次检查
        checkAndRefresh()
    }

    /**
     * 停止定时检查
     */
    const stopAutoRefresh = () => {
        if (refreshTimer) {
            clearInterval(refreshTimer)
            refreshTimer = null
        }
    }

    // 组件挂载时启动
    onMounted(() => {
        startAutoRefresh()
    })

    // 组件卸载时停止
    onUnmounted(() => {
        stopAutoRefresh()
    })

    return {
        checkAndRefresh,
        startAutoRefresh,
        stopAutoRefresh
    }
}
