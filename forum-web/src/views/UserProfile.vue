<template>
  <div class="user-profile-page">
    <!-- 加载状态 -->
    <el-card v-if="loading" class="profile-card">
      <div class="loading-container">
        <el-skeleton :rows="5" animated />
      </div>
    </el-card>
    
    <el-card v-else-if="user" class="profile-card">
      <div class="profile-header">
        <el-avatar :size="80" :src="user.avatar">
          {{ (user.nickname || user.username || '?').charAt(0) }}
        </el-avatar>
        <div class="profile-info">
          <h1>{{ user.nickname || user.username }}</h1>
          <p class="signature">{{ user.bio || user.signature || '这个人很懒，什么都没写~' }}</p>
          <div class="stats">
            <span>帖子 {{ user.postCount || 0 }}</span>
            <span>评论 {{ user.commentCount || 0 }}</span>
            <span>粉丝 {{ user.followerCount || 0 }}</span>
            <span>关注 {{ user.followingCount || 0 }}</span>
          </div>
        </div>
        <div class="profile-actions" v-if="!isOwnProfile">
          <el-button type="primary" @click="handleFollow" :loading="followLoading">
            {{ isFollowing ? '取消关注' : '关注' }}
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 用户不存在时的提示 -->
    <el-card v-else class="profile-card">
      <el-empty description="用户不存在或已被删除">
        <el-button type="primary" @click="router.push('/')">返回首页</el-button>
      </el-empty>
    </el-card>

    <el-card class="content-card" v-if="user && !loading">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange as any">
        <el-tab-pane label="帖子" name="posts">
          <div v-if="postsLoading" class="loading-container">
            <el-skeleton :rows="3" animated />
          </div>
          <div v-else-if="posts.length" class="post-list">
            <div v-for="post in posts" :key="post.id" class="post-item" @click="navigateToPost(post.id)">
              <h3>{{ post.title }}</h3>
              <p>{{ post.summary || post.content?.slice(0, 100) }}</p>
              <div class="meta">
                <span>{{ post.createTime }}</span>
                <span>浏览 {{ post.viewCount }}</span>
                <span>评论 {{ post.commentCount }}</span>
              </div>
            </div>
          </div>
          <el-empty v-else description="暂无帖子" />
        </el-tab-pane>
        <el-tab-pane label="收藏" name="collections" v-if="isOwnProfile">
          <div v-if="collections.length" class="post-list">
            <div v-for="item in collections" :key="item.id" class="post-item" @click="navigateToPost(item.postId)">
              <h3>{{ item.postTitle }}</h3>
              <p>{{ item.postSummary }}</p>
              <div class="meta">
                <span>收藏于 {{ item.createTime }}</span>
              </div>
            </div>
          </div>
          <el-empty v-else description="暂无收藏" />
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getUserById, followUser, unfollowUser, getUserCollections } from '@/api/user'
import { getPostList } from '@/api/post'
import { useUserStore } from '@/stores/user'
import type { UserDetailVO, PostVO } from '@/types'

// 收藏项类型
interface CollectionItem {
  id: number
  postId: number
  postTitle: string
  postSummary: string
  createTime: string
}

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 参数验证状态
const isValidUserId = ref(true)
const userId = ref(0)

// 验证用户ID
function validateUserId() {
  const userIdParam = route.params.id
  const id = Number(userIdParam)
  
  if (isNaN(id) || id <= 0) {
    isValidUserId.value = false
    ElMessage.error('用户ID无效')
    router.push('/').catch(() => {})
    return false
  }
  
  userId.value = id
  return true
}

const user = ref<UserDetailVO | null>(null)
const posts = ref<PostVO[]>([])
const collections = ref<CollectionItem[]>([])
const activeTab = ref('posts')
const isFollowing = ref(false)
const followLoading = ref(false)
const loading = ref(true)
const postsLoading = ref(false)

const isOwnProfile = computed(() => userStore.userInfo?.id === userId.value)

async function fetchUser() {
  if (!isValidUserId.value) return
  
  loading.value = true
  try {
    const res = await getUserById(userId.value)
    user.value = res
    // 初始化关注状态 - 后端返回的字段名可能是 isFollowing 或 followed
    isFollowing.value = res.isFollowing || res.followed || false
  } catch (e: any) {
    console.error('获取用户信息失败:', e)
    ElMessage.error(e?.message || '获取用户信息失败')
  } finally {
    loading.value = false
  }
}

