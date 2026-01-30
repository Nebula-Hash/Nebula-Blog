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
import { NConfigProvider, NMessageProvider, NDialogProvider, NNotificationProvider } from 'naive-ui'
import { useThemeStore } from '@/stores/theme'

const themeStore = useThemeStore()

const themeOverrides = {
  common: {
    primaryColor: '#2ADB5C',
    primaryColorHover: '#4AE577',
    primaryColorPressed: '#1FC049',
    primaryColorSuppl: '#2ADB5C',
    // 深色主题配色
    bodyColor: '#090A0B',
    cardColor: '#141517',
    modalColor: '#141517',
    popoverColor: '#1F1F1F',
    tableColor: '#141517',
    inputColor: '#1F1F1F',
    codeColor: '#1F1F1F',
    textColor1: 'rgba(255, 255, 255, 0.9)',
    textColor2: 'rgba(255, 255, 255, 0.82)',
    textColor3: 'rgba(255, 255, 255, 0.52)',
    borderColor: 'rgba(255, 255, 255, 0.12)',
    dividerColor: 'rgba(255, 255, 255, 0.09)'
  },
  Card: {
    borderRadius: '12px',
    color: '#141517',
    colorEmbedded: '#0D0E0F'
  },
  Button: {
    borderRadiusMedium: '8px'
  },
  Input: {
    borderRadius: '8px'
  }
}
</script>
