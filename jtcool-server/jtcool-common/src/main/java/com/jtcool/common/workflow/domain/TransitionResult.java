package com.jtcool.common.workflow.domain;

public class TransitionResult {
    private WorkflowState oldState;
    private WorkflowState newState;
    private boolean success;
    private String message;

    public TransitionResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public TransitionResult(WorkflowState oldState, WorkflowState newState, boolean success, String message) {
        this.oldState = oldState;
        this.newState = newState;
        this.success = success;
        this.message = message;
    }

    public WorkflowState getOldState() {
        return oldState;
    }

    public void setOldState(WorkflowState oldState) {
        this.oldState = oldState;
    }

    public WorkflowState getNewState() {
        return newState;
    }

    public void setNewState(WorkflowState newState) {
        this.newState = newState;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
