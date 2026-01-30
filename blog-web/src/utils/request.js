// 通信请求与响应封装（含无感刷新Token机制）

import axios from 'axios'
import * as tokenService from '@/services/tokenService'
import { useUserStore } from '@/stores/user'
import { showError, showWarning } from '@/utils/common'

const request = axios.create({
  baseURL: '/api/client',
  timeout: 10000
})

// 创建独立的axios实例用于刷新Token，避免被拦截器处理
const refreshRequest = axios.create({
  baseURL: '/api/client',
  timeout: 10000
})

// 无感刷新相关状态
let isRefreshing = false
let refreshSubscribers = []
const MAX_QUEUE_SIZE = 50
const REFRESH_TIMEOUT = 10000

// 错误去重机制：防止短时间内相同错误重复弹窗
const errorMessageCache = new Map()
const ERROR_CACHE_DURATION = 1000 // 1秒内相同错误只显示一次

/**
 * 显示错误消息（带去重）
 */
function showErrorMessage(message, type = 'error') {
  const now = Date.now()
  const cached = errorMessageCache.get(message)

  // 如果1秒内已经显示过相同消息，则跳过
  if (cached && now - cached < ERROR_CACHE_DURATION) {
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
  }, ERROR_CACHE_DURATION)
}

/**
 * 订阅Token刷新完成事件
 */
const subscribeTokenRefresh = (cb) => {
  refreshSubscribers.push(cb)
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
  refreshSubscribers = []
}

// 请求拦截器
request.interceptors.request.use(
  config => {
    const token = tokenService.getToken()
    if (token) {
      config.headers['Authorization'] = token
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
    if (res.code === 200) {
      return res
    } else {
      // Token无效或过期
      if (res.code === 401) {
        return handleTokenExpired(response.config)
      } else {
        // 业务错误处理（只在非静默模式下弹窗）
        if (!response.config.silent) {
          handleBusinessError(res)
        }
      }
      return Promise.reject(new Error(res.message || '请求失败'))
    }
  },
  error => {
    // HTTP状态码401 - Token验证失败
    if (error.response?.status === 401) {
      return handleTokenExpired(error.config)
    } else {
      // 网络错误处理（只在非静默模式下弹窗）
      if (!error.config?.silent) {
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

  // 检查是否有Token，如果没有Token说明是未登录用户，静默失败
  const token = tokenService.getToken()
  if (!token) {
    // 未登录用户，静默失败，不提示"登录已过期"
    return Promise.reject(new Error('未登录'))
  }

  if (!isRefreshing) {
    isRefreshing = true

    try {
      // 使用独立的axios实例刷新Token，避免被拦截器处理
      const refreshPromise = refreshRequest.post('/auth/refresh', null, {
        headers: { 'Authorization': token }
      })

      const timeoutPromise = new Promise((_, reject) =>
        setTimeout(() => reject(new Error('刷新超时')), REFRESH_TIMEOUT)
      )

      const res = await Promise.race([refreshPromise, timeoutPromise])

      if (res.data.code === 200) {
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
    if (refreshSubscribers.length >= MAX_QUEUE_SIZE) {
      return Promise.reject(new Error('请求队列已满，请稍后重试'))
    }

    // 等待刷新完成
    return new Promise((resolve, reject) => {
      subscribeTokenRefresh((newToken) => {
        originalConfig.headers['Authorization'] = newToken
        resolve(request(originalConfig))
      })

      // 设置超时
      setTimeout(() => {
        reject(new Error('等待刷新超时'))
      }, REFRESH_TIMEOUT)
    })
  }
}

/**
 * 处理业务错误
 */
function handleBusinessError(res) {
  const code = res.code
  const message = res.message || '请求失败'

  // 特殊错误码处理
  switch (code) {
    case 40001:
      showErrorMessage('用户名或密码错误')
      break
    case 40002:
      // 账号锁定 - 尝试从消息中提取剩余时间
      const lockMessage = message.includes('分钟')
        ? message
        : '账号已被锁定，请稍后再试'
      showErrorMessage(lockMessage)
      break
    case 40003:
      showErrorMessage('用户名已存在')
      break
    case 40004:
      showErrorMessage('注册过于频繁，请稍后再试')
      break
    case 403:
      showErrorMessage('权限不足，无法访问')
      break
    case 404:
      showErrorMessage('请求的资源不存在')
      break
    case 500:
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
    // 服务器返回了错误状态码
    const status = error.response.status
    switch (status) {
      case 403:
        showErrorMessage('权限不足，无法访问')
        break
      case 404:
        showErrorMessage('请求的资源不存在')
        break
      case 500:
      case 502:
      case 503:
      case 504:
        showErrorMessage('服务器错误，请稍后重试')
        break
      default:
        showErrorMessage(error.message || '网络错误')
    }
  } else if (error.request) {
    // 请求已发送但没有收到响应
    showErrorMessage('网络连接失败，请检查网络')
  } else {
    // 其他错误
    showErrorMessage(error.message || '请求失败')
  }
}

/**
 * 静默登出（客户端不强制跳转，只清除状态）
 * @param {boolean} showMessage - 是否显示提示消息（默认true）
 */
function silentLogout(showMessage = true) {
  const userStore = useUserStore()
  userStore.clearAuth()
  if (showMessage) {
    showErrorMessage('登录已过期，请重新登录', 'warning')
  }
}

export default request
