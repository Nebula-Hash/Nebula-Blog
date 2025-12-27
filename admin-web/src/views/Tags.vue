<template>
  <div class="tags-page">
    <n-card title="标签管理">
      <template #header-extra>
        <n-button type="primary" @click="handleAdd">
          <template #icon>
            <n-icon :component="AddOutline" />
          </template>
          新增标签
        </n-button>
      </template>

      <n-data-table
        :columns="columns"
        :data="tagList"
        :loading="loading"
      />
    </n-card>

    <!-- 新增/编辑标签弹窗 -->
    <n-modal v-model:show="showModal" preset="card" :title="modalTitle" style="width: 500px">
      <n-form ref="formRef" :model="formData" :rules="rules" label-placement="left" label-width="100">
        <n-form-item label="标签名称" path="tagName">
          <n-input v-model:value="formData.tagName" placeholder="请输入标签名称" />
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
import { NButton, NSpace, NIcon, NPopconfirm, NTag } from 'naive-ui'
import { AddOutline, CreateOutline, TrashOutline } from '@vicons/ionicons5'
import { getTagList, createTag, updateTag, deleteTag } from '@/api/category'

const loading = ref(false)
const saveLoading = ref(false)
const showModal = ref(false)
const tagList = ref([])

const formRef = ref(null)
const formData = ref({
  id: null,
  tagName: ''
})

const rules = {
  tagName: [{ required: true, message: '请输入标签名称', trigger: 'blur' }]
}

const modalTitle = computed(() => (formData.value.id ? '编辑标签' : '新增标签'))

const columns = [
  { title: 'ID', key: 'id', width: 80 },
  {
    title: '标签名称',
    key: 'tagName',
    render: (row) => h(NTag, { type: 'info' }, { default: () => row.tagName })
  },
  { title: '创建时间', key: 'createTime', width: 180 },
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
              default: () => '确定要删除这个标签吗？'
            }
          )
        ]
      })
  }
]

const loadTags = async () => {
  try {
    loading.value = true
    const res = await getTagList()
    tagList.value = res.data
  } catch (error) {
    console.error('加载标签列表失败:', error)
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
  showModal.value = true
}

const handleDelete = async (id) => {
  try {
    await deleteTag(id)
    window.$message.success('删除成功')
    loadTags()
  } catch (error) {
    console.error('删除失败:', error)
  }
}

const handleSave = async () => {
  try {
    await formRef.value?.validate()
    saveLoading.value = true

    if (formData.value.id) {
      await updateTag(formData.value)
      window.$message.success('更新成功')
    } else {
      await createTag(formData.value)
      window.$message.success('创建成功')
    }

    showModal.value = false
    loadTags()
  } catch (error) {
    console.error('保存失败:', error)
  } finally {
    saveLoading.value = false
  }
}

const resetForm = () => {
  formData.value = {
    id: null,
    tagName: ''
  }
}

onMounted(() => {
  loadTags()
})
</script>

<style scoped>
.tags-page {
  max-width: 1400px;
}
</style>
