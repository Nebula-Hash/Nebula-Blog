/**
 * useAuth 组合式函数
 * 为组件提供统一的认证功能接口
 */
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import * as authService from '@/services/authService'
import { useUserStore } from '@/stores/user'

export function useAuth() {
    const router = useRouter()
    const userStore = useUserStore()

    const loading = ref(false)
    const error = ref(null)

    /**
     * 登录
     * @param {string} username - 用户名
     * @param {string} password - 密码
     * @returns {Promise<boolean>} 是否成功
     */
    const login = async (username, password) => {
        loading.value = true
        error.value = null

        try {
            const result = await authService.login(username, password)
            if (result.success) {
                window.$message?.success('登录成功')
                return true
            } else {
                error.value = result.error || '登录失败'
                window.$message?.error(error.value)
                return false
            }
        } catch (err) {
            error.value = err.message || '登录失败'
            window.$message?.error(error.value)
            return false
        } finally {
            loading.value = false
        }
    }

    /**
     * 注册
     * @param {Object} registerData - 注册数据
     * @returns {Promise<boolean>} 是否成功
     */
    const register = async (registerData) => {
        loading.value = true
        error.value = null

        try {
            const result = await authService.register(registerData)
            if (result.success) {
                window.$message?.success('注册成功')
                return true
            } else {
                error.value = result.error || '注册失败'
                window.$message?.error(error.value)
                return false
            }
        } catch (err) {
            error.value = err.message || '注册失败'
            window.$message?.error(error.value)
            return false
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
            await authService.logout()
            window.$message?.success('已退出登录')
            router.push('/')
        } catch (err) {
            console.error('登出失败:', err)
            // 即使失败也清除本地状态
            userStore.clearAuth()
            router.push('/')
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
