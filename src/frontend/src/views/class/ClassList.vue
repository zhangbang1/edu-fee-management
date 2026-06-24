<template>
  <div class="class-list">
    <el-card shadow="never" class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="班级名称">
          <el-input v-model="searchForm.keyword" placeholder="请输入班级名称" clearable :prefix-icon="Search" @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="所属课程">
          <el-select v-model="searchForm.courseId" placeholder="选择课程" clearable style="width: 180px">
            <el-option v-for="c in courseOptions" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="选择状态" clearable style="width: 130px">
            <el-option label="招生中" value="open" />
            <el-option label="已开课" value="active" />
            <el-option label="已结课" value="closed" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="RefreshRight" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="table-card">
      <div class="table-header">
        <div class="table-title">班级列表</div>
        <div class="table-actions">
          <el-button type="primary" :icon="Plus" @click="handleAdd">新增班级</el-button>
        </div>
      </div>

      <el-table v-loading="loading" :data="tableData" border stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="60" align="center" />
        <el-table-column prop="name" label="班级名称" min-width="150" />
        <el-table-column prop="courseName" label="所属课程" min-width="140" />
        <el-table-column prop="teacherName" label="授课老师" min-width="100" />
        <el-table-column prop="studentCount" label="学员人数" width="90" align="center" />
        <el-table-column prop="maxStudents" label="最大人数" width="90" align="center" />
        <el-table-column prop="startDate" label="开班日期" min-width="110" />
        <el-table-column prop="schedule" label="上课时间" min-width="130" />
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="primary" link size="small" @click="handleViewStudents(row)">学员</el-button>
            <el-popconfirm title="确定删除该班级吗？" @confirm="handleDelete(row)">
              <template #reference>
                <el-button type="danger" link size="small">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.page" v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50]" :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper" background
          @size-change="handleSizeChange" @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="550px" :close-on-click-modal="false" destroy-on-close>
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
        <el-form-item label="班级名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入班级名称" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="所属课程" prop="courseId">
              <el-select v-model="formData.courseId" placeholder="选择课程" style="width: 100%">
                <el-option v-for="c in courseOptions" :key="c.id" :label="c.name" :value="c.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="授课老师" prop="teacherName">
              <el-input v-model="formData.teacherName" placeholder="请输入老师姓名" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="最大人数" prop="maxStudents">
              <el-input-number v-model="formData.maxStudents" :min="1" :max="100" style="width: 100%" controls-position="right" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="班容状态">
              <div class="class-capacity" :class="{ full: capacityRatio >= 1, warning: capacityRatio >= 0.8 && capacityRatio < 1 }">
                {{ formData.studentCount || 0 }} / {{ formData.maxStudents || 0 }}
                <span v-if="capacityRatio >= 1">(已满)</span>
                <span v-else-if="capacityRatio >= 0.8">(将满)</span>
              </div>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="开班日期" prop="startDate">
              <el-date-picker v-model="formData.startDate" type="date" placeholder="选择日期" style="width: 100%" value-format="YYYY-MM-DD" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="上课时间" prop="schedule">
              <el-input v-model="formData.schedule" placeholder="例如：周六 09:00-11:00" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="状态">
          <el-radio-group v-model="formData.status">
            <el-radio value="open">招生中</el-radio>
            <el-radio value="active">已开课</el-radio>
            <el-radio value="closed">已结课</el-radio>
          </el-radio-group>
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
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, RefreshRight, Plus } from '@element-plus/icons-vue'
import { getClassList, createClass, updateClass, deleteClass, getClassStudents } from '@/api/class'

const loading = ref(false); const dialogVisible = ref(false)
const dialogTitle = ref('新增班级'); const submitLoading = ref(false)
const formRef = ref(null); const isEdit = ref(false); const currentId = ref(null)

const searchForm = reactive({ keyword: '', courseId: '', status: '' })
const pagination = reactive({ page: 1, pageSize: 10, total: 0 })
const tableData = ref([])
const courseOptions = ref([])

const formData = reactive({ name: '', courseId: null, teacherName: '', maxStudents: 30, studentCount: 0, startDate: '', schedule: '', status: 'open' })
const formRules = { name: [{ required: true, message: '请输入班级名称', trigger: 'blur' }], courseId: [{ required: true, message: '请选择所属课程', trigger: 'change' }], maxStudents: [{ required: true, message: '请输入最大人数', trigger: 'blur' }] }

const capacityRatio = computed(() => (formData.studentCount || 0) / (formData.maxStudents || 1))

function statusTagType(s) { const m = { open: 'primary', active: 'success', closed: 'info' }; return m[s] || 'info' }
function statusLabel(s) { const m = { open: '招生中', active: '已开课', closed: '已结课' }; return m[s] || s }

async function fetchData() {
  loading.value = true
  try {
    const res = await getClassList({ page: pagination.page, pageSize: pagination.pageSize, ...searchForm })
    tableData.value = res.data?.records || []
    pagination.total = res.data?.total || 0
  } catch { /* ignore */ } finally { loading.value = false }
}

function handleSearch() { pagination.page = 1; fetchData() }
function handleReset() { searchForm.keyword = ''; searchForm.courseId = ''; searchForm.status = ''; pagination.page = 1; fetchData() }
function handlePageChange(p) { pagination.page = p; fetchData() }
function handleSizeChange(s) { pagination.pageSize = s; pagination.page = 1; fetchData() }

function handleAdd() {
  isEdit.value = false; currentId.value = null; dialogTitle.value = '新增班级'
  Object.assign(formData, { name: '', courseId: null, teacherName: '', maxStudents: 30, studentCount: 0, startDate: '', schedule: '', status: 'open' })
  dialogVisible.value = true
}
function handleEdit(row) {
  isEdit.value = true; currentId.value = row.id; dialogTitle.value = '编辑班级'
  Object.assign(formData, {
    name: row.name || '', courseId: row.courseId || null, teacherName: row.teacherName || '',
    maxStudents: row.maxStudents || 30, studentCount: row.studentCount || 0,
    startDate: row.startDate || '', schedule: row.schedule || '', status: row.status || 'open'
  })
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    if (isEdit.value) { await updateClass(currentId.value, formData); ElMessage.success('班级更新成功') }
    else { await createClass(formData); ElMessage.success('班级新增成功') }
    dialogVisible.value = false; fetchData()
  } catch { /* ignore */ } finally { submitLoading.value = false }
}

async function handleDelete(row) { try { await deleteClass(row.id); ElMessage.success('删除成功'); fetchData() } catch { /* ignore */ } }

function handleViewStudents(row) { ElMessage.info(`查看班级"${row.name}"的学员列表 - 功能开发中`) }

onMounted(() => {
  fetchData()
  courseOptions.value = [
    { id: 1, name: '少儿编程基础' }, { id: 2, name: 'Python进阶' },
    { id: 3, name: '英语口语强化' }, { id: 4, name: '数学思维训练' }
  ]
})
</script>

<style scoped>
.class-list { max-width: 1400px; margin: 0 auto; }
.search-card { margin-bottom: 16px; border-radius: 8px; }
.table-card { border-radius: 8px; }
.table-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.table-title { font-size: 16px; font-weight: 600; color: #303133; }
.table-actions { display: flex; gap: 8px; }
.pagination-wrapper { display: flex; justify-content: flex-end; margin-top: 16px; }
.class-capacity { font-size: 14px; color: #67C23A; }
.class-capacity.warning { color: #E6A23C; }
.class-capacity.full { color: #F56C6C; }
</style>
