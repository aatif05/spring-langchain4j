package com.aatif.rag.langchain4j.service;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.rag.query.Query;
import dev.langchain4j.rag.query.transformer.QueryTransformer;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.Result;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;

import java.util.Arrays;

import org.springframework.stereotype.Service;

@Service
public class RagService {

    private final ModelFactory modelFactory;

    public RagService(ModelFactory modelFactory) {
        this.modelFactory = modelFactory;
    }

    interface PDFAssistant {

        @SystemMessage("""
            You are a helpful corporate assistant. 
                1. Use ONLY the provided context.\s
                    2. Do NOT combine unrelated sentences to form new meanings.
                    3. If the context contains a list of 'Other Concerns', treat each concern as a separate fact.
                    4. Provide the answer in a direct, verbatim manner if possible.
            Answer the user's question ONLY using the provided context.
            
            CRITICAL RULE: For every fact you state, you MUST cite the source file name 
            found in the metadata of the context.
            
            Example: "The holiday policy allows 20 days off (Source: HR_Policy_2024.pdf)."
            
            If the answer is not in the context, say 'I don't know'.
            """)
        Result<String> chat(String userMessage);
    }

    public String ask(String question) {
        PgVectorEmbeddingStore embeddingStore = modelFactory.createVectorStore();
        ChatLanguageModel chatModel = modelFactory.createChatModel();
        EmbeddingModel embeddingModel = modelFactory.createEmbeddingModel();

        //Multi Query retrieval approach.
        //This is used to give the LLM multiple different versions of the same query to improve retrieval results.
        QueryTransformer queryTransformer = (query) -> {
            String prompt = "Generate 3 different versions of this question to help find better documents: " + query.text();
            String variations = chatModel.generate(prompt);

            // Split the LLM response into a list of queries
            return Arrays.stream(variations.split("\n"))
                .map(String::trim)
                .filter(line -> !line.isEmpty())
                .map(line -> line.replaceAll("^\\d+\\.\\s*", ""))
                .map(Query::from)
                .toList();
        };

        EmbeddingStoreContentRetriever embeddingStoreContentRetriever = EmbeddingStoreContentRetriever.builder()
            .embeddingStore(embeddingStore)
            .embeddingModel(embeddingModel)
            .maxResults(3)
            .minScore(0.75)
            .build();

        RetrievalAugmentor retrievalAugmentor = DefaultRetrievalAugmentor.builder()
           .queryTransformer(queryTransformer)
            .contentRetriever(embeddingStoreContentRetriever)
            .build();

        PDFAssistant assistant = AiServices.builder(PDFAssistant.class)
            .chatLanguageModel(chatModel)
            .retrievalAugmentor(retrievalAugmentor)
            .build();

        Result<String> result = assistant.chat(question);
        System.out.println("\nAI Response: " + result.content());

        System.out.println("\nSOURCES USED:");
        result.sources().stream()
            .map(content -> content.textSegment().metadata().getString("file_name"))
            .distinct()
            .forEach(fileName -> System.out.println("- " + fileName));

        return result.content();
    }




}
