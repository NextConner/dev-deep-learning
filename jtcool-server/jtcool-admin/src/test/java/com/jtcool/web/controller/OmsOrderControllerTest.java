package com.jtcool.web.controller;

import com.jtcool.oms.domain.OmsOrder;
import com.jtcool.oms.service.IOmsOrderService;
import com.jtcool.web.controller.oms.OmsOrderController;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OmsOrderController.class)
class OmsOrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private IOmsOrderService orderService;

    @Test
    void testAddOrder_WithNullCustomerId_ShouldReturn400() throws Exception {
        OmsOrder order = new OmsOrder();
        order.setCustomerId(null);
        order.setTotalAmount(new BigDecimal("100.00"));

        mockMvc.perform(post("/oms/order")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(order)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testAddOrder_WithNegativeAmount_ShouldReturn400() throws Exception {
        OmsOrder order = new OmsOrder();
        order.setCustomerId(1L);
        order.setTotalAmount(new BigDecimal("-100.00"));

        mockMvc.perform(post("/oms/order")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(order)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testAddOrder_WithZeroAmount_ShouldReturn400() throws Exception {
        OmsOrder order = new OmsOrder();
        order.setCustomerId(1L);
        order.setTotalAmount(BigDecimal.ZERO);

        mockMvc.perform(post("/oms/order")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(order)))
                .andExpect(status().isBadRequest());
    }
}
