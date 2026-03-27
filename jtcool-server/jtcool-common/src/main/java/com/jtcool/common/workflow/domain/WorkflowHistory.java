package com.jtcool.common.workflow.domain;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "workflow_history")
public class WorkflowHistory implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "instance_id", nullable = false)
    private Long instanceId;

    @Column(name = "from_state_id")
    private Long fromStateId;

    @Column(name = "to_state_id", nullable = false)
    private Long toStateId;

    @Column(name = "operator", nullable = false, length = 50)
    private String operator;

    @Column(name = "transition_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date transitionTime;

    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(Long instanceId) {
        this.instanceId = instanceId;
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

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public Date getTransitionTime() {
        return transitionTime;
    }

    public void setTransitionTime(Date transitionTime) {
        this.transitionTime = transitionTime;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
