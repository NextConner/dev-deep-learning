import request from '@/utils/request'

export function listStockBill(query) {
  return request({ url: '/wms/stockBill/list', method: 'get', params: query })
}

export function getStockBill(billId) {
  return request({ url: '/wms/stockBill/' + billId, method: 'get' })
}

export function addStockBill(data) {
  return request({ url: '/wms/stockBill', method: 'post', data: data })
}

export function updateStockBill(data) {
  return request({ url: '/wms/stockBill', method: 'put', data: data })
}

export function delStockBill(billId) {
  return request({ url: '/wms/stockBill/' + billId, method: 'delete' })
}

export function confirmStockBill(billId, operatorId) {
  return request({ url: '/wms/stockBill/confirm/' + billId, method: 'post', params: { operatorId } })
}
