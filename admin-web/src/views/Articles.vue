<template>
  <div class="articles-page">
    <n-card title="文章管理">
      <template #header-extra>
        <n-button type="primary" @click="handleAdd">
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
            v-model:value="searchParams.title"
            placeholder="搜索文章标题"
            clearable
            style="width: 200px"
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <n-icon :component="SearchOutline" />
            </template>
          </n-input>
          <n-input
            v-model:value="searchParams.authorName"
            placeholder="作者名称"
            clearable
            style="width: 150px"
            @keyup.enter="handleSearch"
          />
          <n-input
            v-model:value="searchParams.categoryName"
            placeholder="分类名称"
            clearable
            style="width: 150px"
            @keyup.enter="handleSearch"
          />
          <n-select
            v-model:value="searchParams.isDraft"
            :options="draftOptions"
            placeholder="发布状态"
            clearable
            style="width: 120px"
          />
          <n-select
            v-model:value="searchParams.isTop"
            :options="topOptions"
            placeholder="置顶状态"
            clearable
            style="width: 120px"
          />
          <n-button type="primary" @click="handleSearch">
            <template #icon>
              <n-icon :component="SearchOutline" />
            </template>
            搜索
          </n-button>
          <n-button @click="handleReset">重置</n-button>
        </n-space>

        <!-- 文章列表 -->
        <n-data-table
          :columns="columns"
          :data="articleList"
          :loading="loading"
          :pagination="pagination"
          :scroll-x="1400"
          @update:page="handlePageChange"
        />
      </n-space>
    </n-card>

    <!-- 新增/编辑文章弹窗 -->
    <n-modal v-model:show="showModal" preset="card" :title="modalTitle" style="width: 90%; max-width: 1200px">
      <n-form ref="formRef" :model="formData" :rules="rules" label-placement="left" label-width="100">
        <n-grid :cols="24" :x-gap="24">
          <!-- 左侧：基本信息 -->
          <n-form-item-gi :span="24" label="文章标题" path="title">
            <n-input v-model:value="formData.title" placeholder="请输入文章标题" maxlength="100" show-count />
          </n-form-item-gi>

          <n-form-item-gi :span="24" label="文章摘要" path="summary">
            <n-input
              v-model:value="formData.summary"
              type="textarea"
              placeholder="请输入文章摘要（选填，留空则自动从内容提取）"
              :rows="3"
              maxlength="200"
              show-count
            />
          </n-form-item-gi>

          <n-form-item-gi :span="12" label="文章分类" path="categoryId">
            <n-select
              v-model:value="formData.categoryId"
              :options="categoryOptions"
              placeholder="请选择分类"
              filterable
            />
          </n-form-item-gi>

          <n-form-item-gi :span="12" label="文章标签">
            <n-select
              v-model:value="formData.tagIds"
              :options="tagOptions"
              multiple
              placeholder="请选择标签（可多选）"
              filterable
            />
          </n-form-item-gi>

          <n-form-item-gi :span="12" label="是否置顶">
            <n-switch v-model:value="formData.isTop" :checked-value="1" :unchecked-value="0">
              <template #checked>是</template>
              <template #unchecked>否</template>
            </n-switch>
          </n-form-item-gi>

          <n-form-item-gi :span="12" label="发布状态">
            <n-switch v-model:value="formData.isDraft" :checked-value="0" :unchecked-value="1">
              <template #checked>发布</template>
              <template #unchecked>草稿</template>
            </n-switch>
          </n-form-item-gi>

          <!-- 封面图上传 -->
          <n-form-item-gi :span="24" label="封面图">
            <n-space vertical style="width: 100%">
              <n-upload
                :max="1"
                :custom-request="handleCoverUpload"
                list-type="image-card"
                :file-list="coverFileList"
                @remove="handleCoverRemove"
                :disabled="uploadLoading"
              >
                <n-button :loading="uploadLoading">
                  <template #icon>
                    <n-icon :component="CloudUploadOutline" />
                  </template>
                  {{ uploadLoading ? '上传中...' : '点击上传封面图' }}
                </n-button>
              </n-upload>
              <n-input
                v-model:value="formData.coverImage"
                placeholder="或直接输入封面图URL"
                clearable
              />
            </n-space>
          </n-form-item-gi>

          <!-- 文章内容 -->
          <n-form-item-gi :span="24" label="文章内容" path="content">
            <n-input
              v-model:value="formData.content"
              type="textarea"
              placeholder="请输入文章内容（支持Markdown格式）"
              :rows="20"
              :autosize="{ minRows: 20, maxRows: 30 }"
            />
          </n-form-item-gi>

          <!-- Markdown预览提示 -->
          <n-form-item-gi :span="24">
            <n-alert type="info" :show-icon="false">
              <template #header>
                <n-space align="center">
                  <n-icon :component="InformationCircleOutline" />
                  <span>Markdown 编辑提示</span>
                </n-space>
              </template>
              支持标准 Markdown 语法，包括标题、列表、代码块、图片、链接等。保存后将自动渲染为 HTML 格式。
            </n-alert>
          </n-form-item-gi>
        </n-grid>
      </n-form>

      <template #footer>
        <n-space justify="end">
          <n-button @click="showModal = false">取消</n-button>
          <n-button type="primary" @click="handleSave" :loading="saveLoading">
            {{ formData.isDraft === 1 ? '保存草稿' : '发布文章' }}
          </n-button>
        </n-space>
      </template>
    </n-modal>

    <!-- 图片预览弹窗 -->
    <n-modal v-model:show="showImagePreview" preset="card" title="图片预览" style="width: 80%; max-width: 800px">
      <img :src="previewImageUrl" alt="预览" style="width: 100%; height: auto" />
    </n-modal>
  </div>
