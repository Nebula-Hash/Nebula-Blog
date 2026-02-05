<template>
  <div class="article-skeleton">
    <n-card v-for="i in count" :key="i" class="skeleton-card">
      <n-space vertical :size="16">
        <!-- 标题 -->
        <n-skeleton text :width="'80%'" class="skeleton-title" />
        
        <!-- 作者信息 -->
        <n-space :size="12" align="center">
          <n-skeleton circle :width="32" :height="32" />
          <div class="skeleton-author-content">
            <n-skeleton text :width="'30%'" class="skeleton-author-name" />
            <n-skeleton text :width="'50%'" />
          </div>
        </n-space>
        
        <!-- 封面图 -->
        <n-skeleton :width="'100%'" :height="200" />
        
        <!-- 摘要 -->
        <n-skeleton text :repeat="3" />
        
        <!-- 元信息 -->
        <n-space :size="20">
          <n-skeleton text :width="60" />
          <n-skeleton text :width="60" />
          <n-skeleton text :width="60" />
          <n-skeleton text :width="100" />
        </n-space>
      </n-space>
    </n-card>
  </div>
</template>

<script setup>
import { NCard, NSkeleton, NSpace } from 'naive-ui'

defineProps({
  count: {
    type: Number,
    default: 3
  }
})
</script>

<style scoped>
.article-skeleton {
  width: 100%;
}

.skeleton-card {
  margin-bottom: 20px;
  border-radius: var(--radius-lg);
}

.skeleton-card:last-child {
  margin-bottom: 0;
}

.skeleton-title {
  height: 28px;
}

.skeleton-author-content {
  flex: 1;
}

.skeleton-author-name {
  margin-bottom: 4px;
}
</style>
