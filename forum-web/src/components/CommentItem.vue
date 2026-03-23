<template>
  <div class="comment-item">
    <div class="comment-avatar">
      <el-avatar :size="40" :src="comment.userAvatar">
        {{ comment.username?.charAt(0) }}
      </el-avatar>
    </div>
    <div class="comment-content">
      <div class="comment-header">
        <router-link :to="`/user/${comment.userId}`" class="username">
          {{ comment.username }}
        </router-link>
        <span v-if="comment.replyToUsername" class="reply-to">
          回复 <router-link :to="`/user/${comment.replyToUserId}`">@{{ comment.replyToUsername }}</router-link>
        </span>
        <span class="time">{{ formatTime(comment.createTime) }}</span>
      </div>
      <div class="comment-text">{{ comment.content }}</div>
      <div class="comment-actions">
        <el-button link size="small" @click="handleReply">
          <el-icon><ChatDotRound /></el-icon>
          回复
        </el-button>
        <el-button link size="small" :type="comment.isLiked ? 'primary' : 'default'" @click="handleLike">
          <el-icon><Star /></el-icon>
          {{ comment.likeCount || 0 }}
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
import { computed } from 'vue'
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

const userStore = useUserStore()

// 兼容后端字段名：userName -> username, replies -> children
const commentChildren = computed(() => {
  return props.comment.children || props.comment.replies || []
})

const canDelete = computed(() => {
  return userStore.isAdmin || props.comment.userId === userStore.userInfo?.id
})

function formatTime(time: string) {
  return dayjs(time).fromNow()
}

function handleReply() {
  emit('reply', props.comment)
}

function handleDelete() {
  emit('delete', props.comment.id)
}

async function handleLike() {
  if (!userStore.isLoggedIn) {
    return
  }
  try {
    const result = await likeComment(props.comment.id)
    // 更新本地状态和计数
    const isLiked = result.isLike
    props.comment.isLiked = isLiked
    props.comment.likeCount = (props.comment.likeCount || 0) + (isLiked ? 1 : -1)
  } catch (e) {
    console.error('点赞失败:', e)
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
