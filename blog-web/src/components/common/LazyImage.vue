<template>
  <div class="lazy-image" ref="containerRef">
    <!-- 占位符 -->
    <div v-if="!isLoaded && !error" class="placeholder" :style="placeholderStyle">
      <n-spin v-if="isVisible" size="small" />
    </div>

    <!-- 实际图片 -->
    <picture v-if="isVisible && !error">
      <!-- WebP 格式（如果支持）-->
      <source v-if="webpSrc" :srcset="webpSrc" type="image/webp" />
      <!-- 原始格式 -->
      <img v-show="isLoaded" :src="currentSrc" :alt="alt" :class="['image', { 'fade-in': showAnimation && isLoaded }]"
        :loading="nativeLazy ? 'lazy' : 'eager'" @load="handleLoad" @error="handleError" />
    </picture>

    <!-- 错误降级 -->
    <img v-if="error" :src="fallbackSrc" :alt="alt" class="image fallback" />
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { NSpin } from 'naive-ui'
import { useLazyLoad } from '@/composables/helper/useLazyLoad'

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
  },
  // 是否使用原生懒加载
  nativeLazy: {
    type: Boolean,
    default: false
  },
  // 是否支持 WebP
  supportWebp: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits(['load', 'error'])

const isLoaded = ref(false)
const error = ref(false)
const currentSrc = ref(null)

// 检查是否支持 WebP
const supportsWebP = ref(false)

// 检测 WebP 支持
const checkWebPSupport = () => {
  if (!props.supportWebp) return false

  const canvas = document.createElement('canvas')
  if (canvas.getContext && canvas.getContext('2d')) {
    return canvas.toDataURL('image/webp').indexOf('data:image/webp') === 0
  }
  return false
}

supportsWebP.value = checkWebPSupport()

// WebP 图片源
const webpSrc = computed(() => {
  if (!supportsWebP.value || !props.src || !props.supportWebp) return null

  // 如果原图已经是 WebP，直接返回
  if (props.src.endsWith('.webp')) return props.src

  // 对于新上传的图片（包含日期格式），已经是WebP格式，直接使用
  // 后端已经自动转换，URL中的扩展名就是.webp
  if (/\/\d{4}-\d{2}-\d{2}/.test(props.src)) {
    // 新上传的图片，后端已处理为WebP
    return props.src
  }

  // 对于历史图片（不包含日期格式），尝试加载WebP版本
  // 这部分用于未来的历史数据迁移
  const ext = props.src.split('.').pop()
  if (ext && ext.length <= 4) {
    return props.src.replace(`.${ext}`, '.webp')
  }

  return null
})

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
const { targetRef: containerRef, isVisible, setupObserver, cleanupObserver } = useLazyLoad({
  rootMargin: '100px', // 提前 100px 开始加载
  threshold: 0.01,
  onVisible: () => {
    // 图片进入视口时开始加载
    if (!isLoaded.value && !error.value) {
      currentSrc.value = props.src
    }
  }
})

// 手动设置和清理观察器
onMounted(() => {
  setupObserver()
})

onUnmounted(() => {
  cleanupObserver()
})

// 图片加载成功
const handleLoad = (event) => {
  isLoaded.value = true

  // 记录图片加载性能
  if (window.performance && window.performance.now) {
    const loadTime = window.performance.now()
    console.debug(`[LazyImage] 图片加载完成: ${props.src}, 耗时: ${loadTime.toFixed(2)}ms`)
  }

  emit('load', event)
}

// 图片加载失败
const handleError = (event) => {
  error.value = true

  // 只在非WebP fallback的情况下记录警告
  // 如果是WebP加载失败，picture标签会自动fallback到原图，这是正常行为
  const isWebPFallback = webpSrc.value && props.src !== webpSrc.value

  if (!isWebPFallback) {
    console.warn(`[LazyImage] 图片加载失败: ${props.src}`)
  }

  emit('error', event)
}

// 监听src变化，重置状态
watch(() => props.src, () => {
  isLoaded.value = false
  error.value = false
  currentSrc.value = null
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
