import request from '@/utils/request'

export function addPoints(data) {
  return request({
    url: '/points/add',
    method: 'post',
    data
  })
}

export function getPointsById(id) {
  return request({
    url: `/points/${id}`,
    method: 'get'
  })
}

export function getPointsByUserId(userId) {
  return request({
    url: `/points/user/${userId}`,
    method: 'get'
  })
}

export function getTotalPoints(userId) {
  return request({
    url: `/points/total/${userId}`,
    method: 'get'
  })
}

export function getRecentPoints(userId, limit = 10) {
  return request({
    url: `/points/recent/${userId}`,
    method: 'get',
    params: { limit }
  })
}

export function signIn(userId) {
  return request({
    url: `/points/signIn/${userId}`,
    method: 'post'
  })
}