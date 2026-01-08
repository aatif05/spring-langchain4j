package com.aatif.rag.langchain4j.service;

import com.aatif.rag.langchain4j.config.ModelConfig;
import com.aatif.rag.langchain4j.config.VectorStoreConfig;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import org.springframework.stereotype.Service;

@Service
public class ModelFactory {

    private final ModelConfig modelConfig;
    private final VectorStoreConfig vectorStoreConfig;

    public ModelFactory(ModelConfig modelConfig, VectorStoreConfig vectorStoreConfig) {
        this.modelConfig = modelConfig;
        this.vectorStoreConfig = vectorStoreConfig;
    }

    public EmbeddingModel createEmbeddingModel() {
        return OllamaEmbeddingModel.builder()
            .baseUrl(modelConfig.getBaseUrl())
            .modelName(modelConfig.getEmbeddingModel())
            .build();
    }

    public ChatLanguageModel createChatModel() {
        return OllamaChatModel.builder()
            .baseUrl(modelConfig.getBaseUrl())
            .modelName(modelConfig.getChatModel())
            .temperature(modelConfig.getChatTemperature())
            .build();
    }

    public PgVectorEmbeddingStore createVectorStore() {
        return PgVectorEmbeddingStore.builder()
            .host(vectorStoreConfig.getHost())
            .port(vectorStoreConfig.getPort())
            .database(vectorStoreConfig.getDatabase())
            .user(vectorStoreConfig.getUser())
            .password(vectorStoreConfig.getPassword())
            .table(vectorStoreConfig.getTable())
            .dimension(vectorStoreConfig.getDimension())
            .build();
    }
}

