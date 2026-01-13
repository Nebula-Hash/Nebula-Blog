<template>
  <div class="articles-page">
    <n-card title="文章管理">
      <template #header-extra>
        <n-button type="primary" @click="showModal = true">
          <template #icon>
            <n-icon :component="AddOutline" />
          </template>
          新增文章
        </n-button>
      </template>

      <n-space vertical :size="16">
        <!-- 搜索栏 -->
        <n-space>
          <n-input
            v-model:value="searchKeyword"
            placeholder="搜索文章标题"
            clearable
            style="width: 300px"
          >
            <template #prefix>
              <n-icon :component="SearchOutline" />
            </template>
          </n-input>
          <n-button type="primary" @click="loadArticles">搜索</n-button>
        </n-space>

        <!-- 文章列表 -->
        <n-data-table
          :columns="columns"
          :data="articleList"
          :loading="loading"
          :pagination="pagination"
          @update:page="handlePageChange"
        />
      </n-space>
    </n-card>

    <!-- 新增/编辑文章弹窗 -->
    <n-modal v-model:show="showModal" preset="card" title="文章编辑" style="width: 800px">
      <n-form ref="formRef" :model="formData" :rules="rules" label-placement="left" label-width="100">
        <n-form-item label="文章标题" path="title">
          <n-input v-model:value="formData.title" placeholder="请输入文章标题" />
        </n-form-item>

        <n-form-item label="文章摘要" path="summary">
          <n-input
            v-model:value="formData.summary"
            type="textarea"
            placeholder="请输入文章摘要"
            :rows="3"
          />
        </n-form-item>

        <n-form-item label="封面图">
          <n-space vertical style="width: 100%">
            <n-upload
              :max="1"
              :custom-request="handleCoverUpload"
              list-type="image-card"
              :default-file-list="coverFileList"
              @remove="handleCoverRemove"
            >
              点击上传封面图
            </n-upload>
            <n-input v-model:value="formData.coverImage" placeholder="或直接输入封面图URL" />
          </n-space>
        </n-form-item>

        <n-form-item label="文章分类" path="categoryId">
          <n-select
            v-model:value="formData.categoryId"
            :options="categoryOptions"
            placeholder="请选择分类"
          />
        </n-form-item>

        <n-form-item label="文章标签">
          <n-select
            v-model:value="formData.tagIds"
            :options="tagOptions"
            multiple
            placeholder="请选择标签"
          />
        </n-form-item>

        <n-form-item label="文章内容" path="content">
          <n-input
            v-model:value="formData.content"
            type="textarea"
            placeholder="请输入文章内容(支持Markdown)"
            :rows="15"
          />
        </n-form-item>

        <n-form-item label="是否置顶">
          <n-switch v-model:value="formData.isTop" :checked-value="1" :unchecked-value="0" />
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
import { ref, h, onMounted } from 'vue'
import { NButton, NTag, NSpace, NIcon, NPopconfirm } from 'naive-ui'
import { AddOutline, SearchOutline, CreateOutline, TrashOutline } from '@vicons/ionicons5'
import { getArticleList, publishArticle, updateArticle, deleteArticle } from '@/api/article'
import { getCategoryList } from '@/api/category'
import { getTagList } from '@/api/category'
import { uploadImage } from '@/api/file'

const loading = ref(false)
const saveLoading = ref(false)
const showModal = ref(false)
const searchKeyword = ref('')
const articleList = ref([])
const categoryOptions = ref([])
const tagOptions = ref([])
const coverFileList = ref([])

const formRef = ref(null)
const formData = ref({
  id: null,
  title: '',
  summary: '',
  coverImage: '',
  categoryId: null,
  tagIds: [],
  content: '',
  isTop: 0,
  isDraft: 0
})

