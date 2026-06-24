<template>
  <div class="course-list">
    <!-- 搜索栏 -->
    <el-card shadow="never" class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="课程名称">
          <el-input
            v-model="searchForm.keyword"
            placeholder="请输入课程名称"
            clearable
            :prefix-icon="Search"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="课程分类">
          <el-select v-model="searchForm.category" placeholder="选择分类" clearable style="width: 150px">
            <el-option label="编程开发" value="programming" />
            <el-option label="语言培训" value="language" />
            <el-option label="学科辅导" value="academic" />
            <el-option label="艺术素养" value="art" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="选择状态" clearable style="width: 120px">
            <el-option label="启用" value="active" />
            <el-option label="停用" value="inactive" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="RefreshRight" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 课程表格 -->
    <el-card shadow="never" class="table-card">
      <div class="table-header">
        <div class="table-title">课程列表</div>
        <div class="table-actions">
          <el-button type="primary" :icon="Plus" @click="handleAdd">新增课程</el-button>
        </div>
      </div>

      <el-table v-loading="loading" :data="tableData" border stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="60" align="center" />
        <el-table-column prop="name" label="课程名称" min-width="150" />
        <el-table-column prop="categoryName" label="分类" width="100" align="center" />
        <el-table-column prop="price" label="学费标准" min-width="110" align="right">
          <template #default="{ row }">
            <span class="price-text">{{ formatMoney(row.price) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="totalHours" label="总课时" width="80" align="center" />
        <el-table-column prop="className" label="上课班级" min-width="130" />
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 'active' ? 'success' : 'info'" size="small">
              {{ row.status === 'active' ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button
              :type="row.status === 'active' ? 'warning' : 'success'"
              link
              size="small"
              @click="handleToggleStatus(row)"
            >
              {{ row.status === 'active' ? '停用' : '启用' }}
            </el-button>
            <el-popconfirm
              title="确定删除该课程吗？"
              @confirm="handleDelete(row)"
            >
              <template #reference>
                <el-button type="danger" link size="small">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="550px"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
        <el-form-item label="课程名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入课程名称" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="课程分类" prop="category">
              <el-select v-model="formData.category" placeholder="选择分类" style="width: 100%">
                <el-option label="编程开发" value="programming" />
                <el-option label="语言培训" value="language" />
                <el-option label="学科辅导" value="academic" />
                <el-option label="艺术素养" value="art" />
                <el-option label="其他" value="other" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="学费标准" prop="price">
              <el-input-number
                v-model="formData.price"
                :min="0"
                :precision="2"
                :step="100"
                placeholder="请输入学费"
                style="width: 100%"
                controls-position="right"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="总课时" prop="totalHours">
              <el-input-number
                v-model="formData.totalHours"
                :min="1"
                :step="1"
                placeholder="请输入总课时"
                style="width: 100%"
                controls-position="right"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-switch
                v-model="formData.status"
                active-value="active"
                inactive-value="inactive"
                active-text="启用"
                inactive-text="停用"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="课程描述">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="3"
            placeholder="课程描述（可选）"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, RefreshRight, Plus } from '@element-plus/icons-vue'
import { getCourseList, createCourse, updateCourse, deleteCourse } from '@/api/course'

const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新增课程')
const submitLoading = ref(false)
const formRef = ref(null)
const isEdit = ref(false)
const currentId = ref(null)

const searchForm = reactive({ keyword: '', category: '', status: '' })
const pagination = reactive({ page: 1, pageSize: 10, total: 0 })
const tableData = ref([])

const formData = reactive({
  name: '',
  category: '',
  price: 0,
  totalHours: 30,
  status: 'active',
  description: ''
})

const formRules = {
  name: [{ required: true, message: '请输入课程名称', trigger: 'blur' }],
  category: [{ required: true, message: '请选择课程分类', trigger: 'change' }],
  price: [{ required: true, message: '请输入学费标准', trigger: 'blur' }],
  totalHours: [{ required: true, message: '请输入总课时', trigger: 'blur' }]
}

const categoryMap = {
  programming: '编程开发',
  language: '语言培训',
  academic: '学科辅导',
  art: '艺术素养',
  other: '其他'
}

function formatMoney(value) {
  return '¥' + (value || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2 })
}

async function fetchData() {
  loading.value = true
  try {
    const res = await getCourseList({ page: pagination.page, pageSize: pagination.pageSize, ...searchForm })
    tableData.value = (res.data?.records || []).map(item => ({
      ...item,
      categoryName: categoryMap[item.category] || item.category
    }))
    pagination.total = res.data?.total || 0
  } catch {
    // ignore
  } finally {
    loading.value = false
  }
}

function handleSearch() { pagination.page = 1; fetchData() }
function handleReset() { searchForm.keyword = ''; searchForm.category = ''; searchForm.status = ''; pagination.page = 1; fetchData() }
function handlePageChange(page) { pagination.page = page; fetchData() }
function handleSizeChange(size) { pagination.pageSize = size; pagination.page = 1; fetchData() }

function handleAdd() {
  isEdit.value = false; currentId.value = null; dialogTitle.value = '新增课程'
  Object.assign(formData, { name: '', category: '', price: 0, totalHours: 30, status: 'active', description: '' })
  dialogVisible.value = true
}

function handleEdit(row) {
  isEdit.value = true; currentId.value = row.id; dialogTitle.value = '编辑课程'
  Object.assign(formData, {
    name: row.name || '', category: row.category || '', price: row.price || 0,
    totalHours: row.totalHours || 30, status: row.status || 'active', description: row.description || ''
  })
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    if (isEdit.value) {
      await updateCourse(currentId.value, formData)
      ElMessage.success('课程更新成功')
    } else {
      await createCourse(formData)
      ElMessage.success('课程新增成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch { /* ignore */ } finally { submitLoading.value = false }
}

async function handleDelete(row) {
  try {
    await deleteCourse(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch { /* ignore */ }
}

async function handleToggleStatus(row) {
  const newStatus = row.status === 'active' ? 'inactive' : 'active'
  try {
    await updateCourse(row.id, { status: newStatus })
    ElMessage.success(newStatus === 'active' ? '已启用' : '已停用')
    fetchData()
  } catch { /* ignore */ }
}

onMounted(() => { fetchData() })
</script>

<style scoped>
.course-list { max-width: 1400px; margin: 0 auto; }
.search-card { margin-bottom: 16px; border-radius: 8px; }
.table-card { border-radius: 8px; }
.table-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.table-title { font-size: 16px; font-weight: 600; color: #303133; }
.table-actions { display: flex; gap: 8px; }
.pagination-wrapper { display: flex; justify-content: flex-end; margin-top: 16px; }
.price-text { font-weight: 600; color: #E6A23C; }
</style>
