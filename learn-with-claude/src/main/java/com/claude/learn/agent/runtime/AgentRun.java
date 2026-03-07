package com.claude.learn.agent.runtime;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class AgentRun {

    private final String runId;
    private final String username;
    private final String userQuestion;
    private final LocalDateTime startedAt;
    private final List<AgentStep> steps;
    private AgentRunStatus status;
    private String finalAnswer;
    private LocalDateTime endedAt;

    public AgentRun(String username, String userQuestion) {
        this.runId = UUID.randomUUID().toString();
        this.username = username;
        this.userQuestion = userQuestion;
        this.startedAt = LocalDateTime.now();
        this.steps = new ArrayList<>();
        this.status = AgentRunStatus.RUNNING;
    }

    public AgentStep newStep(String toolName, String inputSummary) {
        AgentStep step = new AgentStep(steps.size() + 1, toolName, inputSummary);
        this.steps.add(step);
        return step;
    }

    public void markSuccess(String finalAnswer) {
        this.status = AgentRunStatus.SUCCESS;
        this.finalAnswer = finalAnswer;
        this.endedAt = LocalDateTime.now();
    }

    public void markFailed() {
        this.status = AgentRunStatus.FAILED;
        this.endedAt = LocalDateTime.now();
    }

    public long totalLatencyMs() {
        if (endedAt == null) {
            return 0L;
        }
        return Duration.between(startedAt, endedAt).toMillis();
    }

    public String getRunId() {
        return runId;
    }

    public String getUsername() {
        return username;
    }

    public String getUserQuestion() {
        return userQuestion;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public List<AgentStep> getSteps() {
        return Collections.unmodifiableList(steps);
    }

    public AgentRunStatus getStatus() {
        return status;
    }

    public String getFinalAnswer() {
        return finalAnswer;
    }

    public LocalDateTime getEndedAt() {
        return endedAt;
    }
}
