<template>
    <n-card class="category-card panel-card panel-card-hoverable">
        <template #header>
            <n-space :size="8" align="center">
                <n-icon :component="FolderOutline" size="20" color="var(--color-primary)" />
                <span class="category-title">分类</span>
            </n-space>
        </template>
        <n-spin :show="loading" size="small">
            <n-space v-if="!loading && categories.length > 0" vertical>
                <div v-for="cat in displayCategories" :key="cat.id" class="category-item" @click="goToCategory(cat.id)">
                    <span class="category-name">{{ cat.categoryName }}</span>
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
    margin-bottom: 20px;
}

.category-title {
    font-weight: 600;
    font-size: 16px;
    color: var(--text-primary);
}

.category-name {
    color: var(--text-primary);
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
