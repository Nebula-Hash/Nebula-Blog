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
import { useArticleDetail, useArticleInteraction } from '@/composables/useArticle'
import { useAbortController } from '@/composables/useAbortController'
import { asyncWrapper } from '@/utils/errorHandler'
import { getCommentList } from '@/api/comment'
import ArticleHeader from '@/components/article/ArticleHeader.vue'
import ArticleTags from '@/components/article/ArticleTags.vue'
import ArticleCommentSection from '@/components/article/ArticleCommentSection.vue'
import MarkdownRenderer from '@/components/article/MarkdownRenderer.vue'
import { showWarning } from '@/utils/common'
import { PAGINATION_CONFIG } from '@/config/constants'
import { NCard, NDivider, NSpin } from 'naive-ui'

const route = useRoute()

// 使用文章详情组合式函数
const { loading, article, loadArticle } = useArticleDetail()

// 使用文章交互组合式函数
const { liking, collecting, toggleLike, toggleCollect } = useArticleInteraction()

// 使用请求取消控制器
const { createSignal } = useAbortController()

const comments = ref([])

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
  showWarning('点赞功能需要登录，请前往权限测试项目体验')
}

const handleCollect = async () => {
  showWarning('收藏功能需要登录，请前往权限测试项目体验')
}

const handlePublishComment = async (content) => {
  showWarning('评论功能需要登录，请前往权限测试项目体验')
}

const handleReply = async (replyData) => {
  showWarning('回复功能需要登录，请前往权限测试项目体验')
}

const handleCommentLike = async (commentId) => {
  showWarning('点赞功能需要登录，请前往权限测试项目体验')
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
