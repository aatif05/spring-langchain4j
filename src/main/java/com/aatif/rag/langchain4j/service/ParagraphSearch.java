package com.aatif.rag.langchain4j.service;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public class ParagraphSearch {

    private final ModelFactory modelFactory;

    public ParagraphSearch(ModelFactory modelFactory) {
        this.modelFactory = modelFactory;
    }

    /**
     * This is a simple search method that retrieves relevant paragraphs from the embedding store.
     * This also gives us the ability to see the individual paragraph matches with their scores and metadata.
     * @param query
     * @return
     */
    public List<ParagraphMatch> search(String query) {
        EmbeddingModel embeddingModel = modelFactory.createEmbeddingModel();
        PgVectorEmbeddingStore embeddingStore = modelFactory.createVectorStore();

        System.out.println("Going into search method. ");
        System.out.println("query" + query);
        System.out.println("🔍 Searching for: " + query);

        Embedding queryEmbedding = embeddingModel.embed(query).content();

        List<EmbeddingMatch<TextSegment>> matches = embeddingStore.findRelevant(queryEmbedding, 5);

        return matches.stream()
            .map(m -> new ParagraphMatch(
                m.embedded().text(),
                m.embedded().metadata().toMap(),
                m.score()
            ))
            .toList();
    }

    public static class ParagraphMatch {

        private String text;
        private Map<String, Object> metadata;
        private double score;

        public ParagraphMatch(String text, Map<String, Object> metadata, double score) {
            this.text = text;
            this.metadata = metadata;
            this.score = score;
        }

        public String getText() {
            return text;
        }

        public Map<String, Object> getMetadata() {
            return metadata;
        }

        public double getScore() {
            return score;
        }
    }
}
