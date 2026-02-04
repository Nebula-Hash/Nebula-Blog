<template>
    <n-card class="comment-card">
        <template #header>
            <n-space :size="8" align="center">
                <n-icon :component="ChatbubbleEllipsesOutline" size="20" color="#3D7EAE" />
                <span style="font-weight: 600; font-size: 16px;">评论</span>
            </n-space>
        </template>

        <!-- 发表评论 -->
        <div class="publish-section">
            <n-input v-model:value="commentContent" type="textarea" placeholder="写下你的评论..." :rows="3" :maxlength="500"
                show-count class="comment-input" />
            <n-button type="primary" @click="handlePublish" :loading="publishing" :disabled="!commentContent.trim()"
                style="margin-top: 10px">
                发表评论
            </n-button>
        </div>

        <!-- 评论列表 -->
        <div class="comments-list">
            <n-list v-if="comments.length > 0">
                <n-list-item v-for="comment in comments" :key="comment.id">
                    <CommentItem :comment="comment" @reply="handleReply" @like="handleLike" />
                </n-list-item>
            </n-list>
            <n-empty v-else description="暂无评论，快来发表第一条评论吧！" style="margin-top: 20px" />
        </div>

        <!-- 回复对话框 -->
        <n-modal v-model:show="showReplyDialog" preset="card" title="回复评论" style="width: 500px">
            <n-input v-model:value="replyContent" type="textarea" placeholder="写下你的回复..." :rows="3" :maxlength="500"
                show-count />
            <template #footer>
                <n-space justify="end">
                    <n-button @click="showReplyDialog = false">取消</n-button>
                    <n-button type="primary" @click="handlePublishReply" :loading="replyLoading"
                        :disabled="!replyContent.trim()">
                        发布
                    </n-button>
                </n-space>
            </template>
        </n-modal>
    </n-card>
</template>

<script setup>
import { ref } from 'vue'
import { NCard, NSpace, NIcon, NInput, NButton, NList, NListItem, NEmpty, NModal } from 'naive-ui'
import { ChatbubbleEllipsesOutline } from '@vicons/ionicons5'
import CommentItem from '@/components/comment/CommentItem.vue'

const props = defineProps({
    comments: {
        type: Array,
        default: () => []
    },
    publishing: {
        type: Boolean,
        default: false
    }
})

const emit = defineEmits(['publish', 'reply', 'like'])

const commentContent = ref('')
const showReplyDialog = ref(false)
const replyContent = ref('')
const replyLoading = ref(false)
const replyTarget = ref(null)

const handlePublish = () => {
    if (!commentContent.value.trim()) return
    emit('publish', commentContent.value)
    commentContent.value = ''
}

const handleReply = (comment) => {
    replyTarget.value = comment
    showReplyDialog.value = true
}

const handlePublishReply = async () => {
    if (!replyContent.value.trim()) return

    replyLoading.value = true
    try {
        await emit('reply', {
            content: replyContent.value,
            parentId: replyTarget.value.id,
            replyUserId: replyTarget.value.userId
        })
        replyContent.value = ''
        showReplyDialog.value = false
    } finally {
        replyLoading.value = false
    }
}

const handleLike = (commentId) => {
    emit('like', commentId)
}
</script>

<style scoped>
.comment-card {
    margin-top: var(--spacing-lg);
    border-radius: var(--radius-xl);
    box-shadow: var(--shadow-md);
    border: 1px solid var(--border-secondary);
    background: var(--surface-primary);
}

.publish-section {
    margin-bottom: var(--spacing-lg);
}

.comment-input {
    width: 100%;
}

.comments-list {
    margin-top: var(--spacing-lg);
}
</style>
