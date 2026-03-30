---
inclusion: fileMatch
fileMatchPattern: '**/*.java'
---

# TDD 开发规范（后端）

## TDD 核心原则

**Test-Driven Development (TDD)** 是强制性的开发方法，遵循 Red-Green-Refactor 循环：

1. **Red**: 先写失败的测试
2. **Green**: 写最小代码让测试通过
3. **Refactor**: 重构代码，保持测试通过

## 开发流程约束

### 新功能开发（强制 TDD）

**禁止**在没有测试的情况下编写业务代码。必须按以下顺序：

1. **编写测试用例**
   - 在 `src/test/java` 对应包下创建测试类
   - 测试类命名：`{类名}Test.java`（如 `OmsOrderServiceTest.java`）
   - 先写测试，此时测试应该失败（Red）

2. **实现最小代码**
   - 编写刚好让测试通过的代码（Green）
   - 不要过度设计，不要添加未测试的功能

3. **重构优化**
   - 在测试保护下重构代码（Refactor）
   - 每次重构后运行测试确保通过

## 测试覆盖率要求

### 强制测试的代码

- **Service 层**：所有业务逻辑方法必须有单元测试（覆盖率 ≥ 80%）
- **Controller 层**：关键接口必须有集成测试
- **复杂算法**：边界条件、异常情况必须全覆盖

### 可选测试的代码

- **Mapper 层**：简单 CRUD 可不测试，复杂 SQL 必须测试
- **Entity/DTO**：简单 POJO 可不测试

## 测试组织结构

### 测试类结构

```java
@SpringBootTest
class OmsOrderServiceTest {

    @Autowired
    private OmsOrderService orderService;

    @MockBean
    private OmsOrderMapper orderMapper;

    // 测试方法按功能分组
    @Nested
    @DisplayName("创建订单测试")
    class CreateOrderTests {

        @Test
        @DisplayName("正常创建订单")
        void shouldCreateOrderSuccessfully() {
            // Given - 准备测试数据
            // When - 执行被测试方法
            // Then - 验证结果
        }

        @Test
        @DisplayName("库存不足时创建订单应失败")
        void shouldFailWhenStockInsufficient() {
            // 测试异常情况
        }
    }
}
```

## 测试命名规范

### 测试方法命名

使用 `should...When...` 或 `given...When...Then...` 模式：

- `shouldReturnOrderWhenOrderExists()` - 当订单存在时应返回订单
- `shouldThrowExceptionWhenOrderNotFound()` - 当订单不存在时应抛出异常
- `givenValidOrder_whenCreate_thenReturnSuccess()` - 给定有效订单，创建时，返回成功

### @DisplayName 注解

使用中文描述测试意图，便于阅读测试报告：

```java
@Test
@DisplayName("当库存充足时，创建订单应该成功并扣减库存")
void shouldCreateOrderAndReduceStockWhenStockSufficient() {
    // 测试代码
}
```

## Mock 和依赖处理

### Mock 原则

- **Mock 外部依赖**：数据库、外部 API、消息队列等
- **不 Mock 被测试类**：只 Mock 被测试类的依赖
- **使用 @MockBean**：Spring Boot 测试中使用 `@MockBean` 替换真实 Bean

### 示例

```java
@SpringBootTest
class OmsOrderServiceTest {

    @Autowired
    private OmsOrderService orderService; // 被测试对象，不 Mock

    @MockBean
    private OmsOrderMapper orderMapper; // 依赖，Mock

    @MockBean
    private WmsInventoryService inventoryService; // 外部服务，Mock

    @Test
    void shouldCreateOrderSuccessfully() {
        // Given
        when(inventoryService.checkStock(anyLong())).thenReturn(true);
        when(orderMapper.insert(any())).thenReturn(1);

        // When
        AjaxResult result = orderService.createOrder(orderDto);

        // Then
        assertEquals(200, result.get("code"));
        verify(inventoryService).checkStock(anyLong());
        verify(orderMapper).insert(any());
    }
}
```

## TDD 实践要点

### 测试先行（强制）

**错误做法** ❌：
1. 先写 Service 实现
2. 再写测试

**正确做法** ✅：
1. 先写测试（此时编译失败或测试失败）
2. 写最小实现让测试通过
3. 重构优化

### 小步前进

- 每次只测试一个行为
- 每次只让一个测试通过
- 频繁运行测试（每次代码改动后）

### 测试独立性

- 每个测试方法独立运行
- 不依赖测试执行顺序
- 使用 `@BeforeEach` 准备测试数据

## 与项目集成

### 已完成的 TDD 实践

根据项目提交历史：
- ✅ Phase 2 已补充单元测试
- 新功能开发必须延续 TDD 实践

### 测试运行

```bash
# 运行所有测试
mvn test

# 运行特定测试类
mvn test -Dtest=OmsOrderServiceTest

# 查看测试覆盖率
mvn clean test jacoco:report
```

## 注意事项

1. **不要跳过测试**：即使是简单功能也要先写测试
2. **测试要快**：单元测试应在毫秒级完成
3. **测试要清晰**：测试代码比生产代码更重要，要易读易维护
4. **持续重构**：在测试保护下大胆重构

## 违反 TDD 的后果

- 代码审查不通过
- 功能不允许合并到主分支
- 增加后期维护成本和 Bug 风险