async function fetchPosts() {
  if (!isValidUserId.value) return
  
  postsLoading.value = true
  try {
    const res = await getPostList({ page: 1, size: 10, userId: userId.value })
    posts.value = res.records || []
  } catch (e: any) {
    console.error('获取帖子失败:', e)
    ElMessage.error(e?.message || '获取帖子列表失败')
  } finally {
    postsLoading.value = false
  }
}

async function fetchCollections() {
  // 只有查看自己的收藏时才加载，且需要登录
  if (!isOwnProfile.value) {
    collections.value = []
    return
  }
  // 检查登录状态
  if (!userStore.isLoggedIn) {
    collections.value = []
    return
  }
  try {
    // 使用封装好的 API 函数获取收藏列表
    const res = await getUserCollections(userId.value, { page: 1, size: 10 })
    const records = res.records || res.list || []
    collections.value = records.map((item: any) => ({
      id: item.id,
      postId: item.postId,
      postTitle: item.postTitle || item.title,
      postSummary: item.postSummary || item.summary || '',
      createTime: item.createTime
    }))
  } catch (e: any) {
    console.error('获取收藏失败:', e)
    ElMessage.error(e?.message || '获取收藏列表失败')
    collections.value = []
  }
}

function handleTabChange(tab: string) {
  if (tab === 'collections') {
    fetchCollections()
  }
}

// 导航到帖子详情页
function navigateToPost(postId: number) {
  // 验证postId是否有效（必须为正整数）
  if (!postId || isNaN(postId) || postId <= 0) {
    ElMessage.error('帖子ID无效')
    return
  }
  router.push(`/post/${postId}`).catch((err) => {
    console.error('路由跳转失败:', err)
  })
}

async function handleFollow() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login').catch(() => {})
    return
  }
  
  // 防止重复点击
  if (followLoading.value) return
  
  followLoading.value = true
  try {
    // 保存当前状态用于计算粉丝数变化
    const wasFollowing = isFollowing.value
    
    if (wasFollowing) {
      await unfollowUser(userId.value)
      isFollowing.value = false
      ElMessage.success('已取消关注')
    } else {
      await followUser(userId.value)
      isFollowing.value = true
      ElMessage.success('关注成功')
    }
    // 更新粉丝数：如果之前是关注状态现在取消，粉丝数-1；如果之前不是关注状态现在关注，粉丝数+1
    if (user.value) {
      user.value.followerCount = (user.value.followerCount || 0) + (wasFollowing ? -1 : 1)
    }
  } catch (e: any) {
    console.error('操作失败:', e)
    ElMessage.error(e?.message || '操作失败，请稍后重试')
  } finally {
    followLoading.value = false
  }
}

onMounted(() => {
  // 先验证用户ID
  if (validateUserId()) {
    fetchUser()
    fetchPosts()
  }
})
</script>

<style scoped lang="scss">
.user-profile-page {
  max-width: 900px;
  margin: 0 auto;

  .loading-container {
    padding: 20px;
  }

  .profile-card {
    .profile-header {
      display: flex;
      align-items: flex-start;
      gap: 24px;

      .profile-info {
        flex: 1;

        h1 {
          margin: 0 0 8px;
          font-size: 24px;
        }

        .signature {
          color: #909399;
          margin: 0 0 16px;
        }

        .stats {
          display: flex;
          gap: 24px;
          color: #606266;
        }
      }
    }
  }

  .content-card {
    margin-top: 16px;

    .post-item {
      padding: 16px 0;
      border-bottom: 1px solid #f0f0f0;
      cursor: pointer;

      &:hover h3 {
        color: var(--el-color-primary);
      }

      h3 {
        margin: 0 0 8px;
        font-size: 16px;
      }

      p {
        margin: 0 0 8px;
        color: #606266;
        font-size: 14px;
      }

      .meta {
        display: flex;
        gap: 16px;
        font-size: 12px;
        color: #909399;
      }
    }
  }
}
</style>
