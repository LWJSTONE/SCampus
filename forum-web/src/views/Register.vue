/**
 * 注册页面组件
 * 处理用户注册逻辑
 */
<template>
  <div class="register-page">
    <div class="register-container">
      <!-- Logo 和标题 -->
      <div class="register-header">
        <el-icon :size="48" color="#409eff"><School /></el-icon>
        <h1>注册账号</h1>
        <p>加入我们，开启精彩校园生活</p>
      </div>

      <!-- 注册表单 -->
      <el-form
        ref="formRef"
        :model="registerForm"
        :rules="registerRules"
        class="register-form"
        @submit.prevent="handleRegister"
      >
        <!-- 用户名 -->
        <el-form-item prop="username">
          <el-input
            v-model="registerForm.username"
            placeholder="请输入用户名"
            prefix-icon="User"
            size="large"
          />
        </el-form-item>

        <!-- 邮箱 -->
        <el-form-item prop="email">
          <el-input
            v-model="registerForm.email"
            placeholder="请输入邮箱"
            prefix-icon="Message"
            size="large"
          />
        </el-form-item>

        <!-- 邮箱验证码 -->
        <el-form-item prop="code">
          <div class="code-row">
            <el-input
              v-model="registerForm.code"
              placeholder="请输入验证码"
              prefix-icon="Key"
              size="large"
              class="code-input"
            />
            <el-button
              size="large"
              :disabled="countdown > 0"
              :loading="sendingCode"
              @click="handleSendCode"
            >
              {{ countdown > 0 ? `${countdown}秒后重试` : '发送验证码' }}
            </el-button>
          </div>
        </el-form-item>

        <!-- 密码 -->
        <el-form-item prop="password">
          <el-input
            v-model="registerForm.password"
            type="password"
            placeholder="请输入密码"
            prefix-icon="Lock"
            size="large"
            show-password
          />
        </el-form-item>

        <!-- 确认密码 -->
        <el-form-item prop="confirmPassword">
          <el-input
            v-model="registerForm.confirmPassword"
            type="password"
            placeholder="请确认密码"
            prefix-icon="Lock"
            size="large"
            show-password
          />
        </el-form-item>

        <!-- 用户协议 -->
        <el-form-item prop="agreement">
          <el-checkbox v-model="registerForm.agreement">
            我已阅读并同意
            <el-button link type="primary">《用户协议》</el-button>
            和
            <el-button link type="primary">《隐私政策》</el-button>
          </el-checkbox>
        </el-form-item>

        <!-- 注册按钮 -->
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            class="register-btn"
            :loading="loading"
            @click="handleRegister"
          >
            注册
          </el-button>
        </el-form-item>

        <!-- 登录链接 -->
        <div class="login-link">
          已有账号？
          <router-link to="/login">立即登录</router-link>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * 注册页面组件逻辑
 */
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, FormInstance, FormRules } from 'element-plus'
import { School } from '@element-plus/icons-vue'
import { register, sendEmailCode, getCaptcha } from '@/api/auth'

// ==================== 状态定义 ====================

const router = useRouter()

// 表单引用
const formRef = ref<FormInstance>()

// 加载状态
const loading = ref(false)

// 发送验证码状态
const sendingCode = ref(false)

// 倒计时
const countdown = ref(0)

// 注册表单
const registerForm = reactive({
  username: '',
  email: '',
  code: '',
  password: '',
  confirmPassword: '',
  agreement: false,
  captcha: '',
  captchaKey: ''
})

// 密码确认验证
const validateConfirmPassword = (_rule: any, value: string, callback: any) => {
  if (value !== registerForm.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

// 表单验证规则
const registerRules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度为 3 到 20 个字符', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]+$/, message: '用户名只能包含字母、数字和下划线', trigger: 'blur' },
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' },
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为 6 到 20 个字符', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' },
  ],
  agreement: [
    {
      validator: (_rule, value, callback) => {
        if (!value) {
          callback(new Error('请同意用户协议和隐私政策'))
        } else {
          callback()
        }
      },
      trigger: 'change',
    },
  ],
}

// ==================== 方法定义 ====================

/**
 * 发送验证码
 */
const handleSendCode = async () => {
  // 验证邮箱
  if (!registerForm.email) {
    ElMessage.warning('请先输入邮箱')
    return
  }

  // 邮箱格式验证
  const emailReg = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!emailReg.test(registerForm.email)) {
    ElMessage.warning('请输入正确的邮箱格式')
    return
  }

  sendingCode.value = true
  try {
    await sendEmailCode(registerForm.email)
    ElMessage.success('验证码已发送')

    // 开始倒计时
    countdown.value = 60
    const timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) {
        clearInterval(timer)
      }
    }, 1000)
  } catch (error) {
    console.error('发送验证码失败：', error)
  } finally {
    sendingCode.value = false
  }
}

/**
 * 处理注册
 */
const handleRegister = async () => {
  // 表单验证
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  // 获取图形验证码
  try {
    const captchaData = await getCaptcha()
    registerForm.captchaKey = captchaData.key
    registerForm.captcha = 'skip' // 跳过图形验证码
  } catch (e) {
    console.error('获取验证码失败:', e)
  }

  loading.value = true
  try {
    await register({
      username: registerForm.username,
      password: registerForm.password,
      confirmPassword: registerForm.confirmPassword,
      email: registerForm.email,
      captcha: registerForm.captcha,
      captchaKey: registerForm.captchaKey,
      code: registerForm.code,
    })

    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch (error: any) {
    console.error('注册失败：', error)
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.register-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 24px 0;
}

.register-container {
  width: 400px;
  padding: 32px;
  background-color: #ffffff;
  border-radius: 12px;
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.12);
}

.register-header {
  text-align: center;
  margin-bottom: 32px;

  h1 {
    margin: 8px 0;
    font-size: 18px;
    color: #303133;
  }

  p {
    color: #909399;
    font-size: 13px;
  }
}

.register-form {
  .code-row {
    display: flex;
    gap: 8px;

    .code-input {
      flex: 1;
    }
  }

  .register-btn {
    width: 100%;
  }

  .login-link {
    text-align: center;
    color: #909399;
    font-size: 13px;

    a {
      color: var(--el-color-primary);
      font-weight: 500;

      &:hover {
        text-decoration: underline;
      }
    }
  }
}
</style>
