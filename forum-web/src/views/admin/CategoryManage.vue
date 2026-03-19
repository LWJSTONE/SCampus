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
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCategoryList, deleteCategory } from '@/api/category'
import type { CategoryVO } from '@/types'

const loading = ref(false)
const categories = ref<CategoryVO[]>([])

async function fetchCategories() {
  loading.value = true
  try {
    categories.value = await getCategoryList()
  } catch (e) {
    console.error('获取分类失败:', e)
  } finally {
    loading.value = false
  }
}

function handleAdd() {
  ElMessage.info('新增分类功能开发中')
}

function handleEdit(_row: CategoryVO) {
  ElMessage.info('编辑功能开发中')
}

function handleAddForum(_row: CategoryVO) {
  ElMessage.info('添加版块功能开发中')
}

async function handleDelete(row: CategoryVO) {
  try {
    await ElMessageBox.confirm('确定要删除该分类吗？', '提示')
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
