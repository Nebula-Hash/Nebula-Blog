import { defineStore } from 'pinia'
import { ref } from 'vue'
import { darkTheme } from 'naive-ui'

export const useThemeStore = defineStore('theme', () => {
  const isDark = ref(localStorage.getItem('theme') === 'dark' || true) // 默认深色主题
  const theme = ref(isDark.value ? darkTheme : null)

  const toggleTheme = () => {
    isDark.value = !isDark.value
    theme.value = isDark.value ? darkTheme : null
    localStorage.setItem('theme', isDark.value ? 'dark' : 'light')
  }

  const setDarkTheme = () => {
    isDark.value = true
    theme.value = darkTheme
    localStorage.setItem('theme', 'dark')
  }

  const setLightTheme = () => {
    isDark.value = false
    theme.value = null
    localStorage.setItem('theme', 'light')
  }

  return {
    isDark,
    theme,
    toggleTheme,
    setDarkTheme,
    setLightTheme
  }
})
