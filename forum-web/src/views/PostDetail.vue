/**
 * 帖子详情页面组件
 * 展示帖子内容和评论列表
 */
<template>
  <div class="post-detail-page">
    <el-skeleton v-if="loading" :rows="10" animated />

    <template v-else-if="postDetail">
      <!-- 帖子内容 -->
      <el-card class="post-card">
        <!-- 标题 -->
        <h1 class="post-title">{{ postDetail.title }}</h1>

        <!-- 作者信息 -->
        <div class="post-author">
          <el-avatar :size="40" :src="postDetail.authorAvatar || postDetail.userAvatar">
            {{ (postDetail.authorName || postDetail.username)?.charAt(0) }}
          </el-avatar>
          <div class="author-info">
            <router-link :to="`/user/${postDetail.authorId || postDetail.userId}`" class="author-name">
              {{ postDetail.authorName || postDetail.username }}
            </router-link>
            <div class="post-meta">
              <span>{{ formatRelativeTime(postDetail.createTime) }}</span>
              <span>·</span>
              <span>{{ postDetail.categoryName || postDetail.forumName }}</span>
              <span>·</span>
              <span>{{ postDetail.viewCount }} 浏览</span>
            </div>
          </div>
          <div class="post-actions">
            <!-- 标签 -->
            <template v-if="postDetail.isTop">
              <el-tag type="danger" size="small">置顶</el-tag>
            </template>
            <template v-if="postDetail.isEssence">
              <el-tag type="warning" size="small">精华</el-tag>
            </template>
          </div>
        </div>

        <el-divider />

        <!-- 正文内容 -->
        <div class="post-content" v-html="sanitizedContent"></div>

        <!-- 标签 -->
        <div class="post-tags" v-if="postDetail.tags?.length">
          <el-tag
            v-for="tag in postDetail.tags"
            :key="tag"
            type="info"
            size="small"
            class="tag-item"
          >
            {{ tag }}
          </el-tag>
        </div>

        <!-- 互动操作 -->
        <div class="post-interaction">
          <el-button
            :type="postDetail.liked ? 'primary' : 'default'"
            @click="handleLike"
          >
            点赞 ({{ postDetail.likeCount }})
          </el-button>
          <el-button
            :type="postDetail.collected ? 'warning' : 'default'"
            @click="handleCollect"
          >
            收藏 ({{ postDetail.collectCount }})
          </el-button>
          <el-button @click="handleShare">分享</el-button>
        </div>
      </el-card>

      <!-- 评论列表 -->
      <el-card class="comment-card">
        <template #header>
          <div class="card-header">
            <span>评论 ({{ commentTotal }})</span>
          </div>
        </template>

        <!-- 发表评论 -->
        <div class="comment-form">
          <el-input
            v-model="commentContent"
            type="textarea"
            :rows="3"
            placeholder="发表你的看法..."
            maxlength="500"
            show-word-limit
          />
          <div class="form-actions">
            <el-button type="primary" :loading="submitting" @click="handleComment">
              发表评论
            </el-button>
          </div>
        </div>

        <el-divider />

        <!-- 评论列表 -->
        <div class="comment-list">
          <template v-if="commentList.length > 0">
            <CommentItem
              v-for="comment in commentList"
              :key="comment.id"
              :comment="comment"
              @reply="handleReply"
              @delete="handleDeleteComment"
            />
          </template>
          <el-empty v-else description="暂无评论，快来发表第一条评论吧" />
        </div>

        <!-- 分页 -->
        <div class="pagination-wrapper" v-if="commentTotal > queryParams.size">
          <el-pagination
            v-model:current-page="queryParams.page"
            v-model:page-size="queryParams.size"
            :total="commentTotal"
            layout="prev, pager, next"
            @current-change="fetchComments"
          />
        </div>
      </el-card>
    </template>

    <el-empty v-else description="帖子不存在或已被删除" />
  </div>
</template>

<script setup lang="ts">
/**
 * 帖子详情页面组件逻辑
 */
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getPostById, likePost, collectPost } from '@/api/post'
import { getPostComments, createComment, deleteComment } from '@/api/comment'
import type { PostDetailVO, CommentVO, PageQuery } from '@/types'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'
import { useUserStore } from '@/stores/user'
import CommentItem from '@/components/CommentItem.vue'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

// ==================== 状态定义 ====================

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 帖子ID - 添加有效性验证
const postId = Number(route.params.id)
const isValidPostId = computed(() => !isNaN(postId) && postId > 0)

// 加载状态
const loading = ref(false)
const submitting = ref(false)

// 帖子详情
const postDetail = ref<PostDetailVO | null>(null)

