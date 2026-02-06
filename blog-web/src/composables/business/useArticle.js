/**
 * 文章相关的组合式函数
 * 封装文章的通用逻辑，提供统一的数据获取和交互接口
 */
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ArticleQueryService } from '@/services/articleService'
import { showWarning } from '@/utils/common'
import { createErrorHandler } from '@/utils/errorHandler'

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
     */
    const loadArticles = async (params = {}) => {
        loading.value = true
        try {
            const queryParams = {
                current: currentPage.value,
                size: pageSize,
                ...params
            }

            const data = await ArticleQueryService.getList(queryParams, useCache)
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
        await loadArticles(params)
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
     */
    const loadArticle = async (articleId) => {
        loading.value = true
        try {
            const data = await ArticleQueryService.getDetail(articleId, useCache)
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
        await ArticleQueryService.getDetail(articleId, false)
    }

    return {
        loading,
        article,
        loadArticle,
        refresh
    }
}

/**
 * 文章交互操作（点赞、收藏）- 已禁用，需要登录功能
 * @returns {Object}
 */
export function useArticleInteraction() {
    const liking = ref(false)
    const collecting = ref(false)

    /**
     * 点赞文章 - 已禁用
     */
    const toggleLike = async (articleId, articleRef = null) => {
        showWarning('点赞功能需要登录，请前往权限测试项目体验')
        return false
    }

    /**
     * 收藏文章 - 已禁用
     */
    const toggleCollect = async (articleId, articleRef = null) => {
        showWarning('收藏功能需要登录，请前往权限测试项目体验')
        return false
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
