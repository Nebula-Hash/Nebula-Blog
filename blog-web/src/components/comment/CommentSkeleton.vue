<template>
  <div class="comment-skeleton">
    <n-space v-for="i in count" :key="i" align="start" :size="15" class="skeleton-item">
      <n-skeleton circle :width="40" :height="40" />
      <div class="skeleton-content">
        <n-skeleton text :width="'30%'" style="margin-bottom: 8px" />
        <n-skeleton text :repeat="2" />
        <n-space :size="20" style="margin-top: 12px">
          <n-skeleton text :width="60" />
          <n-skeleton text :width="60" />
        </n-space>
      </div>
    </n-space>
  </div>
</template>

<script setup>
import { NSkeleton, NSpace } from 'naive-ui'

defineProps({
  count: {
    type: Number,
    default: 3
  }
})
</script>

<style scoped>
.comment-skeleton {
  width: 100%;
}

.skeleton-item {
  width: 100%;
  margin-bottom: 20px;
}

.skeleton-content {
  flex: 1;
}
</style>
