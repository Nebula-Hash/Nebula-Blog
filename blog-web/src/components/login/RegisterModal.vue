<template>
  <n-modal v-model:show="show" preset="card" title="注册" style="width: 400px; max-width: 90vw;">
    <n-form ref="formRef" :model="formData" :rules="rules">
      <n-form-item path="username" label="用户名">
        <n-input 
          v-model:value="formData.username" 
          placeholder="请输入用户名（3-20个字符）"
          autocomplete="username"
        />
      </n-form-item>
      
      <n-form-item path="nickname" label="昵称">
        <n-input 
          v-model:value="formData.nickname" 
          placeholder="请输入昵称（可选）"
          autocomplete="nickname"
        />
      </n-form-item>
      
      <n-form-item path="email" label="邮箱">
        <n-input 
          v-model:value="formData.email" 
          placeholder="请输入邮箱（可选）"
          autocomplete="email"
        />
      </n-form-item>
      
      <n-form-item path="password" label="密码">
        <n-input 
          v-model:value="formData.password" 
          type="password"
          show-password-on="click"
          placeholder="请输入密码（6-20个字符）"
          autocomplete="new-password"
        />
      </n-form-item>

      <!-- 密码强度显示 -->
      <n-form-item v-if="formData.password" label="密码强度">
        <n-space vertical style="width: 100%">
          <n-space align="center">
            <n-tag :type="passwordStrengthColor" size="small">
              {{ passwordStrengthText }}
            </n-tag>
            <n-progress 
              type="line" 
              :percentage="passwordStrength.score" 
              :color="passwordStrengthProgressColor"
              :show-indicator="false"
              style="flex: 1"
            />
          </n-space>
          <n-space v-if="passwordStrength.suggestions.length > 0" vertical size="small">
            <n-text depth="3" style="font-size: 12px">改进建议：</n-text>
            <n-text 
              v-for="(suggestion, index) in passwordStrength.suggestions" 
              :key="index"
              depth="3" 
              style="font-size: 12px"
            >
              • {{ suggestion }}
            </n-text>
          </n-space>
        </n-space>
      </n-form-item>
      
      <n-form-item path="confirmPassword" label="确认密码">
        <n-input 
          v-model:value="formData.confirmPassword" 
          type="password"
          show-password-on="click"
          placeholder="请再次输入密码"
          autocomplete="new-password"
          @keyup.enter="handleRegister"
        />
      </n-form-item>
    </n-form>
    
    <template #footer>
      <n-space justify="space-between" style="width: 100%">
        <n-button text @click="switchToLogin">已有账号？去登录</n-button>
        <n-space>
          <n-button @click="show = false">取消</n-button>
          <n-button type="primary" :loading="loading" @click="handleRegister">
            注册
          </n-button>
        </n-space>
      </n-space>
    </template>
  </n-modal>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useAuth } from '@/composables/useAuth'
import { checkPasswordStrength } from '@/utils/security'
import { 
  usernameRules, 
  passwordRules, 
  confirmPasswordRules, 
  nicknameRules, 
  emailRules 
} from '@/utils/validators'

const props = defineProps({
  modelValue: Boolean
})

const emit = defineEmits(['update:modelValue', 'switchToLogin'])

const show = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const { loading, register } = useAuth()

const formRef = ref(null)
const formData = ref({
  username: '',
  nickname: '',
  email: '',
  password: '',
  confirmPassword: ''
})

// 密码强度状态
const passwordStrength = ref({ strength: 'weak', score: 0, suggestions: [] })

// 监听密码变化，实时计算强度
watch(() => formData.value.password, (newPassword) => {
  if (newPassword) {
    passwordStrength.value = checkPasswordStrength(newPassword)
  } else {
    passwordStrength.value = { strength: 'weak', score: 0, suggestions: [] }
  }
})

// 密码强度文本
const passwordStrengthText = computed(() => {
  const textMap = {
    weak: '弱',
    medium: '中',
    strong: '强'
  }
  return textMap[passwordStrength.value.strength] || '弱'
})

// 密码强度标签颜色
const passwordStrengthColor = computed(() => {
  const colorMap = {
    weak: 'error',
    medium: 'warning',
    strong: 'success'
  }
  return colorMap[passwordStrength.value.strength] || 'default'
})

// 密码强度进度条颜色
const passwordStrengthProgressColor = computed(() => {
  const colorMap = {
    weak: '#d03050',
    medium: '#f0a020',
    strong: '#18a058'
  }
  return colorMap[passwordStrength.value.strength] || '#d03050'
})

// 使用统一的验证规则
const rules = {
  username: usernameRules,
  nickname: nicknameRules,
  email: emailRules,
  password: passwordRules,
  confirmPassword: confirmPasswordRules(formData.value)
}

const handleRegister = async () => {
  try {
    await formRef.value?.validate()
    const success = await register(formData.value)
    if (success) {
      show.value = false
      formData.value = {
        username: '',
        nickname: '',
        email: '',
        password: '',
        confirmPassword: ''
      }
    }
  } catch (error) {
    // 验证失败
  }
}

const switchToLogin = () => {
  show.value = false
  emit('switchToLogin')
}
</script>
