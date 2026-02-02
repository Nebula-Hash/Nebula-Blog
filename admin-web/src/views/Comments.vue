<template>
  <div class="comments-page">
    <n-card title="评论管理">
      <!-- 搜索筛选区域 -->
      <template #header-extra>
        <n-space>
          <n-badge :value="pendingCount" :max="99" :show-zero="false">
            <n-button type="warning" @click="handleFilterPending">
              待审核
            </n-button>
          </n-badge>
          <n-button @click="handleResetFilter">重置筛选</n-button>
        </n-space>
      </template>

      <!-- 筛选表单 -->
      <n-space vertical :size="16">
        <n-form inline :model="filterForm" label-placement="left">
          <n-form-item label="文章ID">
            <n-input-number v-model:value="filterForm.articleId" placeholder="请输入文章ID" clearable style="width: 150px" />
          </n-form-item>
          <n-form-item label="用户ID">
            <n-input-number v-model:value="filterForm.userId" placeholder="请输入用户ID" clearable style="width: 150px" />
          </n-form-item>
          <n-form-item label="审核状态">
            <n-select v-model:value="filterForm.auditStatus" :options="auditStatusOptions" placeholder="请选择审核状态"
              clearable style="width: 150px" />
          </n-form-item>
          <n-form-item label="关键词">
            <n-input v-model:value="filterForm.keyword" placeholder="搜索评论内容" clearable style="width: 200px"
              @keyup.enter="handleSearch" />
          </n-form-item>
          <n-form-item>
            <n-button type="primary" @click="handleSearch">搜索</n-button>
          </n-form-item>
        </n-form>

        <!-- 批量操作 -->
        <n-space v-if="checkedRowKeys.length > 0">
          <n-button type="success" @click="handleBatchAudit(COMMENT_AUDIT_STATUS.APPROVED)">
            批量通过 ({{ checkedRowKeys.length }})
          </n-button>
          <n-button type="error" @click="handleBatchAudit(COMMENT_AUDIT_STATUS.REJECTED)">
            批量拒绝 ({{ checkedRowKeys.length }})
          </n-button>
          <n-popconfirm @positive-click="handleBatchDelete">
            <template #trigger>
              <n-button type="error">批量删除 ({{ checkedRowKeys.length }})</n-button>
            </template>
            确定要删除选中的 {{ checkedRowKeys.length }} 条评论吗？删除根评论会级联删除其所有子评论。
          </n-popconfirm>
        </n-space>

        <!-- 数据表格 -->
        <n-data-table :columns="columns" :data="commentList" :loading="loading" :pagination="pagination"
          :row-key="(row) => row.id" @update:checked-row-keys="handleCheck" @update:page="handlePageChange"
          @update:page-size="handlePageSizeChange" />
      </n-space>
    </n-card>

    <!-- 评论详情对话框 -->
    <n-modal v-model:show="showDetailModal" preset="card" title="评论详情" style="width: 600px">
      <n-descriptions v-if="currentComment" :column="1" bordered>
        <n-descriptions-item label="评论ID">{{ currentComment.id }}</n-descriptions-item>
        <n-descriptions-item label="文章标题">{{ currentComment.articleTitle }}</n-descriptions-item>
        <n-descriptions-item label="评论用户">
          <n-space align="center">
            <n-avatar :src="currentComment.avatar" :fallback-src="defaultAvatar" size="small" />
            <span>{{ currentComment.nickname }}</span>
          </n-space>
        </n-descriptions-item>
        <n-descriptions-item v-if="currentComment.replyNickname" label="回复用户">
          {{ currentComment.replyNickname }}
        </n-descriptions-item>
        <n-descriptions-item label="评论内容">
          <div style="white-space: pre-wrap; word-break: break-all">{{ currentComment.content }}</div>
        </n-descriptions-item>
        <n-descriptions-item label="点赞数">{{ currentComment.likeCount }}</n-descriptions-item>
        <n-descriptions-item label="审核状态">
          <n-tag :type="COMMENT_AUDIT_STATUS_MAP[currentComment.auditStatus].type">
            {{ currentComment.auditStatusDesc }}
          </n-tag>
        </n-descriptions-item>
        <n-descriptions-item label="评论时间">{{ currentComment.createTime }}</n-descriptions-item>
        <n-descriptions-item label="更新时间">{{ currentComment.updateTime }}</n-descriptions-item>
      </n-descriptions>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, h, onMounted, reactive } from 'vue'
