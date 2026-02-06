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
import { computed, unref } from 'vue'
import { NConfigProvider, NMessageProvider, NDialogProvider, NNotificationProvider } from 'naive-ui'
import { useThemeStore } from '@/stores'
import { createNaiveTheme } from '@/config/theme'

const themeStore = useThemeStore()

// 动态主题配置，根据当前主题切换
const themeOverrides = computed(() => createNaiveTheme(unref(themeStore.isDark)))
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
