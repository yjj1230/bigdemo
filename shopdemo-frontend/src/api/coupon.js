import request from '@/utils/request'

export const getAvailableCoupons = () => {
  return request({
    url: '/coupons/available',
    method: 'get'
  })
}

export const getAvailableCouponsForUser = () => {
  return request({
    url: '/coupons/available-for-user',
    method: 'get'
  })
}

export const receiveCoupon = (couponId) => {
  return request({
    url: `/coupons/receive/${couponId}`,
    method: 'post'
  })
}

export const getUserCoupons = () => {
  return request({
    url: '/coupons/user',
    method: 'get'
  })
}

export const getAvailableUserCoupons = () => {
  return request({
    url: '/coupons/user/available',
    method: 'get'
  })
}

export const calculateDiscount = (userCouponId, orderAmount) => {
  return request({
    url: '/coupons/calculate-discount',
    method: 'get',
    params: { userCouponId, orderAmount }
  })
}
