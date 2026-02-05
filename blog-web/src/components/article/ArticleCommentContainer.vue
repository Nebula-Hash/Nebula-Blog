<template>
    <n-card class="comment-card panel-card">
        <template #header>
            <n-space :size="8" align="center">
                <n-icon :component="ChatbubbleEllipsesOutline" size="20" color="var(--color-primary)" />
                <span class="comment-title">评论</span>
            </n-space>
        </template>

        <!-- 使用 CommentList 组件 -->
        <CommentList :article-id="articleId" />
    </n-card>
</template>

<script setup>
import { NCard, NSpace, NIcon } from 'naive-ui'
import { ChatbubbleEllipsesOutline } from '@vicons/ionicons5'
import CommentList from '@/components/comment/CommentList.vue'

const props = defineProps({
    articleId: {
        type: [String, Number],
        required: true
    }
})
</script>

<style scoped>
.comment-card {
    margin-top: var(--spacing-lg);
}

.comment-title {
    font-weight: 600;
    font-size: 16px;
    color: var(--text-primary);
}
</style>
