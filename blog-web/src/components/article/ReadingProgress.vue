<template>
  <div class="reading-progress" :class="{ visible: progress > 0 }">
    <div class="progress-bar" :style="{ width: `${progress}%` }" />
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { throttle } from '@/utils/performance'

const props = defineProps({
  target: {
    type: [String, HTMLElement],
    default: null
  }
})

const progress = ref(0)

// 计算阅读进度
const calculateProgress = () => {
  let scrollTop, scrollHeight, clientHeight

  if (props.target) {
    // 如果指定了目标元素
    const element = typeof props.target === 'string'
      ? document.querySelector(props.target)
      : props.target

    if (!element) return

    scrollTop = element.scrollTop
    scrollHeight = element.scrollHeight
    clientHeight = element.clientHeight
  } else {
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

  // 监听滚动事件
  if (props.target) {
    const element = typeof props.target === 'string'
      ? document.querySelector(props.target)
      : props.target

    if (element) {
      element.addEventListener('scroll', handleScroll, { passive: true })
    }
  } else {
    window.addEventListener('scroll', handleScroll, { passive: true })
  }
})

onUnmounted(() => {
  // 移除事件监听
  if (props.target) {
    const element = typeof props.target === 'string'
      ? document.querySelector(props.target)
      : props.target

    if (element) {
      element.removeEventListener('scroll', handleScroll)
    }
  } else {
    window.removeEventListener('scroll', handleScroll)
  }
})
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
