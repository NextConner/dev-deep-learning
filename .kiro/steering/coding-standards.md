---
inclusion: fileMatch
fileMatchPattern: '**/*.{java,vue,js}'
---

# 代码规范

## 后端规范 (Java/Spring Boot)

### Controller 层
- 使用 `@RestController` 和 `@RequestMapping`
- 方法使用 `@GetMapping`、`@PostMapping` 等
- 返回类型统一使用 `AjaxResult`
- 参数校验使用 `@Validated` 和 `@RequestBody`

### Service 层
- 接口定义在 `service` 包，实现在 `service.impl` 包
- 事务方法添加 `@Transactional` 注解
- 业务异常使用 `ServiceException` 抛出

### 数据访问
- Mapper 接口使用 `@Mapper` 注解
- SQL 写在对应的 XML 文件中
- 复杂查询必须添加索引

## 前端规范 (Vue 3)

### API 调用
- 统一使用 `request.js` 封装的 axios
- API 函数命名：`list{Entity}`、`get{Entity}`、`add{Entity}`、`update{Entity}`、`del{Entity}`
- 导出功能调用：`export{Entity}`

### 组件开发
- 使用 Vue 3 Composition API (`<script setup>`)
- 表格使用 `<el-table>`，分页使用 `<el-pagination>`
- 表单使用 `<el-form>`，校验使用 `rules`

### 样式
- 使用 `<style scoped>` 避免样式污染
- 优先使用 Element Plus 组件样式

## 通用规范

### 大数据处理
- **导出功能必须使用异步导出**（Phase 2 已完成基础设施）
- 列表查询超过 1000 条记录时考虑分页优化
- 统计查询必须添加时间范围限制

### 错误处理
- 后端统一异常处理，返回标准错误格式
- 前端使用 `this.$modal.msgError()` 显示错误信息
