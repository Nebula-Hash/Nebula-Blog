<template>
  <div class="article-detail">
    <n-spin :show="loading">
      <n-card v-if="article">
        <div class="article-header">
          <h1 class="article-title">{{ article.title }}</h1>
          <div class="article-meta">
            <n-space :size="10">
              <n-avatar round :size="32" :src="article.authorAvatar" />
              <div>
                <div>{{ article.authorNickname }}</div>
                <n-text depth="3" style="font-size: 12px">
                  {{ formatDate(article.createTime) }} · {{ article.viewCount }} 阅读
                </n-text>
              </div>
            </n-space>
            <n-space :size="15">
              <n-button :type="article.isLiked ? 'primary' : 'default'" @click="handleLike">
                <template #icon>
                  <n-icon :component="article.isLiked ? Heart : HeartOutline" />
                </template>
                {{ article.likeCount }}
              </n-button>
              <n-button :type="article.isCollected ? 'primary' : 'default'" @click="handleCollect">
                <template #icon>
                  <n-icon :component="article.isCollected ? Star : StarOutline" />
                </template>
                {{ article.collectCount }}
              </n-button>
            </n-space>
          </div>
        </div>

        <n-divider />

        <div class="article-content" v-html="safeArticleContent"></div>

        <n-divider />

        <div class="article-tags">
          <n-space>
            <n-tag v-for="tag in article.tags" :key="tag.id" :bordered="false" round>
              {{ tag.tagName }}
            </n-tag>
          </n-space>
        </div>
      </n-card>

      <!-- 评论区 -->
      <n-card style="margin-top: 20px" class="comment-card">
        <template #header>
          <n-space :size="8" align="center">
            <n-icon :component="ChatbubbleEllipsesOutline" size="20" color="#2ADB5C" />
            <span style="font-weight: 600; font-size: 16px;">评论</span>
          </n-space>
        </template>
        <!-- 发表评论 -->
        <n-input v-model:value="commentContent" type="textarea" placeholder="写下你的评论..." :rows="3"
          style="margin-bottom: 10px" />
        <n-button type="primary" @click="handlePublishComment" :loading="commentLoading">
          发表评论
        </n-button>

        <!-- 评论列表 -->
        <n-list v-if="comments.length > 0" style="margin-top: 20px">
          <n-list-item v-for="comment in comments" :key="comment.id">
            <CommentItem :comment="comment" @reply="handleReply" @like="handleCommentLike" />
          </n-list-item>
        </n-list>
        <n-empty v-else description="暂无评论" style="margin-top: 20px" />
      </n-card>
    </n-spin>

    <!-- 回复对话框 -->
    <n-modal v-model:show="showReplyDialog" preset="card" title="回复评论" style="width: 500px">
      <n-input v-model:value="replyContent" type="textarea" placeholder="写下你的回复..." :rows="3" />
      <template #footer>
        <n-space justify="end">
          <n-button @click="showReplyDialog = false">取消</n-button>
          <n-button type="primary" @click="handlePublishReply" :loading="replyLoading">
            发布
          </n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getArticleDetail, likeArticle, collectArticle } from '@/api/article'
import { getCommentList, publishComment, likeComment } from '@/api/comment'
import CommentItem from '@/components/CommentItem.vue'
import { formatDateTime, showSuccess, showWarning, checkLogin, validateNotEmpty } from '@/utils/common'
import { escapeHtml } from '@/utils/security'
import { PAGINATION_CONFIG } from '@/config/constants'
import {
  NCard,
  NSpace,
  NAvatar,
  NText,
  NButton,
  NDivider,
  NTag,
  NInput,
  NList,
  NListItem,
  NEmpty,
  NSpin,
  NModal,
  NIcon
} from 'naive-ui'
import {
  HeartOutline,
  Heart,
  StarOutline,
  Star,
  ChatbubbleEllipsesOutline
} from '@vicons/ionicons5'

const route = useRoute()
const userStore = useUserStore()

const loading = ref(false)
const article = ref(null)
const comments = ref([])
const commentContent = ref('')
const commentLoading = ref(false)

const showReplyDialog = ref(false)
const replyContent = ref('')
const replyLoading = ref(false)
const replyTarget = ref(null)

// 创建 AbortController 用于取消请求
const abortController = new AbortController()

