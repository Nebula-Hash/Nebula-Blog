/**
 * 通用缓存管理器
 * 提供统一的缓存管理功能，支持过期时间、容量限制等
 */
import { getItem, setItem } from '../../utils/storage'

function stableStringify(value) {
    if (value === null || value === undefined) return String(value)
    const type = typeof value
    if (type === 'string') return JSON.stringify(value)
    if (type === 'number' || type === 'boolean' || type === 'bigint') return String(value)
    if (type === 'function' || type === 'symbol') return JSON.stringify(String(value))

    if (Array.isArray(value)) {
        return `[${value.map(stableStringify).join(',')}]`
    }

    if (value instanceof Date) {
        return JSON.stringify(value.toISOString())
    }

    if (value instanceof Map) {
        const entries = Array.from(value.entries()).map(([k, v]) => [stableStringify(k), stableStringify(v)])
        entries.sort((a, b) => (a[0] > b[0] ? 1 : a[0] < b[0] ? -1 : 0))
        return `{${entries.map(([k, v]) => `${k}:${v}`).join(',')}}`
    }

    if (value instanceof Set) {
        const items = Array.from(value.values()).map(stableStringify).sort()
        return `[${items.join(',')}]`
    }

    if (type === 'object') {
        const keys = Object.keys(value).sort()
        const pairs = keys.map((k) => `${JSON.stringify(k)}:${stableStringify(value[k])}`)
        return `{${pairs.join(',')}}`
    }

    return JSON.stringify(String(value))
}

/**
 * 创建缓存管理器
 * @param {Object} options - 配置选项
 * @param {string} options.storageKey - localStorage 存储键名
 * @param {number} options.ttl - 缓存过期时间（毫秒）
 * @param {number} options.maxSize - 最大缓存数量
 * @param {Function} options.keyGenerator - 缓存键生成函数
 * @returns {Object} 缓存管理器实例
 */
