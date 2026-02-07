/**
 * 文章相关的组合式函数
 * 封装文章的通用逻辑，提供统一的数据获取和交互接口
 */
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ArticleQueryService, ArticleInteractionService } from '@/services/articleService'
import { showWarning } from '@/utils/common'
import { createErrorHandler } from '@/utils/errorHandler'
import { useArticleStore } from '@/stores'

/**
 * 文章列表管理
 * @param {Object} options - 配置选项
 * @param {boolean} options.useCache - 是否使用缓存，默认 true
 * @param {number} options.pageSize - 每页大小
 * @returns {Object}
 */
export function useArticleList(options = {}) {
    const {
        useCache = true,
        pageSize = 10
    } = options

    const errorHandler = createErrorHandler('ArticleList')

    const loading = ref(false)
    const articles = ref([])
    const currentPage = ref(1)
    const total = ref(0)
    const totalPages = computed(() => Math.ceil(total.value / pageSize))

    /**
     * 加载文章列表
     * @param {Object} params - 查询参数
     * @param {Object} options - 行为选项
     * @param {boolean} options.useCacheOverride - 是否覆盖默认缓存策略
     */
    const loadArticles = async (params = {}, options = {}) => {
        loading.value = true
        try {
            const queryParams = {
                current: currentPage.value,
                size: pageSize,
                ...params
            }

            const shouldUseCache = options.useCacheOverride ?? useCache
            const data = await ArticleQueryService.getList(queryParams, shouldUseCache)
            articles.value = data.records || []
            total.value = data.total || 0
        } catch (error) {
            errorHandler.handleLoad(error, '文章列表')
        } finally {
            loading.value = false
        }
    }

    /**
     * 刷新列表（强制从服务器获取）
     */
    const refresh = async (params = {}) => {
        currentPage.value = 1
        await loadArticles(params, { useCacheOverride: false })
    }

    /**
     * 切换页码
     */
    const changePage = async (page, params = {}) => {
        currentPage.value = page
        await loadArticles(params)
    }

    return {
        loading,
        articles,
        currentPage,
        total,
        totalPages,
        loadArticles,
        refresh,
        changePage
    }
}

/**
 * 文章详情管理
 * @param {Object} options - 配置选项
 * @param {boolean} options.useCache - 是否使用缓存，默认 true
 * @returns {Object}
 */
export function useArticleDetail(options = {}) {
    const {
        useCache = true
    } = options

    const errorHandler = createErrorHandler('ArticleDetail')

    const loading = ref(false)
    const article = ref(null)

    /**
     * 加载文章详情
     * @param {number|string} articleId - 文章ID
     * @param {Object} options - 行为选项
     * @param {boolean} options.useCacheOverride - 是否覆盖默认缓存策略
     */
    const loadArticle = async (articleId, options = {}) => {
        loading.value = true
        try {
            const shouldUseCache = options.useCacheOverride ?? useCache
            const data = await ArticleQueryService.getDetail(articleId, shouldUseCache)
            article.value = data
        } catch (error) {
            errorHandler.handleLoad(error, '文章详情')
            throw error
        } finally {
            loading.value = false
        }
    }

    /**
     * 刷新文章详情（强制从服务器获取）
     */
    const refresh = async (articleId) => {
        await loadArticle(articleId, { useCacheOverride: false })
    }

    return {
        loading,
        article,
        loadArticle,
        refresh
    }
}

/**
 * 通用文章集合加载器（热门/推荐等）
 */
function createArticleCollection(fetcher, resourceName) {
    const errorHandler = createErrorHandler(resourceName)
    const loading = ref(false)
    const articles = ref([])

    const load = async () => {
        loading.value = true
        try {
            articles.value = await fetcher()
        } catch (error) {
            errorHandler.handleLoad(error, resourceName)
        } finally {
            loading.value = false
        }
    }

    return {
        loading,
        articles,
        load
    }
}

/**
 * 热门文章
 * @param {Object} options - 配置选项
 * @param {number} options.limit - 数量限制
 * @param {boolean} options.useCache - 是否使用缓存
 */
export function useHotArticles(options = {}) {
    const { limit = 5, useCache = true } = options
    return createArticleCollection(
        () => ArticleQueryService.getHotArticles(limit, useCache),
        '热门文章'
    )
}

/**
 * 推荐文章
 * @param {Object} options - 配置选项
 * @param {number} options.limit - 数量限制
 * @param {boolean} options.useCache - 是否使用缓存
 */
