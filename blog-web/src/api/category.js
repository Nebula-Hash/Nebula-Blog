import request from '@/utils/request'

/**
 * 获取分类列表（客户端）
 * @returns {Promise} 分类列表
 */
export const getCategoryList = () => {
  return request({
    url: '/category/list',
    method: 'get'
  })
}

/**
 * 根据ID获取分类详情
 * @param {number} id 分类ID
 * @returns {Promise} 分类详情
 */
export const getCategoryDetail = (id) => {
  return request({
    url: `/category/detail/${id}`,
    method: 'get'
  })
}
