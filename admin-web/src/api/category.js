// 分类管理

import request from '@/utils/request'

// 获取分类列表
export const getCategoryList = () => {
  return request({
    url: '/category/list',
    method: 'get'
  })
}

// 创建分类
export const createCategory = (data) => {
  return request({
    url: '/category/create',
    method: 'post',
    data
  })
}

// 更新分类
export const updateCategory = (data) => {
  return request({
    url: '/category/update',
    method: 'put',
    data
  })
}

// 删除分类
export const deleteCategory = (id) => {
  return request({
    url: `/category/${id}`,
    method: 'delete'
  })
}
