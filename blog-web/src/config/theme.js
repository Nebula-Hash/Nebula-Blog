/**
 * 主题配置
 * 统一管理 Naive UI 主题和 CSS 变量
 */

const getCssVar = (name, fallback) => {
    if (typeof window === 'undefined') return fallback
    const value = getComputedStyle(document.documentElement).getPropertyValue(name).trim()
    return value || fallback
}

const readCssThemeTokens = (isDark) => {
    const primary = getCssVar('--color-primary', '#2563eb')
    const primaryLight = getCssVar('--color-primary-light', '#60a5fa')
    const primaryDark = getCssVar('--color-primary-dark', '#1d4ed8')
    const primaryAlpha20 = getCssVar('--color-primary-alpha-20', 'rgba(37, 99, 235, 0.2)')

    const bgPrimary = getCssVar('--bg-primary', isDark ? '#070a12' : '#f7f9fc')
    const bgSecondary = getCssVar('--bg-secondary', isDark ? '#0b1020' : '#eef2f7')
    const surfacePrimary = getCssVar('--surface-primary', isDark ? '#0d162b' : '#ffffff')
    const surfaceSecondary = getCssVar('--surface-secondary', isDark ? '#101e3a' : '#f4f7fb')
    const surfaceHover = getCssVar('--surface-hover', isDark ? 'rgba(0, 229, 255, 0.06)' : 'rgba(37, 99, 235, 0.06)')
    const surfaceActive = getCssVar('--surface-active', isDark ? 'rgba(0, 229, 255, 0.1)' : 'rgba(37, 99, 235, 0.1)')

    const textPrimary = getCssVar('--text-primary', isDark ? 'rgba(233, 244, 255, 0.94)' : 'rgba(15, 23, 42, 0.92)')
    const textSecondary = getCssVar('--text-secondary', isDark ? 'rgba(233, 244, 255, 0.76)' : 'rgba(15, 23, 42, 0.68)')
    const textTertiary = getCssVar('--text-tertiary', isDark ? 'rgba(233, 244, 255, 0.56)' : 'rgba(15, 23, 42, 0.48)')

    const borderPrimary = getCssVar('--border-primary', isDark ? 'rgba(120, 200, 255, 0.18)' : 'rgba(15, 23, 42, 0.16)')
    const borderSecondary = getCssVar('--border-secondary', isDark ? 'rgba(120, 200, 255, 0.12)' : 'rgba(15, 23, 42, 0.1)')
    const dividerPrimary = getCssVar('--divider-primary', isDark ? 'rgba(120, 200, 255, 0.12)' : 'rgba(15, 23, 42, 0.12)')
    const placeholderColor = getCssVar('--placeholder-color', isDark ? 'rgba(233, 244, 255, 0.38)' : 'rgba(15, 23, 42, 0.4)')
    const iconPrimary = getCssVar('--icon-primary', isDark ? 'rgba(233, 244, 255, 0.78)' : 'rgba(15, 23, 42, 0.68)')

    const shadowPrimary = getCssVar('--shadow-primary', isDark ? '0 6px 18px rgba(0, 0, 0, 0.45)' : '0 2px 10px rgba(15, 23, 42, 0.08)')
    const shadowElevated = getCssVar('--shadow-elevated', isDark ? '0 14px 44px rgba(0, 0, 0, 0.55)' : '0 10px 30px rgba(15, 23, 42, 0.12)')

    const success = getCssVar('--color-success', '#52C41A')
    const warning = getCssVar('--color-warning', '#FAAD14')
    const error = getCssVar('--color-error', '#F5222D')
    const info = getCssVar('--color-info', '#1890FF')

    return {
        primary,
        primaryHover: primaryDark,
        primaryPressed: primaryDark,
        primarySuppl: primaryLight,
        primaryLight,
        primaryDark,
        primaryAlpha20,

        success,
        warning,
        error,
        info,

        bodyColor: bgPrimary,
        cardColor: surfacePrimary,
        modalColor: surfacePrimary,
        popoverColor: surfaceSecondary,
        tableColor: surfacePrimary,
        inputColor: surfaceSecondary,
        codeColor: bgSecondary,

        textColor1: textPrimary,
        textColor2: textSecondary,
        textColor3: textTertiary,

        borderColor: borderPrimary,
        dividerColor: dividerPrimary,
        placeholderColor,
        iconColor: iconPrimary,

        hoverColor: surfaceHover,
        pressedColor: surfaceActive,

        boxShadow1: shadowPrimary,
        boxShadow2: shadowElevated,
        boxShadow3: shadowElevated,

        borderSecondary,
        bgSecondary
    }
}

