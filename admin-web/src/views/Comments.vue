<template>
  <div class="comments-page">
    <n-card title="评论管理">
      <n-data-table :columns="columns" :data="commentList" :loading="loading" :pagination="pagination"
        @update:page="handlePageChange" />
    </n-card>
  </div>
</template>

<script setup>
import { ref, h, onMounted } from 'vue'
import { NButton, NTag, NSpace, NIcon, NPopconfirm } from 'naive-ui'
import { TrashOutline } from '@vicons/ionicons5'
import { showSuccess, createPagination } from '@/utils/common'

const loading = ref(false)
const commentList = ref([])

const pagination = ref(createPagination())

const columns = [
  { title: 'ID', key: 'id', width: 80 },
  { title: '文章标题', key: 'articleTitle', ellipsis: { tooltip: true } },
  { title: '评论用户', key: 'username', width: 120 },
  { title: '评论内容', key: 'content', ellipsis: { tooltip: true } },
  { title: '点赞数', key: 'likeCount', width: 100 },
  {
    title: '审核状态',
    key: 'auditStatus',
    width: 120,
    render: (row) => {
      const statusMap = { 0: '待审核', 1: '审核通过', 2: '审核拒绝' }
      const colorMap = { 0: 'warning', 1: 'success', 2: 'error' }
      return h(NTag, { type: colorMap[row.auditStatus] }, { default: () => statusMap[row.auditStatus] })
    }
  },
  { title: '评论时间', key: 'createTime', width: 180 },
  {
    title: '操作',
    key: 'actions',
    width: 100,
    render: (row) =>
      h(
        NPopconfirm,
        {
          onPositiveClick: () => handleDelete(row.id)
        },
        {
          trigger: () =>
            h(
              NButton,
              { size: 'small', type: 'error' },
              { icon: () => h(NIcon, null, { default: () => h(TrashOutline) }), default: () => '删除' }
            ),
          default: () => '确定要删除这条评论吗？'
        }
      )
  }
]

const loadComments = async () => {
  try {
    loading.value = true
    // 模拟数据
    commentList.value = [
      {
        id: 1,
        articleTitle: 'Spring Boot 4.0 入门教程',
        username: '用户A',
        content: '写得真好，学习了！',
        likeCount: 5,
        auditStatus: 1,
        createTime: '2024-01-10 14:30:00'
      },
      {
        id: 2,
        articleTitle: 'Vue3 组合式API完全指南',
        username: '用户B',
        content: 'Vue3确实很强大',
        likeCount: 8,
        auditStatus: 1,
        createTime: '2024-01-11 10:20:00'
      }
    ]
    pagination.value.itemCount = commentList.value.length
  } catch (error) {
    console.error('加载评论列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handlePageChange = (page) => {
  pagination.value.page = page
  loadComments()
}

const handleDelete = async (id) => {
  showSuccess('删除成功')
  loadComments()
}

onMounted(() => {
  loadComments()
})
</script>

<style scoped>
.comments-page {
  max-width: 1400px;
}
</style>