// XSS净化：过滤危险HTML标签和属性
const sanitizedContent = computed(() => {
  if (!postDetail.value?.content) return ''
  const content = postDetail.value.content

  // 允许的标签白名单
  const allowedTags = ['p', 'br', 'b', 'i', 'u', 'strong', 'em', 'span', 'div',
    'h1', 'h2', 'h3', 'h4', 'h5', 'h6', 'ul', 'ol', 'li', 'blockquote',
    'pre', 'code', 'a', 'img', 'table', 'thead', 'tbody', 'tr', 'td', 'th', 'hr']

  // 允许的属性白名单
  const allowedAttrs: Record<string, string[]> = {
    'a': ['href', 'title', 'target'],
    'img': ['src', 'alt', 'title', 'width', 'height'],
    'span': ['style'],
    'div': ['style', 'class'],
    'p': ['style'],
    'pre': ['class'],
    'code': ['class']
  }

  // 允许的样式白名单
  const allowedStyles = ['color', 'background-color', 'font-size', 'font-weight', 'text-align']

  // 危险的协议
  const dangerousProtocols = ['javascript:', 'vbscript:', 'data:', 'file:']

  let result = content

  // 移除script标签及其内容
  result = result.replace(/<script\b[^<]*(?:(?!<\/script>)<[^<]*)*<\/script>/gi, '')

  // 移除style标签及其内容
  result = result.replace(/<style\b[^<]*(?:(?!<\/style>)<[^<]*)*<\/style>/gi, '')

  // 移除所有事件处理属性
  result = result.replace(/\s*on\w+\s*=\s*["'][^"']*["']/gi, '')

  // 处理所有HTML标签
  result = result.replace(/<\/?(\w+)([^>]*)>/gi, (match, tagName, attrs) => {
    const tag = tagName.toLowerCase()
    if (!allowedTags.includes(tag)) {
      return ''
    }

    // 过滤属性
    if (attrs) {
      const allowedForTag = allowedAttrs[tag] || []
      const filteredAttrs = attrs.split(/\s+/)
        .filter(attr => {
          if (!attr) return false
          const [name] = attr.split('=')
          const attrName = name.toLowerCase().trim()

          if (!allowedForTag.includes(attrName)) return false

          // 检查href和src属性是否包含危险协议
          if (attrName === 'href' || attrName === 'src') {
            const value = attr.substring(attr.indexOf('=') + 1).replace(/["']/g, '').trim().toLowerCase()
            if (dangerousProtocols.some(p => value.startsWith(p))) {
              return false
            }
          }

          // 过滤style属性中的危险内容
          if (attrName === 'style') {
            const value = attr.substring(attr.indexOf('=') + 1).replace(/["']/g, '').trim()
            const styles = value.split(';').filter(s => {
              const [prop] = s.split(':')
              return allowedStyles.includes(prop.trim().toLowerCase())
            })
            return styles.length > 0
          }

          return true
        })
        .join(' ')

      return `<${tag}${filteredAttrs ? ' ' + filteredAttrs : ''}>`
    }

    return match
  })

  return result
})

// 评论列表
const commentList = ref<CommentVO[]>([])
const commentTotal = ref(0)

// 评论内容
const commentContent = ref('')

// 回复的评论
const replyTo = ref<CommentVO | null>(null)

// 查询参数
const queryParams = reactive<PageQuery>({
  page: 1,
  size: 10,
})

// ==================== 方法定义 ====================

function formatRelativeTime(time: string) {
  return dayjs(time).fromNow()
}

/**
 * 获取帖子详情
 */
async function fetchPostDetail() {
  // 在函数内验证postId
  if (!isValidPostId.value) {
    ElMessage.error('帖子ID无效')
    router.push('/')
    return
  }
  loading.value = true
  try {
    const res = await getPostById(postId)
    // 兼容多种字段名
    res.liked = res.liked ?? res.isLiked ?? false
    res.collected = res.collected ?? res.isCollected ?? false
    res.authorId = res.authorId ?? res.userId
    // 兼容 userName 和 username 字段
    res.authorName = res.authorName ?? res.username ?? res.userName
    res.username = res.username ?? res.userName
    res.authorAvatar = res.authorAvatar ?? res.userAvatar
    res.categoryName = res.categoryName ?? res.forumName
    // 兼容 isTop 和 isEssence 的不同格式
    res.isTop = res.isTop === 1 || res.isTop === true
    res.isEssence = res.isEssence === 1 || res.isEssence === true
    postDetail.value = res
  } catch (error: any) {
    console.error('获取帖子详情失败：', error)
    ElMessage.error(error?.message || '获取帖子详情失败')
  } finally {
    loading.value = false
  }
}

/**
 * 获取评论列表
 */
async function fetchComments() {
  // 在函数内验证postId
  if (!isValidPostId.value) {
    return
  }
  try {
    const res = await getPostComments(postId, queryParams)
    // 兼容后端返回的字段名
    const records = (res.records || res.list || []).map((comment: any) => ({
      ...comment,
      username: comment.username || comment.userName,
      replyToUsername: comment.replyToUsername || comment.replyToUserName,
      children: comment.children || comment.replies || []
    }))
    commentList.value = records
    commentTotal.value = res.total
  } catch (error: any) {
    console.error('获取评论失败：', error)
    ElMessage.error(error?.message || '获取评论失败')
  }
}

/**
 * 处理点赞（toggle模式）
 */
async function handleLike() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  try {
    const result = await likePost(postId)
    if (postDetail.value) {
      // 兼容后端返回的字段名 isLike 或 isLiked
      const isLiked = result.isLike !== undefined ? result.isLike : result.isLiked
      postDetail.value.liked = isLiked
      postDetail.value.isLiked = isLiked
      if (isLiked) {
        postDetail.value.likeCount++
      } else {
        postDetail.value.likeCount--
      }
      ElMessage.success(result.message || (isLiked ? '点赞成功' : '已取消点赞'))
    }
  } catch (error: any) {
    console.error('操作失败：', error)
    ElMessage.error(error?.message || '操作失败，请稍后重试')
  }
}

/**
 * 处理收藏（toggle模式）
 */
async function handleCollect() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  try {
    const result = await collectPost(postId)
    if (postDetail.value) {
      // 兼容后端返回的字段名 isCollect 或 isCollected
      const isCollected = result.isCollect !== undefined ? result.isCollect : result.isCollected
      postDetail.value.collected = isCollected
      postDetail.value.isCollected = isCollected
      if (isCollected) {
        postDetail.value.collectCount++
      } else {
        postDetail.value.collectCount--
      }
      ElMessage.success(result.message || (isCollected ? '收藏成功' : '已取消收藏'))
    }
  } catch (error: any) {
    console.error('操作失败：', error)
    ElMessage.error(error?.message || '操作失败，请稍后重试')
  }
}

/**
 * 处理分享
 */
async function handleShare() {
  // 复制链接到剪贴板
  const url = window.location.href
  try {
    if (navigator.clipboard && navigator.clipboard.writeText) {
      await navigator.clipboard.writeText(url)
    } else {
      // 降级方案：使用textarea
      const textarea = document.createElement('textarea')
      textarea.value = url
      textarea.style.position = 'fixed'
      textarea.style.opacity = '0'
      document.body.appendChild(textarea)
      textarea.select()
      document.execCommand('copy')
      document.body.removeChild(textarea)
    }
    ElMessage.success('链接已复制到剪贴板')
  } catch (error: any) {
    console.error('复制失败:', error)
    ElMessage.error('复制失败，请手动复制链接')
  }
}

/**
 * 处理发表评论
 */
async function handleComment() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  if (!commentContent.value.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }

  submitting.value = true
  try {
    await createComment({
      postId,
      content: commentContent.value,
      parentId: replyTo.value?.id,
    })

    ElMessage.success('评论成功')
    commentContent.value = ''
    replyTo.value = null
    queryParams.page = 1
    fetchComments()
  } catch (error: any) {
    console.error('评论失败：', error)
    ElMessage.error(error?.message || '评论失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

/**
 * 处理回复
 */
function handleReply(comment: CommentVO) {
  replyTo.value = comment
  commentContent.value = `@${comment.username} `
}

/**
 * 处理删除评论
 */
async function handleDeleteComment(commentId: number) {
  try {
    await ElMessageBox.confirm('确定要删除这条评论吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })

    await deleteComment(commentId)
    ElMessage.success('删除成功')
    fetchComments()
  } catch (error: any) {
    // 用户取消或删除失败
    if (error !== 'cancel') {
      console.error('删除评论失败:', error)
      ElMessage.error(error?.message || '删除失败')
    }
  }
}

// ==================== 生命周期 ====================

onMounted(() => {
  fetchPostDetail()
  fetchComments()
})
</script>

<style lang="scss" scoped>
.post-detail-page {
  max-width: 900px;
  margin: 0 auto;
}

.post-card {
  margin-bottom: 16px;

  .post-title {
    font-size: 24px;
    font-weight: bold;
    color: #303133;
    margin-bottom: 16px;
  }

  .post-author {
    display: flex;
    align-items: center;
    gap: 16px;

    .author-info {
      flex: 1;

      .author-name {
        font-size: 14px;
        font-weight: 500;
        color: #303133;

        &:hover {
          color: var(--el-color-primary);
        }
      }

      .post-meta {
        font-size: 12px;
        color: #909399;
        margin-top: 4px;

        span {
          margin-right: 4px;
        }
      }
    }
  }

  .post-content {
    font-size: 14px;
    line-height: 1.8;
    color: #606266;

    :deep(img) {
      max-width: 100%;
      border-radius: 4px;
    }

    :deep(pre) {
      background-color: #f5f7fa;
      padding: 16px;
      border-radius: 4px;
      overflow-x: auto;
    }
  }

  .post-tags {
    margin-top: 24px;

    .tag-item {
      margin-right: 8px;
    }
  }

  .post-interaction {
    margin-top: 24px;
    padding-top: 24px;
    border-top: 1px solid #e4e7ed;
    display: flex;
    gap: 16px;
  }
}

.comment-card {
  .card-header {
    font-weight: bold;
  }

  .comment-form {
    .form-actions {
      display: flex;
      justify-content: flex-end;
      margin-top: 8px;
    }
  }

  .comment-list {
    margin-top: 16px;
  }

  .pagination-wrapper {
    margin-top: 24px;
    display: flex;
    justify-content: center;
  }
}
</style>
