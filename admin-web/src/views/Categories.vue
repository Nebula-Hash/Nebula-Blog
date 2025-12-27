<template>
  <div class="categories-page">
    <n-card title="分类管理">
      <template #header-extra>
        <n-button type="primary" @click="handleAdd">
          <template #icon>
            <n-icon :component="AddOutline" />
          </template>
          新增分类
        </n-button>
      </template>

      <n-data-table
        :columns="columns"
        :data="categoryList"
        :loading="loading"
      />
    </n-card>

    <!-- 新增/编辑分类弹窗 -->
    <n-modal v-model:show="showModal" preset="card" :title="modalTitle" style="width: 500px">
      <n-form ref="formRef" :model="formData" :rules="rules" label-placement="left" label-width="100">
        <n-form-item label="分类名称" path="categoryName">
          <n-input v-model:value="formData.categoryName" placeholder="请输入分类名称" />
        </n-form-item>

        <n-form-item label="分类描述">
          <n-input
            v-model:value="formData.categoryDesc"
            type="textarea"
            placeholder="请输入分类描述"
            :rows="3"
          />
        </n-form-item>

        <n-form-item label="排序">
          <n-input-number v-model:value="formData.sort" placeholder="排序值" style="width: 100%" />
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
import { NButton, NSpace, NIcon, NPopconfirm } from 'naive-ui'
import { AddOutline, CreateOutline, TrashOutline } from '@vicons/ionicons5'
import { getCategoryList, createCategory, updateCategory, deleteCategory } from '@/api/category'

const loading = ref(false)
const saveLoading = ref(false)
const showModal = ref(false)
const categoryList = ref([])

const formRef = ref(null)
const formData = ref({
  id: null,
  categoryName: '',
  categoryDesc: '',
  sort: 0
})

const rules = {
  categoryName: [{ required: true, message: '请输入分类名称', trigger: 'blur' }]
}

const modalTitle = computed(() => (formData.value.id ? '编辑分类' : '新增分类'))

const columns = [
  { title: 'ID', key: 'id', width: 80 },
  { title: '分类名称', key: 'categoryName' },
  { title: '分类描述', key: 'categoryDesc', ellipsis: { tooltip: true } },
  { title: '排序', key: 'sort', width: 100 },
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
              default: () => '确定要删除这个分类吗？'
            }
          )
        ]
      })
  }
]

const loadCategories = async () => {
  try {
    loading.value = true
    const res = await getCategoryList()
    categoryList.value = res.data
  } catch (error) {
    console.error('加载分类列表失败:', error)
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
    await deleteCategory(id)
    window.$message.success('删除成功')
    loadCategories()
  } catch (error) {
    console.error('删除失败:', error)
  }
}

const handleSave = async () => {
  try {
    await formRef.value?.validate()
    saveLoading.value = true

    if (formData.value.id) {
      await updateCategory(formData.value)
      window.$message.success('更新成功')
    } else {
      await createCategory(formData.value)
      window.$message.success('创建成功')
    }

    showModal.value = false
    loadCategories()
  } catch (error) {
    console.error('保存失败:', error)
  } finally {
    saveLoading.value = false
  }
}

const resetForm = () => {
  formData.value = {
    id: null,
    categoryName: '',
    categoryDesc: '',
    sort: 0
  }
}

onMounted(() => {
  loadCategories()
})
</script>

<style scoped>
.categories-page {
  max-width: 1400px;
}
</style>
