package com.ncepuljxx.hmdp.config;


import dev.langchain4j.community.store.embedding.redis.RedisEmbeddingStore;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.loader.ClassPathDocumentLoader;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;

@Configuration
public class CommonConfig {
    @Autowired
    private ChatMemoryStore redisChatMemoryStore;
    @Autowired
    private EmbeddingModel embeddingModel;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    //知识库导入完成标记:存在则跳过导入,删除该键后重启应用可强制重建知识库
    private static final String KB_INGESTED_KEY = "rag:content:ingested";

    //构建会话记忆对象
    @Bean
    public ChatMemory chatMemory(){
        MessageWindowChatMemory memory = MessageWindowChatMemory.builder()
                .maxMessages(20)
                .build();
        return memory;
    }

    //构建ChatMemoryProvider对象
    @Bean
    public ChatMemoryProvider chatMemoryProvider(){
        ChatMemoryProvider chatMemoryProvider = new ChatMemoryProvider() {
            @Override
            public ChatMemory get(Object memoryId) {
                return MessageWindowChatMemory.builder()
                        .id(memoryId)
                        .maxMessages(20)
                        .chatMemoryStore(redisChatMemoryStore)
                        .build();
            }
        };
        return chatMemoryProvider;
    }

    //构建向量数据库操作对象(Redis版,由langchain4j自动配置创建,持久化存储)
    @Bean
    public EmbeddingStore store(RedisEmbeddingStore redisEmbeddingStore){
        //知识库已在Redis中,直接复用(删除标记键rag:content:ingested后重启可强制重建)
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(KB_INGESTED_KEY))) {
            return redisEmbeddingStore;
        }
        //1.加载文档
        List<Document> documents = ClassPathDocumentLoader.loadDocuments("content");
        //构建文档分割器对象
        DocumentSplitter ds = DocumentSplitters.recursive(500,100);
        //2.构建一个EmbeddingStoreIngestor对象,完成文本数据切割,向量化, 存储
        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                .embeddingStore(redisEmbeddingStore)
                .documentSplitter(ds)
                .embeddingModel(embeddingModel)
                .build();
        ingestor.ingest(documents);
        //3.写入导入完成标记
        stringRedisTemplate.opsForValue().set(KB_INGESTED_KEY, "1");
        return redisEmbeddingStore;
    }

    //构建向量数据库检索对象
    @Bean
    public ContentRetriever contentRetriever(EmbeddingStore store){
        EmbeddingStoreContentRetriever baseRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(store)
                .minScore(0.5)
                .maxResults(3)
                .embeddingModel(embeddingModel)
                .build();
        //包装一层:业务数据查询(商家/优惠券/预约)必须走工具查数据库,检索资料只用于泛知识问答
        return query -> {
            String text = query.text();
            if (isBusinessQuery(text)) {
                return java.util.Collections.emptyList();
            }
            return baseRetriever.retrieve(query);
        };
    }

    //判断是否为业务数据查询(此类问题由工具查数据库,不注入检索资料)
    private boolean isBusinessQuery(String text) {
        if (text == null) {
            return false;
        }
        return text.contains("商家") || text.contains("优惠券") || text.contains("代金券")
                || text.contains("预约") || text.contains("到店") || text.contains("券");
    }
}
