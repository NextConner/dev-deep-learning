package com.jtcool.oms.service.impl;

import com.jtcool.common.enums.BillTypeEnum;
import com.jtcool.common.exception.ServiceException;
import com.jtcool.common.workflow.service.WorkflowEngine;
import com.jtcool.common.workflow.domain.WorkflowInstance;
import com.jtcool.common.workflow.domain.WorkflowTransition;
import com.jtcool.oms.domain.OmsOrder;
import com.jtcool.oms.domain.OmsOrderFlow;
import com.jtcool.oms.domain.OmsOrderItem;
import com.jtcool.oms.domain.enums.OrderStatusEnum;
import com.jtcool.oms.mapper.OmsOrderItemMapper;
import com.jtcool.oms.mapper.OmsOrderMapper;
import com.jtcool.oms.service.IOmsOrderFlowService;
import com.jtcool.oms.service.IOmsOrderService;
import com.jtcool.wms.domain.WmsStockBill;
import com.jtcool.wms.domain.WmsStockBillItem;
import com.jtcool.wms.service.IWmsStockBillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class OmsOrderServiceImpl implements IOmsOrderService {
    private static final Logger log = LoggerFactory.getLogger(OmsOrderServiceImpl.class);

    @Autowired
    private OmsOrderMapper omsOrderMapper;

    @Autowired
    private IOmsOrderFlowService orderFlowService;

    @Autowired
    private OmsOrderItemMapper orderItemMapper;

    @Autowired
    private IWmsStockBillService wmsStockBillService;

    @Autowired
    private WorkflowEngine workflowEngine;

    @Value("${workflow.engine.enabled:false}")
    private boolean workflowEngineEnabled;

    @Override
    public List<OmsOrder> selectOmsOrderList(OmsOrder omsOrder) {
        return omsOrderMapper.selectOmsOrderList(omsOrder);
    }

    @Override
    public OmsOrder selectOmsOrderById(Long orderId) {
        return omsOrderMapper.selectOmsOrderById(orderId);
    }

    @Override
    @Transactional
    public int insertOmsOrder(OmsOrder omsOrder) {
        int result = omsOrderMapper.insertOmsOrder(omsOrder);

        // Dual-write: Start workflow instance if engine is enabled
        if (workflowEngineEnabled && result > 0) {
            try {
                workflowEngine.startWorkflow("ORDER_APPROVAL_V1", "OMS_ORDER", omsOrder.getOrderId());
                log.info("Workflow instance started for order: {}", omsOrder.getOrderId());
            } catch (Exception e) {
                log.error("Failed to start workflow instance for order: {}", omsOrder.getOrderId(), e);
                // Don't fail the transaction - workflow is supplementary
            }
        }

        return result;
    }

    @Override
    public int updateOmsOrder(OmsOrder omsOrder) {
        return omsOrderMapper.updateOmsOrder(omsOrder);
    }

    @Override
    public int deleteOmsOrderByIds(Long[] orderIds) {
        return omsOrderMapper.deleteOmsOrderByIds(orderIds);
    }

    @Override
    public int updateOrderStatus(Long orderId, String status) {
        OmsOrder order = new OmsOrder();
        order.setOrderId(orderId);
        order.setOrderStatus(status);
        return omsOrderMapper.updateOmsOrder(order);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    private void transitionStatus(Long orderId, OrderStatusEnum targetStatus, String operator, String remark) {
        OmsOrder order = selectOmsOrderById(orderId);
        if (order == null) {
            throw new ServiceException("订单不存在");
        }

        OrderStatusEnum currentStatus = OrderStatusEnum.fromCode(order.getOrderStatus());
        if (!currentStatus.canTransitionTo(targetStatus.getCode())) {
            throw new ServiceException("无效的状态流转: " + currentStatus.getDescription() + " -> " + targetStatus.getDescription());
        }

        // Old system: Update order status directly
        order.setOrderStatus(targetStatus.getCode());
        updateOmsOrder(order);

        OmsOrderFlow flow = new OmsOrderFlow();
        flow.setOrderId(orderId);
        flow.setFlowStatus(targetStatus.getCode());
        flow.setOperatorName(operator);
        flow.setActionTime(new Date());
        flow.setRemark(remark);
        orderFlowService.insertOmsOrderFlow(flow);

        // Dual-write: Update workflow engine if enabled
        if (workflowEngineEnabled) {
            try {
                WorkflowInstance instance = workflowEngine.getWorkflowInstance("OMS_ORDER", orderId);
                if (instance != null) {
                    String transitionName = getTransitionName(targetStatus);
                    workflowEngine.executeTransition(instance.getId(), transitionName, operator);
                    log.info("Workflow transition executed: {} -> {}", currentStatus.getCode(), targetStatus.getCode());
                } else {
                    log.warn("Workflow instance not found for order: {}", orderId);
                }
            } catch (Exception e) {
                log.error("Failed to execute workflow transition for order: {}", orderId, e);
                // Don't fail the transaction - workflow is supplementary during migration
            }
        }

        if (targetStatus == OrderStatusEnum.WAREHOUSE_CONFIRMED) {
            createWmsOutboundBill(order);
        }
    }

    private String getTransitionName(OrderStatusEnum targetStatus) {
        switch (targetStatus) {
            case SALES_CONFIRMED: return "销售确认";
            case ORDER_REVIEWED: return "订单审核";
            case WAREHOUSE_CONFIRMED: return "仓库确认";
            case OUTBOUND_REGISTERED: return "出库登记";
            case SHIPMENT_CONFIRMED: return "发货确认";
            case CUSTOMER_RECEIVED: return "客户签收";
            default: throw new ServiceException("Unknown target status: " + targetStatus);
        }
    }

    private void createWmsOutboundBill(OmsOrder order) {
        List<OmsOrderItem> orderItems = orderItemMapper.selectOmsOrderItemByOrderId(order.getOrderId());
        if (orderItems == null || orderItems.isEmpty()) {
            throw new ServiceException("订单明细不存在，无法创建出库单");
        }

        WmsStockBill bill = new WmsStockBill();
        bill.setBillType(BillTypeEnum.OUT_SALES.getCode());
        bill.setBillDate(new Date());
        bill.setRelatedOrderId(order.getOrderId());
        bill.setCustomerId(order.getCustomerId());
        bill.setBillStatus("PENDING");

        List<WmsStockBillItem> billItems = new ArrayList<>();
        for (OmsOrderItem item : orderItems) {
            WmsStockBillItem billItem = new WmsStockBillItem();
            billItem.setProductId(item.getProductId());
            billItem.setQuantity(item.getQuantity());
            billItems.add(billItem);
        }
        bill.setItems(billItems);

        wmsStockBillService.insertWmsStockBill(bill);
    }

    @Override
    public void confirmBySales(Long orderId, String operator) {
        transitionStatus(orderId, OrderStatusEnum.SALES_CONFIRMED, operator, "销售确认");
    }

    @Override
    public void reviewOrder(Long orderId, String operator) {
        transitionStatus(orderId, OrderStatusEnum.ORDER_REVIEWED, operator, "订单审核通过");
    }

    @Override
    public void confirmByWarehouse(Long orderId, String operator) {
        transitionStatus(orderId, OrderStatusEnum.WAREHOUSE_CONFIRMED, operator, "仓库确认");
    }

    @Override
    public void registerOutbound(Long orderId, String operator) {
        transitionStatus(orderId, OrderStatusEnum.OUTBOUND_REGISTERED, operator, "登记出库");
    }

    @Override
    public void confirmShipment(Long orderId, String operator, String trackingNumber) {
        transitionStatus(orderId, OrderStatusEnum.SHIPMENT_CONFIRMED, operator, "确认发货，物流单号：" + trackingNumber);
    }

    @Override
    public void confirmReceipt(Long orderId, String operator) {
        transitionStatus(orderId, OrderStatusEnum.CUSTOMER_RECEIVED, operator, "客户签收");
    }
}
