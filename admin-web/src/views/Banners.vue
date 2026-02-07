<template>
  <div class="banners-page">
    <n-card title="轮播图管理">
      <template #header-extra>
        <n-button type="primary" @click="handleAdd">
          <template #icon>
            <n-icon :component="AddOutline" />
          </template>
          新增轮播图
        </n-button>
      </template>

      <n-data-table :columns="columns" :data="bannerList" :loading="loading" :pagination="pagination"
        @update:page="handlePageChange" @update:page-size="handlePageSizeChange" />
    </n-card>

    <!-- 新增/编辑轮播图弹窗 -->
    <n-modal v-model:show="showModal" preset="card" :title="modalTitle" style="width: 600px">
      <n-form ref="formRef" :model="formData" :rules="rules" label-placement="left" label-width="100">
        <n-form-item label="标题" path="title">
          <n-input v-model:value="formData.title" placeholder="请输入标题" />
        </n-form-item>

        <n-form-item label="图片" path="imageUrl">
          <n-space vertical style="width: 100%">
            <n-upload :max="1" :custom-request="handleImageUpload" list-type="image-card"
              v-model:file-list="imageFileList" @remove="handleImageRemove" :disabled="uploadLoading"
              accept="image/*" />
            <n-text v-if="uploadLoading" type="info" style="font-size: 12px">
              图片上传中...
            </n-text>
            <n-text v-if="formData.imageUrl" depth="3" style="font-size: 12px">
              当前图片URL: {{ formData.imageUrl }}
            </n-text>
          </n-space>
        </n-form-item>

        <n-form-item label="关联文章" path="articleId">
          <n-select v-model:value="formData.articleId" :options="articleOptions" placeholder="请选择文章"
            filterable :loading="articlesLoading" />
        </n-form-item>

        <n-form-item label="排序">
          <n-input-number v-model:value="formData.sort" placeholder="排序值，数值越小越靠前" style="width: 100%" :min="0" />
        </n-form-item>

        <n-form-item label="状态">
          <n-switch v-model:value="formData.status" :checked-value="1" :unchecked-value="0">
            <template #checked>启用</template>
            <template #unchecked>禁用</template>
          </n-switch>
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
import { NButton, NTag, NSpace, NIcon, NPopconfirm, NImage, NText } from 'naive-ui'
import { AddOutline, CreateOutline, TrashOutline } from '@vicons/ionicons5'
import { getBannerList, getBannerDetail, getPublishedArticles, uploadBannerImage, addBanner, updateBanner, deleteBanner } from '@/api/banner'
import { formatDateTime, showSuccess, showError, showInfo, createPagination, updatePagination } from '@/utils/common'

const loading = ref(false)
const saveLoading = ref(false)
const uploadLoading = ref(false)
const articlesLoading = ref(false)
const showModal = ref(false)
const bannerList = ref([])
const imageFileList = ref([])
const articleOptions = ref([])

const pagination = ref(createPagination(10))

const formRef = ref(null)
const formData = ref({
  id: null,
  title: '',
  imageUrl: '',
  articleId: null,
  sort: 0,
  status: 1
})

const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  imageUrl: [{ required: true, message: '请上传图片', trigger: 'change' }],
  articleId: [{ required: true, type: 'number', message: '请选择关联文章', trigger: 'change' }]
}

const modalTitle = computed(() => (formData.value.id ? '编辑轮播图' : '新增轮播图'))

const columns = [
  { title: 'ID', key: 'id', width: 80 },
  { title: '标题', key: 'title', width: 150 },
  {
    title: '图片',
    key: 'imageUrl',
    width: 150,
    render: (row) => h(NImage, { src: row.imageUrl, width: 120, height: 60, objectFit: 'cover' })
  },
  {
    title: '关联文章',
    key: 'articleTitle',
    width: 200,
    ellipsis: { tooltip: true },
    render: (row) => row.articleTitle || '未关联'
  },
  { title: '排序', key: 'sort', width: 80 },
  {
    title: '状态',
    key: 'status',
    width: 80,
    render: (row) =>
      h(NTag, { type: row.status === 1 ? 'success' : 'default' }, { default: () => (row.status === 1 ? '启用' : '禁用') })
  },
  {
    title: '上次更新',
    key: 'updateTime',
    width: 180,
    render: (row) => formatDateTime(row.updateTime)
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
              onClick: () => handleEdit(row.id)
            },
            { icon: () => h(NIcon, null, { default: () => h(CreateOutline) }), default: () => '编辑' }
          ),
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
              default: () => '确定要删除这个轮播图吗？'
            }
          )
        ]
      })
  }
]

