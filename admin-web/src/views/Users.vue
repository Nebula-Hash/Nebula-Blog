<template>
  <div class="users-page">
    <!-- Tab切换：管理员 / 普通用户 -->
    <n-tabs v-model:value="activeTab" type="line" animated @update:value="handleTabChange">
      <n-tab-pane name="admin" tab="管理员管理">
        <n-card>
          <template #header>
            <div style="display: flex; justify-content: space-between; align-items: center; width: 100%;">
              <span>管理员列表</span>
              <n-button type="primary" @click="handleAdd('admin')">
                <template #icon>
                  <n-icon :component="AddOutline" />
                </template>
                新增管理员
              </n-button>
            </div>
          </template>

          <n-data-table :columns="adminColumns" :data="userList" :loading="loading" :pagination="pagination"
            :bordered="false" :single-line="false" @update:page="handlePageChange"
            @update:page-size="handlePageSizeChange" />
        </n-card>
      </n-tab-pane>

      <n-tab-pane name="client" tab="普通用户管理">
        <n-card>
          <template #header>
            <div style="display: flex; justify-content: space-between; align-items: center; width: 100%;">
              <span>普通用户列表</span>
              <n-button type="primary" @click="handleAdd('client')">
                <template #icon>
                  <n-icon :component="AddOutline" />
                </template>
                新增用户
              </n-button>
            </div>
          </template>

          <n-data-table :columns="clientColumns" :data="userList" :loading="loading" :pagination="pagination"
            :bordered="false" :single-line="false" @update:page="handlePageChange"
            @update:page-size="handlePageSizeChange" />
        </n-card>
      </n-tab-pane>
    </n-tabs>

    <!-- 新增/编辑用户弹窗 -->
    <n-modal v-model:show="showModal" preset="card" :title="modalTitle" style="width: 600px">
      <n-form ref="formRef" :model="formData" :rules="rules" label-placement="left" label-width="100">
        <n-form-item label="用户名" path="username">
          <n-input v-model:value="formData.username" placeholder="请输入用户名（2-20个字符）" :maxlength="20" show-count
            :disabled="!!formData.id" />
        </n-form-item>

        <n-form-item label="密码" :path="formData.id ? '' : 'password'">
          <n-input v-model:value="formData.password" type="password" show-password-on="click"
            :placeholder="formData.id ? '留空则不修改密码' : '请输入密码（6-20个字符）'" :maxlength="20" show-count />
        </n-form-item>

        <n-form-item label="昵称" path="nickname">
          <n-input v-model:value="formData.nickname" placeholder="请输入昵称（选填，最多30个字符）" :maxlength="30" show-count />
        </n-form-item>

        <n-form-item label="邮箱" path="email">
          <n-input v-model:value="formData.email" placeholder="请输入邮箱（选填）" />
        </n-form-item>

        <n-form-item label="头像" path="avatar">
          <n-input v-model:value="formData.avatar" placeholder="请输入头像URL（选填）" />
        </n-form-item>

        <n-form-item label="个人简介" path="intro">
          <n-input v-model:value="formData.intro" type="textarea" placeholder="请输入个人简介（选填，最多200个字符）" :rows="3"
            :maxlength="200" show-count />
        </n-form-item>

        <n-form-item label="状态" path="status">
          <n-radio-group v-model:value="formData.status">
            <n-radio :value="1">启用</n-radio>
            <n-radio :value="0">禁用</n-radio>
          </n-radio-group>
        </n-form-item>
      </n-form>

      <template #footer>
        <n-space justify="end">
          <n-button @click="showModal = false">取消</n-button>
          <n-button type="primary" @click="handleSave" :loading="saveLoading">保存</n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, h, onMounted, computed } from 'vue'
import { NButton, NSpace, NIcon, NPopconfirm, NTag, NAvatar } from 'naive-ui'
import { AddOutline, CreateOutline, TrashOutline } from '@vicons/ionicons5'
import {
  getAdminList,
  createAdmin,
  updateAdmin,
  deleteAdmin,
  getClientList,
  createClient,
  updateClient,
  deleteClient
} from '@/api/user'
import { formatDateTime, showSuccess, showError, createPagination, updatePagination, getUserInitial } from '@/utils/common'

const activeTab = ref('admin')
const loading = ref(false)
const saveLoading = ref(false)
const showModal = ref(false)
const userList = ref([])
const currentUserType = ref('admin') // 当前操作的用户类型

const formRef = ref(null)
const formData = ref({
  id: null,
  username: '',
  password: '',
  nickname: '',
  email: '',
  avatar: '',
  intro: '',
  status: 1
})

const pagination = ref(createPagination())

// 表单验证规则
const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '用户名长度需在2-20个字符之间', trigger: 'blur' }
  ],
  password: [
    {
      validator: (rule, value) => {
        // 新增时密码必填
        if (!formData.value.id && !value) {
          return new Error('请输入密码')
        }
        // 如果有输入密码，验证长度
        if (value && (value.length < 6 || value.length > 20)) {
          return new Error('密码长度需在6-20个字符之间')
        }
        return true
      },
      trigger: 'blur'
    }
  ],
  nickname: [
    { max: 30, message: '昵称长度不能超过30个字符', trigger: 'blur' }
  ],
  email: [
    {
      validator: (rule, value) => {
        // 邮箱为选填，但如果填写了则需要验证格式
        if (value && !/^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/.test(value)) {
          return new Error('邮箱格式不正确')
        }
        return true
      },
      trigger: 'blur'
    }
  ],
  intro: [
    { max: 200, message: '个人简介不能超过200个字符', trigger: 'blur' }
  ]
}

