import request from '@/utils/request'

export function listInventory(query) {
  return request({ url: '/wms/inventory/list', method: 'get', params: query })
}

export function getInventory(inventoryId) {
  return request({ url: '/wms/inventory/' + inventoryId, method: 'get' })
}

export function addInventory(data) {
  return request({ url: '/wms/inventory', method: 'post', data: data })
}

export function updateInventory(data) {
  return request({ url: '/wms/inventory', method: 'put', data: data })
}

export function delInventory(inventoryId) {
  return request({ url: '/wms/inventory/' + inventoryId, method: 'delete' })
}
