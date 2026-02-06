import { defineStore } from 'pinia'
import { createCacheManager } from '../helper/cacheManager'
import { getBannerList } from '@/api/banner'
import { getCategoryList } from '@/api/category'
import { getTagList } from '@/api/tag'
import { getHotArticles, getRecommendArticles } from '@/api/article'
import { LOCAL_CACHE_CONFIG } from '@/config/constants'

/**
 * 统一缓存管理 Store
 * 为 banner、category、tag 等提供统一的缓存策略
 */
export const useCacheStore = defineStore('cache', () => {
    // Banner 缓存管理器
    const bannerCacheManager = createCacheManager({
        storageKey: `${LOCAL_CACHE_CONFIG.KEY_PREFIX}banner`,
        ttl: LOCAL_CACHE_CONFIG.TTL.THIRTY_MINUTES,
        maxSize: 5
    })

    // Category 缓存管理器
    const categoryCacheManager = createCacheManager({
        storageKey: `${LOCAL_CACHE_CONFIG.KEY_PREFIX}category`,
        ttl: LOCAL_CACHE_CONFIG.TTL.THIRTY_MINUTES,
        maxSize: 5
    })

    // Tag 缓存管理器
    const tagCacheManager = createCacheManager({
        storageKey: `${LOCAL_CACHE_CONFIG.KEY_PREFIX}tag`,
        ttl: LOCAL_CACHE_CONFIG.TTL.THIRTY_MINUTES,
        maxSize: 5
    })

    // 热门文章缓存管理器
    const hotArticlesCacheManager = createCacheManager({
        storageKey: `${LOCAL_CACHE_CONFIG.KEY_PREFIX}hotArticles`,
        ttl: LOCAL_CACHE_CONFIG.TTL.TEN_MINUTES,
        maxSize: 5
    })

    // 推荐文章缓存管理器
    const recommendArticlesCacheManager = createCacheManager({
        storageKey: `${LOCAL_CACHE_CONFIG.KEY_PREFIX}recommendArticles`,
        ttl: LOCAL_CACHE_CONFIG.TTL.TEN_MINUTES,
        maxSize: 5
    })

    /**
     * 获取 Banner 列表（带缓存）
     */
    async function fetchBannerList(useCache = true) {
        const cacheKey = 'bannerList'

        if (useCache) {
            const cached = bannerCacheManager.get(cacheKey)
            if (cached) return cached
        }

        try {
            const response = await getBannerList()
            const data = response.data || []
            bannerCacheManager.set(cacheKey, data)
            return data
        } catch (error) {
            throw error
        }
    }

    /**
     * 获取分类列表（带缓存）
     */
    async function fetchCategoryList(useCache = true) {
        const cacheKey = 'categoryList'

        if (useCache) {
            const cached = categoryCacheManager.get(cacheKey)
            if (cached) return cached
        }

        try {
            const response = await getCategoryList()
            const data = response.data || []
            categoryCacheManager.set(cacheKey, data)
            return data
        } catch (error) {
            throw error
        }
    }

    /**
     * 获取标签列表（带缓存）
     */
    async function fetchTagList(useCache = true) {
        const cacheKey = 'tagList'

        if (useCache) {
            const cached = tagCacheManager.get(cacheKey)
            if (cached) return cached
        }

        try {
            const response = await getTagList()
            const data = response.data || []
            tagCacheManager.set(cacheKey, data)
            return data
        } catch (error) {
            throw error
        }
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

        try {
            const response = await getHotArticles(limit)
            const data = response.data || []
            hotArticlesCacheManager.set(cacheKey, data)
            return data
        } catch (error) {
            throw error
        }
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

        try {
            const response = await getRecommendArticles(limit)
            const data = response.data || []
            recommendArticlesCacheManager.set(cacheKey, data)
            return data
        } catch (error) {
            throw error
        }
    }

    /**
     * 清除所有缓存
     */
    function clearAllCache() {
        bannerCacheManager.clear()
        categoryCacheManager.clear()
        tagCacheManager.clear()
        hotArticlesCacheManager.clear()
        recommendArticlesCacheManager.clear()
    }

    /**
     * 清除过期缓存
     */
    function clearExpiredCache() {
        bannerCacheManager.clearExpired()
        categoryCacheManager.clearExpired()
        tagCacheManager.clearExpired()
        hotArticlesCacheManager.clearExpired()
        recommendArticlesCacheManager.clearExpired()
    }

    /**
     * 获取缓存统计信息
     */
    function getCacheStats() {
        return {
            banner: bannerCacheManager.getStats(),
            category: categoryCacheManager.getStats(),
            tag: tagCacheManager.getStats(),
            hotArticles: hotArticlesCacheManager.getStats(),
            recommendArticles: recommendArticlesCacheManager.getStats()
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
        // Actions
        fetchBannerList,
        fetchCategoryList,
        fetchTagList,
        fetchHotArticles,
        fetchRecommendArticles,
        clearAllCache,
        clearExpiredCache,
        getCacheStats,
        initialize
    }
})
