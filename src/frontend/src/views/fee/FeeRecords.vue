<template>
  <div class="fee-records">
    <el-card shadow="never" class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="学员姓名">
          <el-input v-model="searchForm.keyword" placeholder="请输入学员姓名" clearable :prefix-icon="Search" @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="支付方式">
          <el-select v-model="searchForm.paymentMethod" placeholder="选择支付方式" clearable style="width: 130px">
            <el-option label="微信支付" value="微信支付" />
            <el-option label="支付宝" value="支付宝" />
            <el-option label="银行转账" value="银行转账" />
            <el-option label="现金" value="现金" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="选择状态" clearable style="width: 110px">
            <el-option label="已缴费" value="paid" />
            <el-option label="已退款" value="refunded" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期范围">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            @change="handleDateChange"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="RefreshRight" @click="handleReset">重置</el-button>
          <el-button :icon="Download" @click="handleExport">导出</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="table-card">
      <div class="table-header">
        <div class="table-title">收费台账</div>
        <div class="summary-info">
          <span v-if="summary.totalAmount">合计实收：<b class="total-money">{{ formatMoney(summary.totalAmount) }}</b></span>
        </div>
      </div>

      <el-table v-loading="loading" :data="tableData" border stripe style="width: 100%" @sort-change="handleSortChange">
        <el-table-column prop="id" label="流水号" width="100" align="center" />
        <el-table-column prop="studentName" label="学员" min-width="100" />
        <el-table-column prop="feeItems" label="收费项目" min-width="150">
          <template #default="{ row }">
            <span v-for="(item, idx) in row.feeItems" :key="idx" class="fee-item-tag">
              {{ item.feeType }}
            </span>
            <span v-if="!row.feeItems || row.feeItems.length === 0">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="totalAmount" label="原价" min-width="100" align="right" sortable="custom">
          <template #default="{ row }">
            <span class="money-text">{{ formatMoney(row.totalAmount) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="discountAmount" label="优惠" min-width="90" align="right">
          <template #default="{ row }">
            <span class="discount-text">{{ row.discountAmount > 0 ? '-' + formatMoney(row.discountAmount) : '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="paidAmount" label="实收" min-width="110" align="right" sortable="custom">
          <template #default="{ row }">
            <span class="paid-text">{{ formatMoney(row.paidAmount) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="paymentMethod" label="支付方式" min-width="110" align="center" />
        <el-table-column prop="operatorName" label="收款人" width="90" align="center" />
        <el-table-column prop="payTime" label="收款时间" min-width="150" sortable="custom" />
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 'paid' ? 'success' : 'danger'" size="small">
              {{ row.status === 'paid' ? '已缴费' : '已退款' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleViewDetail(row)">详情</el-button>
            <el-button
              v-if="row.status === 'paid'"
              type="danger" link size="small"
              @click="handleRefund(row)"
            >退款</el-button>
          </template>
        </el-table-column>
      </el-table>

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

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="收费详情" width="550px" destroy-on-close>
      <el-descriptions :column="2" border size="small" v-if="currentRecord">
        <el-descriptions-item label="流水号">{{ currentRecord.id }}</el-descriptions-item>
        <el-descriptions-item label="学员">{{ currentRecord.studentName }}</el-descriptions-item>
        <el-descriptions-item label="支付方式">{{ currentRecord.paymentMethod }}</el-descriptions-item>
        <el-descriptions-item label="收款时间">{{ currentRecord.payTime }}</el-descriptions-item>
        <el-descriptions-item label="原价">{{ formatMoney(currentRecord.totalAmount) }}</el-descriptions-item>
        <el-descriptions-item label="优惠">-{{ formatMoney(currentRecord.discountAmount) }}</el-descriptions-item>
        <el-descriptions-item label="实收金额">
          <span class="paid-detail">{{ formatMoney(currentRecord.paidAmount) }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="currentRecord.status === 'paid' ? 'success' : 'danger'" size="small">
            {{ currentRecord.status === 'paid' ? '已缴费' : '已退款' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="收费项目" :span="2">
          <div v-for="(item, idx) in currentRecord.feeItems" :key="idx">
            {{ item.feeType }}：{{ formatMoney(item.amount) }}
          </div>
        </el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ currentRecord.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 退款弹窗 -->
    <el-dialog v-model="refundVisible" title="退款操作" width="480px" destroy-on-close>
      <el-form ref="refundFormRef" :model="refundForm" :rules="refundRules" label-width="100px">
        <el-form-item label="退款金额" prop="refundAmount">
          <el-input-number
            v-model="refundForm.refundAmount"
            :min="0.01"
            :max="refundMaxAmount"
            :precision="2"
            style="width: 100%"
            controls-position="right"
          />
        </el-form-item>
        <el-form-item label="退款原因" prop="refundReason">
          <el-input v-model="refundForm.refundReason" type="textarea" :rows="3" placeholder="请输入退款原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="refundVisible = false">取消</el-button>
        <el-button type="danger" :loading="refundLoading" @click="handleRefundSubmit">确认退款</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, RefreshRight, Download } from '@element-plus/icons-vue'
import { getFeeRecords, getFeeDetail, refundFee, getFeeStatistics, exportFeeRecords } from '@/api/fee'

const loading = ref(false); const detailVisible = ref(false); const refundVisible = ref(false)
const refundLoading = ref(false); const refundFormRef = ref(null); const currentRecord = ref(null)

const dateRange = ref([])

const searchForm = reactive({
  keyword: '', paymentMethod: '', status: '',
  startDate: '', endDate: ''
})

const pagination = reactive({ page: 1, pageSize: 10, total: 0 })
const tableData = ref([])
const summary = reactive({ totalAmount: 0 })

const refundForm = reactive({ refundAmount: 0, refundReason: '' })
const refundRules = {
  refundAmount: [{ required: true, message: '请输入退款金额', trigger: 'blur' }],
  refundReason: [{ required: true, message: '请输入退款原因', trigger: 'blur' }]
}

const refundMaxAmount = computed(() => currentRecord.value?.paidAmount || 0)

function formatMoney(value) {
  return '¥' + (value || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2 })
}

function handleDateChange(val) {
  if (val) { searchForm.startDate = val[0]; searchForm.endDate = val[1] }
  else { searchForm.startDate = ''; searchForm.endDate = '' }
}

async function fetchData() {
  loading.value = true
  try {
    const [listRes, statRes] = await Promise.all([
      getFeeRecords({ page: pagination.page, pageSize: pagination.pageSize, ...searchForm }),
      getFeeStatistics({ ...searchForm }).catch(() => ({ data: { totalAmount: 0 } }))
    ])
    tableData.value = listRes.data?.records || []
    pagination.total = listRes.data?.total || 0
    summary.totalAmount = statRes.data?.totalAmount || 0
  } catch { /* ignore */ } finally { loading.value = false }
}

function handleSearch() { pagination.page = 1; fetchData() }
function handleReset() {
  Object.assign(searchForm, { keyword: '', paymentMethod: '', status: '', startDate: '', endDate: '' })
  dateRange.value = []
  pagination.page = 1; fetchData()
}
function handlePageChange(p) { pagination.page = p; fetchData() }
function handleSizeChange(s) { pagination.pageSize = s; pagination.page = 1; fetchData() }
function handleSortChange() { /* 可扩展 */ }

async function handleViewDetail(row) {
  try {
    const res = await getFeeDetail(row.id)
    currentRecord.value = res.data
    detailVisible.value = true
  } catch { /* ignore */ }
}

function handleRefund(row) {
  currentRecord.value = row
  refundForm.refundAmount = row.paidAmount || 0
  refundForm.refundReason = ''
  refundVisible.value = true
}

async function handleRefundSubmit() {
  const valid = await refundFormRef.value.validate().catch(() => false)
  if (!valid) return
  refundLoading.value = true
  try {
    await refundFee(currentRecord.value.id, {
      refundAmount: refundForm.refundAmount,
      refundReason: refundForm.refundReason
    })
    ElMessage.success('退款成功')
    refundVisible.value = false
    fetchData()
  } catch { /* ignore */ } finally { refundLoading.value = false }
}

async function handleExport() {
  try {
    const res = await exportFeeRecords(searchForm)
    const blob = new Blob([res], { type: 'application/vnd.ms-excel' })
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `收费台账_${new Date().toISOString().slice(0, 10)}.xlsx`
    link.click()
    URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch { ElMessage.error('导出失败') }
}

onMounted(() => { fetchData() })
</script>

<style scoped>
.fee-records { max-width: 1400px; margin: 0 auto; }
.search-card { margin-bottom: 16px; border-radius: 8px; }
.table-card { border-radius: 8px; }
.table-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.table-title { font-size: 16px; font-weight: 600; color: #303133; }
.total-money { font-size: 18px; color: #67C23A; }
.pagination-wrapper { display: flex; justify-content: flex-end; margin-top: 16px; }
.money-text { font-weight: 500; color: #303133; }
.discount-text { color: #F56C6C; }
.paid-text { font-weight: bold; color: #67C23A; font-size: 15px; }
.paid-detail { font-size: 20px; font-weight: bold; color: #67C23A; }
.fee-item-tag {
  display: inline-block;
  margin-right: 4px;
  padding: 1px 6px;
  background: #ecf5ff;
  color: #409EFF;
  border-radius: 3px;
  font-size: 12px;
}
.summary-info { font-size: 14px; color: #606266; }
</style>
