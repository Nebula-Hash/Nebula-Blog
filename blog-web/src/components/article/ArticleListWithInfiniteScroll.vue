<template>
    <div class="article-infinite-list">
        <n-card class="list-card">
            <template #header>
                <n-space :size="8" align="center">
                    <n-icon :component="DocumentTextOutline" size="20" :color="'#2ADB5C'" />
                    <span style="font-weight: 600; font-size: 16px;">文章列表</span>
                </n-space>
            </template>

            <n-spin :show="loading && articles.length === 0">
                <n-list v-if="articles.length > 0">
                    <n-list-item v-for="article in articles" :key="article.id">
                        <ArticleCard :article="article" @click="goToDetail(article.id)" />
                    </n-list-item>
                </n-list>
                <n-empty v-else description="暂无文章" />
            </n-spin>

            <!-- 无限滚动触发器 -->
            <div ref="targetRef" style="height: 20px; margin-top: 20px;">
                <n-spin v-if="loading && hasMore" size="small" style="display: flex; justify-content: center;" />
                <n-text v-else-if="!hasMore && articles.length > 0" depth="3"
                    style="text-align: center; display: block;">
                    没有更多文章了
                </n-text>
            </div>
        </n-card>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useArticleNavigation } from '@/composables/business/useArticle'
import { useInfiniteScroll } from '@/composables/helper/useInfiniteScroll'
import { ArticleQueryService } from '@/services/articleService'
import ArticleCard from '@/components/article/ArticleCard.vue'
import { NCard, NList, NListItem, NSpace, NSpin, NEmpty, NText, NIcon } from 'naive-ui'
import { DocumentTextOutline } from '@vicons/ionicons5'
import { PAGINATION_CONFIG } from '@/config/constants'
import { createErrorHandler } from '@/utils/errorHandler'

const errorHandler = createErrorHandler('ArticleInfiniteList')
const { goToDetail } = useArticleNavigation()

const articles = ref([])
const currentPage = ref(1)
const pageSize = PAGINATION_CONFIG.DEFAULT_PAGE_SIZE

// 加载更多文章
const loadMore = async () => {
    try {
        const data = await ArticleQueryService.getList({
            current: currentPage.value,
            size: pageSize
        })

        if (data.records && data.records.length > 0) {
            articles.value.push(...data.records)
            currentPage.value++

            // 如果返回的数据少于页面大小，说明没有更多数据了
            return data.records.length >= pageSize
        }

        return false // 没有更多数据
    } catch (error) {
        errorHandler.handleLoad(error, '文章列表')
        return false
    }
}

// 使用无限滚动
const { targetRef, loading, hasMore } = useInfiniteScroll(loadMore, {
    rootMargin: '100px',
    immediate: true
})

onMounted(() => {
    // 初始化已在 useInfiniteScroll 中通过 immediate: true 完成
})
</script>

<style scoped>
.article-infinite-list {
    width: 100%;
}

.list-card {
    border-radius: 12px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
    border: 1px solid rgba(42, 219, 92, 0.1);
    background: #141517;
}
</style>
