<template>
  <div class="login-container">
    <el-card class="login-card">
      <template #header>
        <h2>登录</h2>
      </template>
      
      <el-form :model="loginForm" :rules="rules" ref="formRef" @submit.prevent="handleLogin">
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
        
        <el-form-item>
          <el-alert
            title="默认账号"
            type="info"
            :closable="false"
            show-icon
          >
            <template #default>
              <p>管理员账号：<strong>admin</strong> / 密码：<strong>admin123</strong></p>
              <p>普通用户：请先注册账号</p>
            </template>
          </el-alert>
        </el-form-item>
        
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            @click="handleLogin"
            style="width: 100%"
          >
            登录
          </el-button>
        </el-form-item>
        
        <div class="register-link">
          还没有账号？<router-link to="/register">立即注册</router-link>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { login } from '@/api/user'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const res = await login(loginForm)
        const role = res.role
        
        userStore.setToken(res.token)
        userStore.setUserInfo(res.user)
        userStore.setRole(role)
        
        ElMessage.success({ message: '登录成功', duration: 800 })
        
        if (role === 'ADMIN') {
          router.push('/admin')
        } else {
          router.push('/home')
        }
      } catch (error) {
        ElMessage.error({ message: error.message || '登录失败', duration: 800 })
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-card {
  width: 450px;
  border-radius: 10px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.login-card :deep(.el-card__header) {
  text-align: center;
  background: #f5f7fa;
  border-bottom: 1px solid #ebeef5;
}

.login-card h2 {
  margin: 0;
  color: #303133;
}

.register-link {
  text-align: center;
  margin-top: 10px;
}

.register-link a {
  color: #409eff;
  text-decoration: none;
}

.register-link a:hover {
  text-decoration: underline;
}

:deep(.el-alert) {
  padding: 12px;
}

:deep(.el-alert__title) {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 8px;
}

:deep(.el-alert__description) {
  font-size: 13px;
  line-height: 1.6;
}

:deep(.el-alert__description p) {
  margin: 4px 0;
}

:deep(.el-alert__description strong) {
  color: #409eff;
  font-weight: 600;
}
</style>
