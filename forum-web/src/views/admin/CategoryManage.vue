<template>
  <div class="category-manage-page">
    <el-card>
      <template #header>
        <div class="header">
          <span>版块管理</span>
          <el-button type="primary" @click="handleAdd">新增分类</el-button>
        </div>
      </template>

      <el-table :data="categories" v-loading="loading" row-key="id">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="名称" />
        <el-table-column prop="code" label="编码" />
        <el-table-column prop="description" label="描述" />
        <el-table-column prop="sortOrder" label="排序" width="80" />
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="primary" @click="handleAddForum(row)">添加版块</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 分类表单对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form :model="categoryForm" label-width="80px">
        <el-form-item label="名称" required>
          <el-input v-model="categoryForm.name" placeholder="请输入分类名称" maxlength="50" />
        </el-form-item>
        <el-form-item label="编码">
          <el-input v-model="categoryForm.code" placeholder="请输入分类编码" maxlength="50" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="categoryForm.description" type="textarea" :rows="3" placeholder="请输入分类描述" maxlength="200" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="categoryForm.sortOrder" :min="0" :max="999" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 版块表单对话框 -->
    <el-dialog v-model="forumDialogVisible" title="添加版块" width="500px">
      <el-form :model="forumForm" label-width="80px">
        <el-form-item label="所属分类">
          <el-input :value="currentCategory?.name" disabled />
        </el-form-item>
        <el-form-item label="名称" required>
          <el-input v-model="forumForm.name" placeholder="请输入版块名称" maxlength="50" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="forumForm.description" type="textarea" :rows="3" placeholder="请输入版块描述" maxlength="200" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="forumDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitForum">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCategoryList, createCategory, updateCategory, deleteCategory, createForum } from '@/api/category'
import type { CategoryVO } from '@/types'

const loading = ref(false)
const categories = ref<CategoryVO[]>([])

// 分类表单对话框
const dialogVisible = ref(false)
const dialogTitle = ref('新增分类')
const editingId = ref<number | null>(null)
const submitting = ref(false)
const categoryForm = reactive({
  name: '',
  code: '',
  description: '',
  sortOrder: 0
})

// 版块表单对话框
const forumDialogVisible = ref(false)
const currentCategory = ref<CategoryVO | null>(null)
const forumForm = reactive({
  name: '',
  description: ''
})

async function fetchCategories() {
  loading.value = true
  try {
    categories.value = await getCategoryList()
  } catch (e) {
    console.error('获取分类失败:', e)
    categories.value = []
  } finally {
    loading.value = false
  }
}

function handleAdd() {
  dialogTitle.value = '新增分类'
  editingId.value = null
  Object.assign(categoryForm, {
    name: '',
    code: '',
    description: '',
    sortOrder: 0
  })
  dialogVisible.value = true
}

function handleEdit(row: CategoryVO) {
  dialogTitle.value = '编辑分类'
  editingId.value = row.id
  Object.assign(categoryForm, {
    name: row.name,
    code: row.code || '',
    description: row.description || '',
    sortOrder: row.sortOrder || 0
  })
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!categoryForm.name.trim()) {
    ElMessage.warning('请输入分类名称')
    return
  }
  
  submitting.value = true
  try {
    if (editingId.value) {
      await updateCategory(editingId.value, categoryForm)
      ElMessage.success('更新成功')
    } else {
      await createCategory(categoryForm)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    fetchCategories()
  } catch (e) {
    console.error('操作失败:', e)
  } finally {
    submitting.value = false
  }
}

function handleAddForum(row: CategoryVO) {
  currentCategory.value = row
  Object.assign(forumForm, {
    name: '',
    description: ''
  })
  forumDialogVisible.value = true
}

async function submitForum() {
  if (!forumForm.name.trim()) {
    ElMessage.warning('请输入版块名称')
    return
  }
  if (!currentCategory.value) return
  
  submitting.value = true
  try {
    await createForum({
      categoryId: currentCategory.value.id,
      name: forumForm.name,
      description: forumForm.description
    })
    ElMessage.success('创建成功')
    forumDialogVisible.value = false
    fetchCategories()
  } catch (e) {
    console.error('创建版块失败:', e)
  } finally {
    submitting.value = false
  }
}

async function handleDelete(row: CategoryVO) {
  try {
    await ElMessageBox.confirm('确定要删除该分类吗？删除分类将同时删除该分类下的所有版块', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteCategory(row.id)
    ElMessage.success('删除成功')
    fetchCategories()
  } catch (e) {
    // 用户取消
  }
}

onMounted(() => {
  fetchCategories()
})
</script>

<style scoped lang="scss">
.category-manage-page {
  .header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-size: 18px;
    font-weight: 500;
  }
}
</style>
