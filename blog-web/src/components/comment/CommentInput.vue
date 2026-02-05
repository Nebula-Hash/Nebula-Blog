<template>
  <div class="comment-input">
    <n-input
      v-model:value="content"
      type="textarea"
      :placeholder="placeholder"
      :autosize="{
        minRows: 3,
        maxRows: 8
      }"
      :maxlength="maxLength"
      show-count
      clearable
      @keydown.ctrl.enter="handleSubmit"
    />
    <n-space justify="end" :size="12" class="comment-actions">
      <n-button v-if="showCancel" @click="handleCancel">
        取消
      </n-button>
      <n-button type="primary" :loading="loading" :disabled="!isValid" @click="handleSubmit">
        {{ submitText }}
      </n-button>
    </n-space>
    <n-text v-if="error" type="error" class="comment-error">
      {{ error }}
    </n-text>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { NInput, NButton, NSpace, NText } from 'naive-ui'

const props = defineProps({
  placeholder: {
    type: String,
    default: '写下你的评论...'
  },
  submitText: {
    type: String,
    default: '发布'
  },
  showCancel: {
    type: Boolean,
    default: false
  },
  maxLength: {
    type: Number,
    default: 500
  },
  loading: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['submit', 'cancel'])

const content = ref('')
const error = ref('')

// 验证输入是否有效
const isValid = computed(() => {
  return content.value.trim().length > 0 && content.value.length <= props.maxLength
})

// 提交评论
const handleSubmit = () => {
  if (!isValid.value) {
    error.value = '评论内容不能为空'
    return
  }

  error.value = ''
  emit('submit', content.value.trim())
}

// 取消评论
const handleCancel = () => {
  content.value = ''
  error.value = ''
  emit('cancel')
}

// 清空输入
const clear = () => {
  content.value = ''
  error.value = ''
}

// 暴露方法给父组件
defineExpose({
  clear
})
</script>

<style scoped>
.comment-input {
  width: 100%;
}

.comment-actions {
  margin-top: 12px;
}

.comment-error {
  margin-top: 8px;
  display: block;
}
</style>
