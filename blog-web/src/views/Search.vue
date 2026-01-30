<template>
  <div class="search-page">
    <n-card :title="`搜索: ${keyword}`">
      <n-spin :show="loading">
        <n-list v-if="articles.length > 0">
          <n-list-item v-for="article in articles" :key="article.id">
            <ArticleCard :article="article" @click="goToArticle(article.id)" />
          </n-list-item>
        </n-list>
        <n-empty v-else description="没有找到相关文章" />
      </n-spin>

      <n-pagination v-if="totalPages > 1" v-model:page="currentPage" :page-count="totalPages"
        style="margin-top: 20px; justify-content: center" @update:page="loadArticles" />
    </n-card>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getArticleList } from '@/api/article'
import ArticleCard from '@/components/ArticleCard.vue'
import { NCard, NList, NListItem, NEmpty, NSpin, NPagination } from 'naive-ui'
import { PAGINATION_CONFIG } from '@/config/constants'
import { createErrorHandler } from '@/utils/errorHandler'

const route = useRoute()
const router = useRouter()
const errorHandler = createErrorHandler('Search')

const loading = ref(false)
const articles = ref([])
const keyword = ref('')
const currentPage = ref(1)
const pageSize = ref(PAGINATION_CONFIG.DEFAULT_PAGE_SIZE)
const totalPages = ref(0)

const loadArticles = async () => {
  loading.value = true
  keyword.value = route.query.keyword || ''
  try {
    const res = await getArticleList({
      current: currentPage.value,
      size: pageSize.value,
      keyword: keyword.value
    })
    articles.value = res.data.records
    totalPages.value = Math.ceil(res.data.total / pageSize.value)
  } catch (error) {
    errorHandler.handleLoad(error, '搜索结果')
    articles.value = []
  } finally {
    loading.value = false
  }
}

const goToArticle = (id) => {
  router.push(`/article/${id}`)
}

watch(() => route.query.keyword, () => {
  currentPage.value = 1
  loadArticles()
})

onMounted(() => {
  loadArticles()
})
</script>

<style scoped>
.search-page {
  max-width: 900px;
  margin: 0 auto;
}
</style>
