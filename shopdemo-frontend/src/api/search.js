import request from '@/utils/request'

export const addSearchHistory = (keyword) => {
  return request({
    url: '/search-history',
    method: 'post',
    params: { keyword }
  })
}

export const getSearchHistory = (limit = 10) => {
  return request({
    url: '/search-history',
    method: 'get',
    params: { limit }
  })
}

export const deleteSearchHistory = (id) => {
  return request({
    url: `/search-history/${id}`,
    method: 'delete'
  })
}

export const clearSearchHistory = () => {
  return request({
    url: '/search-history/clear',
    method: 'delete'
  })
}

export const getHotSearch = (limit = 10) => {
  return request({
    url: '/search-history/hot',
    method: 'get',
    params: { limit }
  })
}
