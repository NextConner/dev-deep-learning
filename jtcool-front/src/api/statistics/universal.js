import request from '@/utils/request'

// 解析查询
export function parseQuery(data) {
  return request({
    url: '/statistics/universal/parse',
    method: 'post',
    data: data
  })
}

// 估算查询性能
export function estimateQuery(data) {
  return request({
    url: '/statistics/universal/estimate',
    method: 'post',
    data: data
  })
}

// 执行查询
export function executeQuery(data) {
  return request({
    url: '/statistics/universal/execute',
    method: 'post',
    data: data
  })
}

// 推荐图表类型
export function recommendChart(data) {
  return request({
    url: '/statistics/universal/recommend-chart',
    method: 'post',
    data: data
  })
}
