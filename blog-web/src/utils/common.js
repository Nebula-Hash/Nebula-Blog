/**
 * 公共工具函数
 */
import { DEFAULTS } from '@/config/constants'
import { showSuccess, showError, showInfo, showWarning } from '@/utils/uiMessage'

/**
 * 格式化日期时间
 * @param {string|Date} dateTime - 日期时间
 * @param {string} format - 格式类型 'datetime' | 'date' | 'time'
 * @returns {string} 格式化后的日期时间字符串
 */
export const formatDateTime = (dateTime, format = 'datetime') => {
    if (!dateTime) return DEFAULTS.EMPTY_TEXT

    const date = new Date(dateTime)

    if (isNaN(date.getTime())) return DEFAULTS.EMPTY_TEXT

    const options = {
        datetime: {
            year: 'numeric',
            month: '2-digit',
            day: '2-digit',
            hour: '2-digit',
            minute: '2-digit',
            second: '2-digit',
            hour12: false
        },
        date: {
            year: 'numeric',
            month: '2-digit',
            day: '2-digit'
        },
        time: {
            hour: '2-digit',
            minute: '2-digit',
            second: '2-digit',
            hour12: false
        }
    }

    return date.toLocaleString('zh-CN', options[format] || options.datetime)
}

export { showSuccess, showError, showInfo, showWarning }
