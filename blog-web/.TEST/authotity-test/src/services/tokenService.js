/**
 * Token管理服务
 * 统一管理Token的存储、获取、刷新和清除
 * 注意：此服务现在委托给 Pinia store 管理状态
 */
import { useUserStore } from '@/stores/user'
import axios from 'axios'
import { HTTP_CONFIG } from '@/config/constants'

/**
 * Token服务配置
 */
const config = {
    baseURL: HTTP_CONFIG.CLIENT_BASE_URL
}

/**
 * 获取 Token
 * @returns {string|null} Token值
 */
export function getToken() {
    const userStore = useUserStore()
    return userStore.getToken()
}

/**
 * 设置 Token
 * @param {string} token - Token值
 * @param {number} timeout - 过期时间（秒）
 */
export function setToken(token, timeout) {
    const userStore = useUserStore()
    userStore.setToken(token, timeout)
}

/**
 * 获取 Token 过期时间
 * @returns {number} 过期时间戳
 */
export function getTokenExpireTime() {
    const userStore = useUserStore()
    return userStore.tokenExpireTime
}

/**
 * 检查 Token 是否过期
 * @returns {boolean} 是否过期
 */
export function isTokenExpired() {
    const userStore = useUserStore()
    return userStore.isTokenExpired
}

/**
 * 检查 Token 是否即将过期（5分钟内）
 * @returns {boolean} 是否即将过期
 */
export function isTokenExpiring() {
    const userStore = useUserStore()
    return userStore.isTokenExpiring
}

/**
 * 清除 Token
 */
export function clearToken() {
    const userStore = useUserStore()
    userStore.clearToken()
}

/**
 * 刷新 Token
 * 创建独立的axios实例，避免被拦截器处理
 * @returns {Promise<void>}
 */
export async function refreshToken() {
    const token = getToken()

    if (!token) {
        throw new Error('没有可用的Token')
    }

    try {
        // 创建独立的axios实例用于刷新Token
        const refreshRequest = axios.create({
            baseURL: config.baseURL,
            timeout: HTTP_CONFIG.TIMEOUT
        })

        const response = await refreshRequest.post('/auth/refresh', null, {
            headers: {
                'Authorization': token
            }
        })

        if (response.data.code === 200) {
            const { token: newToken, tokenTimeout } = response.data.data
            setToken(newToken, tokenTimeout)
            console.log('[TokenService] Token刷新成功')
        } else {
            throw new Error(response.data.message || 'Token刷新失败')
        }
    } catch (error) {
        console.error('[TokenService] Token刷新失败:', error)
        clearToken()
        throw error
    }
}

export default {
    getToken,
    setToken,
    getTokenExpireTime,
    isTokenExpired,
    isTokenExpiring,
    clearToken,
    refreshToken
}

