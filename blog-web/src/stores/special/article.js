import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import {
    getArticleList,
    getArticleDetail,
    getHotArticles,
    getRecommendArticles,
    likeArticle,
    collectArticle
} from '@/api/article'
import { createCacheManager } from '../helper/cacheManager'
import { LOCAL_CACHE_CONFIG } from '@/config/constants'

export const useArticleStore = defineStore('article', () => {
    // 创建缓存管理器
    const listCacheManager = createCacheManager({
        storageKey: `${LOCAL_CACHE_CONFIG.KEY_PREFIX}articleList`,
        ttl: LOCAL_CACHE_CONFIG.TTL.FIVE_MINUTES,
        maxSize: 10
    })

    const detailCacheManager = createCacheManager({
        storageKey: `${LOCAL_CACHE_CONFIG.KEY_PREFIX}articleDetail`,
        ttl: LOCAL_CACHE_CONFIG.TTL.TEN_MINUTES,
        maxSize: 20
    })

    const hotArticlesCacheManager = createCacheManager({
        storageKey: `${LOCAL_CACHE_CONFIG.KEY_PREFIX}hotArticles`,
        ttl: LOCAL_CACHE_CONFIG.TTL.TEN_MINUTES,
        maxSize: 5
    })

    const recommendArticlesCacheManager = createCacheManager({
        storageKey: `${LOCAL_CACHE_CONFIG.KEY_PREFIX}recommendArticles`,
        ttl: LOCAL_CACHE_CONFIG.TTL.TEN_MINUTES,
        maxSize: 5
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
     * 获取热门文章（带缓存）
     */
    async function fetchHotArticles(limit = 5, useCache = true) {
        const cacheKey = `hotArticles_${limit}`

        if (useCache) {
            const cached = hotArticlesCacheManager.get(cacheKey)
            if (cached) return cached
        }

        const response = await getHotArticles(limit)
        const data = response.data || []
        hotArticlesCacheManager.set(cacheKey, data)

        return data
    }

    /**
     * 获取推荐文章（带缓存）
     */
    async function fetchRecommendArticles(limit = 5, useCache = true) {
        const cacheKey = `recommendArticles_${limit}`

        if (useCache) {
            const cached = recommendArticlesCacheManager.get(cacheKey)
            if (cached) return cached
        }

        const response = await getRecommendArticles(limit)
        const data = response.data || []
        recommendArticlesCacheManager.set(cacheKey, data)

        return data
    }

    /**
     * 更新所有缓存中的文章数据
     */
    function updateCachedArticle(articleId, updater) {
        if (!articleId || typeof updater !== 'function') return

        const updateRecords = (records) => {
            let changed = false
            const nextRecords = records.map((item) => {
                if (item.id !== articleId) return item
                changed = true
                return updater({ ...item })
            })
            return changed ? nextRecords : null
        }

        const detail = detailCacheManager.get(articleId)
        if (detail) {
            const updatedDetail = updater({ ...detail })
            if (updatedDetail !== undefined) {
                detailCacheManager.set(articleId, updatedDetail)
            }
        }

        listCacheManager.updateAll((data) => {
            if (!data || !Array.isArray(data.records)) return undefined
            const nextRecords = updateRecords(data.records)
            if (!nextRecords) return undefined
            return {
                ...data,
                records: nextRecords
            }
        })

        hotArticlesCacheManager.updateAll((data) => {
            if (!Array.isArray(data)) return undefined
            const nextRecords = updateRecords(data)
            return nextRecords || undefined
        })

        recommendArticlesCacheManager.updateAll((data) => {
            if (!Array.isArray(data)) return undefined
            const nextRecords = updateRecords(data)
            return nextRecords || undefined
        })
    }

    /**
     * 点赞文章（乐观更新）
     */
    async function toggleLike(articleId) {
        const originalState = likedArticles.value[articleId] || false
        const nextState = !originalState
        const applyLikeUpdate = (state) => {
            updateCachedArticle(articleId, (article) => ({
                ...article,
                likeCount: (article.likeCount || 0) + (state ? 1 : -1),
                isLiked: state
            }))
        }

        try {
            // 乐观更新UI
            likedArticles.value[articleId] = nextState
            applyLikeUpdate(nextState)

            // 发送请求
            await likeArticle(articleId)
        } catch (error) {
            // 回滚状态
            likedArticles.value[articleId] = originalState
            applyLikeUpdate(originalState)
            throw error
        }
    }

    /**
     * 收藏文章（乐观更新）
     */
    async function toggleFavorite(articleId) {
        const originalState = favoriteArticles.value[articleId] || false
        const nextState = !originalState
        const applyFavoriteUpdate = (state) => {
            updateCachedArticle(articleId, (article) => ({
                ...article,
                collectCount: (article.collectCount || 0) + (state ? 1 : -1),
                isCollected: state
            }))
        }

        try {
            // 乐观更新UI
            favoriteArticles.value[articleId] = nextState
            applyFavoriteUpdate(nextState)

            // 发送请求
            await collectArticle(articleId)
        } catch (error) {
            // 回滚状态
            favoriteArticles.value[articleId] = originalState
            applyFavoriteUpdate(originalState)
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
        hotArticlesCacheManager.clear()
        recommendArticlesCacheManager.clear()
    }

    /**
     * 清除过期缓存
     */
    function clearExpiredCache() {
        listCacheManager.clearExpired()
        detailCacheManager.clearExpired()
        hotArticlesCacheManager.clearExpired()
        recommendArticlesCacheManager.clearExpired()
    }

    /**
     * 获取缓存统计信息
     */
    function getCacheStats() {
        return {
            list: listCacheManager.getStats(),
            detail: detailCacheManager.getStats(),
            hot: hotArticlesCacheManager.getStats(),
            recommend: recommendArticlesCacheManager.getStats()
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
        fetchHotArticles,
        fetchRecommendArticles,
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
