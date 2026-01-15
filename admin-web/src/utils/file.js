// 文件上传与删除相关接口

import request from '@/utils/request'

// 上传图片
export const uploadImage = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/file/upload/image',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 上传文章图片
export const uploadArticleImage = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/file/upload/article',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 删除文件
export const deleteFile = (fileUrl) => {
  return request({
    url: '/file/delete',
    method: 'delete',
    params: { fileUrl }
  })
}