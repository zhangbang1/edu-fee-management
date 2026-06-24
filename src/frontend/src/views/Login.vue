<template>
  <div class="login-container">
    <div class="login-bg">
      <div class="bg-shape shape-1"></div>
      <div class="bg-shape shape-2"></div>
      <div class="bg-shape shape-3"></div>
    </div>

    <div class="login-card-wrapper">
      <div class="login-header">
        <div class="login-icon">
          <el-icon :size="40" color="#fff"><School /></el-icon>
        </div>
        <h1 class="login-title">EduFeeMS</h1>
        <p class="login-subtitle">教育培训机构教务收费管理系统</p>
      </div>

      <el-card class="login-card" shadow="always">
        <h2 class="login-form-title">用户登录</h2>

        <el-form
          ref="formRef"
          :model="loginForm"
          :rules="rules"
          size="large"
          @keyup.enter="handleLogin"
        >
          <el-form-item prop="username">
            <el-input
              v-model="loginForm.username"
              placeholder="请输入用户名"
              :prefix-icon="User"
              clearable
            />
          </el-form-item>

          <el-form-item prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
              :prefix-icon="Lock"
              show-password
              clearable
            />
          </el-form-item>

          <el-form-item>
            <div class="form-options">
              <el-checkbox v-model="loginForm.remember">记住密码</el-checkbox>
            </div>
          </el-form-item>

          <el-form-item>
            <el-button
              type="primary"
              :loading="loading"
              class="login-btn"
              @click="handleLogin"
            >
              {{ loading ? '登录中...' : '登 录' }}
            </el-button>
          </el-form-item>
        </el-form>

        <div class="login-tips">
          <span>演示账号：admin / admin123</span>
        </div>
      </el-card>
    </div>

    <div class="login-footer">
      <span>Copyright &copy; 2024 EduFeeMS. All rights reserved.</span>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const formRef = ref(null)
const loading = ref(false)

const loginForm = reactive({
  username: 'admin',
  password: 'admin123',
  remember: false
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '用户名长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 4, max: 20, message: '密码长度在 4 到 20 个字符', trigger: 'blur' }
  ]
}

async function handleLogin() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await userStore.doLogin({
      username: loginForm.username,
      password: loginForm.password
    })
    ElMessage.success('登录成功')
    const redirectPath = route.query.redirect || '/dashboard'
    router.push(redirectPath)
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '登录失败，请检查用户名和密码')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

/* 背景装饰 */
.login-bg {
  position: absolute;
  inset: 0;
  overflow: hidden;
}

.bg-shape {
  position: absolute;
  border-radius: 50%;
  opacity: 0.1;
  background: #fff;
}

.shape-1 {
  width: 600px;
  height: 600px;
  top: -200px;
  right: -100px;
}

.shape-2 {
  width: 400px;
  height: 400px;
  bottom: -100px;
  left: -50px;
}

.shape-3 {
  width: 200px;
  height: 200px;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
}

/* 登录卡片容器 */
.login-card-wrapper {
  position: relative;
  z-index: 1;
  width: 420px;
}

.login-header {
  text-align: center;
  margin-bottom: 24px;
}

.login-icon {
  width: 72px;
  height: 72px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 16px;
  backdrop-filter: blur(10px);
}

.login-title {
  color: #fff;
  font-size: 32px;
  font-weight: bold;
  letter-spacing: 4px;
  margin: 0;
}

.login-subtitle {
  color: rgba(255, 255, 255, 0.8);
  font-size: 14px;
  margin-top: 8px;
}

.login-card {
  border-radius: 12px;
  overflow: hidden;
}

.login-card :deep(.el-card__body) {
  padding: 32px;
}

.login-form-title {
  text-align: center;
  font-size: 20px;
  color: #303133;
  margin: 0 0 24px;
  font-weight: 600;
}

.form-options {
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.login-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
  letter-spacing: 4px;
}

.login-tips {
  text-align: center;
  margin-top: 16px;
  font-size: 12px;
  color: #909399;
}

/* 底部 */
.login-footer {
  position: absolute;
  bottom: 20px;
  z-index: 1;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.6);
}
</style>
