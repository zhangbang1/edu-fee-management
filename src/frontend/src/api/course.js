import request from './request'

/**
 * 获取课程列表
 * @param {Object} params - { page, pageSize, keyword, category, status }
 */
export function getCourseList(params) {
  return request.get('/courses', { params })
}

/**
 * 获取所有课程（下拉选择用）
 */
export function getAllCourses() {
  return request.get('/courses/all')
}

/**
 * 获取课程详情
 * @param {Number} id
 */
export function getCourseDetail(id) {
  return request.get(`/courses/${id}`)
}

/**
 * 新增课程
 * @param {Object} data
 */
export function createCourse(data) {
  return request.post('/courses', data)
}

/**
 * 更新课程
 * @param {Number} id
 * @param {Object} data
 */
export function updateCourse(id, data) {
  return request.put(`/courses/${id}`, data)
}

/**
 * 删除课程
 * @param {Number} id
 */
export function deleteCourse(id) {
  return request.delete(`/courses/${id}`)
}
