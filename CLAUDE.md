# JTCool 项目开发指南

## 项目概述
JTCool 是一个企业级 ERP 系统，包含订单管理（OMS）、仓库管理（WMS）和产品管理模块。

## 技术栈
- **后端**: Spring Boot 2.x + MyBatis + MySQL
- **前端**: Vue 3 + Element Plus + Vite
- **认证**: Spring Security + JWT

## 项目结构
```
dev-deep-learning/
├── jtcool-server/     # 后端 Spring Boot 项目
│   ├── jtcool-admin/  # 主应用
│   ├── jtcool-common/ # 公共模块
│   └── jtcool-framework/ # 框架模块
└── jtcool-front/      # 前端 Vue 项目
```

## 核心模块
- **OMS**: 订单管理、客户管理、财务管理
- **WMS**: 仓库管理、库存管理、出入库单据
- **Product**: 产品、品牌、供应商、分类管理
- **System**: 用户、角色、权限、菜单管理

## 已完成的优化
✅ Phase 1: 连接池优化（支持 60 并发）
✅ Phase 2: 异步导出基础设施
✅ Phase 3: OMS/WMS 导出优化

## 开发新功能时的关键约束
1. **导出功能必须使用异步导出**（不要用同步方式）
2. **数据库查询必须添加索引**
3. **遵循现有的模块结构和命名规范**
4. **统计查询必须添加时间范围限制**

## 快速开始

### 后端开发
1. Controller → Service → Mapper → XML
2. 使用 `AjaxResult` 统一返回格式
3. Service 方法添加 `@Transactional`

### 前端开发
1. API 定义 → View 页面 → Router 注册
2. 使用 Composition API (`<script setup>`)
3. 表格用 `el-table`，表单用 `el-form`

## 更多约束
详细的架构约束、代码规范和性能要求请查看 `.kiro/steering/` 目录。
