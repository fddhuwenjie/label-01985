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
export const getScoreTrend = (params) => request.get('/score/trend', { params })
export const getClassComparison = (params) => request.get('/score/class-compare', { params })
