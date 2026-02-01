<template>
  <div class="lazy-image" ref="containerRef">
    <!-- 占位符 -->
    <div v-if="!isLoaded && !error" class="placeholder" :style="placeholderStyle">
      <n-spin v-if="isVisible" size="small" />
    </div>
    
    <!-- 实际图片 -->
    <img
      v-show="isLoaded && !error"
      :src="src"
      :alt="alt"
      :class="['image', { 'fade-in': showAnimation && isLoaded }]"
      @load="handleLoad"
      @error="handleError"
    />
    
    <!-- 错误降级 -->
    <img
      v-if="error"
      :src="fallbackSrc"
      :alt="alt"
      class="image fallback"
    />
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { NSpin } from 'naive-ui'
import { useLazyLoad } from '@/composables/useLazyLoad'

const props = defineProps({
  src: {
    type: String,
    required: true
  },
  alt: {
    type: String,
    default: ''
  },
  fallbackSrc: {
    type: String,
    default: '/default-image.png'
  },
  showAnimation: {
    type: Boolean,
    default: true
  },
  width: {
    type: [String, Number],
    default: '100%'
  },
  height: {
    type: [String, Number],
    default: 'auto'
  },
  aspectRatio: {
    type: String,
    default: null
  }
})

const emit = defineEmits(['load', 'error'])

const isLoaded = ref(false)
const error = ref(false)
const imgSrc = ref(null)

// 占位符样式
const placeholderStyle = computed(() => {
  const style = {
    width: typeof props.width === 'number' ? `${props.width}px` : props.width,
    height: typeof props.height === 'number' ? `${props.height}px` : props.height
  }
  
  if (props.aspectRatio) {
    style.aspectRatio = props.aspectRatio
  }
  
  return style
})

// 使用懒加载
const { targetRef: containerRef, isVisible } = useLazyLoad({
  rootMargin: '50px',
  threshold: 0.01,
  onVisible: () => {
    // 图片进入视口时开始加载
    if (!isLoaded.value && !error.value) {
      imgSrc.value = props.src
    }
  }
})

// 图片加载成功
const handleLoad = () => {
  isLoaded.value = true
  emit('load')
}

// 图片加载失败
const handleError = () => {
  error.value = true
  emit('error')
}

// 监听src变化，重置状态
watch(() => props.src, () => {
  isLoaded.value = false
  error.value = false
  imgSrc.value = null
})
</script>

<style scoped>
.lazy-image {
  position: relative;
  overflow: hidden;
  display: inline-block;
  width: 100%;
}

.placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: loading 1.5s ease-in-out infinite;
}

@keyframes loading {
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
}

.image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.image.fade-in {
  animation: fadeIn 0.3s ease-in;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.image.fallback {
  opacity: 0.5;
}
</style>
