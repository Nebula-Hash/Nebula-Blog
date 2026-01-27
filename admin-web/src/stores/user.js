import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('admin_token') || '')
  const tokenExpireTime = ref(Number(localStorage.getItem('admin_token_expire')) || 0)
  const userInfo = ref(JSON.parse(localStorage.getItem('admin_userInfo') || '{}'))

  /**
   * 检查Token是否已过期
   */
  const isTokenExpired = computed(() => {
    if (!token.value || !tokenExpireTime.value) return true
    return Date.now() > tokenExpireTime.value
  })

  /**
   * 检查是否已登录（Token存在且未过期）
   */
  const isLoggedIn = computed(() => {
    return !!token.value && !isTokenExpired.value
  })

  /**
   * 设置Token和过期时间
   * @param {string} newToken - 新Token
   * @param {number} timeout - 过期时间（秒）
   */
  const setToken = (newToken, timeout) => {
    token.value = newToken
    localStorage.setItem('admin_token', newToken)

    if (timeout && timeout > 0) {
      const expireTime = Date.now() + timeout * 1000
      tokenExpireTime.value = expireTime
      localStorage.setItem('admin_token_expire', String(expireTime))
    }
  }

  /**
   * 设置用户信息
   */
  const setUserInfo = (info) => {
    userInfo.value = info
    localStorage.setItem('admin_userInfo', JSON.stringify(info))
  }

  /**
   * 登出，清除所有认证信息
   */
  const logout = () => {
    token.value = ''
    tokenExpireTime.value = 0
    userInfo.value = {}
    localStorage.removeItem('admin_token')
    localStorage.removeItem('admin_token_expire')
    localStorage.removeItem('admin_userInfo')
  }

  return {
    token,
    tokenExpireTime,
    userInfo,
    isTokenExpired,
    isLoggedIn,
    setToken,
    setUserInfo,
    logout
  }
})
