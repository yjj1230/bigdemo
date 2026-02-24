import request from '@/utils/request'

export const getProducts = (params) => {
  return request({
    url: '/product/list/page',
    method: 'get',
    params
  })
}

export const getProductDetail = (id) => {
  return request({
    url: `/product/${id}`,
    method: 'get'
  })
}

export const searchProducts = (keyword) => {
  return request({
    url: '/product/search',
    method: 'get',
    params: { keyword }
  })
}

export const getRecommendations = () => {
  return request({
    url: '/product/recommendations',
    method: 'get'
  })
}

export const getAllProductsList = () => {
  return request({
    url: '/product/list',
    method: 'get'
  })
}

export const addFavorite = (productId) => {
  return request({
    url: `/favorites/${productId}`,
    method: 'post'
  })
}

export const removeFavorite = (productId) => {
  return request({
    url: `/favorites/${productId}`,
    method: 'delete'
  })
}

export const getFavorites = () => {
  return request({
    url: '/favorites',
    method: 'get'
  })
}

export const addReview = (data) => {
  return request({
    url: '/reviews',
    method: 'post',
    data
  })
}

export const getReviews = (productId) => {
  return request({
    url: `/reviews/product/${productId}`,
    method: 'get'
  })
}

export const canReview = (productId) => {
  return request({
    url: `/reviews/can-review/${productId}`,
    method: 'get'
  })
}

export const getReviewStatistics = (productId) => {
  return request({
    url: `/reviews/statistics/${productId}`,
    method: 'get'
  })
}

export const filterProducts = (params) => {
  return request({
    url: '/product/filter',
    method: 'get',
    params
  })
}
