package com.claude.learn.agent;

import com.claude.learn.service.HybridSearchService;
import com.claude.learn.service.DatabaseQueryService;
import com.claude.learn.service.ToolPolicyGuardService;
import com.jtcool.oms.domain.OmsOrder;
import com.jtcool.oms.service.IOmsOrderService;
import com.jtcool.wms.domain.WmsInventory;
import com.jtcool.wms.service.IWmsInventoryService;
import com.jtcool.product.domain.PrdProduct;
import com.jtcool.product.service.IPrdProductService;
import com.jtcool.common.workflow.service.WorkflowEngine;
import com.jtcool.common.workflow.domain.WorkflowInstance;
import com.jtcool.common.workflow.domain.WorkflowTransition;
import com.jtcool.common.workflow.domain.TransitionResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.agent.tool.Tool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class AgentTools {


    private static final Logger log = LoggerFactory.getLogger(AgentTools.class);

    private final HybridSearchService hybridSearchService;
    private final ToolPolicyGuardService toolPolicyGuardService;
    private final DatabaseQueryService databaseQueryService;
    private final IOmsOrderService omsOrderService;
    private final IWmsInventoryService wmsInventoryService;
    private final IPrdProductService prdProductService;
    private final ObjectMapper objectMapper;
    private final WorkflowEngine workflowEngine;

    @Value("${workflow.engine.enabled:false}")
    private boolean workflowEngineEnabled;

    public AgentTools(HybridSearchService hybridSearchService,
                      ToolPolicyGuardService toolPolicyGuardService,
                      DatabaseQueryService databaseQueryService,
                      IOmsOrderService omsOrderService,
                      IWmsInventoryService wmsInventoryService,
                      IPrdProductService prdProductService,
                      ObjectMapper objectMapper,
                      WorkflowEngine workflowEngine) {
        this.hybridSearchService = hybridSearchService;
        this.toolPolicyGuardService = toolPolicyGuardService;
        this.databaseQueryService = databaseQueryService;
        this.omsOrderService = omsOrderService;
        this.wmsInventoryService = wmsInventoryService;
        this.prdProductService = prdProductService;
        this.objectMapper = objectMapper;
        this.workflowEngine = workflowEngine;
    }

    /**
     * tools 定义,需要在LLM回答之前，传递过去
     * @param query
     * @return
     */

    @Tool("查询公司内部政策文档。当用户询问差旅相关费用标准、报销流程、审批规则时调用此工具，例如：机票报销、酒店住宿标准、打车费用上限、餐饮报销、商务舱审批等问题")
    public String searchPolicy(String query) {
        toolPolicyGuardService.checkToolAccess("searchPolicy");
        log.info("调用者：{}", Thread.currentThread().getStackTrace()[2].getClassName());
        var results = hybridSearchService.hybridSearch(query, 3);
        return String.join("\n", results);
    }

    @Tool("查询指定城市的实时天气")
    public String getWeather(String city){
        toolPolicyGuardService.checkToolAccess("getWeather");
        // 这里可以调用天气API获取实时天气信息
        // 由于这是一个示例，我们将返回一个固定的字符串
        log.info("getWeather:{}",city);
        return "当前" + city + "的天气是晴天，温度25摄氏度。";
    }

    @Tool("发送邮件给指定收件人")
    public String sendEmail(String recipient) {
        toolPolicyGuardService.checkToolAccess("sendEmail");
        // 模拟发送邮件
        return "邮件已发送给：" + recipient;
    }

    @Tool("查询业务数据库。支持查询订单、库存、商品等业务数据。参数是标准的SQL SELECT语句，例如：SELECT * FROM oms_order WHERE order_status='PENDING' LIMIT 10")
    public String queryDatabase(String sqlQuery) {
        toolPolicyGuardService.checkToolAccess("queryDatabase");
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Database query by {}: {}", username, sqlQuery);
        return databaseQueryService.executeQuery(sqlQuery, username);
    }

    @Tool("查询订单信息。参数：订单ID（数字）")
    public String queryOrder(String orderId) {
        toolPolicyGuardService.checkToolAccess("queryOrder");
        try {
            Long id = Long.parseLong(orderId);
            OmsOrder order = omsOrderService.selectOmsOrderById(id);
            if (order == null) {
                return "订单不存在：" + orderId;
            }
            return objectMapper.writeValueAsString(order);
        } catch (Exception e) {
            log.error("查询订单失败", e);
            return "查询订单失败：" + e.getMessage();
        }
    }

    @Tool("创建新订单。参数：订单详情（JSON格式），包含customerId、items（productId、quantity、price）、deliveryAddress、contactPhone")
    public String createOrder(String orderDetails) {
        toolPolicyGuardService.checkToolAccess("createOrder");
        try {
            OmsOrder order = objectMapper.readValue(orderDetails, OmsOrder.class);
            int result = omsOrderService.insertOmsOrder(order);
            if (result > 0) {
                return "订单创建成功，订单ID：" + order.getOrderId();
            }
            return "订单创建失败";
        } catch (Exception e) {
            log.error("创建订单失败", e);
            return "创建订单失败：" + e.getMessage();
        }
    }

    @Tool("推进订单状态。参数：订单ID和目标状态（可选，如不指定则推进到下一个可用状态）")
    public String advanceOrderStatus(String orderId, String targetStatus) {
        toolPolicyGuardService.checkToolAccess("advanceOrderStatus");
        try {
            Long id = Long.parseLong(orderId);
            String operator = SecurityContextHolder.getContext().getAuthentication().getName();

            if (workflowEngineEnabled) {
                WorkflowInstance instance = workflowEngine.getWorkflowInstance("OMS_ORDER", id);
                if (instance == null) {
                    return "订单工作流实例不存在：" + orderId;
                }

                List<WorkflowTransition> transitions = workflowEngine.getAvailableTransitions(instance.getId(), operator);
                if (transitions.isEmpty()) {
                    return "该订单当前状态无法推进";
                }

                WorkflowTransition transition = transitions.get(0);
                TransitionResult result = workflowEngine.executeTransition(instance.getId(), transition.getTransitionName(), operator);

                return String.format("订单状态已更新: %s → %s",
                    result.getOldState().getStateName(),
                    result.getNewState().getStateName());
            } else {
                if (targetStatus == null || targetStatus.isEmpty()) {
                    return "请指定目标状态";
                }

                switch (targetStatus) {
                    case "SALES_CONFIRMED":
                        omsOrderService.confirmBySales(id, operator);
                        break;
                    case "ORDER_REVIEWED":
                        omsOrderService.reviewOrder(id, operator);
                        break;
                    case "WAREHOUSE_CONFIRMED":
                        omsOrderService.confirmByWarehouse(id, operator);
                        break;
                    case "OUTBOUND_REGISTERED":
                        omsOrderService.registerOutbound(id, operator);
                        break;
                    case "SHIPMENT_CONFIRMED":
                        omsOrderService.confirmShipment(id, operator, null);
                        break;
                    case "CUSTOMER_RECEIVED":
                        omsOrderService.confirmReceipt(id, operator);
                        break;
                    default:
                        return "无效的目标状态：" + targetStatus;
                }
                return "订单状态已更新为：" + targetStatus;
            }
        } catch (Exception e) {
            log.error("推进订单状态失败", e);
            return "推进订单状态失败：" + e.getMessage();
        }
    }

    @Tool("查询库存。参数：商品ID（数字）")
    public String queryInventory(String productId) {
        toolPolicyGuardService.checkToolAccess("queryInventory");
        try {
            Long id = Long.parseLong(productId);
            WmsInventory query = new WmsInventory();
            query.setProductId(id);
            List<WmsInventory> inventories = wmsInventoryService.selectWmsInventoryList(query);
            if (inventories.isEmpty()) {
                return "未找到商品库存：" + productId;
            }
            return objectMapper.writeValueAsString(inventories);
        } catch (Exception e) {
            log.error("查询库存失败", e);
            return "查询库存失败：" + e.getMessage();
        }
    }

    @Tool("查询商品信息。参数：商品ID（数字）")
    public String queryProduct(String productId) {
        toolPolicyGuardService.checkToolAccess("queryProduct");
        try {
            Long id = Long.parseLong(productId);
            PrdProduct product = prdProductService.selectPrdProductById(id);
            if (product == null) {
                return "商品不存在：" + productId;
            }
            return objectMapper.writeValueAsString(product);
        } catch (Exception e) {
            log.error("查询商品失败", e);
            return "查询商品失败：" + e.getMessage();
        }
    }

    @Tool("查询工作流定义和当前状态。参数：工作流代码（如ORDER_APPROVAL_V1）和实体ID（可选）")
    public String describeWorkflow(String workflowCode, String entityId) {
        toolPolicyGuardService.checkToolAccess("describeWorkflow");
        try {
            if (!workflowEngineEnabled) {
                return "工作流引擎未启用";
            }

            StringBuilder result = new StringBuilder();
            var workflow = workflowEngine.loadWorkflow(workflowCode);
            result.append("工作流：").append(workflow.getWorkflowName()).append("\n");
            result.append("代码：").append(workflow.getWorkflowCode()).append("\n");
            result.append("版本：").append(workflow.getVersion()).append("\n");

            if (entityId != null && !entityId.isEmpty()) {
                Long id = Long.parseLong(entityId);
                WorkflowInstance instance = workflowEngine.getWorkflowInstance(workflow.getEntityType(), id);
                if (instance != null) {
                    result.append("\n当前实例状态：").append(instance.getStatus()).append("\n");
                    List<WorkflowTransition> transitions = workflowEngine.getAvailableTransitions(
                        instance.getId(),
                        SecurityContextHolder.getContext().getAuthentication().getName()
                    );
                    result.append("可用操作：").append(transitions.size()).append("个\n");
                }
            }

            return result.toString();
        } catch (Exception e) {
            log.error("查询工作流失败", e);
            return "查询工作流失败：" + e.getMessage();
        }
    }
}
