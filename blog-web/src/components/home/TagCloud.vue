<template>
    <n-card class="tag-card">
        <template #header>
            <n-space :size="8" align="center">
                <n-icon :component="PricetagsOutline" size="20" :color="'#2ADB5C'" />
                <span style="font-weight: 600; font-size: 16px; color: rgba(255, 255, 255, 0.9);">标签</span>
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
import { getTagList } from '@/api/tag'
import { NCard, NTag, NSpace, NIcon, NSpin, NEmpty, useMessage } from 'naive-ui'
import { PricetagsOutline } from '@vicons/ionicons5'

const router = useRouter()
const message = useMessage()

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
        const res = await getTagList()
        if (res.code === 200 && res.data) {
            tags.value = res.data
        } else {
            console.error('加载标签列表失败:', res.message)
        }
    } catch (error) {
        console.error('加载标签列表失败:', error)
        message.error('加载标签列表失败')
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
.tag-card {
    border-radius: 12px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
    transition: all 0.3s;
    border: 1px solid rgba(42, 219, 92, 0.1);
    background: #141517;
}

.tag-card:hover {
    box-shadow: 0 8px 24px rgba(42, 219, 92, 0.2);
    transform: translateY(-2px);
    border-color: rgba(42, 219, 92, 0.3);
}

.tag-item {
    cursor: pointer;
    transition: all 0.3s;
}

.tag-item:hover {
    transform: scale(1.1);
}
</style>
