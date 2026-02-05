<template>
    <div class="article-header">
        <h1 class="article-title">{{ article.title }}</h1>

        <div class="article-meta">
            <n-space :size="10">
                <n-avatar round :size="32" :src="article.authorAvatar" />
                <div>
                    <div class="author-name">{{ article.authorNickname }}</div>
                    <n-text depth="3" class="article-meta-text">
                        {{ formatDate(article.createTime) }} · {{ article.viewCount }} 阅读
                    </n-text>
                </div>
            </n-space>

            <n-space :size="15">
                <n-button :type="article.isLiked ? 'primary' : 'default'" :loading="liking" @click="handleLike">
                    <template #icon>
                        <n-icon :component="article.isLiked ? Heart : HeartOutline" />
                    </template>
                    {{ article.likeCount }}
                </n-button>
                <n-button :type="article.isCollected ? 'primary' : 'default'" :loading="collecting"
                    @click="handleCollect">
                    <template #icon>
                        <n-icon :component="article.isCollected ? Star : StarOutline" />
                    </template>
                    {{ article.collectCount }}
                </n-button>
            </n-space>
        </div>

        <!-- 文章标签 -->
        <div v-if="article.tags && article.tags.length > 0" class="article-tags">
            <n-space :size="8">
                <n-tag v-for="tag in article.tags" :key="tag.id" :bordered="false" round class="tag-item"
                    @click="handleTagClick(tag)">
                    {{ tag.tagName }}
                </n-tag>
            </n-space>
        </div>
    </div>
</template>

<script setup>
import { NSpace, NAvatar, NText, NButton, NIcon, NTag } from 'naive-ui'
import { HeartOutline, Heart, StarOutline, Star } from '@vicons/ionicons5'
import { formatDateTime } from '@/utils/common'
import { useRouter } from 'vue-router'

const props = defineProps({
    article: {
        type: Object,
        required: true
    },
    liking: {
        type: Boolean,
        default: false
    },
    collecting: {
        type: Boolean,
        default: false
    }
})

const emit = defineEmits(['like', 'collect'])
const router = useRouter()

const handleLike = () => {
    emit('like')
}

const handleCollect = () => {
    emit('collect')
}

const handleTagClick = (tag) => {
    router.push(`/tag/${tag.id}`)
}

const formatDate = (date) => {
    return formatDateTime(date)
}
</script>

<style scoped>
.article-header {
    margin-bottom: 20px;
}

.article-title {
    font-size: 32px;
    font-weight: 700;
    margin: 0 0 20px 0;
    line-height: 1.4;
    color: var(--text-primary);
    text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
}

.article-meta {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
}

.author-name {
    font-weight: 500;
    color: var(--text-primary);
}

.article-meta-text {
    font-size: 12px;
}

.article-tags {
    margin-top: 16px;
    padding-top: 16px;
    border-top: 1px solid var(--border-secondary);
}

.tag-item {
    cursor: pointer;
    transition: all 0.3s;
}

.tag-item:hover {
    transform: translateY(-2px);
    box-shadow: 0 2px 8px var(--color-primary-alpha-30);
}
</style>
