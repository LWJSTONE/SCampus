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
        <el-table-column prop="reportType" label="类型" width="100">
          <template #default="{ row }">
            <el-tag>{{ typeMap[row.reportType] || '未知' }}</el-tag>
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
        :page-size="size"
        layout="total, prev, pager, next"
        @current-change="fetchReports"
      />
    </el-card>

    <!-- 处理举报对话框 -->
    <el-dialog v-model="processDialogVisible" title="处理举报" width="500px">
      <el-form :model="handleForm" label-width="100px">
        <el-form-item label="处理结果">
          <el-radio-group v-model="handleForm.result">
            <el-radio :value="1">通过（处罚）</el-radio>
            <el-radio :value="2">驳回</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="处罚措施" v-if="handleForm.result === 1">
          <el-radio-group v-model="handleForm.action">
            <el-radio :value="1">删除内容</el-radio>
            <el-radio :value="2">警告用户</el-radio>
            <el-radio :value="3">封禁用户</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="封禁天数" v-if="handleForm.result === 1 && handleForm.action === 3">
          <el-input-number v-model="handleForm.banDays" :min="1" :max="365" />
        </el-form-item>
        <el-form-item label="处理备注">
          <el-input v-model="handleForm.handleRemark" type="textarea" :rows="3" placeholder="请输入处理备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="processDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitHandle">确定</el-button>
      </template>
    </el-dialog>

    <!-- 查看详情对话框 -->
    <el-dialog v-model="viewDialogVisible" title="举报详情" width="600px">
      <el-descriptions :column="2" border v-if="currentReport">
        <el-descriptions-item label="举报ID">{{ currentReport.id }}</el-descriptions-item>
        <el-descriptions-item label="举报类型">{{ typeMap[currentReport.reportType] }}</el-descriptions-item>
        <el-descriptions-item label="目标ID">{{ currentReport.targetId }}</el-descriptions-item>
        <el-descriptions-item label="目标标题">{{ currentReport.targetTitle || '-' }}</el-descriptions-item>
        <el-descriptions-item label="举报原因" :span="2">{{ currentReport.reason }}</el-descriptions-item>
        <el-descriptions-item label="举报人">{{ currentReport.reporterName }}</el-descriptions-item>
        <el-descriptions-item label="举报时间">{{ currentReport.createTime }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ statusMap[currentReport.status]?.text }}</el-descriptions-item>
        <el-descriptions-item label="处理人">{{ currentReport.handlerName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="处理时间" :span="2">{{ currentReport.handleTime || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getReportList, handleReport, type ReportVO, type ReportHandleDTO } from '@/api/report'

const loading = ref(false)
const reports = ref<ReportVO[]>([])
const page = ref(1)
const total = ref(0)
const size = 10

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

// 处理对话框
const processDialogVisible = ref(false)
const submitting = ref(false)
const processingId = ref<number | null>(null)
const handleForm = reactive<ReportHandleDTO>({
  result: 1,
  action: 1,
  banDays: 7,
  handleRemark: ''
})

// 查看详情对话框
const viewDialogVisible = ref(false)
const currentReport = ref<ReportVO | null>(null)

async function fetchReports() {
  loading.value = true
  try {
    const res = await getReportList({ current: page.value, size })
    reports.value = res.records || res.list || []
    total.value = res.total || 0
  } catch (e: any) {
    console.error('获取举报列表失败:', e)
    ElMessage.error(e?.message || '获取举报列表失败')
    reports.value = []
  } finally {
    loading.value = false
  }
}

function handleView(row: ReportVO) {
  currentReport.value = row
  viewDialogVisible.value = true
}

function handleProcess(row: ReportVO) {
  processingId.value = row.id
  Object.assign(handleForm, {
    result: 1,
    action: 1,
    banDays: 7,
    handleRemark: ''
  })
  processDialogVisible.value = true
}

async function submitHandle() {
  if (!processingId.value) return
  
  submitting.value = true
  try {
    await handleReport(processingId.value, handleForm)
    ElMessage.success('处理成功')
    processDialogVisible.value = false
    fetchReports()
  } catch (e: any) {
    console.error('处理举报失败:', e)
    ElMessage.error(e?.message || '处理失败，请稍后重试')
  } finally {
    submitting.value = false
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
