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

      <n-data-table :columns="columns" :data="bannerList" :loading="loading" />
    </n-card>

    <!-- 新增/编辑轮播图弹窗 -->
    <n-modal v-model:show="showModal" preset="card" :title="modalTitle" style="width: 600px">
      <n-form ref="formRef" :model="formData" :rules="rules" label-placement="left" label-width="100">
        <n-form-item label="标题" path="title">
          <n-input v-model:value="formData.title" placeholder="请输入标题" />
        </n-form-item>

        <n-form-item label="图片" path="imageUrl">
          <n-space vertical style="width: 100%">
            <n-upload
              :max="1"
              :custom-request="handleImageUpload"
              list-type="image-card"
              :default-file-list="imageFileList"
              @remove="handleImageRemove"
            >
              点击上传图片
            </n-upload>
            <n-input v-model:value="formData.imageUrl" placeholder="或直接输入图片URL" />
          </n-space>
        </n-form-item>

        <n-form-item label="链接URL">
          <n-input v-model:value="formData.linkUrl" placeholder="请输入链接URL" />
        </n-form-item>

        <n-form-item label="排序">
          <n-input-number v-model:value="formData.sort" placeholder="排序值" style="width: 100%" />
        </n-form-item>

        <n-form-item label="状态">
          <n-switch v-model:value="formData.status" :checked-value="1" :unchecked-value="0" />
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
import { NButton, NTag, NSpace, NIcon, NPopconfirm, NImage } from 'naive-ui'
import { AddOutline, CreateOutline, TrashOutline } from '@vicons/ionicons5'
import { uploadImage } from '@/api/file'

const loading = ref(false)
const saveLoading = ref(false)
const showModal = ref(false)
const bannerList = ref([])
const imageFileList = ref([])

const formRef = ref(null)
const formData = ref({
  id: null,
  title: '',
  imageUrl: '',
  linkUrl: '',
  sort: 0,
  status: 1
})

const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  imageUrl: [{ required: true, message: '请上传图片', trigger: 'blur' }]
}

const modalTitle = computed(() => (formData.value.id ? '编辑轮播图' : '新增轮播图'))

const columns = [
  { title: 'ID', key: 'id', width: 80 },
  { title: '标题', key: 'title' },
  {
    title: '图片',
    key: 'imageUrl',
    width: 120,
    render: (row) => h(NImage, { src: row.imageUrl, width: 100, height: 50, objectFit: 'cover' })
  },
  { title: '链接', key: 'linkUrl', ellipsis: { tooltip: true } },
  { title: '排序', key: 'sort', width: 100 },
  {
    title: '状态',
    key: 'status',
    width: 100,
    render: (row) =>
      h(NTag, { type: row.status === 1 ? 'success' : 'default' }, { default: () => (row.status === 1 ? '启用' : '禁用') })
  },
  {
    title: '操作',
    key: 'actions',
    width: 180,
    render: (row) =>
      h(NSpace, null, {
        default: () => [
          h(
            NButton,
            {
              size: 'small',
              type: 'primary',
              onClick: () => handleEdit(row)
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

const loadBanners = async () => {
  try {
    loading.value = true
    // 模拟数据
    bannerList.value = [
      {
        id: 1,
        title: '欢迎来到博客系统',
        imageUrl: 'https://picsum.photos/1920/500?random=1',
        linkUrl: '/article/1',
        sort: 1,
        status: 1
      },
      {
        id: 2,
        title: '技术分享平台',
        imageUrl: 'https://picsum.photos/1920/500?random=2',
        linkUrl: '/article/2',
        sort: 2,
        status: 1
      }
    ]
  } catch (error) {
    console.error('加载轮播图列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  resetForm()
  showModal.value = true
}

const handleEdit = (row) => {
  formData.value = { ...row }
  if (row.imageUrl) {
    imageFileList.value = [
      {
        id: Date.now().toString(),
        name: 'banner',
        status: 'finished',
        url: row.imageUrl
      }
    ]
  }
  showModal.value = true
}

const handleDelete = async (id) => {
  window.$message.success('删除成功')
  loadBanners()
}

const handleImageUpload = async ({ file, onFinish, onError }) => {
  try {
    const res = await uploadImage(file.file)
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
      window.$message.success('图片上传成功')
      onFinish()
    } else {
      window.$message.error(res.msg || '上传失败')
      onError()
    }
  } catch (error) {
    console.error('上传失败:', error)
    window.$message.error('上传失败')
    onError()
  }
}

const handleImageRemove = () => {
  formData.value.imageUrl = ''
  imageFileList.value = []
  return true
}

const handleSave = async () => {
  try {
    await formRef.value?.validate()
    saveLoading.value = true

    window.$message.success(formData.value.id ? '更新成功' : '创建成功')
    showModal.value = false
    loadBanners()
  } catch (error) {
    console.error('保存失败:', error)
  } finally {
    saveLoading.value = false
  }
}

const resetForm = () => {
  formData.value = {
    id: null,
    title: '',
    imageUrl: '',
    linkUrl: '',
    sort: 0,
    status: 1
  }
  imageFileList.value = []
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
