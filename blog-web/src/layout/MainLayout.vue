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
          <n-button text @click="themeStore.toggleTheme" class="nav-button" title="切换主题">
            <template #icon>
              <n-icon :component="themeStore.isDark ? SunnyOutline : MoonOutline" size="20" />
            </template>
          </n-button>
        </n-space>

        <n-space class="user-actions" v-if="isLoggedIn">
          <n-dropdown :options="userOptions" @select="handleUserAction">
            <n-avatar round :size="40" :src="userInfo?.avatar" />
          </n-dropdown>
        </n-space>
        <n-space v-else>
          <n-button @click="showLoginModal = true">登录</n-button>
          <n-button type="primary" @click="showRegisterModal = true">注册</n-button>
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
  <n-drawer v-model:show="showCategoryDrawer" width="400" placement="right">
    <n-drawer-content title="文章分类">
      <n-list hoverable clickable>
        <n-list-item v-for="cat in categories" :key="cat.id" @click="goToCategory(cat.id)">
          <template #suffix>
            <n-tag :bordered="false">{{ cat.articleCount }}</n-tag>
          </template>
          {{ cat.categoryName }}
        </n-list-item>
      </n-list>
    </n-drawer-content>
  </n-drawer>

  <!-- 标签抽屉 -->
  <n-drawer v-model:show="showTagDrawer" width="400" placement="right">
    <n-drawer-content title="文章标签">
      <n-space>
        <n-tag v-for="tag in tags" :key="tag.id" :bordered="false" round style="cursor: pointer"
          @click="goToTag(tag.id)">
          {{ tag.tagName }} ({{ tag.articleCount }})
        </n-tag>
      </n-space>
    </n-drawer-content>
  </n-drawer>

  <!-- 登录模态框 -->
  <LoginModal v-model="showLoginModal" @switch-to-register="switchToRegister" />

  <!-- 注册模态框 -->
  <RegisterModal v-model="showRegisterModal" @switch-to-login="switchToLogin" />
</template>

<script setup>
import { ref, onMounted, h, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuth } from '@/composables/useAuth'
import { useTokenRefresh } from '@/composables/useTokenRefresh'
import { useThemeStore } from '@/stores/theme'
import { getCategoryList } from '@/api/category'
import { getTagList } from '@/api/tag'
import LoginModal from '@/components/LoginModal.vue'
import RegisterModal from '@/components/RegisterModal.vue'
import {
  NLayout,
  NLayoutHeader,
  NLayoutContent,
  NLayoutFooter,
  NButton,
  NSpace,
  NInput,
  NIcon,
  NAvatar,
  NDropdown,
  NDrawer,
  NDrawerContent,
  NList,
  NListItem,
  NTag
} from 'naive-ui'
import {
  SearchOutline,
  PersonOutline,
  LogOutOutline,
  BookOutline,
  PricetagOutline,
  GridOutline,
  HomeOutline,
  MoonOutline,
  SunnyOutline
} from '@vicons/ionicons5'

const router = useRouter()
const { isLoggedIn, userInfo, logout } = useAuth()
const themeStore = useThemeStore()

// 启动Token自动刷新
useTokenRefresh()

const searchKeyword = ref('')
const showCategoryDrawer = ref(false)
const showTagDrawer = ref(false)
const showLoginModal = ref(false)
const showRegisterModal = ref(false)

const categories = ref([])
const tags = ref([])

// 创建 AbortController 用于取消请求
const abortController = new AbortController()

const userOptions = [
  {
    label: '个人中心',
    key: 'profile',
    icon: () => h(NIcon, null, { default: () => h(PersonOutline) })
  },
  {
    label: '退出登录',
    key: 'logout',
    icon: () => h(NIcon, null, { default: () => h(LogOutOutline) })
  }
]

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

const handleUserAction = (key) => {
  if (key === 'logout') {
    logout()
  } else if (key === 'profile') {
    router.push('/profile')
  }
}

const switchToRegister = () => {
  showLoginModal.value = false
  showRegisterModal.value = true
}

const switchToLogin = () => {
  showRegisterModal.value = false
  showLoginModal.value = true
}

const loadCategories = async () => {
  try {
    const res = await getCategoryList({ signal: abortController.signal })
    categories.value = res.data
  } catch (error) {
    // 如果是取消请求，不处理错误
    if (error.name === 'AbortError' || error.name === 'CanceledError') {
      return
    }
    console.error('加载分类失败:', error)
  }
}

const loadTags = async () => {
  try {
    const res = await getTagList({ signal: abortController.signal })
    tags.value = res.data
  } catch (error) {
    // 如果是取消请求，不处理错误
    if (error.name === 'AbortError' || error.name === 'CanceledError') {
      return
    }
    console.error('加载标签失败:', error)
  }
}

onMounted(() => {
  loadCategories()
  loadTags()
})

// 组件卸载时取消所有进行中的请求
onUnmounted(() => {
  abortController.abort()
})
</script>

<style scoped>
.main-layout {
  min-height: 100vh;
}

.header {
  padding: 0 20px;
  background: #001529;
  background-color: transparent;
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.3);
  border-bottom: 1px solid rgba(42, 219, 92, 0.1);
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
  color: rgba(255, 255, 255, 0.82);
  font-size: 16px;
  font-weight: 500;
  padding: 8px 16px;
  transition: all 0.3s;
}

.nav-button:hover {
  color: #2ADB5C;
  background-color: rgba(42, 219, 92, 0.1);
}

.nav-menu {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
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
  background-color: transparent;
  background: #001529;
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  color: rgba(255, 255, 255, 0.5);
  border-top: 1px solid rgba(42, 219, 92, 0.1);
}
</style>
