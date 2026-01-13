<template>
  <n-config-provider :theme-overrides="themeOverrides">
    <n-message-provider>
      <n-dialog-provider>
        <n-notification-provider>
          <AppProvider>
            <router-view/>
          </AppProvider>
        </n-notification-provider>
      </n-dialog-provider>
    </n-message-provider>
  </n-config-provider>
</template>

<script setup>
import { NConfigProvider, NMessageProvider, NDialogProvider, NNotificationProvider, useMessage, useDialog, useNotification } from 'naive-ui'
import { defineComponent, h } from 'vue'

const themeOverrides = {
  common: {
    primaryColor: '#18A058',
    primaryColorHover: '#36ad6a',
    primaryColorPressed: '#0c7a43',
    primaryColorSuppl: '#36ad6a'
  }
}

// 创建一个组件来设置全局的 message、dialog、notification
const AppProvider = defineComponent({
  setup() {
    window.$message = useMessage()
    window.$dialog = useDialog()
    window.$notification = useNotification()
  },
  render() {
    return h('div', null, this.$slots)
  }
})
</script>
