// 用户认证

import request from '@/utils/request'

// 用户登录
export const login = (data) => {
  return request({
    url: '/auth/login',
    method: 'post',
    data
  })
}

// 获取当前用户信息
export const getUserInfo = () => {
  return request({
    url: '/auth/info',
    method: 'get'
  })
}

// 用户登出
export const logout = () => {
  return request({
    url: '/auth/logout',
    method: 'post'
  })
}

// 刷新Token
export const refreshToken = () => {
  return request({
    url: '/auth/refresh',
    method: 'post'
  })
}

// 检查登录状态
export const checkLogin = () => {
  return request({
    url: '/auth/check',
    method: 'get'
  })
}
