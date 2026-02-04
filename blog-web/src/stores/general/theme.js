import { defineStore } from 'pinia'
import { ref, watch } from 'vue'
import { darkTheme } from 'naive-ui'

export const useThemeStore = defineStore('theme', () => {
  // 初始化主题：优先使用持久化的值，其次使用系统偏好
  const getInitialTheme = () => {
    // 检测系统主题偏好
    if (window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches) {
      return true
    }
    return true // 默认深色主题
  }

  const isDark = ref(getInitialTheme())
  const theme = ref(isDark.value ? darkTheme : null)

  // 应用主题到DOM
  const applyTheme = (dark) => {
    const root = document.documentElement
    if (dark) {
      root.classList.add('dark-theme')
      root.classList.remove('light-theme')
    } else {
      root.classList.add('light-theme')
      root.classList.remove('dark-theme')
    }
  }

  // 切换主题
  const toggleTheme = () => {
    isDark.value = !isDark.value
    theme.value = isDark.value ? darkTheme : null
    applyTheme(isDark.value)
  }

  // 设置深色主题
  const setDarkTheme = () => {
    isDark.value = true
    theme.value = darkTheme
    applyTheme(true)
  }

  // 设置浅色主题
  const setLightTheme = () => {
    isDark.value = false
    theme.value = null
    applyTheme(false)
  }

  // 监听系统主题变化
  const setupSystemThemeListener = () => {
    if (window.matchMedia) {
      const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)')
      mediaQuery.addEventListener('change', (e) => {
        // 可以选择是否跟随系统主题
        // 这里保持用户选择的主题不变
      })
    }
  }

  // 初始化时应用主题
  applyTheme(isDark.value)
  setupSystemThemeListener()

  // 监听主题变化，同步到其他标签页
  watch(isDark, (newValue) => {
    window.dispatchEvent(new CustomEvent('theme-change', { detail: { isDark: newValue } }))
  })

  return {
    isDark,
    theme,
    toggleTheme,
    setDarkTheme,
    setLightTheme
  }
}, {
  // 使用 Pinia 持久化插件
  persist: {
    key: 'theme-store',
    paths: ['isDark']
  }
})
