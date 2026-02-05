<template>
  <div class="comment-item" :class="{ 'is-child': isChild }">
    <n-space align="start" :size="15">
      <n-avatar round :size="40" :src="comment.avatar" />
      <div class="comment-content">
        <div class="comment-header">
          <n-text strong>{{ comment.nickname }}</n-text>
          <n-text depth="3" class="comment-time">
            {{ formatDate(comment.createTime) }}
          </n-text>
        </div>
        <div class="comment-text">
          <span v-if="comment.replyNickname">
            回复 <n-text type="primary">@{{ comment.replyNickname }}</n-text>:
          </span>
          <span v-html="renderContent(comment.content)"></span>
        </div>
        <n-space :size="20" class="comment-actions">
          <n-button text @click="handleLike" :class="{ 'liked': isLiked }">
            <n-icon :component="isLiked ? Heart : HeartOutline" :color="isLiked ? 'var(--color-error)' : undefined" />
            {{ comment.likeCount || 0 }}
          </n-button>
          <n-button text @click="toggleReplyInput">
            <n-icon :component="ChatbubbleOutline" /> 回复
          </n-button>
          <n-button v-if="canDelete" text type="error" @click="handleDelete">
            <n-icon :component="TrashOutline" /> 删除
          </n-button>
        </n-space>

        <!-- 回复输入框 -->
        <div v-if="showReplyInput" class="reply-input-wrapper">
          <CommentInput ref="replyInputRef" :placeholder="`回复 @${comment.nickname}`" submit-text="回复" show-cancel
            @submit="handleSubmitReply" @cancel="showReplyInput = false" />
        </div>
      </div>
    </n-space>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { NSpace, NAvatar, NText, NButton, NIcon } from 'naive-ui'
import { HeartOutline, Heart, ChatbubbleOutline, TrashOutline } from '@vicons/ionicons5'
import { formatDateTime } from '@/utils/common'
import CommentInput from './CommentInput.vue'

const props = defineProps({
  comment: {
    type: Object,
    required: true
  },
  articleId: {
    type: [String, Number],
    required: true
  },
  isLiked: {
    type: Boolean,
    default: false
  },
  canDelete: {
    type: Boolean,
    default: false
  },
  isChild: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['reply', 'like', 'delete'])

const showReplyInput = ref(false)
const replyInputRef = ref(null)

const toggleReplyInput = () => {
  showReplyInput.value = !showReplyInput.value
}

const handleLike = () => {
  emit('like', props.comment.id)
}

const handleDelete = () => {
  emit('delete', props.comment.id)
}

const handleSubmitReply = (content) => {
  emit('reply', {
    content,
    parentId: props.comment.id,
    replyUserId: props.comment.userId
  })
  showReplyInput.value = false
  if (replyInputRef.value) {
    replyInputRef.value.clear()
  }
}

const formatDate = (date) => {
  return formatDateTime(date)
}

// 简单的Markdown渲染（支持基本格式）
const renderContent = (content) => {
  if (!content) return ''

  // 转义HTML
  let html = content
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')

  // 支持基本Markdown格式
  html = html
    .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>') // 粗体
    .replace(/\*(.+?)\*/g, '<em>$1</em>') // 斜体
    .replace(/`(.+?)`/g, '<code>$1</code>') // 行内代码
    .replace(/\n/g, '<br>') // 换行

  return html
}
</script>

<style scoped>
.comment-item {
  width: 100%;
}

.comment-item.is-child {
  padding: 12px 0;
}

.comment-content {
  flex: 1;
}

.comment-header {
  margin-bottom: 8px;
}

.comment-time {
  font-size: 12px;
  margin-left: 10px;
}

.comment-text {
  margin-bottom: 8px;
  line-height: 1.6;
  word-wrap: break-word;
}

.comment-text :deep(code) {
  background-color: var(--surface-hover);
  padding: 2px 6px;
  border-radius: var(--radius-xs);
  font-family: var(--font-family-mono);
  font-size: 0.9em;
}

.comment-actions {
  margin-top: 8px;
}

.comment-actions .n-button.liked {
  color: var(--color-error);
}

.reply-input-wrapper {
  margin-top: 12px;
  padding: 12px;
  background-color: var(--surface-hover);
  border-radius: var(--radius-md);
}
</style>
