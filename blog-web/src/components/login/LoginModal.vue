<template>
  <n-modal v-model:show="show" preset="card" title="登录" style="width: 400px; max-width: 90vw;">
    <n-form ref="formRef" :model="formData" :rules="rules">
      <n-form-item path="username" label="用户名">
        <n-input 
          v-model:value="formData.username" 
          placeholder="请输入用户名"
          autocomplete="username"
          @keyup.enter="handleLogin"
        />
      </n-form-item>
      
      <n-form-item path="password" label="密码">
        <n-input 
          v-model:value="formData.password" 
          type="password"
          show-password-on="click"
          placeholder="请输入密码"
          autocomplete="current-password"
          @keyup.enter="handleLogin"
        />
      </n-form-item>
    </n-form>
    
    <template #footer>
      <n-space justify="space-between" style="width: 100%">
        <n-button text @click="switchToRegister">还没有账号？去注册</n-button>
        <n-space>
          <n-button @click="show = false">取消</n-button>
          <n-button type="primary" :loading="loading" @click="handleLogin">
            登录
          </n-button>
        </n-space>
      </n-space>
    </template>
  </n-modal>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useAuth } from '@/composables/useAuth'
import { required } from '@/utils/validators'

const props = defineProps({
  modelValue: Boolean
})

const emit = defineEmits(['update:modelValue', 'switchToRegister'])

const show = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const { loading, login } = useAuth()

const formRef = ref(null)
const formData = ref({
  username: '',
  password: ''
})

const rules = {
  username: [required('请输入用户名')],
  password: [required('请输入密码')]
}

const handleLogin = async () => {
  try {
    await formRef.value?.validate()
    const success = await login(formData.value.username, formData.value.password)
    if (success) {
      show.value = false
      formData.value = { username: '', password: '' }
    }
  } catch (error) {
    // 验证失败
  }
}

const switchToRegister = () => {
  show.value = false
  emit('switchToRegister')
}
</script>
