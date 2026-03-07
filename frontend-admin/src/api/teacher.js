import request from './request'

export const getTeacherPage = (params) => request.get('/teacher/page', { params })
export const getTeacherById = (id) => request.get(`/teacher/${id}`)
export const addTeacher = (data) => request.post('/teacher', data)
export const updateTeacher = (data) => request.put('/teacher', data)
export const deleteTeacher = (id) => request.delete(`/teacher/${id}`)
export const getTeacherList = () => request.get('/teacher/list')
