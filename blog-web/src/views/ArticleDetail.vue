<template>
  <div class="article-detail">
    <n-spin :show="loading">
      <n-card v-if="article">
        <!-- 文章头部 -->
        <ArticleHeader :article="article" :liking="liking" :collecting="collecting" @like="handleLike"
          @collect="handleCollect" />

        <n-divider />

        <!-- 文章内容 -->
        <MarkdownRenderer v-if="article.content" :content="article.content" />

        <n-divider />

        <!-- 文章标签 -->
        <ArticleTags :tags="article.tags" />
      </n-card>

      <!-- 评论区 -->
      <ArticleCommentSection :comments="comments" :publishing="commentLoading" @publish="handlePublishComment"
        @reply="handleReply" @like="handleCommentLike" />
    </n-spin>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useArticleDetail, useArticleInteraction } from '@/composables/useArticle'
import { useAbortController } from '@/composables/useAbortController'
import { asyncWrapper } from '@/utils/errorHandler'
import { getCommentList, publishComment, likeComment } from '@/api/comment'
import ArticleHeader from '@/components/article/ArticleHeader.vue'
import ArticleTags from '@/components/article/ArticleTags.vue'
import ArticleCommentSection from '@/components/article/ArticleCommentSection.vue'
import MarkdownRenderer from '@/components/article/MarkdownRenderer.vue'
import { showSuccess, checkLogin, validateNotEmpty } from '@/utils/common'
import { PAGINATION_CONFIG } from '@/config/constants'
import { NCard, NDivider, NSpin } from 'naive-ui'

const route = useRoute()
const userStore = useUserStore()

// 使用文章详情组合式函数
const { loading, article, loadArticle } = useArticleDetail()

// 使用文章交互组合式函数
const { liking, collecting, toggleLike, toggleCollect } = useArticleInteraction()

// 使用请求取消控制器
const { createSignal } = useAbortController()

const comments = ref([])
const commentLoading = ref(false)

const loadComments = async () => {
  return asyncWrapper(
    async () => {
      const res = await getCommentList(route.params.id, {
        current: 1,
        size: PAGINATION_CONFIG.COMMENT_PAGE_SIZE,
        signal: createSignal()
      })
      comments.value = res.data.records
    },
    { operation: '加载评论', silent: false }
  )
}

const handleLike = async () => {
  await toggleLike(route.params.id, article)
}

const handleCollect = async () => {
  await toggleCollect(route.params.id, article)
}

const handlePublishComment = async (content) => {
  if (!checkLogin(userStore)) return
  if (!validateNotEmpty(content, '请输入评论内容')) return

  commentLoading.value = true
  await asyncWrapper(
    async () => {
      await publishComment({
        articleId: route.params.id,
        content
      })
      showSuccess('评论成功')
      await loadComments()
    },
    { operation: '发表评论' }
  )
  commentLoading.value = false
}

const handleReply = async (replyData) => {
  if (!checkLogin(userStore)) return
  if (!validateNotEmpty(replyData.content, '请输入回复内容')) return

  await asyncWrapper(
    async () => {
      await publishComment({
        articleId: route.params.id,
        parentId: replyData.parentId,
        replyUserId: replyData.replyUserId,
        content: replyData.content
      })
      showSuccess('回复成功')
      await loadComments()
    },
    { operation: '发表回复' }
  )
}

const handleCommentLike = async (commentId) => {
  if (!checkLogin(userStore)) return

  await asyncWrapper(
    async () => {
      await likeComment(commentId)
      showSuccess('点赞成功')
      await loadComments()
    },
    { operation: '点赞评论' }
  )
}

onMounted(() => {
  loadArticle(route.params.id)
  loadComments()
})
</script>

<style scoped>
.article-detail {
  max-width: 900px;
  margin: 0 auto;
}
</style>
