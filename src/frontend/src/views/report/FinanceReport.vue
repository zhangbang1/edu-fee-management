<template>
  <div class="finance-report">
    <el-card shadow="never" class="filter-card">
      <el-form :model="filterForm" inline>
        <el-form-item label="时间范围">
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
        <el-form-item label="收款人">
          <el-select v-model="filterForm.operatorId" placeholder="全部" clearable style="width: 150px">
            <el-option label="管理员" value="1" />
            <el-option label="财务" value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="fetchData">查询</el-button>
          <el-button :icon="Download" @click="handleExport">导出报表</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 核心指标 -->
    <el-row :gutter="20" class="kpi-row">
      <el-col :xs="24" :sm="8">
        <el-card shadow="hover" class="kpi-card">
          <div class="kpi-content">
            <div class="kpi-label">总营收</div>
            <div class="kpi-value up">{{ formatMoney(reportData.totalRevenue) }}</div>
            <div class="kpi-sub">交易笔数：{{ reportData.totalCount }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="8">
        <el-card shadow="hover" class="kpi-card">
          <div class="kpi-content">
            <div class="kpi-label">退款总额</div>
            <div class="kpi-value down">{{ formatMoney(reportData.totalRefund) }}</div>
            <div class="kpi-sub">退款笔数：{{ reportData.refundCount }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="8">
        <el-card shadow="hover" class="kpi-card">
          <div class="kpi-content">
            <div class="kpi-label">净营收</div>
            <div class="kpi-value primary">{{ formatMoney(reportData.netRevenue) }}</div>
            <div class="kpi-sub">退款率：{{ reportData.refundRate }}%</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表 -->
    <el-row :gutter="20" class="chart-row">
      <el-col :xs="24" :lg="14">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="card-header">
              <span>每日营收趋势</span>
            </div>
          </template>
          <div class="chart-container" ref="trendChartRef"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="10">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="card-header">
              <span>费用类型分布</span>
            </div>
          </template>
          <div class="chart-container" ref="pieChartRef"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 明细表格 -->
    <el-card shadow="hover" class="table-card">
      <template #header>
        <div class="card-header">
          <span>收费明细</span>
        </div>
      </template>
      <el-table :data="detailList" border stripe size="small" v-loading="loading">
        <el-table-column type="index" label="#" width="50" align="center" />
        <el-table-column prop="date" label="日期" width="110" />
        <el-table-column prop="studentName" label="学员" min-width="100" />
        <el-table-column prop="feeType" label="费用类型" min-width="100" />
        <el-table-column prop="amount" label="金额" min-width="100" align="right">
          <template #default="{ row }">
            <span :class="row.type === 'refund' ? 'refund-text' : 'income-text'">
              {{ row.type === 'refund' ? '-' : '' }}{{ formatMoney(row.amount) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="paymentMethod" label="支付方式" width="100" align="center" />
        <el-table-column prop="operatorName" label="操作人" width="90" align="center" />
        <el-table-column prop="type" label="类型" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.type === 'income' ? 'success' : 'danger'" size="small">
              {{ row.type === 'income' ? '收入' : '退款' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Download } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { getFeeStatistics, exportFeeRecords } from '@/api/fee'

const loading = ref(false)
const dateRange = ref([])
const filterForm = reactive({ startDate: '', endDate: '', operatorId: '' })
const trendChartRef = ref(null)
const pieChartRef = ref(null)

let trendChart = null
let pieChart = null

const reportData = reactive({
  totalRevenue: 0, totalRefund: 0, netRevenue: 0,
  totalCount: 0, refundCount: 0, refundRate: 0
})

const detailList = ref([])

function formatMoney(value) {
  return '¥' + (value || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2 })
}

function handleDateChange(val) {
  if (val) { filterForm.startDate = val[0]; filterForm.endDate = val[1] }
  else { filterForm.startDate = ''; filterForm.endDate = '' }
}

async function fetchData() {
  loading.value = true
  try {
    const res = await getFeeStatistics({ ...filterForm })
    if (res.data) {
      Object.assign(reportData, res.data)
    }
    // 模拟明细数据
    detailList.value = [
      { date: '2024-12-15', studentName: '张三', feeType: '学费', amount: 5800, paymentMethod: '微信支付', operatorName: '管理员', type: 'income' },
      { date: '2024-12-15', studentName: '李四', feeType: '学费', amount: 12800, paymentMethod: '银行转账', operatorName: '财务', type: 'income' },
      { date: '2024-12-14', studentName: '王五', feeType: '教材费', amount: 800, paymentMethod: '支付宝', operatorName: '管理员', type: 'income' },
      { date: '2024-12-14', studentName: '赵六', feeType: '学费', amount: 9800, paymentMethod: '现金', operatorName: '管理员', type: 'income' },
      { date: '2024-12-13', studentName: '孙七', feeType: '报名费', amount: 1200, paymentMethod: '微信支付', operatorName: '财务', type: 'income' },
      { date: '2024-12-12', studentName: '周八', feeType: '活动费', amount: 500, paymentMethod: '现金', operatorName: '管理员', type: 'income' },
      { date: '2024-12-11', studentName: '吴九', feeType: '学费', amount: 3500, paymentMethod: '支付宝', operatorName: '管理员', type: 'refund' },
      { date: '2024-12-10', studentName: '郑十', feeType: '学费', amount: 6800, paymentMethod: '微信支付', operatorName: '财务', type: 'income' }
    ]
  } catch { /* ignore */ } finally { loading.value = false }
}

function initTrendChart() {
  if (!trendChartRef.value) return
  trendChart = echarts.init(trendChartRef.value)
  const dates = ['6月', '7月', '8月', '9月', '10月', '11月', '12月']
  trendChart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['收入', '退款', '净营收'] },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: dates },
    yAxis: { type: 'value', axisLabel: { formatter: v => v >= 10000 ? (v / 10000) + '万' : v } },
    series: [
      { name: '收入', type: 'bar', data: [82000, 95000, 110000, 135000, 158000, 172000, 186000], itemStyle: { color: '#67C23A' } },
      { name: '退款', type: 'bar', data: [2000, 3500, 4200, 5000, 6800, 7200, 8500], itemStyle: { color: '#F56C6C' } },
      { name: '净营收', type: 'line', smooth: true, data: [80000, 91500, 105800, 130000, 151200, 164800, 177500], itemStyle: { color: '#409EFF' } }
    ]
  })
}

