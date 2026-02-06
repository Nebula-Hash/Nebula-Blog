import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { darkTheme } from 'naive-ui'

export const useThemeStore = defineStore('theme', () => {
  // 主题模式：'light' | 'dark' | 'auto'
  const themeMode = ref('dark') // 默认深色主题
  const systemPrefersDark = ref(false)

  // 计算实际是否为深色主题
  const isDark = computed(() => {
    if (themeMode.value === 'auto') {
      return systemPrefersDark.value
    }
    return themeMode.value === 'dark'
  })

  // Naive UI 主题配置
  const theme = computed(() => isDark.value ? darkTheme : null)

  // 切换主题（循环：light -> dark -> auto）
  const toggleTheme = () => {
    const modes = ['light', 'dark', 'auto']
    const currentIndex = modes.indexOf(themeMode.value)
    const nextIndex = (currentIndex + 1) % modes.length
    themeMode.value = modes[nextIndex]
  }

  // 设置深色主题
  const setDarkTheme = () => {
    themeMode.value = 'dark'
  }

  // 设置浅色主题
  const setLightTheme = () => {
    themeMode.value = 'light'
  }

  // 设置自动主题
  const setAutoTheme = () => {
    themeMode.value = 'auto'
  }

  // 设置指定主题模式
  const setThemeMode = (mode) => {
    if (['light', 'dark', 'auto'].includes(mode)) {
      themeMode.value = mode
    }
  }

  // 监听主题变化

  // 获取主题图标
  const getThemeIcon = computed(() => {
    const icons = {
      light: '☀️',
      dark: '🌙',
      auto: '🌓'
    }
    return icons[themeMode.value] || icons.dark
  })

  // 获取主题标签
  const getThemeLabel = computed(() => {
    const labels = {
      light: '浅色',
      dark: '深色',
      auto: '跟随系统'
    }
    return labels[themeMode.value] || labels.dark
  })

  return {
    // 状态
    isDark,
    theme,
    themeMode,
    systemPrefersDark,

    // 计算属性
    getThemeIcon,
    getThemeLabel,

    // 方法
    toggleTheme,
    setDarkTheme,
    setLightTheme,
    setAutoTheme,
    setThemeMode
  }
}, {
  // 使用 Pinia 持久化插件
  persist: {
    key: 'nebula-theme',
    paths: ['themeMode'],
    storage: localStorage
  }
})
