<template>
  <div class="users-page">
    <!-- 搜索区域 -->
    <n-card style="margin-bottom: 16px">
      <n-space>
        <n-input v-model:value="searchKeyword" placeholder="搜索用户名或昵称" clearable style="width: 300px"
          @keyup.enter="handleSearch">
          <template #prefix>
            <n-icon :component="SearchOutline" />
          </template>
        </n-input>
        <n-select v-model:value="searchStatus" placeholder="用户状态" clearable style="width: 150px"
          :options="statusOptions" />
        <n-button type="primary" @click="handleSearch">搜索</n-button>
      </n-space>
    </n-card>

    <!-- Tab切换：管理员 / 普通用户 / 搜索结果 -->
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

      <n-tab-pane name="search" tab="搜索结果列表" :disabled="!isSearchMode">
        <n-card title="搜索结果">
          <n-data-table :columns="searchColumns" :data="userList" :loading="loading" :pagination="pagination"
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
          <n-space vertical style="width: 100%">
            <n-input v-model:value="formData.password" type="password" show-password-on="click"
              :placeholder="formData.id ? '留空则不修改密码' : '请输入密码（6-20个字符）'" :maxlength="20" show-count />

            <!-- 密码强度提示 -->
            <n-space v-if="formData.password" align="center" size="small">
              <span style="font-size: 12px; color: #666;">密码强度：</span>
              <n-tag :type="passwordStrengthColor" size="small">
                {{ passwordStrengthText }}
              </n-tag>
              <n-progress type="line" :percentage="passwordStrength.score"
                :status="passwordStrengthColor === 'success' ? 'success' : passwordStrengthColor === 'warning' ? 'warning' : 'error'"
                :show-indicator="false" style="width: 150px;" />
            </n-space>

            <!-- 密码建议 -->
            <n-space v-if="formData.password && passwordStrength.suggestions.length > 0" vertical size="small">
              <span style="font-size: 12px; color: #999;">建议：</span>
              <n-space vertical size="small">
                <span v-for="(suggestion, index) in passwordStrength.suggestions" :key="index"
                  style="font-size: 12px; color: #999;">
                  • {{ suggestion }}
                </span>
              </n-space>
            </n-space>
          </n-space>
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
import { ref, computed, onMounted, watch } from 'vue'
import { AddOutline, SearchOutline } from '@vicons/ionicons5'
import {
  getAdminList,
  createAdmin,
  updateAdmin,
  deleteAdmin,
  getClientList,
  createClient,
  updateClient,
  deleteClient,
  searchUsers
} from '@/api/user'
import { showSuccess, createPagination, updatePagination } from '@/utils/common'
import { createErrorHandler } from '@/utils/errorHandler'
import { createUserColumns } from '@/utils/tableColumns'
import { debounce } from '@/utils/performance'
import { checkPasswordStrength } from '@/utils/security'
import {
  usernameRules,
  passwordRulesOptional,
  nicknameRules,
  emailRules,
  introRules
} from '@/utils/validators'
import { USER_STATUS, VALIDATION_CONFIG } from '@/config/constants'

// 创建错误处理器
const errorHandler = createErrorHandler('Users')

const activeTab = ref('admin')
const loading = ref(false)
const saveLoading = ref(false)
const showModal = ref(false)
const userList = ref([])
const currentUserType = ref('admin') // 当前操作的用户类型

// 搜索相关
const searchKeyword = ref('')
const searchStatus = ref(null)
const isSearchMode = ref(false) // 标记是否处于搜索模式

// 密码强度相关
const passwordStrength = ref({ strength: 'weak', score: 0, suggestions: [] })

// 状态选项
const statusOptions = [
  { label: '启用', value: USER_STATUS.ENABLED },
  { label: '禁用', value: USER_STATUS.DISABLED }
]

const formRef = ref(null)
const formData = ref({
  id: null,
  username: '',
  password: '',
  nickname: '',
  email: '',
  avatar: '',
  intro: '',
  status: USER_STATUS.ENABLED
})

const pagination = ref(createPagination())

// 表单验证规则
const rules = {
  username: usernameRules,
  password: passwordRulesOptional(formData.value),
  nickname: nicknameRules,
  email: emailRules,
  intro: introRules
}

const modalTitle = computed(() => {
  const userTypeText = currentUserType.value === 'admin' ? '管理员' : '用户'
  return formData.value.id ? `编辑${userTypeText}` : `新增${userTypeText}`
})

// 密码强度颜色
const passwordStrengthColor = computed(() => {
  const colors = {
    weak: 'error',
    medium: 'warning',
    strong: 'success'
  }
  return colors[passwordStrength.value.strength] || 'default'
})