function initPieChart() {
  if (!pieChartRef.value) return
  pieChart = echarts.init(pieChartRef.value)
  pieChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    series: [{
      type: 'pie', radius: ['45%', '70%'], center: ['50%', '55%'],
      itemStyle: { borderRadius: 4, borderColor: '#fff', borderWidth: 2 },
      label: { formatter: '{b}\n{d}%' },
      data: [
        { value: 125000, name: '学费', itemStyle: { color: '#409EFF' } },
        { value: 28000, name: '报名费', itemStyle: { color: '#67C23A' } },
        { value: 15000, name: '教材费', itemStyle: { color: '#E6A23C' } },
        { value: 8000, name: '活动费', itemStyle: { color: '#909399' } },
        { value: 1500, name: '其他', itemStyle: { color: '#F56C6C' } }
      ]
    }]
  })
}

function resizeCharts() { trendChart?.resize(); pieChart?.resize() }

async function handleExport() {
  try {
    const res = await exportFeeRecords(filterForm)
    const blob = new Blob([res], { type: 'application/vnd.ms-excel' })
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `财务报表_${new Date().toISOString().slice(0, 10)}.xlsx`
    link.click()
    URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch { ElMessage.error('导出失败') }
}

onMounted(() => {
  nextTick(() => { initTrendChart(); initPieChart() })
  window.addEventListener('resize', resizeCharts)
  fetchData()
  // 模拟数据
  Object.assign(reportData, {
    totalRevenue: 186500, totalRefund: 8500, netRevenue: 178000,
    totalCount: 145, refundCount: 8, refundRate: 5.5
  })
})

onUnmounted(() => {
  window.removeEventListener('resize', resizeCharts)
  trendChart?.dispose(); pieChart?.dispose()
})
</script>

<style scoped>
.finance-report { max-width: 1400px; margin: 0 auto; }
.filter-card { margin-bottom: 20px; border-radius: 8px; }
.kpi-row { margin-bottom: 20px; }
.kpi-card { border-radius: 8px; }
.kpi-content { text-align: center; padding: 8px 0; }
.kpi-label { font-size: 14px; color: #909399; margin-bottom: 8px; }
.kpi-value { font-size: 28px; font-weight: bold; margin-bottom: 8px; }
.kpi-value.up { color: #67C23A; }
.kpi-value.down { color: #F56C6C; }
.kpi-value.primary { color: #409EFF; }
.kpi-sub { font-size: 12px; color: #909399; }
.chart-row { margin-bottom: 20px; }
.chart-card { border-radius: 8px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.card-header span { font-size: 16px; font-weight: 600; }
.chart-container { height: 300px; width: 100%; }
.table-card { border-radius: 8px; }
.income-text { font-weight: 600; color: #67C23A; }
.refund-text { font-weight: 600; color: #F56C6C; }
</style>
