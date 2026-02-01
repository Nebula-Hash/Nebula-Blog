/**
 * 文章服务层
 * 封装文章相关的业务逻辑，提供统一的数据处理接口
 * 类似后端的 BlogArticleService
 */
import {
    getArticleList as getArticleListApi,
    getArticleDetail as getArticleDetailApi,
    getHotArticles as getHotArticlesApi,
    getRecommendArticles as getRecommendArticlesApi,
    likeArticle as likeArticleApi,
    collectArticle as collectArticleApi
} from '@/api/article'

/**
 * 文章查询服务
 */
export class ArticleQueryService {
    /**
     * 获取文章列表
     * @param {Object} params - 查询参数
     * @returns {Promise<Object>}
     */
    static async getList(params = {}) {
        const response = await getArticleListApi(params)
        return response.data
    }

    /**
     * 获取文章详情
     * @param {number|string} articleId - 文章ID
     * @returns {Promise<Object>}
     */
    static async getDetail(articleId) {
        const response = await getArticleDetailApi(articleId)
        return response.data
    }

    /**
     * 获取热门文章
     * @param {number} limit - 数量限制
     * @returns {Promise<Array>}
     */
    static async getHotArticles(limit = 5) {
        const response = await getHotArticlesApi(limit)
        return response.data
    }

    /**
     * 获取推荐文章
     * @param {number} limit - 数量限制
     * @returns {Promise<Array>}
     */
    static async getRecommendArticles(limit = 5) {
        const response = await getRecommendArticlesApi(limit)
        return response.data
    }

    /**
     * 搜索文章
     * @param {Object} searchParams - 搜索参数
     * @param {string} searchParams.title - 标题关键词
     * @param {string} searchParams.authorName - 作者名称
     * @param {string} searchParams.categoryName - 分类名称
     * @param {string} searchParams.tagName - 标签名称
     * @param {number} searchParams.current - 当前页
     * @param {number} searchParams.size - 每页大小
     * @returns {Promise<Object>}
     */
    static async search(searchParams) {
        return await this.getList(searchParams)
    }
}

/**
 * 文章交互服务
 */
export class ArticleInteractionService {
    /**
     * 点赞文章
     * @param {number|string} articleId - 文章ID
     * @returns {Promise<void>}
     */
    static async like(articleId) {
        await likeArticleApi(articleId)
    }

    /**
     * 收藏文章
     * @param {number|string} articleId - 文章ID
     * @returns {Promise<void>}
     */
    static async collect(articleId) {
        await collectArticleApi(articleId)
    }
}

/**
 * 文章数据转换服务
 */
export class ArticleConverterService {
    /**
     * 格式化文章列表数据
     * @param {Array} articles - 文章列表
     * @returns {Array}
     */
    static formatArticleList(articles) {
        if (!Array.isArray(articles)) return []

        return articles.map(article => this.formatArticleItem(article))
    }

    /**
     * 格式化单个文章项
     * @param {Object} article - 文章对象
     * @returns {Object}
     */
    static formatArticleItem(article) {
        return {
            ...article,
            // 确保数值字段有默认值
            viewCount: article.viewCount || 0,
            likeCount: article.likeCount || 0,
            commentCount: article.commentCount || 0,
            collectCount: article.collectCount || 0,
            // 确保布尔字段有默认值
            isLiked: article.isLiked || false,
            isCollected: article.isCollected || false,
            isTop: article.isTop || false,
            // 确保数组字段有默认值
            tags: article.tags || []
        }
    }

    /**
     * 格式化文章详情数据
     * @param {Object} article - 文章详情对象
     * @returns {Object}
     */
    static formatArticleDetail(article) {
        if (!article) return null

        return {
            ...this.formatArticleItem(article),
            // 确保内容字段存在
            content: article.content || '',
            htmlContent: article.htmlContent || ''
        }
    }

    /**
     * 提取文章摘要
     * @param {string} content - 文章内容
     * @param {number} maxLength - 最大长度
     * @returns {string}
     */
    static extractSummary(content, maxLength = 150) {
        if (!content) return ''

        // 移除 Markdown 标记
        const plainText = content
            .replace(/[#*`>\-\[\]()]/g, '')
            .replace(/\n+/g, ' ')
            .trim()

        if (plainText.length <= maxLength) {
            return plainText
        }

        return plainText.substring(0, maxLength) + '...'
    }
}

/**
 * 统一的文章服务入口
 */
export default {
    query: ArticleQueryService,
    interaction: ArticleInteractionService,
    converter: ArticleConverterService
}
