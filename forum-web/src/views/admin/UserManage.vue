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
            <el-option label="正常" :value="1" />
            <el-option label="禁用" :value="0" />
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
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="180" />
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link :type="row.status === 1 ? 'danger' : 'success'" @click="handleToggleStatus(row)">
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="queryParams.page"
        v-model:page-size="queryParams.size"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="handlePageChange"
      />
    </el-card>

    <!-- 用户编辑对话框 -->
    <el-dialog v-model="dialogVisible" title="编辑用户" width="500px">
      <el-form ref="formRef" :model="userForm" :rules="formRules" label-width="80px">
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="userForm.nickname" placeholder="请输入昵称" maxlength="20" />
        </el-form-item>
        <el-form-item label="学校" prop="school">
          <el-input v-model="userForm.school" placeholder="请输入学校" maxlength="50" />
        </el-form-item>
        <el-form-item label="性别">
          <el-radio-group v-model="userForm.gender">
            <el-radio :value="0">保密</el-radio>
            <el-radio :value="1">男</el-radio>
            <el-radio :value="2">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="签名" prop="bio">
          <el-input v-model="userForm.bio" type="textarea" :rows="3" placeholder="请输入个性签名" maxlength="200" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { getUserList, updateUser, updateUserStatus } from '@/api/user'
import type { UserVO, UserUpdateDTO } from '@/types'

const loading = ref(false)
const users = ref<UserVO[]>([])
const total = ref(0)
const queryParams = reactive({
  page: 1,
  size: 10,
  keyword: '',
  status: undefined as number | undefined
})

// 用户编辑对话框
const dialogVisible = ref(false)
const editingUser = ref<UserVO | null>(null)
const formRef = ref<FormInstance>()
const userForm = reactive<UserUpdateDTO>({
  nickname: '',
  bio: '',
  school: '',
  gender: 0
})
const submitting = ref(false)

// 表单验证规则
const formRules: FormRules = {
  nickname: [
    { min: 2, max: 20, message: '昵称长度为2-20个字符', trigger: 'blur' }
  ],
  school: [
    { max: 50, message: '学校名称不能超过50个字符', trigger: 'blur' }
  ],
  bio: [
    { max: 200, message: '个性签名不能超过200个字符', trigger: 'blur' }
  ]
}

async function fetchUsers() {
  loading.value = true
  try {
    const res = await getUserList(queryParams)
    users.value = res.records || []
    total.value = res.total || 0
  } catch (e: any) {
    console.error('获取用户列表失败:', e)
    ElMessage.error(e?.message || '获取用户列表失败')
    users.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function handlePageChange(page: number) {
  queryParams.page = page
  fetchUsers()
}

function handleEdit(row: UserVO) {
  editingUser.value = row
  Object.assign(userForm, {
    nickname: row.nickname || '',
    bio: row.signature || row.bio || '',  // 兼容两种字段名
    school: row.school || '',
    gender: row.gender ?? 0
  })
  dialogVisible.value = true
  // 清除表单验证状态
  nextTick(() => {
    formRef.value?.clearValidate()
  })
}

async function handleSubmit() {
  if (!editingUser.value) return

  // 表单验证
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    await updateUser(editingUser.value.id, userForm)
    ElMessage.success('更新成功')
    dialogVisible.value = false
    fetchUsers()
  } catch (e) {
    console.error('更新用户失败:', e)
    ElMessage.error('更新用户失败')
  } finally {
    submitting.value = false
  }
}

async function handleToggleStatus(row: UserVO) {
  // 后端状态定义：0-禁用，1-正常
  const newStatus = row.status === 1 ? 0 : 1
  const action = newStatus === 1 ? '启用' : '禁用'
  // 保存原始状态以便回滚
  const originalStatus = row.status

  try {
    await ElMessageBox.confirm(`确定要${action}该用户吗？`, '提示')
  } catch {
    // 用户取消确认
    return
  }

  try {
    // 先更新UI（乐观更新）
    row.status = newStatus
    await updateUserStatus(row.id, newStatus)
    ElMessage.success(`${action}成功`)
  } catch (e) {
    console.error(`${action}用户失败:`, e)
    // API调用失败时回滚状态
    row.status = originalStatus
    ElMessage.error(`${action}用户失败`)
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
