<template>
  <div class="home-page">
    <!-- 轮播图 -->
    <n-carousel autoplay class="banner-carousel" v-if="banners.length > 0">
      <div v-for="banner in banners" :key="banner.id" class="banner-item" @click="handleBannerClick(banner)">
        <img :src="banner.imageUrl" :alt="banner.title" />
        <div class="banner-title">{{ banner.title }}</div>
      </div>
    </n-carousel>

    <n-grid :cols="4" :x-gap="20" :y-gap="20" style="margin-top: 30px">
      <n-gi :span="3">
        <!-- 推荐文章 -->
        <n-card v-if="recommendArticles.length > 0" style="margin-bottom: 20px" class="recommend-card">
          <template #header>
            <n-space :size="8" align="center">
              <n-icon :component="BookmarkOutline" size="20" :color="'#2ADB5C'" />
              <span style="font-weight: 600; font-size: 16px; color: rgba(255, 255, 255, 0.9);">推荐文章</span>
            </n-space>
          </template>
          <n-list hoverable clickable>
            <n-list-item v-for="article in recommendArticles" :key="article.id" @click="goToArticle(article.id)">
              <template #prefix>
                <n-tag type="warning" size="small">置顶</n-tag>
              </template>
              <span style="color: rgba(255, 255, 255, 0.85);">{{ article.title }}</span>
              <template #suffix>
                <n-space :size="6" align="center" style="display: flex; flex-direction: row;">
                  <n-icon :component="EyeOutline" size="16" style="display: flex;" />
                  <span style="color: rgba(255, 255, 255, 0.6); line-height: 1;">{{ article.viewCount }}</span>
                </n-space>
              </template>
            </n-list-item>
          </n-list>
        </n-card>

        <!-- 最新文章列表 -->
        <n-card class="article-list-card">
          <template #header>
            <n-space :size="8" align="center">
              <n-icon :component="DocumentTextOutline" size="20" :color="'#2ADB5C'" />
              <span style="font-weight: 600; font-size: 16px; color: rgba(255, 255, 255, 0.9);">最新文章</span>
            </n-space>
          </template>
          <n-spin :show="loading">
            <n-list v-if="articles.length > 0">
              <n-list-item v-for="article in articles" :key="article.id">
                <ArticleCard :article="article" @click="goToArticle(article.id)" />
              </n-list-item>
            </n-list>
            <n-empty v-else description="暂无文章" />
          </n-spin>

          <n-pagination
            v-model:page="currentPage"
            :page-count="totalPages"
            style="margin-top: 20px; justify-content: center"
            @update:page="loadArticles"
          />
        </n-card>
      </n-gi>

      <n-gi>
        <!-- 热门文章 -->
        <n-card style="margin-bottom: 20px" class="hot-card">
          <template #header>
            <n-space :size="8" align="center">
              <n-icon :component="FlameOutline" size="20" :color="'#2ADB5C'" />
              <span style="font-weight: 600; font-size: 16px; color: rgba(255, 255, 255, 0.9);">热门文章</span>
            </n-space>
          </template>
          <n-list hoverable clickable>
            <n-list-item v-for="(article, index) in hotArticles" :key="article.id" @click="goToArticle(article.id)">
              <template #prefix>
                <n-tag :type="index < 3 ? 'error' : 'default'" size="small" round>
                  {{ index + 1 }}
                </n-tag>
              </template>
              <n-ellipsis style="max-width: 200px; color: rgba(255, 255, 255, 0.85);">
                {{ article.title }}
              </n-ellipsis>
              <template #suffix>
                <n-text depth="3" style="font-size: 12px; color: rgba(255, 255, 255, 0.5);">
                  {{ article.viewCount }}
                </n-text>
              </template>
            </n-list-item>
          </n-list>
        </n-card>

        <!-- 分类统计 -->
        <n-card style="margin-bottom: 20px" class="category-card">
          <template #header>
            <n-space :size="8" align="center">
              <n-icon :component="FolderOutline" size="20" :color="'#2ADB5C'" />
              <span style="font-weight: 600; font-size: 16px; color: rgba(255, 255, 255, 0.9);">分类</span>
            </n-space>
          </template>
          <n-space vertical>
            <div v-for="cat in categories.slice(0, 6)" :key="cat.id" class="category-item" @click="goToCategory(cat.id)">
              <span style="color: rgba(255, 255, 255, 0.85);">{{ cat.categoryName }}</span>
              <n-tag :bordered="false" size="small">{{ cat.articleCount }}</n-tag>
            </div>
          </n-space>
        </n-card>

        <!-- 标签云 -->
        <n-card class="tag-card">
          <template #header>
            <n-space :size="8" align="center">
              <n-icon :component="PricetagsOutline" size="20" :color="'#2ADB5C'" />
              <span style="font-weight: 600; font-size: 16px; color: rgba(255, 255, 255, 0.9);">标签</span>
            </n-space>
          </template>
          <n-space>
            <n-tag
              v-for="tag in tags.slice(0, 10)"
              :key="tag.id"
              :bordered="false"
              round
              style="cursor: pointer"
              @click="goToTag(tag.id)"
            >
              {{ tag.tagName }}
            </n-tag>
          </n-space>
        </n-card>
      </n-gi>
    </n-grid>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getArticleList, getHotArticles, getRecommendArticles } from '@/api/article'
