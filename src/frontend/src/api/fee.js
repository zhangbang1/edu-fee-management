import request from './request'

/**
 * 获取收费记录列表
 * @param {Object} params - { page, pageSize, keyword, studentId, paymentMethod, startDate, endDate, status }
 */
export function getFeeRecords(params) {
  return request.get('/fees', { params })
}

/**
 * 获取收费记录详情
 * @param {Number} id
 */
export function getFeeDetail(id) {
  return request.get(`/fees/${id}`)
}

/**
 * 创建收费记录（收款）
 * @param {Object} data - { studentId, items, totalAmount, discountAmount, paidAmount, paymentMethod, remark }
 */
export function createFeeRecord(data) {
  return request.post('/fees', data)
}

/**
 * 退款操作
 * @param {Number} id
 * @param {Object} data - { refundAmount, refundReason }
 */
export function refundFee(id, data) {
  return request.post(`/fees/${id}/refund`, data)
}

/**
 * 获取待缴费学员列表
 * @param {Object} params
 */
export function getPendingPayments(params) {
  return request.get('/fees/pending', { params })
}

/**
 * 获取收费统计数据
 * @param {Object} params - { startDate, endDate }
 */
export function getFeeStatistics(params) {
  return request.get('/fees/statistics', { params })
}

/**
 * 导出收费记录
 * @param {Object} params
 */
export function exportFeeRecords(params) {
  return request.get('/fees/export', { params, responseType: 'blob' })
}

/**
 * 计算费用（根据课程自动计算）
 * @param {Object} data - { studentId, courseIds }
 */
export function calculateFee(data) {
  return request.post('/fees/calculate', data)
}

/**
 * 获取支付方式列表
 */
export function getPaymentMethods() {
  return request.get('/fees/payment-methods')
}
