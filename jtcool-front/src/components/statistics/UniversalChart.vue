<template>
  <div class="universal-chart">
    <div v-if="!chartData || !chartData.length" class="empty">暂无数据</div>
    <div v-else-if="chartType === 'table'" class="table-wrapper">
      <el-table :data="chartData" border stripe>
        <el-table-column v-for="col in columns" :key="col" :prop="col" :label="col" />
      </el-table>
    </div>
    <div v-else ref="chartRef" class="chart-wrapper"></div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted, onBeforeUnmount, nextTick } from 'vue'
import * as echarts from 'echarts'

const props = defineProps({
  chartType: { type: String, default: 'line' },
  chartData: { type: Array, default: () => [] },
  columns: { type: Array, default: () => [] }
})

const chartRef = ref(null)
let chartInstance = null

const renderChart = () => {
  if (!chartRef.value || !props.chartData.length) return

  if (!chartInstance) {
    chartInstance = echarts.init(chartRef.value)
  }

  const option = buildChartOption()
  chartInstance.setOption(option, true)
}

const buildChartOption = () => {
  const xData = props.chartData.map(item => item.group_key || item[props.columns[0]])
  const yData = props.chartData.map(item => item.value || item[props.columns[1]])

  const baseOption = {
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: xData },
    yAxis: { type: 'value' }
  }

  if (props.chartType === 'line') {
    return {
      ...baseOption,
      series: [{ type: 'line', data: yData, smooth: true }]
    }
  } else if (props.chartType === 'bar') {
    return {
      ...baseOption,
      series: [{ type: 'bar', data: yData }]
    }
  } else if (props.chartType === 'pie') {
    return {
      tooltip: { trigger: 'item' },
      series: [{
        type: 'pie',
        radius: '50%',
        data: props.chartData.map(item => ({
          name: item.group_key || item[props.columns[0]],
          value: item.value || item[props.columns[1]]
        }))
      }]
    }
  }

  return baseOption
}

const handleResize = () => {
  if (chartInstance) {
    chartInstance.resize()
  }
}

watch(() => [props.chartType, props.chartData], async () => {
  await nextTick()
  renderChart()
}, { deep: true })

onMounted(() => {
  renderChart()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  if (chartInstance) {
    chartInstance.dispose()
    chartInstance = null
  }
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.universal-chart {
  width: 100%;
  height: 100%;
}
.chart-wrapper {
  width: 100%;
  height: 400px;
}
.table-wrapper {
  width: 100%;
}
.empty {
  text-align: center;
  padding: 50px;
  color: #999;
}
</style>
