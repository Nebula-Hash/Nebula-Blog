// 通信请求与响应封装（简化版 - 无认证功能）
//
// 错误处理说明：
// 1. 默认情况下，拦截器不会显示错误提示，由业务层使用 errorHandler.js 统一处理
// 2. 如需在拦截器中显示错误，可在请求配置中设置 showErrorInInterceptor: true
// 3. 使用 silent: true 可完全静默处理错误（拦截器和业务层都不显示）

import axios from 'axios'
import { showError } from '@/utils/common'
import {
  HTTP_CONFIG,
  HTTP_STATUS,
  CACHE_CONFIG
} from '@/config/constants'

const request = axios.create({
  baseURL: HTTP_CONFIG.CLIENT_BASE_URL,
  timeout: HTTP_CONFIG.TIMEOUT
})

// 错误去重机制：防止短时间内相同错误重复弹窗
const errorMessageCache = new Map()

/**
 * 显示错误消息（带去重）
 */
function showErrorMessage(message) {
  const now = Date.now()
  const cached = errorMessageCache.get(message)

  // 如果缓存时长内已经显示过相同消息，则跳过
  if (cached && now - cached < CACHE_CONFIG.ERROR_MESSAGE_TTL) {
    return
  }

  errorMessageCache.set(message, now)
  showError(message)

  // 清理过期缓存
  setTimeout(() => {
    errorMessageCache.delete(message)
  }, CACHE_CONFIG.ERROR_MESSAGE_TTL)
}

// 请求拦截器
request.interceptors.request.use(
  config => {
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
    // 服务器返回了错误状态码
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
    // 请求已发送但没有收到响应
    showErrorMessage('网络连接失败，请检查网络')
  } else {
    // 其他错误
    showErrorMessage(error.message || '请求失败')
  }
}

/**
 * 请求去重管理器
 * 防止相同请求在短时间内重复发送
 */
class RequestDeduplicator {
  constructor() {
    this.pendingRequests = new Map()
  }

  /**
   * 生成请求唯一键
   */
  generateKey(config) {
    const { method, url, params, data } = config
    return `${method}:${url}:${JSON.stringify(params)}:${JSON.stringify(data)}`
  }

  /**
   * 添加请求
   */
  addRequest(config, promise) {
    const key = this.generateKey(config)
    this.pendingRequests.set(key, promise)

    // 请求完成后移除
    promise.finally(() => {
      this.pendingRequests.delete(key)
    })

    return promise
  }

  /**
   * 获取已存在的请求
   */
  getRequest(config) {
    const key = this.generateKey(config)
    return this.pendingRequests.get(key)
  }

  /**
   * 移除请求
   */
  removeRequest(config) {
    const key = this.generateKey(config)
    this.pendingRequests.delete(key)
  }
}

/**
 * 请求取消管理器
 * 管理可取消的请求
 */
class RequestCanceller {
  constructor() {
    this.cancelTokens = new Map()
  }

  /**
   * 添加取消令牌
   */
  addCancelToken(key, cancelToken) {
    this.cancelTokens.set(key, cancelToken)
  }

  /**
   * 取消指定请求
   */
  cancel(key, message = 'Request canceled') {
    const cancelToken = this.cancelTokens.get(key)
    if (cancelToken) {
      cancelToken.cancel(message)
      this.cancelTokens.delete(key)
    }
  }

  /**
   * 取消所有请求
   */
  cancelAll(message = 'All requests canceled') {
    this.cancelTokens.forEach((cancelToken) => {
      cancelToken.cancel(message)
    })
    this.cancelTokens.clear()
  }

  /**
   * 移除取消令牌
   */
  remove(key) {
    this.cancelTokens.delete(key)
  }
}

/**
 * 创建可取消的请求
 * @param {Object} config - axios请求配置
 * @param {string} cancelKey - 取消键（可选）
 * @returns {Promise} 请求Promise
 */
export function createCancellableRequest(config, cancelKey) {
  const source = axios.CancelToken.source()
  const key = cancelKey || `${config.method}:${config.url}`

  config.cancelToken = source.token

  // 添加到取消管理器
  requestCanceller.addCancelToken(key, source)

  const promise = request(config)

  // 请求完成后移除
  promise.finally(() => {
    requestCanceller.remove(key)
  })

  return {
    promise,
    cancel: (message) => source.cancel(message),
    key
  }
}

// 导出单例实例
export const requestDeduplicator = new RequestDeduplicator()
export const requestCanceller = new RequestCanceller()

export default request
