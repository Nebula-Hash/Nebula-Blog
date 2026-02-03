/**
 * 表单验证规则（认证相关）
 * 提供统一的、可复用的验证规则
 */
import { VALIDATION_CONFIG } from '@/config/constants'

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

// ==================== 用户相关验证规则 ====================

/**
 * 用户名验证规则
 */
export const usernameRules = [
    required('请输入用户名'),
    length(
        VALIDATION_CONFIG.USERNAME_MIN_LENGTH,
        VALIDATION_CONFIG.USERNAME_MAX_LENGTH,
        `用户名长度为${VALIDATION_CONFIG.USERNAME_MIN_LENGTH}-${VALIDATION_CONFIG.USERNAME_MAX_LENGTH}个字符`
    ),
    pattern(/^[a-zA-Z0-9_]+$/, '用户名只能包含字母、数字和下划线')
]

/**
 * 密码验证规则（注册时必填）
 */
export const passwordRules = [
    required('请输入密码'),
    length(
        VALIDATION_CONFIG.PASSWORD_MIN_LENGTH,
        VALIDATION_CONFIG.PASSWORD_MAX_LENGTH,
        `密码长度为${VALIDATION_CONFIG.PASSWORD_MIN_LENGTH}-${VALIDATION_CONFIG.PASSWORD_MAX_LENGTH}个字符`
    )
]

/**
 * 确认密码验证规则
 * @param {Object} formData - 表单数据对象
 * @returns {Array} 验证规则数组
 */
export const confirmPasswordRules = (formData) => [
    required('请再次输入密码'),
    custom((rule, value) => {
        if (value !== formData.password) {
            return new Error('两次密码不一致')
        }
        return true
    })
]

/**
 * 昵称验证规则
 */
export const nicknameRules = [
    {
        max: VALIDATION_CONFIG.NICKNAME_MAX_LENGTH,
        message: `昵称长度不能超过${VALIDATION_CONFIG.NICKNAME_MAX_LENGTH}个字符`,
        trigger: 'blur'
    }
]

/**
 * 邮箱验证规则
 */
export const emailRules = [
    {
        type: 'email',
        message: '请输入正确的邮箱格式',
        trigger: 'blur'
    }
]

export default {
    required,
    length,
    pattern,
    custom,
    usernameRules,
    passwordRules,
    confirmPasswordRules,
    nicknameRules,
    emailRules
}
