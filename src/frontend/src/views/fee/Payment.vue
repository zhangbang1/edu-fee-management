<template>
  <div class="payment-page">
    <el-row :gutter="20">
      <!-- 左侧：学员信息 & 费用明细 -->
      <el-col :xs="24" :lg="16">
        <el-card shadow="never" class="section-card">
          <template #header><span class="section-title">学员信息</span></template>
          <div class="student-search">
            <el-select
              v-model="selectedStudentId"
              filterable
              remote
              reserve-keyword
              placeholder="搜索学员（输入姓名或手机号）"
              :remote-method="searchStudents"
              :loading="studentSearchLoading"
              size="large"
              style="width: 100%"
              @change="handleStudentChange"
            >
              <el-option
                v-for="s in studentOptions"
                :key="s.id"
                :label="`${s.name} - ${s.phone}`"
                :value="s.id"
              />
            </el-select>
          </div>

          <div v-if="selectedStudent" class="student-detail">
            <el-descriptions :column="3" border size="small">
              <el-descriptions-item label="姓名">{{ selectedStudent.name }}</el-descriptions-item>
              <el-descriptions-item label="手机号">{{ selectedStudent.phone }}</el-descriptions-item>
              <el-descriptions-item label="主修课程">{{ selectedStudent.courseName || '-' }}</el-descriptions-item>
              <el-descriptions-item label="所在班级">{{ selectedStudent.className || '-' }}</el-descriptions-item>
              <el-descriptions-item label="累计缴费">
                <span class="money-highlight">{{ formatMoney(selectedStudent.totalFee) }}</span>
              </el-descriptions-item>
              <el-descriptions-item label="状态">
                <el-tag :type="selectedStudent.status === 'active' ? 'success' : 'warning'" size="small">
                  {{ selectedStudent.status === 'active' ? '在读' : '非在读' }}
                </el-tag>
              </el-descriptions-item>
            </el-descriptions>
          </div>
        </el-card>

        <!-- 收费项目明细 -->
        <el-card shadow="never" class="section-card">
          <template #header>
            <div class="card-header-row">
              <span class="section-title">收费项目</span>
              <el-button type="primary" size="small" :icon="Plus" @click="addFeeItem">添加项目</el-button>
            </div>
          </template>

          <el-table :data="feeItems" border stripe size="small">
            <el-table-column type="index" label="序号" width="55" align="center" />
            <el-table-column prop="feeType" label="费用类型" min-width="130">
              <template #default="{ row }">
                <el-select v-model="row.feeType" placeholder="选择费用类型" size="small" style="width: 100%">
                  <el-option label="学费" value="学费" />
                  <el-option label="教材费" value="教材费" />
                  <el-option label="报名费" value="报名费" />
                  <el-option label="活动费" value="活动费" />
                  <el-option label="补考费" value="补考费" />
                  <el-option label="其他" value="其他" />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column prop="amount" label="金额" min-width="120" align="right">
              <template #default="{ row }">
                <el-input-number
                  v-model="row.amount"
                  :min="0"
                  :precision="2"
                  size="small"
                  style="width: 100%"
                  controls-position="right"
                  @change="calculateTotal"
                />
              </template>
            </el-table-column>
            <el-table-column prop="remark" label="备注" min-width="150">
              <template #default="{ row }">
                <el-input v-model="row.remark" size="small" placeholder="备注（可选）" />
              </template>
            </el-table-column>
            <el-table-column label="操作" width="70" align="center">
              <template #default="{ $index }">
                <el-button type="danger" :icon="Delete" size="small" circle @click="removeFeeItem($index)" />
              </template>
            </el-table-column>
          </el-table>

          <div v-if="feeItems.length === 0" class="empty-hint">暂无收费项目，请点击"添加项目"</div>
        </el-card>
      </el-col>

      <!-- 右侧：费用汇总 & 收据 -->
      <el-col :xs="24" :lg="8">
        <el-card shadow="never" class="section-card receipt-card">
          <template #header>
            <div class="receipt-header">
              <el-icon :size="20" color="#409EFF"><Tickets /></el-icon>
              <span class="section-title">收据预览</span>
            </div>
          </template>

          <div class="receipt-body">
            <div class="receipt-org">EduFeeMS</div>
            <div class="receipt-title">收费收据</div>

            <el-divider />

            <div class="receipt-row">
              <span>学员：</span>
              <span class="receipt-value">{{ selectedStudent?.name || '-' }}</span>
            </div>
            <div class="receipt-row">
              <span>日期：</span>
              <span class="receipt-value">{{ today }}</span>
            </div>

            <el-divider />

            <div v-for="(item, idx) in feeItems" :key="idx" class="receipt-row">
              <span>{{ item.feeType || '未选择' }}</span>
              <span class="receipt-value">{{ formatMoney(item.amount) }}</span>
            </div>

            <el-divider />

            <div class="receipt-row">
              <span>小计：</span>
              <span class="receipt-value total">{{ formatMoney(subTotal) }}</span>
            </div>
            <div class="receipt-row">
              <span>优惠：</span>
              <el-input-number
                v-model="discountAmount"
                :min="0"
                :max="subTotal"
                :precision="2"
                size="small"
                style="width: 140px"
                controls-position="right"
                @change="calculateTotal"
              />
            </div>
            <div class="receipt-row grand-total">
              <span>实收金额：</span>
              <span class="receipt-value grand">{{ formatMoney(grandTotal) }}</span>
            </div>

            <el-divider />

            <div class="receipt-row">
              <span>支付方式：</span>
              <div class="payment-method-select">
                <el-radio-group v-model="paymentMethod" size="small">
                  <el-radio value="微信支付">微信</el-radio>
                  <el-radio value="支付宝">支付宝</el-radio>
                  <el-radio value="银行转账">银行</el-radio>
                  <el-radio value="现金">现金</el-radio>
                </el-radio-group>
              </div>
            </div>
          </div>

          <div class="receipt-actions">
            <el-button
              type="primary"
              :loading="payLoading"
              :disabled="!canPay"
              size="large"
              @click="handlePay"
            >
              <el-icon><Check /></el-icon>
              确认收款
            </el-button>
            <el-button size="large" @click="handlePrintReceipt">打印收据</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Delete, Tickets, Check } from '@element-plus/icons-vue'
