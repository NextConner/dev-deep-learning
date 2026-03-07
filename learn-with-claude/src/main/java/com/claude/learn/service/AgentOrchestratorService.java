package com.claude.learn.service;

import com.claude.learn.agent.PolicyAgent;
import com.claude.learn.agent.runtime.AgentErrorCode;
import com.claude.learn.agent.runtime.AgentRun;
import com.claude.learn.agent.runtime.AgentStep;
import com.claude.learn.config.AgentRuntimeProperties;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
public class AgentOrchestratorService {

    private final PolicyAgent policyAgent;
    private final AgentRuntimeProperties runtimeProperties;
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    public AgentOrchestratorService(PolicyAgent policyAgent, AgentRuntimeProperties runtimeProperties) {
        this.policyAgent = policyAgent;
        this.runtimeProperties = runtimeProperties;
    }

    public AgentRun run(String username, String userMessage, String systemPrompt) {
        AgentRun run = new AgentRun(username, userMessage);
        String workingMessage = plan(userMessage);

        for (int i = 0; i < runtimeProperties.getMaxSteps(); i++) {
            AgentStep step = run.newStep("policy_agent", summarize(workingMessage));
            step.start();

            String answer = actWithRetry(step, workingMessage, systemPrompt);
            if (answer != null) {
                observe(step, answer);
                run.markSuccess(answer);
                return run;
            }

            workingMessage = reflect(userMessage, run);
        }

        AgentStep guardStep = run.newStep("runtime_guard", "max steps exceeded");
        guardStep.start();
        guardStep.markFailed(AgentErrorCode.MAX_STEPS_EXCEEDED, "Agent reached max steps without a valid answer");
        run.markFailed();
        return run;
    }

    private String plan(String userMessage) {
        return userMessage;
    }

    private String actWithRetry(AgentStep step, String message, String systemPrompt) {
        AgentErrorCode lastErrorCode = AgentErrorCode.TOOL_EXECUTION_FAIL;
        String lastErrorMessage = "Unknown error";

        for (int attempt = 0; attempt <= runtimeProperties.getRetryTimes(); attempt++) {
            try {
                return invokeWithTimeout(message, systemPrompt);
            } catch (TimeoutException e) {
                lastErrorCode = AgentErrorCode.TOOL_TIMEOUT;
                lastErrorMessage = "Step timeout after " + runtimeProperties.getStepTimeoutMs() + "ms";
            } catch (Exception e) {
                lastErrorCode = AgentErrorCode.TOOL_EXECUTION_FAIL;
                lastErrorMessage = e.getMessage() == null ? "Tool execution failed" : e.getMessage();
            }
        }

        if (lastErrorCode == AgentErrorCode.TOOL_TIMEOUT) {
            step.markTimeout(lastErrorMessage);
        } else {
            step.markFailed(lastErrorCode, lastErrorMessage);
        }
        return null;
    }

    private String invokeWithTimeout(String message, String systemPrompt) throws Exception {
        Future<String> future = executor.submit(() -> policyAgent.chat(message, systemPrompt));
        try {
            return future.get(runtimeProperties.getStepTimeoutMs(), TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            throw e;
        }
    }

    private void observe(AgentStep step, String answer) {
        step.markSuccess(summarize(answer));
    }

    private String reflect(String originalQuestion, AgentRun run) {
        return """
                User question: %s
                Previous execution failed. Please provide a direct, complete answer.
                """.formatted(originalQuestion);
    }

    private String summarize(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }
        int limit = 160;
        return text.length() <= limit ? text : text.substring(0, limit) + "...";
    }
}
