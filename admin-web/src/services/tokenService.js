/**
 * Token管理服务
 * 统一管理Token的存储、获取、刷新和清除
 */
import { getItem, setItem, removeItem } from '@/utils/storage'
import { TOKEN_CONFIG, API_CONFIG } from '@/config/constants'
import axios from 'axios'

/**
 * 获取Token存储键名
 */
const getTokenKey = () => `${TOKEN_CONFIG.KEY_PREFIX}token`

/**
 * 获取Token过期时间存储键名
 */
const getTokenExpireKey = () => `${TOKEN_CONFIG.KEY_PREFIX}token_expire`

/**
 * 获取Token
 * @returns {string|null} Token值
 */
export function getToken() {
    return getItem(getTokenKey(), null)
}

/**
 * 设置Token
 * @param {string} token - Token值
 * @param {number} timeout - 过期时间（秒）
 */
export function setToken(token, timeout) {
    setItem(getTokenKey(), token)

    if (timeout && timeout > 0) {
        const expireTime = Date.now() + timeout * 1000
        setItem(getTokenExpireKey(), expireTime)
    }
}

/**
 * 获取Token过期时间
 * @returns {number} 过期时间戳
 */
export function getTokenExpireTime() {
    return getItem(getTokenExpireKey(), 0)
}

/**
 * 检查Token是否过期
 * @returns {boolean} 是否过期
 */
export function isTokenExpired() {
    const token = getToken()
    const expireTime = getTokenExpireTime()

    if (!token || !expireTime) {
        return true
    }

    return Date.now() > expireTime
}

/**
 * 检查Token是否即将过期（5分钟内）
 * @returns {boolean} 是否即将过期
 */
export function isTokenExpiring() {
    const token = getToken()
    const expireTime = getTokenExpireTime()

    if (!token || !expireTime) {
        return false
    }

    return Date.now() + TOKEN_CONFIG.REFRESH_THRESHOLD > expireTime && Date.now() < expireTime
}

/**
 * 清除Token
 */
export function clearToken() {
    removeItem(getTokenKey())
    removeItem(getTokenExpireKey())
}

/**
 * 刷新Token
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
            baseURL: API_CONFIG.BASE_URL,
            timeout: API_CONFIG.TIMEOUT
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
