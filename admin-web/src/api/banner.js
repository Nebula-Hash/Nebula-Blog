// 轮播图管理
import request from '@/utils/request'

/**
 * 分页查询轮播图列表
 * @param {number} current - 当前页码
 * @param {number} size - 每页数量
 */
export const getBannerList = (current = 1, size = 5) => {
    return request({
        url: '/banner/list',
        method: 'get',
        params: { current, size }
    })
}

/**
 * 获取轮播图详情
 * @param {number} id - 轮播图ID
 */
export const getBannerDetail = (id) => {
    return request({
        url: `/banner/detail/${id}`,
        method: 'get'
    })
}

/**
 * 上传轮播图图片（上传到临时目录）
 * @param {File} file - 图片文件
 */
export const uploadBannerImage = (file) => {
    const formData = new FormData()
    formData.append('file', file)
    return request({
        url: '/banner/upload',
        method: 'post',
        data: formData,
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    })
}

/**
 * 添加轮播图
 * @param {object} data - 轮播图数据
 */
export const addBanner = (data) => {
    return request({
        url: '/banner/add',
        method: 'post',
        data
    })
}

/**
 * 编辑轮播图
 * @param {object} data - 轮播图数据
 */
export const updateBanner = (data) => {
    return request({
        url: '/banner/update',
        method: 'put',
        data
    })
}

/**
 * 删除轮播图
 * @param {number} id - 轮播图ID
 */
export const deleteBanner = (id) => {
    return request({
        url: `/banner/${id}`,
        method: 'delete'
    })
}
