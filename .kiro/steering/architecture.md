# 项目架构约束

## 模块划分

### 核心业务模块
- **OMS**: 订单管理系统 (Order Management System) - 订单、客户、财务
- **WMS**: 仓库管理系统 (Warehouse Management System) - 仓库、库存、出入库
- **Product**: 产品管理 - 产品、品牌、供应商、分类
- **Statistics**: 统计分析 - 通用统计、OMS统计、WMS统计

### 系统支撑模块
- **System**: 系统管理 - 用户、角色、权限、菜单、字典
- **Monitor**: 系统监控 - 在线用户、日志、缓存、服务器监控
- **Tool**: 开发工具 - 代码生成器、表单构建、Swagger文档

## 后端结构 (Spring Boot)

### 模块组织
```
jtcool-server/
├── jtcool-admin/          # 主应用模块（启动入口）
│   └── controller/        # 控制器层（所有业务Controller）
├── jtcool-oms/            # OMS业务模块
├── jtcool-wms/            # WMS业务模块
├── jtcool-product/        # 产品业务模块
├── jtcool-system/         # 系统业务模块
├── jtcool-common/         # 公共模块
├── jtcool-framework/      # 框架模块（安全、配置等）
├── jtcool-generator/      # 代码生成器
├── jtcool-quartz/         # 定时任务
└── jtcool-assister/       # AI助手模块（实验性）
```

### 模块说明
- **业务模块**（oms/wms/product/system）：包含各自的 domain、service、mapper
- **jtcool-admin**：统一的 Controller 层和应用启动入口
- **jtcool-common**：通用工具类、常量、异常定义
- **jtcool-framework**：Spring Security、Redis、MyBatis 配置

**分层规范**:
- **Controller**: 接收请求，参数校验，调用 Service
- **Service**: 业务逻辑，事务管理
- **Mapper**: MyBatis 数据访问层
- **统一返回**: 使用 `AjaxResult` 包装响应

## 前端结构 (Vue 3)
```
jtcool-front/src/
├── api/              # API 接口定义
├── views/            # 页面视图
├── components/       # 可复用组件
└── router/           # 路由配置
```

## 新功能开发标准流程

### 后端开发顺序
1. Controller: 定义接口路径和参数
2. Service: 实现业务逻辑
3. Mapper: 编写 SQL 映射
4. XML: MyBatis SQL 配置

### 前端开发顺序
1. API: 在 `src/api/` 定义接口调用
2. View: 在 `src/views/` 创建页面
3. Component: 如需复用，提取到 `src/components/`
4. Router: 在 `router/index.js` 注册路由

## 命名规范
- Controller: `{模块}{功能}Controller.java` (如 `OmsOrderController`)
- API 文件: `{模块}/{功能}.js` (如 `oms/order.js`)
- View 文件: `{模块}/{功能}/index.vue` (如 `oms/order/index.vue`)
