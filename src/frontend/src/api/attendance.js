import request from './request'

/**
 * 获取考勤记录列表
 * @param {Object} params - { page, pageSize, classId, studentId, startDate, endDate, status }
 */
export function getAttendanceList(params) {
  return request.get('/attendance', { params })
}

/**
 * 批量签到/签退
 * @param {Object} data - { classId, date, records: [{ studentId, status, remark }] }
 */
export function batchCheckIn(data) {
  return request.post('/attendance/batch', data)
}

/**
 * 单个签到/签退
 * @param {Object} data - { studentId, classId, date, status, remark }
 */
export function singleCheckIn(data) {
  return request.post('/attendance', data)
}

/**
 * 更新考勤记录
 * @param {Number} id
 * @param {Object} data
 */
export function updateAttendance(id, data) {
  return request.put(`/attendance/${id}`, data)
}

/**
 * 获取考勤统计
 * @param {Object} params - { classId, startDate, endDate }
 */
export function getAttendanceStatistics(params) {
  return request.get('/attendance/statistics', { params })
}

/**
 * 导出考勤记录
 * @param {Object} params
 */
export function exportAttendance(params) {
  return request.get('/attendance/export', { params, responseType: 'blob' })
}
