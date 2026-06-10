import request from './request'

export const getClassList = () => request.get('/class/list')
export const getClassById = (id) => request.get(`/class/${id}`)
