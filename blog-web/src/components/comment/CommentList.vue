<template>
  <div class="comment-list">
    <!-- 评论总数 -->
    <div class="comment-header">
      <n-text strong class="comment-total">
        评论 {{ total }}
      </n-text>
    </div>

    <!-- 发布评论输入框 -->
    <div class="publish-comment">
      <CommentInput placeholder="写下你的评论..." submit-text="发布评论" @submit="handlePublish" />
    </div>

    <!-- 评论列表 -->
    <div class="comments-container">
      <!-- 加载中骨架屏 -->
      <CommentSkeleton v-if="loading && comments.length === 0" :count="3" />

      <!-- 评论树 -->
      <CommentTree v-else-if="comments.length > 0" :comments="comments" :liked-comments="likedComments"
        :current-user-id="currentUserId" :article-id="props.articleId" @reply="handleReply" @like="handleLike"
        @delete="handleDelete" @load-more-replies="handleLoadMoreReplies" />

      <!-- 空状态 -->
      <n-empty v-else description="暂无评论，快来发表第一条评论吧！" class="comment-empty" />

      <!-- 加载更多触发器 -->
      <div v-if="hasMore" ref="loadMoreTrigger" class="load-more">
        <n-spin v-if="loading" size="small" />
        <n-text v-else depth="3">加载更多...</n-text>
      </div>

      <!-- 没有更多 -->
      <div v-else-if="comments.length > 0" class="no-more">
        <n-text depth="3">没有更多评论了</n-text>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, watch } from 'vue'
import { NText, NSpin, NEmpty, useMessage } from 'naive-ui'
import { useCommentStore } from '@/stores'
import { useInfiniteScroll } from '@/composables/helper/useInfiniteScroll'
import { debounce } from '@/utils/performance'
import { asyncWrapper } from '@/utils/errorHandler'
import CommentInput from './CommentInput.vue'
import CommentTree from './CommentTree.vue'
import CommentSkeleton from './CommentSkeleton.vue'

const props = defineProps({
  articleId: {
    type: String,
    required: true
  }
})

const message = useMessage()
const commentStore = useCommentStore()


// 从store获取数据
const comments = computed(() => commentStore.getArticleComments(props.articleId))
const likedComments = computed(() => commentStore.likedComments)
const loading = computed(() => commentStore.loading[props.articleId] || false)
const total = computed(() => commentStore.getCommentTotal(props.articleId))
const hasMore = computed(() => commentStore.hasMoreComments(props.articleId))
const currentUserId = computed(() => null) // 未登录状态

// 无限滚动
const { targetRef: loadMoreTrigger } = useInfiniteScroll(
  async () => {
    if (!hasMore.value || loading.value) return false

    const result = await asyncWrapper(
      async () => {
        await commentStore.loadMoreComments(props.articleId)
        return hasMore.value
      },
      { operation: '加载评论', silent: false }
    )

    return result !== null ? result : false
  },
  {
    rootMargin: '100px',
    threshold: 0.01
  }
)

// 发布评论 - 已禁用
const handlePublish = async (content) => {
  message.warning('评论功能需要登录，请前往权限测试项目体验')
}

// 回复评论 - 已禁用
const handleReply = debounce(async (replyData) => {
  message.warning('回复功能需要登录，请前往权限测试项目体验')
}, 500)

// 加载更多回复
const handleLoadMoreReplies = async ({ rootId, currentPage }) => {
  await asyncWrapper(
    async () => {
      await commentStore.loadMoreReplies(rootId, props.articleId, currentPage)
    },
    { operation: '加载回复' }
  )
}

// 点赞评论 - 已禁用
const handleLike = debounce(async (commentId) => {
  message.warning('点赞功能需要登录，请前往权限测试项目体验')
}, 300)

// 删除评论 - 已禁用
const handleDelete = async (commentId) => {
  message.warning('删除功能需要登录，请前往权限测试项目体验')
}

// 初始化加载评论
onMounted(async () => {
  await asyncWrapper(
    async () => {
      await commentStore.loadComments(props.articleId)
    },
    { operation: '加载评论' }
  )
})

// 监听文章ID变化，重新加载评论
watch(() => props.articleId, async (newId) => {
  if (newId) {
    await asyncWrapper(
      async () => {
        await commentStore.loadComments(newId)
      },
      { operation: '加载评论' }
    )
  }
})
</script>

<style scoped>
.comment-list {
  width: 100%;
  max-width: 800px;
  margin: 0 auto;
}

.comment-header {
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--border-secondary);
}

.comment-total {
  font-size: 18px;
}

.publish-comment {
  margin-bottom: 30px;
  padding: 20px;
  background-color: var(--surface-hover);
  border-radius: var(--radius-md);
  border: 1px solid var(--border-secondary);
}

.comments-container {
  margin-top: 20px;
}

.load-more {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px;
  margin-top: 20px;
}

.no-more {
  display: flex;
  justify-content: center;
  padding: 20px;
  margin-top: 20px;
}

.comment-empty {
  margin: 40px 0;
}
</style>