import { getStudentList } from '@/api/student'
import { createFeeRecord, calculateFee } from '@/api/fee'

const today = new Date().toISOString().slice(0, 10)

const studentSearchLoading = ref(false)
const payLoading = ref(false)
const selectedStudentId = ref(null)
const selectedStudent = ref(null)
const studentOptions = ref([])
const discountAmount = ref(0)
const paymentMethod = ref('微信支付')

const feeItems = reactive([
  { feeType: '学费', amount: 0, remark: '' }
])

const subTotal = computed(() => feeItems.reduce((sum, item) => sum + (item.amount || 0), 0))
const grandTotal = computed(() => Math.max(0, subTotal.value - (discountAmount.value || 0)))
const canPay = computed(() => selectedStudent.value && grandTotal.value > 0 && feeItems.some(f => f.amount > 0))

function formatMoney(value) {
  return '¥' + (value || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2 })
}

function calculateTotal() {
  // 触发表单重新计算
}

async function searchStudents(query) {
  if (!query || query.length < 1) { studentOptions.value = []; return }
  studentSearchLoading.value = true
  try {
    const res = await getStudentList({ keyword: query, page: 1, pageSize: 20 })
    studentOptions.value = res.data?.records || []
  } catch { /* ignore */ } finally { studentSearchLoading.value = false }
}

