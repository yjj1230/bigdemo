import request from '@/utils/request'

export const getCart = () => {
  return request({
    url: '/cart/list',
    method: 'get'
  })
}

export const addToCart = (data) => {
  return request({
    url: '/cart/add',
    method: 'post',
    data
  })
}

export const updateCartItem = (data) => {
  return request({
    url: `/cart/update/${data.cartId}`,
    method: 'put',
    params: { quantity: data.quantity }
  })
}

export const removeFromCart = (cartId) => {
  return request({
    url: `/cart/${cartId}`,
    method: 'delete'
  })
}

export const clearCart = () => {
  return request({
    url: '/cart/clear',
    method: 'delete'
  })
}
