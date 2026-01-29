/**
 * 本地存储工具类
 * 提供统一的localStorage操作接口
 */

/**
 * 获取存储的值
 * @param {string} key - 存储键名
 * @param {*} defaultValue - 默认值
 * @returns {*} 存储的值或默认值
 */
export function getItem(key, defaultValue = null) {
    try {
        const value = localStorage.getItem(key)
        if (value === null) {
            return defaultValue
        }
        // 尝试解析JSON
        try {
            return JSON.parse(value)
        } catch {
            // 如果不是JSON，返回原始字符串
            return value
        }
    } catch (error) {
        console.error(`获取存储失败 [${key}]:`, error)
        return defaultValue
    }
}

/**
 * 设置存储的值
 * @param {string} key - 存储键名
 * @param {*} value - 要存储的值
 * @returns {boolean} 是否成功
 */
export function setItem(key, value) {
    try {
        const valueToStore = typeof value === 'string' ? value : JSON.stringify(value)
        localStorage.setItem(key, valueToStore)
        return true
    } catch (error) {
        console.error(`设置存储失败 [${key}]:`, error)
        return false
    }
}

/**
 * 移除存储的值
 * @param {string} key - 存储键名
 * @returns {boolean} 是否成功
 */
export function removeItem(key) {
    try {
        localStorage.removeItem(key)
        return true
    } catch (error) {
        console.error(`移除存储失败 [${key}]:`, error)
        return false
    }
}

/**
 * 清空所有存储
 * @returns {boolean} 是否成功
 */
export function clear() {
    try {
        localStorage.clear()
        return true
    } catch (error) {
        console.error('清空存储失败:', error)
        return false
    }
}

/**
 * 批量设置存储
 * @param {Object} items - 键值对对象
 * @returns {boolean} 是否全部成功
 */
export function setItems(items) {
    try {
        Object.entries(items).forEach(([key, value]) => {
            setItem(key, value)
        })
        return true
    } catch (error) {
        console.error('批量设置存储失败:', error)
        return false
    }
}

/**
 * 批量获取存储
 * @param {string[]} keys - 键名数组
 * @returns {Object} 键值对对象
 */
export function getItems(keys) {
    const result = {}
    keys.forEach(key => {
        result[key] = getItem(key)
    })
    return result
}

/**
 * 批量移除存储
 * @param {string[]} keys - 键名数组
 * @returns {boolean} 是否全部成功
 */
export function removeItems(keys) {
    try {
        keys.forEach(key => {
            removeItem(key)
        })
        return true
    } catch (error) {
        console.error('批量移除存储失败:', error)
        return false
    }
}

/**
 * 检查键是否存在
 * @param {string} key - 存储键名
 * @returns {boolean} 是否存在
 */
export function hasItem(key) {
    return localStorage.getItem(key) !== null
}

/**
 * 获取所有键名
 * @returns {string[]} 键名数组
 */
export function getAllKeys() {
    return Object.keys(localStorage)
}

/**
 * 获取存储项数量
 * @returns {number} 存储项数量
 */
export function getLength() {
    return localStorage.length
}

export default {
    getItem,
    setItem,
    removeItem,
    clear,
    setItems,
    getItems,
    removeItems,
    hasItem,
    getAllKeys,
    getLength
}
