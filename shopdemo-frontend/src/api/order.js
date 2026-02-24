import request from '@/utils/request'

export const createOrder = (data) => {
  return request({
    url: '/order/create',
    method: 'post',
    data
  })
}

export const createOrderFromCart = (addressId, remark) => {
  return request({
    url: '/order/createFromCart',
    method: 'post',
    data: { addressId, remark }
  })
}

export const getOrders = (params) => {
  return request({
    url: '/order/list',
    method: 'get',
    params
  })
}

export const getOrderDetail = (id) => {
  return request({
    url: `/order/${id}`,
    method: 'get'
  })
}

export const payOrder = (id) => {
  return request({
    url: `/order/pay/${id}`,
    method: 'post'
  })
}

export const cancelOrder = (id) => {
  return request({
    url: `/order/cancel/${id}`,
    method: 'post'
  })
}

export const confirmOrder = (id) => {
  return request({
    url: `/order/finish/${id}`,
    method: 'post'
  })
}

export const getPayRemainingTime = (id) => {
  return request({
    url: `/order/remainingTime/${id}`,
    method: 'get'
  })
}
