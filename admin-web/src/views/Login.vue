<template>
  <div class="login-container">
    <!-- 左侧装饰区 -->
    <div class="login-left">
      <div class="left-content">
        <h1 class="system-title">博客后台管理系统</h1>
        <p class="system-desc">Nebula-Blog Admin System</p>
        <div class="decoration-circle circle-1"></div>
        <div class="decoration-circle circle-2"></div>
        <div class="decoration-circle circle-3"></div>
      </div>
    </div>

    <!-- 右侧登录表单 -->
    <div class="login-right">
      <div class="login-form-wrapper">
        <div class="form-header">
          <h2>欢迎登录</h2>
          <p>Welcome Back</p>
        </div>

        <n-form ref="formRef" :model="formData" :rules="rules" size="large">
          <n-form-item path="username">
            <n-input v-model:value="formData.username" placeholder="请输入用户名" @keyup.enter="handleLogin">
              <template #prefix>
                <n-icon :component="PersonOutline" />
              </template>
            </n-input>
          </n-form-item>

          <n-form-item path="password">
            <n-input v-model:value="formData.password" type="password" show-password-on="click" placeholder="请输入密码"
              @keyup.enter="handleLogin">
              <template #prefix>
                <n-icon :component="LockClosedOutline" />
              </template>
            </n-input>
          </n-form-item>

          <n-form-item>
            <n-button type="primary" size="large" block :loading="loading" @click="handleLogin">
              登录
            </n-button>
          </n-form-item>
        </n-form>

      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { NForm, NFormItem, NInput, NButton, NIcon } from 'naive-ui'
import { PersonOutline, LockClosedOutline } from '@vicons/ionicons5'
import { login } from '@/api/auth'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref(null)
const loading = ref(false)

const formData = ref({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  try {
    await formRef.value?.validate()
    loading.value = true

    const res = await login(formData.value)

    userStore.setToken(res.data.token)
    userStore.setUserInfo({
      userId: res.data.userId,
      username: res.data.username,
      nickname: res.data.nickname
    })

    window.$message.success('登录成功')
    router.push('/')
  } catch (error) {
    console.error('登录失败:', error)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  width: 100%;
  height: 100vh;
  background: linear-gradient(135deg, #0a0e27 0%, #1a1f3a 100%);
}

.login-left {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, #0f1419 0%, #1a1f2e 100%);
}

.left-content {
  text-align: center;
  color: #2ADB5C;
  z-index: 1;
  padding: 40px;
}

.system-title {
  font-size: 48px;
  font-weight: 700;
  margin-bottom: 20px;
  text-shadow: 0 0 20px rgba(42, 219, 92, 0.5);
  background: linear-gradient(135deg, #2ADB5C 0%, #18A058 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.system-desc {
  font-size: 24px;
  opacity: 0.9;
  letter-spacing: 2px;
  color: #fff;
  text-shadow: 0 0 10px rgba(42, 219, 92, 0.3);
}

.decoration-circle {
  position: absolute;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(42, 219, 92, 0.15) 0%, rgba(42, 219, 92, 0.05) 50%, transparent 100%);
  animation: float 6s ease-in-out infinite;
  border: 2px solid rgba(42, 219, 92, 0.2);
}

.circle-1 {
  width: 300px;
  height: 300px;
  top: 10%;
  left: 10%;
  animation-delay: 0s;
}

.circle-2 {
  width: 200px;
  height: 200px;
  bottom: 20%;
  right: 15%;
  animation-delay: 2s;
}

.circle-3 {
  width: 150px;
  height: 150px;
  top: 50%;
  right: 20%;
  animation-delay: 4s;
}

@keyframes float {

  0%,
  100% {
    transform: translateY(0) scale(1);
    opacity: 0.6;
  }

  50% {
    transform: translateY(-20px) scale(1.05);
    opacity: 0.8;
  }
}

.login-right {
  width: 500px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #1a1d2e;
  box-shadow: -5px 0 30px rgba(42, 219, 92, 0.1);
}

.login-form-wrapper {
  width: 100%;
  max-width: 380px;
  padding: 40px;
  background: rgba(26, 29, 46, 0.6);
  border-radius: 16px;
  border: 1px solid rgba(42, 219, 92, 0.2);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
  backdrop-filter: blur(10px);
}

.form-header {
  text-align: center;
  margin-bottom: 40px;
}

.form-header h2 {
  font-size: 32px;
  font-weight: 600;
  color: #2ADB5C;
  margin-bottom: 8px;
  text-shadow: 0 0 10px rgba(42, 219, 92, 0.3);
}

.form-header p {
  font-size: 14px;
  color: #8b8d98;
}

.login-tips {
  margin-top: 20px;
  text-align: center;
  color: #6b6d78;
  font-size: 13px;
}

.login-tips p {
  margin: 5px 0;
  padding: 8px;
  background: rgba(42, 219, 92, 0.1);
  border-radius: 4px;
  border-left: 3px solid #2ADB5C;
}

@media (max-width: 768px) {
  .login-left {
    display: none;
  }

  .login-right {
    width: 100%;
  }
}
</style>
