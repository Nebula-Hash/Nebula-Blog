<template>
  <router-view />
</template>

<script setup>
import { onMounted } from 'vue'
import { useMessage, useDialog, useNotification } from 'naive-ui'

onMounted(() => {
  window.$message = useMessage()
  window.$dialog = useDialog()
  window.$notification = useNotification()
})
</script>
