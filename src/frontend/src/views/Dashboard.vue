<template>
  <div class="dashboard">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :xs="24" :sm="12" :lg="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-info">
              <div class="stat-label">学员总数</div>
              <div class="stat-value">{{ stats.totalStudents }}</div>
              <div class="stat-trend up">
                <el-icon><Top /></el-icon>
                <span>较上月 +{{ stats.studentGrowth }}%</span>
              </div>
            </div>
            <div class="stat-icon students">
              <el-icon :size="40"><UserFilled /></el-icon>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="12" :lg="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-info">
              <div class="stat-label">本月营收</div>
              <div class="stat-value">{{ formatMoney(stats.monthRevenue) }}</div>
              <div class="stat-trend up">
                <el-icon><Top /></el-icon>
                <span>较上月 +{{ stats.revenueGrowth }}%</span>
              </div>
            </div>
            <div class="stat-icon revenue">
              <el-icon :size="40"><Money /></el-icon>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="12" :lg="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-info">
              <div class="stat-label">在授课程</div>
              <div class="stat-value">{{ stats.activeCourses }}</div>
              <div class="stat-trend normal">
                <span>开设{{ stats.totalCourses }}门</span>
              </div>
            </div>
            <div class="stat-icon courses">
              <el-icon :size="40"><Notebook /></el-icon>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="12" :lg="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-info">
              <div class="stat-label">待缴费</div>
              <div class="stat-value">{{ stats.pendingPayments }}</div>
              <div class="stat-trend down">
                <span>共{{ formatMoney(stats.pendingAmount) }}</span>
              </div>
            </div>
            <div class="stat-icon pending">
              <el-icon :size="40"><WarningFilled /></el-icon>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="chart-row">
      <el-col :xs="24" :lg="16">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="card-header">
              <span>近6个月营收趋势</span>
              <el-radio-group v-model="revenuePeriod" size="small">
                <el-radio-button value="month">按月</el-radio-button>
                <el-radio-button value="quarter">按季</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div class="chart-container" ref="revenueChartRef"></div>
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="8">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="card-header">
              <span>收费方式分布</span>
            </div>
          </template>
          <div class="chart-container" ref="paymentChartRef"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 快捷操作和最近记录 -->
    <el-row :gutter="20" class="bottom-row">
      <el-col :xs="24" :lg="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>快捷操作</span>
            </div>
          </template>
          <div class="quick-actions">
            <el-button type="primary" :icon="Plus" @click="goTo('/fees/payment')">
              新建收费
            </el-button>
            <el-button type="success" :icon="User" @click="goTo('/students')">
              添加学员
            </el-button>
            <el-button type="warning" :icon="Notebook" @click="goTo('/courses')">
              管理课程
            </el-button>
            <el-button type="info" :icon="Calendar" @click="goTo('/attendance')">
              考勤签到
            </el-button>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>最近收费记录</span>
              <el-button type="primary" link size="small" @click="goTo('/fees/records')">
                查看全部
              </el-button>
            </div>
          </template>
          <el-table :data="recentFees" style="width: 100%" size="small">
            <el-table-column prop="studentName" label="学员" min-width="80" />
            <el-table-column prop="amount" label="金额" min-width="80">
              <template #default="{ row }">
                <span class="amount-text">{{ formatMoney(row.amount) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="paymentMethod" label="支付方式" min-width="80" />
            <el-table-column prop="payTime" label="时间" min-width="100" />
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import { Plus, User, Notebook, Calendar } from '@element-plus/icons-vue'

const router = useRouter()

const revenuePeriod = ref('month')
const revenueChartRef = ref(null)
const paymentChartRef = ref(null)

let revenueChart = null
let paymentChart = null

// 统计数据
const stats = reactive({
  totalStudents: 358,
  studentGrowth: 12.5,
  monthRevenue: 186500,
  revenueGrowth: 8.3,
  activeCourses: 28,
  totalCourses: 45,
  pendingPayments: 23,
  pendingAmount: 68500
})

// 最近收费记录
const recentFees = ref([
  { studentName: '张三', amount: 5800, paymentMethod: '微信支付', payTime: '2024-12-15 14:30' },
  { studentName: '李四', amount: 12800, paymentMethod: '银行转账', payTime: '2024-12-15 11:20' },
  { studentName: '王五', amount: 3600, paymentMethod: '支付宝', payTime: '2024-12-14 16:45' },
  { studentName: '赵六', amount: 9800, paymentMethod: '现金', payTime: '2024-12-14 09:30' },
  { studentName: '孙七', amount: 4800, paymentMethod: '微信支付', payTime: '2024-12-13 15:10' }
])

function formatMoney(value) {
  return '¥' + (value || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

function goTo(path) {
  router.push(path)
}

function initRevenueChart() {
  if (!revenueChartRef.value) return
  revenueChart = echarts.init(revenueChartRef.value)
  const months = ['7月', '8月', '9月', '10月', '11月', '12月']
  const option = {
    tooltip: {
      trigger: 'axis',
      formatter: (params) => {
        const p = params[0]
        return `${p.name}<br/>${p.seriesName}: ${formatMoney(p.value)}`
      }
    },
    legend: {
      data: ['报名费', '学费', '教材费', '其他']
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: months,
      boundaryGap: false
    },
    yAxis: {
      type: 'value',
      axisLabel: {
        formatter: (val) => {
          if (val >= 10000) return (val / 10000) + '万'
          return val
        }
      }
    },
    series: [
      {
        name: '报名费',
        type: 'line',
        smooth: true,
        data: [12000, 15000, 18000, 22000, 28000, 35000],
        itemStyle: { color: '#409EFF' }
      },
      {
        name: '学费',
        type: 'line',
        smooth: true,
        data: [45000, 52000, 60000, 78000, 95000, 110000],
        itemStyle: { color: '#67C23A' }
      },
      {
        name: '教材费',
        type: 'line',
        smooth: true,
        data: [8000, 9000, 10000, 12000, 14000, 16500],
        itemStyle: { color: '#E6A23C' }
      },
      {
        name: '其他',
        type: 'line',
        smooth: true,
        data: [5000, 6000, 7000, 8000, 9000, 10000],
        itemStyle: { color: '#909399' }
      }
    ]
  }
  revenueChart.setOption(option)
}

function initPaymentChart() {
  if (!paymentChartRef.value) return
  paymentChart = echarts.init(paymentChartRef.value)
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)'
    },
    series: [
      {
        type: 'pie',
        radius: ['50%', '75%'],
        center: ['50%', '50%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 4,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: true,
          position: 'outside',
          formatter: '{b}\n{d}%'
        },
        data: [
          { value: 45, name: '微信支付', itemStyle: { color: '#07C160' } },
          { value: 25, name: '支付宝', itemStyle: { color: '#1677FF' } },
          { value: 18, name: '银行转账', itemStyle: { color: '#E6A23C' } },
          { value: 12, name: '现金', itemStyle: { color: '#909399' } }
        ]
      }
    ]
  }
  paymentChart.setOption(option)
}

function resizeCharts() {
  revenueChart?.resize()
  paymentChart?.resize()
}

onMounted(() => {
  nextTick(() => {
    initRevenueChart()
    initPaymentChart()
  })
  window.addEventListener('resize', resizeCharts)
})

onUnmounted(() => {
  window.removeEventListener('resize', resizeCharts)
  revenueChart?.dispose()
  paymentChart?.dispose()
})
</script>

<style scoped>
.dashboard {
  max-width: 1400px;
  margin: 0 auto;
}

/* 统计卡片 */
.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  border-radius: 8px;
  transition: transform 0.2s;
}

.stat-card:hover {
  transform: translateY(-2px);
}

.stat-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 8px;
}

.stat-trend {
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 2px;
}

.stat-trend.up {
  color: #67C23A;
}

.stat-trend.down {
  color: #F56C6C;
}

.stat-trend.normal {
  color: #909399;
}

.stat-icon {
  width: 64px;
  height: 64px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-icon.students {
  background: rgba(64, 158, 255, 0.1);
  color: #409EFF;
}

.stat-icon.revenue {
  background: rgba(103, 194, 58, 0.1);
  color: #67C23A;
}

.stat-icon.courses {
  background: rgba(230, 162, 60, 0.1);
  color: #E6A23C;
}

.stat-icon.pending {
  background: rgba(245, 108, 108, 0.1);
  color: #F56C6C;
}

/* 图表行 */
.chart-row {
  margin-bottom: 20px;
}

.chart-card {
  border-radius: 8px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header span {
  font-size: 16px;
  font-weight: 600;
}

.chart-container {
  height: 320px;
  width: 100%;
}

/* 底部区域 */
.bottom-row {
  margin-bottom: 20px;
}

.quick-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.amount-text {
  font-weight: 600;
  color: #303133;
}
</style>
