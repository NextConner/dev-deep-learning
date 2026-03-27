import request from '@/utils/request'

export function listCustomer(query) {
  return request({ url: '/oms/customer/list', method: 'get', params: query })
}

export function getCustomer(customerId) {
  return request({ url: '/oms/customer/' + customerId, method: 'get' })
}

export function addCustomer(data) {
  return request({ url: '/oms/customer', method: 'post', data: data })
}

export function updateCustomer(data) {
  return request({ url: '/oms/customer', method: 'put', data: data })
}

export function delCustomer(customerId) {
  return request({ url: '/oms/customer/' + customerId, method: 'delete' })
}