export function createCacheManager(options = {}) {
    const {
        storageKey = 'cache',
        ttl = 5 * 60 * 1000, // 默认 5 分钟
        maxSize = 50,
        keyGenerator = (key) => stableStringify(key)
    } = options

    let cache = {}

    /**
     * 初始化缓存（从 localStorage 恢复）
     */
    function initialize() {
        cache = getItem(storageKey, {})
        clearExpired()
    }

    /**
     * 生成缓存键
     * @param {*} key - 原始键
     * @returns {string} 缓存键
     */
    function generateKey(key) {
        return keyGenerator(key)
    }

    /**
     * 检查缓存是否过期
     * @param {number} timestamp - 时间戳
     * @returns {boolean}
     */
    function isExpired(timestamp) {
        return Date.now() - timestamp > ttl
    }

    /**
     * 获取缓存
     * @param {*} key - 缓存键
     * @returns {*} 缓存数据，不存在或已过期返回 null
     */
    function get(key) {
        const cacheKey = generateKey(key)
        const cached = cache[cacheKey]

        if (!cached) return null

        if (isExpired(cached.timestamp)) {
            delete cache[cacheKey]
            save()
            return null
        }

        return cached.data
    }

    /**
     * 设置缓存
     * @param {*} key - 缓存键
     * @param {*} data - 缓存数据
     */
    function set(key, data) {
        const cacheKey = generateKey(key)
        cache[cacheKey] = {
            data,
            timestamp: Date.now()
        }

        // 检查缓存大小，超出限制则清理最旧的
        limitSize()
        save()
    }

    /**
     * 删除缓存
     * @param {*} key - 缓存键
     */
    function remove(key) {
        const cacheKey = generateKey(key)
        delete cache[cacheKey]
        save()
    }

    /**
     * 检查缓存是否存在且未过期
     * @param {*} key - 缓存键
     * @returns {boolean}
     */
    function has(key) {
        return get(key) !== null
    }

    /**
     * 清除过期缓存
     */
    function clearExpired() {
        const keys = Object.keys(cache)
        let hasExpired = false

        keys.forEach(key => {
            if (isExpired(cache[key].timestamp)) {
                delete cache[key]
                hasExpired = true
            }
        })

        if (hasExpired) {
            save()
        }
    }

    /**
     * 清除所有缓存
     */
    function clear() {
        cache = {}
        save()
    }

    /**
     * 限制缓存大小
     */
    function limitSize() {
        const keys = Object.keys(cache)

        if (keys.length <= maxSize) return

        // 按时间戳排序，保留最新的
        const sortedKeys = keys.sort((a, b) => {
            return cache[b].timestamp - cache[a].timestamp
        })

        const keysToKeep = sortedKeys.slice(0, maxSize)
        const newCache = {}

        keysToKeep.forEach(key => {
            newCache[key] = cache[key]
        })

        cache = newCache
    }

    /**
     * 保存到 localStorage
     */
    function save() {
        setItem(storageKey, cache)
    }

    /**
     * 获取缓存统计信息
     * @returns {Object} 统计信息
     */
    function getStats() {
        const keys = Object.keys(cache)
        const now = Date.now()

        let validCount = 0
        let expiredCount = 0

        keys.forEach(key => {
            if (isExpired(cache[key].timestamp)) {
                expiredCount++
            } else {
                validCount++
            }
        })

        return {
            total: keys.length,
            valid: validCount,
            expired: expiredCount,
            maxSize,
            ttl
        }
    }

    /**
     * 获取所有缓存键
     * @returns {Array} 缓存键数组
     */
    function keys() {
        return Object.keys(cache)
    }

    /**
     * 获取所有有效缓存的值
     * @returns {Array} 缓存值数组
     */
    function values() {
        const result = []
        const cacheKeys = Object.keys(cache)

        cacheKeys.forEach(key => {
            if (!isExpired(cache[key].timestamp)) {
                result.push(cache[key].data)
            }
        })

        return result
    }

    /**
     * 批量更新缓存数据
     * @param {Function} updater - (data, cacheKey) => newData | undefined
     */
    function updateAll(updater) {
        if (typeof updater !== 'function') return

        let hasChanged = false
        const cacheKeys = Object.keys(cache)

        cacheKeys.forEach(cacheKey => {
            const entry = cache[cacheKey]
            if (!entry) return

            if (isExpired(entry.timestamp)) {
                delete cache[cacheKey]
                hasChanged = true
                return
            }

            const nextData = updater(entry.data, cacheKey)
            if (nextData !== undefined) {
                cache[cacheKey] = {
                    ...entry,
                    data: nextData
                }
                hasChanged = true
            }
        })

        if (hasChanged) {
            save()
        }
    }

    // 初始化
    initialize()

    return {
        get,
        set,
        remove,
        has,
        clear,
        clearExpired,
        getStats,
        keys,
        values,
        updateAll,
        initialize
    }
}

/**
 * 创建带自动刷新的缓存管理器
 * @param {Object} options - 配置选项
 * @param {Function} options.fetcher - 数据获取函数
 * @param {Object} options.cacheOptions - 缓存管理器配置
 * @returns {Object} 缓存管理器实例
 */
export function createAutoRefreshCache(options = {}) {
    const { fetcher, cacheOptions = {} } = options
    const cacheManager = createCacheManager(cacheOptions)

    /**
     * 获取数据（自动从缓存或服务器获取）
     * @param {*} key - 缓存键
     * @param {boolean} forceRefresh - 是否强制刷新
     * @returns {Promise<*>} 数据
     */
    async function fetch(key, forceRefresh = false) {
        // 如果不强制刷新，先尝试从缓存获取
        if (!forceRefresh) {
            const cached = cacheManager.get(key)
            if (cached !== null) {
                return cached
            }
        }

        // 从服务器获取
        const data = await fetcher(key)

        // 缓存数据
        cacheManager.set(key, data)

        return data
    }

    return {
        ...cacheManager,
        fetch
    }
}

export default {
    createCacheManager,
    createAutoRefreshCache
}
