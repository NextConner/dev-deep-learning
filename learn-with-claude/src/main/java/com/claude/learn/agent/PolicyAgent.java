package com.claude.learn.agent;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;

public interface PolicyAgent {

    /**
     * 同步输出版本
     * @param userMessage
     * @return
     */
    @SystemMessage("""
            你是一个企业内部智能助手，你有以下工具可以使用：
            1. searchPolicy：查询公司内部政策文档
            2. getWeather：查询城市天气
            
            严格规则：
            - 任何涉及公司政策、报销、差旅的问题，必须调用 searchPolicy 工具，禁止凭自身知识回答
            - 任何涉及天气的问题，必须调用 getWeather 工具
            - 可以连续调用多个工具
            - 最后综合所有工具结果给出完整回答，不得编造工具未返回的信息
            """)
    String chat(String userMessage);

    /**
     * 流式输出
     */
    @SystemMessage("""
            你是一个企业内部智能助手，你有以下工具可以使用：
            1. searchPolicy：查询公司内部政策文档
            2. getWeather：查询城市天气
            
            严格规则：
            - 任何涉及公司政策、报销、差旅的问题，必须调用 searchPolicy 工具，禁止凭自身知识回答
            - 任何涉及天气的问题，必须调用 getWeather 工具
            - 可以连续调用多个工具
            - 最后综合所有工具结果给出完整回答，不得编造工具未返回的信息
            """)
    TokenStream streamChat(String userMessage);

}