export function useRecommendArticles(options = {}) {
    const { limit = 3, useCache = true } = options
    return createArticleCollection(
        () => ArticleQueryService.getRecommendArticles(limit, useCache),
        '推荐文章'
    )
}

/**
 * 文章交互操作（点赞、收藏）- 已禁用，需要登录功能
 * @returns {Object}
 */
export function useArticleInteraction(options = {}) {
    const {
        checkAuth = null,
        onAuthFail = null
    } = options

    const liking = ref(false)
    const collecting = ref(false)
    const errorHandler = createErrorHandler('ArticleInteraction')
    const articleStore = useArticleStore()

    const resolveAuth = () => {
        if (typeof checkAuth === 'function') {
            return !!checkAuth()
        }
        return false
    }

    const handleAuthFail = () => {
        if (typeof onAuthFail === 'function') {
            onAuthFail()
            return
        }
        showWarning('操作需要登录，请先登录')
    }

    const updateArticleRef = (articleRef, updater) => {
        if (!articleRef || typeof updater !== 'function') return

        if (Object.prototype.hasOwnProperty.call(articleRef, 'value')) {
            const current = articleRef.value || {}
            articleRef.value = updater(current)
            return
        }

        const next = updater(articleRef)
        Object.assign(articleRef, next)
    }

    /**
     * 点赞文章
     */
    const toggleLike = async (articleId, articleRef = null) => {
        if (!resolveAuth()) {
            handleAuthFail()
            return false
        }

        const currentState = !!(articleRef?.value?.isLiked ?? articleRef?.isLiked ?? articleStore.isArticleLiked(articleId))
        const nextState = !currentState

        liking.value = true
        updateArticleRef(articleRef, (article) => ({
            ...article,
            isLiked: nextState,
            likeCount: Math.max(0, (article.likeCount || 0) + (nextState ? 1 : -1))
        }))

        try {
            await ArticleInteractionService.like(articleId)
            return true
        } catch (error) {
            updateArticleRef(articleRef, (article) => ({
                ...article,
                isLiked: currentState,
                likeCount: Math.max(0, (article.likeCount || 0) + (currentState ? 1 : -1))
            }))
            errorHandler.handle(error, '点赞')
            return false
        } finally {
            liking.value = false
        }
    }

    /**
     * 收藏文章
     */
    const toggleCollect = async (articleId, articleRef = null) => {
        if (!resolveAuth()) {
            handleAuthFail()
            return false
        }

        const currentState = !!(articleRef?.value?.isCollected ?? articleRef?.isCollected ?? articleStore.isArticleFavorited(articleId))
        const nextState = !currentState

        collecting.value = true
        updateArticleRef(articleRef, (article) => ({
            ...article,
            isCollected: nextState,
            collectCount: Math.max(0, (article.collectCount || 0) + (nextState ? 1 : -1))
        }))

        try {
            await ArticleInteractionService.collect(articleId)
            return true
        } catch (error) {
            updateArticleRef(articleRef, (article) => ({
                ...article,
                isCollected: currentState,
                collectCount: Math.max(0, (article.collectCount || 0) + (currentState ? 1 : -1))
            }))
            errorHandler.handle(error, '收藏')
            return false
        } finally {
            collecting.value = false
        }
    }

    return {
        liking,
        collecting,
        toggleLike,
        toggleCollect
    }
}

/**
 * 文章导航
 * @returns {Object}
 */
export function useArticleNavigation() {
    const router = useRouter()

    /**
     * 跳转到文章详情
     * @param {number|string} articleId - 文章ID
     */
    const goToDetail = (articleId) => {
        router.push(`/article/${articleId}`)
    }

    /**
     * 跳转到分类页面
     * @param {number|string} categoryId - 分类ID
     */
    const goToCategory = (categoryId) => {
        router.push(`/category/${categoryId}`)
    }

    /**
     * 跳转到标签页面
     * @param {number|string} tagId - 标签ID
     */
    const goToTag = (tagId) => {
        router.push(`/tag/${tagId}`)
    }

    /**
     * 跳转到搜索页面
     * @param {string} keyword - 搜索关键词
     */
    const goToSearch = (keyword) => {
        router.push({
            path: '/search',
            query: { keyword }
        })
    }

    return {
        goToDetail,
        goToCategory,
        goToTag,
        goToSearch
    }
}
