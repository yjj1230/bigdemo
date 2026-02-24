import request from '@/utils/request'

export function createNotification(data) {
  return request({
    url: '/notification/create',
    method: 'post',
    data
  })
}

export function getNotificationById(id) {
  return request({
    url: `/notification/${id}`,
    method: 'get'
  })
}

export function getNotificationsByUserId(userId) {
  return request({
    url: `/notification/user/${userId}`,
    method: 'get'
  })
}

export function getUnreadNotifications(userId) {
  return request({
    url: `/notification/unread/${userId}`,
    method: 'get'
  })
}

export function getUnreadCount(userId) {
  return request({
    url: `/notification/count/unread/${userId}`,
    method: 'get'
  })
}

export function markAsRead(id) {
  return request({
    url: `/notification/read/${id}`,
    method: 'post'
  })
}

export function markAllAsRead(userId) {
  return request({
    url: `/notification/readAll/${userId}`,
    method: 'post'
  })
}

export function deleteNotification(id) {
  return request({
    url: `/notification/${id}`,
    method: 'delete'
  })
}