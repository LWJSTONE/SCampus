<template>
  <div class="login-page">
    <div class="login-card">
      <div class="login-header">
        <h2>SCampus 校园论坛</h2>
        <p>欢迎回来，请登录您的账号</p>
      </div>

      <el-form
        ref="formRef"
        :model="loginForm"
        :rules="rules"
        class="login-form"
        @submit.prevent="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名"
            prefix-icon="User"
            size="large"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            prefix-icon="Lock"
            size="large"
            show-password
          />
        </el-form-item>

        <el-form-item prop="captcha" v-if="captchaUrl">
          <div class="captcha-row">
            <el-input
              v-model="loginForm.captcha"
              placeholder="请输入验证码"
              prefix-icon="Picture"
              size="large"
              style="flex: 1"
            />
            <img :src="captchaUrl" @click="refreshCaptcha" class="captcha-img" alt="验证码" />
          </div>
        </el-form-item>

        <el-form-item>
          <div class="form-options">
            <el-checkbox v-model="loginForm.rememberMe">记住我</el-checkbox>
            <el-link type="primary" @click="showForgotPassword">忘记密码？</el-link>
          </div>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            class="login-btn"
            @click="handleLogin"
          >
            登 录
          </el-button>
        </el-form-item>

        <div class="register-link">
          还没有账号？
          <router-link to="/register">立即注册</router-link>
        </div>
      </el-form>
    </div>

    <!-- 忘记密码对话框 -->
    <el-dialog v-model="forgotPasswordVisible" title="重置密码" width="400px">
      <el-form ref="forgotFormRef" :model="forgotForm" :rules="forgotRules" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="forgotForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="forgotForm.email" placeholder="请输入注册邮箱" />
        </el-form-item>
        <el-form-item label="验证码" prop="code">
          <div class="captcha-row">
            <el-input v-model="forgotForm.code" placeholder="邮箱验证码" style="flex: 1" />
            <el-button :disabled="forgotCountdown > 0" :loading="sendingCode" @click="sendForgotCode">
              {{ forgotCountdown > 0 ? `${forgotCountdown}秒后重试` : '发送验证码' }}
            </el-button>
          </div>
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="forgotForm.newPassword" type="password" show-password placeholder="请输入新密码" />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="forgotForm.confirmPassword" type="password" show-password placeholder="请确认新密码" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="forgotPasswordVisible = false">取消</el-button>
        <el-button type="primary" :loading="resettingPassword" @click="handleResetPassword">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { getCaptcha, sendEmailCode, resetPassword } from '@/api/auth'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const formRef = ref<FormInstance>()
const forgotFormRef = ref<FormInstance>()
const loading = ref(false)
const captchaUrl = ref('')
const captchaKey = ref('')

const loginForm = reactive({
  username: '',
  password: '',
  captcha: '',
  rememberMe: false
})

// 动态验证规则：当captchaUrl存在时才要求验证码
const rules = computed<FormRules>(() => ({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  captcha: captchaUrl.value ? [
    { required: true, message: '请输入验证码', trigger: 'blur' }
  ] : []
}))

// 忘记密码相关
const forgotPasswordVisible = ref(false)
const sendingCode = ref(false)
const resettingPassword = ref(false)
const forgotCountdown = ref(0)
let forgotCountdownTimer: ReturnType<typeof setInterval> | null = null

const forgotForm = reactive({
  username: '',
  email: '',
  code: '',
  newPassword: '',
  confirmPassword: ''
})

