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
                                <UserInfo v-if="userStore.isLoggedIn" :user-info="userStore.userInfo"
                                    @logout="handleLogout" @profile="handleProfile" @settings="handleSettings" />
                                <AuthButtons v-else @show-login="showLoginModal = true"
                                    @show-register="showRegisterModal = true" />
                            </div>
                        </div>
                    </div>

                    <!-- 主内容区 -->
                    <div class="main-content">
                        <n-grid :cols="2" :x-gap="20" :y-gap="20" responsive="screen">
                            <!-- 左侧：功能测试面板 -->
                            <n-gi>
                                <n-card title="认证功能测试面板">
                                    <n-space vertical :size="20">
                                        <!-- 登录状态 -->
                                        <n-alert :type="userStore.isLoggedIn ? 'success' : 'info'"
                                            :title="userStore.isLoggedIn ? '✓ 已登录' : '○ 未登录'">
                                            <template v-if="userStore.isLoggedIn">
                                                <n-space vertical>
                                                    <n-text><strong>用户ID:</strong> {{ userStore.userInfo?.userId || '-'
                                                        }}</n-text>
                                                    <n-text><strong>用户名:</strong> {{ userStore.userInfo?.username || '-'
                                                        }}</n-text>
                                                    <n-text><strong>昵称:</strong> {{ userStore.userInfo?.nickname || '-'
                                                        }}</n-text>
                                                    <n-text><strong>邮箱:</strong> {{ userStore.userInfo?.email || '-'
                                                        }}</n-text>
                                                    <n-text><strong>角色:</strong> {{ userStore.userInfo?.roleKey || '-'
                                                        }}</n-text>
                                                    <n-text><strong>头像:</strong> {{ userStore.userInfo?.avatar || '无'
                                                        }}</n-text>
                                                    <n-text><strong>Token状态:</strong>
                                                        <n-tag :type="userStore.isTokenExpired ? 'error' : 'success'"
                                                            size="small">
                                                            {{ userStore.isTokenExpired ? '已过期' : '有效' }}
                                                        </n-tag>
                                                    </n-text>
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
                                            <n-button v-if="userStore.isLoggedIn" @click="handleLogout" type="error">
                                                <template #icon>
                                                    <n-icon :component="LogOutOutline" />
                                                </template>
                                                退出登录
                                            </n-button>
                                            <n-button v-if="userStore.isLoggedIn" @click="handleRefreshUserInfo"
                                                :loading="refreshing">
                                                <template #icon>
                                                    <n-icon :component="RefreshOutline" />
                                                </template>
                                                刷新用户信息
                                            </n-button>
                                            <n-button v-if="userStore.isLoggedIn" @click="handleRefreshToken"
                                                :loading="tokenRefreshing">
                                                <template #icon>
                                                    <n-icon :component="SyncOutline" />
                                                </template>
                                                手动刷新Token
                                            </n-button>
                                            <n-button @click="handleClearStorage" type="warning">
                                                <template #icon>
                                                    <n-icon :component="TrashOutline" />
                                                </template>
                                                清空存储
                                            </n-button>
                                        </n-space>

                                        <!-- Token信息 -->
                                        <n-card v-if="userStore.isLoggedIn" title="Token 详细信息" size="small">
                                            <n-space vertical>
                                                <n-text depth="3"><strong>Token:</strong></n-text>
                                                <n-input :value="userStore.token" type="textarea" readonly
                                                    :autosize="{ minRows: 2, maxRows: 4 }" size="small" />
                                                <n-text depth="3"><strong>过期时间:</strong> {{
                                                    userStore.formattedTokenExpireTime }}</n-text>
                                                <n-text depth="3"><strong>剩余时间:</strong> {{
                                                    userStore.formattedTokenRemainingTime }}</n-text>
                                                <n-text depth="3"><strong>是否即将过期:</strong>
                                                    <n-tag :type="userStore.isTokenExpiring ? 'warning' : 'success'"
                                                        size="small">
                                                        {{ userStore.isTokenExpiring ? '是（5分钟内）' : '否' }}
                                                    </n-tag>
                                                </n-text>
                                            </n-space>
                                        </n-card>

                                        <!-- LocalStorage 信息 -->
                                        <n-card title="LocalStorage 存储信息" size="small">
                                            <n-space vertical>
                                                <n-text depth="3"><strong>Token:</strong> {{ userStore.storageInfo.token
                                                    }}</n-text>
                                                <n-text depth="3"><strong>Token过期时间:</strong> {{
                                                    userStore.storageInfo.tokenExpire }}</n-text>
                                                <n-text depth="3"><strong>用户信息:</strong> {{
                                                    userStore.storageInfo.userInfo }}</n-text>
                                                <n-divider style="margin: 8px 0" />
                                                <n-collapse>
                                                    <n-collapse-item title="查看完整存储数据" name="storage">
                                                        <n-code :code="userStore.storageDataJson" language="json" />
                                                    </n-collapse-item>
                                                </n-collapse>
                                            </n-space>
                                        </n-card>

                                        <!-- 测试说明 -->
                                        <n-card title="测试说明" size="small">
                                            <n-space vertical>
                                                <n-text depth="3">1. 点击"打开登录弹窗"或"打开注册弹窗"测试认证功能</n-text>
                                                <n-text depth="3">2. 注册成功后会自动登录并显示完整响应信息</n-text>
                                                <n-text depth="3">3. 登录成功后可以看到用户信息、Token信息和存储信息</n-text>
                                                <n-text depth="3">4. 可以测试退出登录功能，观察存储清空过程</n-text>
                                                <n-text depth="3">5. 刷新页面后会自动恢复登录状态（如果Token未过期）</n-text>
                                                <n-text depth="3">6. Token即将过期时会自动刷新（无感刷新）</n-text>
                                                <n-text depth="3">7. 可以手动刷新Token测试刷新机制</n-text>
                                                <n-text depth="3">8. 所有操作都会在右侧日志面板显示详细信息</n-text>
                                            </n-space>
                                        </n-card>
                                    </n-space>
                                </n-card>
                            </n-gi>

                            <!-- 右侧：操作日志 -->
                            <n-gi>
                                <n-card title="操作日志" class="log-card">
                                    <template #header-extra>
                                        <n-space>
                                            <n-tag :bordered="false" type="info" size="small">
                                                共 {{ logs.length }} 条
                                            </n-tag>
                                            <n-button size="small" @click="clearLogs">清空日志</n-button>
                                        </n-space>
                                    </template>
                                    <div class="log-container">
                                        <n-empty v-if="logs.length === 0" description="暂无日志" />
                                        <n-timeline v-else>
                                            <n-timeline-item v-for="(log, index) in logs" :key="index" :type="log.type"
                                                :title="log.title" :time="log.time">
                                                <n-collapse>
                                                    <n-collapse-item :title="log.summary" :name="index">
                                                        <n-code v-if="log.data"
                                                            :code="JSON.stringify(log.data, null, 2)" language="json" />
                                                        <n-text v-else>{{ log.message }}</n-text>
                                                    </n-collapse-item>
                                                </n-collapse>
                                            </n-timeline-item>
                                        </n-timeline>
                                    </div>
                                </n-card>
                            </n-gi>
                        </n-grid>
                    </div>

                    <!-- 登录模态框 -->
                    <LoginModal v-model="showLoginModal" @switch-to-register="switchToRegister"
                        @success="handleLoginSuccess" />

                    <!-- 注册模态框 -->
                    <RegisterModal v-model="showRegisterModal" @switch-to-login="switchToLogin"
                        @success="handleRegisterSuccess" />
                </div>
            </n-dialog-provider>
        </n-message-provider>
    </n-config-provider>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useAuth } from './composables/useAuth'
