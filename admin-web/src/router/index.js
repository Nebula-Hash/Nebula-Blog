import { createRouter, createWebHistory } from 'vue-router'
import * as tokenService from '@/services/tokenService'
import * as authService from '@/services/authService'
import { useUserStore } from '@/stores/user'
import { showWarning, showError } from '@/utils/common'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/components/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/',
    component: () => import('@/layout/MainLayout.vue'),
    redirect: '/datapanel',
    children: [
      {
        path: '/datapanel',
        name: 'DataPanel',
        component: () => import('@/views/DataPanel.vue'),
        meta: { title: '首页' }
      },
      {
        path: '/articles',
        name: 'Articles',
        component: () => import('@/views/Articles.vue'),
        meta: { title: '文章管理' }
      },
      {
        path: '/categories',
        name: 'Categories',
        component: () => import('@/views/Categories.vue'),
        meta: { title: '分类管理' }
      },
      {
        path: '/tags',
        name: 'Tags',
        component: () => import('@/views/Tags.vue'),
        meta: { title: '标签管理' }
      },
      {
        path: '/users',
        name: 'Users',
        component: () => import('@/views/Users.vue'),
        meta: { title: '用户管理' }
      },
      {
        path: '/banners',
        name: 'Banners',
        component: () => import('@/views/Banners.vue'),
        meta: { title: '轮播图管理' }
      },
      {
        path: '/comments',
        name: 'Comments',
        component: () => import('@/views/Comments.vue'),
        meta: { title: '评论管理' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/components/NotFound.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach(async (to, from, next) => {
  const userStore = useUserStore()

  // 访问登录页
  if (to.path === '/login') {
    // 已登录且Token未过期，跳转到首页
    if (userStore.isLoggedIn) {
      next('/')
    } else {
      next()
    }
    return
  }

  // 访问其他页面，需要登录
  if (!tokenService.getToken()) {
    next('/login')
    return
  }

  // Token已过期
  if (tokenService.isTokenExpired()) {
    userStore.clearAuth()
    showWarning('登录已过期，请重新登录')
    next('/login')
    return
  }

  // 检查用户信息，不存在则自动获取
  if (!userStore.userInfo) {
    try {
      await authService.getCurrentUser()
    } catch (error) {
      console.error('[Router] 获取用户信息失败:', error)
      userStore.clearAuth()
      showError('获取用户信息失败，请重新登录')
      next('/login')
      return
    }
  }

  // 注意：管理员角色检查已由后端 Sa-Token 拦截器完成
  // 如果用户不是管理员，后端会返回 403 错误
  // 前端可选择在登录成功后验证 roleKey，提前拦截非管理员用户
  // 但这不是必须的，因为后端已做了完整的权限校验

  next()
})

export default router
