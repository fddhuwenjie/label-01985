import request from './request'

export const getCoursePage = (params) => request.get('/course/page', { params })
export const getCourseById = (id) => request.get(`/course/${id}`)
export const addCourse = (data) => request.post('/course', data)
export const updateCourse = (data) => request.put('/course', data)
export const deleteCourse = (id) => request.delete(`/course/${id}`)
export const getCourseList = () => request.get('/course/list')
