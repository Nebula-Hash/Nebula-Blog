<template>
  <div class="home-page">
    <!-- 轮播图 -->
    <BannerCarousel />

    <n-grid :cols="4" :x-gap="20" :y-gap="20" class="home-grid">
      <n-gi :span="3">
        <!-- 推荐文章 -->
        <RecommendArticles />

        <!-- 最新文章列表 -->
        <ArticleList />
      </n-gi>

      <n-gi>
        <!-- 热门文章 -->
        <HotArticles />

        <!-- 分类统计 -->
        <CategoryList />

        <!-- 标签云 -->
        <TagCloud />
      </n-gi>
    </n-grid>
  </div>
</template>

<script setup>
import { NGrid, NGi } from 'naive-ui'
import { createLazyComponent } from '@/utils/lazyComponent'

// 使用懒加载组件
const BannerCarousel = createLazyComponent(
  () => import('@/components/home/BannerCarousel.vue'),
  { delay: 0 }
)

const RecommendArticles = createLazyComponent(
  () => import('@/components/home/RecommendArticles.vue')
)

const ArticleList = createLazyComponent(
  () => import('@/components/home/ArticleList.vue')
)

const HotArticles = createLazyComponent(
  () => import('@/components/home/HotArticles.vue')
)

const CategoryList = createLazyComponent(
  () => import('@/components/home/CategoryList.vue')
)

const TagCloud = createLazyComponent(
  () => import('@/components/home/TagCloud.vue')
)
</script>

<style scoped>
.home-page {
  width: 100%;
}

.home-grid {
  margin-top: 30px;
}
</style>
