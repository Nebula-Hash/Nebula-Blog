<template>
  <div class="home-page">
    <!-- 轮播图 -->
    <BannerCarousel />

    <n-grid :cols="4" :x-gap="20" :y-gap="20" style="margin-top: 30px">
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
import BannerCarousel from '@/components/home/BannerCarousel.vue'
import RecommendArticles from '@/components/home/RecommendArticles.vue'
import ArticleList from '@/components/home/ArticleList.vue'
import HotArticles from '@/components/home/HotArticles.vue'
import CategoryList from '@/components/home/CategoryList.vue'
import TagCloud from '@/components/home/TagCloud.vue'
import { NGrid, NGi } from 'naive-ui'
</script>

<style scoped>
.home-page {
  width: 100%;
}
</style>
