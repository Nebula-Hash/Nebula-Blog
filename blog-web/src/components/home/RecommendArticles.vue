<template>
    <n-card v-if="recommendArticles.length > 0" style="margin-bottom: 20px" class="recommend-card">
        <template #header>
            <n-space :size="8" align="center">
                <n-icon :component="BookmarkOutline" size="20" color="#3D7EAE" />
                <span style="font-weight: 600; font-size: 16px; color: var(--text-primary);">推荐文章</span>
            </n-space>
        </template>
        <n-spin :show="loading">
            <n-list hoverable clickable>
                <n-list-item v-for="article in recommendArticles" :key="article.id" @click="goToDetail(article.id)">
                    <template #prefix>
                        <n-tag type="warning" size="small">置顶</n-tag>
                    </template>
                    <span style="color: var(--text-primary);">{{ article.title }}</span>
                    <template #suffix>
                        <n-space :size="6" align="center" style="white-space: nowrap;">
                            <n-icon :component="EyeOutline" size="16" />
                            <span style="color: var(--text-tertiary);">{{ article.viewCount }}</span>
                        </n-space>
                    </template>
                </n-list-item>
            </n-list>
        </n-spin>
    </n-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useArticleNavigation } from '@/composables/business/useArticle'
import { useCacheStore } from '@/stores'
import { NCard, NList, NListItem, NTag, NSpace, NIcon, NSpin } from 'naive-ui'
import { EyeOutline, BookmarkOutline } from '@vicons/ionicons5'

const cacheStore = useCacheStore()
const { goToDetail } = useArticleNavigation()

const loading = ref(false)
const recommendArticles = ref([])

// 加载推荐文章
const loadRecommendArticles = async () => {
    loading.value = true
    try {
        recommendArticles.value = await cacheStore.fetchRecommendArticles(3)
    } catch (error) {
        console.error('[RecommendArticles] 加载推荐文章失败:', error)
    } finally {
        loading.value = false
    }
}

onMounted(() => {
    loadRecommendArticles()
})
</script>

<style scoped>
.recommend-card {
    border-radius: var(--radius-xl);
    box-shadow: var(--shadow-md);
    transition: all var(--transition-base);
    border: 1px solid var(--color-primary-alpha-10);
    background: var(--surface-primary);
}

.recommend-card:hover {
    box-shadow: var(--shadow-elevated);
    transform: translateY(-2px);
    border-color: var(--color-primary-alpha-30);
}
</style>
