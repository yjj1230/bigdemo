import request from '@/utils/request'

/**
 * 发送消息给智能Agent
 * @param message 用户消息
 * @returns Agent的响应
 */
export const sendMessage = (message) => {
  return request({
    url: '/agent/chat',
    method: 'post',
    data: { message }
  })
}

/**
 * 获取Agent支持的功能列表
 * @returns Agent能力列表
 */
export const getAgentCapabilities = () => {
  return request({
    url: '/agent/capabilities',
    method: 'get'
  })
}

/**
 * 健康检查
 * @returns 健康状态
 */
export const healthCheck = () => {
  return request({
    url: '/agent/health',
    method: 'get'
  })
}