// 加载文章列表
const loadArticles = async () => {
  try {
    articlesLoading.value = true
    const res = await getPublishedArticles()
    articleOptions.value = res.data.map(article => ({
      label: article.title,
      value: article.id
    }))
  } catch (error) {
    console.error('加载文章列表失败:', error)
    showError(error, '加载文章列表失败')
  } finally {
    articlesLoading.value = false
  }
}

// 加载轮播图列表
const loadBanners = async () => {
  try {
    loading.value = true
    const res = await getBannerList(pagination.value.page, pagination.value.pageSize)
    bannerList.value = res.data.records
    updatePagination(pagination.value, res.data)
  } catch (error) {
    console.error('加载轮播图列表失败:', error)
    showError(error, '加载轮播图列表失败')
  } finally {
    loading.value = false
  }
}

// 页码变化
const handlePageChange = (page) => {
  pagination.value.page = page
  loadBanners()
}

// 每页数量变化
const handlePageSizeChange = (pageSize) => {
  pagination.value.pageSize = pageSize
  pagination.value.page = 1
  loadBanners()
}

// 新增轮播图
const handleAdd = async () => {
  resetForm()
  await loadArticles()
  showModal.value = true
}

// 编辑轮播图
const handleEdit = async (id) => {
  try {
    loading.value = true
    await loadArticles()
    const res = await getBannerDetail(id)
    const banner = res.data

    formData.value = {
      id: banner.id,
      title: banner.title,
      imageUrl: banner.imageUrl,
      articleId: banner.articleId,
      sort: banner.sort,
      status: banner.status
    }

    // 设置图片预览
    if (banner.imageUrl) {
      imageFileList.value = [
        {
          id: Date.now().toString(),
          name: 'banner',
          status: 'finished',
          url: banner.imageUrl
        }
      ]
    }

    showModal.value = true
  } catch (error) {
    console.error('获取轮播图详情失败:', error)
    showError(error, '获取轮播图详情失败')
  } finally {
    loading.value = false
  }
}

// 删除轮播图
const handleDelete = async (id) => {
  try {
    await deleteBanner(id)
    showSuccess('删除成功')
    // 如果删除后当前页没有数据，返回上一页
    if (bannerList.value.length === 1 && pagination.value.page > 1) {
      pagination.value.page--
    }
    loadBanners()
  } catch (error) {
    console.error('删除失败:', error)
    showError(error, '删除失败')
  }
}

// 上传图片
const handleImageUpload = async ({ file, onFinish, onError }) => {
  console.log('handleImageUpload 被调用, file:', file)

  try {
    // 获取原始 File 对象
    const rawFile = file.file
    console.log('原始文件对象:', rawFile)

    if (!rawFile) {
      showError('无法获取文件，请重试')
      onError()
      return
    }

    uploadLoading.value = true
    showInfo('图片上传中...')

    // 上传到临时目录
    const res = await uploadBannerImage(file.file)

    if (res.code === 200) {
      formData.value.imageUrl = res.data

      imageFileList.value = [
        {
          id: Date.now().toString(),
          name: file.name,
          status: 'finished',
          url: res.data
        }
      ]

      showSuccess('图片上传成功')
      onFinish()
    } else {
      showError(res.message || '上传失败')
      onError()
    }
  } catch (error) {
    console.error('上传失败:', error)
    showError(error, '上传失败')
    onError()
  } finally {
    uploadLoading.value = false
  }
}

// 移除图片
const handleImageRemove = () => {
  formData.value.imageUrl = ''
  imageFileList.value = []
  return true
}

// 保存轮播图
const handleSave = async () => {
  try {
    await formRef.value?.validate()
    saveLoading.value = true

    if (formData.value.id) {
      // 编辑
      await updateBanner(formData.value)
      showSuccess('更新成功')
    } else {
      // 新增
      await addBanner(formData.value)
      showSuccess('添加成功')
    }

    showModal.value = false
    loadBanners()
  } catch (error) {
    if (error?.errors) {
      // 表单验证错误
      return
    }
    console.error('保存失败:', error)
    showError(error, '保存失败')
  } finally {
    saveLoading.value = false
  }
}

// 重置表单
const resetForm = () => {
  formData.value = {
    id: null,
    title: '',
    imageUrl: '',
    articleId: null,
    sort: 0,
    status: 1
  }
  imageFileList.value = []
  formRef.value?.restoreValidation()
}

onMounted(() => {
  loadBanners()
})
</script>

<style scoped>
.banners-page {
  max-width: 1400px;
}
</style>
