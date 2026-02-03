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

export default {
    escapeHtml,
}
