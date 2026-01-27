import request from '@/utils/request'

/**
 * 获取标签列表（客户端）
 * @returns {Promise} 标签列表
 */
export const getTagList = () => {
  return request({
    url: '/tag/list',
    method: 'get'
  })
}

/**
 * 根据ID获取标签详情
 * @param {number} id 标签ID
 * @returns {Promise} 标签详情
 */
export const getTagDetail = (id) => {
  return request({
    url: `/tag/detail/${id}`,
    method: 'get'
  })
}
