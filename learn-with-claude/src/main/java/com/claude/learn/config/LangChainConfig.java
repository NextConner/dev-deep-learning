package com.claude.learn.config;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class LangChainConfig {

    @Value("${DEEPSEEK_API_KEY:sk-5a95b401ad1d437b9530fe2dffd479e1}")
    private String apiKey;

    @Value("${EMBEDDING_API_KEY:sk-5c101756cc4e4e328ef21e7980471aaf}")
    private String embeddingApiKey;

//    @Bean
//    public ChatLanguageModel chatLanguageModel() {
//        return OpenAiChatModel.builder()
//                .baseUrl("https://api.deepseek.com")
//                .apiKey("apiKey")
//                .modelName("deepseek-chat")
//                .build();
//    }


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



}
