import request from './request'

export const getAlertPage = (params) => request.get('/alert/page', { params })
export const detectAlerts = () => request.post('/alert/detect')
export const processAlert = (id) => request.put(`/alert/${id}/process`)
