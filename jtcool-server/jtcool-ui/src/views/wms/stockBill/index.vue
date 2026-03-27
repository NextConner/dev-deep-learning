<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" :inline="true">
      <el-form-item label="单据类型" prop="billType">
        <el-select v-model="queryParams.billType" placeholder="请选择" clearable>
          <el-option label="采购入库" value="IN_PURCHASE" />
          <el-option label="销售出库" value="OUT_SALES" />
          <el-option label="调拨入库" value="IN_TRANSFER" />
          <el-option label="调拨出库" value="OUT_TRANSFER" />
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

    <el-table v-loading="loading" :data="stockBillList">
      <el-table-column label="单据编号" prop="billNo" />
      <el-table-column label="单据类型" prop="billType" />
      <el-table-column label="单据日期" prop="billDate" width="180" />
      <el-table-column label="单据状态" prop="billStatus" />
      <el-table-column label="操作" align="center" width="200">
        <template slot-scope="scope">
          <el-button size="mini" type="text" @click="handleUpdate(scope.row)">修改</el-button>
          <el-button size="mini" type="text" @click="handleConfirm(scope.row)" v-if="scope.row.billStatus === 'PENDING'">确认</el-button>
          <el-button size="mini" type="text" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" :visible.sync="open" width="600px">
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="单据类型" prop="billType">
          <el-select v-model="form.billType" placeholder="请选择">
            <el-option label="采购入库" value="IN_PURCHASE" />
            <el-option label="销售出库" value="OUT_SALES" />
          </el-select>
        </el-form-item>
        <el-form-item label="仓库" prop="warehouseId">
          <el-input v-model="form.warehouseId" placeholder="请输入仓库ID" />
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
import { listStockBill, getStockBill, addStockBill, updateStockBill, delStockBill, confirmStockBill } from '@/api/wms/stockBill'

export default {
  name: 'StockBill',
  data() {
    return {
      loading: false,
      stockBillList: [],
      total: 0,
      title: '',
      open: false,
      queryParams: { pageNum: 1, pageSize: 10, billType: null },
      form: {},
      rules: {
        billType: [{ required: true, message: '单据类型不能为空', trigger: 'change' }]
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listStockBill(this.queryParams).then(response => {
        this.stockBillList = response.rows
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
      this.title = '添加出入库单'
    },
    handleUpdate(row) {
      this.reset()
      getStockBill(row.billId).then(response => {
        this.form = response.data
        this.open = true
        this.title = '修改出入库单'
      })
    },
    handleConfirm(row) {
      this.$confirm('是否确认该出入库单?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        return confirmStockBill(row.billId)
      }).then(() => {
        this.getList()
        this.msgSuccess('确认成功')
      })
    },
    handleDelete(row) {
      this.$confirm('是否确认删除该出入库单?', '警告', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        return delStockBill(row.billId)
      }).then(() => {
        this.getList()
        this.msgSuccess('删除成功')
      })
    },
    submitForm() {
      this.$refs['form'].validate(valid => {
        if (valid) {
          if (this.form.billId) {
            updateStockBill(this.form).then(() => {
              this.msgSuccess('修改成功')
              this.open = false
              this.getList()
            })
          } else {
            addStockBill(this.form).then(() => {
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
