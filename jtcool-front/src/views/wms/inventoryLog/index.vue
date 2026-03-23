<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="产品" prop="productId">
        <el-select v-model="queryParams.productId" placeholder="请选择产品" clearable filterable>
          <el-option v-for="item in productOptions" :key="item.productId" :label="item.productName" :value="item.productId" />
        </el-select>
      </el-form-item>
      <el-form-item label="变更类型" prop="changeType">
        <el-select v-model="queryParams.changeType" placeholder="变更类型" clearable>
          <el-option label="入库" value="IN" />
          <el-option label="出库" value="OUT" />
        </el-select>
      </el-form-item>
      <el-form-item label="日期范围">
        <el-date-picker v-model="dateRange" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="logList">
      <el-table-column label="产品名称" align="center" prop="productName" :show-overflow-tooltip="true" />
      <el-table-column label="单据号" align="center" prop="billNo" width="180" />
      <el-table-column label="变更类型" align="center" prop="changeType">
        <template #default="scope">
          <el-tag v-if="scope.row.changeType === 'IN'" type="success">入库</el-tag>
          <el-tag v-else type="warning">出库</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="变更数量" align="center" prop="changeQuantity" />
      <el-table-column label="变更前" align="center" prop="beforeQuantity" />
      <el-table-column label="变更后" align="center" prop="afterQuantity" />
      <el-table-column label="操作人" align="center" prop="operatorName" />
      <el-table-column label="创建时间" align="center" prop="createTime" width="160">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total>0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
  </div>
</template>

<script setup name="WmsInventoryLog">
import { listInventoryLog } from "@/api/wms/inventoryLog";
import { listProduct } from "@/api/product/product";

const { proxy } = getCurrentInstance();

const logList = ref([]);
const productOptions = ref([]);
const loading = ref(true);
const showSearch = ref(true);
const total = ref(0);
const dateRange = ref([]);

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    productId: undefined,
    changeType: undefined
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
  listInventoryLog(params).then(response => {
    logList.value = response.rows;
    total.value = response.total;
    loading.value = false;
  });
}

function getProductOptions() {
  listProduct().then(response => {
    productOptions.value = response.rows;
  });
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

getList();
getProductOptions();
</script>
