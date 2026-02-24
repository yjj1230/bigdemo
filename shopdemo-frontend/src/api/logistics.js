import request from '@/utils/request'

export function getLogisticsByOrderId(orderId) {
  return request({
    url: `/logistics/order/${orderId}`,
    method: 'get'
  })
}

export function trackLogistics(logisticsNo) {
  return request({
    url: `/logistics/track/${logisticsNo}`,
    method: 'get'
  })
}

export function createLogistics(data) {
  return request({
    url: '/logistics/create',
    method: 'post',
    data
  })
}

export function updateLogistics(data) {
  return request({
    url: '/logistics/update',
    method: 'put',
    data
  })
}