import {
  NButton, NTag, NSpace, NIcon, NPopconfirm, NAvatar, NTooltip, NEllipsis
} from 'naive-ui'
import {
  TrashOutline, CheckmarkCircleOutline, CloseCircleOutline, EyeOutline
} from '@vicons/ionicons5'
import { showSuccess, showError, showWarning, createPagination, updatePagination } from '@/utils/common'
import { COMMENT_AUDIT_STATUS, COMMENT_AUDIT_STATUS_MAP } from '@/config/constants'
import {
  getCommentList, auditComment, deleteComment,
  batchAuditComments, batchDeleteComments, getPendingAuditCount
} from '@/api/comment'

// ==================== 数据定义 ====================
const loading = ref(false)
const commentList = ref([])
const pagination = ref(createPagination())
const checkedRowKeys = ref([])
const pendingCount = ref(0)
const showDetailModal = ref(false)
const currentComment = ref(null)
const defaultAvatar = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iNDgiIGhlaWdodD0iNDgiIHZpZXdCb3g9IjAgMCA0OCA0OCIgZmlsbD0ibm9uZSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48Y2lyY2xlIGN4PSIyNCIgY3k9IjI0IiByPSIyNCIgZmlsbD0iI0U1RTdFQiIvPjxwYXRoIGQ9Ik0yNCAyNkMyNy4zMTM3IDI2IDMwIDIzLjMxMzcgMzAgMjBDMzAgMTYuNjg2MyAyNy4zMTM3IDE0IDI0IDE0QzIwLjY4NjMgMTQgMTggMTYuNjg2MyAxOCAyMEMxOCAyMy4zMTM3IDIwLjY4NjMgMjYgMjQgMjZaIiBmaWxsPSIjOUM5RkE2Ii8+PHBhdGggZD0iTTEyIDM4QzEyIDMyLjQ3NzIgMTYuNDc3MiAyOCAyMiAyOEgyNkMzMS41MjI4IDI4IDM2IDMyLjQ3NzIgMzYgMzhWNDBIMTJWMzhaIiBmaWxsPSIjOUM5RkE2Ii8+PC9zdmc+'

// 筛选表单
const filterForm = reactive({
  articleId: null,
  userId: null,
  auditStatus: null,
  keyword: ''
})

// 审核状态选项
const auditStatusOptions = [
  { label: '待审核', value: COMMENT_AUDIT_STATUS.PENDING },
  { label: '审核通过', value: COMMENT_AUDIT_STATUS.APPROVED },
  { label: '审核拒绝', value: COMMENT_AUDIT_STATUS.REJECTED }
]

// ==================== 表格列定义 ====================
const columns = [
  {
    type: 'selection'
  },
  {
    title: 'ID',
    key: 'id',
    width: 80,
    fixed: 'left'
  },
  {
    title: '文章标题',
    key: 'articleTitle',
    width: 200,
    ellipsis: {
      tooltip: true
    }
  },
  {
    title: '评论用户',
    key: 'nickname',
    width: 150,
    render: (row) => h(
      NSpace,
      { align: 'center' },
      {
        default: () => [
          h(NAvatar, {
            src: row.avatar,
            fallbackSrc: defaultAvatar,
            size: 'small'
          }),
          h('span', row.nickname)
        ]
      }
    )
  },
  {
    title: '回复用户',
    key: 'replyNickname',
    width: 120,
    render: (row) => row.replyNickname || '-'
  },
  {
    title: '评论内容',
    key: 'content',
    width: 300,
    render: (row) => h(
      NTooltip,
      {},
      {
        trigger: () => h(NEllipsis, { style: 'max-width: 280px' }, { default: () => row.content }),
        default: () => row.content
      }
    )
  },
  {
    title: '点赞数',
    key: 'likeCount',
    width: 100,
    align: 'center'
  },
  {
    title: '审核状态',
    key: 'auditStatus',
    width: 120,
    align: 'center',
    render: (row) => h(
      NTag,
      { type: COMMENT_AUDIT_STATUS_MAP[row.auditStatus].type },
      { default: () => row.auditStatusDesc }
    )
  },
  {
    title: '评论时间',
    key: 'createTime',
    width: 180
  },
  {
    title: '操作',
    key: 'actions',
    width: 240,
    fixed: 'right',
    render: (row) => h(
      NSpace,
      {},
      {
        default: () => [
          // 查看详情按钮
          h(
            NButton,
            {
              size: 'small',
              onClick: () => handleViewDetail(row)
            },
            {
              icon: () => h(NIcon, null, { default: () => h(EyeOutline) }),
              default: () => '详情'
            }
          ),
          // 审核按钮（仅待审核状态显示）
          row.auditStatus === COMMENT_AUDIT_STATUS.PENDING && h(
            NButton,
            {
              size: 'small',
              type: 'success',
              onClick: () => handleAudit(row.id, COMMENT_AUDIT_STATUS.APPROVED)
            },
            {
              icon: () => h(NIcon, null, { default: () => h(CheckmarkCircleOutline) }),
              default: () => '通过'
            }
          ),
          row.auditStatus === COMMENT_AUDIT_STATUS.PENDING && h(
            NButton,
            {
              size: 'small',
              type: 'warning',
              onClick: () => handleAudit(row.id, COMMENT_AUDIT_STATUS.REJECTED)
            },
            {
              icon: () => h(NIcon, null, { default: () => h(CloseCircleOutline) }),
              default: () => '拒绝'
            }
          ),
          // 删除按钮
          h(
            NPopconfirm,
            {
              onPositiveClick: () => handleDelete(row.id)
            },
            {
              trigger: () => h(
                NButton,
                { size: 'small', type: 'error' },
                {
                  icon: () => h(NIcon, null, { default: () => h(TrashOutline) }),
                  default: () => '删除'
                }
              ),
              default: () => row.rootId === null
                ? '确定要删除这条评论吗？删除根评论会级联删除其所有子评论。'
                : '确定要删除这条评论吗？'
            }
          )
        ]
      }
    )
  }
]