// 忘记密码表单验证规则
const validateConfirmPassword = (_rule: any, value: string, callback: any) => {
  if (value !== forgotForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const forgotRules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度为 3 到 20 个字符', trigger: 'blur' },
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' },
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为 6 到 20 个字符', trigger: 'blur' },
    { pattern: /^(?=.*[a-zA-Z])(?=.*\d).+$/, message: '密码必须包含字母和数字', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' },
  ],
}

function showForgotPassword() {
  forgotPasswordVisible.value = true
  Object.assign(forgotForm, {
    username: '',
    email: '',
    code: '',
    newPassword: '',
    confirmPassword: ''
  })
}

async function sendForgotCode() {
  // 验证用户名
  if (!forgotForm.username || !forgotForm.username.trim()) {
    ElMessage.warning('请输入用户名')
    return
  }
  // 验证邮箱
  if (!forgotForm.email) {
    ElMessage.warning('请输入邮箱')
    return
  }
  const emailReg = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!emailReg.test(forgotForm.email)) {
    ElMessage.warning('请输入正确的邮箱格式')
    return
  }

  sendingCode.value = true
  try {
    await sendEmailCode(forgotForm.email)
    ElMessage.success('验证码已发送')
    forgotCountdown.value = 60
    forgotCountdownTimer = setInterval(() => {
      forgotCountdown.value--
      if (forgotCountdown.value <= 0) {
        if (forgotCountdownTimer) {
          clearInterval(forgotCountdownTimer)
          forgotCountdownTimer = null
        }
      }
    }, 1000)
  } catch (e: any) {
    console.error('发送验证码失败:', e)
    ElMessage.error(e?.message || '发送验证码失败，请稍后重试')
  } finally {
    sendingCode.value = false
  }
}

async function handleResetPassword() {
  // 表单验证
  const valid = await forgotFormRef.value?.validate().catch(() => false)
  if (!valid) {
    ElMessage.warning('请检查表单')
    return
  }

  resettingPassword.value = true
  try {
    await resetPassword({
      email: forgotForm.email,
      code: forgotForm.code,
      password: forgotForm.newPassword,
      username: forgotForm.username
    })
    ElMessage.success('密码重置成功，请登录')
    forgotPasswordVisible.value = false
  } catch (e: any) {
    console.error('重置密码失败:', e)
    ElMessage.error(e?.message || '重置密码失败，请检查信息是否正确')
  } finally {
    resettingPassword.value = false
  }
}

async function refreshCaptcha() {
  try {
    const res = await getCaptcha()
    // 兼容两种字段名
    captchaUrl.value = res.captchaImage || res.image || ''
    captchaKey.value = res.captchaKey || res.key || ''
  } catch (e) {
    console.error('获取验证码失败:', e)
  }
}

async function handleLogin() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) {
    ElMessage.warning('请检查表单')
    return
  }

  loading.value = true
  try {
    await userStore.login({
      username: loginForm.username,
      password: loginForm.password,
      captcha: loginForm.captcha,
      captchaKey: captchaKey.value,
      rememberMe: loginForm.rememberMe
    })

    ElMessage.success('登录成功')

    // 跳转到之前的页面或首页
    const redirect = route.query.redirect as string
    router.push(redirect || '/')
  } catch (e: any) {
    refreshCaptcha()
    console.error('登录失败:', e)
    ElMessage.error(e?.message || '登录失败，请检查用户名和密码')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  refreshCaptcha()
})

onUnmounted(() => {
  if (forgotCountdownTimer) {
    clearInterval(forgotCountdownTimer)
    forgotCountdownTimer = null
  }
})
</script>

<style scoped lang="scss">
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);

  .login-card {
    width: 400px;
    padding: 40px;
    background: #fff;
    border-radius: 12px;
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);

    .login-header {
      text-align: center;
      margin-bottom: 32px;

      h2 {
        margin: 0 0 8px;
        font-size: 24px;
        color: #303133;
      }

      p {
        margin: 0;
        color: #909399;
        font-size: 14px;
      }
    }

    .login-form {
      .captcha-row {
        display: flex;
        gap: 12px;

        .captcha-img {
          width: 120px;
          height: 40px;
          cursor: pointer;
          border-radius: 4px;
        }
      }

      .form-options {
        width: 100%;
        display: flex;
        justify-content: space-between;
        align-items: center;
      }

      .login-btn {
        width: 100%;
      }

      .register-link {
        text-align: center;
        color: #909399;
        font-size: 14px;

        a {
          color: var(--el-color-primary);
          text-decoration: none;

          &:hover {
            text-decoration: underline;
          }
        }
      }
    }
  }
}
</style>
