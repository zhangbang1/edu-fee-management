<template>
  <el-container class="main-container">
    <!-- 侧边栏 -->
    <el-aside :width="isCollapsed ? '64px' : '220px'" class="sidebar">
      <div class="logo-container" @click="goHome">
        <el-icon :size="28" color="#409EFF"><School /></el-icon>
        <span v-show="!isCollapsed" class="logo-text">EduFeeMS</span>
      </div>

      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapsed"
        :collapse-transition="false"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
        router
        class="sidebar-menu"
      >
        <el-menu-item index="/dashboard">
          <el-icon><Odometer /></el-icon>
          <template #title>仪表盘</template>
        </el-menu-item>

        <el-menu-item index="/students">
          <el-icon><User /></el-icon>
          <template #title>学员管理</template>
        </el-menu-item>

        <el-menu-item index="/courses">
          <el-icon><Notebook /></el-icon>
          <template #title>课程管理</template>
        </el-menu-item>

        <el-menu-item index="/classes">
          <el-icon><Grid /></el-icon>
          <template #title>班级管理</template>
        </el-menu-item>

        <el-sub-menu index="fee-group">
          <template #title>
            <el-icon><Money /></el-icon>
            <span>收费管理</span>
          </template>
          <el-menu-item index="/fees/payment">收费操作</el-menu-item>
          <el-menu-item index="/fees/records">收费台账</el-menu-item>
        </el-sub-menu>

        <el-menu-item index="/attendance">
          <el-icon><Calendar /></el-icon>
          <template #title>考勤管理</template>
        </el-menu-item>

        <el-menu-item
          v-if="canAccessReport"
          index="/reports/finance"
        >
          <el-icon><TrendCharts /></el-icon>
          <template #title>财务报表</template>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <!-- 右侧主体 -->
    <el-container class="right-container">
      <!-- 顶部导航栏 -->
      <el-header class="top-header">
        <div class="header-left">
          <el-icon
            class="collapse-btn"
            :size="22"
            @click="toggleSidebar"
          >
            <Fold v-if="!isCollapsed" />
            <Expand v-else />
          </el-icon>
          <el-breadcrumb separator="/" class="breadcrumb">
            <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="currentTitle">{{ currentTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <div class="header-right">
          <el-badge :value="3" :max="99" class="notice-badge">
            <el-icon :size="20"><Bell /></el-icon>
          </el-badge>

          <el-dropdown @command="handleUserCommand" trigger="click">
            <div class="user-info">
              <el-avatar :size="32" :src="userStore.avatar">
                <el-icon :size="18"><UserFilled /></el-icon>
              </el-avatar>
              <span class="user-name">{{ userStore.userName }}</span>
              <el-icon class="dropdown-icon"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>
                  <span>个人信息</span>
                </el-dropdown-item>
                <el-dropdown-item command="changePassword">
                  <el-icon><Lock /></el-icon>
                  <span>修改密码</span>
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>
                  <span>退出登录</span>
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 内容区域 -->
      <el-main class="main-content">
        <router-view v-slot="{ Component }">
          <transition name="fade-transform" mode="out-in">
            <keep-alive :include="cachedViews">
              <component :is="Component" />
            </keep-alive>
          </transition>
        </router-view>
      </el-main>

      <!-- 底部 -->
      <el-footer class="app-footer">
        <span>&copy; 2024 EduFeeMS - 教育培训机构教务收费管理系统 v1.0</span>
      </el-footer>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useAppStore } from '@/stores/app'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const appStore = useAppStore()

const isCollapsed = computed(() => appStore.sidebarCollapsed)
const activeMenu = computed(() => route.path)
const currentTitle = computed(() => route.meta.title || '')
const canAccessReport = computed(() => {
  return userStore.role === 'admin' || userStore.role === 'finance'
})

// 需要缓存的页面组件名称
const cachedViews = ['StudentList', 'CourseList', 'ClassList', 'FeeRecords']

function toggleSidebar() {
  appStore.toggleSidebar()
}

function goHome() {
  router.push('/dashboard')
}

function handleUserCommand(command) {
  switch (command) {
    case 'profile':
      ElMessage.info('个人信息功能开发中')
      break
    case 'changePassword':
      showChangePasswordDialog()
      break
    case 'logout':
      handleLogout()
      break
  }
}

async function handleLogout() {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await userStore.doLogout()
    router.push('/login')
    ElMessage.success('已退出登录')
  } catch {
    // 用户取消
  }
}

function showChangePasswordDialog() {
  ElMessage.info('修改密码功能开发中')
}
</script>

<script>
import { ElMessage, ElMessageBox } from 'element-plus'
export default {}
</script>

<style scoped>
.main-container {
  height: 100vh;
  overflow: hidden;
}

/* 侧边栏 */
.sidebar {
  background-color: #304156;
  overflow: hidden;
  transition: width 0.3s ease;
  display: flex;
  flex-direction: column;
}

.logo-container {
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  cursor: pointer;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  user-select: none;
}

.logo-text {
  color: #fff;
  font-size: 18px;
  font-weight: bold;
  letter-spacing: 2px;
  white-space: nowrap;
}

.sidebar-menu {
  border-right: none;
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
}

.sidebar-menu:not(.el-menu--collapse) {
  width: 220px;
}

.el-menu--collapse {
  width: 64px;
}

/* 右侧主体 */
.right-container {
  flex-direction: column;
  overflow: hidden;
}

/* 顶部导航 */
.top-header {
  height: 56px !important;
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
  z-index: 10;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.collapse-btn {
  cursor: pointer;
  color: #606266;
  transition: color 0.2s;
}

.collapse-btn:hover {
  color: #409EFF;
}

.breadcrumb {
  font-size: 14px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.notice-badge {
  cursor: pointer;
  color: #606266;
}

.notice-badge:hover {
  color: #409EFF;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
  transition: background-color 0.2s;
}

.user-info:hover {
  background-color: #f5f7fa;
}

.user-name {
  font-size: 14px;
  color: #303133;
}

.dropdown-icon {
  color: #909399;
  font-size: 12px;
}

/* 主内容区 */
.main-content {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background-color: #f0f2f5;
}

/* 底部 */
.app-footer {
  height: 40px !important;
  background: #fff;
  border-top: 1px solid #e4e7ed;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  color: #909399;
}

/* 页面切换动画 */
.fade-transform-enter-active,
.fade-transform-leave-active {
  transition: all 0.3s ease;
}

.fade-transform-enter-from {
  opacity: 0;
  transform: translateX(-20px);
}

.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(20px);
}
</style>
