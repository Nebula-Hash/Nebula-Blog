<template>
    <n-space class="user-info" align="center">
        <n-dropdown :options="userOptions" @select="handleUserAction">
            <n-space align="center" style="cursor: pointer">
                <n-avatar round :size="40" :src="userInfo?.avatar">
                    {{ userInfo?.nickname?.[0] || userInfo?.username?.[0] || 'U' }}
                </n-avatar>
                <n-text>{{ userInfo?.nickname || userInfo?.username || '用户' }}</n-text>
            </n-space>
        </n-dropdown>
    </n-space>
</template>

<script setup>
import { h } from 'vue'
import { NIcon } from 'naive-ui'
import { PersonOutline, LogOutOutline, SettingsOutline } from '@vicons/ionicons5'

const props = defineProps({
    userInfo: {
        type: Object,
        default: null
    }
})

const emit = defineEmits(['logout', 'profile', 'settings'])

const userOptions = [
    {
        label: '个人中心',
        key: 'profile',
        icon: () => h(NIcon, null, { default: () => h(PersonOutline) })
    },
    {
        label: '设置',
        key: 'settings',
        icon: () => h(NIcon, null, { default: () => h(SettingsOutline) })
    },
    {
        type: 'divider',
        key: 'd1'
    },
    {
        label: '退出登录',
        key: 'logout',
        icon: () => h(NIcon, null, { default: () => h(LogOutOutline) })
    }
]

const handleUserAction = (key) => {
    emit(key)
}
</script>

<style scoped>
.user-info {
    display: flex;
    align-items: center;
}
</style>
