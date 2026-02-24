import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const request = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 10000
})

request.interceptors.request.use(
  config => {
    const userStore = useUserStore()
    if (userStore.token && !config.url.includes('/login') && !config.url.includes('/register')) {
      config.headers.Authorization = userStore.token
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

request.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code === 200) {
      return res.data
    } else {
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || '请求失败'))
    }
  },
  error => {
    if (error.response) {
      const status = error.response.status
      if (status === 401) {
        const userStore = useUserStore()
        userStore.logout()
        window.location.href = '/login'
      } else if (status === 403) {
        ElMessage.error('没有权限访问')
      } else if (status === 500) {
        ElMessage.error('服务器错误')
      } else {
        ElMessage.error(error.response.data.message || '请求失败')
      }
    } else {
      ElMessage.error('网络错误')
    }
    return Promise.reject(error)
  }
)

export default request
