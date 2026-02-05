<template>
    <n-card class="hot-card panel-card panel-card-hoverable">
        <template #header>
            <n-space :size="8" align="center">
                <n-icon :component="FlameOutline" size="20" color="var(--color-primary)" />
                <span class="hot-title">热门文章</span>
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
                    <n-ellipsis class="hot-article-title">
                        {{ article.title }}
                    </n-ellipsis>
                    <template #suffix>
                        <span class="hot-article-suffix">
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
    margin-bottom: 20px;
}

.hot-title {
    font-weight: 600;
    font-size: 16px;
    color: var(--text-primary);
}

.hot-card :deep(.n-list-item) {
    width: 100%;
    overflow: hidden;
}

.hot-card :deep(.n-list-item__main) {
    min-width: 0;
}

.hot-article-title {
    flex: 1;
    min-width: 0;
    color: var(--text-primary);
}

.hot-article-suffix {
    font-size: 12px;
    color: var(--text-tertiary);
    white-space: nowrap;
    flex: none;
}
</style>
