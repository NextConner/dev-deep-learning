package com.jtcool.common.workflow.service;

import com.jtcool.common.workflow.domain.*;
import com.jtcool.common.workflow.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class WorkflowEngine {

    @Autowired
    private WorkflowDefinitionRepository workflowDefinitionRepository;

    @Autowired
    private WorkflowStateRepository workflowStateRepository;

    @Autowired
    private WorkflowTransitionRepository workflowTransitionRepository;

    @Autowired
    private WorkflowInstanceRepository workflowInstanceRepository;

    @Autowired
    private WorkflowHistoryRepository workflowHistoryRepository;

    @Autowired
    private WorkflowConditionEvaluator conditionEvaluator;

    public WorkflowDefinition loadWorkflow(String workflowCode) {
        return workflowDefinitionRepository.findByWorkflowCodeAndIsActive(workflowCode, true)
                .orElseThrow(() -> new RuntimeException("Workflow not found: " + workflowCode));
    }

    @Transactional
    public WorkflowInstance startWorkflow(String workflowCode, String entityType, Long entityId) {
        WorkflowDefinition workflow = loadWorkflow(workflowCode);

        List<WorkflowState> states = workflowStateRepository.findByWorkflowId(workflow.getId());
        WorkflowState initialState = states.stream()
                .filter(s -> "START".equals(s.getStateType()))
                .findFirst()
                .orElse(states.get(0));

        WorkflowInstance instance = new WorkflowInstance();
        instance.setWorkflowId(workflow.getId());
        instance.setEntityType(entityType);
        instance.setEntityId(entityId);
        instance.setCurrentStateId(initialState.getId());
        instance.setStatus("RUNNING");
        instance.setStartedAt(new Date());

        return workflowInstanceRepository.save(instance);
    }

    @Transactional
    public TransitionResult executeTransition(Long instanceId, String transitionName, String operator) {
        WorkflowInstance instance = workflowInstanceRepository.findById(instanceId)
                .orElseThrow(() -> new RuntimeException("Workflow instance not found"));

        WorkflowState currentState = workflowStateRepository.findById(instance.getCurrentStateId())
                .orElseThrow(() -> new RuntimeException("Current state not found"));

        List<WorkflowTransition> transitions = workflowTransitionRepository
                .findByWorkflowIdAndFromStateIdOrderByPriorityAsc(instance.getWorkflowId(), currentState.getId());

        WorkflowTransition transition = transitions.stream()
                .filter(t -> t.getTransitionName().equals(transitionName))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Transition not found: " + transitionName));

        WorkflowState newState = workflowStateRepository.findById(transition.getToStateId())
                .orElseThrow(() -> new RuntimeException("Target state not found"));

        instance.setCurrentStateId(newState.getId());
        if ("END".equals(newState.getStateType())) {
            instance.setStatus("COMPLETED");
            instance.setCompletedAt(new Date());
        }
        workflowInstanceRepository.save(instance);

        WorkflowHistory history = new WorkflowHistory();
        history.setInstanceId(instanceId);
        history.setFromStateId(currentState.getId());
        history.setToStateId(newState.getId());
        history.setOperator(operator);
        history.setTransitionTime(new Date());
        workflowHistoryRepository.save(history);

        return new TransitionResult(currentState, newState, true, "Transition successful");
    }

    public List<WorkflowTransition> getAvailableTransitions(Long instanceId, String userRole) {
        WorkflowInstance instance = workflowInstanceRepository.findById(instanceId)
                .orElseThrow(() -> new RuntimeException("Workflow instance not found"));

        List<WorkflowTransition> transitions = workflowTransitionRepository
                .findByWorkflowIdAndFromStateIdOrderByPriorityAsc(instance.getWorkflowId(), instance.getCurrentStateId());

        return transitions.stream()
                .filter(t -> t.getRequiredRole() == null || t.getRequiredRole().isEmpty() || t.getRequiredRole().equals(userRole))
                .collect(Collectors.toList());
    }

    public WorkflowInstance getWorkflowInstance(String entityType, Long entityId) {
        return workflowInstanceRepository.findByEntityTypeAndEntityId(entityType, entityId)
                .orElse(null);
    }
}
