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
        :page-size="size"
        layout="total, prev, pager, next"
        @current-change="fetchNotices"
      />
    </el-card>

    <!-- 公告表单对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form :model="noticeForm" label-width="80px">
        <el-form-item label="标题" required>
          <el-input v-model="noticeForm.title" placeholder="请输入公告标题" maxlength="100" />
        </el-form-item>
        <el-form-item label="类型">
          <el-radio-group v-model="noticeForm.type">
            <el-radio :value="0">公告</el-radio>
            <el-radio :value="1">通知</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="noticeForm.status">
            <el-radio :value="0">发布</el-radio>
            <el-radio :value="1">草稿</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="内容" required>
          <el-input
            v-model="noticeForm.content"
            type="textarea"
            :rows="5"
            placeholder="请输入公告内容"
            maxlength="500"
            show-word-limit
          />
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getNoticeList, createNotice, updateNotice, deleteNotice } from '@/api/notify'
import type { NoticeVO, NoticeCreateDTO } from '@/api/notify'

const loading = ref(false)
const notices = ref<NoticeVO[]>([])
const page = ref(1)
const total = ref(0)
const size = 10

// 公告表单对话框
const dialogVisible = ref(false)
const dialogTitle = ref('发布公告')
const noticeForm = reactive<NoticeCreateDTO>({
  title: '',
  content: '',
  type: 0,
  status: 0
})
const editingId = ref<number | null>(null)
const submitting = ref(false)

async function fetchNotices() {
  loading.value = true
  try {
    const res = await getNoticeList({ current: page.value, size })
    notices.value = res.records || res.list || []
    total.value = res.total || 0
  } catch (e) {
    console.error('获取公告列表失败:', e)
    notices.value = []
  } finally {
    loading.value = false
  }
}

function handleAdd() {
  dialogTitle.value = '发布公告'
  editingId.value = null
  Object.assign(noticeForm, {
    title: '',
    content: '',
    type: 0,
    status: 0
  })
  dialogVisible.value = true
}

function handleEdit(row: NoticeVO) {
  dialogTitle.value = '编辑公告'
  editingId.value = row.id
  Object.assign(noticeForm, {
    title: row.title,
    content: row.content,
    type: row.type,
    status: row.status
  })
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!noticeForm.title.trim()) {
    ElMessage.warning('请输入公告标题')
    return
  }
  if (!noticeForm.content.trim()) {
    ElMessage.warning('请输入公告内容')
    return
  }
  
  submitting.value = true
  try {
    if (editingId.value) {
      await updateNotice(editingId.value, noticeForm)
      ElMessage.success('更新成功')
    } else {
      await createNotice(noticeForm)
      ElMessage.success('发布成功')
    }
    dialogVisible.value = false
    fetchNotices()
  } catch (e: any) {
    console.error('操作失败:', e)
    ElMessage.error(e?.message || '操作失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

async function handleDelete(row: NoticeVO) {
  try {
    await ElMessageBox.confirm('确定要删除该公告吗？', '提示')
    await deleteNotice(row.id)
    ElMessage.success('删除成功')
    fetchNotices()
  } catch (e: any) {
    // 用户取消确认
    if (e !== 'cancel') {
      console.error('删除失败:', e)
      ElMessage.error(e?.message || '删除失败，请稍后重试')
    }
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
