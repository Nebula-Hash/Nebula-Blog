<template>
    <n-card style="margin-bottom: 20px" class="hot-card">
        <template #header>
            <n-space :size="8" align="center">
                <n-icon :component="FlameOutline" size="20" color="#3D7EAE" />
                <span style="font-weight: 600; font-size: 16px; color: var(--text-primary);">热门文章</span>
            </n-space>
        </template>
        <n-spin :show="loading">
            <n-list v-if="hotArticles.length > 0" hoverable clickable>
                <n-list-item v-for="(article, index) in hotArticles" :key="article.id" @click="goToDetail(article.id)">
                    <template #prefix>
                        <n-tag :type="index < 3 ? 'error' : 'default'" size="small" round>
                            {{ index + 1 }}
                        </n-tag>
                    </template>
                    <n-ellipsis style="max-width: 180px; color: var(--text-primary);">
                        {{ article.title }}
                    </n-ellipsis>
                    <template #suffix>
                        <span style="font-size: 12px; color: var(--text-tertiary); white-space: nowrap;">
                            {{ article.viewCount }}
                        </span>
                    </template>
                </n-list-item>
            </n-list>
            <n-empty v-else description="暂无热门文章" size="small" />
        </n-spin>
    </n-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useArticleNavigation } from '@/composables/business/useArticle'
import { useCacheStore } from '@/stores'
import { NCard, NList, NListItem, NTag, NSpace, NEllipsis, NIcon, NSpin, NEmpty } from 'naive-ui'
import { FlameOutline } from '@vicons/ionicons5'
import { PAGINATION_CONFIG } from '@/config/constants'

const cacheStore = useCacheStore()
const { goToDetail } = useArticleNavigation()

const loading = ref(false)
const hotArticles = ref([])

// 加载热门文章
const loadHotArticles = async () => {
    loading.value = true
    try {
        hotArticles.value = await cacheStore.fetchHotArticles(PAGINATION_CONFIG.HOT_ARTICLES_SIZE)
    } catch (error) {
        console.error('[HotArticles] 加载热门文章失败:', error)
    } finally {
        loading.value = false
    }
}

onMounted(() => {
    loadHotArticles()
})
</script>

<style scoped>
.hot-card {
    border-radius: var(--radius-xl);
    box-shadow: var(--shadow-md);
    transition: all var(--transition-base);
    border: 1px solid var(--color-primary-alpha-10);
    background: var(--surface-primary);
}

.hot-card:hover {
    box-shadow: var(--shadow-elevated);
    transform: translateY(-2px);
    border-color: var(--color-primary-alpha-30);
}
</style>
