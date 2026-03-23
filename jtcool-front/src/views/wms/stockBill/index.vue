<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="单据号" prop="billNo">
        <el-input v-model="queryParams.billNo" placeholder="请输入单据号" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="单据类型" prop="billType">
        <el-select v-model="queryParams.billType" placeholder="单据类型" clearable>
          <el-option label="采购入库" value="IN_PURCHASE" />
          <el-option label="退货入库" value="IN_RETURN" />
          <el-option label="销售出库" value="OUT_SALES" />
          <el-option label="退货出库" value="OUT_RETURN" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd">新增</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="stockBillList">
      <el-table-column label="单据号" align="center" prop="billNo" width="180" />
      <el-table-column label="单据类型" align="center" prop="billType">
        <template #default="scope">
          <el-tag v-if="scope.row.billType.startsWith('IN')" type="success">{{ getBillTypeLabel(scope.row.billType) }}</el-tag>
          <el-tag v-else type="warning">{{ getBillTypeLabel(scope.row.billType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="仓库" align="center" prop="warehouseName" />
      <el-table-column label="单据状态" align="center" prop="billStatus">
        <template #default="scope">
          <el-tag v-if="scope.row.billStatus === 'DRAFT'" type="info">草稿</el-tag>
          <el-tag v-else type="success">已确认</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="160">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="200">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click="handleDetail(scope.row)">详情</el-button>
          <el-button link type="primary" @click="handleConfirm(scope.row)" v-if="scope.row.billStatus === 'DRAFT'">确认</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-if="scope.row.billStatus === 'DRAFT'">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total>0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
  </div>
</template>

<script setup name="WmsStockBill">
import { listStockBill, delStockBill, confirmStockBill } from "@/api/wms/stockBill";
import { useRouter } from "vue-router";

const { proxy } = getCurrentInstance();
const router = useRouter();

const stockBillList = ref([]);
const loading = ref(true);
const showSearch = ref(true);
const total = ref(0);

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    billNo: undefined,
    billType: undefined
  }
});

const { queryParams } = toRefs(data);

const billTypeMap = {
  'IN_PURCHASE': '采购入库',
  'IN_RETURN': '退货入库',
  'IN_OTHER': '其他入库',
  'OUT_SALES': '销售出库',
  'OUT_RETURN': '退货出库',
  'OUT_LOSS': '损耗出库',
  'OUT_OTHER': '其他出库'
};

function getBillTypeLabel(type) {
  return billTypeMap[type] || type;
}

function getList() {
  loading.value = true;
  listStockBill(queryParams.value).then(response => {
    stockBillList.value = response.rows;
    total.value = response.total;
    loading.value = false;
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

function handleAdd() {
  router.push({ path: '/wms/stockBill/detail/0' });
}

function handleDetail(row) {
  router.push({ path: '/wms/stockBill/detail/' + row.billId });
}

function handleConfirm(row) {
  proxy.$modal.confirm('确认后将触发库存变更，是否继续?').then(() => {
    return confirmStockBill(row.billId, proxy.$store.state.user.userId);
  }).then(() => {
    proxy.$modal.msgSuccess("确认成功");
    getList();
  }).catch(() => {});
}

function handleDelete(row) {
  proxy.$modal.confirm('是否确认删除单据号为"' + row.billNo + '"的数据项?').then(() => {
    return delStockBill(row.billId);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("删除成功");
  }).catch(() => {});
}

getList();
</script>
