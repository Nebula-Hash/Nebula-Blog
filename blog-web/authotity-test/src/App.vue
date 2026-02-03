<template>
  <n-config-provider :theme="null">
    <n-message-provider>
      <n-dialog-provider>
        <div class="auth-test-app">
          <!-- 头部 -->
          <div class="header">
            <div class="header-content">
              <h1 class="title">权限功能测试</h1>

              <!-- 认证状态显示 -->
              <div class="auth-section">
                <UserInfo v-if="isLoggedIn" :user-info="userInfo" @logout="handleLogout" @profile="handleProfile"
                  @settings="handleSettings" />
                <AuthButtons v-else @show-login="showLoginModal = true" @show-register="showRegisterModal = true" />
              </div>
            </div>
          </div>

          <!-- 主内容区 -->
          <div class="main-content">
            <n-card title="认证功能测试面板">
              <n-space vertical :size="20">
                <!-- 登录状态 -->
                <n-alert :type="isLoggedIn ? 'success' : 'info'" :title="isLoggedIn ? '已登录' : '未登录'">
                  <template v-if="isLoggedIn">
                    <n-space vertical>
                      <n-text>用户名: {{ userInfo?.username || '-' }}</n-text>
                      <n-text>昵称: {{ userInfo?.nickname || '-' }}</n-text>
                      <n-text>邮箱: {{ userInfo?.email || '-' }}</n-text>
                      <n-text>Token状态: {{ isTokenExpired ? '已过期' : '有效' }}</n-text>
                    </n-space>
                  </template>
                  <template v-else>
                    <n-text>请点击右上角的登录或注册按钮进行测试</n-text>
                  </template>
                </n-alert>

                <!-- 功能测试按钮 -->
                <n-space>
                  <n-button @click="showLoginModal = true">
                    <template #icon>
                      <n-icon :component="LogInOutline" />
                    </template>
                    打开登录弹窗
                  </n-button>
                  <n-button @click="showRegisterModal = true">
                    <template #icon>
                      <n-icon :component="PersonAddOutline" />
                    </template>
                    打开注册弹窗
                  </n-button>
                  <n-button v-if="isLoggedIn" @click="handleLogout" type="error">
                    <template #icon>
                      <n-icon :component="LogOutOutline" />
                    </template>
                    退出登录
                  </n-button>
                  <n-button v-if="isLoggedIn" @click="handleRefreshUserInfo" :loading="refreshing">
                    <template #icon>
                      <n-icon :component="RefreshOutline" />
                    </template>
                    刷新用户信息
                  </n-button>
                </n-space>

                <!-- Token信息 -->
                <n-card v-if="isLoggedIn" title="Token信息" size="small">
                  <n-space vertical>
                    <n-text depth="3">Token: {{ tokenPreview }}</n-text>
                    <n-text depth="3">过期时间: {{ tokenExpireTime }}</n-text>
                  </n-space>
                </n-card>

                <!-- 测试说明 -->
                <n-card title="测试说明" size="small">
                  <n-space vertical>
                    <n-text depth="3">1. 点击"打开登录弹窗"或"打开注册弹窗"测试认证功能</n-text>
                    <n-text depth="3">2. 注册成功后会自动登录</n-text>
                    <n-text depth="3">3. 登录成功后可以看到用户信息和Token信息</n-text>
                    <n-text depth="3">4. 可以测试退出登录功能</n-text>
                    <n-text depth="3">5. 刷新页面后会自动恢复登录状态（如果Token未过期）</n-text>
                    <n-text depth="3">6. Token即将过期时会自动刷新（无感刷新）</n-text>
                  </n-space>
                </n-card>
              </n-space>
            </n-card>
          </div>

          <!-- 登录模态框 -->
          <LoginModal v-model="showLoginModal" @switch-to-register="switchToRegister" />

          <!-- 注册模态框 -->
          <RegisterModal v-model="showRegisterModal" @switch-to-login="switchToLogin" />
        </div>
      </n-dialog-provider>
    </n-message-provider>
  </n-config-provider>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useAuth } from './composables/useAuth'
import { useTokenRefresh } from './composables/useTokenRefresh'
import * as tokenService from './services/tokenService'
import LoginModal from './components/LoginModal.vue'
import RegisterModal from './components/RegisterModal.vue'
import UserInfo from './components/UserInfo.vue'
import AuthButtons from './components/AuthButtons.vue'
import {
  LogInOutline,
  LogOutOutline,
  PersonAddOutline,
  RefreshOutline
} from '@vicons/ionicons5'

const { isLoggedIn, userInfo, logout, fetchUserInfo } = useAuth()

// 启动Token自动刷新
useTokenRefresh()

const showLoginModal = ref(false)
const showRegisterModal = ref(false)
const refreshing = ref(false)

// Token是否过期
const isTokenExpired = computed(() => {
  return tokenService.isTokenExpired()
})

// Token预览（前10个字符）
const tokenPreview = computed(() => {
  const token = tokenService.getToken()
  return token ? `${token.substring(0, 30)}...` : '-'
})

// Token过期时间
const tokenExpireTime = computed(() => {
  const expireTime = tokenService.getTokenExpireTime()
  if (!expireTime) return '-'
  return new Date(expireTime).toLocaleString('zh-CN')
})

const handleLogout = async () => {
  await logout()
}

const handleProfile = () => {
  console.log('跳转到个人中心')
}

const handleSettings = () => {
  console.log('跳转到设置页面')
}

const handleRefreshUserInfo = async () => {
  refreshing.value = true
  await fetchUserInfo()
  refreshing.value = false
}

const switchToRegister = () => {
  showLoginModal.value = false
  showRegisterModal.value = true
}

const switchToLogin = () => {
  showRegisterModal.value = false
  showLoginModal.value = true
}
</script>

<style scoped>
.auth-test-app {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.header {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  padding: 0 20px;
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 64px;
}

.title {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.auth-section {
  display: flex;
  align-items: center;
}

.main-content {
  max-width: 1200px;
  margin: 40px auto;
  padding: 0 20px;
}
</style>
