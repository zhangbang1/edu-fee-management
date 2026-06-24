import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录', noAuth: true }
  },
  {
    path: '/',
    component: () => import('@/layout/MainLayout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '仪表盘', icon: 'Odometer' }
      },
      {
        path: 'students',
        name: 'StudentList',
        component: () => import('@/views/student/StudentList.vue'),
        meta: { title: '学员管理', icon: 'User' }
      },
      {
        path: 'students/:id',
        name: 'StudentDetail',
        component: () => import('@/views/student/StudentDetail.vue'),
        meta: { title: '学员详情', hidden: true }
      },
      {
        path: 'courses',
        name: 'CourseList',
        component: () => import('@/views/course/CourseList.vue'),
        meta: { title: '课程管理', icon: 'Notebook' }
      },
      {
        path: 'classes',
        name: 'ClassList',
        component: () => import('@/views/class/ClassList.vue'),
        meta: { title: '班级管理', icon: 'Grid' }
      },
      {
        path: 'fees/payment',
        name: 'Payment',
        component: () => import('@/views/fee/Payment.vue'),
        meta: { title: '收费操作', icon: 'Money' }
      },
      {
        path: 'fees/records',
        name: 'FeeRecords',
        component: () => import('@/views/fee/FeeRecords.vue'),
        meta: { title: '收费台账', icon: 'Tickets' }
      },
      {
        path: 'attendance',
        name: 'AttendanceManage',
        component: () => import('@/views/attendance/AttendanceManage.vue'),
        meta: { title: '考勤管理', icon: 'Calendar' }
      },
      {
        path: 'reports/finance',
        name: 'FinanceReport',
        component: () => import('@/views/report/FinanceReport.vue'),
        meta: { title: '财务报表', icon: 'TrendCharts', roles: ['admin', 'finance'] }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/dashboard'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 权限守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  const token = userStore.token

  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - EduFeeMS` : 'EduFeeMS'

  // 不需要认证的页面直接放行
  if (to.meta.noAuth) {
    if (token && to.path === '/login') {
      next({ path: '/dashboard' })
    } else {
      next()
    }
    return
  }

  // 需要认证但未登录
  if (!token) {
    next({ path: '/login', query: { redirect: to.fullPath } })
    return
  }

  // 角色权限检查
  const requiredRoles = to.meta.roles
  if (requiredRoles && Array.isArray(requiredRoles)) {
    const hasRole = requiredRoles.includes(userStore.role)
    if (!hasRole) {
      next({ path: '/dashboard' })
      return
    }
  }

  next()
})

export default router
