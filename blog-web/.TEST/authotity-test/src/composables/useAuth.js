/**
 * useAuth 组合式函数
 * 为组件提供统一的认证功能接口
 */
import { ref, computed } from 'vue'
import * as authService from '@/services/authService'
import { useUserStore } from '@/stores/user'
import { showSuccess, showError } from '@/utils/common'

export function useAuth() {
    const userStore = useUserStore()

    const loading = ref(false)
    const error = ref(null)

    /**
     * 登录
     * @param {string} username - 用户名
     * @param {string} password - 密码
     * @returns {Promise<{success: boolean, data?: any}>} 登录结果
     */
    const login = async (username, password) => {
        loading.value = true
        error.value = null

        try {
            const result = await authService.login(username, password)
            if (result.success) {
                showSuccess('登录成功')
                return { success: true, data: result.data }
            } else {
                error.value = result.error || '登录失败'
                showError(error.value)
                return { success: false }
            }
        } catch (err) {
            error.value = err.message || '登录失败'
            showError(error.value)
            return { success: false }
        } finally {
            loading.value = false
        }
    }

    /**
     * 注册
     * @param {Object} registerData - 注册数据
     * @returns {Promise<{success: boolean, data?: any}>} 注册结果
     */
    const register = async (registerData) => {
        loading.value = true
        error.value = null

        try {
            const result = await authService.register(registerData)
            if (result.success) {
                showSuccess('注册成功')
                return { success: true, data: result.data }
            } else {
                error.value = result.error || '注册失败'
                showError(error.value)
                return { success: false }
            }
        } catch (err) {
            error.value = err.message || '注册失败'
            showError(error.value)
            return { success: false }
        } finally {
            loading.value = false
        }
    }

    /**
     * 登出
     * @returns {Promise<void>}
     */
    const logout = async () => {
        loading.value = true

        try {
            const result = await authService.logout()
            if (result.success) {
                showSuccess('已退出登录')
            }
        } catch (err) {
            // 登出服务已经处理了所有情况，这里不应该有错误
            console.error('[useAuth] 登出异常:', err)
            showSuccess('已退出登录')
        } finally {
            loading.value = false
        }
    }

    /**
     * 获取用户信息
     * @returns {Promise<boolean>} 是否成功
     */
    const fetchUserInfo = async () => {
        loading.value = true
        error.value = null

        try {
            await authService.getCurrentUser()
            return true
        } catch (err) {
            error.value = err.message || '获取用户信息失败'
            console.error('获取用户信息失败:', err)
            return false
        } finally {
            loading.value = false
        }
    }

    return {
        loading,
        error,
        isLoggedIn: computed(() => userStore.isLoggedIn),
        userInfo: computed(() => userStore.userInfo),
        login,
        register,
        logout,
        fetchUserInfo
    }
}
