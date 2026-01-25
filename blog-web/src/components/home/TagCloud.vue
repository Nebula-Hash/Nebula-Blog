<template>
    <n-card class="tag-card">
        <template #header>
            <n-space :size="8" align="center">
                <n-icon :component="PricetagsOutline" size="20" :color="'#2ADB5C'" />
                <span style="font-weight: 600; font-size: 16px; color: rgba(255, 255, 255, 0.9);">标签</span>
            </n-space>
        </template>
        <n-space>
            <n-tag v-for="tag in tags.slice(0, 10)" :key="tag.id" :bordered="false" round style="cursor: pointer"
                @click="goToTag(tag.id)">
                {{ tag.tagName }}
            </n-tag>
        </n-space>
    </n-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getTagList } from '@/api/tag'
import { NCard, NTag, NSpace, NIcon } from 'naive-ui'
import { PricetagsOutline } from '@vicons/ionicons5'

const router = useRouter()
const tags = ref([])

// 加载标签列表
const loadTags = async () => {
    const res = await getTagList()
    tags.value = res.data
}

// 跳转到标签页面
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
</style>
