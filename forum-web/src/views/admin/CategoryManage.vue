<template>
  <div class="category-manage-page">
    <el-card>
      <template #header>
        <div class="header">
          <span>版块管理</span>
          <el-button type="primary" @click="handleAdd">新增分类</el-button>
        </div>
      </template>

      <el-table :data="flattenedCategories" v-loading="loading" row-key="id">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="名称">
          <template #default="{ row }">
            <span :style="{ paddingLeft: (row._level || 0) * 20 + 'px' }">
              {{ row.name }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row._isForum ? 'success' : 'primary'" size="small">
              {{ row._isForum ? '版块' : '分类' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column prop="sort" label="排序" width="80">
          <template #default="{ row }">
            {{ row.sort || row.sortOrder || 0 }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <template v-if="!row._isForum">
              <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
              <el-button link type="primary" @click="handleAddForum(row)">添加版块</el-button>
              <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
            </template>
            <template v-else>
              <el-button link type="primary" @click="handleEditForum(row)">编辑</el-button>
              <el-button link type="danger" @click="handleDeleteForum(row)">删除</el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 分类表单对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form ref="categoryFormRef" :model="categoryForm" :rules="categoryRules" label-width="80px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="categoryForm.name" placeholder="请输入分类名称" maxlength="50" />
        </el-form-item>
        <el-form-item label="图标">
          <el-input v-model="categoryForm.icon" placeholder="请输入图标名称" maxlength="50" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="categoryForm.description" type="textarea" :rows="3" placeholder="请输入分类描述" maxlength="200" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="categoryForm.sort" :min="0" :max="999" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 版块表单对话框 -->
    <el-dialog v-model="forumDialogVisible" :title="forumDialogTitle" width="500px">
      <el-form ref="forumFormRef" :model="forumForm" :rules="forumRules" label-width="80px">
        <el-form-item label="所属分类">
          <el-input :value="currentCategory?.name" disabled />
        </el-form-item>
        <el-form-item label="名称" prop="name">
          <el-input v-model="forumForm.name" placeholder="请输入版块名称" maxlength="50" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
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
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { getCategoryList, createCategory, updateCategory, deleteCategory, createForum, updateForum, deleteForum } from '@/api/category'
import type { CategoryVO } from '@/types'

interface CategoryTreeVO extends CategoryVO {
  sort?: number
  parentId?: number
  children?: CategoryTreeVO[]
  forums?: any[]
  _level?: number
  _isForum?: boolean
  _parentId?: number
}

const loading = ref(false)
const categories = ref<CategoryTreeVO[]>([])

// 扁平化的分类列表，用于表格显示
const flattenedCategories = computed(() => {
  const result: CategoryTreeVO[] = []
  function flatten(items: CategoryTreeVO[], level: number = 0, parentId?: number) {
    items.forEach(item => {
      result.push({
        ...item,
        _level: level,
        _isForum: false,
        _parentId: parentId
      })
      // 添加版块
      if (item.forums && item.forums.length > 0) {
        item.forums.forEach(forum => {
          result.push({
            ...forum,
            _level: level + 1,
            _isForum: true,
            _parentId: item.id
          })
        })
      }
      // 递归处理子分类
      if (item.children && item.children.length > 0) {
        flatten(item.children, level + 1, item.id)
      }
    })
  }
  flatten(categories.value)
  return result
})

// 分类表单对话框
const dialogVisible = ref(false)
const dialogTitle = ref('新增分类')
const editingId = ref<number | null>(null)
const submitting = ref(false)
const categoryFormRef = ref<FormInstance>()
const categoryForm = reactive({
  name: '',
  icon: '',
  description: '',
  sort: 0
})

// 版块表单对话框
const forumDialogVisible = ref(false)
const forumDialogTitle = ref('添加版块')
const currentCategory = ref<CategoryTreeVO | null>(null)
const editingForumId = ref<number | null>(null)
const forumFormRef = ref<FormInstance>()
const forumForm = reactive({
  name: '',
  description: ''
})

// 分类表单验证规则
const categoryRules: FormRules = {
  name: [
    { required: true, message: '请输入分类名称', trigger: 'blur' },
    { min: 2, max: 50, message: '分类名称长度为2-50个字符', trigger: 'blur' }
  ],
  description: [
    { max: 200, message: '描述不能超过200个字符', trigger: 'blur' }
  ]
}

// 版块表单验证规则
const forumRules: FormRules = {
  name: [
    { required: true, message: '请输入版块名称', trigger: 'blur' },
    { min: 2, max: 50, message: '版块名称长度为2-50个字符', trigger: 'blur' }
  ],
  description: [
    { max: 200, message: '描述不能超过200个字符', trigger: 'blur' }
  ]
}

async function fetchCategories() {
  loading.value = true
  try {
    const data = await getCategoryList()
    categories.value = data || []
  } catch (e: any) {
    console.error('获取分类失败:', e)
    ElMessage.error(e?.message || '获取分类列表失败')
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
    icon: '',
    description: '',
    sort: 0
  })
  dialogVisible.value = true
}

function handleEdit(row: CategoryTreeVO) {
  dialogTitle.value = '编辑分类'
  editingId.value = row.id
  Object.assign(categoryForm, {
    name: row.name,
    icon: row.icon || '',
    description: row.description || '',
    sort: row.sort || row.sortOrder || 0
  })
  dialogVisible.value = true
}

async function handleSubmit() {
  // 表单验证
  const valid = await categoryFormRef.value?.validate().catch(() => false)
  if (!valid) return

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
  } catch (e: any) {
    console.error('操作失败:', e)
    ElMessage.error(e?.message || '操作失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

function handleAddForum(row: CategoryTreeVO) {
  forumDialogTitle.value = '添加版块'
  currentCategory.value = row
  editingForumId.value = null
  Object.assign(forumForm, {
    name: '',
    description: ''
  })
  forumDialogVisible.value = true
}

function handleEditForum(row: CategoryTreeVO) {
  forumDialogTitle.value = '编辑版块'
  editingForumId.value = row.id
  // 找到所属分类
  currentCategory.value = flattenedCategories.value.find(c => c.id === row._parentId && !c._isForum) || null
  Object.assign(forumForm, {
    name: row.name,
    description: row.description || ''
  })
  forumDialogVisible.value = true
}

async function submitForum() {
  // 表单验证
  const valid = await forumFormRef.value?.validate().catch(() => false)
  if (!valid) return

  if (!currentCategory.value && !editingForumId.value) return

  submitting.value = true
  try {
    if (editingForumId.value) {
      await updateForum(editingForumId.value, {
        name: forumForm.name,
        description: forumForm.description
      })
      ElMessage.success('更新成功')
    } else {
      await createForum({
        categoryId: currentCategory.value!.id,
        name: forumForm.name,
        description: forumForm.description
      })
      ElMessage.success('创建成功')
    }
    forumDialogVisible.value = false
    fetchCategories()
  } catch (e: any) {
    console.error('操作版块失败:', e)
    ElMessage.error(e?.message || '操作失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

async function handleDelete(row: CategoryTreeVO) {
  try {
    await ElMessageBox.confirm('确定要删除该分类吗？删除分类将同时删除该分类下的所有版块', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteCategory(row.id)
    ElMessage.success('删除成功')
    fetchCategories()
  } catch (e: any) {
    if (e !== 'cancel') {
      console.error('删除失败:', e)
      ElMessage.error(e?.message || '删除失败')
    }
  }
}

async function handleDeleteForum(row: CategoryTreeVO) {
  try {
    await ElMessageBox.confirm('确定要删除该版块吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteForum(row.id)
    ElMessage.success('删除成功')
    fetchCategories()
  } catch (e: any) {
    if (e !== 'cancel') {
      console.error('删除版块失败:', e)
      ElMessage.error(e?.message || '删除失败')
    }
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
