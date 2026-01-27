import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

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
      },
      {
        path: '/write',
        name: 'Write',
        component: () => import('@/views/Write.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: '/my',
        name: 'My',
        component: () => import('@/views/My.vue'),
        meta: { requiresAuth: true }
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
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()

  // 需要登录的页面
  if (to.meta.requiresAuth) {
    // 未登录
    if (!userStore.token) {
      window.$message?.warning('请先登录')
      next('/')
      return
    }

    // Token已过期，清除状态并提示
    if (userStore.isTokenExpired) {
      userStore.clearAuth()
      window.$message?.warning('登录已过期，请重新登录')
      next('/')
      return
    }
  }

  next()
})

export default router
