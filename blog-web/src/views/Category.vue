<template>
  <div class="category-page">
    <n-card :title="`分类: ${categoryName || '加载中...'}`">
      <n-spin :show="loading">
        <n-list v-if="!loading && articles.length > 0">
          <n-list-item v-for="article in articles" :key="article.id">
            <ArticleCard :article="article" @click="goToDetail(article.id)" />
          </n-list-item>
        </n-list>
        <n-empty v-else-if="!loading && articles.length === 0" description="该分类下暂无文章" />
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

const categoryName = ref('')

/**
 * 获取分类名称
 */
const fetchCategoryName = async (categoryId) => {
  try {
    const categories = await cacheStore.fetchCategoryList()
    const category = categories.find(c => c.id === parseInt(categoryId))
    categoryName.value = category ? category.categoryName : '未知分类'
  } catch (error) {
    console.error('[Category] 获取分类名称失败:', error)
    categoryName.value = '未知分类'
  }
}

/**
 * 加载分类文章
 */
const loadCategoryArticles = async () => {
  const categoryId = route.params.id
  if (!categoryId) return

  // 并行获取分类名称和文章列表
  await Promise.all([
    loadArticles({ categoryId }),
    categoryName.value ? Promise.resolve() : fetchCategoryName(categoryId)
  ])
}

/**
 * 页码变化处理
 */
const handlePageChange = async (page) => {
  await changePage(page, { categoryId: route.params.id })
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

// 监听路由参数变化
watch(() => route.params.id, (newId, oldId) => {
  if (newId && newId !== oldId) {
    currentPage.value = 1
    categoryName.value = ''
    loadCategoryArticles()
  }
}, { immediate: false })

onMounted(() => {
  loadCategoryArticles()
})
</script>

<style scoped>
.category-page {
  max-width: 900px;
  margin: 0 auto;
  padding: 20px;
}

@media (max-width: 768px) {
  .category-page {
    padding: 10px;
  }
}
</style>
