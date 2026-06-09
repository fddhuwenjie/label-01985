import request from './request'

export const getScorePage = (params) => request.get('/score/page', { params })
export const addScore = (data) => request.post('/score', data)
export const updateScore = (data) => request.put('/score', data)
export const deleteScore = (id) => request.delete(`/score/${id}`)
export const getScoreStats = (semester) => request.get('/score/stats', { params: { semester } })
export const getStudentRanking = (semester) => request.get('/score/ranking', { params: { semester } })
export const exportScore = (semester) => request.get('/score/export', {
  params: { semester },
  responseType: 'blob'
})

export const getStudentScoreTrend = (params) => request.get('/score-alert/trend', { params })
export const getScoreAlertPage = (params) => request.get('/score-alert/page', { params })
export const markAlertProcessed = (id) => request.put(`/score-alert/${id}/process`)
export const generateScoreAlerts = (threshold) => request.post('/score-alert/generate', null, { params: { threshold } })
export const getClassScoreComparison = (params) => request.get('/score-alert/class-comparison', { params })

export const getClassList = () => request.get('/class/list')
export const getCourseList = () => request.get('/course/page', { params: { current: 1, size: 100 } })
export const getStudentList = () => request.get('/student/page', { params: { current: 1, size: 100 } })
