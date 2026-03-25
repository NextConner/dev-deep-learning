<template>
  <div class="timeline-chart">
    <div v-if="!events.length" class="empty">暂无数据</div>
    <div v-else ref="chartRef" class="chart-wrapper"></div>
  </div>
</template>

<script setup>
import { computed, getCurrentInstance, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import * as echarts from 'echarts'

const props = defineProps({
  events: { type: Array, required: true },
  timeUnit: { type: String, default: 'hour' }
})

const emit = defineEmits(['eventClick'])

const { proxy } = getCurrentInstance()
const chartRef = ref(null)
let chartInstance

const seriesData = computed(() => {
  const sortedEvents = [...props.events].sort((a, b) => new Date(a.createTime) - new Date(b.createTime))
  let runningValue = 0

  return sortedEvents.map(event => {
    const quantity = Number(event.changeQuantity || 0)
    runningValue += event.changeType === 'IN' ? quantity : -quantity
    return {
      value: runningValue,
      event
    }
  })
})

function formatAxisTime(value) {
  return props.timeUnit === 'hour'
    ? proxy.parseTime(value, '{m}-{d} {h}:{i}')
    : proxy.parseTime(value, '{m}-{d}')
}

function formatTooltipTime(value) {
  return proxy.parseTime(value, '{y}-{m}-{d} {h}:{i}')
}

function renderChart() {
  if (!chartRef.value || !seriesData.value.length) {
    return
  }

  if (!chartInstance) {
    chartInstance = echarts.init(chartRef.value)
    chartInstance.on('click', params => {
      const event = params.data?.event
      if (event) {
        emit('eventClick', event)
      }
    })
  }

  chartInstance.setOption({
    grid: {
      left: 48,
      right: 24,
      top: 24,
      bottom: 36
    },
    tooltip: {
      trigger: 'axis',
      formatter: items => {
        const current = items?.[0]?.data
        if (!current?.event) {
          return ''
        }
        const { event, value } = current
        const sign = event.changeType === 'IN' ? '+' : '-'
        return [
          formatTooltipTime(event.createTime),
          `${event.productName || ''} ${event.warehouseName || ''}`.trim(),
          `${event.changeType === 'IN' ? '入库' : '出库'} ${sign}${event.changeQuantity}`,
          `累计净变化 ${value}`
        ].join('<br/>')
      }
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: seriesData.value.map(item => formatAxisTime(item.event.createTime)),
      axisLabel: {
        color: '#666'
      }
    },
    yAxis: {
      type: 'value',
      name: '累计净变化',
      axisLabel: {
        color: '#666'
      },
      splitLine: {
        lineStyle: {
          color: '#f0f0f0'
        }
      }
    },
    series: [
      {
        name: '库存变化',
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 8,
        lineStyle: {
          width: 3,
          color: '#409eff'
        },
        itemStyle: {
          color: params => params.data.event.changeType === 'IN' ? '#10b981' : '#ef4444'
        },
        areaStyle: {
          color: 'rgba(64, 158, 255, 0.12)'
        },
        data: seriesData.value
      }
    ]
  })

  chartInstance.resize()
}

function handleResize() {
  chartInstance?.resize()
}

watch(() => [props.events, props.timeUnit], async () => {
  if (!props.events.length) {
    chartInstance?.dispose()
    chartInstance = null
    return
  }
  await nextTick()
  renderChart()
}, { deep: true, immediate: true })

onMounted(() => {
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  chartInstance?.dispose()
  chartInstance = null
})
</script>

<style lang="scss" scoped>
.timeline-chart {
  min-height: 320px;
}

.empty {
  text-align: center;
  padding: 40px 20px;
  color: #999;
}

.chart-wrapper {
  height: 320px;
}
</style>
