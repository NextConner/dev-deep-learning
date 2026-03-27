# 工作流引擎系统

## 概述

工作流引擎是一个数据库驱动的可配置审批流程系统，用于替代硬编码的枚举状态机。

## 核心概念

### 工作流定义（Workflow Definition）
- 工作流模板，定义了完整的审批流程
- 包含工作流代码、名称、实体类型、版本号
- 可以有多个版本，通过is_active标识当前激活版本

### 工作流状态（Workflow State）
- 流程中的各个节点/阶段
- 状态类型：START（起始）、NORMAL（普通）、END（结束）
- 可配置超时时间（timeout_hours）

### 工作流转换（Workflow Transition）
- 定义状态之间的流转规则
- 支持条件表达式（condition_expression）：如"order.totalAmount > 10000"
- 支持角色权限控制（required_role）
- 支持优先级排序（priority）

### 工作流实例（Workflow Instance）
- 具体业务实体的流程运行实例
- 记录当前状态、启动时间、完成时间
- 每个业务实体（如订单）对应一个实例

### 工作流历史（Workflow History）
- 完整的状态变更审计日志
- 记录操作人、变更时间、备注信息

## 特性

### 动态条件分支
使用SpEL表达式实现条件判断：
- 高价值订单需要额外审批：`order.totalAmount > 10000`
- VIP客户跳过审核：`order.customerType == 'VIP'`
- 低库存触发预警：`inventory.quantity < 100`

### 角色权限控制
- 每个转换可指定required_role
- 只有具备相应角色的用户才能执行该转换
- 通过getAvailableTransitions获取当前用户可执行的操作

### 完整审计追踪
- 所有状态变更记录在workflow_history表
- 包含操作人、时间戳、备注
- 支持流程回溯和问题排查

## 启用方式

通过配置文件控制：
```yaml
workflow:
  engine:
    enabled: true  # 启用工作流引擎
```

默认为false，用于渐进式迁移。
