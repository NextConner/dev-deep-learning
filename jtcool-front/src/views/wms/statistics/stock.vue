<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true">
      <el-form-item label="统计类型" prop="statType">
        <el-select v-model="queryParams.statType" placeholder="统计类型">
          <el-option label="日报" value="DAILY" />
          <el-option label="月报" value="MONTHLY" />
          <el-option label="季报" value="QUARTERLY" />
          <el-option label="年报" value="YEARLY" />
        </el-select>
      </el-form-item>
      <el-form-item label="日期范围">
        <el-date-picker v-model="dateRange" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">查询</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="20">
      <el-col :span="8">
        <el-card>
          <template #header>总入库</template>
          <div class="stat-value">{{ summary.inQuantity }}</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card>
          <template #header>总出库</template>
          <div class="stat-value">{{ summary.outQuantity }}</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card>
          <template #header>净变化</template>
          <div class="stat-value">{{ summary.inQuantity - summary.outQuantity }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card style="margin-top: 20px">
      <div ref="chartRef" style="height: 400px"></div>
    </el-card>

    <el-table v-loading="loading" :data="statisticsList" style="margin-top: 20px">
      <el-table-column label="统计日期" align="center" prop="statDate" />
      <el-table-column label="入库数量" align="center" prop="inQuantity" />
      <el-table-column label="出库数量" align="center" prop="outQuantity" />
      <el-table-column label="净变化" align="center">
        <template #default="scope">
          {{ scope.row.inQuantity - scope.row.outQuantity }}
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total>0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
  </div>
</template>

<script setup name="WmsStockStatistics">
import { listStockStatistics } from "@/api/wms/statistics";
import * as echarts from 'echarts';

const { proxy } = getCurrentInstance();

const statisticsList = ref([]);
const loading = ref(true);
const total = ref(0);
const chartRef = ref(null);
const dateRange = ref([]);
const summary = ref({ inQuantity: 0, outQuantity: 0 });

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    statType: 'DAILY'
  }
});

const { queryParams } = toRefs(data);

function getList() {
  loading.value = true;
  const params = { ...queryParams.value };
  if (dateRange.value && dateRange.value.length === 2) {
    params.beginTime = dateRange.value[0];
    params.endTime = dateRange.value[1];
  }
  listStockStatistics(params).then(response => {
    statisticsList.value = response.rows;
    total.value = response.total;
    loading.value = false;
    calculateSummary();
    renderChart();
  });
}

function calculateSummary() {
  summary.value = statisticsList.value.reduce((acc, item) => ({
    inQuantity: acc.inQuantity + (item.inQuantity || 0),
    outQuantity: acc.outQuantity + (item.outQuantity || 0)
  }), { inQuantity: 0, outQuantity: 0 });
}

function renderChart() {
  if (!chartRef.value) return;
  const chart = echarts.init(chartRef.value);
  const option = {
    tooltip: { trigger: 'axis' },
    legend: { data: ['入库数量', '出库数量'] },
    xAxis: { type: 'category', data: statisticsList.value.map(item => item.statDate) },
    yAxis: { type: 'value' },
    series: [
      { name: '入库数量', type: 'bar', data: statisticsList.value.map(item => item.inQuantity) },
      { name: '出库数量', type: 'bar', data: statisticsList.value.map(item => item.outQuantity) }
    ]
  };
  chart.setOption(option);
}

function handleQuery() {
  queryParams.value.pageNum = 1;
  getList();
}

function resetQuery() {
  dateRange.value = [];
  proxy.resetForm("queryRef");
  handleQuery();
}

onMounted(() => {
  getList();
});
</script>

<style scoped>
.stat-value {
  font-size: 24px;
  font-weight: bold;
  text-align: center;
  padding: 20px 0;
}
</style>
