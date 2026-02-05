<template>
    <n-card class="comment-card">
        <template #header>
            <n-space :size="8" align="center">
                <n-icon :component="ChatbubbleEllipsesOutline" size="20" color="#3D7EAE" />
                <span style="font-weight: 600; font-size: 16px;">评论</span>
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
    border-radius: var(--radius-xl);
    box-shadow: var(--shadow-md);
    border: 1px solid var(--border-secondary);
    background: var(--surface-primary);
}
</style>
