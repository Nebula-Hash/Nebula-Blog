<template>
  <n-layout has-sider style="height: 100vh">
    <n-layout-sider bordered collapse-mode="width" :collapsed-width="64" :width="240" :collapsed="collapsed"
      show-trigger @collapse="collapsed = true" @expand="collapsed = false">
      <div class="logo-container">
        <n-icon v-if="collapsed" :component="RocketOutline" size="28" color="#18A058" />
        <template v-else>
          <n-icon :component="RocketOutline" size="28" color="#18A058" />
          <span class="logo-text">博客管理</span>
        </template>
      </div>

      <n-menu v-model:value="activeKey" :collapsed="collapsed" :collapsed-width="64" :collapsed-icon-size="22"
        :options="menuOptions" @update:value="handleMenuSelect" />
    </n-layout-sider>

    <n-layout>
      <n-layout-header bordered
        style="height: 60px; padding: 0 24px; display: flex; align-items: center; justify-content: space-between;">
        <n-breadcrumb>
          <n-breadcrumb-item v-for="item in breadcrumbs" :key="item.name">
            {{ item.title }}
          </n-breadcrumb-item>
        </n-breadcrumb>

        <n-dropdown :options="userOptions" @select="handleUserSelect">
          <n-button text>
            <template #icon>
              <n-icon :component="PersonCircleOutline" />
            </template>
            {{ userInfo?.nickname || '用户' }}
          </n-button>
        </n-dropdown>
      </n-layout-header>

      <n-layout-content content-style="padding: 24px;" :native-scrollbar="false">
        <router-view />
      </n-layout-content>
    </n-layout>
  </n-layout>
</template>

<script setup>
import { ref, computed, h, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { NIcon } from 'naive-ui'
import {
  RocketOutline,
  DocumentTextOutline,
  FolderOutline,
  PricetagsOutline,
  PeopleOutline,
  ImagesOutline,
  ChatbubblesOutline,
  PersonCircleOutline,
  LogOutOutline,
  HomeOutline
} from '@vicons/ionicons5'
import { useAuth } from '@/composables/useAuth'

const router = useRouter()
const route = useRoute()
const { userInfo, logout } = useAuth()

const collapsed = ref(false)
const activeKey = ref(route.path)

const renderIcon = (icon) => {
  return () => h(NIcon, null, { default: () => h(icon) })
}

const menuOptions = [
  {
    label: '首页',
    key: '/datapanel',
    icon: renderIcon(HomeOutline)
  },
  {
    label: '文章管理',
    key: '/articles',
    icon: renderIcon(DocumentTextOutline)
  },
  {
    label: '分类管理',
    key: '/categories',
    icon: renderIcon(FolderOutline)
  },
  {
    label: '标签管理',
    key: '/tags',
    icon: renderIcon(PricetagsOutline)
  },
  {
    label: '用户管理',
    key: '/users',
    icon: renderIcon(PeopleOutline)
  },
  {
    label: '轮播图管理',
    key: '/banners',
    icon: renderIcon(ImagesOutline)
  },
  {
    label: '评论管理',
    key: '/comments',
    icon: renderIcon(ChatbubblesOutline)
  }
]

const userOptions = [
  {
    label: '退出登录',
    key: 'logout',
    icon: renderIcon(LogOutOutline)
  }
]

const breadcrumbs = computed(() => {
  const matched = route.matched.filter(item => item.meta?.title)
  return matched.map(item => ({
    name: item.name,
    title: item.meta.title
  }))
})

const handleMenuSelect = (key) => {
  activeKey.value = key
  router.push(key)
}

const handleUserSelect = async (key) => {
  if (key === 'logout') {
    await logout()
  }
}

watch(
  () => route.path,
  (path) => {
    activeKey.value = path
  }
)
</script>

<style scoped>
.logo-container {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  border-bottom: 1px solid #e0e0e6;
}

.logo-text {
  font-size: 18px;
  font-weight: 600;
  color: #18A058;
}
</style>
