import { createApp } from 'vue'
import { createPinia } from 'pinia'
import naive from 'naive-ui'
import App from './App.vue'
import './style.css'

// 配置 highlight.js 用于代码高亮
import hljs from 'highlight.js/lib/core'
import json from 'highlight.js/lib/languages/json'

// 注册需要的语言
hljs.registerLanguage('json', json)

import { useUserStore } from './stores/user'
import * as tokenService from './services/tokenService'
import * as authService from './services/authService'

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
app.use(naive)

// 配置 Naive UI 的 hljs
app.provide('hljs', hljs)

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
