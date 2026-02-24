import request from '@/utils/request'

export function getAllOrders() {
  return request({
    url: '/order/all',
    method: 'get'
  })
}

export function shipOrder(orderId) {
  return request({
    url: `/order/ship/${orderId}`,
    method: 'post'
  })
}

export function getAllProducts() {
  return request({
    url: '/product/list',
    method: 'get'
  })
}

export function getAllProductsIncludingOffShelf() {
  return request({
    url: '/product/list/all',
    method: 'get'
  })
}

export function createProduct(data) {
  return request({
    url: '/product/add',
    method: 'post',
    data
  })
}

export function updateProduct(data) {
  return request({
    url: '/product/update',
    method: 'put',
    data
  })
}

export function deleteProduct(id) {
  return request({
    url: `/product/${id}`,
    method: 'delete'
  })
}

export function onShelfProduct(id) {
  return request({
    url: `/product/onShelf/${id}`,
    method: 'put'
  })
}

export function offShelfProduct(id) {
  return request({
    url: `/product/offShelf/${id}`,
    method: 'put'
  })
}

export function updateProductStock(id, stock) {
  return request({
    url: '/product/updateStock',
    method: 'put',
    params: { id, stock }
  })
}

export function getAllUsers() {
  return request({
    url: '/user/all',
    method: 'get'
  })
}

export function updateUserRole(userId, role) {
  return request({
    url: `/user/${userId}/role`,
    method: 'put',
    data: { role }
  })
}

export function deleteUser(userId) {
  return request({
    url: `/user/${userId}`,
    method: 'delete'
  })
}

export function updateUserById(userId, data) {
  return request({
    url: `/user/${userId}`,
    method: 'put',
    data
  })
}