// 字体配置
export const FONT_CONFIG = {
    fontFamily: '-apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif',
    fontSize: '16px',
    fontSizeMini: '12px',
    fontSizeTiny: '14px',
    fontSizeSmall: '14px',
    fontSizeMedium: '16px',
    fontSizeLarge: '18px',
    fontSizeHuge: '20px'
}

// 圆角配置
export const BORDER_RADIUS = {
    small: '6px',
    medium: '8px',
    large: '12px'
}

/**
 * 生成 Naive UI 主题配置
 * @param {boolean} isDark - 是否为深色主题
 * @returns {object} Naive UI themeOverrides 配置
 */
export function createNaiveTheme(isDark) {
    const themeColors = readCssThemeTokens(isDark)

    return {
        common: {
            ...themeColors,
            ...FONT_CONFIG,
            borderRadius: BORDER_RADIUS.medium,
            borderRadiusSmall: BORDER_RADIUS.small
        },

        Card: {
            borderRadius: BORDER_RADIUS.large,
            paddingMedium: '24px',
            color: themeColors.cardColor,
            colorEmbedded: themeColors.codeColor,
            borderColor: themeColors.borderSecondary,
            boxShadow: themeColors.boxShadow1
        },

        Button: {
            borderRadiusMedium: BORDER_RADIUS.medium,
            borderRadiusLarge: '10px',
            paddingMedium: '0 20px',
            heightMedium: '40px',
            fontSizeMedium: '16px',
            fontWeightStrong: '500',
            textColorText: themeColors.textColor2,
            textColorTextHover: themeColors.primary,
            textColorTextPressed: themeColors.primaryPressed,
            textColorGhost: themeColors.textColor2,
            textColorGhostHover: themeColors.primary,
            textColorGhostPressed: themeColors.primaryPressed
        },

        Input: {
            borderRadius: BORDER_RADIUS.medium,
            heightMedium: '40px',
            paddingMedium: '0 16px',
            color: themeColors.inputColor,
            colorFocus: themeColors.inputColor,
            border: `1px solid ${themeColors.borderColor}`,
            borderHover: `1px solid ${themeColors.primary}`,
            borderFocus: `1px solid ${themeColors.primary}`,
            boxShadowFocus: `0 0 0 3px ${themeColors.primaryAlpha20}`
        },

        Tag: {
            borderRadius: BORDER_RADIUS.small,
            padding: '0 12px',
            heightMedium: '28px',
            fontSize: '14px'
        },

        Drawer: {
            color: themeColors.cardColor,
            headerBorderBottom: `1px solid ${themeColors.dividerColor}`,
            footerBorderTop: `1px solid ${themeColors.dividerColor}`
        },

        Dialog: {
            borderRadius: '16px',
            color: themeColors.cardColor
        },

        Message: {
            borderRadius: BORDER_RADIUS.medium,
            padding: '12px 16px'
        },

        Notification: {
            borderRadius: BORDER_RADIUS.large,
            padding: '16px 20px'
        },

        Dropdown: {
            borderRadius: BORDER_RADIUS.medium,
            padding: '8px',
            color: themeColors.popoverColor
        },

        Tooltip: {
            borderRadius: BORDER_RADIUS.small,
            padding: '8px 12px',
            color: 'rgba(0, 0, 0, 0.9)'
        }
    }
}
