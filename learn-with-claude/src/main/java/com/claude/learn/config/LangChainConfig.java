package com.claude.learn.config;

import com.claude.learn.agent.AgentTools;
import com.claude.learn.agent.PolicyAgent;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import org.aspectj.weaver.loadtime.Agent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import javax.sql.DataSource;

@Configuration(proxyBeanMethods = false)
public class LangChainConfig {

    @Value("${DEEPSEEK_API_KEY}")
    private String apiKey;

    @Value("${EMBEDDING_API_KEY}")
    private String embeddingApiKey;



    @Bean
    public EmbeddingModel embeddingModel(){
        return OpenAiEmbeddingModel.builder()
                .baseUrl("https://dashscope.aliyuncs.com/compatible-mode/v1")
                .apiKey(embeddingApiKey)
                .modelName("text-embedding-v2")
                .build();
    }


    @Bean
    public EmbeddingStore<TextSegment> embeddingStore(DataSource dataSource){
        return PgVectorEmbeddingStore.builder()
                .host("192.168.20.129")
                .port(5432)
                .database("ragdb")
                .user("pgsql")
                .password("123456")
                .table("rag_embeddings")
                .dimension(1536)
                .createTable(true)
                .build();
    }


    /**
     * LangChain4j 的 ChatMemory 用于在对话过程中保留上下文信息，增强模型的连续对话能力。
     * @return
     */
    @Bean
    public ChatMemory chatMemory() {
        return MessageWindowChatMemory.withMaxMessages(10); // 保留最近10条对话
    }

    /**
     * 注入自定义 agent
     */
    @Bean
    public PolicyAgent policyAgent(ChatLanguageModel chatLanguageModel, AgentTools agentTools, ChatMemory chatMemory) {
        return AiServices.builder(PolicyAgent.class)
                .chatLanguageModel(chatLanguageModel)
                .streamingChatLanguageModel(streamingChatLanguageModel())
                .tools(agentTools)
                .chatMemory(chatMemory)
                .build();
    }

    @Bean
    public OpenAiStreamingChatModel streamingChatLanguageModel(){
                return OpenAiStreamingChatModel.builder()
                .baseUrl("https://api.deepseek.com")
                .apiKey(apiKey)
                .modelName("deepseek-chat")
                .build();
    }


}
