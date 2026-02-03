<template>
  <n-layout class="main-layout">
    <n-layout-header bordered class="header">
      <div class="header-content">
        <div class="logo" @click="router.push('/')">
          <n-space :size="8" align="center">
            <n-icon :component="BookOutline" size="32" color="#2ADB5C" />
            <h2 style="color: #2ADB5C; margin: 0; font-weight: 600; font-size: 24px;">技术博客</h2>
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
          <n-input v-model:value="searchKeyword" placeholder="搜索文章..." style="width: 200px" @keyup.enter="handleSearch">
            <template #suffix>
              <n-icon :component="SearchOutline" @click="handleSearch" style="cursor: pointer" />
            </template>
          </n-input>
          <n-button text @click="themeStore.toggleTheme" class="nav-button theme-toggle"
            :title="themeStore.isDark ? '切换到浅色模式' : '切换到深色模式'">
            <template #icon>
              <n-icon :component="themeStore.isDark ? SunnyOutline : MoonOutline" size="20" class="theme-icon" />
            </template>
          </n-button>
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
  <n-drawer v-model:show="showCategoryDrawer" width="400" placement="right" :trap-focus="true" :block-scroll="true"
    @after-enter="handleCategoryDrawerOpen">
    <n-drawer-content title="文章分类">
      <n-spin v-if="loadingCategories" style="width: 100%; padding: 40px 0" />
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
  <n-drawer v-model:show="showTagDrawer" width="400" placement="right" :trap-focus="true" :block-scroll="true"
    @after-enter="handleTagDrawerOpen">
    <n-drawer-content title="文章标签">
      <n-spin v-if="loadingTags" style="width: 100%; padding: 40px 0" />
      <n-space v-else>
        <n-tag v-for="tag in tags" :key="tag.id" :bordered="false" round style="cursor: pointer" tabindex="0"
          @click="goToTag(tag.id)" @keyup.enter="goToTag(tag.id)">
          {{ tag.tagName }} ({{ tag.articleCount }})
        </n-tag>
      </n-space>
    </n-drawer-content>
  </n-drawer>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useAbortController } from '@/composables/useAbortController'
import { useThemeStore } from '@/stores/theme'
import { asyncWrapper } from '@/utils/errorHandler'
import { getCategoryList } from '@/api/category'
import { getTagList } from '@/api/tag'
import {
  NLayout,
  NLayoutHeader,
  NLayoutContent,
  NLayoutFooter,
  NButton,
  NSpace,
  NInput,
  NIcon,
  NDropdown,
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
  HomeOutline,
  MoonOutline,
  SunnyOutline
} from '@vicons/ionicons5'

const router = useRouter()
const themeStore = useThemeStore()

// 使用请求取消控制器
const { createSignal } = useAbortController()

const searchKeyword = ref('')
const showCategoryDrawer = ref(false)
const showTagDrawer = ref(false)

const categories = ref([])
const tags = ref([])
const loadingCategories = ref(false)
const loadingTags = ref(false)

const handleSearch = () => {
  if (searchKeyword.value.trim()) {
    router.push({
      path: '/search',
      query: { keyword: searchKeyword.value }
    })
  }
}

const goToCategory = (id) => {
  showCategoryDrawer.value = false
  router.push(`/category/${id}`)
}

const goToTag = (id) => {
  showTagDrawer.value = false
  router.push(`/tag/${id}`)
}

// 抽屉打开后的焦点管理
const handleCategoryDrawerOpen = () => {
  nextTick(() => {
    console.log('[MainLayout] 分类抽屉已打开')
  })
}

const handleTagDrawerOpen = () => {
  nextTick(() => {
    console.log('[MainLayout] 标签抽屉已打开')
  })
}

const loadCategories = async () => {
  loadingCategories.value = true
  await asyncWrapper(
    async () => {
      const res = await getCategoryList({ signal: createSignal() })
      categories.value = res.data
    },
    { operation: '加载分类', silent: true }
  )
  loadingCategories.value = false
}

const loadTags = async () => {
  loadingTags.value = true
  await asyncWrapper(
    async () => {
      const res = await getTagList({ signal: createSignal() })
      tags.value = res.data
    },
    { operation: '加载标签', silent: true }
  )
  loadingTags.value = false
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
  border-bottom: 1px solid rgba(42, 219, 92, 0.1);
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

.nav-button {
  color: var(--text-secondary);
  font-size: 16px;
  font-weight: 500;
  padding: 8px 16px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.nav-button:hover {
  color: #2ADB5C;
  background-color: rgba(42, 219, 92, 0.1);
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
  border-top: 1px solid rgba(42, 219, 92, 0.1);
  transition: background-color 0.3s cubic-bezier(0.4, 0, 0.2, 1),
    color 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}
</style>
