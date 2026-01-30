<template>
  <div class="comment-item">
    <n-space align="start" :size="15">
      <n-avatar round :size="40" :src="comment.avatar" />
      <div class="comment-content">
        <div class="comment-header">
          <n-text strong>{{ comment.nickname }}</n-text>
          <n-text depth="3" style="font-size: 12px; margin-left: 10px">
            {{ formatDate(comment.createTime) }}
          </n-text>
        </div>
        <div class="comment-text" v-if="comment.replyNickname">
          回复 <n-text type="primary">@{{ comment.replyNickname }}</n-text>: {{ comment.content }}
        </div>
        <div class="comment-text" v-else>{{ comment.content }}</div>
        <n-space :size="20" class="comment-actions">
          <n-button text @click="handleLike">
            <n-icon :component="HeartOutline" /> {{ comment.likeCount }}
          </n-button>
          <n-button text @click="handleReply">
            <n-icon :component="ChatbubbleOutline" /> 回复
          </n-button>
        </n-space>

        <!-- 子评论 -->
        <div v-if="comment.children && comment.children.length > 0" class="children-comments">
          <CommentItem v-for="child in comment.children" :key="child.id" :comment="child" @reply="$emit('reply', child)"
            @like="$emit('like', child.id)" />
        </div>
      </div>
    </n-space>
  </div>
</template>

<script setup>
import { NSpace, NAvatar, NText, NButton, NIcon } from 'naive-ui'
import { HeartOutline, ChatbubbleOutline } from '@vicons/ionicons5'
import { formatDateTime } from '@/utils/common'

const props = defineProps({
  comment: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['reply', 'like'])

const handleReply = () => {
  emit('reply', props.comment)
}

const handleLike = () => {
  emit('like', props.comment.id)
}

const formatDate = (date) => {
  return formatDateTime(date)
}
</script>

<style scoped>
.comment-item {
  width: 100%;
}

.comment-content {
  flex: 1;
}

.comment-header {
  margin-bottom: 8px;
}

.comment-text {
  margin-bottom: 8px;
  line-height: 1.6;
}

.comment-actions {
  margin-top: 8px;
}

.children-comments {
  margin-top: 15px;
  padding-left: 20px;
  border-left: 2px solid rgba(42, 219, 92, 0.2);
}
</style>
