<template>
  <n-card hoverable class="article-card" @click="handleClick">
    <div class="article-content">
      <LazyImage v-if="article.coverImage" :src="article.coverImage" :alt="article.title" class="cover-image"
        :support-webp="true" width="200" height="150" />
      <div class="article-info">
        <h3 class="article-title">{{ article.title }}</h3>
        <p class="article-summary">{{ article.summary }}</p>

        <div class="article-meta">
          <n-space :size="10">
            <n-avatar round :size="24" :src="article.authorAvatar" />
            <n-text depth="3">{{ article.authorNickname }}</n-text>
          </n-space>

          <n-space :size="15">
            <n-text depth="3" v-if="article.categoryName">
              <n-tag size="small" :bordered="false">{{ article.categoryName }}</n-tag>
            </n-text>
            <n-text depth="3">
              <n-icon :component="EyeOutline" /> {{ article.viewCount }}
            </n-text>
            <n-text depth="3">
              <n-icon :component="HeartOutline" /> {{ article.likeCount }}
            </n-text>
            <n-text depth="3">
              <n-icon :component="ChatbubbleOutline" /> {{ article.commentCount }}
            </n-text>
            <n-text depth="3">{{ formatDate(article.createTime) }}</n-text>
          </n-space>
        </div>
      </div>
    </div>
  </n-card>
</template>

<script setup>
import { NCard, NSpace, NAvatar, NText, NTag, NIcon } from 'naive-ui'
import { EyeOutline, HeartOutline, ChatbubbleOutline } from '@vicons/ionicons5'
import { formatDateTime } from '@/utils/common'
import LazyImage from '../common/LazyImage.vue'

const props = defineProps({
  article: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['click'])

const handleClick = () => {
  emit('click')
}

const formatDate = (date) => {
  return formatDateTime(date, 'date')
}
</script>

<style scoped>
.article-card {
  position: relative;
  cursor: pointer;
  background: var(--surface-primary);
  border: 1px solid var(--border-secondary);
  border-radius: var(--radius-xl);
  overflow: hidden;
  transition: all var(--transition-base);
}

.article-card::after {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(
    135deg,
    transparent 0%,
    var(--color-primary-alpha-10) 100%
  );
  opacity: 0;
  transition: opacity var(--transition-base);
  pointer-events: none;
}

.article-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-elevated);
  border-color: var(--color-primary-alpha-30);
}

.article-card:hover::after {
  opacity: 1;
}

.article-content {
  position: relative;
  z-index: 1;
  display: flex;
  gap: var(--spacing-lg);
}

.cover-image {
  width: 200px;
  height: 150px;
  border-radius: var(--radius-lg);
  flex-shrink: 0;
  overflow: hidden;
}

.cover-image :deep(.image) {
  object-fit: cover;
  border-radius: var(--radius-lg);
  transition: transform var(--transition-slow);
}

.article-card:hover .cover-image :deep(.image) {
  transform: scale(1.08);
}

.article-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-width: 0;
}

.article-title {
  margin: 0 0 var(--spacing-sm) 0;
  font-size: var(--font-size-xl);
  font-weight: var(--font-weight-semibold);
  color: var(--text-primary);
  line-height: var(--line-height-tight);
  transition: color var(--transition-fast);
}

.article-card:hover .article-title {
  color: var(--color-primary);
}

.article-summary {
  margin: 0;
  color: var(--text-secondary);
  line-height: var(--line-height-relaxed);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.article-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: var(--spacing-sm);
  font-size: var(--font-size-sm);
  color: var(--text-tertiary);
}

/* 响应式 */
@media (max-width: 768px) {
  .article-content {
    flex-direction: column;
  }
  
  .cover-image {
    width: 100%;
    height: 180px;
  }
}
</style>
