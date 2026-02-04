import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import {
    getArticleList,
    getArticleDetail,
    likeArticle,
    collectArticle
} from '@/api/article'
import { createCacheManager } from '../helper/cacheManager'
import { useGlobalStore } from '../general/global'

export const useArticleStore = defineStore('article', () => {
    const globalStore = useGlobalStore()

    // 创建缓存管理器
    const listCacheManager = createCacheManager({
        storageKey: 'articleListCache',
        ttl: 5 * 60 * 1000, // 5分钟
        maxSize: 10
    })

    const detailCacheManager = createCacheManager({
        storageKey: 'articleDetailCache',
        ttl: 10 * 60 * 1000, // 10分钟
        maxSize: 20
    })

    // 用户点赞状态 { [articleId]: boolean }
    const likedArticles = ref({})

    // 用户收藏状态 { [articleId]: boolean }
    const favoriteArticles = ref({})

    /**
     * 获取文章列表（带缓存）
     */
    async function fetchArticleList(params = {}, useCache = true) {
        // 尝试从缓存获取
        if (useCache) {
            const cached = listCacheManager.get(params)
            if (cached) {
                return cached
            }
        }

        // 从服务器获取
        const response = await getArticleList(params)
        const data = response.data

        // 缓存数据
        listCacheManager.set(params, data)

        return data
    }

    /**
     * 获取文章详情（带缓存）
     */
    async function fetchArticleDetail(articleId, useCache = true) {
        // 尝试从缓存获取
        if (useCache) {
            const cached = detailCacheManager.get(articleId)
            if (cached) {
                return cached
            }
        }

        // 从服务器获取
        const response = await getArticleDetail(articleId)
        const data = response.data

        // 缓存数据
        detailCacheManager.set(articleId, data)

        return data
    }

    /**
     * 点赞文章（乐观更新）
     */
    async function toggleLike(articleId) {
        const originalState = likedArticles.value[articleId] || false

        try {
            // 乐观更新UI
            likedArticles.value[articleId] = !originalState

            // 更新缓存中的点赞数
            const cached = detailCacheManager.get(articleId)
            if (cached) {
                cached.likeCount = (cached.likeCount || 0) + (originalState ? -1 : 1)
                cached.isLiked = !originalState
                detailCacheManager.set(articleId, cached)
            }

            // 发送请求
            await likeArticle(articleId)
        } catch (error) {
            // 回滚状态
            likedArticles.value[articleId] = originalState

            // 回滚缓存
            const cached = detailCacheManager.get(articleId)
            if (cached) {
                cached.likeCount = (cached.likeCount || 0) + (originalState ? 1 : -1)
                cached.isLiked = originalState
                detailCacheManager.set(articleId, cached)
            }

            globalStore.setError(error)
            console.error('[ArticleStore] 点赞操作失败:', error)
            throw error
        }
    }

    /**
     * 收藏文章（乐观更新）
     */
    async function toggleFavorite(articleId) {
        const originalState = favoriteArticles.value[articleId] || false

        try {
            // 乐观更新UI
            favoriteArticles.value[articleId] = !originalState

            // 更新缓存中的收藏数
            const cached = detailCacheManager.get(articleId)
            if (cached) {
                cached.collectCount = (cached.collectCount || 0) + (originalState ? -1 : 1)
                cached.isCollected = !originalState
                detailCacheManager.set(articleId, cached)
            }

            // 发送请求
            await collectArticle(articleId)
        } catch (error) {
            // 回滚状态
            favoriteArticles.value[articleId] = originalState

            // 回滚缓存
            const cached = detailCacheManager.get(articleId)
            if (cached) {
                cached.collectCount = (cached.collectCount || 0) + (originalState ? 1 : -1)
                cached.isCollected = originalState
                detailCacheManager.set(articleId, cached)
            }

            globalStore.setError(error)
            console.error('[ArticleStore] 收藏操作失败:', error)
            throw error
        }
    }

    /**
     * 检查文章是否已点赞
     */
    const isArticleLiked = computed(() => (articleId) => {
        return !!likedArticles.value[articleId]
    })

    /**
     * 检查文章是否已收藏
     */
    const isArticleFavorited = computed(() => (articleId) => {
        return !!favoriteArticles.value[articleId]
    })

    /**
     * 清除所有缓存
     */
    function clearAllCache() {
        listCacheManager.clear()
        detailCacheManager.clear()
    }

    /**
     * 清除过期缓存
     */
    function clearExpiredCache() {
        listCacheManager.clearExpired()
        detailCacheManager.clearExpired()
    }

    /**
     * 获取缓存统计信息
     */
    function getCacheStats() {
        return {
            list: listCacheManager.getStats(),
            detail: detailCacheManager.getStats()
        }
    }

    /**
     * 初始化 - 清除过期缓存
     */
    function initialize() {
        clearExpiredCache()
    }

    // 自动初始化
    initialize()

    return {
        // State
        likedArticles,
        favoriteArticles,

        // Getters
        isArticleLiked,
        isArticleFavorited,

        // Actions
        fetchArticleList,
        fetchArticleDetail,
        toggleLike,
        toggleFavorite,
        clearAllCache,
        clearExpiredCache,
        getCacheStats,
        initialize
    }
}, {
    // 使用 Pinia 持久化插件
    persist: {
        key: 'article-store',
        paths: ['likedArticles', 'favoriteArticles']
    }
})
