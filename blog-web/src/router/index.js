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
        path: '/article/:id',
        name: 'ArticleDetail',
        component: () => import(/* webpackChunkName: "article" */ '@/views/ArticleDetail.vue'),
        meta: {
          title: '文章详情'
        }
      },
      {
        path: '/category/:id',
        name: 'Category',
        component: () => import(/* webpackChunkName: "category" */ '@/views/Category.vue'),
        meta: {
          title: '分类'
        }
      },
      {
        path: '/tag/:id',
        name: 'Tag',
        component: () => import(/* webpackChunkName: "tag" */ '@/views/Tag.vue'),
        meta: {
          title: '标签'
        }
      },
      {
        path: '/search',
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
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach(async (to, from, next) => {
  // 设置页面标题
  if (to.meta.title) {
    document.title = `${to.meta.title} - 技术博客`
  }

  next()
})

// 路由后置守卫 - 滚动到顶部
router.afterEach((to, from) => {
  // 页面切换时滚动到顶部
  if (to.path !== from.path) {
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }
})

export default router
