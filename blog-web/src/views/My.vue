<template>
  <div class="my-page">
    <n-card class="my-card">
      <template #header>
        <n-space :size="8" align="center">
          <n-icon :component="DocumentTextOutline" size="20" color="#2ADB5C"/>
          <span style="font-weight: 600; font-size: 16px;">我的文章</span>
        </n-space>
      </template>
      <template #header-extra>
        <n-button type="primary" @click="router.push('/write')">
          <template #icon>
            <n-icon :component="CreateOutline"/>
          </template>
          写文章
        </n-button>
      </template>

      <n-spin :show="loading">
        <n-list v-if="articles.length > 0">
          <n-list-item v-for="article in articles" :key="article.id">
            <template #prefix>
              <n-tag v-if="article.isDraft" type="warning">草稿</n-tag>
              <n-tag v-else-if="article.isTop" type="error">置顶</n-tag>
              <n-tag v-else type="success">已发布</n-tag>
            </template>
            <n-thing :title="article.title" @click="goToArticle(article.id)" style="cursor: pointer">
              <template #description>
                <n-space>
                  <n-text depth="3">{{ formatDate(article.createTime) }}</n-text>
                  <n-text depth="3">浏览: {{ article.viewCount }}</n-text>
                  <n-text depth="3">点赞: {{ article.likeCount }}</n-text>
                  <n-text depth="3">评论: {{ article.commentCount }}</n-text>
                </n-space>
              </template>
            </n-thing>
            <template #suffix>
              <n-space>
                <n-button size="small" @click="handleEdit(article)">编辑</n-button>
                <n-button size="small" type="error" @click="handleDelete(article.id)">删除</n-button>
              </n-space>
            </template>
          </n-list-item>
        </n-list>
        <n-empty v-else description="还没有文章,快去写一篇吧!"/>
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
import {ref, onMounted} from 'vue'
import {useRouter} from 'vue-router'
import {getMyArticles, deleteArticle} from '@/api/article'
import {
  NCard,
  NButton,
  NList,
  NListItem,
  NTag,
  NThing,
  NSpace,
  NText,
  NEmpty,
  NSpin,
  NPagination,
  NIcon
} from 'naive-ui'
import {
  CreateOutline,
  DocumentTextOutline
} from '@vicons/ionicons5'

const router = useRouter()

const loading = ref(false)
const articles = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const totalPages = ref(0)

const loadArticles = async () => {
  loading.value = true
  try {
    const res = await getMyArticles({
      current: currentPage.value,
      size: pageSize.value
    })
    articles.value = res.data.records
    totalPages.value = Math.ceil(res.data.total / pageSize.value)
  } finally {
    loading.value = false
  }
}

const goToArticle = (id) => {
  router.push(`/article/${id}`)
}

const handleEdit = (article) => {
  // TODO: 编辑功能待实现
  window.$message.info('编辑功能开发中...')
}

const handleDelete = (id) => {
  window.$dialog.warning({
    title: '确认删除',
    content: '确定要删除这篇文章吗?',
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      await deleteArticle(id)
      window.$message.success('删除成功')
      loadArticles()
    }
  })
}

const formatDate = (date) => {
  return new Date(date).toLocaleDateString('zh-CN')
}

onMounted(() => {
  loadArticles()
})
</script>

<style scoped>
.my-page {
  max-width: 900px;
  margin: 0 auto;
}

.my-card {
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
  border: 1px solid rgba(42, 219, 92, 0.1);
  background: #141517;
}
</style>