const modalTitle = computed(() => {
  const userTypeText = currentUserType.value === 'admin' ? '管理员' : '用户'
  return formData.value.id ? `编辑${userTypeText}` : `新增${userTypeText}`
})

// 创建表格列配置（提取公共逻辑，减少重复代码）
const createColumns = (userType) => {
  const userTypeText = userType === 'admin' ? '管理员' : '用户'
  const isAdmin = userType === 'admin'

  const baseColumns = [
    { title: 'ID', key: 'id', width: 80 },
    {
      title: '头像',
      key: 'avatar',
      width: 80,
      render: (row) => {
        if (row.avatar) {
          return h(NAvatar, { src: row.avatar, round: true })
        }
        return h(NAvatar, { round: true }, { default: () => getUserInitial(row.username) })
      }
    },
    { title: '用户名', key: 'username', width: 150 },
    { title: '昵称', key: 'nickname', width: 150 },
    { title: '邮箱', key: 'email', ellipsis: { tooltip: true } }
  ]

  // 普通用户显示个人简介列
  if (!isAdmin) {
    baseColumns.push({
      title: '个人简介',
      key: 'intro',
      ellipsis: { tooltip: true },
      render: (row) => row.intro || '-'
    })
  }

  // 添加状态、创建时间和操作列
  baseColumns.push(
    {
      title: '状态',
      key: 'status',
      width: 100,
      render: (row) =>
        h(
          NTag,
          { type: row.status === 1 ? 'success' : 'default' },
          { default: () => (row.status === 1 ? '启用' : '禁用') }
        )
    },
    {
      title: '创建时间',
      key: 'createTime',
      width: 180,
      render: (row) => formatDateTime(row.createTime)
    },
    {
      title: '操作',
      key: 'actions',
      width: 180,
      fixed: 'right',
      render: (row) =>
        h(NSpace, null, {
          default: () => [
            h(
              NButton,
              {
                size: 'small',
                type: 'primary',
                onClick: () => handleEdit(row, userType)
              },
              { icon: () => h(NIcon, null, { default: () => h(CreateOutline) }), default: () => '编辑' }
            ),
            h(
              NPopconfirm,
              {
                onPositiveClick: () => handleDelete(row.id, userType)
              },
              {
                trigger: () =>
                  h(
                    NButton,
                    { size: 'small', type: 'error' },
                    { icon: () => h(NIcon, null, { default: () => h(TrashOutline) }), default: () => '删除' }
                  ),
                default: () => `确定要删除这个${userTypeText}吗？`
              }
            )
          ]
        })
    }
  )

  return baseColumns
}

// 管理员列表列配置
const adminColumns = createColumns('admin')

// 普通用户列表列配置
const clientColumns = createColumns('client')

// 加载用户列表
const loadUsers = async () => {
  try {
    loading.value = true
    const params = {
      current: pagination.value.page,
      size: pagination.value.pageSize
    }

    let res
    if (activeTab.value === 'admin') {
      res = await getAdminList(params)
    } else {
      res = await getClientList(params)
    }

    userList.value = res.data.records
    updatePagination(pagination.value, res.data)
  } catch (error) {
    console.error('加载用户列表失败:', error)
    showError(error, '加载用户列表失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// Tab切换
const handleTabChange = (value) => {
  activeTab.value = value
  pagination.value.page = 1
  loadUsers()
}

// 分页变化
const handlePageChange = (page) => {
  pagination.value.page = page
  loadUsers()
}

// 每页大小变化
const handlePageSizeChange = (pageSize) => {
  pagination.value.pageSize = pageSize
  pagination.value.page = 1
  loadUsers()
}

// 新增用户
const handleAdd = (userType) => {
  resetForm()
  currentUserType.value = userType
  showModal.value = true
}

// 编辑用户
const handleEdit = (row, userType) => {
  formData.value = {
    id: row.id,
    username: row.username,
    password: '', // 编辑时密码留空
    nickname: row.nickname || '',
    email: row.email || '',
    avatar: row.avatar || '',
    intro: row.intro || '',
    status: row.status
  }
  currentUserType.value = userType
  showModal.value = true
}

// 删除用户
const handleDelete = async (id, userType) => {
  try {
    if (userType === 'admin') {
      await deleteAdmin(id)
    } else {
      await deleteClient(id)
    }
    showSuccess('删除成功')
    await loadUsers()
  } catch (error) {
    console.error('删除失败:', error)
    showError(error, '删除失败，请稍后重试')
  }
}

// 保存用户
const handleSave = async () => {
  try {
    await formRef.value?.validate()
    saveLoading.value = true

    // 准备提交数据
    const submitData = { ...formData.value }

    // 如果是编辑且密码为空，则不提交密码字段
    if (submitData.id && !submitData.password) {
      delete submitData.password
    }

    if (currentUserType.value === 'admin') {
      if (submitData.id) {
        await updateAdmin(submitData)
        showSuccess('更新管理员成功')
      } else {
        await createAdmin(submitData)
        showSuccess('新增管理员成功')
      }
    } else {
      if (submitData.id) {
        await updateClient(submitData)
        showSuccess('更新用户成功')
      } else {
        await createClient(submitData)
        showSuccess('新增用户成功')
      }
    }

    showModal.value = false
    await loadUsers()
  } catch (error) {
    console.error('保存失败:', error)
    if (error.errors) {
      // 表单验证错误
      return
    }
    showError(error, '保存失败，请稍后重试')
  } finally {
    saveLoading.value = false
  }
}

// 重置表单
const resetForm = () => {
  formData.value = {
    id: null,
    username: '',
    password: '',
    nickname: '',
    email: '',
    avatar: '',
    intro: '',
    status: 1
  }
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
