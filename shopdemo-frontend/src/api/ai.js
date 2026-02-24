import request from '@/utils/request'
import axios from 'axios'
import { ElMessage } from 'element-plus'

// 创建一个不使用baseURL的axios实例，用于AI接口
const aiRequest = axios.create({
  baseURL: 'http://localhost:8080',
  timeout: 10000
})

// 添加请求拦截器，添加token
aiRequest.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    console.log('Token:', token)
    if (token && !config.url.includes('/login') && !config.url.includes('/register')) {
      config.headers.Authorization = token
    } else {
      console.warn('No token found in localStorage')
    }
    return config
  },
  error => {
    console.error('Request interceptor error:', error)
    return Promise.reject(error)
  }
)

// 添加响应拦截器
aiRequest.interceptors.response.use(
  response => {
    console.log('Response:', response)
    const res = response.data
    console.log('Response data:', res)
    if (res.code === 200) {
      console.log('Response data.data:', res.data)
      return res.data
    } else {
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || '请求失败'))
    }
  },
  error => {
    console.error('Response error:', error)
    if (error.response) {
      ElMessage.error(error.response.data.message || '服务器错误')
    } else {
      ElMessage.error('网络错误')
    }
    return Promise.reject(error)
  }
)

// 智能客服（增强版）
export function intelligentCustomerService(userId, question) {
  return aiRequest({
    url: '/api/ai/customer-service',
    method: 'post',
    params: { userId, question },
    headers: {
      'Content-Type': 'application/json'
    }
  })
}

// 个性化商品推荐
export function personalizedRecommendation(userId, requestData) {
  return aiRequest({
    url: '/api/ai/recommendation',
    method: 'post',
    params: { userId },
    data: requestData,
    headers: {
      'Content-Type': 'application/json'
    }
  })
}

// 智能搜索（增强版）
export function intelligentSearch(searchQuery) {
  return aiRequest({
    url: '/api/ai/search',
    method: 'post',
    params: { searchQuery },
    headers: {
      'Content-Type': 'application/json'
    }
  })
}

// 智能订单助手
export function intelligentOrderAssistant(userId, orderNo) {
  return aiRequest({
    url: '/api/ai/order-assistant',
    method: 'post',
    params: { userId, orderNo },
    headers: {
      'Content-Type': 'application/json'
    }
  })
}

// 智能商品对比
export function intelligentProductComparison(userId, product1Id, product2Id) {
  return aiRequest({
    url: '/api/ai/product-comparison',
    method: 'post',
    params: { userId, product1Id, product2Id },
    headers: {
      'Content-Type': 'application/json'
    }
  })
}