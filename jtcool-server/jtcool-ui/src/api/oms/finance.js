import request from '@/utils/request'

export function listFinance(query) {
  return request({ url: '/oms/finance/list', method: 'get', params: query })
}

export function getFinance(financeId) {
  return request({ url: '/oms/finance/' + financeId, method: 'get' })
}

export function addPayment(data) {
  return request({ url: '/oms/finance/payment', method: 'post', data: data })
}

export function updateInvoice(data) {
  return request({ url: '/oms/finance/invoice', method: 'put', data: data })
}
