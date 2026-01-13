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
          <n-input
            v-model:value="searchKeyword"
            placeholder="搜索文章..."
            style="width: 200px"
            @keyup.enter="handleSearch"
          >
            <template #suffix>
              <n-icon :component="SearchOutline" @click="handleSearch" style="cursor: pointer" />
            </template>
          </n-input>
          <n-button
            text
            @click="themeStore.toggleTheme"
            class="nav-button"
            title="切换主题"
          >
            <template #icon>
              <n-icon :component="themeStore.isDark ? SunnyOutline : MoonOutline" size="20" />
            </template>
          </n-button>
        </n-space>

        <n-space class="user-actions" v-if="userStore.token">
          <n-button type="primary" @click="router.push('/write')">
            <template #icon>
              <n-icon :component="CreateOutline" />
            </template>
            写文章
          </n-button>
          <n-dropdown :options="userOptions" @select="handleUserAction">
            <n-avatar round :size="40" :src="userStore.userInfo?.avatar" />
          </n-dropdown>
        </n-space>
        <n-space v-else>
          <n-button @click="showLogin = true">登录</n-button>
          <n-button type="primary" @click="showRegister = true">注册</n-button>
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
        <n-tag
          v-for="tag in tags"
          :key="tag.id"
          :bordered="false"
          round
          style="cursor: pointer"
          @click="goToTag(tag.id)"
        >
          {{ tag.tagName }} ({{ tag.articleCount }})
        </n-tag>
      </n-space>
    </n-drawer-content>
  </n-drawer>

  <!-- 登录对话框 -->
  <n-modal v-model:show="showLogin" preset="card" title="登录" style="width: 400px">
    <n-form ref="loginFormRef" :model="loginForm" :rules="loginRules">
      <n-form-item path="username" label="用户名">
        <n-input v-model:value="loginForm.username" placeholder="请输入用户名" />
      </n-form-item>
      <n-form-item path="password" label="密码">
        <n-input
          v-model:value="loginForm.password"
          type="password"
          placeholder="请输入密码"
          @keyup.enter="handleLogin"
        />
      </n-form-item>
    </n-form>
    <template #footer>
      <n-space justify="end">
        <n-button @click="showLogin = false">取消</n-button>
        <n-button type="primary" @click="handleLogin" :loading="loginLoading">登录</n-button>
      </n-space>
    </template>
  </n-modal>

  <!-- 注册对话框 -->
  <n-modal v-model:show="showRegister" preset="card" title="注册" style="width: 400px">
    <n-form ref="registerFormRef" :model="registerForm" :rules="registerRules">
      <n-form-item path="username" label="用户名">
        <n-input v-model:value="registerForm.username" placeholder="请输入用户名" />
      </n-form-item>
      <n-form-item path="nickname" label="昵称">
        <n-input v-model:value="registerForm.nickname" placeholder="请输入昵称" />
      </n-form-item>
      <n-form-item path="email" label="邮箱">
        <n-input v-model:value="registerForm.email" placeholder="请输入邮箱" />
      </n-form-item>
      <n-form-item path="password" label="密码">
        <n-input v-model:value="registerForm.password" type="password" placeholder="请输入密码" />
      </n-form-item>
      <n-form-item path="confirmPassword" label="确认密码">
        <n-input
          v-model:value="registerForm.confirmPassword"
          type="password"
          placeholder="请再次输入密码"
          @keyup.enter="handleRegister"
        />
      </n-form-item>
    </n-form>
    <template #footer>
      <n-space justify="end">
        <n-button @click="showRegister = false">取消</n-button>
        <n-button type="primary" @click="handleRegister" :loading="registerLoading">注册</n-button>
      </n-space>
    </template>
  </n-modal>
</template>

<script setup>
import { ref, onMounted, h } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useThemeStore } from '@/stores/theme'
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
  NAvatar,
  NDropdown,
  NDrawer,
  NDrawerContent,
  NList,
  NListItem,
  NTag,
  NModal,
  NForm,
  NFormItem
} from 'naive-ui'
import {
  SearchOutline,
  CreateOutline,
  PersonOutline,
  DocumentTextOutline,
  LogOutOutline,
  BookOutline,
  NewspaperOutline,
  PricetagOutline,
  GridOutline,
  HomeOutline,
  MoonOutline,
  SunnyOutline
} from '@vicons/ionicons5'

const router = useRouter()
const userStore = useUserStore()
const themeStore = useThemeStore()

const searchKeyword = ref('')
const showCategoryDrawer = ref(false)
const showTagDrawer = ref(false)
const showLogin = ref(false)
const showRegister = ref(false)

const categories = ref([])
const tags = ref([])

const loginForm = ref({
  username: '',
  password: ''
})

const registerForm = ref({
  username: '',
  nickname: '',
  email: '',
  password: '',
  confirmPassword: ''
})

const loginFormRef = ref(null)
const registerFormRef = ref(null)
const loginLoading = ref(false)
const registerLoading = ref(false)

const loginRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const registerRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  email: [{ required: true, type: 'email', message: '请输入正确的邮箱', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    {
      validator: (rule, value) => value === registerForm.value.password,
      message: '两次密码不一致',
      trigger: 'blur'
    }
  ]
}

const userOptions = [
  {
    label: '个人中心',
    key: 'profile',
    icon: () => h(NIcon, null, { default: () => h(PersonOutline) })
  },
  {
    label: '我的文章',
    key: 'my',
    icon: () => h(NIcon, null, { default: () => h(DocumentTextOutline) })
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

const handleLogin = async () => {
  try {
    await loginFormRef.value?.validate()
    loginLoading.value = true
    await userStore.handleLogin(loginForm.value)
    window.$message.success('登录成功')
    showLogin.value = false
    loginForm.value = { username: '', password: '' }
  } catch (error) {
    console.error(error)
  } finally {
    loginLoading.value = false
  }
}

const handleRegister = async () => {
  try {
    await registerFormRef.value?.validate()
    registerLoading.value = true
    await userStore.handleRegister(registerForm.value)
    window.$message.success('注册成功')
    showRegister.value = false
    registerForm.value = {
      username: '',
      nickname: '',
      email: '',
      password: '',
      confirmPassword: ''
    }
  } catch (error) {
    console.error(error)
  } finally {
    registerLoading.value = false
  }
}

const handleUserAction = (key) => {
  if (key === 'logout') {
    userStore.handleLogout()
    window.$message.success('已退出登录')
    router.push('/')
  } else if (key === 'my') {
    router.push('/my')
  }
}

const loadCategories = async () => {
  const res = await getCategoryList()
  categories.value = res.data
}

const loadTags = async () => {
  const res = await getTagList()
  tags.value = res.data
}

onMounted(() => {
  loadCategories()
  loadTags()
  if (userStore.token && !userStore.userInfo) {
    userStore.fetchUserInfo()
  }
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
