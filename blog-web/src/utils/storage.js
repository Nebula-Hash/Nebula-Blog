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

export default {
    getItem,
    setItem,
    removeItem,
    clear
}
