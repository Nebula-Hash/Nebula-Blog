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
  cursor: pointer;
  transition: all 0.3s;
  border-radius: 12px;
  background: #141517;
  border: 1px solid rgba(42, 219, 92, 0.08);
}

.article-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 32px rgba(42, 219, 92, 0.2);
  border-color: rgba(42, 219, 92, 0.3);
}

.article-content {
  display: flex;
  gap: 20px;
}

.cover-image {
  width: 200px;
  height: 150px;
  border-radius: 8px;
  flex-shrink: 0;
}

.cover-image :deep(.image) {
  object-fit: cover;
  border-radius: 8px;
}

.article-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.article-title {
  margin: 0 0 10px 0;
  font-size: 20px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.95);
  line-height: 1.4;
}

.article-summary {
  margin: 0;
  color: rgba(255, 255, 255, 0.65);
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.article-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 10px;
}
</style>
