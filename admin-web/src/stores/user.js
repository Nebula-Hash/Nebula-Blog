import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import * as tokenService from '@/services/tokenService'
import { getItem, setItem, removeItem } from '@/utils/storage'

export const useUserStore = defineStore('user', () => {
  // 用户信息状态
  const userInfo = ref(null)

  /**
   * 检查Token是否已过期
   */
  const isTokenExpired = computed(() => {
    return tokenService.isTokenExpired()
  })

  /**
   * 检查是否已登录（Token存在且未过期）
   */
  const isLoggedIn = computed(() => {
    return !!tokenService.getToken() && !isTokenExpired.value
  })

  /**
   * 设置用户信息
   * @param {Object} info - 用户信息
   */
  const setUserInfo = (info) => {
    userInfo.value = info
    setItem('admin_userInfo', info)
  }

  /**
   * 清除用户信息
   */
  const clearUserInfo = () => {
    userInfo.value = null
    removeItem('admin_userInfo')
  }

  /**
   * 清除所有认证信息
   */
  const clearAuth = () => {
    userInfo.value = null
    tokenService.clearToken()
    removeItem('admin_userInfo')
  }

  /**
   * 初始化 - 从localStorage恢复状态
   */
  const initialize = () => {
    // 恢复用户信息
    const savedUserInfo = getItem('admin_userInfo', null)
    if (savedUserInfo) {
      userInfo.value = savedUserInfo
    }

    // 检查Token是否过期
    if (tokenService.isTokenExpired()) {
      clearAuth()
    }
  }

  return {
    userInfo,
    isTokenExpired,
    isLoggedIn,
    setUserInfo,
    clearUserInfo,
    clearAuth,
    initialize
  }
})