async function handleStudentChange(val) {
  if (!val) { selectedStudent.value = null; return }
  selectedStudent.value = studentOptions.value.find(s => s.id === val)
  if (selectedStudent.value && selectedStudent.value.courseId) {
    try {
      const res = await calculateFee({ studentId: val })
      if (res.data?.items) {
        feeItems.length = 0
        res.data.items.forEach(item => {
          feeItems.push({ feeType: item.feeType, amount: item.amount || 0, remark: item.remark || '' })
        })
      }
    } catch { /* ignore */ }
  }
}

function addFeeItem() {
  feeItems.push({ feeType: '', amount: 0, remark: '' })
}

function removeFeeItem(index) {
  if (feeItems.length <= 1) {
    ElMessage.warning('至少保留一个收费项目')
    return
  }
  feeItems.splice(index, 1)
  calculateTotal()
}

async function handlePay() {
  if (!canPay.value) return

  try {
    await ElMessageBox.confirm(
      `确认向学员"${selectedStudent.value.name}"收取费用 ${formatMoney(grandTotal.value)}？`,
      '确认收款',
      { confirmButtonText: '确认收款', cancelButtonText: '取消', type: 'info' }
    )
  } catch { return }

  payLoading.value = true
  try {
    await createFeeRecord({
      studentId: selectedStudentId.value,
      items: feeItems,
      totalAmount: subTotal.value,
      discountAmount: discountAmount.value,
      paidAmount: grandTotal.value,
      paymentMethod: paymentMethod.value
    })
    ElMessage.success('收费成功！')
    // 重置表单
    selectedStudentId.value = null
    selectedStudent.value = null
    feeItems.length = 0
    feeItems.push({ feeType: '学费', amount: 0, remark: '' })
    discountAmount.value = 0
  } catch { /* ignore */ } finally { payLoading.value = false }
}

function handlePrintReceipt() {
  if (!selectedStudent.value) { ElMessage.warning('请先选择学员'); return }
  ElMessage.info('打印功能开发中 - 将调用浏览器打印')
  window.print()
}

onMounted(() => {
  // 如果 URL 包含 studentId 参数，自动加载
  const urlParams = new URLSearchParams(window.location.search)
  const sid = urlParams.get('studentId')
  if (sid) {
    selectedStudentId.value = Number(sid)
    getStudentList({ keyword: '', page: 1, pageSize: 1 }).then(res => {
      const match = (res.data?.records || []).find(s => s.id === Number(sid))
      if (match) {
        studentOptions.value = [match]
        selectedStudent.value = match
      }
    })
  }
})
</script>

<style scoped>
.payment-page { max-width: 1400px; margin: 0 auto; }
.section-card { border-radius: 8px; margin-bottom: 16px; }
.section-title { font-size: 16px; font-weight: 600; }
.card-header-row { display: flex; justify-content: space-between; align-items: center; }

.student-search { margin-bottom: 16px; }
.money-highlight { font-size: 16px; font-weight: bold; color: #E6A23C; }
.empty-hint { text-align: center; color: #909399; padding: 40px 0; font-size: 14px; }

/* 收据样式 */
.receipt-card { position: sticky; top: 20px; }
.receipt-header { display: flex; align-items: center; gap: 8px; }
.receipt-body { padding: 8px; }
.receipt-org { text-align: center; font-size: 20px; font-weight: bold; color: #409EFF; letter-spacing: 3px; }
.receipt-title { text-align: center; font-size: 14px; color: #606266; margin-top: 4px; }
.receipt-row { display: flex; justify-content: space-between; align-items: center; padding: 6px 0; font-size: 14px; }
.receipt-row .receipt-value { font-weight: 500; }
.receipt-row .receipt-value.total { color: #409EFF; }
.receipt-row.grand-total { background: #f0f9eb; border-radius: 4px; padding: 10px 8px; margin: 8px 0; }
.receipt-row.grand-total .receipt-value.grand { font-size: 20px; font-weight: bold; color: #67C23A; }
.payment-method-select { display: flex; flex-wrap: wrap; }
.receipt-actions { display: flex; gap: 12px; margin-top: 16px; }
.receipt-actions .el-button { flex: 1; }
</style>
