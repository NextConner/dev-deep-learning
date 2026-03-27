package com.jtcool.common.workflow.domain;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "workflow_transition")
public class WorkflowTransition implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "workflow_id", nullable = false)
    private Long workflowId;

    @Column(name = "from_state_id")
    private Long fromStateId;

    @Column(name = "to_state_id", nullable = false)
    private Long toStateId;

    @Column(name = "transition_name", nullable = false, length = 100)
    private String transitionName;

    @Column(name = "condition_expression", length = 500)
    private String conditionExpression;

    @Column(name = "required_role", length = 50)
    private String requiredRole;

    @Column(name = "action_bean", length = 100)
    private String actionBean;

    @Column(name = "priority")
    private Integer priority = 0;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(Long workflowId) {
        this.workflowId = workflowId;
    }

    public Long getFromStateId() {
        return fromStateId;
    }

    public void setFromStateId(Long fromStateId) {
        this.fromStateId = fromStateId;
    }

    public Long getToStateId() {
        return toStateId;
    }

    public void setToStateId(Long toStateId) {
        this.toStateId = toStateId;
    }

    public String getTransitionName() {
        return transitionName;
    }

    public void setTransitionName(String transitionName) {
        this.transitionName = transitionName;
    }

    public String getConditionExpression() {
        return conditionExpression;
    }

    public void setConditionExpression(String conditionExpression) {
        this.conditionExpression = conditionExpression;
    }

    public String getRequiredRole() {
        return requiredRole;
    }

    public void setRequiredRole(String requiredRole) {
        this.requiredRole = requiredRole;
    }

    public String getActionBean() {
        return actionBean;
    }

    public void setActionBean(String actionBean) {
        this.actionBean = actionBean;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }
}
