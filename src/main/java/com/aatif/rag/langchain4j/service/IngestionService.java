package com.aatif.rag.langchain4j.service;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.parser.apache.pdfbox.ApachePdfBoxDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IngestionService {

    private final ModelFactory modelFactory;

    public IngestionService(ModelFactory modelFactory) {
        this.modelFactory = modelFactory;
    }

    public void ingestDocuments(String documentsPath) {
        PgVectorEmbeddingStore repository = modelFactory.createVectorStore();
        EmbeddingModel embeddingModel = modelFactory.createEmbeddingModel();

        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
            .documentSplitter(DocumentSplitters.recursive(1000, 200))
            .embeddingModel(embeddingModel)
            .embeddingStore(repository)
            .build();

        System.out.println("Reading PDFs...");
        List<Document> docs = FileSystemDocumentLoader.loadDocuments(documentsPath, new ApachePdfBoxDocumentParser());

        System.out.println("Processing " + docs.size() + " documents into vectors...");
        ingestor.ingest(docs);

        System.out.println("✅ All documents are now searchable in Postgres!");
    }
}
