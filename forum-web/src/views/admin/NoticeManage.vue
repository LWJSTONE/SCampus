<template>
  <div class="notice-manage-page">
    <el-card>
      <template #header>
        <div class="header">
          <span>公告管理</span>
          <el-button type="primary" @click="handleAdd">发布公告</el-button>
        </div>
      </template>

      <el-table :data="notices" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="标题" />
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.type === 0 ? 'info' : 'warning'">
              {{ row.type === 0 ? '公告' : '通知' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 0 ? 'success' : 'info'">
              {{ row.status === 0 ? '已发布' : '草稿' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="发布时间" width="180" />
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="page"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="fetchNotices"
      />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

interface Notice {
  id: number
  title: string
  content: string
  type: number
  status: number
  createTime: string
}

const loading = ref(false)
const notices = ref<Notice[]>([])
const page = ref(1)
const total = ref(0)

async function fetchNotices() {
  loading.value = true
  try {
    // 模拟数据
    notices.value = [
      { id: 1, title: '系统升级公告', content: '系统将于今晚升级', type: 0, status: 0, createTime: '2024-01-01 10:00:00' }
    ]
    total.value = 1
  } finally {
    loading.value = false
  }
}

function handleAdd() {
  ElMessage.info('发布公告功能开发中')
}

function handleEdit(_row: Notice) {
  ElMessage.info('编辑功能开发中')
}

async function handleDelete(_row: Notice) {
  try {
    await ElMessageBox.confirm('确定要删除该公告吗？', '提示')
    ElMessage.success('删除成功')
    fetchNotices()
  } catch (e) {
    // 用户取消
  }
}

onMounted(() => {
  fetchNotices()
})
</script>

<style scoped lang="scss">
.notice-manage-page {
  .header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-size: 18px;
    font-weight: 500;
  }

  .el-pagination {
    margin-top: 16px;
    justify-content: flex-end;
  }
}
</style>
