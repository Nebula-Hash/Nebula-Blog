<template>
    <n-card style="margin-bottom: 20px" class="category-card">
        <template #header>
            <n-space :size="8" align="center">
                <n-icon :component="FolderOutline" size="20" color="#3D7EAE" />
                <span style="font-weight: 600; font-size: 16px; color: var(--text-primary);">分类</span>
            </n-space>
        </template>
        <n-spin :show="loading" size="small">
            <n-space v-if="!loading && categories.length > 0" vertical>
                <div v-for="cat in displayCategories" :key="cat.id" class="category-item" @click="goToCategory(cat.id)">
                    <span style="color: var(--text-primary);">{{ cat.categoryName }}</span>
                    <n-tag :bordered="false" size="small">{{ cat.articleCount || 0 }}</n-tag>
                </div>
            </n-space>
            <n-empty v-else-if="!loading && categories.length === 0" description="暂无分类" size="small" />
        </n-spin>
    </n-card>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useCacheStore } from '@/stores'
import { NCard, NTag, NSpace, NIcon, NSpin, NEmpty } from 'naive-ui'
import { FolderOutline } from '@vicons/ionicons5'

const router = useRouter()
const cacheStore = useCacheStore()

const loading = ref(false)
const categories = ref([])

// 显示前6个分类
const displayCategories = computed(() => categories.value.slice(0, 6))

/**
 * 加载分类列表
 */
const loadCategories = async () => {
    loading.value = true
    try {
        categories.value = await cacheStore.fetchCategoryList()
    } catch (error) {
        console.error('[CategoryList] 加载分类列表失败:', error)
    } finally {
        loading.value = false
    }
}

/**
 * 跳转到分类页面
 */
const goToCategory = (id) => {
    router.push(`/category/${id}`)
}

onMounted(() => {
    loadCategories()
})
</script>

<style scoped>
.category-card {
    border-radius: var(--radius-xl);
    box-shadow: var(--shadow-md);
    transition: all var(--transition-base);
    border: 1px solid var(--border-secondary);
    background: var(--surface-primary);
}

.category-card:hover {
    box-shadow: var(--shadow-elevated);
    transform: translateY(-2px);
    border-color: var(--color-primary-alpha-30);
}

.category-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: var(--spacing-sm) var(--spacing-xs);
    cursor: pointer;
    transition: all var(--transition-base);
    border-radius: var(--radius-md);
}

.category-item:hover {
    color: var(--color-primary);
    background-color: var(--color-primary-alpha-10);
    transform: translateX(5px);
}
</style>