import { useTokenRefresh } from './composables/useTokenRefresh'
import { useUserStore } from './stores/user'
import * as tokenService from './services/tokenService'
import { showSuccess, showError, showInfo } from './utils/common'
import LoginModal from './components/LoginModal.vue'
import RegisterModal from './components/RegisterModal.vue'
import UserInfo from './components/UserInfo.vue'
import AuthButtons from './components/AuthButtons.vue'
import {
    LogInOutline,
    LogOutOutline,
    PersonAddOutline,
    RefreshOutline,
    SyncOutline,
    TrashOutline
} from '@vicons/ionicons5'

const { logout, fetchUserInfo } = useAuth()
const userStore = useUserStore()

// 启动Token自动刷新
useTokenRefresh()

const showLoginModal = ref(false)
const showRegisterModal = ref(false)
const refreshing = ref(false)
const tokenRefreshing = ref(false)
const logs = ref([])

// 添加日志
const addLog = (type, title, summary, data = null, message = null) => {
    logs.value.unshift({
        type,
        title,
        summary,
        data,
        message,
        time: new Date().toLocaleTimeString('zh-CN')
    })
    // 限制日志数量
    if (logs.value.length > 50) {
        logs.value.pop()
    }
}

// 清空日志
const clearLogs = () => {
    logs.value = []
    showInfo('日志已清空')
}

