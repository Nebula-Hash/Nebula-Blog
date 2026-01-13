import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login, register, getUserInfo, logout } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(null)

  // 登录
  const handleLogin = async (loginForm) => {
    const res = await login(loginForm)
    token.value = res.data.token
    localStorage.setItem('token', res.data.token)
    userInfo.value = {
      userId: res.data.userId,
      username: res.data.username,
      nickname: res.data.nickname
    }
    return res
  }

  // 注册
  const handleRegister = async (registerForm) => {
    const res = await register(registerForm)
    token.value = res.data.token
    localStorage.setItem('token', res.data.token)
    userInfo.value = {
      userId: res.data.userId,
      username: res.data.username,
      nickname: res.data.nickname
    }
    return res
  }

  // 获取用户信息
  const fetchUserInfo = async () => {
    const res = await getUserInfo()
    userInfo.value = res.data
    return res
  }

  // 登出
  const handleLogout = async () => {
    await logout()
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
  }

  return {
    token,
    userInfo,
    handleLogin,
    handleRegister,
    fetchUserInfo,
    handleLogout
  }
})
