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
    import('./composables/usePerformance').then(({ usePerformance }) => {
        // 全局性能监控
        const { monitor } = usePerformance({
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
    })
}

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
app.use(router)
app.use(naive)

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
