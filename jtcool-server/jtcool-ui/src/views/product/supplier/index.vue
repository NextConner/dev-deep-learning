<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" :inline="true">
      <el-form-item label="供应商名称" prop="supplierName">
        <el-input v-model="queryParams.supplierName" placeholder="请输入供应商名称" clearable @keyup.enter.native="handleQuery" />
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

    <el-table v-loading="loading" :data="supplierList">
      <el-table-column label="供应商名称" prop="supplierName" />
      <el-table-column label="联系人" prop="contactPerson" />
      <el-table-column label="联系电话" prop="contactPhone" />
      <el-table-column label="地址" prop="address" />
      <el-table-column label="操作" align="center" width="150">
        <template slot-scope="scope">
          <el-button size="mini" type="text" @click="handleUpdate(scope.row)">修改</el-button>
          <el-button size="mini" type="text" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" :visible.sync="open" width="600px">
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="供应商名称" prop="supplierName">
          <el-input v-model="form.supplierName" placeholder="请输入供应商名称" />
        </el-form-item>
        <el-form-item label="联系人" prop="contactPerson">
          <el-input v-model="form.contactPerson" placeholder="请输入联系人" />
        </el-form-item>
        <el-form-item label="联系电话" prop="contactPhone">
          <el-input v-model="form.contactPhone" placeholder="请输入联系电话" />
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
import { listSupplier, getSupplier, addSupplier, updateSupplier, delSupplier } from '@/api/product/supplier'

export default {
  name: 'Supplier',
  data() {
    return {
      loading: false,
      supplierList: [],
      total: 0,
      title: '',
      open: false,
      queryParams: { pageNum: 1, pageSize: 10, supplierName: null },
      form: {},
      rules: {
        supplierName: [{ required: true, message: '供应商名称不能为空', trigger: 'blur' }]
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listSupplier(this.queryParams).then(response => {
        this.supplierList = response.rows
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
      this.title = '添加供应商'
    },
    handleUpdate(row) {
      this.reset()
      getSupplier(row.supplierId).then(response => {
        this.form = response.data
        this.open = true
        this.title = '修改供应商'
      })
    },
    handleDelete(row) {
      this.$confirm('是否确认删除供应商"' + row.supplierName + '"?', '警告', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        return delSupplier(row.supplierId)
      }).then(() => {
        this.getList()
        this.msgSuccess('删除成功')
      })
    },
    submitForm() {
      this.$refs['form'].validate(valid => {
        if (valid) {
          if (this.form.supplierId) {
            updateSupplier(this.form).then(() => {
              this.msgSuccess('修改成功')
              this.open = false
              this.getList()
            })
          } else {
            addSupplier(this.form).then(() => {
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
