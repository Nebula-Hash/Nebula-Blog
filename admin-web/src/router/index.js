import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
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
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()

  // 访问登录页
  if (to.path === '/login') {
    // 已登录且Token未过期，跳转到首页
    if (userStore.token && !userStore.isTokenExpired) {
      next('/')
    } else {
      next()
    }
    return
  }

  // 访问其他页面，需要登录
  if (!userStore.token) {
    next('/login')
    return
  }

  // Token已过期，清除状态并跳转登录
  if (userStore.isTokenExpired) {
    userStore.logout()
    window.$message?.warning('登录已过期，请重新登录')
    next('/login')
    return
  }

  next()
})

export default router
