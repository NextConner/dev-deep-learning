<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="产品" prop="productId">
        <el-select v-model="queryParams.productId" placeholder="请选择产品" clearable filterable>
          <el-option v-for="item in productOptions" :key="item.productId" :label="item.productName" :value="item.productId" />
        </el-select>
      </el-form-item>
      <el-form-item label="仓库" prop="warehouseId">
        <el-select v-model="queryParams.warehouseId" placeholder="请选择仓库" clearable>
          <el-option v-for="item in warehouseOptions" :key="item.warehouseId" :label="item.warehouseName" :value="item.warehouseId" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="inventoryList">
      <el-table-column label="产品名称" align="center" prop="productName" :show-overflow-tooltip="true" />
      <el-table-column label="仓库" align="center" prop="warehouseName" />
      <el-table-column label="总数量" align="center" prop="quantity" />
      <el-table-column label="锁定数量" align="center" prop="lockedQuantity" />
      <el-table-column label="可用数量" align="center" prop="availableQuantity">
        <template #default="scope">
          <span :style="{color: scope.row.availableQuantity < scope.row.warningStock ? 'red' : ''}">
            {{ scope.row.availableQuantity }}
            <el-tag type="danger" size="small" v-if="scope.row.availableQuantity < scope.row.warningStock">低库存</el-tag>
          </span>
        </template>
      </el-table-column>
      <el-table-column label="更新时间" align="center" prop="updateTime" width="160">
        <template #default="scope">
          <span>{{ parseTime(scope.row.updateTime) }}</span>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total>0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
  </div>
</template>

<script setup name="WmsInventory">
import { listInventory } from "@/api/wms/inventory";
import { listWarehouse } from "@/api/wms/warehouse";
import { listProduct } from "@/api/product/product";

const { proxy } = getCurrentInstance();

const inventoryList = ref([]);
const warehouseOptions = ref([]);
const productOptions = ref([]);
const loading = ref(true);
const showSearch = ref(true);
const total = ref(0);

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    productId: undefined,
    warehouseId: undefined
  }
});

const { queryParams } = toRefs(data);

function getList() {
  loading.value = true;
  listInventory(queryParams.value).then(response => {
    inventoryList.value = response.rows;
    total.value = response.total;
    loading.value = false;
  });
}

function getOptions() {
  listWarehouse().then(response => {
    warehouseOptions.value = response.rows;
  });
  listProduct().then(response => {
    productOptions.value = response.rows;
  });
}

function handleQuery() {
  queryParams.value.pageNum = 1;
  getList();
}

function resetQuery() {
  proxy.resetForm("queryRef");
  handleQuery();
}

getList();
getOptions();
</script>
