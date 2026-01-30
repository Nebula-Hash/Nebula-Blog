import { createRouter, createWebHistory } from 'vue-router'
import * as tokenService from '@/services/tokenService'
import * as authService from '@/services/authService'
import { useUserStore } from '@/stores/user'
import { showWarning, showError } from '@/utils/common'

const routes = [
  {
    path: '/',
    component: () => import('@/layout/MainLayout.vue'),
    children: [
      {
        path: '',
        name: 'Home',
        component: () => import('@/views/Home.vue')
      },
      {
        path: '/article/:id',
        name: 'ArticleDetail',
        component: () => import('@/views/ArticleDetail.vue')
      },
      {
        path: '/category/:id',
        name: 'Category',
        component: () => import('@/views/Category.vue')
      },
      {
        path: '/tag/:id',
        name: 'Tag',
        component: () => import('@/views/Tag.vue')
      },
      {
        path: '/search',
        name: 'Search',
        component: () => import('@/views/Search.vue')
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

  // 不需要认证的页面
  if (!to.meta.requiresAuth) {
    next()
    return
  }

  // 需要认证的页面
  if (!tokenService.getToken()) {
    showWarning('请先登录')
    next('/')
    return
  }

  // Token已过期
  if (tokenService.isTokenExpired()) {
    userStore.clearAuth()
    showWarning('登录已过期，请重新登录')
    next('/')
    return
  }

  // 检查用户信息
  if (!userStore.userInfo) {
    try {
      await authService.getCurrentUser()
    } catch (error) {
      console.error('[Router] 获取用户信息失败:', error)
      userStore.clearAuth()
      showError('获取用户信息失败，请重新登录')
      next('/')
      return
    }
  }

  next()
})

export default router
