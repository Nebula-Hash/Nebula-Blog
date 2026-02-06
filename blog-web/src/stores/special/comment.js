import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import {
    getCommentList,
    publishComment,
    toggleLikeComment,
    deleteComment,
    getMoreReplies
} from '@/api/comment'

/**
 * 评论状态管理 - 优化版
 * 使用扁平化数据结构提升性能
 */
export const useCommentStore = defineStore('comment', () => {
    // 扁平化存储所有评论 { [commentId]: comment }
    const commentsById = ref(new Map())

    // 按文章ID存储评论ID列表 { [articleId]: { ids: [], total, current, size, pages } }
    const commentsByArticle = ref(new Map())

    // 用户点赞状态 { [commentId]: boolean }
    const likedComments = ref({})

    // 加载状态 { [articleId]: boolean }
    const loading = ref({})

    // 回复加载状态 { [rootId]: boolean }
    const replyLoading = ref({})

    /**
     * 获取文章的评论列表（树形结构）
     */
    const getArticleComments = computed(() => (articleId) => {
        const articleData = commentsByArticle.value.get(articleId)
        if (!articleData) return []

        // 将扁平化的评论转换为树形结构
        return buildCommentTree(articleData.ids)
    })

    /**
     * 构建评论树
     */
    function buildCommentTree(commentIds) {
        const rootComments = []
        const commentMap = new Map()

        // 第一遍：创建所有评论的副本
        commentIds.forEach(id => {
            const comment = commentsById.value.get(id)
            if (comment) {
                commentMap.set(id, { ...comment, children: [] })
            }
        })

        // 第二遍：构建树形结构
        commentMap.forEach(comment => {
            if (!comment.parentId) {
                // 根评论
                rootComments.push(comment)
            } else {
                // 子评论
                const parent = commentMap.get(comment.parentId)
                if (parent) {
                    parent.children.push(comment)
                }
            }
        })

        return rootComments
    }

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
        return commentsByArticle.value.get(articleId)?.total || 0
    })

    /**
     * 检查是否还有更多评论
     */
    const hasMoreComments = computed(() => (articleId) => {
        const data = commentsByArticle.value.get(articleId)
        if (!data) return false
        return data.current < data.pages
    })

    /**
     * 存储评论到扁平化结构
     */
    function storeComments(comments) {
        comments.forEach(comment => {
            commentsById.value.set(comment.id, comment)

            // 初始化点赞状态
            if (comment.isLiked !== undefined) {
                likedComments.value[comment.id] = comment.isLiked
            }

            // 递归存储子评论
            if (comment.children && comment.children.length > 0) {
                storeComments(comment.children)
            }
        })
    }

    /**
     * 提取所有评论ID（包括子评论）
     */
    function extractCommentIds(comments) {
        const ids = []
        comments.forEach(comment => {
            ids.push(comment.id)
            if (comment.children && comment.children.length > 0) {
                ids.push(...extractCommentIds(comment.children))
            }
        })
        return ids
    }

    /**
     * 加载评论列表（初始加载）
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
            const comments = pageData.records || []

            // 存储评论到扁平化结构
            storeComments(comments)

            // 提取所有评论ID
            const commentIds = extractCommentIds(comments)

            // 存储文章的评论索引
            commentsByArticle.value.set(articleId, {
                ids: commentIds,
                total: pageData.total || 0,
                current: pageData.current || 1,
                size: pageData.size || 10,
                pages: pageData.pages || 1
            })

            return pageData
        } catch (error) {
            throw error
        } finally {
            loading.value[articleId] = false
        }
    }

    /**
     * 加载更多评论（分页）
     */
    async function loadMoreComments(articleId) {
        const commentData = commentsByArticle.value.get(articleId)
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
            const comments = pageData.records || []

            // 存储新评论
            storeComments(comments)

            // 提取新评论ID
            const newCommentIds = extractCommentIds(comments)

            // 追加到文章的评论列表
            commentData.ids.push(...newCommentIds)
            commentData.current = pageData.current
            commentData.pages = pageData.pages

            return pageData
        } catch (error) {
            throw error
        } finally {
            loading.value[articleId] = false
        }
    }

    /**
     * 加载根评论的更多回复
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

            // 存储新回复
            storeComments(newReplies)

            // 提取新回复ID并添加到文章的评论列表
            const newReplyIds = extractCommentIds(newReplies)
            const articleData = commentsByArticle.value.get(articleId)
            if (articleData) {
                articleData.ids.push(...newReplyIds)
            }

            return {
                replies: newReplies,
                hasMore: pageData.current < pageData.pages,
                currentPage: pageData.current
            }
        } catch (error) {
            throw error
        } finally {
            replyLoading.value[rootId] = false
        }
    }

    /**
     * 发布评论或回复
     */
    async function publishCommentAction(data) {
        if (!data.content || !data.content.trim()) {
            throw new Error('评论内容不能为空')
        }

        try {
            const response = await publishComment(data)
            const newCommentId = response.data

            // 重新加载评论列表
            const articleData = commentsByArticle.value.get(data.articleId)
            await loadComments(data.articleId, {
                current: 1,
                size: articleData?.size || 10
            })

            return newCommentId
        } catch (error) {
            throw error
        }
    }

    /**
     * 切换点赞状态（乐观更新）
     */
    async function toggleLike(commentId, articleId) {
        const originalState = likedComments.value[commentId] || false
        const comment = commentsById.value.get(commentId)

        if (!comment) {
            console.warn('[CommentStore] 评论不存在:', commentId)
            return
        }

        try {
            // 乐观更新UI
            likedComments.value[commentId] = !originalState

            // 更新评论的点赞数
            const originalLikeCount = comment.likeCount || 0
            comment.likeCount = originalLikeCount + (originalState ? -1 : 1)
            comment.isLiked = !originalState

            // 发送请求
            await toggleLikeComment(commentId)
        } catch (error) {
            // 回滚状态
            likedComments.value[commentId] = originalState
            comment.likeCount = (comment.likeCount || 0) + (originalState ? 1 : -1)
            comment.isLiked = originalState
            throw error
        }
    }

    /**
     * 删除评论（递归删除子评论）
     */
    async function deleteCommentAction(commentId, articleId) {
        try {
            await deleteComment(commentId)

            // 收集要删除的所有评论ID（包括子评论）
            const idsToDelete = collectCommentIdsRecursive(commentId)

            // 从扁平化存储中删除
            idsToDelete.forEach(id => {
                commentsById.value.delete(id)
                delete likedComments.value[id]
            })

            // 从文章的评论列表中删除
            const articleData = commentsByArticle.value.get(articleId)
            if (articleData) {
                articleData.ids = articleData.ids.filter(id => !idsToDelete.includes(id))
                articleData.total = Math.max(0, articleData.total - idsToDelete.length)
            }
        } catch (error) {
            throw error
        }
    }

    /**
     * 递归收集评论及其子评论的ID
     */
    function collectCommentIdsRecursive(commentId) {
        const ids = [commentId]
        const comment = commentsById.value.get(commentId)

        if (comment) {
            // 查找所有子评论
            commentsById.value.forEach((c, id) => {
                if (c.parentId === commentId) {
                    ids.push(...collectCommentIdsRecursive(id))
                }
            })
        }

        return ids
    }

    /**
     * 清除文章的评论数据
     */
    function clearArticleComments(articleId) {
        const articleData = commentsByArticle.value.get(articleId)
        if (articleData) {
            // 删除所有相关评论
            articleData.ids.forEach(id => {
                commentsById.value.delete(id)
            })
            commentsByArticle.value.delete(articleId)
        }
    }

    /**
     * 初始化
     */
    function initialize() {
        // 清理可能存在的旧数据
        if (!(commentsById.value instanceof Map)) {
            commentsById.value = new Map()
        }
        if (!(commentsByArticle.value instanceof Map)) {
            commentsByArticle.value = new Map()
        }
    }

    return {
        // State
        commentsById,
        commentsByArticle,
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
        clearArticleComments,
        initialize
    }
}, {
    // 使用 Pinia 持久化插件
    persist: {
        key: 'comment-store',
        paths: ['likedComments']
    }
})
