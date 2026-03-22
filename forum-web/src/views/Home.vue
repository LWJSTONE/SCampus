<template>
  <div class="home-page">
    <!-- 轮播图/公告 -->
    <el-card class="announcement-card" v-if="announcements.length">
      <el-carousel height="60px" direction="vertical" :interval="5000">
        <el-carousel-item v-for="item in announcements" :key="item.id">
          <div class="announcement-item">
            <el-tag type="danger" size="small">公告</el-tag>
            <span class="title">{{ item.title }}</span>
          </div>
        </el-carousel-item>
      </el-carousel>
    </el-card>

    <!-- 帖子列表 -->
    <div class="post-list">
      <el-card v-for="post in posts" :key="post.id" class="post-card" @click="viewPost(post.id)">
        <div class="post-header">
          <el-avatar :src="post.userAvatar" :size="40">
            {{ post.username?.charAt(0) }}
          </el-avatar>
          <div class="post-info">
            <div class="post-author">
              <span class="username">{{ post.username }}</span>
              <el-tag v-if="post.isTop" type="danger" size="small">置顶</el-tag>
              <el-tag v-if="post.isEssence" type="warning" size="small">精华</el-tag>
            </div>
            <div class="post-meta">
              <span>{{ post.forumName }}</span>
              <span>·</span>
              <span>{{ formatTime(post.createTime) }}</span>
            </div>
          </div>
        </div>

        <div class="post-content">
          <h3 class="post-title">{{ post.title }}</h3>
          <div class="post-summary">{{ post.summary }}</div>
          <div v-if="post.cover" class="post-cover">
            <el-image :src="post.cover" fit="cover" />
          </div>
        </div>

        <div class="post-footer">
          <span><el-icon><View /></el-icon> {{ post.viewCount }}</span>
          <span><el-icon><ChatDotRound /></el-icon> {{ post.commentCount }}</span>
          <span><el-icon><Star /></el-icon> {{ post.likeCount }}</span>
          <span><el-icon><CollectionTag /></el-icon> {{ post.collectCount }}</span>
        </div>
      </el-card>

      <!-- 加载更多 -->
      <div class="load-more" v-if="hasMore">
        <el-button @click="loadMore" :loading="loading">加载更多</el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getPostList } from '@/api/post'
import { getNoticeList } from '@/api/notify'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

const router = useRouter()

interface Post {
  id: number
  title: string
  summary: string
  content: string
  username: string
  userAvatar: string
  forumName: string
  cover: string
  viewCount: number
  commentCount: number
  likeCount: number
  collectCount: number
  isTop: boolean
  isEssence: boolean
  createTime: string
}

interface Announcement {
  id: number
  title: string
}

const posts = ref<Post[]>([])
const loading = ref(false)
const hasMore = ref(true)
const page = ref(1)
const size = 10
const announcements = ref<Announcement[]>([])

function formatTime(time: string) {
  return dayjs(time).fromNow()
}

async function fetchAnnouncements() {
  try {
    const res = await getNoticeList({ current: 1, size: 5 })
    const records = res.records || res.list || []
    announcements.value = records.map((item: any) => ({
      id: item.id,
      title: item.title
    }))
  } catch (e) {
    console.error('获取公告失败:', e)
    // 保留默认公告
    announcements.value = [
      { id: 1, title: '欢迎使用SCampus校园论坛系统！' },
      { id: 2, title: '新版功能上线，支持Markdown编辑器' }
    ]
  }
}

async function fetchPosts() {
  loading.value = true
  try {
    const res = await getPostList({ page: page.value, size })
    // 兼容后端返回的字段名 userName -> username
    const records = res.records.map((post: any) => ({
      ...post,
      username: post.username || post.userName,
      userAvatar: post.userAvatar || post.avatar,
      forumName: post.forumName || post.categoryName,
      isTop: post.isTop === 1 || post.isTop === true,
      isEssence: post.isEssence === 1 || post.isEssence === true
    }))
    posts.value = [...posts.value, ...records]
    hasMore.value = res.current < res.pages
  } catch (e) {
    console.error('获取帖子列表失败:', e)
  } finally {
    loading.value = false
  }
}

async function loadMore() {
  page.value++
  await fetchPosts()
}

function viewPost(id: number) {
  router.push(`/post/${id}`)
}

onMounted(() => {
  fetchAnnouncements()
  fetchPosts()
})
</script>

<style scoped lang="scss">
.home-page {
  max-width: 900px;
  margin: 0 auto;

  .announcement-card {
    margin-bottom: 20px;

    .announcement-item {
      display: flex;
      align-items: center;
      gap: 12px;

      .title {
        font-size: 14px;
        cursor: pointer;

        &:hover {
          color: var(--el-color-primary);
        }
      }
    }
  }

  .post-list {
    .post-card {
      margin-bottom: 16px;
      cursor: pointer;
      transition: all 0.3s;

      &:hover {
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
      }

      .post-header {
        display: flex;
        align-items: center;
        gap: 12px;
        margin-bottom: 12px;

        .post-info {
          .post-author {
            display: flex;
            align-items: center;
            gap: 8px;

            .username {
              font-weight: 500;
            }
          }

          .post-meta {
            font-size: 12px;
            color: #909399;
            margin-top: 4px;
          }
        }
      }

      .post-content {
        .post-title {
          font-size: 16px;
          font-weight: 600;
          margin: 0 0 8px;
          line-height: 1.4;
        }

        .post-summary {
          color: #606266;
          font-size: 14px;
          line-height: 1.6;
          display: -webkit-box;
          -webkit-line-clamp: 2;
          -webkit-box-orient: vertical;
          overflow: hidden;
        }

        .post-cover {
          margin-top: 12px;

          .el-image {
            width: 200px;
            height: 120px;
            border-radius: 4px;
          }
        }
      }

      .post-footer {
        display: flex;
        gap: 20px;
        margin-top: 12px;
        padding-top: 12px;
        border-top: 1px solid #f0f0f0;
        font-size: 13px;
        color: #909399;

        span {
          display: flex;
          align-items: center;
          gap: 4px;
        }
      }
    }
  }

  .load-more {
    text-align: center;
    padding: 20px;
  }
}
</style>
