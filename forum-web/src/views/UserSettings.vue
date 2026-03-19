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
          <el-button link type="primary" style="margin-left: 12px">更换头像</el-button>
        </el-form-item>

        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="form.nickname" placeholder="请输入昵称" maxlength="20" />
        </el-form-item>

        <el-form-item label="个性签名" prop="signature">
          <el-input
            v-model="form.signature"
            type="textarea"
            :rows="3"
            placeholder="请输入个性签名"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="学校" prop="school">
          <el-input v-model="form.school" placeholder="请输入学校" />
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
          <el-button type="primary" :loading="changingPwd" @click="handleChangePwd">
            修改密码
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, FormInstance, FormRules } from 'element-plus'
import { updateUser, updatePassword, getCurrentUserInfo } from '@/api/user'
import { useUserStore } from '@/stores/user'
import type { UserUpdateDTO } from '@/types'

const userStore = useUserStore()
const formRef = ref<FormInstance>()
const pwdFormRef = ref<FormInstance>()
const saving = ref(false)
const changingPwd = ref(false)

const form = reactive<UserUpdateDTO>({
  nickname: '',
  signature: '',
  school: '',
  gender: 0
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
    { min: 6, max: 20, message: '密码长度为6-20个字符', trigger: 'blur' }
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
  } catch (e) {
    console.error('获取用户信息失败:', e)
  }
}

async function handleSave() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    await updateUser(userStore.userInfo!.id, form)
    ElMessage.success('保存成功')
  } catch (e) {
    console.error('保存失败:', e)
  } finally {
    saving.value = false
  }
}

async function handleChangePwd() {
  const valid = await pwdFormRef.value?.validate().catch(() => false)
  if (!valid) return

  changingPwd.value = true
  try {
    await updatePassword(userStore.userInfo!.id, {
      oldPassword: pwdForm.oldPassword,
      newPassword: pwdForm.newPassword
    })
    ElMessage.success('密码修改成功')
    pwdFormRef.value?.resetFields()
  } catch (e) {
    console.error('修改密码失败:', e)
  } finally {
    changingPwd.value = false
  }
}

onMounted(() => {
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
