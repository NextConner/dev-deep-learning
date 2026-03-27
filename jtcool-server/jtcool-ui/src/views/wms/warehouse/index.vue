<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" :inline="true">
      <el-form-item label="仓库名称" prop="warehouseName">
        <el-input v-model="queryParams.warehouseName" placeholder="请输入仓库名称" clearable @keyup.enter.native="handleQuery" />
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

    <el-table v-loading="loading" :data="warehouseList">
      <el-table-column label="仓库名称" prop="warehouseName" />
      <el-table-column label="仓库编码" prop="warehouseCode" />
      <el-table-column label="地址" prop="address" />
      <el-table-column label="状态" prop="status" />
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
        <el-form-item label="仓库名称" prop="warehouseName">
          <el-input v-model="form.warehouseName" placeholder="请输入仓库名称" />
        </el-form-item>
        <el-form-item label="仓库编码" prop="warehouseCode">
          <el-input v-model="form.warehouseCode" placeholder="请输入仓库编码" />
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input v-model="form.address" placeholder="请输入地址" />
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
import { listWarehouse, getWarehouse, addWarehouse, updateWarehouse, delWarehouse } from '@/api/wms/warehouse'

export default {
  name: 'Warehouse',
  data() {
    return {
      loading: false,
      warehouseList: [],
      total: 0,
      title: '',
      open: false,
      queryParams: { pageNum: 1, pageSize: 10, warehouseName: null },
      form: {},
      rules: {
        warehouseName: [{ required: true, message: '仓库名称不能为空', trigger: 'blur' }]
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listWarehouse(this.queryParams).then(response => {
        this.warehouseList = response.rows
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
      this.title = '添加仓库'
    },
    handleUpdate(row) {
      this.reset()
      getWarehouse(row.warehouseId).then(response => {
        this.form = response.data
        this.open = true
        this.title = '修改仓库'
      })
    },
    handleDelete(row) {
      this.$confirm('是否确认删除仓库"' + row.warehouseName + '"?', '警告', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        return delWarehouse(row.warehouseId)
      }).then(() => {
        this.getList()
        this.msgSuccess('删除成功')
      })
    },
    submitForm() {
      this.$refs['form'].validate(valid => {
        if (valid) {
          if (this.form.warehouseId) {
            updateWarehouse(this.form).then(() => {
              this.msgSuccess('修改成功')
              this.open = false
              this.getList()
            })
          } else {
            addWarehouse(this.form).then(() => {
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
