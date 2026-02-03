import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getItem, setItem, removeItem } from '@/utils/storage'

export const useUserStore = defineStore('user', () => {
  // ==================== 状态 ====================

  // 用户信息
  const userInfo = ref(null)

  // Token
  const token = ref(null)

  // Token 过期时间
  const tokenExpireTime = ref(0)

  // ==================== 计算属性 ====================

  /**
   * 检查 Token 是否已过期
   */
  const isTokenExpired = computed(() => {
    if (!token.value || !tokenExpireTime.value) {
      return true
    }
    return Date.now() > tokenExpireTime.value
  })

  /**
   * 检查 Token 是否即将过期（5分钟内）
   */
  const isTokenExpiring = computed(() => {
    if (!token.value || !tokenExpireTime.value) {
      return false
    }
    const fiveMinutes = 5 * 60 * 1000
    return Date.now() + fiveMinutes > tokenExpireTime.value && Date.now() < tokenExpireTime.value
  })

  /**
   * 检查是否已登录（Token 存在且未过期）
   */
  const isLoggedIn = computed(() => {
    return !!token.value && !isTokenExpired.value
  })

  /**
   * Token 剩余时间（毫秒）
   */
  const tokenRemainingTime = computed(() => {
    if (!tokenExpireTime.value) return 0
    const remaining = tokenExpireTime.value - Date.now()
    return remaining > 0 ? remaining : 0
  })

  /**
   * 格式化的 Token 过期时间
   */
  const formattedTokenExpireTime = computed(() => {
    if (!tokenExpireTime.value) return '-'
    return new Date(tokenExpireTime.value).toLocaleString('zh-CN')
  })

  /**
   * 格式化的 Token 剩余时间
   */
  const formattedTokenRemainingTime = computed(() => {
    const remaining = tokenRemainingTime.value
    if (remaining <= 0) return '已过期'

    const minutes = Math.floor(remaining / 60000)
    const seconds = Math.floor((remaining % 60000) / 1000)
    return `${minutes}分${seconds}秒`
  })

  /**
   * 存储信息（用于显示）
   */
  const storageInfo = computed(() => {
    return {
      token: token.value ? '已存储' : '未存储',
      tokenExpire: tokenExpireTime.value ? formattedTokenExpireTime.value : '未设置',
      userInfo: userInfo.value ? '已存储' : '未存储'
    }
  })

  /**
   * 完整的存储数据（JSON 格式）
   */
  const storageDataJson = computed(() => {
    const data = {
      token: token.value || null,
      token_expire: tokenExpireTime.value || null,
      token_expire_readable: formattedTokenExpireTime.value,
      userInfo: userInfo.value || null
    }
    return JSON.stringify(data, null, 2)
  })

  // ==================== 方法 ====================

  /**
   * 设置 Token
   * @param {string} tokenValue - Token 值
   * @param {number} timeout - 过期时间（秒）
   */
  const setToken = (tokenValue, timeout) => {
    token.value = tokenValue
    setItem('token', tokenValue)

    if (timeout && timeout > 0) {
      const expireTime = Date.now() + timeout * 1000
      tokenExpireTime.value = expireTime
      setItem('token_expire', expireTime)
    }
  }

  /**
   * 获取 Token
   * @returns {string|null} Token 值
   */
  const getToken = () => {
    return token.value
  }

  /**
   * 清除 Token
   */
  const clearToken = () => {
    token.value = null
    tokenExpireTime.value = 0
    removeItem('token')
    removeItem('token_expire')
  }

  /**
   * 设置用户信息
   * @param {Object} info - 用户信息
   */
  const setUserInfo = (info) => {
    userInfo.value = info
    setItem('userInfo', info)
  }

  /**
   * 清除用户信息
   */
  const clearUserInfo = () => {
    userInfo.value = null
    removeItem('userInfo')
  }

  /**
   * 清除所有认证信息
   */
  const clearAuth = () => {
    clearToken()
    clearUserInfo()
  }

  /**
   * 初始化 - 从 localStorage 恢复状态
   */
  const initialize = () => {
    // 恢复 Token
    const savedToken = getItem('token', null)
    const savedTokenExpire = getItem('token_expire', 0)

    if (savedToken && savedTokenExpire) {
      token.value = savedToken
      tokenExpireTime.value = savedTokenExpire
    }

    // 恢复用户信息
    const savedUserInfo = getItem('userInfo', null)
    if (savedUserInfo) {
      userInfo.value = savedUserInfo
    }

    // 检查 Token 是否过期
    if (isTokenExpired.value) {
      clearAuth()
    }
  }

  return {
    // 状态
    userInfo,
    token,
    tokenExpireTime,

    // 计算属性
    isTokenExpired,
    isTokenExpiring,
    isLoggedIn,
    tokenRemainingTime,
    formattedTokenExpireTime,
    formattedTokenRemainingTime,
    storageInfo,
    storageDataJson,

    // 方法
    setToken,
    getToken,
    clearToken,
    setUserInfo,
    clearUserInfo,
    clearAuth,
    initialize
  }
})

