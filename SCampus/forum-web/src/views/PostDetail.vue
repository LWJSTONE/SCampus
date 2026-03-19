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
          <el-avatar :size="40" :src="postDetail.authorAvatar">
            {{ postDetail.authorName?.charAt(0) }}
          </el-avatar>
          <div class="author-info">
            <router-link :to="`/user/${postDetail.authorId}`" class="author-name">
              {{ postDetail.authorName }}
            </router-link>
            <div class="post-meta">
              <span>{{ formatRelativeTime(postDetail.createTime) }}</span>
              <span>·</span>
              <span>{{ postDetail.categoryName }}</span>
              <span>·</span>
              <span>{{ postDetail.viewCount }} 浏览</span>
            </div>
          </div>
          <div class="post-actions">
            <!-- 标签 -->
            <template v-if="postDetail.isTop === 1">
              <el-tag type="danger" size="small">置顶</el-tag>
            </template>
            <template v-if="postDetail.isEssence === 1">
              <el-tag type="warning" size="small">精华</el-tag>
            </template>
          </div>
        </div>

        <el-divider />

        <!-- 正文内容 -->
        <div class="post-content" v-html="postDetail.content"></div>

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
            :icon="postDetail.liked ? 'StarFilled' : 'Star'"
            @click="handleLike"
          >
            点赞 ({{ postDetail.likeCount }})
          </el-button>
          <el-button
            :type="postDetail.collected ? 'warning' : 'default'"
            :icon="postDetail.collected ? 'CollectionTag' : 'Collection'"
            @click="handleCollect"
          >
            收藏 ({{ postDetail.collectCount }})
          </el-button>
          <el-button icon="Share" @click="handleShare">分享</el-button>
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
        <div class="pagination-wrapper" v-if="commentTotal > queryParams.pageSize">
          <el-pagination
            v-model:current-page="queryParams.pageNum"
            v-model:page-size="queryParams.pageSize"
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
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getPostDetail, likePost, unlikePost, collectPost, uncollectPost } from '@/api/post'
import { getPostComments, createComment, deleteComment } from '@/api/comment'
import { PostDetail, Comment, PageQuery } from '@/types'
import { formatRelativeTime } from '@/utils/date'
import { useUserStore } from '@/stores/user'
import CommentItem from '@/components/CommentItem.vue'

// ==================== 状态定义 ====================

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 帖子ID
const postId = Number(route.params.id)

// 加载状态
const loading = ref(false)
const submitting = ref(false)

// 帖子详情
const postDetail = ref<PostDetail | null>(null)

// 评论列表
const commentList = ref<Comment[]>([])
const commentTotal = ref(0)

// 评论内容
const commentContent = ref('')

// 回复的评论
const replyTo = ref<Comment | null>(null)

// 查询参数
const queryParams = reactive<PageQuery>({
  pageNum: 1,
  pageSize: 10,
})

// ==================== 方法定义 ====================

/**
 * 获取帖子详情
 */
const fetchPostDetail = async () => {
  loading.value = true
  try {
    const { data } = await getPostDetail(postId)
    postDetail.value = data
  } catch (error) {
    console.error('获取帖子详情失败：', error)
  } finally {
    loading.value = false
  }
}

/**
 * 获取评论列表
 */
const fetchComments = async () => {
  try {
    const { data } = await getPostComments(postId, queryParams)
    commentList.value = data.list
    commentTotal.value = data.total
  } catch (error) {
    console.error('获取评论失败：', error)
  }
}

/**
 * 处理点赞
 */
const handleLike = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  try {
    if (postDetail.value?.liked) {
      await unlikePost(postId)
      postDetail.value.liked = false
      postDetail.value.likeCount--
    } else {
      await likePost(postId)
      if (postDetail.value) {
        postDetail.value.liked = true
        postDetail.value.likeCount++
      }
    }
  } catch (error) {
    console.error('操作失败：', error)
  }
}

/**
 * 处理收藏
 */
const handleCollect = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  try {
    if (postDetail.value?.collected) {
      await uncollectPost(postId)
      postDetail.value.collected = false
      postDetail.value.collectCount--
    } else {
      await collectPost(postId)
      if (postDetail.value) {
        postDetail.value.collected = true
        postDetail.value.collectCount++
      }
    }
  } catch (error) {
    console.error('操作失败：', error)
  }
}

/**
 * 处理分享
 */
const handleShare = () => {
  // 复制链接到剪贴板
  const url = window.location.href
  navigator.clipboard.writeText(url)
  ElMessage.success('链接已复制到剪贴板')
}

/**
 * 处理发表评论
 */
const handleComment = async () => {
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
      rootId: replyTo.value?.rootId || replyTo.value?.id,
      replyToId: replyTo.value?.id,
    })

    ElMessage.success('评论成功')
    commentContent.value = ''
    replyTo.value = null
    queryParams.pageNum = 1
    fetchComments()
  } catch (error) {
    console.error('评论失败：', error)
  } finally {
    submitting.value = false
  }
}

/**
 * 处理回复
 */
const handleReply = (comment: Comment) => {
  replyTo.value = comment
  commentContent.value = `@${comment.authorName} `
}

/**
 * 处理删除评论
 */
const handleDeleteComment = async (commentId: number) => {
  try {
    await ElMessageBox.confirm('确定要删除这条评论吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })

    await deleteComment(commentId)
    ElMessage.success('删除成功')
    fetchComments()
  } catch (error) {
    // 用户取消或删除失败
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
  margin-bottom: $spacing-md;

  .post-title {
    font-size: $font-size-xxl;
    font-weight: bold;
    color: $text-primary;
    margin-bottom: $spacing-md;
  }

  .post-author {
    display: flex;
    align-items: center;
    gap: $spacing-md;

    .author-info {
      flex: 1;

      .author-name {
        font-size: $font-size-md;
        font-weight: 500;
        color: $text-primary;

        &:hover {
          color: $primary-color;
        }
      }

      .post-meta {
        font-size: $font-size-xs;
        color: $text-secondary;
        margin-top: $spacing-xs;

        span {
          margin-right: $spacing-xs;
        }
      }
    }
  }

  .post-content {
    font-size: $font-size-md;
    line-height: 1.8;
    color: $text-regular;

    :deep(img) {
      max-width: 100%;
      border-radius: $radius-md;
    }

    :deep(pre) {
      background-color: $bg-light;
      padding: $spacing-md;
      border-radius: $radius-md;
      overflow-x: auto;
    }
  }

  .post-tags {
    margin-top: $spacing-lg;

    .tag-item {
      margin-right: $spacing-sm;
    }
  }

  .post-interaction {
    margin-top: $spacing-lg;
    padding-top: $spacing-lg;
    border-top: 1px solid $border-lighter;
    display: flex;
    gap: $spacing-md;
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
      margin-top: $spacing-sm;
    }
  }

  .comment-list {
    margin-top: $spacing-md;
  }

  .pagination-wrapper {
    margin-top: $spacing-lg;
    display: flex;
    justify-content: center;
  }
}
</style>
