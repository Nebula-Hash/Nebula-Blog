import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    component: () => import(/* webpackChunkName: "layout" */ '@/layout/MainLayout.vue'),
    children: [
      {
        path: '',
        name: 'Home',
        component: () => import(/* webpackChunkName: "home" */ '@/views/Home.vue'),
        meta: {
          title: '首页'
        }
      },
      {
        path: 'article/:id',
        name: 'ArticleDetail',
        component: () => import(/* webpackChunkName: "article" */ '@/views/ArticleDetail.vue'),
        meta: {
          title: '文章详情'
        }
      },
      {
        path: 'category/:id',
        name: 'Category',
        component: () => import(/* webpackChunkName: "category" */ '@/views/Category.vue'),
        meta: {
          title: '分类'
        }
      },
      {
        path: 'tag/:id',
        name: 'Tag',
        component: () => import(/* webpackChunkName: "tag" */ '@/views/Tag.vue'),
        meta: {
          title: '标签'
        }
      },
      {
        path: 'search',
        name: 'Search',
        component: () => import(/* webpackChunkName: "search" */ '@/views/Search.vue'),
        meta: {
          title: '搜索'
        }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import(/* webpackChunkName: "error" */ '@/pages/NotFound.vue'),
    meta: {
      title: '页面未找到'
    }
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) return savedPosition
    if (to.path !== from.path) {
      return { top: 0, left: 0, behavior: 'smooth' }
    }
    return undefined
  }
})

// 路由守卫
router.beforeEach((to, from, next) => {
  // 设置页面标题
  if (to.meta.title) {
    document.title = `${to.meta.title} - 技术博客`
  }

  next()
})

export default router
