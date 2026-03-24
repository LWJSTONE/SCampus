<template>
  <div class="category-posts-page">
    <el-card class="category-info-card" v-if="forum">
      <div class="category-header">
        <h1>{{ forum.name }}</h1>
        <p>{{ forum.description }}</p>
      </div>
    </el-card>

    <el-card class="posts-card">
      <div class="post-list">
        <div v-for="post in posts" :key="post.id" class="post-item" @click="$router.push(`/post/${post.id}`)">
          <div class="post-header">
            <el-avatar :size="40" :src="post.userAvatar">
              {{ post.username?.charAt(0) }}
            </el-avatar>
            <div class="post-info">
              <span class="username">{{ post.username }}</span>
              <span class="time">{{ formatTime(post.createTime) }}</span>
            </div>
          </div>
          <h3 class="post-title">{{ post.title }}</h3>
          <p class="post-summary">{{ post.summary }}</p>
          <div class="post-footer">
            <span>浏览 {{ post.viewCount }}</span>
            <span>评论 {{ post.commentCount }}</span>
            <span>点赞 {{ post.likeCount }}</span>
          </div>
        </div>
      </div>

      <div v-if="hasMore" class="load-more">
        <el-button :loading="loading" @click="loadMore">加载更多</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getForumDetail } from '@/api/category'
import { getPostsByForum } from '@/api/post'
import type { ForumVO, PostVO } from '@/types'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

const route = useRoute()
const router = useRouter()

// 参数验证
const forumIdParam = route.params.id
const forumId = Number(forumIdParam)

// 检查forumId是否有效，设置标志位阻止后续API调用
const isValidForumId = !isNaN(forumId) && forumId > 0
if (!isValidForumId) {
  ElMessage.error('版块ID无效')
  router.push('/')
}

const forum = ref<ForumVO | null>(null)
const posts = ref<PostVO[]>([])
const loading = ref(false)
const hasMore = ref(true)
const page = ref(1)
const loadMoreTriggered = ref(false)

function formatTime(time: string) {
  return dayjs(time).fromNow()
}

async function fetchForum() {
  // 如果forumId无效，不执行API请求
  if (!isValidForumId) return
  
  try {
    forum.value = await getForumDetail(forumId)
  } catch (e: any) {
    console.error('获取版块信息失败:', e)
    ElMessage.error(e?.message || '获取版块信息失败')
  }
}

async function fetchPosts() {
  // 如果forumId无效，不执行API请求
  if (!isValidForumId) return
  
  loading.value = true
  try {
    const res = await getPostsByForum(forumId, { page: page.value, size: 10 })
    posts.value = [...posts.value, ...res.records]
    // 兼容后端返回的分页字段
    const current = res.current || page.value
    const pages = res.pages || Math.ceil(res.total / res.size)
    hasMore.value = current < pages
  } catch (e: any) {
    console.error('获取帖子失败:', e)
    ElMessage.error(e?.message || '获取帖子列表失败')
  } finally {
    loading.value = false
    loadMoreTriggered.value = false
  }
}

async function loadMore() {
  // 防止重复触发加载
  if (loadMoreTriggered.value || loading.value || !hasMore.value) return
  
  loadMoreTriggered.value = true
  page.value++
  await fetchPosts()
}

onMounted(() => {
  fetchForum()
  fetchPosts()
})
</script>

<style scoped lang="scss">
.category-posts-page {
  max-width: 900px;
  margin: 0 auto;

  .category-info-card {
    .category-header {
      h1 {
        margin: 0 0 8px;
        font-size: 24px;
      }

      p {
        margin: 0;
        color: #909399;
      }
    }
  }

  .posts-card {
    margin-top: 16px;

    .post-item {
      padding: 16px 0;
      border-bottom: 1px solid #f0f0f0;
      cursor: pointer;

      &:hover .post-title {
        color: var(--el-color-primary);
      }

      .post-header {
        display: flex;
        align-items: center;
        gap: 12px;
        margin-bottom: 12px;

        .post-info {
          .username {
            font-weight: 500;
          }

          .time {
            margin-left: 12px;
            font-size: 12px;
            color: #909399;
          }
        }
      }

      .post-title {
        margin: 0 0 8px;
        font-size: 16px;
      }

      .post-summary {
        margin: 0 0 12px;
        color: #606266;
        font-size: 14px;
      }

      .post-footer {
        display: flex;
        gap: 16px;
        font-size: 12px;
        color: #909399;
      }
    }

    .load-more {
      text-align: center;
      padding: 20px;
    }
  }
}
</style>
