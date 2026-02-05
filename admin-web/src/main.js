import { createApp } from 'vue'
import { createPinia } from 'pinia'
import router from './router'

import App from './App.vue'
import './style.css'
import * as authService from './services/authService'

// 导入 Naive UI
import naive from 'naive-ui'

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
app.use(router)
app.use(naive)

async function bootstrap() {
    await authService.initializeSession()
    app.mount('#app')
}

bootstrap()
