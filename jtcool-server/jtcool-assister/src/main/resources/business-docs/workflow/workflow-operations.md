# 工作流操作指南

## 可用工具

### advanceOrderStatus - 推进订单状态

**功能：** 将订单推进到下一个状态

**参数：**
- orderId：订单ID（必填）
- targetStatus：目标状态（可选，工作流引擎启用时自动选择下一个可用状态）

**使用示例：**
- "帮我把订单12345推进到下一步"
- "推进订单12345的状态"

**工作流引擎模式：**
- 自动获取当前可用的转换
- 验证用户权限
- 执行状态转换
- 记录审计日志

**传统模式（工作流引擎未启用）：**
需要明确指定目标状态：
- SALES_CONFIRMED - 销售确认
- ORDER_REVIEWED - 订单审核
- WAREHOUSE_CONFIRMED - 仓库确认
- OUTBOUND_REGISTERED - 出库登记
- SHIPMENT_CONFIRMED - 发货确认
- CUSTOMER_RECEIVED - 客户签收

### describeWorkflow - 查询工作流

**功能：** 查询工作流定义和当前实例状态

**参数：**
- workflowCode：工作流代码（如ORDER_APPROVAL_V1）
- entityId：实体ID（可选，用于查询具体实例状态）

**使用示例：**
- "查询订单审批工作流"
- "订单12345的工作流状态是什么"

**返回信息：**
- 工作流名称和版本
- 当前实例状态
- 可用操作数量

## 工作流状态说明

### 订单审批流程（ORDER_APPROVAL_V1）

标准7状态流程：
1. PLACED - 已下单（起始状态）
2. SALES_CONFIRMED - 销售确认
3. ORDER_REVIEWED - 订单审核
4. WAREHOUSE_CONFIRMED - 仓库确认
5. OUTBOUND_REGISTERED - 出库登记
6. SHIPMENT_CONFIRMED - 发货确认
7. CUSTOMER_RECEIVED - 客户签收（结束状态）

### 条件分支示例

高价值订单（总金额>10000）：
- SALES_CONFIRMED → FINANCE_APPROVED（财务审批）→ ORDER_REVIEWED

VIP客户订单：
- SALES_CONFIRMED → ORDER_REVIEWED（跳过常规审核）

## 权限控制

不同角色可执行的操作：
- 销售人员：PLACED → SALES_CONFIRMED
- 审核人员：SALES_CONFIRMED → ORDER_REVIEWED
- 仓库人员：ORDER_REVIEWED → WAREHOUSE_CONFIRMED
- 物流人员：WAREHOUSE_CONFIRMED → OUTBOUND_REGISTERED → SHIPMENT_CONFIRMED
- 客户：SHIPMENT_CONFIRMED → CUSTOMER_RECEIVED

## 常见问题

**Q: 如何查看订单当前可以执行哪些操作？**
A: 使用describeWorkflow工具，传入订单ID，会显示当前可用操作数量。

**Q: 推进状态失败怎么办？**
A: 检查：
1. 订单是否存在
2. 当前用户是否有权限
3. 当前状态是否允许转换
4. 是否满足条件表达式（如金额限制）

**Q: 如何回退订单状态？**
A: 工作流引擎默认不支持回退，需要配置反向转换规则。