// ==================== 方法定义 ====================

/**
 * 加载评论列表
 */
const loadComments = async () => {
  try {
    loading.value = true
    const params = {
      current: pagination.value.page,
      size: pagination.value.pageSize,
      ...filterForm
    }

    const res = await getCommentList(params)
    commentList.value = res.data.records
    updatePagination(pagination.value, res.data)
  } catch (error) {
    showError(error, '加载评论列表失败')
  } finally {
    loading.value = false
  }
}

/**
 * 加载待审核数量
 */
const loadPendingCount = async () => {
  try {
    const res = await getPendingAuditCount()
    pendingCount.value = res.data
  } catch (error) {
    console.error('加载待审核数量失败:', error)
  }
}

/**
 * 搜索
 */
const handleSearch = () => {
  pagination.value.page = 1
  loadComments()
}

/**
 * 重置筛选
 */
const handleResetFilter = () => {
  filterForm.articleId = null
  filterForm.userId = null
  filterForm.auditStatus = null
  filterForm.keyword = ''
  pagination.value.page = 1
  loadComments()
}

/**
 * 筛选待审核
 */
const handleFilterPending = () => {
  filterForm.auditStatus = COMMENT_AUDIT_STATUS.PENDING
  pagination.value.page = 1
  loadComments()
}

/**
 * 分页变化
 */
const handlePageChange = (page) => {
  pagination.value.page = page
  loadComments()
}

/**
 * 每页大小变化
 */
const handlePageSizeChange = (pageSize) => {
  pagination.value.pageSize = pageSize
  pagination.value.page = 1
  loadComments()
}

/**
 * 选择变化
 */
const handleCheck = (keys) => {
  checkedRowKeys.value = keys
}

/**
 * 查看详情
 */
const handleViewDetail = (row) => {
  currentComment.value = row
  showDetailModal.value = true
}

/**
 * 审核评论
 */
const handleAudit = async (id, auditStatus) => {
  try {
    await auditComment(id, auditStatus)
    const statusText = auditStatus === COMMENT_AUDIT_STATUS.APPROVED ? '通过' : '拒绝'
    showSuccess(`审核${statusText}成功`)
    loadComments()
    loadPendingCount()
  } catch (error) {
    showError(error, '审核失败')
  }
}

/**
 * 删除评论
 */
const handleDelete = async (id) => {
  try {
    await deleteComment(id)
    showSuccess('删除成功')
    loadComments()
    loadPendingCount()
  } catch (error) {
    showError(error, '删除失败')
  }
}

/**
 * 批量审核
 */
const handleBatchAudit = async (auditStatus) => {
  if (checkedRowKeys.value.length === 0) {
    showWarning('请先选择要审核的评论')
    return
  }

  try {
    const res = await batchAuditComments(checkedRowKeys.value, auditStatus)
    const result = res.data
    showSuccess(result.message)
    checkedRowKeys.value = []
    loadComments()
    loadPendingCount()
  } catch (error) {
    showError(error, '批量审核失败')
  }
}

/**
 * 批量删除
 */
const handleBatchDelete = async () => {
  if (checkedRowKeys.value.length === 0) {
    showWarning('请先选择要删除的评论')
    return
  }

  try {
    const res = await batchDeleteComments(checkedRowKeys.value)
    const result = res.data
    showSuccess(result.message)
    checkedRowKeys.value = []
    loadComments()
    loadPendingCount()
  } catch (error) {
    showError(error, '批量删除失败')
  }
}

// ==================== 生命周期 ====================
onMounted(() => {
  loadComments()
  loadPendingCount()
})
</script>

<style scoped>
.comments-page {
  max-width: 1600px;
}
</style>
