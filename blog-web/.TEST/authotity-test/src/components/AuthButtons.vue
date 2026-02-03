<template>
    <n-space class="auth-buttons">
        <n-button @click="emit('showLogin')">登录</n-button>
        <n-button type="primary" @click="emit('showRegister')">注册</n-button>
    </n-space>
</template>

<script setup>
const emit = defineEmits(['showLogin', 'showRegister'])
</script>

<style scoped>
.auth-buttons {
    display: flex;
    align-items: center;
}
</style>
