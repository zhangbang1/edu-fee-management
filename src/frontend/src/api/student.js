import request from './request'

/**
 * 获取学员列表
 * @param {Object} params - { page, pageSize, keyword, status, courseId, classId }
 */
export function getStudentList(params) {
  return request.get('/students', { params })
}

/**
 * 获取学员详情
 * @param {Number} id
 */
export function getStudentDetail(id) {
  return request.get(`/students/${id}`)
}

/**
 * 新增学员
 * @param {Object} data
 */
export function createStudent(data) {
  return request.post('/students', data)
}

/**
 * 更新学员信息
 * @param {Number} id
 * @param {Object} data
 */
export function updateStudent(id, data) {
  return request.put(`/students/${id}`, data)
}

/**
 * 删除学员
 * @param {Number} id
 */
export function deleteStudent(id) {
  return request.delete(`/students/${id}`)
}

/**
 * 导出学员列表
 * @param {Object} params
 */
export function exportStudents(params) {
  return request.get('/students/export', { params, responseType: 'blob' })
}

/**
 * 获取学员缴费历史
 * @param {Number} id
 */
export function getStudentFeeHistory(id) {
  return request.get(`/students/${id}/fees`)
}

/**
 * 获取学员考勤记录
 * @param {Number} id
 */
export function getStudentAttendance(id, params) {
  return request.get(`/students/${id}/attendance`, { params })
}
