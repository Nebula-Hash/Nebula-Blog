import request from '@/utils/request'

// 用户登录
export const login = (data) => {
  return request({
    url: '/auth/login',
    method: 'post',
    data
  })
}

// 用户注册
export const register = (data) => {
  return request({
    url: '/auth/register',
    method: 'post',
    data
  })
}

// 获取用户信息
export const getUserInfo = () => {
  return request({
    url: '/auth/info',
    method: 'get'
  })
}

// 用户登出（静默模式，即使失败也不显示错误）
export const logout = () => {
  return request({
    url: '/auth/logout',
    method: 'post',
    silent: true  // 静默模式，不显示错误提示
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
