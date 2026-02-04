<template>
  <n-config-provider :theme="themeStore.theme" :theme-overrides="themeOverrides">
    <n-message-provider>
      <n-dialog-provider>
        <n-notification-provider>
          <router-view />
        </n-notification-provider>
      </n-dialog-provider>
    </n-message-provider>
  </n-config-provider>
</template>

<script setup>
import { computed } from 'vue'
import { NConfigProvider, NMessageProvider, NDialogProvider, NNotificationProvider } from 'naive-ui'
import { useThemeStore } from '@/stores'

const themeStore = useThemeStore()

// 动态主题配置，根据当前主题切换
const themeOverrides = computed(() => {
  const isDark = themeStore.isDark

  return {
    common: {
      // 品牌色 - 克莱因蓝
      primaryColor: '#002FA7',
      primaryColorHover: '#1A49B5',
      primaryColorPressed: '#002577',
      primaryColorSuppl: '#2A5F8F',

      // 功能色
      successColor: '#52C41A',
      warningColor: '#FAAD14',
      errorColor: '#F5222D',
      infoColor: '#1890FF',

      // 背景色
      bodyColor: isDark ? '#0A0B0D' : '#FFFFFF',
      cardColor: isDark ? '#12141A' : '#FFFFFF',
      modalColor: isDark ? '#12141A' : '#FFFFFF',
      popoverColor: isDark ? '#1A1D26' : '#FFFFFF',
      tableColor: isDark ? '#12141A' : '#FFFFFF',
      inputColor: isDark ? '#1A1D26' : '#FFFFFF',
      codeColor: isDark ? '#1A1D26' : '#F8F9FA',

      // 文字色
      textColor1: isDark ? 'rgba(255, 255, 255, 0.92)' : 'rgba(0, 0, 0, 0.88)',
      textColor2: isDark ? 'rgba(255, 255, 255, 0.75)' : 'rgba(0, 0, 0, 0.65)',
      textColor3: isDark ? 'rgba(255, 255, 255, 0.55)' : 'rgba(0, 0, 0, 0.45)',

      // 边框色
      borderColor: isDark ? 'rgba(255, 255, 255, 0.12)' : 'rgba(0, 0, 0, 0.12)',
      dividerColor: isDark ? 'rgba(255, 255, 255, 0.09)' : 'rgba(0, 0, 0, 0.09)',

      // 占位符
      placeholderColor: isDark ? 'rgba(255, 255, 255, 0.35)' : 'rgba(0, 0, 0, 0.35)',

      // 图标色
      iconColor: isDark ? 'rgba(255, 255, 255, 0.75)' : 'rgba(0, 0, 0, 0.65)',

      // 交互色
      hoverColor: isDark ? 'rgba(255, 255, 255, 0.06)' : 'rgba(0, 0, 0, 0.04)',
      pressedColor: isDark ? 'rgba(255, 255, 255, 0.08)' : 'rgba(0, 0, 0, 0.06)',

      // 圆角
      borderRadius: '8px',
      borderRadiusSmall: '6px',

      // 字体
      fontFamily: '-apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif',
      fontSize: '16px',
      fontSizeMini: '12px',
      fontSizeTiny: '14px',
      fontSizeSmall: '14px',
      fontSizeMedium: '16px',
      fontSizeLarge: '18px',
      fontSizeHuge: '20px',

      // 阴影
      boxShadow1: isDark
        ? '0 2px 8px rgba(0, 0, 0, 0.3)'
        : '0 2px 8px rgba(0, 0, 0, 0.08)',
      boxShadow2: isDark
        ? '0 4px 16px rgba(0, 0, 0, 0.4)'
        : '0 4px 16px rgba(0, 0, 0, 0.12)',
      boxShadow3: isDark
        ? '0 8px 24px rgba(0, 0, 0, 0.5)'
        : '0 8px 24px rgba(0, 0, 0, 0.16)'
    },

    // 卡片组件
    Card: {
      borderRadius: '12px',
      paddingMedium: '24px',
      color: isDark ? '#12141A' : '#FFFFFF',
      colorEmbedded: isDark ? '#0E1015' : '#F8F9FA',
      borderColor: isDark ? 'rgba(255, 255, 255, 0.08)' : 'rgba(0, 0, 0, 0.08)',
      boxShadow: isDark
        ? '0 2px 8px rgba(0, 0, 0, 0.3)'
        : '0 2px 8px rgba(0, 0, 0, 0.06)'
    },

    // 按钮组件
    Button: {
      borderRadiusMedium: '8px',
      borderRadiusLarge: '10px',
      paddingMedium: '0 20px',
      heightMedium: '40px',
      fontSizeMedium: '16px',
      fontWeightStrong: '500',
      textColorText: isDark ? 'rgba(255, 255, 255, 0.75)' : 'rgba(0, 0, 0, 0.65)',
      textColorTextHover: '#2ADB5C',
      textColorTextPressed: '#1FC049',
      textColorGhost: isDark ? 'rgba(255, 255, 255, 0.75)' : 'rgba(0, 0, 0, 0.65)',
      textColorGhostHover: '#2ADB5C',
      textColorGhostPressed: '#1FC049'
    },

    // 输入框组件
    Input: {
      borderRadius: '8px',
      heightMedium: '40px',
      paddingMedium: '0 16px',
      color: isDark ? '#1A1D26' : '#FFFFFF',
      colorFocus: isDark ? '#1A1D26' : '#FFFFFF',
      border: isDark ? '1px solid rgba(255, 255, 255, 0.12)' : '1px solid rgba(0, 0, 0, 0.12)',
      borderHover: '1px solid #2ADB5C',
      borderFocus: '1px solid #2ADB5C',
      boxShadowFocus: '0 0 0 3px rgba(42, 219, 92, 0.1)'
    },

    // 标签组件
    Tag: {
      borderRadius: '6px',
      padding: '0 12px',
      heightMedium: '28px',
      fontSize: '14px'
    },

    // 抽屉组件
    Drawer: {
      color: isDark ? '#12141A' : '#FFFFFF',
      headerBorderBottom: isDark ? '1px solid rgba(255, 255, 255, 0.09)' : '1px solid rgba(0, 0, 0, 0.06)',
      footerBorderTop: isDark ? '1px solid rgba(255, 255, 255, 0.09)' : '1px solid rgba(0, 0, 0, 0.06)'
    },

    // 对话框组件
    Dialog: {
      borderRadius: '16px',
      color: isDark ? '#12141A' : '#FFFFFF'
    },

    // 消息组件
    Message: {
      borderRadius: '8px',
      padding: '12px 16px'
    },

    // 通知组件
    Notification: {
      borderRadius: '12px',
      padding: '16px 20px'
    },

    // 下拉菜单
    Dropdown: {
      borderRadius: '8px',
      padding: '8px',
      color: isDark ? '#1A1D26' : '#FFFFFF'
    },

    // 工具提示
    Tooltip: {
      borderRadius: '6px',
      padding: '8px 12px',
      color: isDark ? 'rgba(0, 0, 0, 0.9)' : 'rgba(0, 0, 0, 0.9)'
    }
  }
})
</script>

<style>
/* 主题过渡动画 */
:root {
  transition: var(--theme-transition, none);
}

/* 全局样式增强 */
body {
  overflow-x: hidden;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}
</style>
