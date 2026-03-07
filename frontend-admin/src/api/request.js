import axios from 'axios'
import { ElMessage, ElNotification } from 'element-plus'
import router from '@/router'

const service = axios.create({
  baseURL: '/api',
  timeout: 15000
})

// 请求拦截器
service.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  error => {
    ElNotification({ title: '请求异常', message: '请求发送失败，请检查网络连接', type: 'error', duration: 3000 })
    return Promise.reject(error)
  }
)

// 友好错误提示映射
const HTTP_ERROR_MAP = {
  400: '请求参数有误，请检查后重试',
  401: '登录已过期，请重新登录',
  403: '您没有权限执行此操作',
  404: '请求的资源不存在',
  405: '请求方式不被允许',
  408: '请求超时，请稍后重试',
  500: '服务器内部错误，请稍后重试',
  502: '网关错误，服务暂时不可用',
  503: '服务暂时不可用，请稍后重试',
  504: '网关超时，请稍后重试'
}

// 响应拦截器
service.interceptors.response.use(
  response => {
    // 文件下载（blob）直接返回
    if (response.config.responseType === 'blob') {
      return response.data
    }
    const res = response.data
    if (res.code !== 200) {
      ElNotification({
        title: '操作失败',
        message: res.message || '请求处理失败，请稍后重试',
        type: 'error',
        duration: 3000
      })
      if (res.code === 401) {
        localStorage.removeItem('token')
        router.push('/login')
      }
      return Promise.reject(new Error(res.message))
    }
    return res
  },
  error => {
    if (error.response) {
      const status = error.response.status
      const serverMsg = error.response.data?.message
      const friendlyMsg = serverMsg || HTTP_ERROR_MAP[status] || `请求失败（错误码：${status}）`

      if (status === 401) {
        localStorage.removeItem('token')
        ElNotification({ title: '登录过期', message: '您的登录已过期，请重新登录', type: 'warning', duration: 3000 })
        router.push('/login')
      } else if (status === 403) {
        ElNotification({ title: '权限不足', message: '您没有权限执行此操作', type: 'warning', duration: 3000 })
      } else {
        ElNotification({ title: '请求失败', message: friendlyMsg, type: 'error', duration: 3000 })
      }
    } else if (error.code === 'ECONNABORTED') {
      ElNotification({ title: '请求超时', message: '服务器响应超时，请稍后重试', type: 'error', duration: 3000 })
    } else {
      ElNotification({ title: '网络异常', message: '无法连接到服务器，请检查网络连接', type: 'error', duration: 3000 })
    }
    return Promise.reject(error)
  }
)

export default service