import { getCategoryList } from '@/api/category'
import { getTagList } from '@/api/tag'
import { getBannerList } from '@/api/banner'
import ArticleCard from '@/components/ArticleCard.vue'
import {
  NCard,
  NList,
  NListItem,
  NTag,
  NSpace,
  NText,
  NEllipsis,
  NGrid,
  NGi,
  NCarousel,
  NSpin,
  NPagination,
  NEmpty,
  NIcon
} from 'naive-ui'
import {
  EyeOutline,
  FlameOutline,
  BookmarkOutline,
  FolderOutline,
  PricetagsOutline,
  DocumentTextOutline
} from '@vicons/ionicons5'

const router = useRouter()

const loading = ref(false)
const articles = ref([])
const hotArticles = ref([])
const recommendArticles = ref([])
const categories = ref([])
const tags = ref([])
const banners = ref([])

const currentPage = ref(1)
const pageSize = ref(10)
const totalPages = ref(0)

const loadArticles = async () => {
  loading.value = true
  try {
    const res = await getArticleList({
      current: currentPage.value,
      size: pageSize.value
    })
    articles.value = res.data.records
    totalPages.value = Math.ceil(res.data.total / pageSize.value)
  } finally {
    loading.value = false
  }
}

const loadHotArticles = async () => {
  const res = await getHotArticles(5)
  hotArticles.value = res.data
}

const loadRecommendArticles = async () => {
  const res = await getRecommendArticles(3)
  recommendArticles.value = res.data
}

const loadCategories = async () => {
  const res = await getCategoryList()
  categories.value = res.data
}

const loadTags = async () => {
  const res = await getTagList()
  tags.value = res.data
}

const loadBanners = async () => {
  const res = await getBannerList()
  banners.value = res.data
}

const goToArticle = (id) => {
  router.push(`/article/${id}`)
}

const goToCategory = (id) => {
  router.push(`/category/${id}`)
}

const goToTag = (id) => {
  router.push(`/tag/${id}`)
}

const handleBannerClick = (banner) => {
  if (banner.linkUrl) {
    if (banner.linkUrl.startsWith('http')) {
      window.open(banner.linkUrl, '_blank')
    } else {
      router.push(banner.linkUrl)
    }
  }
}

onMounted(() => {
  loadArticles()
  loadHotArticles()
  loadRecommendArticles()
  loadCategories()
  loadTags()
  loadBanners()
})
</script>

<style scoped>
.home-page {
  width: 100%;
}

.banner-carousel {
  height: 400px;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.4);
  border: 1px solid rgba(42, 219, 92, 0.1);
}

.banner-item {
  position: relative;
  height: 400px;
  cursor: pointer;
  transition: transform 0.3s;
}

.banner-item:hover {
  transform: scale(1.02);
}

.banner-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.banner-title {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 30px;
  background: linear-gradient(transparent, rgba(0, 0, 0, 0.85));
  color: white;
  font-size: 28px;
  font-weight: bold;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.5);
}

.recommend-card,
.article-list-card,
.hot-card,
.category-card,
.tag-card {
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
  transition: all 0.3s;
  border: 1px solid rgba(42, 219, 92, 0.1);
  background: #141517;
}

.recommend-card:hover,
.hot-card:hover,
.category-card:hover,
.tag-card:hover {
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
