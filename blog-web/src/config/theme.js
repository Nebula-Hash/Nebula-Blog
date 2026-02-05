/**
 * 主题配置
 * 统一管理 Naive UI 主题和 CSS 变量
 */

// 品牌色配置
export const BRAND_COLORS = {
    primary: '#002FA7',
    primaryHover: '#1A49B5',
    primaryPressed: '#002577',
    primarySuppl: '#2A5F8F',
    primaryLight: '#3d7eae',
    primaryDark: '#2a5f8f'
}

// 功能色配置
export const FUNCTIONAL_COLORS = {
    success: '#52C41A',
    warning: '#FAAD14',
    error: '#F5222D',
    info: '#1890FF'
}

// 深色主题配置
export const DARK_THEME_COLORS = {
    bodyColor: '#0A0B0D',
    cardColor: '#12141A',
    modalColor: '#12141A',
    popoverColor: '#1A1D26',
    tableColor: '#12141A',
    inputColor: '#1A1D26',
    codeColor: '#1A1D26',

    textColor1: 'rgba(255, 255, 255, 0.92)',
    textColor2: 'rgba(255, 255, 255, 0.75)',
    textColor3: 'rgba(255, 255, 255, 0.55)',

    borderColor: 'rgba(255, 255, 255, 0.12)',
    dividerColor: 'rgba(255, 255, 255, 0.09)',
    placeholderColor: 'rgba(255, 255, 255, 0.35)',
    iconColor: 'rgba(255, 255, 255, 0.75)',

    hoverColor: 'rgba(255, 255, 255, 0.06)',
    pressedColor: 'rgba(255, 255, 255, 0.08)',

    boxShadow1: '0 2px 8px rgba(0, 0, 0, 0.3)',
    boxShadow2: '0 4px 16px rgba(0, 0, 0, 0.4)',
    boxShadow3: '0 8px 24px rgba(0, 0, 0, 0.5)'
}

// 浅色主题配置
export const LIGHT_THEME_COLORS = {
    bodyColor: '#FFFFFF',
    cardColor: '#FFFFFF',
    modalColor: '#FFFFFF',
    popoverColor: '#FFFFFF',
    tableColor: '#FFFFFF',
    inputColor: '#FFFFFF',
    codeColor: '#F8F9FA',

    textColor1: 'rgba(0, 0, 0, 0.88)',
    textColor2: 'rgba(0, 0, 0, 0.65)',
    textColor3: 'rgba(0, 0, 0, 0.45)',

    borderColor: 'rgba(0, 0, 0, 0.12)',
    dividerColor: 'rgba(0, 0, 0, 0.09)',
    placeholderColor: 'rgba(0, 0, 0, 0.35)',
    iconColor: 'rgba(0, 0, 0, 0.65)',

    hoverColor: 'rgba(0, 0, 0, 0.04)',
    pressedColor: 'rgba(0, 0, 0, 0.06)',

    boxShadow1: '0 2px 8px rgba(0, 0, 0, 0.08)',
    boxShadow2: '0 4px 16px rgba(0, 0, 0, 0.12)',
    boxShadow3: '0 8px 24px rgba(0, 0, 0, 0.16)'
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
    const themeColors = isDark ? DARK_THEME_COLORS : LIGHT_THEME_COLORS

    return {
        common: {
            ...BRAND_COLORS,
            ...FUNCTIONAL_COLORS,
            ...themeColors,
            ...FONT_CONFIG,
            borderRadius: BORDER_RADIUS.medium,
            borderRadiusSmall: BORDER_RADIUS.small
        },

        Card: {
            borderRadius: BORDER_RADIUS.large,
            paddingMedium: '24px',
            color: themeColors.cardColor,
            colorEmbedded: isDark ? '#0E1015' : '#F8F9FA',
            borderColor: isDark ? 'rgba(255, 255, 255, 0.08)' : 'rgba(0, 0, 0, 0.08)',
            boxShadow: isDark ? '0 2px 8px rgba(0, 0, 0, 0.3)' : '0 2px 8px rgba(0, 0, 0, 0.06)'
        },

        Button: {
            borderRadiusMedium: BORDER_RADIUS.medium,
            borderRadiusLarge: '10px',
            paddingMedium: '0 20px',
            heightMedium: '40px',
            fontSizeMedium: '16px',
            fontWeightStrong: '500',
            textColorText: themeColors.textColor2,
            textColorTextHover: '#2ADB5C',
            textColorTextPressed: '#1FC049',
            textColorGhost: themeColors.textColor2,
            textColorGhostHover: '#2ADB5C',
            textColorGhostPressed: '#1FC049'
        },

        Input: {
            borderRadius: BORDER_RADIUS.medium,
            heightMedium: '40px',
            paddingMedium: '0 16px',
            color: themeColors.inputColor,
            colorFocus: themeColors.inputColor,
            border: `1px solid ${themeColors.borderColor}`,
            borderHover: '1px solid #2ADB5C',
            borderFocus: '1px solid #2ADB5C',
            boxShadowFocus: '0 0 0 3px rgba(42, 219, 92, 0.1)'
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
