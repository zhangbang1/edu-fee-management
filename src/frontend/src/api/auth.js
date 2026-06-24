import request from './request'

/**
 * 登录
 * @param {Object} data - { username, password }
 */
export function login(data) {
  return request.post('/auth/login', data)
}

/**
 * 退出登录
 */
export function logout() {
  return request.post('/auth/logout')
}

/**
 * 获取当前用户信息
 */
export function getUserInfo() {
  return request.get('/auth/userinfo')
}

/**
 * 修改密码
 * @param {Object} data - { oldPassword, newPassword }
 */
export function changePassword(data) {
  return request.put('/auth/password', data)
}

/**
 * 刷新Token
 */
export function refreshToken(refreshToken) {
  return request.post('/auth/refresh', { refreshToken })
}
