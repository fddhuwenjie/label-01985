import request from './request'

// 学生成绩趋势（横轴=考试时间，纵轴=分数；可按 courseId 过滤科目）
export const getScoreTrend = (studentId, courseId) =>
  request.get('/score-analysis/trend', { params: { studentId, courseId } })

// 预警列表（分页）
export const getAlertPage = (params) =>
  request.get('/score-analysis/alert/page', { params })

// 触发预警扫描生成
export const generateAlerts = () =>
  request.post('/score-analysis/alert/generate')

// 标记预警为已处理
export const markAlertHandled = (id) =>
  request.put(`/score-analysis/alert/${id}/handle`)

// 班级成绩对比
export const compareClasses = (classAId, classBId, courseId) =>
  request.get('/score-analysis/class-compare', {
    params: { classAId, classBId, courseId }
  })
