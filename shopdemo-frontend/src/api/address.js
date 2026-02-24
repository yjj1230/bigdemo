import request from '@/utils/request'

export const getAddresses = () => {
  return request({
    url: '/address/list',
    method: 'get'
  })
}

export const getAddressById = (id) => {
  return request({
    url: `/address/${id}`,
    method: 'get'
  })
}

export const addAddress = (data) => {
  return request({
    url: '/address/add',
    method: 'post',
    data
  })
}

export const updateAddress = (data) => {
  return request({
    url: '/address/update',
    method: 'put',
    data
  })
}

export const deleteAddress = (id) => {
  return request({
    url: `/address/${id}`,
    method: 'delete'
  })
}

export const setDefaultAddress = (addressId) => {
  return request({
    url: `/address/default/${addressId}`,
    method: 'put'
  })
}
