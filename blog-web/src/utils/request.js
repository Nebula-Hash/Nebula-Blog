// 通信请求与响应封装（简化版 - 无认证功能）
//
// 错误处理说明：
// 1. 默认情况下，拦截器不会显示错误提示，由业务层使用 errorHandler.js 统一处理
// 2. 如需在拦截器中显示错误，可在请求配置中设置 showErrorInInterceptor: true
// 3. 使用 silent: true 可完全静默处理错误（拦截器和业务层都不显示）

import axios from 'axios'
import {
  HTTP_CONFIG,
  HTTP_STATUS
} from '@/config/constants'

const request = axios.create({
  baseURL: HTTP_CONFIG.CLIENT_BASE_URL,
  timeout: HTTP_CONFIG.TIMEOUT
})

// 请求拦截器
request.interceptors.request.use(
  config => {
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
    const SUCCESS_CODE = 200

    if (res.code === SUCCESS_CODE) {
      return res
    } else {
      // 业务错误处理（只在非静默模式且未禁用拦截器提示时弹窗）
      if (!response.config.silent && response.config.showErrorInInterceptor) {
        handleBusinessError(res)
      }
      // 将完整的响应信息附加到错误对象上，方便业务层使用
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

    // 网络错误处理（只在非静默模式且未禁用拦截器提示时弹窗）
    if (!error.config?.silent && error.config?.showErrorInInterceptor) {
      handleNetworkError(error)
    }
    return Promise.reject(error)
  }
)

/**
 * 处理业务错误
 */
function handleBusinessError(res) {
  const message = res.message || '请求失败'
  const status = res.code

  // 特殊错误码处理
  switch (status) {
    case HTTP_STATUS.FORBIDDEN:
      console.error('[Request] 权限不足，无法访问')
      break
    case HTTP_STATUS.NOT_FOUND:
      console.error('[Request] 请求的资源不存在')
      break
    case HTTP_STATUS.INTERNAL_SERVER_ERROR:
      console.error('[Request] 服务器错误，请稍后重试')
      break
    default:
      console.error('[Request] 请求失败:', message)
  }
}

/**
 * 处理网络错误
 */
function handleNetworkError(error) {
  if (error.code === 'ECONNABORTED' || error.message.includes('timeout')) {
    console.error('[Request] 请求超时，请检查网络连接')
  } else if (error.response) {
    // 服务器返回了错误状态码
    const status = error.response.status
    switch (status) {
      case HTTP_STATUS.FORBIDDEN:
        console.error('[Request] 权限不足，无法访问')
        break
      case HTTP_STATUS.NOT_FOUND:
        console.error('[Request] 请求的资源不存在')
        break
      case HTTP_STATUS.INTERNAL_SERVER_ERROR:
      case HTTP_STATUS.BAD_GATEWAY:
      case HTTP_STATUS.SERVICE_UNAVAILABLE:
      case HTTP_STATUS.GATEWAY_TIMEOUT:
        console.error('[Request] 服务器错误，请稍后重试')
        break
      default:
        console.error('[Request] 网络错误:', error.message || '网络错误')
    }
  } else if (error.request) {
    // 请求已发送但没有收到响应
    console.error('[Request] 网络连接失败，请检查网络')
  } else {
    // 其他错误
    console.error('[Request] 请求失败:', error.message || '请求失败')
  }
}

export default request
