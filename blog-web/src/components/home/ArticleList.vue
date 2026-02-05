<template>
    <n-card class="article-list-card panel-card">
        <template #header>
            <n-space :size="8" align="center">
                <n-icon :component="DocumentTextOutline" size="20" color="var(--color-primary)" />
                <span class="article-list-title">最新文章</span>
            </n-space>
        </template>
        <n-spin :show="loading">
            <n-list v-if="articles.length > 0">
                <n-list-item v-for="article in articles" :key="article.id">
                    <ArticleCard :article="article" @click="goToDetail(article.id)" />
                </n-list-item>
            </n-list>
            <n-empty v-else description="暂无文章" />
        </n-spin>

        <n-pagination v-if="totalPages > 1" v-model:page="currentPage" :page-count="totalPages" class="article-list-pagination"
            @update:page="changePage" />
    </n-card>
</template>

<script setup>
import { onMounted } from 'vue'
import { useArticleList, useArticleNavigation } from '@/composables/business/useArticle'
import ArticleCard from '@/components/article/ArticleCard.vue'
import { NCard, NList, NListItem, NSpace, NSpin, NPagination, NEmpty, NIcon } from 'naive-ui'
import { DocumentTextOutline } from '@vicons/ionicons5'
import { PAGINATION_CONFIG } from '@/config/constants'

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

onMounted(() => {
    loadArticles()
})
</script>

<style scoped>
.article-list-title {
    font-weight: 600;
    font-size: 16px;
    color: var(--text-primary);
}

.article-list-pagination {
    margin-top: 20px;
    justify-content: center;
}
</style>
