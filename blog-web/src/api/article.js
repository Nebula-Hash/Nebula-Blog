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

// 获取热门文章
export const getHotArticles = (limit = 5) => {
  return request({
    url: '/article/hot',
    method: 'get',
    params: { limit }
  })
}

// 获取推荐文章
export const getRecommendArticles = (limit = 5) => {
  return request({
    url: '/article/recommend',
    method: 'get',
    params: { limit }
  })
}

// 获取我的文章
export const getMyArticles = (params) => {
  return request({
    url: '/article/my',
    method: 'get',
    params
  })
}

// 点赞文章
export const likeArticle = (id) => {
  return request({
    url: `/article/like/${id}`,
    method: 'post'
  })
}

// 收藏文章
export const collectArticle = (id) => {
  return request({
    url: `/article/collect/${id}`,
    method: 'post'
  })
}
