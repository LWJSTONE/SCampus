<template>
  <div class="comment-item">
    <div class="comment-avatar">
      <el-avatar :size="40" :src="localComment.userAvatar">
        {{ localComment.username?.charAt(0) }}
      </el-avatar>
    </div>
    <div class="comment-content">
      <div class="comment-header">
        <router-link :to="`/user/${localComment.userId}`" class="username">
          {{ localComment.username }}
        </router-link>
        <span v-if="localComment.replyToUsername" class="reply-to">
          回复 <router-link :to="`/user/${localComment.replyToUserId}`">@{{ localComment.replyToUsername }}</router-link>
        </span>
        <span class="time">{{ formatTime(localComment.createTime) }}</span>
      </div>
      <div class="comment-text">{{ localComment.content }}</div>
      <div class="comment-actions">
        <el-button link size="small" @click="handleReply">
          <el-icon><ChatDotRound /></el-icon>
          回复
        </el-button>
        <el-button link size="small" :type="localComment.isLiked ? 'primary' : 'default'" @click="handleLike">
          <el-icon><Star /></el-icon>
          {{ localComment.likeCount || 0 }}
        </el-button>
        <el-button
          v-if="canDelete"
          link
          size="small"
          type="danger"
          @click="handleDelete"
        >
          删除
        </el-button>
      </div>
      <!-- 子评论 -->
      <div v-if="commentChildren.length" class="comment-children">
        <CommentItem
          v-for="child in commentChildren"
          :key="child.id"
          :comment="child"
          @reply="(e: CommentVO) => emit('reply', e)"
          @delete="(e: number) => emit('delete', e)"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { likeComment } from '@/api/comment'
import type { CommentVO } from '@/types'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

const props = defineProps<{
  comment: CommentVO
}>()

const emit = defineEmits<{
  reply: [comment: CommentVO]
  delete: [id: number]
}>()

const router = useRouter()
const userStore = useUserStore()

// 防重复点击状态
const liking = ref(false)

// 使用本地副本避免直接修改props
const localComment = ref<CommentVO>({ ...props.comment })

// 监听props变化更新本地副本
watch(() => props.comment, (newVal) => {
  localComment.value = { ...newVal }
}, { deep: true })

// 兼容后端字段名：userName -> username, replies -> children
const commentChildren = computed(() => {
  return localComment.value.children || localComment.value.replies || []
})

const canDelete = computed(() => {
  return userStore.isAdmin || localComment.value.userId === userStore.userInfo?.id
})

function formatTime(time: string) {
  return dayjs(time).fromNow()
}

function handleReply() {
  emit('reply', props.comment)
}

function handleDelete() {
  emit('delete', localComment.value.id)
}

async function handleLike() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  // 防止重复点击
  if (liking.value) {
    return
  }

  liking.value = true
  try {
    const result = await likeComment(localComment.value.id)
    // 更新本地状态和计数（不直接修改props）
    const isLiked = result.isLike
    localComment.value = {
      ...localComment.value,
      isLiked: isLiked,
      // 防止计数器变为负数
      likeCount: Math.max(0, (localComment.value.likeCount || 0) + (isLiked ? 1 : -1))
    }
    ElMessage.success(result.message || (isLiked ? '点赞成功' : '已取消点赞'))
  } catch (e: any) {
    console.error('点赞失败:', e)
    ElMessage.error(e?.message || '点赞失败，请稍后重试')
  } finally {
    liking.value = false
  }
}
</script>

<style scoped lang="scss">
.comment-item {
  display: flex;
  gap: 12px;
  padding: 12px 0;

  .comment-avatar {
    flex-shrink: 0;
  }

  .comment-content {
    flex: 1;
    min-width: 0;

    .comment-header {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-bottom: 8px;

      .username {
        font-weight: 500;
        color: #303133;

        &:hover {
          color: var(--el-color-primary);
        }
      }

      .time {
        font-size: 12px;
        color: #909399;
      }
    }

    .comment-text {
      color: #606266;
      line-height: 1.6;
    }

    .comment-actions {
      display: flex;
      gap: 16px;
      margin-top: 8px;
    }

    .comment-children {
      margin-top: 12px;
      padding-left: 24px;
      border-left: 2px solid #f0f0f0;
    }
  }
}
</style>
