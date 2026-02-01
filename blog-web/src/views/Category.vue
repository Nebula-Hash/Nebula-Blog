<template>
  <div class="category-page">
    <n-card :title="`分类: ${categoryName || '加载中...'}`">
      <n-spin :show="loading">
        <n-list v-if="!loading && articles.length > 0">
          <n-list-item v-for="article in articles" :key="article.id">
            <ArticleCard :article="article" @click="goToArticle(article.id)" />
          </n-list-item>
        </n-list>
        <n-empty v-else-if="!loading && articles.length === 0" description="该分类下暂无文章" />
      </n-spin>

      <n-pagination v-if="total > pageSize" v-model:page="currentPage" :page-count="totalPages" :page-size="pageSize"
        :item-count="total" show-size-picker :page-sizes="[10, 20, 30, 50]"
        style="margin-top: 20px; justify-content: center" @update:page="handlePageChange"
        @update:page-size="handlePageSizeChange" />
    </n-card>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getArticleList } from '@/api/article'
import { getCategoryDetail } from '@/api/category'
import { ArticleCard } from '@/components/article'
import { NCard, NList, NListItem, NEmpty, NSpin, NPagination } from 'naive-ui'
import { PAGINATION_CONFIG } from '@/config/constants'
import { createErrorHandler } from '@/utils/errorHandler'
import { showError } from '@/utils/common'

const route = useRoute()
const router = useRouter()
const errorHandler = createErrorHandler('Category')

const loading = ref(false)
const articles = ref([])
const categoryName = ref('')
const currentPage = ref(1)
const pageSize = ref(PAGINATION_CONFIG.DEFAULT_PAGE_SIZE)
const total = ref(0)
const totalPages = ref(0)

/**
 * 获取分类名称
 */
const fetchCategoryName = async (categoryId) => {
  try {
    const res = await getCategoryDetail(categoryId)
    if (res.code === 200 && res.data) {
      categoryName.value = res.data.categoryName
    } else {
      categoryName.value = '未知分类'
    }
  } catch (error) {
    errorHandler.handleLoad(error, '分类名称', true)
    categoryName.value = '未知分类'
  }
}

/**
 * 加载文章列表
 */
const loadArticles = async () => {
  const categoryId = route.params.id
  if (!categoryId) {
    showError('分类ID不能为空')
    return
  }

  loading.value = true
  try {
    // 并行获取分类名称和文章列表
    const [articlesRes] = await Promise.all([
      getArticleList({
        current: currentPage.value,
        size: pageSize.value,
        categoryId
      }),
      categoryName.value ? Promise.resolve() : fetchCategoryName(categoryId)
    ])

    if (articlesRes.code === 200 && articlesRes.data) {
      articles.value = articlesRes.data.records || []
      total.value = articlesRes.data.total || 0
      totalPages.value = articlesRes.data.pages || 0
    } else {
      showError(articlesRes.message || '加载文章列表失败')
      articles.value = []
    }
  } catch (error) {
    errorHandler.handleLoad(error, '文章列表')
    articles.value = []
  } finally {
    loading.value = false
  }
}

/**
 * 跳转到文章详情
 */
const goToArticle = (id) => {
  router.push(`/article/${id}`)
}

/**
 * 页码变化处理
 */
const handlePageChange = (page) => {
  currentPage.value = page
  loadArticles()
  // 滚动到顶部
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

/**
 * 每页数量变化处理
 */
const handlePageSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  loadArticles()
}

// 监听路由参数变化
watch(() => route.params.id, (newId, oldId) => {
  if (newId && newId !== oldId) {
    currentPage.value = 1
    categoryName.value = ''
    loadArticles()
  }
}, { immediate: false })

onMounted(() => {
  loadArticles()
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
