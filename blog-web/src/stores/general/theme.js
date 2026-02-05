import { defineStore } from 'pinia'
import { ref, watch, computed } from 'vue'
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

  // 检测系统主题偏好
  const detectSystemTheme = () => {
    if (window.matchMedia) {
      systemPrefersDark.value = window.matchMedia('(prefers-color-scheme: dark)').matches
    }
  }

  // 应用主题到DOM（带过渡动画）
  const applyTheme = (dark, withTransition = true) => {
    const root = document.documentElement

    // 添加过渡类
    if (withTransition) {
      root.style.setProperty('--theme-transition', 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)')
    }

    // 切换主题类（浅色主题使用 :root 默认 token，无需额外类名）
    if (dark) {
      root.classList.add('dark-theme')
    } else {
      root.classList.remove('dark-theme')
    }

    // 设置meta标签（移动端状态栏颜色）
    updateMetaThemeColor(dark)

    // 移除过渡类
    if (withTransition) {
      setTimeout(() => {
        root.style.removeProperty('--theme-transition')
      }, 300)
    }
  }

  // 更新meta主题颜色（移动端）
  const updateMetaThemeColor = (dark) => {
    let metaThemeColor = document.querySelector('meta[name="theme-color"]')
    if (!metaThemeColor) {
      metaThemeColor = document.createElement('meta')
      metaThemeColor.name = 'theme-color'
      document.head.appendChild(metaThemeColor)
    }
    const bgPrimary = getComputedStyle(document.documentElement)
      .getPropertyValue('--bg-primary')
      .trim()

    metaThemeColor.content = bgPrimary || (dark ? '#070A12' : '#F7F9FC')
  }

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
    detectSystemTheme()
  }

  // 设置指定主题模式
  const setThemeMode = (mode) => {
    if (['light', 'dark', 'auto'].includes(mode)) {
      themeMode.value = mode
      if (mode === 'auto') {
        detectSystemTheme()
      }
    }
  }

  // 监听系统主题变化
  const setupSystemThemeListener = () => {
    if (window.matchMedia) {
      const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)')

      // 初始检测
      detectSystemTheme()

      // 监听变化
      const handler = (e) => {
        systemPrefersDark.value = e.matches
        // 只在auto模式下响应系统主题变化
        if (themeMode.value === 'auto') {
          applyTheme(e.matches)
        }
      }

      // 使用新的API（如果支持）
      if (mediaQuery.addEventListener) {
        mediaQuery.addEventListener('change', handler)
      } else {
        // 兼容旧浏览器
        mediaQuery.addListener(handler)
      }
    }
  }

  // 监听主题变化
  watch(isDark, (newValue) => {
    applyTheme(newValue)

    // 同步到其他标签页
    try {
      localStorage.setItem('theme-sync', JSON.stringify({
        isDark: newValue,
        timestamp: Date.now()
      }))
    } catch (e) {
      console.warn('Failed to sync theme:', e)
    }
  })

  // 监听其他标签页的主题变化
  const setupCrossTabSync = () => {
    window.addEventListener('storage', (e) => {
      if (e.key === 'theme-sync' && e.newValue) {
        try {
          const data = JSON.parse(e.newValue)
          // 避免循环更新
          if (Math.abs(Date.now() - data.timestamp) < 1000) {
            if (data.isDark !== isDark.value) {
              themeMode.value = data.isDark ? 'dark' : 'light'
            }
          }
        } catch (err) {
          console.warn('Failed to parse theme sync data:', err)
        }
      }
    })
  }

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

  // 初始化
  const init = () => {
    setupSystemThemeListener()
    setupCrossTabSync()
    applyTheme(isDark.value, false) // 初始化时不使用过渡动画
  }

  // 立即初始化
  init()

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
