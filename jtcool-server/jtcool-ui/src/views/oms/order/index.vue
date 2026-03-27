<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" :inline="true">
      <el-form-item label="订单号" prop="orderNo">
        <el-input v-model="queryParams.orderNo" placeholder="请输入订单号" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="订单状态" prop="orderStatus">
        <el-select v-model="queryParams.orderStatus" placeholder="请选择" clearable>
          <el-option label="待确认" value="PENDING" />
          <el-option label="已确认" value="CONFIRMED" />
          <el-option label="已发货" value="SHIPPED" />
          <el-option label="已签收" value="RECEIVED" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" icon="el-icon-plus" @click="handleAdd">新增</el-button>
      </el-col>
    </el-row>

    <el-table v-loading="loading" :data="orderList">
      <el-table-column label="订单号" prop="orderNo" />
      <el-table-column label="客户名称" prop="customerName" />
      <el-table-column label="订单金额" prop="totalAmount" />
      <el-table-column label="订单状态" prop="orderStatus" />
      <el-table-column label="创建时间" prop="createTime" width="180" />
      <el-table-column label="操作" align="center" width="200">
        <template slot-scope="scope">
          <el-button size="mini" type="text" @click="handleUpdate(scope.row)">修改</el-button>
          <el-button size="mini" type="text" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" :visible.sync="open" width="600px">
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="客户" prop="customerId">
          <el-input v-model="form.customerId" placeholder="请输入客户ID" />
        </el-form-item>
        <el-form-item label="订单金额" prop="totalAmount">
          <el-input v-model="form.totalAmount" placeholder="请输入订单金额" />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="cancel">取 消</el-button>
        <el-button type="primary" @click="submitForm">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listOrder, getOrder, addOrder, updateOrder, delOrder } from '@/api/oms/order'

export default {
  name: 'Order',
  data() {
    return {
      loading: false,
      orderList: [],
      total: 0,
      title: '',
      open: false,
      queryParams: { pageNum: 1, pageSize: 10, orderNo: null, orderStatus: null },
      form: {},
      rules: {
        customerId: [{ required: true, message: '客户不能为空', trigger: 'blur' }],
        totalAmount: [{ required: true, message: '订单金额不能为空', trigger: 'blur' }]
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listOrder(this.queryParams).then(response => {
        this.orderList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    resetQuery() {
      this.resetForm('queryForm')
      this.handleQuery()
    },
    handleAdd() {
      this.reset()
      this.open = true
      this.title = '添加订单'
    },
    handleUpdate(row) {
      this.reset()
      getOrder(row.orderId).then(response => {
        this.form = response.data
        this.open = true
        this.title = '修改订单'
      })
    },
    handleDelete(row) {
      this.$confirm('是否确认删除订单编号为"' + row.orderNo + '"的数据项?', '警告', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        return delOrder(row.orderId)
      }).then(() => {
        this.getList()
        this.msgSuccess('删除成功')
      })
    },
    submitForm() {
      this.$refs['form'].validate(valid => {
        if (valid) {
          if (this.form.orderId) {
            updateOrder(this.form).then(() => {
              this.msgSuccess('修改成功')
              this.open = false
              this.getList()
            })
          } else {
            addOrder(this.form).then(() => {
              this.msgSuccess('新增成功')
              this.open = false
              this.getList()
            })
          }
        }
      })
    },
    cancel() {
      this.open = false
      this.reset()
    },
    reset() {
      this.form = {}
      this.resetForm('form')
    }
  }
}
</script>
