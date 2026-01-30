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
          <n-input v-model:value="searchKeyword" placeholder="搜索文章标题" clearable style="width: 300px">
            <template #prefix>
              <n-icon :component="SearchOutline" />
            </template>
          </n-input>
          <n-button type="primary" @click="loadArticles">搜索</n-button>
        </n-space>

        <!-- 文章列表 -->
        <n-data-table :columns="columns" :data="articleList" :loading="loading" :pagination="pagination"
          @update:page="handlePageChange" />
      </n-space>
    </n-card>

    <!-- 新增/编辑文章弹窗 -->
    <n-modal v-model:show="showModal" preset="card" title="文章编辑" style="width: 800px">
      <n-form ref="formRef" :model="formData" :rules="rules" label-placement="left" label-width="100">
        <n-form-item label="文章标题" path="title">
          <n-input v-model:value="formData.title" placeholder="请输入文章标题" />
        </n-form-item>

        <n-form-item label="文章摘要" path="summary">
          <n-input v-model:value="formData.summary" type="textarea" placeholder="请输入文章摘要" :rows="3" />
        </n-form-item>

        <n-form-item label="封面图">
          <n-space vertical style="width: 100%">
            <n-upload :max="1" :custom-request="handleCoverUpload" list-type="image-card"
              :default-file-list="coverFileList" @remove="handleCoverRemove">
              点击上传封面图
            </n-upload>
            <n-input v-model:value="formData.coverImage" placeholder="或直接输入封面图URL" />
          </n-space>
        </n-form-item>

        <n-form-item label="文章分类" path="categoryId">
          <n-select v-model:value="formData.categoryId" :options="categoryOptions" placeholder="请选择分类" />
        </n-form-item>

        <n-form-item label="文章标签">
          <n-select v-model:value="formData.tagIds" :options="tagOptions" multiple placeholder="请选择标签" />
        </n-form-item>

        <n-form-item label="文章内容" path="content">
          <n-input v-model:value="formData.content" type="textarea" placeholder="请输入文章内容(支持Markdown)" :rows="15" />
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
import { ref, h, onMounted, computed } from 'vue'
import { NButton, NTag, NSpace, NIcon, NPopconfirm } from 'naive-ui'
import { AddOutline, SearchOutline, CreateOutline, TrashOutline } from '@vicons/ionicons5'
import { getArticleList, publishArticle, updateArticle, deleteArticle } from '@/api/article'
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
  isTop: TOP_STATUS.NO,
  isDraft: ARTICLE_STATUS.PUBLISHED
})

const rules = {
  title: articleTitleRules,
  content: articleContentRules
}

const pagination = ref(createPagination())

// 使用 computed 优化表格列配置（避免每次渲染都重新创建）
const columns = computed(() => createArticleColumns({
  onEdit: handleEdit,
  onDelete: (row) => handleDelete(row.id)
}))

const loadArticles = async () => {
  try {
    loading.value = true
    const res = await getArticleList({
      current: pagination.value.page,
      size: pagination.value.pageSize,
      keyword: searchKeyword.value
    })
    articleList.value = res.data.records
    updatePagination(pagination.value, res.data)
  } catch (error) {
    errorHandler.handleLoad(error, '文章列表')
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
    errorHandler.handleLoad(error, '分类列表', true) // 静默处理
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
    errorHandler.handleLoad(error, '标签列表', true) // 静默处理
  }
}

const handlePageChange = (page) => {
  pagination.value.page = page
  loadArticles()
}

function handleEdit(row) {
  formData.value = {
    id: row.id,
    title: row.title,
    summary: row.summary,
    coverImage: row.coverImage,
    categoryId: row.categoryId,
    tagIds: row.tagIds || [],
    content: row.content,
    isTop: row.isTop,
    isDraft: ARTICLE_STATUS.PUBLISHED
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
    showSuccess('删除成功')
    loadArticles()
  } catch (error) {
    errorHandler.handleDelete(error, '文章')
  }
}

const handleCoverUpload = async ({ file, onFinish, onError }) => {
  try {
    // TODO: 实现图片上传功能
    // const res = await uploadImage(file.file)
    // if (res.code === 200) {
    //   formData.value.coverImage = res.data
    //   coverFileList.value = [
    //     {
    //       id: Date.now().toString(),
    //       name: file.name,
    //       status: 'finished',
    //       url: res.data
    //     }
    //   ]
    //   showSuccess('封面上传成功')
    //   onFinish()
    // } else {
    //   errorHandler.handle(null, '上传失败')
    //   onError()
    // }
    errorHandler.handle(null, '图片上传功能待实现')
    onError()
  } catch (error) {
    errorHandler.handle(error, '上传失败')
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

    const action = formData.value.id ? '更新' : '发布'

    // 准备提交数据，对用户输入进行 XSS 防护
    const submitData = {
      ...formData.value,
      title: escapeHtml(formData.value.title),
      summary: escapeHtml(formData.value.summary)
      // 注意：content 是 Markdown 内容，在后端渲染时需要进行 XSS 防护
      // 这里不对 content 进行转义，因为 Markdown 需要保留特殊字符
    }

    if (submitData.id) {
      await updateArticle(submitData)
    } else {
      await publishArticle(submitData)
    }

    showSuccess(`${action}成功`)
    showModal.value = false
    resetForm()
    loadArticles()
  } catch (error) {
    if (error?.errors) {
      // 表单验证错误，不需要额外处理
      return
    }
    errorHandler.handleSave(error, formData.value.id ? '更新' : '发布')
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
    isTop: TOP_STATUS.NO,
    isDraft: ARTICLE_STATUS.PUBLISHED
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
