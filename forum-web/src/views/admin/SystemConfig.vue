<template>
  <div class="system-config-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>系统配置</span>
          <el-tag type="info" size="small">本地存储</el-tag>
        </div>
      </template>

      <el-alert
        title="提示"
        type="info"
        description="当前配置保存在浏览器本地存储中，后端配置服务未启用。完整功能需要后端配置服务支持。"
        :closable="false"
        show-icon
        style="margin-bottom: 20px"
      />

      <el-form ref="formRef" :model="config" :rules="formRules" label-width="120px" style="max-width: 600px">
        <el-form-item label="网站名称" prop="siteName">
          <el-input v-model="config.siteName" maxlength="50" />
        </el-form-item>
        <el-form-item label="网站描述" prop="siteDescription">
          <el-input v-model="config.siteDescription" type="textarea" :rows="3" maxlength="200" show-word-limit />
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
        <el-form-item label="每页显示数" prop="pageSize">
          <el-input-number v-model="config.pageSize" :min="10" :max="100" />
        </el-form-item>
        <el-form-item label="上传限制(MB)" prop="uploadLimit">
          <el-input-number v-model="config.uploadLimit" :min="1" :max="100" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="saving" @click="handleSave">保存配置</el-button>
          <el-button @click="handleReset">恢复默认</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'

const CONFIG_STORAGE_KEY = 'scampus_system_config'

// 默认配置
const defaultConfig = {
  siteName: 'SCampus校园论坛',
  siteDescription: '一个现代化的校园论坛系统',
  allowRegister: true,
  enableCaptcha: true,
  postAudit: false,
  pageSize: 20,
  uploadLimit: 10
}

const saving = ref(false)
const formRef = ref<FormInstance>()

const config = reactive({ ...defaultConfig })

// 表单验证规则
const formRules: FormRules = {
  siteName: [
    { required: true, message: '请输入网站名称', trigger: 'blur' },
    { max: 50, message: '网站名称不能超过50个字符', trigger: 'blur' }
  ],
  siteDescription: [
    { max: 200, message: '网站描述不能超过200个字符', trigger: 'blur' }
  ],
  pageSize: [
    { type: 'number', min: 10, max: 100, message: '每页显示数需在10-100之间', trigger: 'blur' }
  ],
  uploadLimit: [
    { type: 'number', min: 1, max: 100, message: '上传限制需在1-100MB之间', trigger: 'blur' }
  ]
}

// 从本地存储加载配置
function loadConfig() {
  try {
    const savedConfig = localStorage.getItem(CONFIG_STORAGE_KEY)
    if (savedConfig) {
      const parsed = JSON.parse(savedConfig)
      Object.assign(config, parsed)
    }
  } catch (e) {
    console.error('加载配置失败:', e)
  }
}

// 保存配置到本地存储
function saveConfig() {
  try {
    localStorage.setItem(CONFIG_STORAGE_KEY, JSON.stringify(config))
    return true
  } catch (e) {
    console.error('保存配置失败:', e)
    return false
  }
}

async function fetchConfig() {
  // 尝试从后端加载配置（如果后端支持）
  // 目前使用本地存储
  loadConfig()
}

async function handleSave() {
  // 表单验证
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    // 模拟保存延迟
    await new Promise(resolve => setTimeout(resolve, 300))

    // 保存到本地存储
    if (saveConfig()) {
      ElMessage.success('配置已保存到本地存储')
    } else {
      ElMessage.error('保存配置失败')
    }
  } finally {
    saving.value = false
  }
}

function handleReset() {
  Object.assign(config, defaultConfig)
  ElMessage.success('已恢复默认配置，请点击保存生效')
}

onMounted(() => {
  fetchConfig()
})
</script>

<style scoped lang="scss">
.system-config-page {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
}
</style>
