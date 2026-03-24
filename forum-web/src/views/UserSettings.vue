<template>
  <div class="user-settings-page">
    <el-card>
      <template #header>
        <h2>个人设置</h2>
      </template>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="头像">
          <el-avatar :size="60" :src="form.avatar">
            {{ form.nickname?.charAt(0) }}
          </el-avatar>
          <el-upload
            class="avatar-upload"
            :show-file-list="false"
            :before-upload="beforeAvatarUpload"
            :http-request="handleAvatarUpload"
            accept="image/*"
          >
            <el-button link type="primary" style="margin-left: 12px" :loading="uploadingAvatar">更换头像</el-button>
          </el-upload>
        </el-form-item>

        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="form.nickname" placeholder="请输入昵称" maxlength="20" />
        </el-form-item>

        <el-form-item label="个性签名" prop="bio">
          <el-input
            v-model="form.bio"
            type="textarea"
            :rows="3"
            placeholder="请输入个性签名"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="专业" prop="major">
          <el-input v-model="form.major" placeholder="请输入专业" />
        </el-form-item>

        <el-form-item label="年级" prop="grade">
          <el-input v-model="form.grade" placeholder="请输入年级" />
        </el-form-item>

        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="form.gender">
            <el-radio :value="0">保密</el-radio>
            <el-radio :value="1">男</el-radio>
            <el-radio :value="2">女</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="password-card">
      <template #header>
        <h3>修改密码</h3>
      </template>
      <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-width="100px">
        <el-form-item label="当前密码" prop="oldPassword">
          <el-input v-model="pwdForm.oldPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="pwdForm.newPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="pwdForm.confirmPassword" type="password" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="savingPwd" @click="handleChangePwd">
            修改密码
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, FormInstance, FormRules, type UploadRequestOptions } from 'element-plus'
import { updateUser, updatePassword, updateAvatar, getCurrentUserInfo } from '@/api/user'
import { request } from '@/api/request'
import { useUserStore } from '@/stores/user'
import type { UserUpdateDTO } from '@/types'

const userStore = useUserStore()
const router = useRouter()
const formRef = ref<FormInstance>()
const pwdFormRef = ref<FormInstance>()
const saving = ref(false)
const savingPwd = ref(false)
const uploadingAvatar = ref(false)

const form = reactive<Partial<UserUpdateDTO>>({
  nickname: '',
  bio: '',
  major: '',
  grade: '',
  gender: 0,
  avatar: ''
})

const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const rules: FormRules = {
  nickname: [
    { min: 2, max: 20, message: '昵称长度为2-20个字符', trigger: 'blur' }
  ]
}

const pwdRules: FormRules = {
  oldPassword: [{ required: true, message: '请输入当前密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20个字符', trigger: 'blur' },
    { pattern: /^(?=.*[a-zA-Z])(?=.*\d).+$/, message: '密码必须包含字母和数字', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (_, value, callback) => {
        if (value !== pwdForm.newPassword) {
          callback(new Error('两次密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

async function fetchUserInfo() {
  try {
    const user = await getCurrentUserInfo()
    Object.assign(form, user)
  } catch (e: any) {
    console.error('获取用户信息失败:', e)
    ElMessage.error(e?.message || '获取用户信息失败')
  }
}

// 头像上传前验证
function beforeAvatarUpload(file: File) {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB!')
    return false
  }
  return true
}

// 上传头像
async function handleAvatarUpload(options: UploadRequestOptions) {
  const file = options.file

  // 验证用户是否已登录
  if (!userStore.userInfo?.id) {
    ElMessage.error('用户信息获取失败，请重新登录')
    uploadingAvatar.value = false
    return
  }

  uploadingAvatar.value = true

  try {
    // 先上传文件到服务器
    const formData = new FormData()
    formData.append('file', file)

    const uploadRes = await request.post<{ url: string }>('/files/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })

    const avatarUrl = uploadRes.url

    // 更新头像URL
    await updateAvatar(userStore.userInfo.id, avatarUrl)
    form.avatar = avatarUrl

    // 更新 store 中的用户信息
    userStore.updateUserInfo({ avatar: avatarUrl })

    ElMessage.success('头像更新成功')
  } catch (e: any) {
    console.error('上传头像失败:', e)
    ElMessage.error(e?.message || '上传头像失败')
  } finally {
    uploadingAvatar.value = false
  }
}

async function handleSave() {
  // 防止重复提交
  if (saving.value) return
  
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  // 验证用户是否已登录
  if (!userStore.userInfo?.id) {
    ElMessage.error('用户信息获取失败，请重新登录')
    return
  }

  saving.value = true
  try {
    // 只发送需要更新的字段
    const updateData: Partial<UserUpdateDTO> = {
      nickname: form.nickname,
      bio: form.bio,
      major: form.major,
      grade: form.grade,
      gender: form.gender
    }
    await updateUser(userStore.userInfo.id, updateData)
    // 更新 store 中的用户信息
    userStore.updateUserInfo(updateData)
    ElMessage.success('保存成功')
  } catch (e: any) {
    console.error('保存失败:', e)
    ElMessage.error(e?.message || '保存失败，请稍后重试')
  } finally {
    saving.value = false
  }
}

async function handleChangePwd() {
  // 防止重复提交
  if (savingPwd.value) return
  
  const valid = await pwdFormRef.value?.validate().catch(() => false)
  if (!valid) return

  // 验证用户是否已登录
  if (!userStore.userInfo?.id) {
    ElMessage.error('用户信息获取失败，请重新登录')
    return
  }

  savingPwd.value = true
  try {
    await updatePassword(userStore.userInfo.id, {
      oldPassword: pwdForm.oldPassword,
      newPassword: pwdForm.newPassword
    })
    ElMessage.success('密码修改成功，请重新登录')
    pwdFormRef.value?.resetFields()
    
    // 密码修改成功后退出登录
    setTimeout(() => {
      userStore.logout()
      router.push('/login')
    }, 1500)
  } catch (e: any) {
    console.error('修改密码失败:', e)
    ElMessage.error(e?.message || '修改密码失败，请检查当前密码是否正确')
  } finally {
    savingPwd.value = false
  }
}

onMounted(() => {
  // 检查登录状态
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  fetchUserInfo()
})
</script>

<style scoped lang="scss">
.user-settings-page {
  max-width: 800px;
  margin: 0 auto;

  h2, h3 {
    margin: 0;
    font-size: 18px;
  }

  .password-card {
    margin-top: 16px;
  }
}
</style>
