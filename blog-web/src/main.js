import { createApp } from 'vue'
import { createPinia } from 'pinia'
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate'
import router from './router'

import App from './App.vue'
import './css/style.css'

// 导入 Naive UI
import naive from 'naive-ui'

import { setupTheme } from './app/setupTheme'

// 性能监控（仅在开发环境）
if (import.meta.env.DEV) {
    import('./composables/helper/usePerformance').then(({ createGlobalPerformanceMonitor }) => {
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

// 配置 Pinia 持久化插件
pinia.use(piniaPluginPersistedstate)

app.use(pinia)
app.use(router)
app.use(naive)

setupTheme(pinia)

app.mount('#app')
