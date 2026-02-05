<template>
    <n-card class="tag-card panel-card panel-card-hoverable">
        <template #header>
            <n-space :size="8" align="center">
                <n-icon :component="PricetagsOutline" size="20" color="var(--color-primary)" />
                <span class="tag-title">标签</span>
            </n-space>
        </template>
        <n-spin :show="loading" size="small">
            <n-space v-if="!loading && tags.length > 0">
                <n-tag v-for="tag in displayTags" :key="tag.id" :bordered="false" round class="tag-item"
                    @click="goToTag(tag.id)">
                    {{ tag.tagName }}
                </n-tag>
            </n-space>
            <n-empty v-else-if="!loading && tags.length === 0" description="暂无标签" size="small" />
        </n-spin>
    </n-card>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useCacheStore } from '@/stores'
import { NCard, NTag, NSpace, NIcon, NSpin, NEmpty } from 'naive-ui'
import { PricetagsOutline } from '@vicons/ionicons5'

const router = useRouter()
const cacheStore = useCacheStore()

const loading = ref(false)
const tags = ref([])

// 显示前10个标签
const displayTags = computed(() => tags.value.slice(0, 10))

/**
 * 加载标签列表
 */
const loadTags = async () => {
    loading.value = true
    try {
        tags.value = await cacheStore.fetchTagList()
    } catch (error) {
        console.error('[TagCloud] 加载标签列表失败:', error)
    } finally {
        loading.value = false
    }
}

/**
 * 跳转到标签页面
 */
const goToTag = (id) => {
    router.push(`/tag/${id}`)
}

onMounted(() => {
    loadTags()
})
</script>

<style scoped>
.tag-title {
    font-weight: 600;
    font-size: 16px;
    color: var(--text-primary);
}

.tag-item {
    cursor: pointer;
    transition: all var(--transition-base);
}

.tag-item:hover {
    transform: scale(1.1);
}
</style>
