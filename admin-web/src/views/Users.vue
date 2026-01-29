<template>
  <div class="users-page">
    <n-card title="用户管理">
      <n-data-table :columns="columns" :data="userList" :loading="loading" :pagination="pagination"
        @update:page="handlePageChange" />
    </n-card>
  </div>
</template>

<script setup>
import { ref, h, onMounted } from 'vue'
import { NButton, NTag, NSpace, NAvatar } from 'naive-ui'

const loading = ref(false)
const userList = ref([])

const pagination = ref({
  page: 1,
  pageSize: 10,
  pageCount: 1,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50]
})

const columns = [
  { title: 'ID', key: 'id', width: 80 },
  {
    title: '头像',
    key: 'avatar',
    width: 80,
    render: (row) => h(NAvatar, { src: row.avatar, round: true })
  },
  { title: '用户名', key: 'username' },
  { title: '昵称', key: 'nickname' },
  { title: '邮箱', key: 'email' },
  {
    title: '角色',
    key: 'roleId',
    width: 100,
    render: (row) => {
      const roleMap = { 1: '管理员', 2: '作者', 3: '用户' }
      const colorMap = { 1: 'error', 2: 'warning', 3: 'info' }
      return h(NTag, { type: colorMap[row.roleId] }, { default: () => roleMap[row.roleId] || '-' })
    }
  },
  {
    title: '状态',
    key: 'status',
    width: 100,
    render: (row) =>
      h(NTag, { type: row.status === 1 ? 'success' : 'default' }, { default: () => (row.status === 1 ? '启用' : '禁用') })
  },
  { title: '创建时间', key: 'createTime', width: 180 }
]

const loadUsers = async () => {
  try {
    loading.value = true
    // 模拟数据
    userList.value = [
      {
        id: 1,
        username: 'admin',
        nickname: '管理员',
        email: 'admin@example.com',
        avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=admin',
        roleId: 1,
        status: 1,
        createTime: '2024-01-01 10:00:00'
      },
      {
        id: 2,
        username: 'author',
        nickname: '作者小王',
        email: 'author@example.com',
        avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=author',
        roleId: 2,
        status: 1,
        createTime: '2024-01-02 10:00:00'
      }
    ]
    pagination.value.itemCount = userList.value.length
  } catch (error) {
    console.error('加载用户列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handlePageChange = (page) => {
  pagination.value.page = page
  loadUsers()
}

onMounted(() => {
  loadUsers()
})
</script>

<style scoped>
.users-page {
  max-width: 1400px;
}
</style>
