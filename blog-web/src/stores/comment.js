import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import {
    getCommentList,
    publishComment,
    toggleLikeComment,
    deleteComment,
    getMoreReplies
} from '@/api/comment'
import { getItem, setItem } from '@/utils/storage'

export const useCommentStore = defineStore('comment', () => {
    // 评论列表数据 { [articleId]: { records, total, current, size, pages } }
    const comments = ref({})

    // 用户点赞状态 { [commentId]: boolean }
    const likedComments = ref({})

    // 加载状态 { [articleId]: boolean }
    const loading = ref({})

    // 回复加载状态 { [rootId]: boolean }
    const replyLoading = ref({})

    /**
     * 获取文章的评论列表（扁平结构）
     */
    const getArticleComments = computed(() => (articleId) => {
        return comments.value[articleId]?.records || []
    })

    /**
     * 检查是否已点赞
     */
    const isCommentLiked = computed(() => (commentId) => {
        return !!likedComments.value[commentId]
    })

    /**
     * 获取评论总数
     */
    const getCommentTotal = computed(() => (articleId) => {
        return comments.value[articleId]?.total || 0
    })

    /**
     * 检查是否还有更多评论
     */
    const hasMoreComments = computed(() => (articleId) => {
        const data = comments.value[articleId]
        if (!data) return false
        return data.current < data.pages
    })

    /**
     * 加载评论列表（初始加载）
     * @param {number} articleId - 文章ID
     * @param {Object} params - 查询参数
     */
    async function loadComments(articleId, params = {}) {
        loading.value[articleId] = true

        try {
            const response = await getCommentList(articleId, {
                current: 1,
                size: 10,
                ...params
            })

            const pageData = response.data
            comments.value[articleId] = {
                records: pageData.records || [],
                total: pageData.total || 0,
                current: pageData.current || 1,
                size: pageData.size || 10,
                pages: pageData.pages || 1
            }

            // 初始化点赞状态
            initLikedStatus(pageData.records || [])

            return pageData
        } catch (error) {
            console.error('[CommentStore] 加载评论失败:', error)
            throw error
        } finally {
            loading.value[articleId] = false
        }
    }

    /**
     * 加载更多评论（分页）
     * @param {number} articleId - 文章ID
     */
    async function loadMoreComments(articleId) {
        const commentData = comments.value[articleId]
        if (!commentData || commentData.current >= commentData.pages || loading.value[articleId]) {
            return
        }

        loading.value[articleId] = true

        try {
            const nextPage = commentData.current + 1
            const response = await getCommentList(articleId, {
                current: nextPage,
                size: commentData.size
            })

            const pageData = response.data

            // 追加新评论
            commentData.records.push(...(pageData.records || []))
            commentData.current = pageData.current
            commentData.pages = pageData.pages

            // 初始化点赞状态
            initLikedStatus(pageData.records || [])

            return pageData
        } catch (error) {
            console.error('[CommentStore] 加载更多评论失败:', error)
            throw error
        } finally {
            loading.value[articleId] = false
        }
    }

    /**
     * 加载根评论的更多回复
     * @param {number} rootId - 根评论ID
     * @param {number} articleId - 文章ID
     * @param {number} currentPage - 当前页码
     */
    async function loadMoreReplies(rootId, articleId, currentPage = 1) {
        replyLoading.value[rootId] = true

        try {
            const response = await getMoreReplies(rootId, {
                current: currentPage + 1,
                size: 10
            })

            const pageData = response.data
            const newReplies = pageData.records || []

            // 找到根评论并追加回复
            const commentData = comments.value[articleId]
            if (commentData) {
                const rootComment = findCommentById(commentData.records, rootId)
                if (rootComment && rootComment.children) {
                    rootComment.children.push(...newReplies)
                }
            }

            // 初始化点赞状态
            initLikedStatus(newReplies)

            return {
                replies: newReplies,
                hasMore: pageData.current < pageData.pages,
                currentPage: pageData.current
            }
        } catch (error) {
            console.error('[CommentStore] 加载更多回复失败:', error)
            throw error
        } finally {
            replyLoading.value[rootId] = false
        }
    }

    /**
     * 根据ID查找评论（递归）
     */
    function findCommentById(comments, commentId) {
        for (const comment of comments) {
            if (comment.id === commentId) {
                return comment
            }
            if (comment.children && comment.children.length > 0) {
                const found = findCommentById(comment.children, commentId)
                if (found) return found
            }
        }
        return null
    }

    /**
     * 初始化评论的点赞状态
     */
    function initLikedStatus(commentList) {
        if (!commentList || commentList.length === 0) return

        commentList.forEach(comment => {
            if (comment.isLiked !== undefined) {
                likedComments.value[comment.id] = comment.isLiked
            }
            // 递归处理子评论
            if (comment.children && comment.children.length > 0) {
                initLikedStatus(comment.children)
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
     */
    async function publishCommentAction(data) {
        // 验证评论内容
        if (!data.content || !data.content.trim()) {
            throw new Error('评论内容不能为空')
        }

        try {
            const response = await publishComment(data)
            const newCommentId = response.data

            // 重新加载评论列表以获取完整数据
            // 注意：后端只返回ID，需要重新加载才能获取完整评论信息
            await loadComments(data.articleId, {
                current: 1,
                size: comments.value[data.articleId]?.size || 10
            })

            return newCommentId
        } catch (error) {
            console.error('[CommentStore] 发布评论失败:', error)
            throw error
        }
    }

    /**
     * 切换点赞状态（乐观更新）
     * @param {number} commentId - 评论ID
     * @param {number} articleId - 文章ID（用于更新列表中的点赞数）
     */
    async function toggleLike(commentId, articleId) {
        const originalState = likedComments.value[commentId] || false

        try {
            // 乐观更新UI
            likedComments.value[commentId] = !originalState

            // 更新评论列表中的点赞数（递归查找）
            if (comments.value[articleId]) {
                const comment = findCommentById(comments.value[articleId].records, commentId)
                if (comment) {
                    comment.likeCount = (comment.likeCount || 0) + (originalState ? -1 : 1)
                    comment.isLiked = !originalState
                }
            }

            // 发送请求（后端统一使用切换接口）
            await toggleLikeComment(commentId)

            // 保存点赞状态到本地存储
            saveLikedComments()
        } catch (error) {
            // 回滚状态
            likedComments.value[commentId] = originalState

            // 回滚点赞数
            if (comments.value[articleId]) {
                const comment = findCommentById(comments.value[articleId].records, commentId)
                if (comment) {
                    comment.likeCount = (comment.likeCount || 0) + (originalState ? 1 : -1)
                    comment.isLiked = originalState
                }
            }

            console.error('[CommentStore] 点赞操作失败:', error)
            throw error
        }
    }

    /**
     * 删除评论（递归删除子评论）
     * @param {number} commentId - 评论ID
     * @param {number} articleId - 文章ID
     */
    async function deleteCommentAction(commentId, articleId) {
        try {
            await deleteComment(commentId)

            // 递归删除评论及其子评论
            if (comments.value[articleId]) {
                const deleteCount = removeCommentRecursive(comments.value[articleId].records, commentId)
                comments.value[articleId].total = Math.max(0, comments.value[articleId].total - deleteCount)
            }

            // 移除点赞状态
            delete likedComments.value[commentId]
            saveLikedComments()
        } catch (error) {
            console.error('[CommentStore] 删除评论失败:', error)
            throw error
        }
    }

    /**
     * 递归删除评论及其子评论
     * @returns {number} 删除的评论数量
     */
    function removeCommentRecursive(commentList, commentId) {
        for (let i = 0; i < commentList.length; i++) {
            if (commentList[i].id === commentId) {
                // 计算删除的评论数（包括子评论）
                const deleteCount = countComments(commentList[i])
                commentList.splice(i, 1)
                return deleteCount
            }
            // 递归查找子评论
            if (commentList[i].children && commentList[i].children.length > 0) {
                const count = removeCommentRecursive(commentList[i].children, commentId)
                if (count > 0) return count
            }
        }
        return 0
    }

    /**
     * 统计评论数量（包括子评论）
     */
    function countComments(comment) {
        let count = 1
        if (comment.children && comment.children.length > 0) {
            comment.children.forEach(child => {
                count += countComments(child)
            })
        }
        return count
    }

    /**
     * 保存点赞状态到本地存储
     */
    function saveLikedComments() {
        setItem('likedComments', likedComments.value)
    }

    /**
     * 初始化 - 从localStorage恢复状态
     */
    function initialize() {
        const savedLikedComments = getItem('likedComments', {})
        likedComments.value = savedLikedComments
    }

    return {
        // State
        comments,
        likedComments,
        loading,
        replyLoading,

        // Getters
        getArticleComments,
        isCommentLiked,
        getCommentTotal,
        hasMoreComments,

        // Actions
        loadComments,
        loadMoreComments,
        loadMoreReplies,
        publishComment: publishCommentAction,
        toggleLike,
        deleteComment: deleteCommentAction,
        initialize
    }
})
