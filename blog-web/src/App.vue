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
import { useThemeStore } from '@/stores/theme'

const themeStore = useThemeStore()

// 动态主题配置，根据当前主题切换
const themeOverrides = computed(() => {
  const isDark = themeStore.isDark
  
  return {
    common: {
      primaryColor: '#2ADB5C',
      primaryColorHover: '#4AE577',
      primaryColorPressed: '#1FC049',
      primaryColorSuppl: '#2ADB5C',
      // 根据主题动态切换配色
      bodyColor: isDark ? '#090A0B' : '#FFFFFF',
      cardColor: isDark ? '#141517' : '#FFFFFF',
      modalColor: isDark ? '#141517' : '#FFFFFF',
      popoverColor: isDark ? '#1F1F1F' : '#FFFFFF',
      tableColor: isDark ? '#141517' : '#FFFFFF',
      inputColor: isDark ? '#1F1F1F' : '#FFFFFF',
      codeColor: isDark ? '#1F1F1F' : '#F5F5F5',
      textColor1: isDark ? 'rgba(255, 255, 255, 0.9)' : 'rgba(0, 0, 0, 0.88)',
      textColor2: isDark ? 'rgba(255, 255, 255, 0.82)' : 'rgba(0, 0, 0, 0.65)',
      textColor3: isDark ? 'rgba(255, 255, 255, 0.52)' : 'rgba(0, 0, 0, 0.45)',
      borderColor: isDark ? 'rgba(255, 255, 255, 0.12)' : 'rgba(0, 0, 0, 0.12)',
      dividerColor: isDark ? 'rgba(255, 255, 255, 0.09)' : 'rgba(0, 0, 0, 0.06)',
      placeholderColor: isDark ? 'rgba(255, 255, 255, 0.38)' : 'rgba(0, 0, 0, 0.38)',
      iconColor: isDark ? 'rgba(255, 255, 255, 0.82)' : 'rgba(0, 0, 0, 0.65)',
      hoverColor: isDark ? 'rgba(255, 255, 255, 0.09)' : 'rgba(0, 0, 0, 0.06)',
      pressedColor: isDark ? 'rgba(255, 255, 255, 0.05)' : 'rgba(0, 0, 0, 0.03)'
    },
    Card: {
      borderRadius: '12px',
      color: isDark ? '#141517' : '#FFFFFF',
      colorEmbedded: isDark ? '#0D0E0F' : '#FAFAFA',
      borderColor: isDark ? 'rgba(255, 255, 255, 0.12)' : 'rgba(0, 0, 0, 0.12)'
    },
    Button: {
      borderRadiusMedium: '8px',
      textColorText: isDark ? 'rgba(255, 255, 255, 0.82)' : 'rgba(0, 0, 0, 0.65)',
      textColorTextHover: '#2ADB5C',
      textColorTextPressed: '#1FC049'
    },
    Input: {
      borderRadius: '8px',
      color: isDark ? '#1F1F1F' : '#FFFFFF',
      colorFocus: isDark ? '#1F1F1F' : '#FFFFFF',
      border: isDark ? '1px solid rgba(255, 255, 255, 0.12)' : '1px solid rgba(0, 0, 0, 0.12)',
      borderHover: '1px solid #2ADB5C',
      borderFocus: '1px solid #2ADB5C'
    },
    Tag: {
      borderRadius: '6px'
    },
    Drawer: {
      color: isDark ? '#141517' : '#FFFFFF',
      headerBorderBottom: isDark ? '1px solid rgba(255, 255, 255, 0.09)' : '1px solid rgba(0, 0, 0, 0.06)'
    }
  }
})
</script>
