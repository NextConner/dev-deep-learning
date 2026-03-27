import request from '@/utils/request'

export function listSupplier(query) {
  return request({ url: '/product/supplier/list', method: 'get', params: query })
}

export function getSupplier(supplierId) {
  return request({ url: '/product/supplier/' + supplierId, method: 'get' })
}

export function addSupplier(data) {
  return request({ url: '/product/supplier', method: 'post', data: data })
}

export function updateSupplier(data) {
  return request({ url: '/product/supplier', method: 'put', data: data })
}

export function delSupplier(supplierId) {
  return request({ url: '/product/supplier/' + supplierId, method: 'delete' })
}
