<template>
  <div class="not-found-page">
    <div class="content">
      <h1>404</h1>
      <p>抱歉，您访问的页面不存在</p>
      <el-button type="primary" @click="goHome">返回首页</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

function goHome() {
  router.push('/').catch((err) => {
    console.error('路由跳转失败:', err)
  })
}
</script>

<style scoped lang="scss">
.not-found-page {
  min-height: calc(100vh - 200px);
  display: flex;
  align-items: center;
  justify-content: center;

  .content {
    text-align: center;

    h1 {
      font-size: 120px;
      margin: 0;
      color: var(--el-color-primary);
      line-height: 1;
    }

    p {
      font-size: 18px;
      color: #909399;
      margin: 24px 0;
    }
  }
}
</style>
