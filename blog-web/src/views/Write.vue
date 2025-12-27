<template>
  <div class="write-page">
    <n-card class="write-card">
      <template #header>
        <n-space :size="8" align="center">
          <n-icon :component="CreateOutline" size="20" color="#2ADB5C" />
          <span style="font-weight: 600; font-size: 16px;">写文章</span>
        </n-space>
      </template>
      <n-form ref="formRef" :model="formData" :rules="rules">
        <n-form-item path="title" label="文章标题">
          <n-input v-model:value="formData.title" placeholder="请输入文章标题" />
        </n-form-item>

        <n-form-item path="summary" label="文章摘要">
          <n-input
            v-model:value="formData.summary"
            type="textarea"
            placeholder="请输入文章摘要"
            :rows="3"
          />
        </n-form-item>

        <n-form-item path="coverImage" label="封面图">
          <n-space vertical style="width: 100%;">
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

        <n-form-item path="categoryId" label="文章分类">
          <n-select
            v-model:value="formData.categoryId"
            :options="categoryOptions"
            placeholder="请选择分类"
          />
        </n-form-item>

        <n-form-item path="tagIds" label="文章标签">
          <n-select
            v-model:value="formData.tagIds"
            :options="tagOptions"
            multiple
            placeholder="请选择标签"
          />
        </n-form-item>

        <n-form-item path="content" label="文章内容">
          <n-input
            v-model:value="formData.content"
            type="textarea"
            placeholder="请输入文章内容(支持Markdown)"
            :rows="20"
          />
        </n-form-item>

        <n-form-item>
          <n-space>
            <n-button type="primary" @click="handlePublish(0)" :loading="publishLoading">
              发布文章
            </n-button>
            <n-button @click="handlePublish(1)" :loading="draftLoading">
              保存草稿
            </n-button>
            <n-button @click="router.back()">取消</n-button>
          </n-space>
        </n-form-item>
      </n-form>
    </n-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { publishArticle } from '@/api/article'
import { getCategoryList } from '@/api/category'
import { getTagList } from '@/api/tag'
import { uploadImage, uploadArticleImage } from '@/api/file'
import { NCard, NForm, NFormItem, NInput, NSelect, NButton, NSpace, NIcon, NUpload } from 'naive-ui'
import { CreateOutline } from '@vicons/ionicons5'

const router = useRouter()

const formRef = ref(null)
const publishLoading = ref(false)
const draftLoading = ref(false)
const coverFileList = ref([])

const formData = ref({
  title: '',
  summary: '',
  coverImage: '',
  categoryId: null,
  tagIds: [],
  content: '',
  isDraft: 0,
  isTop: 0
})

const rules = {
  title: [{ required: true, message: '请输入文章标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入文章内容', trigger: 'blur' }]
}

const categoryOptions = ref([])
const tagOptions = ref([])

// 封面图上传
const handleCoverUpload = async ({ file, onFinish, onError }) => {
  try {
    const res = await uploadImage(file.file)
    if (res.code === 200) {
      formData.value.coverImage = res.data
      coverFileList.value = [{
        id: Date.now().toString(),
        name: file.name,
        status: 'finished',
        url: res.data
      }]
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

// 移除封面图
const handleCoverRemove = () => {
  formData.value.coverImage = ''
  coverFileList.value = []
  return true
}

const loadCategories = async () => {
  const res = await getCategoryList()
  categoryOptions.value = res.data.map(cat => ({
    label: cat.categoryName,
    value: cat.id
  }))
}

const loadTags = async () => {
  const res = await getTagList()
  tagOptions.value = res.data.map(tag => ({
    label: tag.tagName,
    value: tag.id
  }))
}

const handlePublish = async (isDraft) => {
  try {
    await formRef.value?.validate()
    
    const loading = isDraft ? draftLoading : publishLoading
    loading.value = true

    formData.value.isDraft = isDraft

    await publishArticle(formData.value)
    window.$message.success(isDraft ? '保存草稿成功' : '发布成功')
    router.push('/my')
  } catch (error) {
    console.error(error)
  } finally {
    if (isDraft) {
      draftLoading.value = false
    } else {
      publishLoading.value = false
    }
  }
}

onMounted(() => {
  loadCategories()
  loadTags()
})
</script>

<style scoped>
.write-page {
  max-width: 900px;
  margin: 0 auto;
}

.write-card {
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
  border: 1px solid rgba(42, 219, 92, 0.1);
  background: #141517;
}
</style>
