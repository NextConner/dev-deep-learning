# 订单业务规则

## 订单创建规则

### 必填字段验证
- 客户ID必须存在且有效
- 至少包含一个商品
- 商品数量必须大于0
- 单价必须大于0
- 收货地址和联系电话不能为空

### 库存检查
- 创建订单时检查商品库存是否充足
- 如果库存不足，订单创建失败并提示缺货商品

### 价格计算
- 订单总金额 = Σ(商品单价 × 数量)
- 系统自动计算，不接受手动输入

## 状态转换规则

### 权限要求
- PLACED → SALES_CONFIRMED: 需要 ROLE_SALES
- SALES_CONFIRMED → ORDER_REVIEWED: 需要 ROLE_AUDITOR
- ORDER_REVIEWED → WAREHOUSE_CONFIRMED: 需要 ROLE_WAREHOUSE
- WAREHOUSE_CONFIRMED → OUTBOUND_REGISTERED: 需要 ROLE_WAREHOUSE
- OUTBOUND_REGISTERED → SHIPMENT_CONFIRMED: 需要 ROLE_LOGISTICS
- SHIPMENT_CONFIRMED → CUSTOMER_RECEIVED: 需要 ROLE_CUSTOMER_SERVICE

### 自动触发动作
- 仓库确认时：自动创建WMS出库单
- 确认发货时：自动发送通知给客户

## 订单金额规则

### 高价值订单
- 订单总金额 > 10000元：需要额外的财务审批（未来功能）
- 订单总金额 > 50000元：需要总经理审批（未来功能）

### 折扣规则
- VIP客户：自动享受95折
- 批量订单（数量>100）：可申请批量折扣

## 时效要求

### 处理时限
- 销售确认：24小时内
- 订单审核：12小时内
- 仓库确认：24小时内
- 发货：48小时内

超时订单会自动提醒相关负责人。

## 异常处理

### 订单取消
- 已下单、销售确认状态：可以取消
- 其他状态：需要走退货流程

### 退货流程
- 客户签收后7天内可申请退货
- 需要提供退货原因
- 审核通过后创建退货单

### 订单修改
- 已下单状态：可以修改商品和数量
- 销售确认后：不可修改，需要取消重新下单
