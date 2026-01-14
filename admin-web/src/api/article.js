// 文章管理

import request from '@/utils/request'

// 获取文章列表
export const getArticleList = (params) => {
  return request({
    url: '/article/list',
    method: 'get',
    params
  })
}

// 获取文章详情
export const getArticleDetail = (id) => {
  return request({
    url: `/article/${id}`,
    method: 'get'
  })
}

// 发布文章
export const publishArticle = (data) => {
  return request({
    url: '/article/publish',
    method: 'post',
    data
  })
}

// 更新文章
export const updateArticle = (data) => {
  return request({
    url: '/article/update',
    method: 'put',
    data
  })
}

// 删除文章
export const deleteArticle = (id) => {
  return request({
    url: `/article/${id}`,
    method: 'delete'
  })
}
