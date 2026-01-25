<template>
    <n-card class="article-list-card">
        <template #header>
            <n-space :size="8" align="center">
                <n-icon :component="DocumentTextOutline" size="20" :color="'#2ADB5C'" />
                <span style="font-weight: 600; font-size: 16px; color: rgba(255, 255, 255, 0.9);">最新文章</span>
            </n-space>
        </template>
        <n-spin :show="loading">
            <n-list v-if="articles.length > 0">
                <n-list-item v-for="article in articles" :key="article.id">
                    <ArticleCard :article="article" @click="goToArticle(article.id)" />
                </n-list-item>
            </n-list>
            <n-empty v-else description="暂无文章" />
        </n-spin>

        <n-pagination v-model:page="currentPage" :page-count="totalPages"
            style="margin-top: 20px; justify-content: center" @update:page="loadArticles" />
    </n-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getArticleList } from '@/api/article'
import ArticleCard from '@/components/ArticleCard.vue'
import { NCard, NList, NListItem, NSpace, NSpin, NPagination, NEmpty, NIcon } from 'naive-ui'
import { DocumentTextOutline } from '@vicons/ionicons5'

const router = useRouter()

const loading = ref(false)
const articles = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const totalPages = ref(0)

// 加载文章列表
const loadArticles = async () => {
    loading.value = true
    try {
        const res = await getArticleList({
            current: currentPage.value,
            size: pageSize.value
        })
        articles.value = res.data.records
        totalPages.value = Math.ceil(res.data.total / pageSize.value)
    } finally {
        loading.value = false
    }
}

// 跳转到文章详情
const goToArticle = (id) => {
    router.push(`/article/${id}`)
}

onMounted(() => {
    loadArticles()
})
</script>

<style scoped>
.article-list-card {
    border-radius: 12px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
    transition: all 0.3s;
    border: 1px solid rgba(42, 219, 92, 0.1);
    background: #141517;
}
</style>
