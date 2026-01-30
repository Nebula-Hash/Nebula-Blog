/**
 * 表单验证规则
 * 统一管理所有表单验证逻辑，避免重复定义
 */

import { VALIDATION_CONFIG } from '@/config/constants'
import { isValidEmail as checkEmail } from './common'

/**
 * 创建必填验证规则
 * @param {string} message - 错误提示消息
 * @param {string} trigger - 触发方式，默认 'blur'
 * @returns {Object} 验证规则
 */
export const required = (message, trigger = 'blur') => ({
    required: true,
    message,
    trigger
})

/**
 * 创建长度验证规则
 * @param {number} min - 最小长度
 * @param {number} max - 最大长度
 * @param {string} message - 错误提示消息
 * @param {string} trigger - 触发方式，默认 'blur'
 * @returns {Object} 验证规则
 */
export const length = (min, max, message, trigger = 'blur') => ({
    min,
    max,
    message,
    trigger
})

/**
 * 创建最小长度验证规则
 * @param {number} min - 最小长度
 * @param {string} message - 错误提示消息
 * @param {string} trigger - 触发方式，默认 'blur'
 * @returns {Object} 验证规则
 */
export const minLength = (min, message, trigger = 'blur') => ({
    min,
    message,
    trigger
})

/**
 * 创建最大长度验证规则
 * @param {number} max - 最大长度
 * @param {string} message - 错误提示消息
 * @param {string} trigger - 触发方式，默认 'blur'
 * @returns {Object} 验证规则
 */
export const maxLength = (max, message, trigger = 'blur') => ({
    max,
    message,
    trigger
})

/**
 * 创建自定义验证规则
 * @param {Function} validator - 验证函数
 * @param {string} trigger - 触发方式，默认 'blur'
 * @returns {Object} 验证规则
 */
export const custom = (validator, trigger = 'blur') => ({
    validator,
    trigger
})

// ==================== 用户名验证规则 ====================
export const usernameRules = [
    required('请输入用户名'),
    length(
        VALIDATION_CONFIG.USERNAME_MIN_LENGTH,
        VALIDATION_CONFIG.USERNAME_MAX_LENGTH,
        `用户名长度需在${VALIDATION_CONFIG.USERNAME_MIN_LENGTH}-${VALIDATION_CONFIG.USERNAME_MAX_LENGTH}个字符之间`
    )
]

// ==================== 密码验证规则 ====================
/**
 * 密码验证规则（新增时）
 */
export const passwordRules = [
    required('请输入密码'),
    length(
        VALIDATION_CONFIG.PASSWORD_MIN_LENGTH,
        VALIDATION_CONFIG.PASSWORD_MAX_LENGTH,
        `密码长度需在${VALIDATION_CONFIG.PASSWORD_MIN_LENGTH}-${VALIDATION_CONFIG.PASSWORD_MAX_LENGTH}个字符之间`
    )
]

/**
 * 密码验证规则（编辑时，可选）
 * @param {Object} formData - 表单数据对象
 * @returns {Array} 验证规则数组
 */
export const passwordRulesOptional = (formData) => [
    custom((rule, value) => {
        // 新增时密码必填
        if (!formData.id && !value) {
            return new Error('请输入密码')
        }
        // 如果有输入密码，验证长度
        if (value && (value.length < VALIDATION_CONFIG.PASSWORD_MIN_LENGTH || value.length > VALIDATION_CONFIG.PASSWORD_MAX_LENGTH)) {
            return new Error(`密码长度需在${VALIDATION_CONFIG.PASSWORD_MIN_LENGTH}-${VALIDATION_CONFIG.PASSWORD_MAX_LENGTH}个字符之间`)
        }
        return true
    })
]

// ==================== 昵称验证规则 ====================
export const nicknameRules = [
    maxLength(VALIDATION_CONFIG.NICKNAME_MAX_LENGTH, `昵称长度不能超过${VALIDATION_CONFIG.NICKNAME_MAX_LENGTH}个字符`)
]

// ==================== 邮箱验证规则 ====================
/**
 * 邮箱验证规则（选填）
 */
export const emailRules = [
    custom((rule, value) => {
        // 邮箱为选填，但如果填写了则需要验证格式
        if (value && !checkEmail(value, false)) {
            return new Error('邮箱格式不正确')
        }
        return true
    })
]

/**
 * 邮箱验证规则（必填）
 */
export const emailRequiredRules = [
    required('请输入邮箱'),
    custom((rule, value) => {
        if (!checkEmail(value, true)) {
            return new Error('邮箱格式不正确')
        }
        return true
    })
]

// ==================== 个人简介验证规则 ====================
export const introRules = [
    maxLength(VALIDATION_CONFIG.INTRO_MAX_LENGTH, `个人简介不能超过${VALIDATION_CONFIG.INTRO_MAX_LENGTH}个字符`)
]

// ==================== 文章标题验证规则 ====================
export const articleTitleRules = [
    required('请输入文章标题'),
    maxLength(100, '文章标题不能超过100个字符')
]

// ==================== 文章内容验证规则 ====================
export const articleContentRules = [
    required('请输入文章内容')
]

// ==================== 分类名称验证规则 ====================
export const categoryNameRules = [
    required('请输入分类名称'),
    length(2, 20, '分类名称长度需在2-20个字符之间')
]

// ==================== 标签名称验证规则 ====================
export const tagNameRules = [
    required('请输入标签名称'),
    length(2, 20, '标签名称长度需在2-20个字符之间')
]

// ==================== 轮播图标题验证规则 ====================
export const bannerTitleRules = [
    required('请输入轮播图标题'),
    maxLength(50, '轮播图标题不能超过50个字符')
]

// ==================== URL验证规则 ====================
export const urlRules = [
    required('请输入URL地址'),
    custom((rule, value) => {
        try {
            new URL(value)
            return true
        } catch {
            return new Error('URL格式不正确')
        }
    })
]

/**
 * URL验证规则（选填）
 */
export const urlRulesOptional = [
    custom((rule, value) => {
        if (!value) return true
        try {
            new URL(value)
            return true
        } catch {
            return new Error('URL格式不正确')
        }
    })
]

// ==================== 排序号验证规则 ====================
export const sortRules = [
    custom((rule, value) => {
        if (value === null || value === undefined || value === '') {
            return true // 允许为空
        }
        if (!/^\d+$/.test(value)) {
            return new Error('排序号必须为正整数')
        }
        return true
    })
]

// ==================== 导出所有验证规则 ====================
export default {
    required,
    length,
    minLength,
    maxLength,
    custom,
    usernameRules,
    passwordRules,
    passwordRulesOptional,
    nicknameRules,
    emailRules,
    emailRequiredRules,
    introRules,
    articleTitleRules,
    articleContentRules,
    categoryNameRules,
    tagNameRules,
    bannerTitleRules,
    urlRules,
    urlRulesOptional,
    sortRules
}
