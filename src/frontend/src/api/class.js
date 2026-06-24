import request from './request'

/**
 * 获取班级列表
 * @param {Object} params - { page, pageSize, keyword, courseId, status }
 */
export function getClassList(params) {
  return request.get('/classes', { params })
}

/**
 * 获取所有班级（下拉选择用）
 */
export function getAllClasses() {
  return request.get('/classes/all')
}

/**
 * 获取班级详情
 * @param {Number} id
 */
export function getClassDetail(id) {
  return request.get(`/classes/${id}`)
}

/**
 * 新增班级
 * @param {Object} data
 */
export function createClass(data) {
  return request.post('/classes', data)
}

/**
 * 更新班级
 * @param {Number} id
 * @param {Object} data
 */
export function updateClass(id, data) {
  return request.put(`/classes/${id}`, data)
}

/**
 * 删除班级
 * @param {Number} id
 */
export function deleteClass(id) {
  return request.delete(`/classes/${id}`)
}

/**
 * 获取班级学员列表
 * @param {Number} id
 */
export function getClassStudents(id) {
  return request.get(`/classes/${id}/students`)
}