// 密码强度文本
const passwordStrengthText = computed(() => {
  const texts = {
    weak: '弱',
    medium: '中',
    strong: '强'
  }
  return texts[passwordStrength.value.strength] || ''
})

// 创建表格列配置
const adminColumns = createUserColumns({
  showId: true,
  showRole: false,
  showIntro: false,
  onEdit: (row) => handleEdit(row, 'admin'),
  onDelete: (row) => handleDelete(row.id, 'admin'),
  deleteConfirmText: '确定要删除这个管理员吗？'
})

const clientColumns = createUserColumns({
  showId: true,
  showRole: false,
  showIntro: true,
  onEdit: (row) => handleEdit(row, 'client'),
  onDelete: (row) => handleDelete(row.id, 'client'),
  deleteConfirmText: '确定要删除这个用户吗？'
})

const searchColumns = createUserColumns({
  showId: false,
  showRole: true,
  showIntro: true,
  onEdit: (row) => {
    const userType = row.roleKey === 'admin' ? 'admin' : 'client'
    handleEdit(row, userType)
  },
  onDelete: (row) => {
    const userType = row.roleKey === 'admin' ? 'admin' : 'client'
    handleDelete(row.id, userType)
  },
  deleteConfirmText: '确定要删除这个用户吗？'
})

// 加载用户列表
const loadUsers = async () => {
  try {
    loading.value = true
    const params = {
      current: pagination.value.page,
      size: pagination.value.pageSize
    }

    let res
    // 如果处于搜索模式，使用搜索接口
    if (activeTab.value === 'search') {
      params.keyword = searchKeyword.value.trim()
      if (searchStatus.value !== null) {
        params.status = searchStatus.value
      }
      res = await searchUsers(params)
    } else {
      // 否则使用原有的分页接口
      if (activeTab.value === 'admin') {
        res = await getAdminList(params)
      } else {
        res = await getClientList(params)
      }
    }

    userList.value = res.data.records
    updatePagination(pagination.value, res.data)
  } catch (error) {
    errorHandler.handleLoad(error, '用户列表')
  } finally {
    loading.value = false
  }
}

// 创建防抖的搜索函数
const debouncedSearch = debounce(() => {
  handleSearch()
}, 500)

// 监听搜索关键词变化，自动触发搜索
watch(searchKeyword, (newVal) => {
  if (newVal && newVal.trim().length >= VALIDATION_CONFIG.SEARCH_KEYWORD_MIN_LENGTH) {
    debouncedSearch()
  }
})

// 监听密码变化，实时检查强度
watch(() => formData.value.password, (newPassword) => {
  if (newPassword) {
    passwordStrength.value = checkPasswordStrength(newPassword)
  } else {
    passwordStrength.value = { strength: 'weak', score: 0, suggestions: [] }
  }
})

// 搜索用户
const handleSearch = () => {
  const keyword = searchKeyword.value.trim()

  // 如果关键词为空，提示用户
  if (!keyword) {
    errorHandler.handle(null, '请输入搜索关键词（至少2个字符）')
    return
  }

  // 验证关键词长度
  if (keyword.length < VALIDATION_CONFIG.SEARCH_KEYWORD_MIN_LENGTH) {
    errorHandler.handle(null, `搜索关键词至少需要${VALIDATION_CONFIG.SEARCH_KEYWORD_MIN_LENGTH}个字符`)
    return
  }

  // 切换到搜索结果Tab
  isSearchMode.value = true
  activeTab.value = 'search'
  pagination.value.page = 1
  loadUsers()
}

// Tab切换
const handleTabChange = (value) => {
  activeTab.value = value

  // 切换到管理员或普通用户Tab时，清空搜索条件
  if (value === 'admin' || value === 'client') {
    searchKeyword.value = ''
    searchStatus.value = null
    isSearchMode.value = false
  }

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
    errorHandler.handleDelete(error)
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

    const action = submitData.id ? '更新' : '新增'
    const userTypeText = currentUserType.value === 'admin' ? '管理员' : '用户'

    if (currentUserType.value === 'admin') {
      if (submitData.id) {
        await updateAdmin(submitData)
      } else {
        await createAdmin(submitData)
      }
    } else {
      if (submitData.id) {
        await updateClient(submitData)
      } else {
        await createClient(submitData)
      }
    }

    showSuccess(`${action}${userTypeText}成功`)
    showModal.value = false
    await loadUsers()
  } catch (error) {
    if (error.errors) {
      // 表单验证错误，不需要额外处理
      return
    }
    errorHandler.handleSave(error, formData.value.id ? '更新' : '新增')
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
    status: USER_STATUS.ENABLED
  }
  passwordStrength.value = { strength: 'weak', score: 0, suggestions: [] }
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
