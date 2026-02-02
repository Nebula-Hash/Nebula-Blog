<template>
    <picture class="webp-image">
        <!-- WebP格式 -->
        <source v-if="webpSrc" :srcset="webpSrc" type="image/webp" />

        <!-- 原始格式（fallback） -->
        <img :src="src" :alt="alt" :class="imageClass" :loading="loading" @load="handleLoad" @error="handleError" />
    </picture>
</template>

<script setup>
import { computed } from 'vue'
import { getWebPUrl, supportsWebP } from '@/utils/webp'

const props = defineProps({
    src: {
        type: String,
        required: true
    },
    alt: {
        type: String,
        default: ''
    },
    imageClass: {
        type: String,
        default: ''
    },
    loading: {
        type: String,
        default: 'lazy',
        validator: (value) => ['lazy', 'eager'].includes(value)
    },
    enableWebP: {
        type: Boolean,
        default: true
    }
})

const emit = defineEmits(['load', 'error'])

// 计算WebP源
const webpSrc = computed(() => {
    if (!props.enableWebP || !supportsWebP()) return null
    return getWebPUrl(props.src)
})

// 图片加载成功
const handleLoad = (event) => {
    emit('load', event)
}

// 图片加载失败
const handleError = (event) => {
    emit('error', event)
}
</script>

<style scoped>
.webp-image {
    display: inline-block;
    line-height: 0;
}

.webp-image img {
    display: block;
    width: 100%;
    height: auto;
}
</style>
