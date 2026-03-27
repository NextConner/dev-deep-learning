<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" :inline="true">
      <el-form-item label="变动类型" prop="changeType">
        <el-select v-model="queryParams.changeType" placeholder="请选择" clearable>
          <el-option label="入库" value="IN" />
          <el-option label="出库" value="OUT" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="logList">
      <el-table-column label="产品名称" prop="productName" />
      <el-table-column label="仓库" prop="warehouseName" />
      <el-table-column label="变动类型" prop="changeType" />
      <el-table-column label="变动数量" prop="changeQuantity" />
      <el-table-column label="变动前" prop="beforeQuantity" />
      <el-table-column label="变动后" prop="afterQuantity" />
      <el-table-column label="关联单号" prop="billNo" />
      <el-table-column label="变动时间" prop="createTime" width="180" />
    </el-table>

    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />
  </div>
</template>

<script>
import { listInventory } from '@/api/wms/inventory'

export default {
  name: 'InventoryLog',
  data() {
    return {
      loading: false,
      logList: [],
      total: 0,
      queryParams: { pageNum: 1, pageSize: 10, changeType: null }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listInventory(this.queryParams).then(response => {
        this.logList = response.rows
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
    }
  }
}
</script>
