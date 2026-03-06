package com.claude.learn.listener;

import jakarta.annotation.PostConstruct;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
public class ToolObservabilityAspect {

    private static final Logger log = LoggerFactory.getLogger(ToolObservabilityAspect.class);

    @PostConstruct
    public void init() {
        log.info("✅ ToolObservabilityAspect 已加载");
    }

    @Around("execution(* com.claude.learn..*(..))")
//    @Around("execution(* com.claude.learn.agent.AgentTools.*(..))")
    public Object observeTool(ProceedingJoinPoint joinPoint) throws Throwable {
        String toolName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();
        // 只打印 AgentTools 相关
        if (className.contains("AgentTools")) {
            log.info("🔧 AgentTools 方法被调用：{}", methodName);
        }else{
            return  joinPoint.proceed();
        }
        log.info("🔧 工具调用开始");
        log.info("   工具名称：{}", toolName);
        log.info("   输入参数：{}", Arrays.toString(args));

        long start = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long elapsed = System.currentTimeMillis() - start;

        log.info("   返回结果：{}", result);
        log.info("   耗时：{} ms", elapsed);

        return result;
    }
}
