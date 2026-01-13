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

// 获取标签列表
export const getTagList = () => {
  return request({
    url: '/tag/list',
    method: 'get'
  })
}

// 创建标签
export const createTag = (data) => {
  return request({
    url: '/tag/create',
    method: 'post',
    data
  })
}

// 更新标签
export const updateTag = (data) => {
  return request({
    url: '/tag/update',
    method: 'put',
    data
  })
}

// 删除标签
export const deleteTag = (id) => {
  return request({
    url: `/tag/${id}`,
    method: 'delete'
  })
}
