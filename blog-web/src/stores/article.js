import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import {
    getArticleList,
    getArticleDetail,
    likeArticle,
    collectArticle
} from '@/api/article'
import { getItem, setItem } from '@/utils/storage'

export const useArticleStore = defineStore('article', () => {
    // 文章列表缓存 { [cacheKey]: { data, timestamp } }
    const listCache = ref({})

    // 文章详情缓存 { [articleId]: { data, timestamp } }
    const detailCache = ref({})

    // 用户点赞状态 { [articleId]: boolean }
    const likedArticles = ref({})

    // 用户收藏状态 { [articleId]: boolean }
    const favoriteArticles = ref({})

    // 缓存过期时间（毫秒）
    const cacheExpiry = {
        list: 5 * 60 * 1000, // 5分钟
        detail: 10 * 60 * 1000, // 10分钟
    }

    /**
     * 生成列表缓存键
     */
    function generateListCacheKey(params) {
        return JSON.stringify(params || {})
    }

    /**
     * 检查缓存是否过期
     */
    function isCacheExpired(timestamp, expiry) {
        return Date.now() - timestamp > expiry
    }

    /**
     * 获取缓存的列表数据
     */
    function getCachedList(params) {
        const key = generateListCacheKey(params)
        const cached = listCache.value[key]

        if (!cached) return null

        if (isCacheExpired(cached.timestamp, cacheExpiry.list)) {
            delete listCache.value[key]
            return null
        }

        return cached.data
    }

    /**
     * 设置列表缓存
     */
    function setCachedList(params, data) {
        const key = generateListCacheKey(params)
        listCache.value[key] = {
            data,
            timestamp: Date.now()
        }
        saveListCache()
    }

    /**
     * 获取缓存的详情数据
     */
    function getCachedDetail(articleId) {
        const cached = detailCache.value[articleId]

        if (!cached) return null

        if (isCacheExpired(cached.timestamp, cacheExpiry.detail)) {
            delete detailCache.value[articleId]
            return null
        }

        return cached.data
    }

    /**
     * 设置详情缓存
     */
    function setCachedDetail(articleId, data) {
        detailCache.value[articleId] = {
            data,
            timestamp: Date.now()
        }
        saveDetailCache()
    }

    /**
     * 清除过期缓存
     */
    function clearExpiredCache() {
        const now = Date.now()

        // 清除过期的列表缓存
        Object.keys(listCache.value).forEach(key => {
            if (isCacheExpired(listCache.value[key].timestamp, cacheExpiry.list)) {
                delete listCache.value[key]
            }
        })

        // 清除过期的详情缓存
        Object.keys(detailCache.value).forEach(key => {
            if (isCacheExpired(detailCache.value[key].timestamp, cacheExpiry.detail)) {
                delete detailCache.value[key]
            }
        })

        saveListCache()
        saveDetailCache()
    }

    /**
     * 清除所有缓存
     */
    function clearAllCache() {
        listCache.value = {}
        detailCache.value = {}
        saveListCache()
        saveDetailCache()
    }

    /**
     * 获取文章列表（带缓存）
     */
    async function fetchArticleList(params = {}, useCache = true) {
        // 尝试从缓存获取
        if (useCache) {
            const cached = getCachedList(params)
            if (cached) {
                return cached
            }
        }

        // 从服务器获取
        const response = await getArticleList(params)
        const data = response.data

        // 缓存数据
        setCachedList(params, data)

        return data
    }

    /**
     * 获取文章详情（带缓存）
     */
    async function fetchArticleDetail(articleId, useCache = true) {
        // 尝试从缓存获取
        if (useCache) {
            const cached = getCachedDetail(articleId)
            if (cached) {
                return cached
            }
        }

        // 从服务器获取
        const response = await getArticleDetail(articleId)
        const data = response.data

        // 缓存数据
        setCachedDetail(articleId, data)

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
            const cached = detailCache.value[articleId]
            if (cached) {
                cached.data.likeCount = (cached.data.likeCount || 0) + (originalState ? -1 : 1)
                cached.data.isLiked = !originalState
            }

            // 发送请求
            await likeArticle(articleId)

            // 保存点赞状态
            saveLikedArticles()
        } catch (error) {
            // 回滚状态
            likedArticles.value[articleId] = originalState

            // 回滚缓存
            const cached = detailCache.value[articleId]
            if (cached) {
                cached.data.likeCount = (cached.data.likeCount || 0) + (originalState ? 1 : -1)
                cached.data.isLiked = originalState
            }

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
            const cached = detailCache.value[articleId]
            if (cached) {
                cached.data.collectCount = (cached.data.collectCount || 0) + (originalState ? -1 : 1)
                cached.data.isCollected = !originalState
            }

            // 发送请求
            await collectArticle(articleId)

            // 保存收藏状态
            saveFavoriteArticles()
        } catch (error) {
            // 回滚状态
            favoriteArticles.value[articleId] = originalState

            // 回滚缓存
            const cached = detailCache.value[articleId]
            if (cached) {
                cached.data.collectCount = (cached.data.collectCount || 0) + (originalState ? 1 : -1)
                cached.data.isCollected = originalState
            }

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
     * 保存列表缓存到本地存储
     */
    function saveListCache() {
        // 只保存最近的10个列表缓存
        const keys = Object.keys(listCache.value)
        if (keys.length > 10) {
            const sortedKeys = keys.sort((a, b) => {
                return listCache.value[b].timestamp - listCache.value[a].timestamp
            })
            const keysToKeep = sortedKeys.slice(0, 10)
            const newCache = {}
            keysToKeep.forEach(key => {
                newCache[key] = listCache.value[key]
            })
            listCache.value = newCache
        }
        setItem('articleListCache', listCache.value)
    }

    /**
     * 保存详情缓存到本地存储
     */
    function saveDetailCache() {
        // 只保存最近的20个详情缓存
        const keys = Object.keys(detailCache.value)
        if (keys.length > 20) {
            const sortedKeys = keys.sort((a, b) => {
                return detailCache.value[b].timestamp - detailCache.value[a].timestamp
            })
            const keysToKeep = sortedKeys.slice(0, 20)
            const newCache = {}
            keysToKeep.forEach(key => {
                newCache[key] = detailCache.value[key]
            })
            detailCache.value = newCache
        }
        setItem('articleDetailCache', detailCache.value)
    }

    /**
     * 保存点赞状态到本地存储
     */
    function saveLikedArticles() {
        setItem('likedArticles', likedArticles.value)
    }

    /**
     * 保存收藏状态到本地存储
     */
    function saveFavoriteArticles() {
        setItem('favoriteArticles', favoriteArticles.value)
    }

    /**
     * 初始化 - 从localStorage恢复状态
     */
    function initialize() {
        listCache.value = getItem('articleListCache', {})
        detailCache.value = getItem('articleDetailCache', {})
        likedArticles.value = getItem('likedArticles', {})
        favoriteArticles.value = getItem('favoriteArticles', {})

        // 清除过期缓存
        clearExpiredCache()
    }

    // 自动初始化
    initialize()

    return {
        // State
        listCache,
        detailCache,
        likedArticles,
        favoriteArticles,
        cacheExpiry,

        // Getters
        isArticleLiked,
        isArticleFavorited,

        // Actions
        getCachedList,
        setCachedList,
        getCachedDetail,
        setCachedDetail,
        clearExpiredCache,
        clearAllCache,
        fetchArticleList,
        fetchArticleDetail,
        toggleLike,
        toggleFavorite,
        initialize
    }
})
