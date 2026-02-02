/**
 * 评论管理 API
 */

import request from '@/utils/request'

/**
 * 获取评论列表（分页）
 * @param {Object} params - 查询参数
 * @param {number} params.current - 当前页
 * @param {number} params.size - 每页大小
 * @param {number} [params.articleId] - 文章ID（可选）
 * @param {number} [params.userId] - 用户ID（可选）
 * @param {number} [params.auditStatus] - 审核状态（可选）0-待审核 1-审核通过 2-审核拒绝
 * @param {string} [params.keyword] - 评论内容关键词（可选）
 * @returns {Promise} 评论分页数据
 */
export const getCommentList = (params) => {
    return request.get('/comment/list', { params })
}

/**
 * 审核评论
 * @param {number} id - 评论ID
 * @param {number} auditStatus - 审核状态 1-通过 2-拒绝
 * @returns {Promise} 操作结果
 */
export const auditComment = (id, auditStatus) => {
    return request.put(`/comment/audit/${id}`, null, {
        params: { auditStatus }
    })
}

/**
 * 删除评论（级联删除子评论）
 * @param {number} id - 评论ID
 * @returns {Promise} 操作结果
 */
export const deleteComment = (id) => {
    return request.delete(`/comment/${id}`)
}

/**
 * 批量审核评论
 * @param {Array<number>} ids - 评论ID列表
 * @param {number} auditStatus - 审核状态 1-通过 2-拒绝
 * @returns {Promise} 批量审核结果
 */
export const batchAuditComments = (ids, auditStatus) => {
    return request.put('/comment/audit/batch', ids, {
        params: { auditStatus }
    })
}

/**
 * 批量删除评论
 * @param {Array<number>} ids - 评论ID列表
 * @returns {Promise} 批量删除结果
 */
export const batchDeleteComments = (ids) => {
    return request.delete('/comment/batch', { data: ids })
}

/**
 * 获取待审核评论数量
 * @returns {Promise<number>} 待审核数量
 */
export const getPendingAuditCount = () => {
    return request.get('/comment/pending/count')
}
