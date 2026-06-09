import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue')
  },
  {
    path: '/',
    component: () => import('@/views/Layout.vue'),
    redirect: '/dashboard',
    children: [
      { path: 'dashboard', name: 'Dashboard', component: () => import('@/views/Dashboard.vue'), meta: { title: '首页' } },
      { path: 'student', name: 'Student', component: () => import('@/views/StudentManage.vue'), meta: { title: '学生管理' } },
      { path: 'teacher', name: 'Teacher', component: () => import('@/views/TeacherManage.vue'), meta: { title: '教师管理' } },
      { path: 'course', name: 'Course', component: () => import('@/views/CourseManage.vue'), meta: { title: '课程管理' } },
      { path: 'score', name: 'Score', component: () => import('@/views/ScoreManage.vue'), meta: { title: '成绩管理' } },
      { path: 'analysis', name: 'Analysis', component: () => import('@/views/ScoreAnalysis.vue'), meta: { title: '成绩分析' } },
      { path: 'trend', name: 'ScoreTrend', component: () => import('@/views/ScoreTrend.vue'), meta: { title: '成绩趋势' } },
      { path: 'alert', name: 'ScoreAlert', component: () => import('@/views/ScoreAlert.vue'), meta: { title: '成绩预警' } },
      { path: 'comparison', name: 'ClassComparison', component: () => import('@/views/ClassComparison.vue'), meta: { title: '班级对比' } }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.path !== '/login' && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router
