import request from './request'

export function getStudentTrend(studentId, courseId) {
  return request({
    url: '/api/analysis/trend',
    method: 'get',
    params: { studentId, courseId }
  })
}

export function classCompare(classId1, classId2, courseId) {
  return request({
    url: '/api/analysis/class-compare',
    method: 'get',
    params: { classId1, classId2, courseId }
  })
}

export function generateAlerts() {
  return request({
    url: '/api/analysis/alerts/generate',
    method: 'post'
  })
}

export function getAlertPage(current, size, params) {
  return request({
    url: '/api/analysis/alerts/page',
    method: 'get',
    params: { current, size, ...params }
  })
}

export function handleAlert(id, remark) {
  return request({
    url: `/api/analysis/alerts/${id}/handle`,
    method: 'put',
    params: { remark }
  })
}

export function getUnhandledCount() {
  return request({
    url: '/api/analysis/alerts/unhandled-count',
    method: 'get'
  })
}

export function listStudents() {
  return request({
    url: '/api/analysis/students',
    method: 'get'
  })
}

export function listCourses() {
  return request({
    url: '/api/analysis/courses',
    method: 'get'
  })
}

export function listClasses() {
  return request({
    url: '/api/analysis/classes',
    method: 'get'
  })
}
