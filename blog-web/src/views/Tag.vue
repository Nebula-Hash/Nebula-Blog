<template>
  <div class="tag-page">
    <n-card :title="`标签: ${tagName || '加载中...'}`">
      <n-spin :show="loading">
        <n-list v-if="!loading && articles.length > 0">
          <n-list-item v-for="article in articles" :key="article.id">
            <ArticleCard :article="article" @click="goToDetail(article.id)" />
          </n-list-item>
        </n-list>
        <n-empty v-else-if="!loading && articles.length === 0" description="该标签下暂无文章" />
      </n-spin>

      <n-pagination v-if="totalPages > 1" v-model:page="currentPage" :page-count="totalPages"
        style="margin-top: 20px; justify-content: center" @update:page="handlePageChange" />
    </n-card>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useArticleList, useArticleNavigation } from '@/composables/business/useArticle'
import { useCacheStore } from '@/stores'
import ArticleCard from '@/components/article/ArticleCard.vue'
import { NCard, NList, NListItem, NEmpty, NSpin, NPagination } from 'naive-ui'
import { PAGINATION_CONFIG } from '@/config/constants'

const route = useRoute()
const cacheStore = useCacheStore()

// 使用文章列表组合式函数
const {
  loading,
  articles,
  currentPage,
  totalPages,
  loadArticles,
  changePage
} = useArticleList({
  pageSize: PAGINATION_CONFIG.DEFAULT_PAGE_SIZE
})

// 使用文章导航组合式函数
const { goToDetail } = useArticleNavigation()

const tagName = ref('')

/**
 * 获取标签名称
 */
const fetchTagName = async (tagId) => {
  try {
    const tags = await cacheStore.fetchTagList()
    const tag = tags.find(t => t.id === parseInt(tagId))
    tagName.value = tag ? tag.tagName : '未知标签'
  } catch (error) {
    console.error('[Tag] 获取标签名称失败:', error)
    tagName.value = '未知标签'
  }
}

/**
 * 加载标签文章
 */
const loadTagArticles = async () => {
  const tagId = route.params.id
  if (!tagId) return

  // 并行获取标签名称和文章列表
  await Promise.all([
    loadArticles({ tagId }),
    tagName.value ? Promise.resolve() : fetchTagName(tagId)
  ])
}

/**
 * 页码变化处理
 */
const handlePageChange = async (page) => {
  await changePage(page, { tagId: route.params.id })
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

// 监听路由参数变化
watch(() => route.params.id, (newId, oldId) => {
  if (newId && newId !== oldId) {
    currentPage.value = 1
    tagName.value = ''
    loadTagArticles()
  }
}, { immediate: false })

onMounted(() => {
  loadTagArticles()
})
</script>

<style scoped>
.tag-page {
  max-width: 900px;
  margin: 0 auto;
  padding: 20px;
}

@media (max-width: 768px) {
  .tag-page {
    padding: 10px;
  }
}
</style>
