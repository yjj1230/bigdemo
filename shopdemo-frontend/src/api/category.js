import request from '@/utils/request'

export const getCategories = () => {
  return request({
    url: '/category',
    method: 'get'
  })
}

export const getCategoryById = (id) => {
  return request({
    url: `/category/${id}`,
    method: 'get'
  })
}

export const getCategoriesByParentId = (parentId) => {
  return request({
    url: `/category/parent/${parentId}`,
    method: 'get'
  })
}

export const addCategory = (data) => {
  return request({
    url: '/category',
    method: 'post',
    data
  })
}

export const updateCategory = (data) => {
  return request({
    url: '/category',
    method: 'put',
    data
  })
}

export const deleteCategory = (id) => {
  return request({
    url: `/category/${id}`,
    method: 'delete'
  })
}
