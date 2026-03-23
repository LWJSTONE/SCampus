<template>
  <div class="post-manage-page">
    <el-card>
      <template #header>
        <div class="header">
          <span>帖子管理</span>
        </div>
      </template>

      <el-form :inline="true" :model="queryParams" class="search-form">
        <el-form-item label="关键词">
          <el-input v-model="queryParams.keyword" placeholder="标题/内容" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="全部" clearable>
            <el-option label="待审核" :value="0" />
            <el-option label="已发布" :value="1" />
            <el-option label="已关闭" :value="2" />
            <el-option label="已删除" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchPosts">搜索</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="posts" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="标题" show-overflow-tooltip />
        <el-table-column prop="username" label="作者" width="120" />
        <el-table-column prop="forumName" label="版块" width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusMap[row.status]?.type || 'info'">
              {{ statusMap[row.status]?.text || '未知' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="发布时间" width="180" />
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleView(row)">查看</el-button>
            <el-button link type="warning" @click="handleTop(row)">
              {{ row.isTop ? '取消置顶' : '置顶' }}
            </el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="queryParams.page"
        v-model:page-size="queryParams.size"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="fetchPosts"
      />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getPostList, deletePost, topPost } from '@/api/post'
import type { PostVO } from '@/types'

const router = useRouter()
const loading = ref(false)
const posts = ref<PostVO[]>([])
const total = ref(0)
const queryParams = reactive({
  page: 1,
  size: 10,
  keyword: '',
  status: undefined as number | undefined
})

const statusMap: Record<number, { text: string; type: 'success' | 'primary' | 'warning' | 'info' | 'danger' }> = {
  0: { text: '待审核', type: 'warning' },
  1: { text: '已发布', type: 'success' },
  2: { text: '已关闭', type: 'info' },
  3: { text: '已删除', type: 'danger' }
}

async function fetchPosts() {
  loading.value = true
  try {
    const res = await getPostList(queryParams)
    posts.value = res.records
    total.value = res.total
  } catch (e: any) {
    console.error('获取帖子列表失败:', e)
    ElMessage.error(e?.message || '获取帖子列表失败')
  } finally {
    loading.value = false
  }
}

function handleView(row: PostVO) {
  router.push(`/post/${row.id}`)
}

async function handleTop(row: PostVO) {
  try {
    // 后端期望 isTop 为 0 或 1，而不是布尔值
    const isTop = row.isTop ? 0 : 1
    await topPost(row.id, isTop)
    row.isTop = isTop === 1
    ElMessage.success(isTop === 1 ? '置顶成功' : '取消置顶成功')
  } catch (e: any) {
    console.error('操作失败:', e)
    ElMessage.error(e?.message || '操作失败，请稍后重试')
  }
}

async function handleDelete(row: PostVO) {
  try {
    await ElMessageBox.confirm('确定要删除该帖子吗？', '提示')
    await deletePost(row.id)
    ElMessage.success('删除成功')
    fetchPosts()
  } catch (e: any) {
    // 用户取消
    if (e !== 'cancel') {
      console.error('删除失败:', e)
      ElMessage.error(e?.message || '删除失败')
    }
  }
}

onMounted(() => {
  fetchPosts()
})
</script>

<style scoped lang="scss">
.post-manage-page {
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