</template>

<script setup>
import { ref, h, onMounted, computed } from 'vue'
import { NButton, NTag, NSpace, NIcon, NPopconfirm } from 'naive-ui'
import {
  AddOutline,
  SearchOutline,
  CreateOutline,
  TrashOutline,
  CloudUploadOutline,
  InformationCircleOutline
} from '@vicons/ionicons5'
import {
  getArticleList,
  getArticleDetail,
  publishArticle,
  updateArticle,
  deleteArticle,
  uploadCoverImage
} from '@/api/article'
import { getCategoryList } from '@/api/category'
import { getTagList } from '@/api/tag'
import { showSuccess, createPagination, updatePagination } from '@/utils/common'
import { createErrorHandler } from '@/utils/errorHandler'
import { createArticleColumns } from '@/utils/tableColumns'
import { articleTitleRules, articleContentRules } from '@/utils/validators'
import { escapeHtml } from '@/utils/security'
import { ARTICLE_STATUS, TOP_STATUS } from '@/config/constants'

// 创建错误处理器
const errorHandler = createErrorHandler('Articles')

const loading = ref(false)
const saveLoading = ref(false)
const uploadLoading = ref(false)
const showModal = ref(false)
const showImagePreview = ref(false)
const previewImageUrl = ref('')

// 搜索参数
const searchParams = ref({
  title: '',
  authorName: '',
  categoryName: '',
  tagName: '',
  isDraft: null,
  isTop: null
})

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
  isTop: TOP_STATUS.NO,
  isDraft: ARTICLE_STATUS.PUBLISHED
})

const rules = {
  title: articleTitleRules,
  content: articleContentRules,
  categoryId: [{ required: true, type: 'number', message: '请选择文章分类', trigger: 'change' }]
}

const pagination = ref(createPagination())

// 发布状态选项
const draftOptions = [
  { label: '已发布', value: ARTICLE_STATUS.PUBLISHED },
  { label: '草稿', value: ARTICLE_STATUS.DRAFT }
]

// 置顶状态选项
const topOptions = [
  { label: '已置顶', value: TOP_STATUS.YES },
  { label: '未置顶', value: TOP_STATUS.NO }
]

// 弹窗标题
const modalTitle = computed(() => {
  return formData.value.id ? '编辑文章' : '新增文章'
})

// 表格列配置
const columns = computed(() =>
  createArticleColumns({
    onEdit: handleEdit,
    onDelete: (row) => handleDelete(row.id),
    onView: handleImagePreview
  })
)

// 加载文章列表
const loadArticles = async () => {
  try {
    loading.value = true
    const res = await getArticleList({
      current: pagination.value.page,
      size: pagination.value.pageSize,
      ...searchParams.value
    })
    articleList.value = res.data.records
    updatePagination(pagination.value, res.data)
  } catch (error) {
    errorHandler.handleLoad(error, '文章列表')
  } finally {
    loading.value = false
  }
}

// 加载分类列表
const loadCategories = async () => {
  try {
    const res = await getCategoryList({ current: 1, size: 100 })
    categoryOptions.value = res.data.records.map((cat) => ({
      label: cat.categoryName,
      value: cat.id
    }))
  } catch (error) {
    errorHandler.handleLoad(error, '分类列表', true)
  }
}

