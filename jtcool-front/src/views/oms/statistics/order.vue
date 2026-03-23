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
      <el-col :span="6">
        <el-card>
          <template #header>订单总数</template>
          <div class="stat-value">{{ summary.orderCount }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <template #header>订单总额</template>
          <div class="stat-value">{{ summary.totalAmount }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <template #header>已收金额</template>
          <div class="stat-value">{{ summary.paidAmount }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <template #header>未收金额</template>
          <div class="stat-value">{{ summary.unpaidAmount }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card style="margin-top: 20px">
      <div ref="chartRef" style="height: 400px"></div>
    </el-card>

    <el-table v-loading="loading" :data="statisticsList" style="margin-top: 20px">
      <el-table-column label="统计日期" align="center" prop="statDate" />
      <el-table-column label="订单数" align="center" prop="orderCount" />
      <el-table-column label="订单总额" align="center" prop="totalAmount" />
      <el-table-column label="已收金额" align="center" prop="paidAmount" />
      <el-table-column label="未收金额" align="center" prop="unpaidAmount" />
    </el-table>

    <pagination v-show="total>0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
  </div>
</template>

<script setup name="OmsOrderStatistics">
import { listOrderStatistics } from "@/api/oms/statistics";
import * as echarts from 'echarts';

const { proxy } = getCurrentInstance();

const statisticsList = ref([]);
const loading = ref(true);
const total = ref(0);
const chartRef = ref(null);
const dateRange = ref([]);
const summary = ref({ orderCount: 0, totalAmount: 0, paidAmount: 0, unpaidAmount: 0 });

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
  listOrderStatistics(params).then(response => {
    statisticsList.value = response.rows;
    total.value = response.total;
    loading.value = false;
    calculateSummary();
    renderChart();
  });
}

function calculateSummary() {
  summary.value = statisticsList.value.reduce((acc, item) => ({
    orderCount: acc.orderCount + item.orderCount,
    totalAmount: acc.totalAmount + parseFloat(item.totalAmount || 0),
    paidAmount: acc.paidAmount + parseFloat(item.paidAmount || 0),
    unpaidAmount: acc.unpaidAmount + parseFloat(item.unpaidAmount || 0)
  }), { orderCount: 0, totalAmount: 0, paidAmount: 0, unpaidAmount: 0 });
}

function renderChart() {
  if (!chartRef.value) return;
  const chart = echarts.init(chartRef.value);
  const option = {
    tooltip: { trigger: 'axis' },
    legend: { data: ['订单总额', '已收金额', '未收金额'] },
    xAxis: { type: 'category', data: statisticsList.value.map(item => item.statDate) },
    yAxis: { type: 'value' },
    series: [
      { name: '订单总额', type: 'line', data: statisticsList.value.map(item => item.totalAmount) },
      { name: '已收金额', type: 'line', data: statisticsList.value.map(item => item.paidAmount) },
      { name: '未收金额', type: 'line', data: statisticsList.value.map(item => item.unpaidAmount) }
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