const handleLogout = async () => {
    addLog('info', '退出登录', '开始执行退出登录操作')
    await logout()
    addLog('success', '退出成功', '已清空Token和用户信息', {
        token: null,
        userInfo: null,
        storage: userStore.storageInfo
    })
}

const handleProfile = () => {
    showInfo('跳转到个人中心')
    addLog('info', '个人中心', '点击了个人中心按钮')
}

const handleSettings = () => {
    showInfo('跳转到设置页面')
    addLog('info', '设置', '点击了设置按钮')
}

const handleRefreshUserInfo = async () => {
    refreshing.value = true
    addLog('info', '刷新用户信息', '开始刷新用户信息')

    try {
        const success = await fetchUserInfo()
        if (success) {
            addLog('success', '刷新成功', '用户信息已更新', {
                userInfo: userStore.userInfo
            })
            showSuccess('用户信息已刷新')
        } else {
            addLog('error', '刷新失败', '获取用户信息失败')
        }
    } catch (error) {
        addLog('error', '刷新失败', error.message, { error: error.toString() })
    } finally {
        refreshing.value = false
    }
}

const handleRefreshToken = async () => {
    tokenRefreshing.value = true
    addLog('info', '刷新Token', '开始手动刷新Token')

    try {
        await tokenService.refreshToken()
        addLog('success', 'Token刷新成功', '新Token已保存', {
            token: userStore.token,
            expireTime: userStore.formattedTokenExpireTime,
            storage: userStore.storageInfo
        })
        showSuccess('Token已刷新')
    } catch (error) {
        addLog('error', 'Token刷新失败', error.message, { error: error.toString() })
        showError('Token刷新失败: ' + error.message)
    } finally {
        tokenRefreshing.value = false
    }
}

const handleClearStorage = () => {
    addLog('warning', '清空存储', '开始清空LocalStorage')
    const beforeData = {
        token: userStore.token,
        userInfo: userStore.userInfo
    }

    userStore.clearAuth()

    addLog('success', '存储已清空', '所有LocalStorage数据已清除', {
        before: beforeData,
        after: {
            token: null,
            userInfo: null
        }
    })
    showSuccess('存储已清空，请刷新页面')
}

const handleLoginSuccess = (data) => {
    addLog('success', '登录成功', '用户登录成功，Token已保存', {
        response: data,
        userInfo: userStore.userInfo,
        token: userStore.token,
        storage: userStore.storageInfo
    })
}

const handleRegisterSuccess = (data) => {
    addLog('success', '注册成功', '用户注册成功，已自动登录', {
        response: data,
        userInfo: userStore.userInfo,
        token: userStore.token,
        storage: userStore.storageInfo
    })
}

const switchToRegister = () => {
    showLoginModal.value = false
    showRegisterModal.value = true
    addLog('info', '切换注册', '从登录切换到注册')
}

const switchToLogin = () => {
    showRegisterModal.value = false
    showLoginModal.value = true
    addLog('info', '切换登录', '从注册切换到登录')
}

// 页面加载时记录初始状态
onMounted(() => {
    addLog('info', '页面加载', '测试页面已加载', {
        isLoggedIn: userStore.isLoggedIn,
        userInfo: userStore.userInfo,
        token: userStore.token ? '存在' : '不存在',
        storage: userStore.storageInfo
    })
})
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
    position: sticky;
    top: 0;
    z-index: 100;
}

.header-content {
    max-width: 1920px;
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
    max-width: 1920px;
    margin: 20px auto;
    padding: 0 20px;
}

.log-card {
    height: calc(100vh - 120px);
    display: flex;
    flex-direction: column;
}

.log-card :deep(.n-card__content) {
    flex: 1;
    overflow: hidden;
    display: flex;
    flex-direction: column;
}

.log-container {
    flex: 1;
    overflow-y: auto;
    padding-right: 8px;
}

.log-container::-webkit-scrollbar {
    width: 8px;
}

.log-container::-webkit-scrollbar-track {
    background: #f1f1f1;
    border-radius: 4px;
}

.log-container::-webkit-scrollbar-thumb {
    background: #888;
    border-radius: 4px;
}

.log-container::-webkit-scrollbar-thumb:hover {
    background: #555;
}

/* 响应式布局 */
@media (max-width: 1400px) {
    .main-content {
        max-width: 100%;
    }
}

@media (max-width: 1200px) {
    .main-content :deep(.n-grid) {
        grid-template-columns: 1fr !important;
    }

    .log-card {
        height: auto;
        min-height: 500px;
    }
}
</style>