// 加载标签列表
const loadTags = async () => {
  try {
    const res = await getTagList({ current: 1, size: 100 })
    tagOptions.value = res.data.records.map((tag) => ({
      label: tag.tagName,
      value: tag.id
    }))
  } catch (error) {
    errorHandler.handleLoad(error, '标签列表', true)
  }
}

// 搜索
const handleSearch = () => {
  pagination.value.page = 1
  loadArticles()
}

// 重置搜索
const handleReset = () => {
  searchParams.value = {
    title: '',
    authorName: '',
    categoryName: '',
    tagName: '',
    isDraft: null,
    isTop: null
  }
  handleSearch()
}

// 分页变化
const handlePageChange = (page) => {
  pagination.value.page = page
  loadArticles()
}

// 新增文章
const handleAdd = () => {
  resetForm()
  showModal.value = true
}

// 编辑文章
const handleEdit = async (row) => {
  try {
    loading.value = true
    const res = await getArticleDetail(row.id)
    const article = res.data

    formData.value = {
      id: article.id,
      title: article.title,
      summary: article.summary || '',
      coverImage: article.coverImage || '',
      categoryId: article.categoryId,
      tagIds: article.tags ? article.tags.map((tag) => tag.id) : [],
      content: article.content,
      isTop: article.isTop,
      isDraft: article.isDraft
    }

    // 设置封面图预览
    if (article.coverImage) {
      coverFileList.value = [
        {
          id: Date.now().toString(),
          name: 'cover',
          status: 'finished',
          url: article.coverImage
        }
      ]
    } else {
      coverFileList.value = []
    }

    showModal.value = true
  } catch (error) {
    errorHandler.handleLoad(error, '文章详情')
  } finally {
    loading.value = false
  }
}

// 删除文章
const handleDelete = async (id) => {
  try {
    await deleteArticle(id)
    showSuccess('删除成功')
    loadArticles()
  } catch (error) {
    errorHandler.handleDelete(error, '文章')
  }
}

// 上传封面图
const handleCoverUpload = async ({ file, onFinish, onError }) => {
  try {
    uploadLoading.value = true
    const res = await uploadCoverImage(file.file)

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
      showSuccess('封面上传成功')
      onFinish()
    } else {
      errorHandler.handle(null, res.message || '上传失败')
      onError()
    }
  } catch (error) {
    errorHandler.handle(error, '上传失败')
    onError()
  } finally {
    uploadLoading.value = false
  }
}

// 移除封面图
const handleCoverRemove = () => {
  formData.value.coverImage = ''
  coverFileList.value = []
  return true
}

// 图片预览
const handleImagePreview = (url) => {
  previewImageUrl.value = url
  showImagePreview.value = true
}

// 保存文章
const handleSave = async () => {
  try {
    await formRef.value?.validate()
    saveLoading.value = true

    const action = formData.value.id ? '更新' : '发布'
    const actionText = formData.value.isDraft === ARTICLE_STATUS.DRAFT ? '保存草稿' : action

    // 准备提交数据，对用户输入进行 XSS 防护
    const submitData = {
      ...formData.value,
      title: escapeHtml(formData.value.title),
      summary: formData.value.summary ? escapeHtml(formData.value.summary) : ''
      // 注意：content 是 Markdown 内容，在后端渲染时需要进行 XSS 防护
      // 这里不对 content 进行转义，因为 Markdown 需要保留特殊字符
    }

    if (submitData.id) {
      await updateArticle(submitData)
    } else {
      await publishArticle(submitData)
    }

    showSuccess(`${actionText}成功`)
    showModal.value = false
    resetForm()
    loadArticles()
  } catch (error) {
    if (error?.errors) {
      // 表单验证错误，不需要额外处理
      return
    }
    const actionText = formData.value.isDraft === ARTICLE_STATUS.DRAFT ? '保存草稿' : '保存'
    errorHandler.handleSave(error, actionText)
  } finally {
    saveLoading.value = false
  }
}

// 重置表单
const resetForm = () => {
  formData.value = {
    id: null,
    title: '',
    summary: '',
    coverImage: '',
    categoryId: null,
    tagIds: [],
    content: '',
    isTop: TOP_STATUS.NO,
    isDraft: ARTICLE_STATUS.PUBLISHED
  }
  coverFileList.value = []
  formRef.value?.restoreValidation()
}

onMounted(() => {
  loadArticles()
  loadCategories()
  loadTags()
})
</script>

<style scoped>
.articles-page {
  max-width: 100%;
}
</style>