const loadArticle = async () => {
  loading.value = true
  try {
    const res = await getArticleDetail(route.params.id, { signal: abortController.signal })
    article.value = res.data
  } catch (error) {
    // 如果是取消请求，不处理错误
    if (error.name === 'AbortError' || error.name === 'CanceledError') {
      return
    }
    throw error
  } finally {
    loading.value = false
  }
}

// 安全的文章内容（XSS 防护）
const safeArticleContent = computed(() => {
  if (!article.value) return ''
  // 如果后端已经返回了 htmlContent，说明已经过处理，直接使用
  // 否则对 content 进行转义
  return article.value.htmlContent || escapeHtml(article.value.content || '')
})

const loadComments = async () => {
  try {
    const res = await getCommentList(route.params.id, {
      current: 1,
      size: PAGINATION_CONFIG.COMMENT_PAGE_SIZE,
      signal: abortController.signal
    })
    comments.value = res.data.records
  } catch (error) {
    // 如果是取消请求，不处理错误
    if (error.name === 'AbortError' || error.name === 'CanceledError') {
      return
    }
    throw error
  }
}

const handleLike = async () => {
  if (!checkLogin(userStore)) return

  await likeArticle(route.params.id)
  article.value.isLiked = !article.value.isLiked
  article.value.likeCount += article.value.isLiked ? 1 : -1
  showSuccess(article.value.isLiked ? '点赞成功' : '取消点赞')
}

const handleCollect = async () => {
  if (!checkLogin(userStore)) return

  await collectArticle(route.params.id)
  article.value.isCollected = !article.value.isCollected
  article.value.collectCount += article.value.isCollected ? 1 : -1
  showSuccess(article.value.isCollected ? '收藏成功' : '取消收藏')
}

const handlePublishComment = async () => {
  if (!checkLogin(userStore)) return
  if (!validateNotEmpty(commentContent.value, '请输入评论内容')) return

  commentLoading.value = true
  try {
    await publishComment({
      articleId: route.params.id,
      content: escapeHtml(commentContent.value)
    })
    showSuccess('评论成功')
    commentContent.value = ''
    loadComments()
  } finally {
    commentLoading.value = false
  }
}

const handleReply = (comment) => {
  if (!checkLogin(userStore)) return

  replyTarget.value = comment
  showReplyDialog.value = true
}

const handlePublishReply = async () => {
  if (!validateNotEmpty(replyContent.value, '请输入回复内容')) return

  replyLoading.value = true
  try {
    await publishComment({
      articleId: route.params.id,
      parentId: replyTarget.value.id,
      replyUserId: replyTarget.value.userId,
      content: escapeHtml(replyContent.value)
    })
    showSuccess('回复成功')
    replyContent.value = ''
    showReplyDialog.value = false
    loadComments()
  } finally {
    replyLoading.value = false
  }
}

const handleCommentLike = async (commentId) => {
  if (!checkLogin(userStore)) return

  await likeComment(commentId)
  showSuccess('点赞成功')
  loadComments()
}

const formatDate = (date) => {
  return formatDateTime(date)
}

onMounted(() => {
  loadArticle()
  loadComments()
})

// 组件卸载时取消所有进行中的请求
onUnmounted(() => {
  abortController.abort()
})
</script>

<style scoped>
.article-detail {
  max-width: 900px;
  margin: 0 auto;
}

.article-header {
  margin-bottom: 20px;
}

.article-title {
  font-size: 32px;
  font-weight: 700;
  margin: 0 0 20px 0;
  line-height: 1.4;
  color: rgba(255, 255, 255, 0.95);
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
}

.article-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.article-content {
  line-height: 1.8;
  font-size: 16px;
  color: rgba(255, 255, 255, 0.85);
}

.article-content :deep(img) {
  max-width: 100%;
  height: auto;
  border-radius: 8px;
  margin: 20px 0;
}

.article-content :deep(pre) {
  background: #1F1F1F;
  padding: 16px;
  border-radius: 8px;
  overflow-x: auto;
  border-left: 3px solid #2ADB5C;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
}

.article-content :deep(code) {
  background: #1F1F1F;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 14px;
  color: #2ADB5C;
}

.article-tags {
  margin-top: 20px;
}

.comment-card {
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
  border: 1px solid rgba(42, 219, 92, 0.1);
  background: #141517;
}
</style>
