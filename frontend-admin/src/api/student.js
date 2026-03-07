import request from './request'

export const getStudentPage = (params) => request.get('/student/page', { params })
export const getStudentById = (id) => request.get(`/student/${id}`)
export const addStudent = (data) => request.post('/student', data)
export const updateStudent = (data) => request.put('/student', data)
export const deleteStudent = (id) => request.delete(`/student/${id}`)
export const getStudentList = () => request.get('/student/list')
