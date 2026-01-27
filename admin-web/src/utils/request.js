// 通信请求与响应封装（含无感刷新Token机制）

import axios from 'axios'
import { useUserStore } from '@/stores/user'

const request = axios.create({
  baseURL: '/api/admin',
  timeout: 10000
})

// 无感刷新相关状态
let isRefreshing = false
let refreshSubscribers = []

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
  (config) => {
    const userStore = useUserStore()
    if (userStore.token) {
      config.headers['Authorization'] = userStore.token
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code === 200) {
      return res
    } else {
      // Token无效或过期
      if (res.code === 401) {
        return handleTokenExpired(response.config)
      } else {
        window.$message?.error(res.message || '请求失败')
      }
      return Promise.reject(new Error(res.message || '请求失败'))
    }
  },
  (error) => {
    // HTTP状态码401 - Token验证失败
    if (error.response?.status === 401) {
      return handleTokenExpired(error.config)
    } else {
      window.$message?.error(error.message || '网络错误')
    }
    return Promise.reject(error)
  }
)

/**
 * 处理Token过期，尝试无感刷新
 */
async function handleTokenExpired(originalConfig) {
  const userStore = useUserStore()

  // 如果是刷新接口本身失败，直接登出
  if (originalConfig.url === '/auth/refresh') {
    forceLogout()
    return Promise.reject(new Error('刷新Token失败'))
  }

  if (!isRefreshing) {
    isRefreshing = true

    try {
      // 调用刷新接口
      const res = await request.post('/auth/refresh')
      const newToken = res.data.token
      const timeout = res.data.tokenTimeout

      // 更新本地Token
      userStore.setToken(newToken, timeout)
      onTokenRefreshed(newToken)
      isRefreshing = false

      // 用新Token重试原请求
      originalConfig.headers['Authorization'] = newToken
      return request(originalConfig)
    } catch (error) {
      isRefreshing = false
      onRefreshFailed()
      forceLogout()
      return Promise.reject(error)
    }
  } else {
    // 已有刷新请求在进行，等待刷新完成后重试
    return new Promise((resolve) => {
      subscribeTokenRefresh((newToken) => {
        originalConfig.headers['Authorization'] = newToken
        resolve(request(originalConfig))
      })
    })
  }
}

/**
 * 强制登出
 */
function forceLogout() {
  const userStore = useUserStore()
  userStore.logout()
  window.$message?.error('登录已过期，请重新登录')
  setTimeout(() => {
    window.location.href = '/login'
  }, 1000)
}

export default request
