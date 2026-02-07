<template>
  <n-layout class="main-layout">
    <n-layout-header bordered class="header">
      <div class="header-content">
        <div class="logo" @click="router.push('/')">
          <n-space :size="8" align="center">
            <n-icon :component="BookOutline" size="32" color="var(--color-primary)" />
            <h2 class="site-title">技术博客</h2>
          </n-space>
        </div>

        <n-space class="nav-menu" :size="30">
          <n-button text @click="router.push('/')" class="nav-button">
            <template #icon>
              <n-icon :component="HomeOutline" />
            </template>
            首页
          </n-button>
          <n-button text @click="showCategoryDrawer = true" class="nav-button">
            <template #icon>
              <n-icon :component="GridOutline" />
            </template>
            分类
          </n-button>
          <n-button text @click="showTagDrawer = true" class="nav-button">
            <template #icon>
              <n-icon :component="PricetagOutline" />
            </template>
            标签
          </n-button>
          <n-button text @click="router.push('/search')" class="nav-button">
            <template #icon>
              <n-icon :component="SearchOutline" />
            </template>
            搜索
          </n-button>
          <ThemeToggle />
        </n-space>

        <!-- 用户区域占位 - 认证功能已迁移到测试项目 -->
        <n-space class="user-actions">
          <n-tooltip placement="bottom">
            <template #trigger>
              <n-button text disabled class="placeholder-button">
                <template #icon>
                  <n-icon :component="PersonCircleOutline" size="24" />
                </template>
              </n-button>
            </template>
            登录功能已迁移至权限测试项目
          </n-tooltip>
        </n-space>
      </div>
    </n-layout-header>

    <n-layout-content class="content">
      <router-view />
    </n-layout-content>

    <n-layout-footer bordered class="footer">
      <p>© 2025 博客系统 - Powered by Spring Boot 4 & Vue 3</p>
    </n-layout-footer>
  </n-layout>

  <!-- 分类抽屉 -->
  <n-drawer v-model:show="showCategoryDrawer" width="400" placement="right" :trap-focus="true" :block-scroll="true">
    <n-drawer-content title="文章分类">
      <n-spin v-if="loadingCategories" class="drawer-loading" />
      <n-list v-else hoverable clickable>
        <n-list-item v-for="cat in categories" :key="cat.id" @click="goToCategory(cat.id)" tabindex="0"
          @keyup.enter="goToCategory(cat.id)">
          <template #suffix>
            <n-tag :bordered="false">{{ cat.articleCount }}</n-tag>
          </template>
          {{ cat.categoryName }}
        </n-list-item>
      </n-list>
    </n-drawer-content>
  </n-drawer>

  <!-- 标签抽屉 -->
  <n-drawer v-model:show="showTagDrawer" width="400" placement="right" :trap-focus="true" :block-scroll="true">
    <n-drawer-content title="文章标签">
      <n-spin v-if="loadingTags" class="drawer-loading" />
      <n-space v-else>
        <n-tag v-for="tag in tags" :key="tag.id" :bordered="false" round class="clickable-tag" tabindex="0"
          @click="goToTag(tag.id)" @keyup.enter="goToTag(tag.id)">
          {{ tag.tagName }} ({{ tag.articleCount }})
        </n-tag>
      </n-space>
    </n-drawer-content>
  </n-drawer>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useCacheStore } from '@/stores'
import ThemeToggle from '@/components/unique/ThemeToggle.vue'
import {
  NLayout,
  NLayoutHeader,
  NLayoutContent,
  NLayoutFooter,
  NButton,
  NSpace,
  NIcon,
  NDrawer,
  NDrawerContent,
  NList,
  NListItem,
  NTag,
  NTooltip,
  NSpin
} from 'naive-ui'
import {
  SearchOutline,
  PersonCircleOutline,
  BookOutline,
  PricetagOutline,
  GridOutline,
  HomeOutline
} from '@vicons/ionicons5'

const router = useRouter()
const cacheStore = useCacheStore()
const showCategoryDrawer = ref(false)
const showTagDrawer = ref(false)

const categories = ref([])
const tags = ref([])
const loadingCategories = ref(false)
const loadingTags = ref(false)

const goToCategory = (id) => {
  showCategoryDrawer.value = false
  router.push(`/category/${id}`)
}

const goToTag = (id) => {
  showTagDrawer.value = false
  router.push(`/tag/${id}`)
}

const loadCategories = async () => {
  loadingCategories.value = true
  try {
    categories.value = await cacheStore.fetchCategoryList()
  } catch (error) {
    console.error('[MainLayout] 加载分类失败:', error)
  } finally {
    loadingCategories.value = false
  }
}

const loadTags = async () => {
  loadingTags.value = true
  try {
    tags.value = await cacheStore.fetchTagList()
  } catch (error) {
    console.error('[MainLayout] 加载标签失败:', error)
  } finally {
    loadingTags.value = false
  }
}

onMounted(() => {
  loadCategories()
  loadTags()
})
</script>

<style scoped>
.main-layout {
  min-height: 100vh;
}

.header {
  padding: 0 20px;
  background-color: var(--bg-secondary);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  box-shadow: 0 2px 12px var(--shadow-color);
  border-bottom: 1px solid var(--border-secondary);
  transition: background-color 0.3s cubic-bezier(0.4, 0, 0.2, 1),
    box-shadow 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.header-content {
  max-width: 1400px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 64px;
}

.logo {
  cursor: pointer;
  user-select: none;
}

.site-title {
  color: var(--color-primary);
  margin: 0;
  font-weight: 600;
  font-size: 24px;
}

.nav-button {
  color: var(--text-secondary);
  font-size: 16px;
  font-weight: 500;
  padding: 8px 16px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.nav-button:hover {
  color: var(--color-primary);
  background-color: var(--color-primary-alpha-10);
}

.theme-toggle {
  position: relative;
}

.theme-icon {
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.theme-toggle:hover .theme-icon {
  transform: rotate(20deg) scale(1.1);
}

.theme-toggle:active .theme-icon {
  transform: rotate(20deg) scale(0.95);
}

/* 移除旧的主题切换样式，因为已使用新组件 */

.nav-menu {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
}

.user-actions {
  min-width: 100px;
  display: flex;
  justify-content: flex-end;
}

.placeholder-button {
  color: var(--text-tertiary);
  opacity: 0.5;
  cursor: not-allowed;
}

.drawer-loading {
  width: 100%;
  padding: 40px 0;
}

.clickable-tag {
  cursor: pointer;
}

.content {
  max-width: 1400px;
  margin: 20px auto;
  padding: 0 20px;
  min-height: calc(100vh - 144px);
  background-color: transparent;
}

.footer {
  text-align: center;
  padding: 20px;
  background-color: var(--bg-secondary);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  color: var(--text-tertiary);
  border-top: 1px solid var(--border-secondary);
  transition: background-color 0.3s cubic-bezier(0.4, 0, 0.2, 1),
    color 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}
</style>