const rules = {
  title: [{ required: true, message: '请输入文章标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入文章内容', trigger: 'blur' }]
}

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
  { title: '标题', key: 'title', ellipsis: { tooltip: true } },
  {
    title: '分类',
    key: 'categoryName',
    width: 120,
    render: (row) => h(NTag, { type: 'success' }, { default: () => row.categoryName || '-' })
  },
  {
    title: '作者',
    key: 'authorName',
    width: 120
  },
  {
    title: '浏览量',
    key: 'viewCount',
    width: 100
  },
  {
    title: '点赞数',
    key: 'likeCount',
    width: 100
  },
  {
    title: '状态',
    key: 'isDraft',
    width: 100,
    render: (row) =>
      h(NTag, { type: row.isDraft === 0 ? 'success' : 'warning' }, { default: () => (row.isDraft === 0 ? '已发布' : '草稿') })
  },
  {
    title: '创建时间',
    key: 'createTime',
    width: 180
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
              default: () => '确定要删除这篇文章吗？'
            }
          )
        ]
      })
  }
]

const loadArticles = async () => {
  try {
    loading.value = true
    const res = await getArticleList({
      current: pagination.value.page,
      size: pagination.value.pageSize,
      keyword: searchKeyword.value
    })
    articleList.value = res.data.records
    pagination.value.pageCount = res.data.pages
    pagination.value.itemCount = res.data.total
  } catch (error) {
    console.error('加载文章列表失败:', error)
  } finally {
    loading.value = false
  }
}

const loadCategories = async () => {
  try {
    const res = await getCategoryList()
    categoryOptions.value = res.data.map((cat) => ({
      label: cat.categoryName,
      value: cat.id
    }))
  } catch (error) {
    console.error('加载分类失败:', error)
  }
}

const loadTags = async () => {
  try {
    const res = await getTagList()
    tagOptions.value = res.data.map((tag) => ({
      label: tag.tagName,
      value: tag.id
    }))
  } catch (error) {
    console.error('加载标签失败:', error)
  }
}

const handlePageChange = (page) => {
  pagination.value.page = page
  loadArticles()
}

const handleEdit = (row) => {
  formData.value = {
    id: row.id,
    title: row.title,
    summary: row.summary,
    coverImage: row.coverImage,
    categoryId: row.categoryId,
    tagIds: row.tagIds || [],
    content: row.content,
    isTop: row.isTop,
    isDraft: 0
  }
  if (row.coverImage) {
    coverFileList.value = [
      {
        id: Date.now().toString(),
        name: 'cover',
        status: 'finished',
        url: row.coverImage
      }
    ]
  }
  showModal.value = true
}

const handleDelete = async (id) => {
  try {
    await deleteArticle(id)
    window.$message.success('删除成功')
    loadArticles()
  } catch (error) {
    console.error('删除失败:', error)
  }
}

const handleCoverUpload = async ({ file, onFinish, onError }) => {
  try {
    const res = await uploadImage(file.file)
    if (res.code === 200) {
      formData.value.coverImage = res.data
      coverFileList.value = [
        {
          id: Date.now().toString(),
          name: file.name,
          status: 'finished',
          url: res.data
        }
      ]
      window.$message.success('封面上传成功')
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

const handleCoverRemove = () => {
  formData.value.coverImage = ''
  coverFileList.value = []
  return true
}

const handleSave = async () => {
  try {
    await formRef.value?.validate()
    saveLoading.value = true

    if (formData.value.id) {
      await updateArticle(formData.value)
      window.$message.success('更新成功')
    } else {
      await publishArticle(formData.value)
      window.$message.success('发布成功')
    }

    showModal.value = false
    resetForm()
    loadArticles()
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
    summary: '',
    coverImage: '',
    categoryId: null,
    tagIds: [],
    content: '',
    isTop: 0,
    isDraft: 0
  }
  coverFileList.value = []
}

onMounted(() => {
  loadArticles()
  loadCategories()
  loadTags()
})
</script>

<style scoped>
.articles-page {
  max-width: 1400px;
}
</style>
