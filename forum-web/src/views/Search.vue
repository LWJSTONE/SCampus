<template>
  <div class="search-page">
    <el-card class="search-card">
      <el-input
        v-model="keyword"
        placeholder="搜索帖子..."
        size="large"
        @keyup.enter="handleSearch"
      >
        <template #append>
          <el-button icon="Search" @click="handleSearch" />
        </template>
      </el-input>
    </el-card>

    <el-card class="results-card" v-if="searched">
      <template #header>
        <span>搜索结果 ({{ total }})</span>
      </template>

      <div v-if="posts.length" class="post-list">
        <div v-for="post in posts" :key="post.id" class="post-item" @click="$router.push(`/post/${post.id}`)">
          <h3 v-html="highlightKeyword(post.title)"></h3>
          <p v-html="highlightKeyword(post.summary || '')"></p>
          <div class="meta">
            <span>{{ post.username }}</span>
            <span>{{ formatTime(post.createTime) }}</span>
          </div>
        </div>
      </div>
      <el-empty v-else description="未找到相关内容" />

      <div v-if="hasMore" class="load-more">
        <el-button :loading="loading" @click="loadMore">加载更多</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { searchPosts } from '@/api/post'
import type { PostVO } from '@/types'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

const route = useRoute()
const router = useRouter()

const keyword = ref('')
const posts = ref<PostVO[]>([])
const total = ref(0)
const loading = ref(false)
const hasMore = ref(true)
const searched = ref(false)
const page = ref(1)

function formatTime(time: string) {
  return dayjs(time).fromNow()
}

function highlightKeyword(text: string) {
  if (!keyword.value) return text
  const regex = new RegExp(`(${keyword.value})`, 'gi')
  return text.replace(regex, '<mark>$1</mark>')
}

async function handleSearch() {
  if (!keyword.value.trim()) return
  
  page.value = 1
  posts.value = []
  router.push({ query: { q: keyword.value } })
  await fetchPosts()
  searched.value = true
}

async function fetchPosts() {
  loading.value = true
  try {
    const res = await searchPosts({ keyword: keyword.value, page: page.value, size: 10 })
    posts.value = [...posts.value, ...res.records]
    total.value = res.total
    hasMore.value = res.current < res.pages
  } catch (e) {
    console.error('搜索失败:', e)
  } finally {
    loading.value = false
  }
}

async function loadMore() {
  page.value++
  await fetchPosts()
}

onMounted(() => {
  if (route.query.q) {
    keyword.value = route.query.q as string
    handleSearch()
  }
})
</script>

<style scoped lang="scss">
.search-page {
  max-width: 900px;
  margin: 0 auto;

  .search-card {
    margin-bottom: 16px;
  }

  .results-card {
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

        :deep(mark) {
          background-color: #fff3cd;
          padding: 0 2px;
        }
      }

      p {
        margin: 0 0 8px;
        color: #606266;
        font-size: 14px;

        :deep(mark) {
          background-color: #fff3cd;
          padding: 0 2px;
        }
      }

      .meta {
        display: flex;
        gap: 12px;
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
