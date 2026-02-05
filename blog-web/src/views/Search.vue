<template>
  <div class="search-page">
    <n-card class="search-card panel-card">
      <template #header>
        <n-space :size="12" align="center">
          <n-icon :component="SearchOutline" size="24" color="var(--color-primary)" />
          <span class="search-title">搜索文章</span>
        </n-space>
      </template>

      <!-- 搜索表单 -->
      <n-form ref="formRef" :model="searchForm" label-placement="left" label-width="80">
        <n-grid :cols="2" :x-gap="20">
          <n-gi>
            <n-form-item label="标题" path="title">
              <n-input v-model:value="searchForm.title" placeholder="请输入文章标题" clearable />
            </n-form-item>
          </n-gi>
          <n-gi>
            <n-form-item label="作者" path="authorName">
              <n-input v-model:value="searchForm.authorName" placeholder="请输入作者名称" clearable />
            </n-form-item>
          </n-gi>
          <n-gi>
            <n-form-item label="分类" path="categoryName">
              <n-input v-model:value="searchForm.categoryName" placeholder="请输入分类名称" clearable />
            </n-form-item>
          </n-gi>
          <n-gi>
            <n-form-item label="标签" path="tagName">
              <n-input v-model:value="searchForm.tagName" placeholder="请输入标签名称" clearable />
            </n-form-item>
          </n-gi>
        </n-grid>

        <n-space justify="end" class="search-actions">
          <n-button @click="handleReset">重置</n-button>
          <n-button type="primary" @click="handleSearch" :loading="loading">
            <template #icon>
              <n-icon :component="SearchOutline" />
            </template>
            搜索
          </n-button>
        </n-space>
      </n-form>
    </n-card>

    <!-- 搜索结果 -->
    <n-card class="result-card panel-card">
      <template #header>
        <n-space justify="space-between" align="center">
          <span>搜索结果</span>
          <n-text depth="3" v-if="total > 0">共找到 {{ total }} 篇文章</n-text>
        </n-space>
      </template>

      <n-spin :show="loading">
        <n-list v-if="articles.length > 0">
          <n-list-item v-for="article in articles" :key="article.id">
            <ArticleCard :article="article" @click="goToDetail(article.id)" />
          </n-list-item>
        </n-list>
        <n-empty v-else description="暂无搜索结果" />
      </n-spin>

      <n-pagination v-if="totalPages > 1" v-model:page="currentPage" :page-count="totalPages" class="search-pagination"
        @update:page="handlePageChange" />
    </n-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useArticleList, useArticleNavigation } from '@/composables/business/useArticle'
import ArticleCard from '@/components/article/ArticleCard.vue'
import {
  NCard,
  NForm,
  NFormItem,
  NGrid,
  NGi,
  NInput,
  NButton,
  NSpace,
  NList,
  NListItem,
  NSpin,
  NEmpty,
  NPagination,
  NText,
  NIcon
} from 'naive-ui'
import { SearchOutline } from '@vicons/ionicons5'
import { PAGINATION_CONFIG } from '@/config/constants'

const route = useRoute()
const { goToDetail } = useArticleNavigation()

// 搜索表单
const searchForm = reactive({
  title: '',
  authorName: '',
  categoryName: '',
  tagName: ''
})

// 使用文章列表组合式函数
const {
  loading,
  articles,
  currentPage,
  total,
  totalPages,
  loadArticles,
  changePage
} = useArticleList({
  pageSize: PAGINATION_CONFIG.DEFAULT_PAGE_SIZE
})

// 执行搜索
const handleSearch = async () => {
  currentPage.value = 1
  await loadArticles(searchForm)
}

// 重置搜索
const handleReset = () => {
  searchForm.title = ''
  searchForm.authorName = ''
  searchForm.categoryName = ''
  searchForm.tagName = ''
  currentPage.value = 1
  loadArticles()
}

// 页码变化
const handlePageChange = async (page) => {
  await changePage(page, searchForm)
}

onMounted(() => {
  // 如果URL中有搜索关键词，自动填充并搜索
  if (route.query.keyword) {
    searchForm.title = route.query.keyword
    handleSearch()
  } else {
    loadArticles()
  }
})
</script>

<style scoped>
.search-page {
  max-width: 1200px;
  margin: 0 auto;
}

.search-title {
  font-weight: 600;
  font-size: 18px;
  color: var(--text-primary);
}

.search-actions {
  margin-top: 10px;
}

.result-card {
  margin-top: 20px;
}

.search-pagination {
  margin-top: 20px;
  justify-content: center;
}

.search-card,
.result-card {
  width: 100%;
}
</style>
