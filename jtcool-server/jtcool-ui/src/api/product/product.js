import request from '@/utils/request'

export function listProduct(query) {
  return request({ url: '/product/product/list', method: 'get', params: query })
}

export function getProduct(productId) {
  return request({ url: '/product/product/' + productId, method: 'get' })
}

export function addProduct(data) {
  return request({ url: '/product/product', method: 'post', data: data })
}

export function updateProduct(data) {
  return request({ url: '/product/product', method: 'put', data: data })
}

export function delProduct(productId) {
  return request({ url: '/product/product/' + productId, method: 'delete' })
}
