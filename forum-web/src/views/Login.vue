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
      <el-form :model="forgotForm" label-width="80px">
        <el-form-item label="用户名">
          <el-input v-model="forgotForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="forgotForm.email" placeholder="请输入注册邮箱" />
        </el-form-item>
        <el-form-item label="验证码">
          <div class="captcha-row">
            <el-input v-model="forgotForm.code" placeholder="邮箱验证码" style="flex: 1" />
            <el-button :disabled="forgotCountdown > 0" :loading="sendingCode" @click="sendForgotCode">
              {{ forgotCountdown > 0 ? `${forgotCountdown}秒后重试` : '发送验证码' }}
            </el-button>
          </div>
        </el-form-item>
        <el-form-item label="新密码">
          <el-input v-model="forgotForm.newPassword" type="password" show-password placeholder="请输入新密码" />
        </el-form-item>
        <el-form-item label="确认密码">
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
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { getCaptcha, sendEmailCode, resetPassword } from '@/api/auth'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const formRef = ref<FormInstance>()
const loading = ref(false)
const captchaUrl = ref('')
const captchaKey = ref('')

const loginForm = reactive({
  username: '',
  password: '',
  captcha: '',
  rememberMe: false
})

const rules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  captcha: [
    { required: true, message: '请输入验证码', trigger: 'blur' }
  ]
}

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
  } catch (e) {
    console.error('发送验证码失败:', e)
  } finally {
    sendingCode.value = false
  }
}

async function handleResetPassword() {
  if (!forgotForm.username) {
    ElMessage.warning('请输入用户名')
    return
  }
  if (!forgotForm.email) {
    ElMessage.warning('请输入邮箱')
    return
  }
  if (!forgotForm.code) {
    ElMessage.warning('请输入验证码')
    return
  }
  if (!forgotForm.newPassword) {
    ElMessage.warning('请输入新密码')
    return
  }
  if (forgotForm.newPassword !== forgotForm.confirmPassword) {
    ElMessage.warning('两次输入的密码不一致')
    return
  }
  if (forgotForm.newPassword.length < 6) {
    ElMessage.warning('密码长度不能少于6位')
    return
  }
  
  resettingPassword.value = true
  try {
    await resetPassword({
      email: forgotForm.email,
      code: forgotForm.code,
      password: forgotForm.newPassword
    })
    ElMessage.success('密码重置成功，请登录')
    forgotPasswordVisible.value = false
  } catch (e) {
    console.error('重置密码失败:', e)
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
  const valid = await formRef.value?.validate()
  if (!valid) return

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
  } catch (e) {
    refreshCaptcha()
    console.error('登录失败:', e)
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
