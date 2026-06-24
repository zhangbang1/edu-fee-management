<template>
  <div class="student-list">
    <!-- 搜索栏 -->
    <el-card shadow="never" class="search-card">
      <el-form :model="searchForm" inline class="search-form">
        <el-form-item label="学员姓名">
          <el-input
            v-model="searchForm.keyword"
            placeholder="请输入姓名或手机号"
            clearable
            :prefix-icon="Search"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="选择状态" clearable style="width: 130px">
            <el-option label="在读" value="active" />
            <el-option label="结业" value="graduated" />
            <el-option label="停课" value="paused" />
            <el-option label="退学" value="dropped" />
          </el-select>
        </el-form-item>
        <el-form-item label="课程">
          <el-select v-model="searchForm.courseId" placeholder="选择课程" clearable style="width: 160px">
            <el-option
              v-for="course in courseOptions"
              :key="course.id"
              :label="course.name"
              :value="course.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="RefreshRight" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格卡片 -->
    <el-card shadow="never" class="table-card">
      <div class="table-header">
        <div class="table-title">学员列表</div>
        <div class="table-actions">
          <el-button type="primary" :icon="Plus" @click="handleAdd">新增学员</el-button>
          <el-button :icon="Download" @click="handleExport">导出</el-button>
        </div>
      </div>

      <el-table
        v-loading="loading"
        :data="tableData"
        border
        stripe
        style="width: 100%"
        @sort-change="handleSortChange"
      >
        <el-table-column prop="id" label="ID" width="60" align="center" />
        <el-table-column prop="name" label="姓名" min-width="100" />
        <el-table-column prop="gender" label="性别" width="70" align="center">
          <template #default="{ row }">
            {{ row.gender === 'male' ? '男' : '女' }}
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" min-width="120" />
        <el-table-column prop="courseName" label="主修课程" min-width="130" />
        <el-table-column prop="className" label="所在班级" min-width="130" />
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">
              {{ statusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="enrollDate" label="报名日期" min-width="110" sortable="custom" />
        <el-table-column prop="totalFee" label="累计缴费" min-width="110" align="right">
          <template #default="{ row }">
            <span class="money-text">{{ formatMoney(row.totalFee) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleView(row)">详情</el-button>
            <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
            <el-popconfirm
              title="确定删除该学员吗？"
              confirm-button-text="确定"
              cancel-button-text="取消"
              @confirm="handleDelete(row)"
            >
              <template #reference>
                <el-button type="danger" link size="small">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
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
      width="600px"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="姓名" prop="name">
              <el-input v-model="formData.name" placeholder="请输入姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="性别" prop="gender">
              <el-radio-group v-model="formData.gender">
                <el-radio value="male">男</el-radio>
                <el-radio value="female">女</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="formData.phone" placeholder="请输入手机号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="生日" prop="birthday">
              <el-date-picker
                v-model="formData.birthday"
                type="date"
                placeholder="选择生日"
                style="width: 100%"
                value-format="YYYY-MM-DD"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="电子邮箱" prop="email">
          <el-input v-model="formData.email" placeholder="请输入电子邮箱" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="主修课程" prop="courseId">
              <el-select v-model="formData.courseId" placeholder="选择课程" style="width: 100%">
                <el-option
                  v-for="course in courseOptions"
                  :key="course.id"
                  :label="course.name"
                  :value="course.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所在班级" prop="classId">
              <el-select v-model="formData.classId" placeholder="选择班级" style="width: 100%">
                <el-option
                  v-for="cls in classOptions"
                  :key="cls.id"
                  :label="cls.name"
                  :value="cls.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="联系地址" prop="address">
          <el-input v-model="formData.address" placeholder="请输入联系地址" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="formData.remark"
            type="textarea"
            :rows="3"
            placeholder="备注信息（可选）"
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
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search, RefreshRight, Plus, Download } from '@element-plus/icons-vue'
import {
  getStudentList,
  createStudent,
  updateStudent,
  deleteStudent,
  exportStudents
} from '@/api/student'

const router = useRouter()
const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新增学员')
const submitLoading = ref(false)
const formRef = ref(null)
const isEdit = ref(false)
const currentId = ref(null)

// 搜索表单
const searchForm = reactive({
  keyword: '',
  status: '',
  courseId: ''
})

// 分页
const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

// 表格数据
const tableData = ref([])

// 选项数据
const courseOptions = ref([])
const classOptions = ref([])

// 表单数据
const formData = reactive({
  name: '',
  gender: 'male',
  phone: '',
  birthday: '',
  email: '',
  courseId: null,
  classId: null,
  address: '',
  remark: ''
})

const formRules = {
  name: [{ required: true, message: '请输入学员姓名', trigger: 'blur' }],
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ]
}

function statusTagType(status) {
  const map = { active: 'success', graduated: 'info', paused: 'warning', dropped: 'danger' }
  return map[status] || 'info'
}

function statusLabel(status) {
  const map = { active: '在读', graduated: '结业', paused: '停课', dropped: '退学' }
  return map[status] || status
}

function formatMoney(value) {
  return '¥' + (value || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2 })
}

async function fetchData() {
  loading.value = true
  try {
    const res = await getStudentList({
      page: pagination.page,
      pageSize: pagination.pageSize,
      ...searchForm
    })
    tableData.value = res.data.records
    pagination.total = res.data.total
  } catch {
    // 错误已在拦截器中处理
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.page = 1
  fetchData()
}

function handleReset() {
  searchForm.keyword = ''
  searchForm.status = ''
  searchForm.courseId = ''
  pagination.page = 1
  fetchData()
}

function handlePageChange(page) {
  pagination.page = page
  fetchData()
}

function handleSizeChange(size) {
  pagination.pageSize = size
  pagination.page = 1
  fetchData()
}

function handleSortChange({ prop, order }) {
  console.log('排序:', prop, order)
  // 可扩展排序逻辑
}

function handleAdd() {
  isEdit.value = false
  currentId.value = null
  dialogTitle.value = '新增学员'
  Object.assign(formData, {
    name: '',
    gender: 'male',
    phone: '',
    birthday: '',
    email: '',
    courseId: null,
    classId: null,
    address: '',
    remark: ''
  })
  dialogVisible.value = true
}

function handleEdit(row) {
  isEdit.value = true
  currentId.value = row.id
  dialogTitle.value = '编辑学员'
  Object.assign(formData, {
    name: row.name || '',
    gender: row.gender || 'male',
    phone: row.phone || '',
    birthday: row.birthday || '',
    email: row.email || '',
    courseId: row.courseId || null,
    classId: row.classId || null,
    address: row.address || '',
    remark: row.remark || ''
  })
  dialogVisible.value = true
}

function handleView(row) {
  router.push(`/students/${row.id}`)
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    if (isEdit.value) {
      await updateStudent(currentId.value, formData)
      ElMessage.success('学员信息更新成功')
    } else {
      await createStudent(formData)
      ElMessage.success('学员新增成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch {
    // 错误已在拦截器中处理
  } finally {
    submitLoading.value = false
  }
}

async function handleDelete(row) {
  try {
    await deleteStudent(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch {
    // 错误已在拦截器中处理
  }
}

async function handleExport() {
  try {
    const res = await exportStudents(searchForm)
    const blob = new Blob([res], { type: 'application/vnd.ms-excel' })
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `学员列表_${new Date().toISOString().slice(0, 10)}.xlsx`
    link.click()
    URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch {
    ElMessage.error('导出失败')
  }
}

onMounted(() => {
  fetchData()
  // 模拟课程和班级选项
  courseOptions.value = [
    { id: 1, name: '少儿编程基础' },
    { id: 2, name: 'Python进阶' },
    { id: 3, name: '英语口语强化' },
    { id: 4, name: '数学思维训练' }
  ]
  classOptions.value = [
    { id: 1, name: '编程一班' },
    { id: 2, name: '编程二班' },
    { id: 3, name: '英语A班' },
    { id: 4, name: '数学提高班' }
  ]
})
</script>

<style scoped>
.student-list {
  max-width: 1400px;
  margin: 0 auto;
}

.search-card {
  margin-bottom: 16px;
  border-radius: 8px;
}

.search-form {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.table-card {
  border-radius: 8px;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.table-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.table-actions {
  display: flex;
  gap: 8px;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.money-text {
  font-weight: 600;
  color: #E6A23C;
}
</style>
