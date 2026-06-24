<template>
  <div class="attendance-manage">
    <el-card shadow="never" class="tool-card">
      <el-form :model="queryForm" inline>
        <el-form-item label="班级">
          <el-select v-model="queryForm.classId" placeholder="选择班级" style="width: 180px" @change="fetchData">
            <el-option v-for="c in classOptions" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期">
          <el-date-picker
            v-model="queryForm.date"
            type="date"
            placeholder="选择日期"
            value-format="YYYY-MM-DD"
            @change="fetchData"
          />
        </el-form-item>
        <el-form-item label="学员">
          <el-input v-model="queryForm.keyword" placeholder="搜索学员" clearable :prefix-icon="Search" @keyup.enter="fetchData" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="fetchData">查询</el-button>
          <el-button type="success" :icon="Check" @click="handleBatchCheckin">批量签到</el-button>
          <el-button :icon="Download" @click="handleExport">导出</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="table-card">
      <div class="table-header">
        <div class="table-title">
          考勤记录
          <span v-if="queryForm.date" class="date-tip">{{ queryForm.date }}</span>
        </div>
      </div>

      <el-table v-loading="loading" :data="tableData" border stripe style="width: 100%">
        <el-table-column prop="studentName" label="学员" min-width="100" />
        <el-table-column prop="className" label="班级" min-width="140" />
        <el-table-column prop="date" label="日期" min-width="110" />
        <el-table-column prop="status" label="考勤状态" width="110" align="center">
          <template #default="{ row }">
            <el-select
              v-model="row.status"
              size="small"
              style="width: 90px"
              @change="handleStatusChange(row)"
            >
              <el-option label="出勤" value="出勤" />
              <el-option label="迟到" value="迟到" />
              <el-option label="请假" value="请假" />
              <el-option label="缺勤" value="缺勤" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column prop="checkInTime" label="签到时间" min-width="110" />
        <el-table-column prop="remark" label="备注" min-width="130">
          <template #default="{ row }">
            <el-input
              v-model="row.remark"
              size="small"
              placeholder="备注"
              @blur="handleRemarkChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="状态标签" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="attTagType(row.status)" size="small">{{ row.status }}</el-tag>
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

    <!-- 批量签到弹窗 -->
    <el-dialog v-model="batchDialogVisible" title="批量签到" width="600px" destroy-on-close>
      <div class="batch-info">
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="班级">{{ batchClass?.name || '-' }}</el-descriptions-item>
          <el-descriptions-item label="日期">{{ queryForm.date }}</el-descriptions-item>
          <el-descriptions-item label="总人数">{{ batchStudents.length }}</el-descriptions-item>
          <el-descriptions-item label="已签到">{{ batchCheckedCount }}</el-descriptions-item>
        </el-descriptions>
      </div>

      <el-table :data="batchStudents" max-height="350" border size="small" style="margin-top: 16px">
        <el-table-column prop="name" label="学员" />
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-select v-model="row.checkinStatus" size="small" style="width: 90px">
              <el-option label="出勤" value="出勤" />
              <el-option label="迟到" value="迟到" />
              <el-option label="请假" value="请假" />
              <el-option label="缺勤" value="缺勤" />
            </el-select>
          </template>
        </el-table-column>
      </el-table>

      <template #footer>
        <el-button @click="batchDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="batchLoading" @click="handleBatchSubmit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Check, Download } from '@element-plus/icons-vue'
import { getAttendanceList, batchCheckIn, updateAttendance, exportAttendance } from '@/api/attendance'

const loading = ref(false); const batchDialogVisible = ref(false); const batchLoading = ref(false)

const queryForm = reactive({ classId: null, date: new Date().toISOString().slice(0, 10), keyword: '' })
const pagination = reactive({ page: 1, pageSize: 10, total: 0 })
const tableData = ref([])
const classOptions = ref([])
const batchStudents = ref([])
const batchClass = ref(null)

const batchCheckedCount = computed(() => batchStudents.value.filter(s => s.checkinStatus !== '未签到').length)

function attTagType(status) {
  const map = { '出勤': 'success', '迟到': 'warning', '请假': 'info', '缺勤': 'danger' }
  return map[status] || 'info'
}

async function fetchData() {
  loading.value = true
  try {
    const res = await getAttendanceList({
      page: pagination.page, pageSize: pagination.pageSize,
      classId: queryForm.classId, date: queryForm.date, keyword: queryForm.keyword
    })
    tableData.value = res.data?.records || []
    pagination.total = res.data?.total || 0
  } catch { /* ignore */ } finally { loading.value = false }
}

function handlePageChange(p) { pagination.page = p; fetchData() }
function handleSizeChange(s) { pagination.pageSize = s; pagination.page = 1; fetchData() }

async function handleStatusChange(row) {
  try {
    await updateAttendance(row.id, { status: row.status })
    ElMessage.success('考勤状态已更新')
  } catch { /* ignore */ }
}

async function handleRemarkChange(row) {
  try {
    await updateAttendance(row.id, { remark: row.remark })
  } catch { /* ignore */ }
}

function handleBatchCheckin() {
  if (!queryForm.classId) { ElMessage.warning('请先选择班级'); return }
  batchClass.value = classOptions.value.find(c => c.id === queryForm.classId)
  // 模拟获取班级学员
  batchStudents.value = [
    { id: 1, name: '张三', checkinStatus: '出勤' },
    { id: 2, name: '李四', checkinStatus: '出勤' },
    { id: 3, name: '王五', checkinStatus: '出勤' },
    { id: 4, name: '赵六', checkinStatus: '出勤' },
    { id: 5, name: '孙七', checkinStatus: '出勤' }
  ]
  batchDialogVisible.value = true
}

async function handleBatchSubmit() {
  const records = batchStudents.value.map(s => ({
    studentId: s.id,
    status: s.checkinStatus,
    classId: queryForm.classId,
    date: queryForm.date
  }))
  batchLoading.value = true
  try {
    await batchCheckIn({ classId: queryForm.classId, date: queryForm.date, records })
    ElMessage.success('批量签到成功')
    batchDialogVisible.value = false
    fetchData()
  } catch { /* ignore */ } finally { batchLoading.value = false }
}

async function handleExport() {
  try {
    const res = await exportAttendance({ classId: queryForm.classId, date: queryForm.date })
    const blob = new Blob([res], { type: 'application/vnd.ms-excel' })
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `考勤记录_${queryForm.date || 'all'}.xlsx`
    link.click()
    URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch { ElMessage.error('导出失败') }
}

onMounted(() => {
  fetchData()
  classOptions.value = [
    { id: 1, name: '编程一班' }, { id: 2, name: '编程二班' },
    { id: 3, name: '英语A班' }, { id: 4, name: '数学提高班' }
  ]
})
</script>

<style scoped>
.attendance-manage { max-width: 1400px; margin: 0 auto; }
.tool-card { margin-bottom: 16px; border-radius: 8px; }
.table-card { border-radius: 8px; }
.table-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.table-title { font-size: 16px; font-weight: 600; }
.date-tip { font-size: 13px; font-weight: normal; color: #909399; margin-left: 12px; }
.pagination-wrapper { display: flex; justify-content: flex-end; margin-top: 16px; }
.batch-info { margin-bottom: 8px; }
</style>
