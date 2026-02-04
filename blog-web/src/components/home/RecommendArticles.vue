<template>
    <n-card v-if="recommendArticles.length > 0" style="margin-bottom: 20px" class="recommend-card">
        <template #header>
            <n-space :size="8" align="center">
                <n-icon :component="BookmarkOutline" size="20" :color="'#2ADB5C'" />
                <span style="font-weight: 600; font-size: 16px; color: rgba(255, 255, 255, 0.9);">推荐文章</span>
            </n-space>
        </template>
        <n-spin :show="loading">
            <n-list hoverable clickable>
                <n-list-item v-for="article in recommendArticles" :key="article.id" @click="goToDetail(article.id)">
                    <template #prefix>
                        <n-tag type="warning" size="small">置顶</n-tag>
                    </template>
                    <span style="color: rgba(255, 255, 255, 0.85);">{{ article.title }}</span>
                    <template #suffix>
                        <n-space :size="6" align="center" style="display: flex; flex-direction: row;">
                            <n-icon :component="EyeOutline" size="16" style="display: flex;" />
                            <span style="color: rgba(255, 255, 255, 0.6); line-height: 1;">{{ article.viewCount
                                }}</span>
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
    border-radius: 12px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
    transition: all 0.3s;
    border: 1px solid rgba(42, 219, 92, 0.1);
    background: #141517;
}

.recommend-card:hover {
    box-shadow: 0 8px 24px rgba(42, 219, 92, 0.2);
    transform: translateY(-2px);
    border-color: rgba(42, 219, 92, 0.3);
}
</style>
