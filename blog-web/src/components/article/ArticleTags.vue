<template>
    <div class="article-tags">
        <n-space>
            <n-tag v-for="tag in tags" :key="tag.id" :bordered="false" round class="tag-item"
                @click="handleTagClick(tag)">
                {{ tag.tagName }}
            </n-tag>
        </n-space>
    </div>
</template>

<script setup>
import { NSpace, NTag } from 'naive-ui'
import { useRouter } from 'vue-router'

const props = defineProps({
    tags: {
        type: Array,
        default: () => []
    }
})

const router = useRouter()

const handleTagClick = (tag) => {
    router.push(`/tag/${tag.id}`)
}
</script>

<style scoped>
.article-tags {
    margin-top: 20px;
}

.tag-item {
    cursor: pointer;
    transition: all 0.3s;
}

.tag-item:hover {
    transform: translateY(-2px);
    box-shadow: 0 2px 8px rgba(42, 219, 92, 0.3);
}
</style>
