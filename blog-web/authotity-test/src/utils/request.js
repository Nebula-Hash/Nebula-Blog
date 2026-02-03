// 通信请求与响应封装（含无感刷新Token机制）
//
// 错误处理说明：
// 1. 默认情况下，拦截器不会显示错误提示，由业务层统一处理
// 2. 如需在拦截器中显示错误，可在请求配置中设置 showErrorInInterceptor: true
// 3. 使用 silent: true 可完全静默处理错误（拦截器和业务层都不显示）

import axios from 'axios'
import * as tokenService from '@/services/tokenService'
import { useUserStore } from '@/stores/user'
import { showError, showWarning } from '@/utils/common'
import {
    TOKEN_CONFIG,
    CACHE_CONFIG,
    BUSINESS_CODE,
    HTTP_CONFIG,
    HTTP_STATUS
} from '@/config/constants'

const request = axios.create({
    baseURL: HTTP_CONFIG.CLIENT_BASE_URL,
    timeout: HTTP_CONFIG.TIMEOUT
})

// 创建独立的axios实例用于刷新Token，避免被拦截器处理
const refreshRequest = axios.create({
    baseURL: HTTP_CONFIG.CLIENT_BASE_URL,
    timeout: HTTP_CONFIG.TIMEOUT
})

// 无感刷新相关状态
let isRefreshing = false
let refreshSubscribers = []

// 错误去重机制：防止短时间内相同错误重复弹窗
const errorMessageCache = new Map()

/**
 * 显示错误消息（带去重）
 */
function showErrorMessage(message, type = 'error') {
    const now = Date.now()
    const cached = errorMessageCache.get(message)

    // 如果缓存时长内已经显示过相同消息，则跳过
    if (cached && now - cached < CACHE_CONFIG.ERROR_MESSAGE_TTL) {
        return
    }

    errorMessageCache.set(message, now)

    // 使用 common.js 的封装函数
    if (type === 'warning') {
        showWarning(message)
    } else {
        showError(message)
    }

    // 清理过期缓存
    setTimeout(() => {
        errorMessageCache.delete(message)
    }, CACHE_CONFIG.ERROR_MESSAGE_TTL)
}

/**
 * 订阅Token刷新完成事件
 * @returns {Function} 清理函数
 */
const subscribeTokenRefresh = (cb) => {
    refreshSubscribers.push(cb)

    // 添加超时清理机制，防止内存泄漏
    const timeoutId = setTimeout(() => {
        const index = refreshSubscribers.indexOf(cb)
        if (index > -1) {
            refreshSubscribers.splice(index, 1)
            console.warn('[Request] Token刷新订阅超时，已清理')
        }
    }, TOKEN_CONFIG.REFRESH_TIMEOUT)

    // 返回清理函数
    return () => {
        clearTimeout(timeoutId)
        const index = refreshSubscribers.indexOf(cb)
        if (index > -1) {
            refreshSubscribers.splice(index, 1)
        }
    }
}

/**
 * Token刷新完成，通知所有等待的请求
 */
const onTokenRefreshed = (newToken) => {
    refreshSubscribers.forEach(cb => cb(newToken))
    refreshSubscribers = []
}

/**
 * Token刷新失败，清空等待队列
 */
const onRefreshFailed = () => {
    refreshSubscribers.forEach(cb => cb(null))
    refreshSubscribers = []
}

// 请求拦截器
request.interceptors.request.use(
    config => {
        const token = tokenService.getToken()
        if (token) {
            config.headers['Authorization'] = token
        }

        // 支持 AbortController signal
        if (config.signal) {
            config.cancelToken = new axios.CancelToken((cancel) => {
                config.signal.addEventListener('abort', () => {
                    cancel('Request canceled by user')
                })
            })
        }

        return config
    },
    error => {
        return Promise.reject(error)
    }
)

// 响应拦截器
request.interceptors.response.use(
    response => {
        const res = response.data
        if (res.code === BUSINESS_CODE.SUCCESS) {
            return res
        } else {
            // Token无效或过期
            if (res.code === BUSINESS_CODE.UNAUTHORIZED) {
                return handleTokenExpired(response.config)
            } else {
                // 业务错误处理
                if (!response.config.silent && response.config.showErrorInInterceptor) {
                    handleBusinessError(res)
                }
            }
            const error = new Error(res.message || '请求失败')
            error.response = response
            return Promise.reject(error)
        }
    },
    error => {
        // 请求被取消，直接返回
        if (axios.isCancel(error)) {
            console.log('[Request] 请求已取消:', error.message)
            return Promise.reject(error)
        }

        // HTTP状态码401 - Token验证失败
        if (error.response?.status === HTTP_STATUS.UNAUTHORIZED) {
            return handleTokenExpired(error.config)
        } else {
            // 网络错误处理
            if (!error.config?.silent && error.config?.showErrorInInterceptor) {
                handleNetworkError(error)
            }
        }
        return Promise.reject(error)
    }
)

