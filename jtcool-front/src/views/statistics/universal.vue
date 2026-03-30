<template>
  <div class="universal-statistics">
    <el-row :gutter="20">
      <!-- 左侧：输入区 -->
      <el-col :span="10">
        <el-card>
          <template #header>查询输入</template>
          <el-form :model="queryForm" label-width="80px">
            <el-form-item label="查询描述">
              <el-input v-model="queryForm.queryText" type="textarea" :rows="3" placeholder="例如：最近30天的订单统计" />
            </el-form-item>
            <el-form-item label="时间范围">
              <el-date-picker v-model="dateRange" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" />
            </el-form-item>
            <el-form-item label="图表类型">
              <el-select v-model="queryForm.chartType" placeholder="自动推荐">
                <el-option label="自动推荐" value="" />
                <el-option label="折线图" value="line" />
                <el-option label="柱状图" value="bar" />
                <el-option label="饼图" value="pie" />
                <el-option label="表格" value="table" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleQuery">执行查询</el-button>
              <el-button @click="handleReset">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <!-- 右侧：执行信息 -->
      <el-col :span="14">
        <el-card>
          <template #header>执行概况</template>
          <div class="execution-log">
            <div v-for="(log, index) in executionLogs" :key="index" class="log-item">
              <el-tag :type="log.type">{{ log.time }}</el-tag>
              <span class="log-message">{{ log.message }}</span>
            </div>
          </div>
          <div v-if="queryResult" class="result-summary">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="数据行数">{{ queryResult.rowCount }}</el-descriptions-item>
              <el-descriptions-item label="执行时间">{{ queryResult.executionTimeMs }}ms</el-descriptions-item>
            </el-descriptions>
            <el-button v-if="!queryResult.hasError" type="success" @click="showChart = true" style="margin-top: 10px">查看图表</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表弹窗 -->
    <el-dialog v-model="showChart" title="数据可视化" width="80%" :close-on-click-modal="false">
      <UniversalChart :chart-type="finalChartType" :chart-data="queryResult?.data || []" :columns="queryResult?.columns || []" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { parseQuery, estimateQuery, executeQuery, recommendChart } from '@/api/statistics/universal'
import UniversalChart from '@/components/statistics/UniversalChart.vue'
import { ElMessage } from 'element-plus'

const queryForm = reactive({
  queryText: '',
  chartType: ''
})

const dateRange = ref([])
const executionLogs = ref([])
const queryResult = ref(null)
const showChart = ref(false)
const finalChartType = ref('line')

const addLog = (message, type = 'info') => {
  executionLogs.value.push({
    time: new Date().toLocaleTimeString(),
    message,
    type
  })
}

const handleQuery = async () => {
  if (!queryForm.queryText) {
    ElMessage.warning('请输入查询描述')
    return
  }

  executionLogs.value = []
  queryResult.value = null

  try {
    addLog('开始解析查询...', 'info')
    const request = {
      queryText: queryForm.queryText,
      beginTime: dateRange.value[0],
      endTime: dateRange.value[1],
      chartType: queryForm.chartType
    }

    const parseRes = await parseQuery(request)
    if (parseRes.code !== 200) {
      addLog('解析失败: ' + parseRes.msg, 'danger')
      return
    }
    addLog('解析成功', 'success')

    const parsed = parseRes.data
    addLog('开始估算性能...', 'info')
    const estimateRes = await estimateQuery(parsed)
    if (estimateRes.code === 200) {
      const est = estimateRes.data
      addLog(`预估行数: ${est.estimatedRows}, 预估时间: ${est.estimatedTimeMs}ms`, 'info')
      if (est.exceedsLimit) {
        addLog('警告: ' + est.warning, 'warning')
      }
    }

    addLog('开始执行查询...', 'info')
    const execRes = await executeQuery(parsed)
    if (execRes.code !== 200) {
      addLog('执行失败: ' + execRes.msg, 'danger')
      return
    }

    queryResult.value = execRes.data
    addLog(`查询完成，返回 ${execRes.data.rowCount} 行数据`, 'success')

    if (queryForm.chartType) {
      finalChartType.value = queryForm.chartType
    } else {
      const recRes = await recommendChart({ query: parsed, result: execRes.data })
      if (recRes.code === 200) {
        finalChartType.value = recRes.data.recommendedType
        addLog(`推荐图表: ${recRes.data.recommendedType} (${recRes.data.reason})`, 'info')
      }
    }

  } catch (error) {
    addLog('发生错误: ' + error.message, 'danger')
  }
}

const handleReset = () => {
  queryForm.queryText = ''
  queryForm.chartType = ''
  dateRange.value = []
  executionLogs.value = []
  queryResult.value = null
}
</script>

<style scoped>
.universal-statistics {
  padding: 20px;
}
.execution-log {
  max-height: 300px;
  overflow-y: auto;
  margin-bottom: 20px;
}
.log-item {
  margin-bottom: 10px;
  display: flex;
  align-items: center;
  gap: 10px;
}
.log-message {
  flex: 1;
}
.result-summary {
  margin-top: 20px;
}
</style>
