import request from '@/utils/request'

export function createRefund(data) {
  return request({
    url: '/refund/create',
    method: 'post',
    data
  })
}

export function getRefundById(id) {
  return request({
    url: `/refund/${id}`,
    method: 'get'
  })
}

export function getRefundsByOrderId(orderId) {
  return request({
    url: `/refund/order/${orderId}`,
    method: 'get'
  })
}

export function getRefundsByUserId(userId) {
  return request({
    url: `/refund/user/${userId}`,
    method: 'get'
  })
}

export function approveRefund(id) {
  return request({
    url: `/refund/approve/${id}`,
    method: 'post'
  })
}

export function rejectRefund(id, rejectReason) {
  return request({
    url: `/refund/reject/${id}`,
    method: 'post',
    params: { rejectReason }
  })
}

export function processRefund(id, refundMethod, refundAccount) {
  return request({
    url: `/refund/process/${id}`,
    method: 'post',
    params: { refundMethod, refundAccount }
  })
}

export function completeRefund(id) {
  return request({
    url: `/refund/complete/${id}`,
    method: 'post'
  })
}