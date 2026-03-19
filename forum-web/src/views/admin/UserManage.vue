<template>
  <div class="user-manage-page">
    <el-card>
      <template #header>
        <div class="header">
          <span>用户管理</span>
        </div>
      </template>

      <el-form :inline="true" :model="queryParams" class="search-form">
        <el-form-item label="关键词">
          <el-input v-model="queryParams.keyword" placeholder="用户名/昵称" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="全部" clearable>
            <el-option label="正常" :value="0" />
            <el-option label="禁用" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchUsers">搜索</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="users" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="nickname" label="昵称" />
        <el-table-column prop="email" label="邮箱" />
        <el-table-column prop="status" label="状态">
          <template #default="{ row }">
            <el-tag :type="row.status === 0 ? 'success' : 'danger'">
              {{ row.status === 0 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="180" />
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link :type="row.status === 0 ? 'danger' : 'success'" @click="handleToggleStatus(row)">
              {{ row.status === 0 ? '禁用' : '启用' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="queryParams.page"
        v-model:page-size="queryParams.size"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="fetchUsers"
      />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserList, updateUserStatus } from '@/api/user'
import type { UserVO } from '@/types'

const loading = ref(false)
const users = ref<UserVO[]>([])
const total = ref(0)
const queryParams = reactive({
  page: 1,
  size: 10,
  keyword: '',
  status: undefined as number | undefined
})

async function fetchUsers() {
  loading.value = true
  try {
    const res = await getUserList(queryParams)
    users.value = res.records
    total.value = res.total
  } catch (e) {
    console.error('获取用户列表失败:', e)
  } finally {
    loading.value = false
  }
}

function handleEdit(_row: UserVO) {
  ElMessage.info('编辑功能开发中')
}

async function handleToggleStatus(row: UserVO) {
  const newStatus = row.status === 0 ? 1 : 0
  const action = newStatus === 1 ? '禁用' : '启用'
  
  try {
    await ElMessageBox.confirm(`确定要${action}该用户吗？`, '提示')
    await updateUserStatus(row.id, newStatus)
    row.status = newStatus
    ElMessage.success(`${action}成功`)
  } catch (e) {
    // 用户取消
  }
}

onMounted(() => {
  fetchUsers()
})
</script>

<style scoped lang="scss">
.user-manage-page {
  .header {
    font-size: 18px;
    font-weight: 500;
  }

  .search-form {
    margin-bottom: 16px;
  }

  .el-pagination {
    margin-top: 16px;
    justify-content: flex-end;
  }
}
</style>
