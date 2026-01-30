/**
 * 安全工具类
 * 提供XSS防护、密码强度验证等安全功能
 */

/**
 * HTML转义 - 防止XSS攻击
 * @param {string} text - 需要转义的文本
 * @returns {string} 转义后的文本
 */
export function escapeHtml(text) {
    if (typeof text !== 'string') {
        return text
    }

    const map = {
        '&': '&amp;',
        '<': '&lt;',
        '>': '&gt;',
        '"': '&quot;',
        "'": '&#039;',
        '/': '&#x2F;'
    }

    return text.replace(/[&<>"'/]/g, (char) => map[char])
}

/**
 * 验证密码强度
 * @param {string} password - 密码
 * @returns {Object} {strength: 'weak'|'medium'|'strong', score: 0-100, suggestions: string[]}
 */
export function checkPasswordStrength(password) {
    if (typeof password !== 'string') {
        return { strength: 'weak', score: 0, suggestions: ['密码不能为空'] }
    }

    let score = 0
    const suggestions = []

    // 长度检查
    if (password.length < 6) {
        suggestions.push('密码长度至少6个字符')
    } else if (password.length < 8) {
        score += 20
        suggestions.push('建议密码长度至少8个字符')
    } else if (password.length < 12) {
        score += 30
    } else {
        score += 40
    }

    // 包含小写字母
    if (/[a-z]/.test(password)) {
        score += 15
    } else {
        suggestions.push('建议包含小写字母')
    }

    // 包含大写字母
    if (/[A-Z]/.test(password)) {
        score += 15
    } else {
        suggestions.push('建议包含大写字母')
    }

    // 包含数字
    if (/\d/.test(password)) {
        score += 15
    } else {
        suggestions.push('建议包含数字')
    }

    // 包含特殊字符
    if (/[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]/.test(password)) {
        score += 15
    } else {
        suggestions.push('建议包含特殊字符')
    }

    // 确定强度等级
    let strength = 'weak'
    if (score >= 80) {
        strength = 'strong'
    } else if (score >= 50) {
        strength = 'medium'
    }

    return { strength, score, suggestions }
}

export default {
    escapeHtml,
    checkPasswordStrength
}
