/**
 * 表单验证规则
 * 提供统一的、可复用的验证规则
 */

// 评论验证配置
const VALIDATION_CONFIG = {
    COMMENT_MIN_LENGTH: 1,
    COMMENT_MAX_LENGTH: 500
}

// ==================== 基础规则构建器 ====================

/**
 * 必填规则
 * @param {string} message - 错误提示消息
 * @returns {Object} 验证规则
 */
export const required = (message) => ({
    required: true,
    message,
    trigger: 'blur'
})

/**
 * 长度范围规则
 * @param {number} min - 最小长度
 * @param {number} max - 最大长度
 * @param {string} message - 错误提示消息
 * @returns {Object} 验证规则
 */
export const length = (min, max, message) => ({
    min,
    max,
    message,
    trigger: 'blur'
})

/**
 * 最小长度规则
 * @param {number} min - 最小长度
 * @param {string} message - 错误提示消息
 * @returns {Object} 验证规则
 */
export const minLength = (min, message) => ({
    min,
    message,
    trigger: 'blur'
})

/**
 * 最大长度规则
 * @param {number} max - 最大长度
 * @param {string} message - 错误提示消息
 * @returns {Object} 验证规则
 */
export const maxLength = (max, message) => ({
    max,
    message,
    trigger: 'blur'
})

/**
 * 正则表达式规则
 * @param {RegExp} pattern - 正则表达式
 * @param {string} message - 错误提示消息
 * @returns {Object} 验证规则
 */
export const pattern = (regex, message) => ({
    pattern: regex,
    message,
    trigger: 'blur'
})

/**
 * 自定义验证规则
 * @param {Function} validator - 验证函数
 * @returns {Object} 验证规则
 */
export const custom = (validator) => ({
    validator,
    trigger: 'blur'
})

// ==================== 评论相关验证规则 ====================

/**
 * 评论内容验证规则
 */
export const commentRules = [
    required('请输入评论内容'),
    length(
        VALIDATION_CONFIG.COMMENT_MIN_LENGTH,
        VALIDATION_CONFIG.COMMENT_MAX_LENGTH,
        `评论长度为${VALIDATION_CONFIG.COMMENT_MIN_LENGTH}-${VALIDATION_CONFIG.COMMENT_MAX_LENGTH}个字符`
    )
]

export default {
    required,
    length,
    minLength,
    maxLength,
    pattern,
    custom,
    commentRules
}
