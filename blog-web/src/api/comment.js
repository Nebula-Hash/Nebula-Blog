import request from '@/utils/request'

// 获取评论列表
export const getCommentList = (articleId, params) => {
  return request({
    url: `/comment/list/${articleId}`,
    method: 'get',
    params
  })
}

// 发布评论
export const publishComment = (data) => {
  return request({
    url: '/comment/publish',
    method: 'post',
    data
  })
}

// 删除评论
export const deleteComment = (id) => {
  return request({
    url: `/comment/${id}`,
    method: 'delete'
  })
}

// 点赞评论
export const likeComment = (id) => {
  return request({
    url: `/comment/like/${id}`,
    method: 'post'
  })
}
