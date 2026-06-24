import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAppStore = defineStore('app', () => {
  // 侧边栏折叠状态
  const sidebarCollapsed = ref(false)

  // 面包屑导航
  const breadcrumb = ref([])

  // 全局加载状态
  const loading = ref(false)

  // 系统信息
  const systemInfo = ref({
    version: 'v1.0.0',
    name: 'EduFeeMS',
    fullName: '教育培训机构教务收费管理系统'
  })

  function toggleSidebar() {
    sidebarCollapsed.value = !sidebarCollapsed.value
  }

  function setBreadcrumb(items) {
    breadcrumb.value = items
  }

  function setLoading(val) {
    loading.value = val
  }

  return {
    sidebarCollapsed,
    breadcrumb,
    loading,
    systemInfo,
    toggleSidebar,
    setBreadcrumb,
    setLoading
  }
}, {
  persist: {
    key: 'edufeems-app',
    storage: localStorage,
    pick: ['sidebarCollapsed']
  }
})
