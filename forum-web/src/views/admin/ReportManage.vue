<template>
  <div class="report-manage-page">
    <el-card>
      <template #header>
        <div class="header">
          <span>举报管理</span>
        </div>
      </template>

      <el-table :data="reports" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">
            <el-tag>{{ typeMap[row.type] || '未知' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="targetId" label="目标ID" width="100" />
        <el-table-column prop="reason" label="举报原因" show-overflow-tooltip />
        <el-table-column prop="reporterName" label="举报人" width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusMap[row.status]?.type || 'info'">
              {{ statusMap[row.status]?.text || '待处理' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="举报时间" width="180" />
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleProcess(row)" v-if="row.status === 0">处理</el-button>
            <el-button link type="info" @click="handleView(row)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="page"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="fetchReports"
      />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

interface Report {
  id: number
  type: number
  targetId: number
  reason: string
  reporterName: string
  status: number
  createTime: string
}

const loading = ref(false)
const reports = ref<Report[]>([])
const page = ref(1)
const total = ref(0)

const typeMap: Record<number, string> = {
  1: '帖子',
  2: '评论',
  3: '用户'
}

const statusMap: Record<number, { text: string; type: 'success' | 'primary' | 'warning' | 'info' | 'danger' }> = {
  0: { text: '待处理', type: 'warning' },
  1: { text: '已处理', type: 'success' },
  2: { text: '已驳回', type: 'info' }
}

async function fetchReports() {
  loading.value = true
  try {
    // 模拟数据
    reports.value = [
      { id: 1, type: 1, targetId: 100, reason: '内容违规', reporterName: 'user1', status: 0, createTime: '2024-01-01 10:00:00' }
    ]
    total.value = 1
  } finally {
    loading.value = false
  }
}

function handleView(_row: Report) {
  ElMessage.info('查看详情功能开发中')
}

async function handleProcess(row: Report) {
  try {
    await ElMessageBox.confirm('请选择处理方式', '处理举报', {
      distinguishCancelAndClose: true,
      confirmButtonText: '通过',
      cancelButtonText: '驳回'
    })
    row.status = 1
    ElMessage.success('处理成功')
  } catch (action) {
    if (action === 'cancel') {
      row.status = 2
      ElMessage.info('已驳回')
    }
  }
}

onMounted(() => {
  fetchReports()
})
</script>

<style scoped lang="scss">
.report-manage-page {
  .header {
    font-size: 18px;
    font-weight: 500;
  }

  .el-pagination {
    margin-top: 16px;
    justify-content: flex-end;
  }
}
</style>
