import { createApp } from 'vue'
import { createPinia } from 'pinia'
import router from './router'

import App from './App.vue'
import './style.css'

import { useUserStore } from './stores/user'
import * as tokenService from './services/tokenService'
import * as authService from './services/authService'

// 导入 Naive UI
import naive from 'naive-ui'

// 性能监控（仅在开发环境）
if (import.meta.env.DEV) {
    import('./composables/usePerformance').then(({ createGlobalPerformanceMonitor }) => {
        // 全局性能监控（不依赖组件生命周期）
        const { monitor, cleanup } = createGlobalPerformanceMonitor({
            onMetric: (name, value) => {
                console.log(`[Performance] ${name}:`, value)
            }
        })

        // 页面加载完成后输出性能指标
        window.addEventListener('load', () => {
            setTimeout(() => {
                const metrics = monitor.getAllMetrics()
                console.table(metrics.pageLoad)
            }, 1000)
        })

        // 页面卸载时清理
        window.addEventListener('beforeunload', () => {
            cleanup()
        })
    })
}

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
app.use(router)
app.use(naive)

// 全局配置 - 禁用 Naive UI 的 aria-hidden 警告（这是库的已知问题）
// 参考：https://github.com/tusen-ai/naive-ui/issues/4820
if (import.meta.env.DEV) {
    const originalWarn = console.warn
    console.warn = (...args) => {
        const msg = args[0]
        // 过滤掉 aria-hidden 相关的警告（这是 Naive UI 的已知问题，不影响功能）
        if (typeof msg === 'string' && msg.includes('aria-hidden')) {
            return
        }
        originalWarn.apply(console, args)
    }
}

// 应用初始化
const userStore = useUserStore()

// 从 localStorage 恢复认证状态
userStore.initialize()

// 如果Token有效但userInfo为空，尝试获取用户信息
if (tokenService.getToken() && !tokenService.isTokenExpired() && !userStore.userInfo) {
    authService.getCurrentUser().catch(error => {
        console.error('[Main] 恢复用户信息失败:', error)
        userStore.clearAuth()
    })
}

app.mount('#app')
