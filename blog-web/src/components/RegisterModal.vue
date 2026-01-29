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
import { ref, computed } from 'vue'
import { useAuth } from '@/composables/useAuth'

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

const validatePasswordSame = (rule, value) => {
  return value === formData.value.password
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度为3-20个字符', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]+$/, message: '用户名只能包含字母、数字和下划线', trigger: 'blur' }
  ],
  nickname: [
    { max: 50, message: '昵称长度不能超过50个字符', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { validator: validatePasswordSame, message: '两次密码不一致', trigger: 'blur' }
  ]
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
