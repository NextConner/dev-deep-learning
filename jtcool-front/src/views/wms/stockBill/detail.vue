<template>
  <div class="app-container">
    <el-page-header @back="goBack" content="出入库单详情" />
    <el-card style="margin-top: 20px">
      <template #header>单据信息</template>
      <el-descriptions :column="2" border v-if="billData.billId">
        <el-descriptions-item label="单据号">{{ billData.billNo }}</el-descriptions-item>
        <el-descriptions-item label="单据类型">{{ getBillTypeLabel(billData.billType) }}</el-descriptions-item>
        <el-descriptions-item label="仓库">{{ billData.warehouseName }}</el-descriptions-item>
        <el-descriptions-item label="单据状态">
          <el-tag v-if="billData.billStatus === 'DRAFT'" type="info">草稿</el-tag>
          <el-tag v-else type="success">已确认</el-tag>
        </el-descriptions-item>
      </el-descriptions>
      <el-form v-else :model="billData" label-width="100px" style="margin-top: 20px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="单据类型">
              <el-select v-model="billData.billType" placeholder="请选择单据类型">
                <el-option label="采购入库" value="IN_PURCHASE" />
                <el-option label="退货入库" value="IN_RETURN" />
                <el-option label="销售出库" value="OUT_SALES" />
                <el-option label="退货出库" value="OUT_RETURN" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="仓库">
              <el-select v-model="billData.warehouseId" placeholder="请选择仓库">
                <el-option v-for="item in warehouseOptions" :key="item.warehouseId" :label="item.warehouseName" :value="item.warehouseId" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </el-card>

    <el-card style="margin-top: 20px">
      <template #header>
        <span>单据明细</span>
        <el-button type="primary" size="small" style="float: right" @click="handleAddItem" v-if="!billData.billId || billData.billStatus === 'DRAFT'">添加明细</el-button>
      </template>
      <el-table :data="billData.items" border>
        <el-table-column label="产品" prop="productId">
          <template #default="scope">
            <el-select v-model="scope.row.productId" placeholder="选择产品" v-if="!billData.billId || billData.billStatus === 'DRAFT'" filterable>
              <el-option v-for="item in productOptions" :key="item.productId" :label="item.productName" :value="item.productId" />
            </el-select>
            <span v-else>{{ scope.row.productName }}</span>
          </template>
        </el-table-column>
        <el-table-column label="数量" prop="quantity">
          <template #default="scope">
            <el-input-number v-model="scope.row.quantity" :min="1" v-if="!billData.billId || billData.billStatus === 'DRAFT'" />
            <span v-else>{{ scope.row.quantity }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" v-if="!billData.billId || billData.billStatus === 'DRAFT'">
          <template #default="scope">
            <el-button link type="danger" @click="handleRemoveItem(scope.$index)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <div style="margin-top: 20px; text-align: center" v-if="!billData.billId || billData.billStatus === 'DRAFT'">
      <el-button type="primary" @click="handleSave">保存</el-button>
      <el-button @click="goBack">取消</el-button>
    </div>
  </div>
</template>

<script setup name="WmsStockBillDetail">
import { getStockBill, addStockBill, updateStockBill } from "@/api/wms/stockBill";
import { listWarehouse } from "@/api/wms/warehouse";
import { listProduct } from "@/api/product/product";
import { useRoute, useRouter } from "vue-router";

const { proxy } = getCurrentInstance();
const route = useRoute();
const router = useRouter();

const billData = ref({ items: [] });
const warehouseOptions = ref([]);
const productOptions = ref([]);

const billTypeMap = {
  'IN_PURCHASE': '采购入库',
  'IN_RETURN': '退货入库',
  'OUT_SALES': '销售出库',
  'OUT_RETURN': '退货出库'
};

function getBillTypeLabel(type) {
  return billTypeMap[type] || type;
}

function getBillDetail() {
  const billId = route.params.billId;
  if (billId && billId !== '0') {
    getStockBill(billId).then(response => {
      billData.value = response.data;
    });
  }
}

function getOptions() {
  listWarehouse().then(response => {
    warehouseOptions.value = response.rows;
  });
  listProduct().then(response => {
    productOptions.value = response.rows;
  });
}

function goBack() {
  router.back();
}

function handleAddItem() {
  billData.value.items.push({
    productId: undefined,
    quantity: 1
  });
}

function handleRemoveItem(index) {
  billData.value.items.splice(index, 1);
}

function handleSave() {
  if (!billData.value.billType || !billData.value.warehouseId) {
    proxy.$modal.msgError("请填写完整信息");
    return;
  }
  if (billData.value.items.length === 0) {
    proxy.$modal.msgError("请添加明细");
    return;
  }

  const promise = billData.value.billId ? updateStockBill(billData.value) : addStockBill(billData.value);
  promise.then(() => {
    proxy.$modal.msgSuccess("保存成功");
    router.back();
  });
}

getBillDetail();
getOptions();
</script>
