<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" :inline="true">
      <el-form-item label="产品名称" prop="productName">
        <el-input v-model="queryParams.productName" placeholder="请输入产品名称" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="inventoryList">
      <el-table-column label="产品名称" prop="productName" />
      <el-table-column label="仓库" prop="warehouseName" />
      <el-table-column label="库存数量" prop="quantity" />
      <el-table-column label="可用数量" prop="availableQuantity" />
      <el-table-column label="锁定数量" prop="lockedQuantity" />
      <el-table-column label="操作" align="center" width="100">
        <template slot-scope="scope">
          <el-button size="mini" type="text" @click="handleView(scope.row)">查看</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />
  </div>
</template>

<script>
import { listInventory } from '@/api/wms/inventory'

export default {
  name: 'Inventory',
  data() {
    return {
      loading: false,
      inventoryList: [],
      total: 0,
      queryParams: { pageNum: 1, pageSize: 10, productName: null }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listInventory(this.queryParams).then(response => {
        this.inventoryList = response.rows
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
    handleView(row) {
      this.$router.push('/wms/inventoryLog?inventoryId=' + row.inventoryId)
    }
  }
}
</script>
