<template>
  <div class="comment-tree">
    <div v-for="comment in comments" :key="comment.id" class="tree-node">
      <CommentItem :comment="comment" :is-liked="isCommentLiked(comment.id)" :can-delete="canDeleteComment(comment)"
        :article-id="articleId" @reply="handleReply" @like="handleLike" @delete="handleDelete" />

      <!-- 子评论列表 -->
      <div v-if="comment.children && comment.children.length > 0" class="children-comments">
        <CommentItem v-for="child in displayChildren(comment)" :key="child.id" :comment="child"
          :is-liked="isCommentLiked(child.id)" :can-delete="canDeleteComment(child)" :article-id="articleId"
          :is-child="true" @reply="handleReply" @like="handleLike" @delete="handleDelete" />

        <!-- 加载更多回复按钮 -->
        <div v-if="shouldShowLoadMore(comment)" class="load-more-replies">
          <n-button text type="primary" size="small" :loading="replyLoading[comment.id]"
            @click="handleLoadMoreReplies(comment)">
            <n-icon :component="ChevronDownOutline" />
            查看更多回复 ({{ comment.replyCount - comment.children.length }})
          </n-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { NButton, NIcon } from 'naive-ui'
import { ChevronDownOutline } from '@vicons/ionicons5'
import { useCommentStore } from '@/stores'
import CommentItem from './CommentItem.vue'

const props = defineProps({
  comments: {
    type: Array,
    required: true
  },
  articleId: {
    type: [String, Number],
    required: true
  },
  collapseThreshold: {
    type: Number,
    default: 3
  },
  likedComments: {
    type: Object,
    default: () => ({})
  },
  currentUserId: {
    type: [String, Number],
    default: null
  }
})

const emit = defineEmits(['reply', 'like', 'delete', 'loadMoreReplies'])

const commentStore = useCommentStore()
const replyLoading = computed(() => commentStore.replyLoading)

// 显示的子评论（处理折叠逻辑）
const displayChildren = (comment) => {
  if (!comment.children) return []
  // 后端已经处理了分页，前端直接显示所有已加载的子评论
  return comment.children
}

// 是否显示"加载更多回复"按钮
const shouldShowLoadMore = (comment) => {
  if (!comment.replyCount || !comment.children) return false
  // 如果回复总数大于已加载的子评论数，显示加载更多按钮
  return comment.replyCount > comment.children.length
}

// 检查评论是否被点赞
const isCommentLiked = (commentId) => {
  return !!props.likedComments[commentId]
}

// 检查是否可以删除评论
const canDeleteComment = (comment) => {
  return props.currentUserId && comment.userId === props.currentUserId
}

// 处理回复
const handleReply = (replyData) => {
  emit('reply', replyData)
}

// 处理点赞
const handleLike = (commentId) => {
  emit('like', commentId)
}

// 处理删除
const handleDelete = (commentId) => {
  emit('delete', commentId)
}

// 处理加载更多回复
const handleLoadMoreReplies = (comment) => {
  const currentPage = Math.ceil(comment.children.length / 10)
  emit('loadMoreReplies', {
    rootId: comment.id,
    currentPage
  })
}
</script>

<style scoped>
.comment-tree {
  width: 100%;
}

.tree-node {
  margin-bottom: 20px;
}

.children-comments {
  margin-top: 15px;
  margin-left: 55px;
  padding-left: 20px;
  border-left: 2px solid rgba(42, 219, 92, 0.2);
}

.load-more-replies {
  margin-top: 12px;
  padding: 8px 0;
}
</style>
