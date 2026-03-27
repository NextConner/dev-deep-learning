package com.jtcool.oms.integration;

import com.jtcool.oms.domain.OmsOrder;
import com.jtcool.oms.domain.OmsOrderItem;
import com.jtcool.oms.service.IOmsOrderService;
import com.jtcool.oms.mapper.OmsOrderMapper;
import com.jtcool.wms.mapper.WmsStockBillMapper;
import com.jtcool.wms.domain.WmsStockBill;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class OrderWorkflowIntegrationTest {

    @Autowired
    private IOmsOrderService orderService;

    @Autowired
    private OmsOrderMapper orderMapper;

    @Autowired
    private WmsStockBillMapper stockBillMapper;

    @Test
    void testCompleteOrderWorkflow() {
        // 1. 创建订单
        OmsOrder order = new OmsOrder();
        order.setCustomerId(1L);
        order.setTotalAmount(new BigDecimal("1000.00"));

        List<OmsOrderItem> items = new ArrayList<>();
        OmsOrderItem item = new OmsOrderItem();
        item.setProductId(1L);
        item.setQuantity(10);
        item.setPrice(new BigDecimal("100.00"));
        items.add(item);
        order.setItems(items);

        orderService.insertOmsOrder(order);
        assertNotNull(order.getOrderId());
        assertEquals("PENDING", order.getOrderStatus());

        // 2. 销售确认
        orderService.confirmBySales(order.getOrderId(), "sales_user");
        OmsOrder afterSales = orderMapper.selectOmsOrderById(order.getOrderId());
        assertEquals("SALES_CONFIRMED", afterSales.getOrderStatus());

        // 3. 订单审核
        orderService.reviewOrder(order.getOrderId(), "reviewer");
        OmsOrder afterReview = orderMapper.selectOmsOrderById(order.getOrderId());
        assertEquals("REVIEWED", afterReview.getOrderStatus());

        // 4. 仓库确认 - 验证自动创建 WMS 出库单
        orderService.confirmByWarehouse(order.getOrderId(), "warehouse_user");
        OmsOrder afterWarehouse = orderMapper.selectOmsOrderById(order.getOrderId());
        assertEquals("WAREHOUSE_CONFIRMED", afterWarehouse.getOrderStatus());

        // 验证 WMS 出库单已创建
        WmsStockBill stockBill = stockBillMapper.selectWmsStockBillByOrderId(order.getOrderId());
        assertNotNull(stockBill, "WMS outbound bill should be created");
        assertEquals("OUT_SALES", stockBill.getBillType());

        // 5. 登记出库
        orderService.registerOutbound(order.getOrderId(), "warehouse_user");
        OmsOrder afterOutbound = orderMapper.selectOmsOrderById(order.getOrderId());
        assertEquals("OUT_REGISTERED", afterOutbound.getOrderStatus());

        // 6. 确认发货
        orderService.confirmShipment(order.getOrderId(), "warehouse_user", "TRACK123");
        OmsOrder afterShipment = orderMapper.selectOmsOrderById(order.getOrderId());
        assertEquals("SHIPPED", afterShipment.getOrderStatus());

        // 7. 客户签收
        orderService.confirmReceipt(order.getOrderId(), "customer");
        OmsOrder afterReceipt = orderMapper.selectOmsOrderById(order.getOrderId());
        assertEquals("RECEIVED", afterReceipt.getOrderStatus());
    }

    @Test
    void testOrderCreationWithInvalidData() {
        OmsOrder order = new OmsOrder();
        order.setCustomerId(null);
        order.setTotalAmount(new BigDecimal("-100.00"));

        assertThrows(Exception.class, () -> {
            orderService.insertOmsOrder(order);
        });
    }
}
