// 用户管理

import request from '@/utils/request'

// ==================== 管理员管理 ====================

// 分页查询管理员列表
export const getAdminList = (params) => {
    return request({
        url: '/user/admin/page',
        method: 'get',
        params
    })
}

// 获取管理员详情
export const getAdminDetail = (id) => {
    return request({
        url: `/user/admin/${id}`,
        method: 'get'
    })
}

// 新增管理员
export const createAdmin = (data) => {
    return request({
        url: '/user/admin',
        method: 'post',
        data
    })
}

// 更新管理员
export const updateAdmin = (data) => {
    return request({
        url: '/user/admin',
        method: 'put',
        data
    })
}

// 删除管理员
export const deleteAdmin = (id) => {
    return request({
        url: `/user/admin/${id}`,
        method: 'delete'
    })
}

// ==================== 普通用户管理 ====================

// 分页查询普通用户列表
export const getClientList = (params) => {
    return request({
        url: '/user/client/page',
        method: 'get',
        params
    })
}

// 获取普通用户详情
export const getClientDetail = (id) => {
    return request({
        url: `/user/client/${id}`,
        method: 'get'
    })
}

// 新增普通用户
export const createClient = (data) => {
    return request({
        url: '/user/client',
        method: 'post',
        data
    })
}

// 更新普通用户
export const updateClient = (data) => {
    return request({
        url: '/user/client',
        method: 'put',
        data
    })
}

// 删除普通用户
export const deleteClient = (id) => {
    return request({
        url: `/user/client/${id}`,
        method: 'delete'
    })
}