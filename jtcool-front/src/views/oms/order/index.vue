<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="订单号" prop="orderNo">
        <el-input v-model="queryParams.orderNo" placeholder="请输入订单号" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="客户" prop="customerId">
        <el-select v-model="queryParams.customerId" placeholder="请选择客户" clearable filterable>
          <el-option v-for="item in customerOptions" :key="item.customerId" :label="item.customerName" :value="item.customerId" />
        </el-select>
      </el-form-item>
      <el-form-item label="订单状态" prop="orderStatus">
        <el-select v-model="queryParams.orderStatus" placeholder="订单状态" clearable>
          <el-option label="已下单" value="CREATED" />
          <el-option label="销售确认" value="SALES_CONFIRMED" />
          <el-option label="订单审核" value="AUDITED" />
          <el-option label="仓库确认" value="WAREHOUSE_CONFIRMED" />
          <el-option label="登记出库" value="OUT_REGISTERED" />
          <el-option label="确认发货" value="SHIPPED" />
          <el-option label="客户签收" value="RECEIVED" />
          <el-option label="已拒绝" value="REJECTED" />
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

    <el-table v-loading="loading" :data="orderList">
      <el-table-column label="订单号" align="center" prop="orderNo" width="180" />
      <el-table-column label="客户名称" align="center" prop="customerName" :show-overflow-tooltip="true" />
      <el-table-column label="订单状态" align="center" prop="orderStatus">
        <template #default="scope">
          <el-tag v-if="scope.row.orderStatus === 'CREATED'" type="info">已下单</el-tag>
          <el-tag v-else-if="scope.row.orderStatus === 'SALES_CONFIRMED'">销售确认</el-tag>
          <el-tag v-else-if="scope.row.orderStatus === 'AUDITED'" type="success">订单审核</el-tag>
          <el-tag v-else-if="scope.row.orderStatus === 'WAREHOUSE_CONFIRMED'" type="primary">仓库确认</el-tag>
          <el-tag v-else-if="scope.row.orderStatus === 'SHIPPED'" type="warning">确认发货</el-tag>
          <el-tag v-else-if="scope.row.orderStatus === 'RECEIVED'" type="success">客户签收</el-tag>
          <el-tag v-else-if="scope.row.orderStatus === 'REJECTED'" type="danger">已拒绝</el-tag>
          <el-tag v-else>{{ scope.row.orderStatus }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="订单金额" align="center" prop="totalAmount" />
      <el-table-column label="实付金额" align="center" prop="finalAmount" />
      <el-table-column label="创建时间" align="center" prop="createTime" width="160">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="200" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click="handleDetail(scope.row)">详情</el-button>
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-if="scope.row.orderStatus === 'CREATED'">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-if="scope.row.orderStatus === 'CREATED'">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total>0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" v-model="open" width="900px" append-to-body>
      <el-form ref="orderRef" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="客户" prop="customerId">
              <el-select v-model="form.customerId" placeholder="请选择客户" filterable>
                <el-option v-for="item in customerOptions" :key="item.customerId" :label="item.customerName" :value="item.customerId" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="订单明细">
              <el-button type="primary" size="small" @click="handleAddItem">添加产品</el-button>
              <el-table :data="form.items" style="margin-top: 10px">
                <el-table-column label="产品" prop="productId">
                  <template #default="scope">
                    <el-select v-model="scope.row.productId" placeholder="选择产品" filterable @change="handleProductChange(scope.row)">
                      <el-option v-for="item in productOptions" :key="item.productId" :label="item.productName" :value="item.productId" />
                    </el-select>
                  </template>
                </el-table-column>
                <el-table-column label="数量" prop="quantity">
                  <template #default="scope">
                    <el-input-number v-model="scope.row.quantity" :min="1" @change="calculateItemTotal(scope.row)" />
                  </template>
                </el-table-column>
                <el-table-column label="单价" prop="unitPrice">
                  <template #default="scope">
                    <el-input-number v-model="scope.row.unitPrice" :precision="2" :min="0" @change="calculateItemTotal(scope.row)" />
                  </template>
                </el-table-column>
                <el-table-column label="小计" prop="totalPrice" />
                <el-table-column label="操作" width="80">
                  <template #default="scope">
                    <el-button link type="danger" @click="handleRemoveItem(scope.$index)">删除</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注">
              <el-input v-model="form.remark" type="textarea" placeholder="请输入备注" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="OmsOrder">
import { listOrder, getOrder, delOrder, addOrder, updateOrder } from "@/api/oms/order";
import { listCustomer } from "@/api/oms/customer";
import { listProduct } from "@/api/product/product";
import { useRouter } from "vue-router";

const { proxy } = getCurrentInstance();
const router = useRouter();

const orderList = ref([]);
const customerOptions = ref([]);
const productOptions = ref([]);
const open = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const total = ref(0);
const title = ref("");

const data = reactive({
  form: { items: [] },
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    orderNo: undefined,
    customerId: undefined,
    orderStatus: undefined
  },
  rules: {
    customerId: [{ required: true, message: "客户不能为空", trigger: "change" }]
  }
});

const { queryParams, form, rules } = toRefs(data);

function getList() {
  loading.value = true;
  listOrder(queryParams.value).then(response => {
    orderList.value = response.rows;
    total.value = response.total;
    loading.value = false;
  });
}

function getCustomerOptions() {
  listCustomer().then(response => {
    customerOptions.value = response.rows;
  });
}

function getProductOptions() {
  listProduct().then(response => {
    productOptions.value = response.rows;
  });
}

function cancel() {
  open.value = false;
  reset();
}

function reset() {
  form.value = {
    orderId: undefined,
    customerId: undefined,
    remark: undefined,
    items: []
  };
  proxy.resetForm("orderRef");
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
  reset();
  open.value = true;
  title.value = "创建订单";
}

function handleUpdate(row) {
  reset();
  getOrder(row.orderId).then(response => {
    form.value = response.data;
    open.value = true;
    title.value = "修改订单";
  });
}

function handleDetail(row) {
  router.push({ path: '/oms/order/detail/' + row.orderId });
}

function handleAddItem() {
  form.value.items.push({
    productId: undefined,
    quantity: 1,
    unitPrice: 0,
    totalPrice: 0
  });
}

function handleRemoveItem(index) {
  form.value.items.splice(index, 1);
}

function handleProductChange(row) {
  const product = productOptions.value.find(p => p.productId === row.productId);
  if (product) {
    row.unitPrice = product.standardPrice;
    calculateItemTotal(row);
  }
}

function calculateItemTotal(row) {
  row.totalPrice = (row.quantity * row.unitPrice).toFixed(2);
}

function submitForm() {
  proxy.$refs["orderRef"].validate(valid => {
    if (valid) {
      if (form.value.orderId != undefined) {
        updateOrder(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功");
          open.value = false;
          getList();
        });
      } else {
        addOrder(form.value).then(response => {
          proxy.$modal.msgSuccess("新增成功");
          open.value = false;
          getList();
        });
      }
    }
  });
}

function handleDelete(row) {
  proxy.$modal.confirm('是否确认删除订单号为"' + row.orderNo + '"的数据项?').then(function() {
    return delOrder(row.orderId);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("删除成功");
  }).catch(() => {});
}

getList();
getCustomerOptions();
getProductOptions();
</script>
