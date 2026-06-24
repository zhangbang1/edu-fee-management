<template>
  <div class="student-detail">
    <!-- 基本信息卡片 -->
    <el-card shadow="never" class="info-card">
      <template #header>
        <div class="card-header">
          <div class="card-title">
            <el-button type="default" :icon="ArrowLeft" @click="goBack">返回</el-button>
            <span class="title-text">学员档案</span>
          </div>
          <div class="card-actions">
            <el-button type="primary" :icon="Edit" @click="handleEdit">编辑档案</el-button>
          </div>
        </div>
      </template>

      <el-descriptions :column="3" border size="default" v-loading="loading">
        <el-descriptions-item label="姓名">{{ student.name || '-' }}</el-descriptions-item>
        <el-descriptions-item label="性别">
          {{ student.gender === 'male' ? '男' : student.gender === 'female' ? '女' : '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="年龄">{{ student.age || '-' }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ student.phone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="电子邮箱">{{ student.email || '-' }}</el-descriptions-item>
        <el-descriptions-item label="生日">{{ student.birthday || '-' }}</el-descriptions-item>
        <el-descriptions-item label="主修课程">{{ student.courseName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="所在班级">{{ student.className || '-' }}</el-descriptions-item>
        <el-descriptions-item label="学员状态">
          <el-tag :type="statusTagType(student.status)" size="small">
            {{ statusLabel(student.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="报名日期">{{ student.enrollDate || '-' }}</el-descriptions-item>
        <el-descriptions-item label="累计缴费">
          <span class="money-highlight">{{ formatMoney(student.totalFee) }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="联系地址" :span="3">{{ student.address || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 缴费历史 -->
    <el-card shadow="never" class="section-card">
      <template #header>
        <div class="card-header">
          <span class="section-title">缴费历史</span>
          <el-button type="primary" size="small" :icon="Plus" @click="handleNewFee">
            新增缴费
          </el-button>
        </div>
      </template>

      <el-table :data="feeHistory" border stripe size="small" v-loading="feeLoading">
        <el-table-column type="index" label="序号" width="55" align="center" />
        <el-table-column prop="feeType" label="费用类型" min-width="100" />
        <el-table-column prop="amount" label="金额" min-width="100" align="right">
          <template #default="{ row }">
            <span class="money-text">{{ formatMoney(row.amount) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="paymentMethod" label="支付方式" min-width="100" />
        <el-table-column prop="payTime" label="缴费时间" min-width="140" />
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 'paid' ? 'success' : 'warning'" size="small">
              {{ row.status === 'paid' ? '已缴费' : '待缴费' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 考勤记录 -->
    <el-card shadow="never" class="section-card">
      <template #header>
        <span class="section-title">最近考勤记录</span>
      </template>

      <el-table :data="attendanceRecords" border stripe size="small" v-loading="attLoading">
        <el-table-column type="index" label="序号" width="55" align="center" />
        <el-table-column prop="date" label="日期" min-width="110" />
        <el-table-column prop="className" label="班级" min-width="120" />
        <el-table-column prop="status" label="考勤状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="attTagType(row.status)" size="small">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="150" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Edit, Plus } from '@element-plus/icons-vue'
import { getStudentDetail, getStudentFeeHistory, getStudentAttendance } from '@/api/student'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const feeLoading = ref(false)
const attLoading = ref(false)

const student = reactive({
  id: null, name: '', gender: '', age: '', phone: '', email: '',
  birthday: '', courseName: '', className: '', status: '',
  enrollDate: '', totalFee: 0, address: '', remark: ''
})

const feeHistory = ref([])
const attendanceRecords = ref([])

function statusTagType(status) {
  const map = { active: 'success', graduated: 'info', paused: 'warning', dropped: 'danger' }
  return map[status] || 'info'
}

function statusLabel(status) {
  const map = { active: '在读', graduated: '结业', paused: '停课', dropped: '退学' }
  return map[status] || status
}

function attTagType(status) {
  const map = { '出勤': 'success', '迟到': 'warning', '请假': 'info', '缺勤': 'danger' }
  return map[status] || 'info'
}

function formatMoney(value) {
  return '¥' + (value || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2 })
}

function goBack() {
  router.push('/students')
}

function handleEdit() {
  ElMessage.info('编辑功能开发中')
}

function handleNewFee() {
  router.push(`/fees/payment?studentId=${student.id}`)
}

async function fetchStudentDetail(id) {
  loading.value = true
  try {
    const res = await getStudentDetail(id)
    Object.assign(student, res.data)
  } catch {
    // 错误已在拦截器中处理
  } finally {
    loading.value = false
  }
}

async function fetchFeeHistory(id) {
  feeLoading.value = true
  try {
    const res = await getStudentFeeHistory(id)
    feeHistory.value = res.data || []
  } catch {
    // ignore
  } finally {
    feeLoading.value = false
  }
}

async function fetchAttendance(id) {
  attLoading.value = true
  try {
    const res = await getStudentAttendance(id, { page: 1, pageSize: 10 })
    attendanceRecords.value = res.data?.records || []
  } catch {
    // ignore
  } finally {
    attLoading.value = false
  }
}

onMounted(() => {
  const id = route.params.id
  if (id) {
    fetchStudentDetail(id)
    fetchFeeHistory(id)
    fetchAttendance(id)
  }
})
</script>

<style scoped>
.student-detail {
  max-width: 1200px;
  margin: 0 auto;
}

.info-card {
  border-radius: 8px;
  margin-bottom: 16px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  display: flex;
  align-items: center;
  gap: 12px;
}

.title-text {
  font-size: 18px;
  font-weight: 600;
}

.section-card {
  border-radius: 8px;
  margin-bottom: 16px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
}

.money-highlight {
  font-size: 18px;
  font-weight: bold;
  color: #E6A23C;
}

.money-text {
  font-weight: 600;
  color: #E6A23C;
}
</style>
