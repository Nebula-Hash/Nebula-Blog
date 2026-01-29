/**
 * 认证服务
 * 处理登录、登出等认证业务逻辑
 */
import * as authApi from '@/api/auth'
import * as tokenService from './tokenService'
import { useUserStore } from '@/stores/user'

// 用户信息缓存
const userInfoCache = {
    data: null,
    timestamp: 0,
    ttl: 5 * 60 * 1000 // 5分钟缓存
}

/**
 * 登录
 * @param {string} username - 用户名
 * @param {string} password - 密码
 * @returns {Promise<{success: boolean, data?: any, error?: string}>}
 */
export async function login(username, password) {
    try {
        const response = await authApi.login({ username, password })

        if (response.code === 200) {
            const { token, tokenTimeout, userId, username: uname, nickname, avatar, roleKey } = response.data

            // 设置Token
            tokenService.setToken(token, tokenTimeout)

            // 设置用户信息
            const userStore = useUserStore()
            userStore.setUserInfo({
                userId,
                username: uname,
                nickname,
                avatar,
                roleKey
            })

            return {
                success: true,
                data: response.data
            }
        } else {
            return {
                success: false,
                error: response.message || '登录失败'
            }
        }
    } catch (error) {
        console.error('[AuthService] 登录失败:', error)
        return {
            success: false,
            error: error.message || '登录失败，请稍后重试'
        }
    }
}

/**
 * 获取当前用户信息（带缓存）
 * @param {boolean} force - 是否强制刷新缓存
 * @returns {Promise<any>}
 */
export async function getCurrentUser(force = false) {
    try {
        const now = Date.now()

        // 如果不强制刷新且缓存有效，返回缓存数据
        if (!force && userInfoCache.data && (now - userInfoCache.timestamp < userInfoCache.ttl)) {
            console.log('[AuthService] 使用缓存的用户信息')
            return userInfoCache.data
        }

        console.log('[AuthService] 从服务器获取用户信息')
        const response = await authApi.getUserInfo()

        if (response.code === 200) {
            const userStore = useUserStore()
            userStore.setUserInfo(response.data)

            // 更新缓存
            userInfoCache.data = response.data
            userInfoCache.timestamp = now

            return response.data
        } else {
            throw new Error(response.message || '获取用户信息失败')
        }
    } catch (error) {
        console.error('[AuthService] 获取用户信息失败:', error)
        throw error
    }
}

/**
 * 清除用户信息缓存
 */
export function clearUserInfoCache() {
    userInfoCache.data = null
    userInfoCache.timestamp = 0
}

/**
 * 登出
 * @returns {Promise<void>}
 */
export async function logout() {
    try {
        // 调用登出接口
        await authApi.logout()
    } catch (error) {
        console.error('[AuthService] 登出接口调用失败:', error)
        // 即使接口失败也继续清除本地状态
    } finally {
        // 清除缓存
        clearUserInfoCache()

        // 清除Token
        tokenService.clearToken()

        // 清除用户信息
        const userStore = useUserStore()
        userStore.clearAuth()
    }
}

/**
 * 检查登录状态
 * @returns {Promise<boolean>}
 */
export async function checkLoginStatus() {
    try {
        // 先检查本地Token
        if (tokenService.isTokenExpired()) {
            return false
        }

        // 调用后端检查接口
        const response = await authApi.checkLogin()
        return response.code === 200
    } catch (error) {
        console.error('[AuthService] 检查登录状态失败:', error)
        return false
    }
}

export default {
    login,
    getCurrentUser,
    logout,
    checkLoginStatus,
    clearUserInfoCache
}
