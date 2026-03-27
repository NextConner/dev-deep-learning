import request from '@/utils/request'

export function listOrder(query) {
  return request({ url: '/oms/order/list', method: 'get', params: query })
}

export function getOrder(orderId) {
  return request({ url: '/oms/order/' + orderId, method: 'get' })
}

export function addOrder(data) {
  return request({ url: '/oms/order', method: 'post', data: data })
}

export function updateOrder(data) {
  return request({ url: '/oms/order', method: 'put', data: data })
}

export function delOrder(orderId) {
  return request({ url: '/oms/order/' + orderId, method: 'delete' })
}

export function updateOrderStatus(orderId, status) {
  return request({ url: '/oms/order/status/' + orderId + '/' + status, method: 'put' })
}

export function confirmBySales(orderId) {
  return request({ url: '/oms/order/workflow/' + orderId + '/sales-confirm', method: 'post' })
}

export function reviewOrder(orderId) {
  return request({ url: '/oms/order/workflow/' + orderId + '/review', method: 'post' })
}

export function confirmByWarehouse(orderId) {
  return request({ url: '/oms/order/workflow/' + orderId + '/warehouse-confirm', method: 'post' })
}

export function registerOutbound(orderId) {
  return request({ url: '/oms/order/workflow/' + orderId + '/register-outbound', method: 'post' })
}

export function confirmShipment(orderId, trackingNumber) {
  return request({ url: '/oms/order/workflow/' + orderId + '/confirm-shipment', method: 'post', params: { trackingNumber } })
}

export function confirmReceipt(orderId) {
  return request({ url: '/oms/order/workflow/' + orderId + '/confirm-receipt', method: 'post' })
}

export function getOrderFlow(orderId) {
  return request({ url: '/oms/order/workflow/' + orderId + '/flow', method: 'get' })
}
