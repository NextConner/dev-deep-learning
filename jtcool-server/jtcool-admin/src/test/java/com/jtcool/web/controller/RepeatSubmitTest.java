package com.jtcool.web.controller;

import com.jtcool.oms.domain.OmsOrder;
import com.jtcool.oms.service.IOmsOrderService;
import com.jtcool.web.controller.oms.OmsOrderController;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RepeatSubmitTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private IOmsOrderService orderService;

    @Test
    void testRepeatSubmit_ShouldBeRejectedWithinInterval() throws Exception {
        OmsOrder order = new OmsOrder();
        order.setCustomerId(1L);
        order.setTotalAmount(new BigDecimal("100.00"));

        when(orderService.insertOmsOrder(any())).thenReturn(1);

        String orderJson = objectMapper.writeValueAsString(order);

        // 第一次提交应该成功
        mockMvc.perform(post("/oms/order")
                .contentType(MediaType.APPLICATION_JSON)
                .content(orderJson))
                .andExpect(status().isOk());

        // 立即第二次提交相同请求应该被拒绝（在5秒间隔内）
        mockMvc.perform(post("/oms/order")
                .contentType(MediaType.APPLICATION_JSON)
                .content(orderJson))
                .andExpect(status().is4xxClientError());

        // 验证服务方法只被调用一次
        verify(orderService, times(1)).insertOmsOrder(any());
    }

    @Test
    void testRepeatSubmit_DifferentRequests_ShouldBothSucceed() throws Exception {
        when(orderService.insertOmsOrder(any())).thenReturn(1);

        OmsOrder order1 = new OmsOrder();
        order1.setCustomerId(1L);
        order1.setTotalAmount(new BigDecimal("100.00"));

        OmsOrder order2 = new OmsOrder();
        order2.setCustomerId(2L);
        order2.setTotalAmount(new BigDecimal("200.00"));

        // 两个不同的请求都应该成功
        mockMvc.perform(post("/oms/order")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(order1)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/oms/order")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(order2)))
                .andExpect(status().isOk());

        verify(orderService, times(2)).insertOmsOrder(any());
    }
}
