import request from '@/utils/request'
import { ElMessage } from 'element-plus'

// 智能客服（使用新的Agent接口）
export function intelligentCustomerService(userId, question) {
  return request({
    url: '/agent/chat',
    method: 'post',
    data: { message: question }
  })
}

// 个性化商品推荐（使用新的Agent接口）
export function personalizedRecommendation(userId, requestData) {
  return request({
    url: '/agent/chat',
    method: 'post',
    data: { message: `推荐商品，偏好：${JSON.stringify(requestData)}` }
  })
}

// 智能搜索（使用新的Agent接口）
export function intelligentSearch(searchQuery) {
  return request({
    url: '/agent/chat',
    method: 'post',
    data: { message: `搜索商品：${searchQuery}` }
  })
}

// 智能订单助手（使用新的Agent接口）
export function intelligentOrderAssistant(userId, orderNo) {
  return request({
    url: '/agent/chat',
    method: 'post',
    data: { message: `查询订单${orderNo}的信息` }
  })
}

// 智能商品对比（使用新的Agent接口）
export function intelligentProductComparison(userId, product1Id, product2Id) {
  return request({
    url: '/agent/chat',
    method: 'post',
    data: { message: `对比商品${product1Id}和商品${product2Id}` }
  })
}