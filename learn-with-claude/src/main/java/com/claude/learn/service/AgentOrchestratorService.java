package com.claude.learn.service;

import com.claude.learn.agent.PolicyAgent;
import com.claude.learn.agent.runtime.AgentErrorCode;
import com.claude.learn.agent.runtime.AgentRun;
import com.claude.learn.agent.runtime.AgentStep;
import com.claude.learn.config.AgentRuntimeProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Agent编排服务 - 负责管理Agent的执行流程
 *
 * 核心职责：
 * 1. 控制Agent执行循环（最多maxSteps步）
 * 2. 实现超时控制和重试机制
 * 3. 提供完整的可观测性（日志、traceId、执行轨迹）
 * 4. 处理执行失败和异常情况
 *
 * 设计模式：
 * - Plan-Act-Observe-Reflect循环
 * - 使用Virtual Thread提高并发性能
 * - MDC实现分布式追踪
 */
@Service
public class AgentOrchestratorService {

    private static final Logger log = LoggerFactory.getLogger(AgentOrchestratorService.class);

    private final PolicyAgent policyAgent;
    private final AgentRuntimeProperties runtimeProperties;
    // 使用Virtual Thread执行器，Java 21新特性，轻量级线程，适合高并发场景
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    public AgentOrchestratorService(PolicyAgent policyAgent, AgentRuntimeProperties runtimeProperties) {
        this.policyAgent = policyAgent;
        this.runtimeProperties = runtimeProperties;
    }

    /**
     * 执行Agent运行的主方法
     *
     * @param username 用户名，用于审计和配额控制
     * @param userMessage 用户问题
     * @param systemPrompt 系统提示词，定义Agent行为
     * @return AgentRun 包含完整执行轨迹的运行记录
     */
    public AgentRun run(String username, String userMessage, String systemPrompt) {
        AgentRun run = new AgentRun(username, userMessage);
        String traceId = run.getRunId();

        // 使用SLF4J的MDC（Mapped Diagnostic Context）实现请求级别的上下文传递
        // MDC中的数据会自动出现在所有日志中，便于分布式追踪
        MDC.put("traceId", traceId);
        MDC.put("username", username);

        try {
            log.info("Starting agent run - runId: {}, question: {}", traceId, summarize(userMessage));

            // Plan阶段：分析用户问题（当前实现为直接返回，未来可扩展为问题分解）
            String workingMessage = plan(userMessage);

            // Agent执行循环：最多执行maxSteps步，防止无限循环
            for (int i = 0; i < runtimeProperties.getMaxSteps(); i++) {
                log.debug("Starting step {} of {}", i + 1, runtimeProperties.getMaxSteps());

                // 创建新的执行步骤
                AgentStep step = run.newStep("policy_agent", summarize(workingMessage));
                step.start();
                log.info("Step {} started - stepId: {}, tool: {}", step.getSequence(), step.getStepId(), step.getToolName());

                // Act阶段：执行工具调用，带重试机制
                String answer = actWithRetry(step, workingMessage, systemPrompt);
                if (answer != null) {
                    // Observe阶段：记录执行结果
                    observe(step, answer);
                    run.markSuccess(answer);
                    log.info("Agent run completed successfully - runId: {}, totalSteps: {}, latency: {}ms",
                            traceId, run.getSteps().size(), run.totalLatencyMs());
                    return run;
                }

                // 如果当前步骤失败，进入Reflect阶段
                log.warn("Step {} failed, preparing for retry - stepId: {}, status: {}",
                        step.getSequence(), step.getStepId(), step.getStatus());
                // Reflect阶段：反思失败原因，调整下一步的输入
                workingMessage = reflect(userMessage, run);
            }

            // 达到最大步数限制，标记为失败
            log.error("Agent run failed - max steps exceeded - runId: {}, maxSteps: {}", traceId, runtimeProperties.getMaxSteps());
            AgentStep guardStep = run.newStep("runtime_guard", "max steps exceeded");
            guardStep.start();
            guardStep.markFailed(AgentErrorCode.MAX_STEPS_EXCEEDED, "Agent reached max steps without a valid answer");
            run.markFailed();
            return run;
        } finally {
            // 清理MDC，避免内存泄漏和上下文污染
            MDC.clear();
        }
    }

    /**
     * Plan阶段：分析和规划用户问题
     * 当前实现为简单的直接返回，未来可扩展为：
     * - 问题分解（将复杂问题拆分为子问题）
     * - 工具选择（预先判断需要哪些工具）
     * - 执行计划生成（生成执行步骤序列）
     */
    private String plan(String userMessage) {
        return userMessage;
    }

