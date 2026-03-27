<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" :inline="true">
      <el-form-item label="订单号" prop="orderNo">
        <el-input v-model="queryParams.orderNo" placeholder="请输入订单号" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="付款状态" prop="paymentStatus">
        <el-select v-model="queryParams.paymentStatus" placeholder="请选择" clearable>
          <el-option label="未付款" value="UNPAID" />
          <el-option label="部分付款" value="PARTIAL" />
          <el-option label="已付款" value="PAID" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="financeList">
      <el-table-column label="订单号" prop="orderNo" />
      <el-table-column label="订单金额" prop="orderAmount" />
      <el-table-column label="已付金额" prop="paidAmount" />
      <el-table-column label="付款状态" prop="paymentStatus" />
      <el-table-column label="操作" align="center" width="150">
        <template slot-scope="scope">
          <el-button size="mini" type="text" @click="handlePayment(scope.row)">付款</el-button>
          <el-button size="mini" type="text" @click="handleInvoice(scope.row)">开票</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog title="添加付款" :visible.sync="paymentOpen" width="500px">
      <el-form ref="paymentForm" :model="paymentForm" label-width="100px">
        <el-form-item label="付款金额">
          <el-input v-model="paymentForm.paymentAmount" placeholder="请输入付款金额" />
        </el-form-item>
        <el-form-item label="付款方式">
          <el-select v-model="paymentForm.paymentMethod" placeholder="请选择">
            <el-option label="现金" value="CASH" />
            <el-option label="银行转账" value="BANK" />
            <el-option label="支付宝" value="ALIPAY" />
            <el-option label="微信" value="WECHAT" />
          </el-select>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="paymentOpen = false">取 消</el-button>
        <el-button type="primary" @click="submitPayment">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listFinance, getFinance, addPayment, updateInvoice } from '@/api/oms/finance'

export default {
  name: 'Finance',
  data() {
    return {
      loading: false,
      financeList: [],
      total: 0,
      paymentOpen: false,
      queryParams: { pageNum: 1, pageSize: 10, orderNo: null, paymentStatus: null },
      paymentForm: { financeId: null, paymentAmount: null, paymentMethod: null }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listFinance(this.queryParams).then(response => {
        this.financeList = response.rows
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
    handlePayment(row) {
      this.paymentForm = { financeId: row.financeId, paymentAmount: null, paymentMethod: null }
      this.paymentOpen = true
    },
    handleInvoice(row) {
      this.$prompt('请输入发票号', '开具发票', {
        confirmButtonText: '确定',
        cancelButtonText: '取消'
      }).then(({ value }) => {
        updateInvoice({ financeId: row.financeId, invoiceNo: value }).then(() => {
          this.msgSuccess('开票成功')
          this.getList()
        })
      })
    },
    submitPayment() {
      addPayment(this.paymentForm).then(() => {
        this.msgSuccess('付款成功')
        this.paymentOpen = false
        this.getList()
      })
    }
  }
}
</script>
