<template>
    <n-card style="margin-bottom: 20px" class="category-card">
        <template #header>
            <n-space :size="8" align="center">
                <n-icon :component="FolderOutline" size="20" :color="'#2ADB5C'" />
                <span style="font-weight: 600; font-size: 16px; color: rgba(255, 255, 255, 0.9);">分类</span>
            </n-space>
        </template>
        <n-spin :show="loading" size="small">
            <n-space v-if="!loading && categories.length > 0" vertical>
                <div v-for="cat in displayCategories" :key="cat.id" class="category-item" @click="goToCategory(cat.id)">
                    <span style="color: rgba(255, 255, 255, 0.85);">{{ cat.categoryName }}</span>
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
import { getCategoryList } from '@/api/category'
import { NCard, NTag, NSpace, NIcon, NSpin, NEmpty, useMessage } from 'naive-ui'
import { FolderOutline } from '@vicons/ionicons5'

const router = useRouter()
const message = useMessage()

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
        const res = await getCategoryList()
        if (res.code === 200 && res.data) {
            categories.value = res.data
        } else {
            console.error('加载分类列表失败:', res.message)
        }
    } catch (error) {
        console.error('加载分类列表失败:', error)
        message.error('加载分类列表失败')
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
    border-radius: 12px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
    transition: all 0.3s;
    border: 1px solid rgba(42, 219, 92, 0.1);
    background: #141517;
}

.category-card:hover {
    box-shadow: 0 8px 24px rgba(42, 219, 92, 0.2);
    transform: translateY(-2px);
    border-color: rgba(42, 219, 92, 0.3);
}

.category-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 8px;
    cursor: pointer;
    transition: all 0.3s;
    border-radius: 8px;
}

.category-item:hover {
    color: #2ADB5C;
    background-color: rgba(42, 219, 92, 0.15);
    transform: translateX(5px);
}
</style>
