<template>
  <div class="user-profile-page">
    <el-card v-if="user" class="profile-card">
      <div class="profile-header">
        <el-avatar :size="80" :src="user.avatar">
          {{ user.nickname?.charAt(0) || user.username?.charAt(0) }}
        </el-avatar>
        <div class="profile-info">
          <h1>{{ user.nickname || user.username }}</h1>
          <p class="signature">{{ user.signature || '这个人很懒，什么都没写~' }}</p>
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

    <el-card class="content-card">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="帖子" name="posts">
          <div v-if="posts.length" class="post-list">
            <div v-for="post in posts" :key="post.id" class="post-item" @click="$router.push(`/post/${post.id}`)">
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
        <el-tab-pane label="收藏" name="collections">
          <div v-if="collections.length" class="post-list">
            <div v-for="item in collections" :key="item.id" class="post-item" @click="$router.push(`/post/${item.postId}`)">
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
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getUserById, followUser, unfollowUser } from '@/api/user'
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
const userStore = useUserStore()
const userId = Number(route.params.id)

const user = ref<UserDetailVO | null>(null)
const posts = ref<PostVO[]>([])
const collections = ref<CollectionItem[]>([])
const activeTab = ref('posts')
const isFollowing = ref(false)
const followLoading = ref(false)

const isOwnProfile = computed(() => userStore.userInfo?.id === userId)

async function fetchUser() {
  try {
    const res = await getUserById(userId)
    user.value = res
    // 初始化关注状态 - 后端返回的字段名可能是 isFollowing 或 followed
    isFollowing.value = res.isFollowing || res.followed || false
  } catch (e) {
    console.error('获取用户信息失败:', e)
  }
}

async function fetchPosts() {
  try {
    const res = await getPostList({ page: 1, size: 10, userId })
    posts.value = res.records
  } catch (e) {
    console.error('获取帖子失败:', e)
  }
}

async function fetchCollections() {
  // 只有查看自己的收藏时才加载
  if (!isOwnProfile.value) {
    collections.value = []
    return
  }
  try {
    // 调用收藏列表API
    const res = await fetch('/api/v1/interactions/collect/list?current=1&size=10', {
      headers: {
        'Authorization': `Bearer ${userStore.token}`
      }
    }).then(r => r.json())
    if (res.code === 200 && res.data) {
      collections.value = (res.data.records || []).map((item: any) => ({
        id: item.id,
        postId: item.postId,
        postTitle: item.postTitle || item.title,
        postSummary: item.postSummary || item.summary || '',
        createTime: item.createTime
      }))
    }
  } catch (e) {
    console.error('获取收藏失败:', e)
    collections.value = []
  }
}

function handleTabChange(tab: string) {
  if (tab === 'collections') {
    fetchCollections()
  }
}

async function handleFollow() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    return
  }
  
  followLoading.value = true
  try {
    if (isFollowing.value) {
      await unfollowUser(userId)
      isFollowing.value = false
      ElMessage.success('已取消关注')
    } else {
      await followUser(userId)
      isFollowing.value = true
      ElMessage.success('关注成功')
    }
    // 更新粉丝数
    if (user.value) {
      user.value.followerCount = (user.value.followerCount || 0) + (isFollowing.value ? 1 : -1)
    }
  } catch (e) {
    console.error('操作失败:', e)
  } finally {
    followLoading.value = false
  }
}

onMounted(() => {
  fetchUser()
  fetchPosts()
})
</script>

<style scoped lang="scss">
.user-profile-page {
  max-width: 900px;
  margin: 0 auto;

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
