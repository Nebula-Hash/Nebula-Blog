import request from '@/utils/request'

/**
 * 获取文章评论列表（根评论分页）
 * @param {number} articleId - 文章ID
 * @param {Object} params - 查询参数
 * @param {number} params.current - 当前页码
 * @param {number} params.size - 每页数量
 * @returns {Promise<{records: Array, total: number, current: number, size: number}>}
 */
export function getCommentList(articleId, params = {}) {
    return request({
        url: `/comment/list/${articleId}`,
        method: 'get',
        params: {
            current: params.current || 1,
            size: params.size || 10
        }
    })
}

/**
 * 发布评论或回复
 * @param {Object} data - 评论数据
 * @param {number} data.articleId - 文章ID
 * @param {string} data.content - 评论内容
 * @param {number} [data.parentId] - 父评论ID（回复时必填）
 * @param {number} [data.replyUserId] - 被回复用户ID（回复时选填）
 * @returns {Promise<number>} 新评论ID
 */
export function publishComment(data) {
    return request({
        url: '/comment/publish',
        method: 'post',
        data
    })
}

/**
 * 点赞/取消点赞评论（切换）
 * @param {number} commentId - 评论ID
 * @returns {Promise<void>}
 */
export function toggleLikeComment(commentId) {
    return request({
        url: `/comment/like/${commentId}`,
        method: 'post'
    })
}

/**
 * 删除自己的评论
 * @param {number} commentId - 评论ID
 * @returns {Promise<void>}
 */
export function deleteComment(commentId) {
    return request({
        url: `/comment/delete/${commentId}`,
        method: 'delete'
    })
}

/**
 * 获取根评论下的更多回复（分页）
 * @param {number} rootId - 根评论ID
 * @param {Object} params - 查询参数
 * @param {number} params.current - 当前页码
 * @param {number} params.size - 每页数量
 * @returns {Promise<{records: Array, total: number, current: number, size: number}>}
 */
export function getMoreReplies(rootId, params = {}) {
    return request({
        url: `/comment/replies/${rootId}`,
        method: 'get',
        params: {
            current: params.current || 1,
            size: params.size || 10
        }
    })
}