    /**
     * Act阶段：执行工具调用，带重试机制
     *
     * 重试策略：
     * - 初始尝试 + N次重试（N由配置决定）
     * - 区分超时错误和执行错误
     * - 记录每次尝试的详细日志
     *
     * @param step 当前执行步骤
     * @param message 发送给LLM的消息
     * @param systemPrompt 系统提示词
     * @return 执行结果，失败返回null
     */
    private String actWithRetry(AgentStep step, String message, String systemPrompt) {
        AgentErrorCode lastErrorCode = AgentErrorCode.TOOL_EXECUTION_FAIL;
        String lastErrorMessage = "Unknown error";

        // 重试循环：初始尝试 + retryTimes次重试
        for (int attempt = 0; attempt <= runtimeProperties.getRetryTimes(); attempt++) {
            try {
                log.debug("Attempting tool execution - stepId: {}, attempt: {}/{}",
                        step.getStepId(), attempt + 1, runtimeProperties.getRetryTimes() + 1);
                String result = invokeWithTimeout(message, systemPrompt);
                log.info("Tool execution succeeded - stepId: {}, attempt: {}", step.getStepId(), attempt + 1);
                return result;
            } catch (TimeoutException e) {
                // 超时异常：LLM响应时间过长
                lastErrorCode = AgentErrorCode.TOOL_TIMEOUT;
                lastErrorMessage = "Step timeout after " + runtimeProperties.getStepTimeoutMs() + "ms";
                log.warn("Tool execution timed out - stepId: {}, attempt: {}, timeout: {}ms",
                        step.getStepId(), attempt + 1, runtimeProperties.getStepTimeoutMs());
            } catch (Exception e) {
                // 其他异常：网络错误、API错误等
                lastErrorCode = AgentErrorCode.TOOL_EXECUTION_FAIL;
                lastErrorMessage = e.getMessage() == null ? "Tool execution failed" : e.getMessage();
                log.error("Tool execution failed - stepId: {}, attempt: {}, error: {}",
                        step.getStepId(), attempt + 1, lastErrorMessage, e);
            }
        }

        // 所有重试都失败，标记步骤失败
        if (lastErrorCode == AgentErrorCode.TOOL_TIMEOUT) {
            step.markTimeout(lastErrorMessage);
        } else {
            step.markFailed(lastErrorCode, lastErrorMessage);
        }
        log.error("Tool execution failed after all retries - stepId: {}, errorCode: {}, message: {}",
                step.getStepId(), lastErrorCode, lastErrorMessage);
        return null;
    }

    /**
     * 带超时控制的工具调用
     *
     * 使用Future.get(timeout)实现超时控制：
     * - 在单独的线程中执行LLM调用
     * - 主线程等待最多stepTimeoutMs毫秒
     * - 超时后取消执行并抛出TimeoutException
     *
     * @throws TimeoutException 执行超时
     * @throws Exception 其他执行异常
     */
    private String invokeWithTimeout(String message, String systemPrompt) throws Exception {
        // 提交任务到Virtual Thread执行器
        Future<String> future = executor.submit(() -> policyAgent.chat(message, systemPrompt));
        try {
            // 等待结果，最多等待stepTimeoutMs毫秒
            return future.get(runtimeProperties.getStepTimeoutMs(), TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            // 超时后取消任务执行
            future.cancel(true);
            throw e;
        }
    }

    /**
     * Observe阶段：观察和记录执行结果
     */
    private void observe(AgentStep step, String answer) {
        step.markSuccess(summarize(answer));
        log.info("Step completed successfully - stepId: {}, latency: {}ms, output: {}",
                step.getStepId(), step.latencyMs(), summarize(answer));
    }

    /**
     * Reflect阶段：反思失败原因，调整策略
     *
     * 当前实现：
     * - 提醒LLM之前的执行失败了
     * - 要求提供直接、完整的答案
     *
     * 未来可扩展为：
     * - 分析失败原因（工具调用失败、参数错误等）
     * - 调整工具选择策略
     * - 简化问题或改变提问方式
     */
    private String reflect(String originalQuestion, AgentRun run) {
        log.debug("Reflecting on failed execution - runId: {}, completedSteps: {}", run.getRunId(), run.getSteps().size());
        return """
                User question: %s
                Previous execution failed. Please provide a direct, complete answer.
                """.formatted(originalQuestion);
    }

    /**
     * 文本摘要：限制长度，避免日志过长
     */
    private String summarize(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }
        int limit = 160;
        return text.length() <= limit ? text : text.substring(0, limit) + "...";
    }
}
