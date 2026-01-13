<template>
  <div class="tag-page">
    <n-card :title="`标签: ${tagName}`">
      <n-spin :show="loading">
        <n-list v-if="articles.length > 0">
          <n-list-item v-for="article in articles" :key="article.id">
            <ArticleCard :article="article" @click="goToArticle(article.id)" />
          </n-list-item>
        </n-list>
        <n-empty v-else description="该标签下暂无文章" />
      </n-spin>

      <n-pagination
        v-if="totalPages > 1"
        v-model:page="currentPage"
        :page-count="totalPages"
        style="margin-top: 20px; justify-content: center"
        @update:page="loadArticles"
      />
    </n-card>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getArticleList } from '@/api/article'
import ArticleCard from '@/components/ArticleCard.vue'
import { NCard, NList, NListItem, NEmpty, NSpin, NPagination } from 'naive-ui'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const articles = ref([])
const tagName = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const totalPages = ref(0)

const loadArticles = async () => {
  loading.value = true
  try {
    const res = await getArticleList({
      current: currentPage.value,
      size: pageSize.value,
      tagId: route.params.id
    })
    articles.value = res.data.records
    totalPages.value = Math.ceil(res.data.total / pageSize.value)
    tagName.value = `标签 ${route.params.id}`
  } finally {
    loading.value = false
  }
}

const goToArticle = (id) => {
  router.push(`/article/${id}`)
}

watch(() => route.params.id, () => {
  currentPage.value = 1
  loadArticles()
})

onMounted(() => {
  loadArticles()
})
</script>

<style scoped>
.tag-page {
  max-width: 900px;
  margin: 0 auto;
}
</style>
