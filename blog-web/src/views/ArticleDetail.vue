<template>
  <div class="article-detail">
    <n-spin :show="loading">
      <n-card v-if="article" class="panel-card">
        <!-- 文章头部（包含标签） -->
        <ArticleHeader :article="article" :liking="liking" :collecting="collecting" @like="handleLike"
          @collect="handleCollect" />

        <n-divider />

        <!-- 文章内容 -->
        <MarkdownRenderer v-if="article.content" :content="article.content" />
      </n-card>

      <!-- 评论区 -->
      <ArticleCommentContainer :article-id="route.params.id" />
    </n-spin>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useArticleDetail, useArticleInteraction } from '@/composables/business/useArticle'
import ArticleHeader from '@/components/article/ArticleHeader.vue'
import ArticleCommentContainer from '@/components/article/ArticleCommentContainer.vue'
import MarkdownRenderer from '@/components/article/MarkdownRenderer.vue'
import { showWarning } from '@/utils/common'
import { NCard, NDivider, NSpin } from 'naive-ui'

const route = useRoute()

// 使用文章详情组合式函数
const { loading, article, loadArticle } = useArticleDetail()

// 使用文章交互组合式函数
const { liking, collecting } = useArticleInteraction()

const handleLike = async () => {
  showWarning('点赞功能需要登录，请前往权限测试项目体验')
}

const handleCollect = async () => {
  showWarning('收藏功能需要登录，请前往权限测试项目体验')
}

onMounted(() => {
  loadArticle(route.params.id)
})
</script>

<style scoped>
.article-detail {
  max-width: 900px;
  margin: 0 auto;
}
</style>
