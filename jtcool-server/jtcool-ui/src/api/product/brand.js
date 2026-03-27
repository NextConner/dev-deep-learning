import request from '@/utils/request'

export function listBrand(query) {
  return request({ url: '/product/brand/list', method: 'get', params: query })
}

export function getBrand(brandId) {
  return request({ url: '/product/brand/' + brandId, method: 'get' })
}

export function addBrand(data) {
  return request({ url: '/product/brand', method: 'post', data: data })
}

export function updateBrand(data) {
  return request({ url: '/product/brand', method: 'put', data: data })
}

export function delBrand(brandId) {
  return request({ url: '/product/brand/' + brandId, method: 'delete' })
}
