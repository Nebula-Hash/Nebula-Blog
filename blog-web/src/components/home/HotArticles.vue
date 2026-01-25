<template>
    <n-card style="margin-bottom: 20px" class="hot-card">
        <template #header>
            <n-space :size="8" align="center">
                <n-icon :component="FlameOutline" size="20" :color="'#2ADB5C'" />
                <span style="font-weight: 600; font-size: 16px; color: rgba(255, 255, 255, 0.9);">热门文章</span>
            </n-space>
        </template>
        <n-list hoverable clickable>
            <n-list-item v-for="(article, index) in hotArticles" :key="article.id" @click="goToArticle(article.id)">
                <template #prefix>
                    <n-tag :type="index < 3 ? 'error' : 'default'" size="small" round>
                        {{ index + 1 }}
                    </n-tag>
                </template>
                <n-ellipsis style="max-width: 200px; color: rgba(255, 255, 255, 0.85);">
                    {{ article.title }}
                </n-ellipsis>
                <template #suffix>
                    <n-text depth="3" style="font-size: 12px; color: rgba(255, 255, 255, 0.5);">
                        {{ article.viewCount }}
                    </n-text>
                </template>
            </n-list-item>
        </n-list>
    </n-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getHotArticles } from '@/api/article'
import { NCard, NList, NListItem, NTag, NSpace, NText, NEllipsis, NIcon } from 'naive-ui'
import { FlameOutline } from '@vicons/ionicons5'

const router = useRouter()
const hotArticles = ref([])

// 加载热门文章
const loadHotArticles = async () => {
    const res = await getHotArticles(5)
    hotArticles.value = res.data
}

// 跳转到文章详情
const goToArticle = (id) => {
    router.push(`/article/${id}`)
}

onMounted(() => {
    loadHotArticles()
})
</script>

<style scoped>
.hot-card {
    border-radius: 12px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
    transition: all 0.3s;
    border: 1px solid rgba(42, 219, 92, 0.1);
    background: #141517;
}

.hot-card:hover {
    box-shadow: 0 8px 24px rgba(42, 219, 92, 0.2);
    transform: translateY(-2px);
    border-color: rgba(42, 219, 92, 0.3);
}
</style>
