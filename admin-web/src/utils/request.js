// 通信请求与响应封装

import axios from 'axios'
import { useUserStore } from '@/stores/user'

const request = axios.create({
  baseURL: '/api/admin',
  timeout: 5000
})

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
      // Token无效或其他业务错误
      if (res.code === 401) {
        window.$message?.error(res.message || '登录已过期，请重新登录')
        const userStore = useUserStore()
        userStore.logout()
        // 跳转到登录页
        setTimeout(() => {
          window.location.href = '/login'
        }, 1000)
      } else {
        window.$message?.error(res.message || '请求失败')
      }
      return Promise.reject(new Error(res.message || '请求失败'))
    }
  },
  (error) => {
    // HTTP状态码401 - Token验证失败
    if (error.response?.status === 401) {
      window.$message?.error('登录已过期，请重新登录')
      const userStore = useUserStore()
      userStore.logout()
      // 跳转到登录页
      setTimeout(() => {
        window.location.href = '/login'
      }, 1000)
    } else {
      window.$message?.error(error.message || '网络错误')
    }
    return Promise.reject(error)
  }
)

export default request
