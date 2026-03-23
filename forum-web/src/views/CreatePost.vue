<template>
  <div class="create-post-page">
    <el-card>
      <template #header>
        <h2>发布帖子</h2>
      </template>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="版块" prop="forumId">
          <el-select v-model="form.forumId" placeholder="请选择版块" style="width: 100%">
            <el-option
              v-for="forum in forums"
              :key="forum.id"
              :label="forum.name"
              :value="forum.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入标题" maxlength="100" show-word-limit />
        </el-form-item>

        <el-form-item label="内容" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="10"
            placeholder="请输入内容"
            maxlength="10000"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="标签">
          <el-select
            v-model="form.tags"
            multiple
            filterable
            allow-create
            placeholder="输入标签后回车添加"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="匿名">
          <el-switch v-model="form.isAnonymous" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">
            发布
          </el-button>
          <el-button @click="$router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, FormInstance, FormRules } from 'element-plus'
import { createPost } from '@/api/post'
import { getForumList } from '@/api/category'
import { useUserStore } from '@/stores/user'
import type { ForumVO } from '@/types'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref<FormInstance>()
const submitting = ref(false)
const forums = ref<ForumVO[]>([])

const form = reactive({
  forumId: undefined as number | undefined,
  title: '',
  content: '',
  tags: [] as string[],
  isAnonymous: false,
  type: 0,
  attachmentIds: [] as number[]
})

const rules: FormRules = {
  forumId: [{ required: true, message: '请选择版块', trigger: 'change' }],
  title: [
    { required: true, message: '请输入标题', trigger: 'blur' },
    { min: 5, max: 100, message: '标题长度为5-100个字符', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入内容', trigger: 'blur' },
    { min: 10, message: '内容至少10个字符', trigger: 'blur' }
  ]
}

async function fetchForums() {
  try {
    const res = await getForumList()
    forums.value = res
  } catch (e: any) {
    console.error('获取版块失败:', e)
    ElMessage.error(e?.message || '获取版块列表失败，请刷新页面重试')
  }
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    const postId = await createPost({
      forumId: form.forumId!,
      title: form.title,
      content: form.content,
      tags: form.tags,
      isAnonymous: form.isAnonymous,
      type: form.type,
      attachmentIds: form.attachmentIds
    })
    ElMessage.success('发布成功')
    router.push(`/post/${postId}`)
  } catch (e: any) {
    console.error('发布失败:', e)
    ElMessage.error(e?.message || '发布失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  // 检查登录状态
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  fetchForums()
})
</script>

<style scoped lang="scss">
.create-post-page {
  max-width: 900px;
  margin: 0 auto;

  h2 {
    margin: 0;
    font-size: 18px;
  }
}
</style>
