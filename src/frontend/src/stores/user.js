import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login, logout, getUserInfo } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  // State
  const token = ref('')
  const userInfo = ref(null)
  const role = ref('')
  const permissions = ref([])

  // Getters
  const isLoggedIn = computed(() => !!token.value)
  const userName = computed(() => userInfo.value?.realName || userInfo.value?.username || '未登录')
  const avatar = computed(() => userInfo.value?.avatar || '')

  // Actions
  async function doLogin(loginForm) {
    const res = await login(loginForm)
    token.value = res.data.accessToken
    role.value = res.data.role
    permissions.value = res.data.permissions || []
    await fetchUserInfo()
  }

  async function fetchUserInfo() {
    try {
      const res = await getUserInfo()
      userInfo.value = res.data
      role.value = res.data.role || role.value
    } catch (error) {
      console.error('获取用户信息失败:', error)
    }
  }

  async function doLogout() {
    try {
      await logout()
    } catch (e) {
      // ignore
    }
    token.value = ''
    userInfo.value = null
    role.value = ''
    permissions.value = []
  }

  function hasPermission(perm) {
    if (role.value === 'admin') return true
    return permissions.value.includes(perm)
  }

  function $reset() {
    token.value = ''
    userInfo.value = null
    role.value = ''
    permissions.value = []
  }

  return {
    token,
    userInfo,
    role,
    permissions,
    isLoggedIn,
    userName,
    avatar,
    doLogin,
    fetchUserInfo,
    doLogout,
    hasPermission,
    $reset
  }
}, {
  persist: {
    key: 'edufeems-user',
    storage: localStorage,
    pick: ['token', 'role', 'permissions']
  }
})
