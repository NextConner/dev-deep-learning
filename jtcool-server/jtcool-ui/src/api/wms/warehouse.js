import request from '@/utils/request'

export function listWarehouse(query) {
  return request({ url: '/wms/warehouse/list', method: 'get', params: query })
}

export function getWarehouse(warehouseId) {
  return request({ url: '/wms/warehouse/' + warehouseId, method: 'get' })
}

export function addWarehouse(data) {
  return request({ url: '/wms/warehouse', method: 'post', data: data })
}

export function updateWarehouse(data) {
  return request({ url: '/wms/warehouse', method: 'put', data: data })
}

export function delWarehouse(warehouseId) {
  return request({ url: '/wms/warehouse/' + warehouseId, method: 'delete' })
}