/**
 * 处理Token过期，尝试无感刷新
 */
async function handleTokenExpired(originalConfig) {
    // 防止刷新接口本身失败导致无限循环
    if (originalConfig.url.includes('/auth/refresh')) {
        silentLogout(true)
        return Promise.reject(new Error('Token刷新失败'))
    }

    // 检查是否有Token
    const token = tokenService.getToken()
    if (!token) {
        return Promise.reject(new Error('未登录'))
    }

    if (!isRefreshing) {
        isRefreshing = true

        try {
            const refreshPromise = refreshRequest.post('/auth/refresh', null, {
                headers: { 'Authorization': token }
            })

            const timeoutPromise = new Promise((_, reject) =>
                setTimeout(() => reject(new Error('刷新超时')), TOKEN_CONFIG.REFRESH_TIMEOUT)
            )

            const res = await Promise.race([refreshPromise, timeoutPromise])

            if (res.data.code === BUSINESS_CODE.SUCCESS) {
                const newToken = res.data.data.token
                const timeout = res.data.data.tokenTimeout

                // 更新Token
                tokenService.setToken(newToken, timeout)
                onTokenRefreshed(newToken)

                // 用新Token重试原请求
                originalConfig.headers['Authorization'] = newToken
                return request(originalConfig)
            } else {
                throw new Error(res.data.message || 'Token刷新失败')
            }
        } catch (error) {
            console.error('[Request] Token刷新失败:', error)
            onRefreshFailed()
            silentLogout(true)
            return Promise.reject(error)
        } finally {
            isRefreshing = false
        }
    } else {
        // 队列已满，拒绝请求
        if (refreshSubscribers.length >= TOKEN_CONFIG.MAX_QUEUE_SIZE) {
            return Promise.reject(new Error('请求队列已满，请稍后重试'))
        }

        // 等待刷新完成
        return new Promise((resolve, reject) => {
            const cleanup = subscribeTokenRefresh((newToken) => {
                if (newToken) {
                    originalConfig.headers['Authorization'] = newToken
                    resolve(request(originalConfig))
                } else {
                    reject(new Error('Token刷新失败'))
                }
            })

            // 设置超时，超时后自动清理
            setTimeout(() => {
                cleanup()
                reject(new Error('等待刷新超时'))
            }, TOKEN_CONFIG.REFRESH_TIMEOUT)
        })
    }
}

/**
 * 处理业务错误
 */
function handleBusinessError(res) {
    const code = res.code
    const message = res.message || '请求失败'

    switch (code) {
        case BUSINESS_CODE.WRONG_CREDENTIALS:
            showErrorMessage('用户名或密码错误')
            break
        case BUSINESS_CODE.ACCOUNT_LOCKED:
            const lockMessage = message.includes('分钟') ? message : '账号已被锁定，请稍后再试'
            showErrorMessage(lockMessage)
            break
        case BUSINESS_CODE.USERNAME_EXISTS:
            showErrorMessage('用户名已存在')
            break
        case BUSINESS_CODE.REGISTER_TOO_FREQUENT:
            showErrorMessage('注册过于频繁，请稍后再试')
            break
        case HTTP_STATUS.FORBIDDEN:
            showErrorMessage('权限不足，无法访问')
            break
        case HTTP_STATUS.NOT_FOUND:
            showErrorMessage('请求的资源不存在')
            break
        case HTTP_STATUS.INTERNAL_SERVER_ERROR:
            showErrorMessage('服务器错误，请稍后重试')
            break
        default:
            showErrorMessage(message)
    }
}

/**
 * 处理网络错误
 */
function handleNetworkError(error) {
    if (error.code === 'ECONNABORTED' || error.message.includes('timeout')) {
        showErrorMessage('请求超时，请检查网络连接')
    } else if (error.response) {
        const status = error.response.status
        switch (status) {
            case HTTP_STATUS.FORBIDDEN:
                showErrorMessage('权限不足，无法访问')
                break
            case HTTP_STATUS.NOT_FOUND:
                showErrorMessage('请求的资源不存在')
                break
            case HTTP_STATUS.INTERNAL_SERVER_ERROR:
            case HTTP_STATUS.BAD_GATEWAY:
            case HTTP_STATUS.SERVICE_UNAVAILABLE:
            case HTTP_STATUS.GATEWAY_TIMEOUT:
                showErrorMessage('服务器错误，请稍后重试')
                break
            default:
                showErrorMessage(error.message || '网络错误')
        }
    } else if (error.request) {
        showErrorMessage('网络连接失败，请检查网络')
    } else {
        showErrorMessage(error.message || '请求失败')
    }
}

/**
 * 静默登出
 */
function silentLogout(showMessage = true) {
    const userStore = useUserStore()
    userStore.clearAuth()
    if (showMessage) {
        showErrorMessage('登录已过期，请重新登录', 'warning')
    }
}

export default request
