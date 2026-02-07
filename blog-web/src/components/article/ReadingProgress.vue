<template>
  <div class="reading-progress" :class="{ visible: progress > 0 }">
    <div class="progress-bar" :style="{ width: `${progress}%` }" />
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { throttle } from '@/utils/performance'

const props = defineProps({
  target: {
    type: [String, Object],
    default: null
  }
})

const progress = ref(0)

const resolveTarget = () => {
  if (!props.target) return null
  if (typeof props.target === 'string') {
    if (typeof document === 'undefined') return null
    return document.querySelector(props.target)
  }
  if (typeof HTMLElement !== 'undefined' && props.target instanceof HTMLElement) {
    return props.target
  }
  return null
}

const boundTarget = ref(null)
const boundToWindow = ref(false)

const bindScroll = () => {
  unbindScroll()

  if (props.target) {
    const element = resolveTarget()
    if (element) {
      boundTarget.value = element
      element.addEventListener('scroll', handleScroll, { passive: true })
    }
    return
  }

  if (typeof window === 'undefined') return
  window.addEventListener('scroll', handleScroll, { passive: true })
  boundToWindow.value = true
}

const unbindScroll = () => {
  if (boundTarget.value) {
    boundTarget.value.removeEventListener('scroll', handleScroll)
    boundTarget.value = null
  }
  if (boundToWindow.value) {
    window.removeEventListener('scroll', handleScroll)
    boundToWindow.value = false
  }
}

// 计算阅读进度
const calculateProgress = () => {
  let scrollTop, scrollHeight, clientHeight

  if (props.target) {
    // 如果指定了目标元素
    const element = resolveTarget()

    if (!element) return

    scrollTop = element.scrollTop
    scrollHeight = element.scrollHeight
    clientHeight = element.clientHeight
  } else {
    if (typeof window === 'undefined') return
    // 默认使用整个页面
    scrollTop = window.pageYOffset || document.documentElement.scrollTop
    scrollHeight = document.documentElement.scrollHeight
    clientHeight = document.documentElement.clientHeight
  }

  // 计算进度百分比
  const scrollableHeight = scrollHeight - clientHeight

  if (scrollableHeight <= 0) {
    progress.value = 0
    return
  }

  const percentage = (scrollTop / scrollableHeight) * 100
  progress.value = Math.min(Math.max(percentage, 0), 100)
}

// 使用节流优化性能（100ms）
const throttledCalculate = throttle(calculateProgress, 100)

// 滚动事件处理
const handleScroll = () => {
  throttledCalculate()
}

onMounted(() => {
  // 初始计算
  calculateProgress()

  bindScroll()
})

onUnmounted(() => {
  unbindScroll()
})

watch(
  () => props.target,
  async () => {
    await nextTick()
    calculateProgress()
    bindScroll()
  }
)
</script>

<style scoped>
.reading-progress {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background-color: var(--divider-secondary);
  z-index: 1000;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.reading-progress.visible {
  opacity: 1;
}

.progress-bar {
  height: 100%;
  background: linear-gradient(90deg, var(--color-primary), var(--color-primary-dark));
  transition: width 0.1s ease-out;
  box-shadow: 0 0 10px var(--color-primary-alpha-30);
}
</style>
