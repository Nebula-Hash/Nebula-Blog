import { watch } from 'vue'
import { useThemeStore } from '@/stores'

export function setupTheme(pinia) {
    const themeStore = useThemeStore(pinia)

    const mediaQuery = window.matchMedia ? window.matchMedia('(prefers-color-scheme: dark)') : null

    const updateSystemPreference = () => {
        if (mediaQuery) {
            themeStore.systemPrefersDark = mediaQuery.matches
        }
    }

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

    const applyTheme = (dark, withTransition = true) => {
        const root = document.documentElement

        if (withTransition) {
            root.style.setProperty('--theme-transition', 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)')
        }

        if (dark) {
            root.classList.add('dark-theme')
        } else {
            root.classList.remove('dark-theme')
        }

        updateMetaThemeColor(dark)

        if (withTransition) {
            setTimeout(() => {
                root.style.removeProperty('--theme-transition')
            }, 300)
        }
    }

    updateSystemPreference()
    applyTheme(themeStore.isDark, false)

    const stopWatch = watch(() => themeStore.isDark, (value) => {
        applyTheme(value)
    })

    const mediaHandler = (e) => {
        themeStore.systemPrefersDark = e.matches
    }

    if (mediaQuery) {
        if (mediaQuery.addEventListener) {
            mediaQuery.addEventListener('change', mediaHandler)
        } else {
            mediaQuery.addListener(mediaHandler)
        }
    }

    return () => {
        stopWatch()

        if (!mediaQuery) return

        if (mediaQuery.removeEventListener) {
            mediaQuery.removeEventListener('change', mediaHandler)
        } else {
            mediaQuery.removeListener(mediaHandler)
        }
    }
}
