<template>
  <div class="system-config-page">
    <el-card>
      <template #header>
        <span>系统配置</span>
      </template>

      <el-form :model="config" label-width="120px" style="max-width: 600px">
        <el-form-item label="网站名称">
          <el-input v-model="config.siteName" />
        </el-form-item>
        <el-form-item label="网站描述">
          <el-input v-model="config.siteDescription" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="开启注册">
          <el-switch v-model="config.allowRegister" />
        </el-form-item>
        <el-form-item label="开启验证码">
          <el-switch v-model="config.enableCaptcha" />
        </el-form-item>
        <el-form-item label="帖子审核">
          <el-switch v-model="config.postAudit" />
        </el-form-item>
        <el-form-item label="每页显示数">
          <el-input-number v-model="config.pageSize" :min="10" :max="100" />
        </el-form-item>
        <el-form-item label="上传限制(MB)">
          <el-input-number v-model="config.uploadLimit" :min="1" :max="100" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="saving" @click="handleSave">保存配置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'

const saving = ref(false)

const config = reactive({
  siteName: 'SCampus校园论坛',
  siteDescription: '一个现代化的校园论坛系统',
  allowRegister: true,
  enableCaptcha: true,
  postAudit: false,
  pageSize: 20,
  uploadLimit: 10
})

async function fetchConfig() {
  // 模拟加载配置
}

async function handleSave() {
  saving.value = true
  try {
    // 模拟保存
    await new Promise(resolve => setTimeout(resolve, 500))
    ElMessage.success('保存成功')
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  fetchConfig()
})
</script>

<style scoped lang="scss">
.system-config-page {
  // 样式
}
</style>
