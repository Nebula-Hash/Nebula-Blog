import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login, register, getUserInfo, logout } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const tokenExpireTime = ref(Number(localStorage.getItem('token_expire')) || 0)
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || 'null'))

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
    localStorage.setItem('token', newToken)

    if (timeout && timeout > 0) {
      const expireTime = Date.now() + timeout * 1000
      tokenExpireTime.value = expireTime
      localStorage.setItem('token_expire', String(expireTime))
    }
  }

  // 登录
  const handleLogin = async (loginForm) => {
    const res = await login(loginForm)
    setToken(res.data.token, res.data.tokenTimeout)
    const info = {
      userId: res.data.userId,
      username: res.data.username,
      nickname: res.data.nickname,
      avatar: res.data.avatar,
      roleKey: res.data.roleKey
    }
    userInfo.value = info
    localStorage.setItem('userInfo', JSON.stringify(info))
    return res
  }

  // 注册
  const handleRegister = async (registerForm) => {
    const res = await register(registerForm)
    setToken(res.data.token, res.data.tokenTimeout)
    const info = {
      userId: res.data.userId,
      username: res.data.username,
      nickname: res.data.nickname,
      avatar: res.data.avatar,
      roleKey: res.data.roleKey
    }
    userInfo.value = info
    localStorage.setItem('userInfo', JSON.stringify(info))
    return res
  }

  // 获取用户信息
  const fetchUserInfo = async () => {
    const res = await getUserInfo()
    userInfo.value = res.data
    localStorage.setItem('userInfo', JSON.stringify(res.data))
    return res
  }

  // 登出
  const handleLogout = async () => {
    try {
      await logout()
    } catch (e) {
      // 忽略登出接口错误
    }
    clearAuth()
  }

  /**
   * 清除认证信息（静默清除，不调用接口）
   */
  const clearAuth = () => {
    token.value = ''
    tokenExpireTime.value = 0
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('token_expire')
    localStorage.removeItem('userInfo')
  }

  return {
    token,
    tokenExpireTime,
    userInfo,
    isTokenExpired,
    isLoggedIn,
    setToken,
    handleLogin,
    handleRegister,
    fetchUserInfo,
    handleLogout,
    clearAuth
  }
})
