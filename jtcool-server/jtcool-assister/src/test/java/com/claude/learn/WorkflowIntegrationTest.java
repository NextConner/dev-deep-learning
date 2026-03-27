package com.claude.learn;

import com.jtcool.common.workflow.domain.WorkflowDefinition;
import com.jtcool.common.workflow.domain.WorkflowInstance;
import com.jtcool.common.workflow.domain.WorkflowTransition;
import com.jtcool.common.workflow.domain.TransitionResult;
import com.jtcool.common.workflow.service.WorkflowEngine;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("pgsql")
public class WorkflowIntegrationTest {

    @Autowired
    private WorkflowEngine workflowEngine;

    @Test
    public void testLoadWorkflow() {
        WorkflowDefinition workflow = workflowEngine.loadWorkflow("ORDER_APPROVAL_V1");

        assertNotNull(workflow);
        assertEquals("ORDER_APPROVAL_V1", workflow.getWorkflowCode());
        assertEquals("订单审批流程V1", workflow.getWorkflowName());
        assertEquals("OMS_ORDER", workflow.getEntityType());
        assertTrue(workflow.getIsActive());

        System.out.println("✓ Test 1: Workflow definition loaded successfully");
    }

    @Test
    public void testStartWorkflow() {
        Long testOrderId = 99999L;

        WorkflowInstance instance = workflowEngine.startWorkflow("ORDER_APPROVAL_V1", "OMS_ORDER", testOrderId);

        assertNotNull(instance);
        assertEquals("OMS_ORDER", instance.getEntityType());
        assertEquals(testOrderId, instance.getEntityId());
        assertEquals("RUNNING", instance.getStatus());
        assertNotNull(instance.getCurrentStateId());

        System.out.println("✓ Test 2: Workflow instance created successfully");
    }

    @Test
    public void testGetAvailableTransitions() {
        Long testOrderId = 88888L;
        WorkflowInstance instance = workflowEngine.startWorkflow("ORDER_APPROVAL_V1", "OMS_ORDER", testOrderId);

        List<WorkflowTransition> transitions = workflowEngine.getAvailableTransitions(instance.getId(), "ROLE_SALES");

        assertNotNull(transitions);
        assertFalse(transitions.isEmpty());
        assertEquals("销售确认", transitions.get(0).getTransitionName());

        System.out.println("✓ Test 3: Available transitions retrieved successfully");
    }

    @Test
    public void testExecuteTransition() {
        Long testOrderId = 77777L;
        WorkflowInstance instance = workflowEngine.startWorkflow("ORDER_APPROVAL_V1", "OMS_ORDER", testOrderId);

        TransitionResult result = workflowEngine.executeTransition(instance.getId(), "销售确认", "testUser");

        assertTrue(result.isSuccess());
        assertNotNull(result.getOldState());
        assertNotNull(result.getNewState());
        assertEquals("已下单", result.getOldState().getStateName());
        assertEquals("销售确认", result.getNewState().getStateName());

        System.out.println("✓ Test 4: Transition executed successfully");
    }

    @Test
    public void testGetWorkflowInstance() {
        Long testOrderId = 66666L;
        workflowEngine.startWorkflow("ORDER_APPROVAL_V1", "OMS_ORDER", testOrderId);

        WorkflowInstance instance = workflowEngine.getWorkflowInstance("OMS_ORDER", testOrderId);

        assertNotNull(instance);
        assertEquals(testOrderId, instance.getEntityId());
        assertEquals("RUNNING", instance.getStatus());

        System.out.println("✓ Test 5: Workflow instance retrieved successfully");
    }

    @Test
    public void testEndToEndOrderFlow() {
        Long testOrderId = 55555L;
        WorkflowInstance instance = workflowEngine.startWorkflow("ORDER_APPROVAL_V1", "OMS_ORDER", testOrderId);

        workflowEngine.executeTransition(instance.getId(), "销售确认", "salesUser");
        workflowEngine.executeTransition(instance.getId(), "订单审核", "reviewUser");
        workflowEngine.executeTransition(instance.getId(), "仓库确认", "warehouseUser");
        workflowEngine.executeTransition(instance.getId(), "出库登记", "warehouseUser");
        workflowEngine.executeTransition(instance.getId(), "发货确认", "logisticsUser");
        TransitionResult finalResult = workflowEngine.executeTransition(instance.getId(), "客户签收", "customerUser");

        assertTrue(finalResult.isSuccess());
        assertEquals("客户签收", finalResult.getNewState().getStateName());

        WorkflowInstance completedInstance = workflowEngine.getWorkflowInstance("OMS_ORDER", testOrderId);
        assertEquals("COMPLETED", completedInstance.getStatus());
        assertNotNull(completedInstance.getCompletedAt());

        System.out.println("✓ Test 6: End-to-end order flow completed successfully");
    }
}
