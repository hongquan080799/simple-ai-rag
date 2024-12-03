package com.example.chat_ai.service;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AIService {
    private final OllamaChatModel llm ;
    private final InMemoryEmbeddingStore<TextSegment> embeddingStore;
    private final Assistant assistant;
    private final EmbeddingStoreIngestor embeddingStoreIngestor;
    public AIService() {
        // Initialize the LLM (Ollama)
        llm = OllamaChatModel.builder()
                .baseUrl("http://localhost:11434") // URL of your LLM API
                .modelName("llama3.2")
                .build();
        embeddingStore = new InMemoryEmbeddingStore<>();
        embeddingStoreIngestor = EmbeddingStoreIngestor.builder()
                .documentSplitter(DocumentSplitters.recursive(1000, 200))
                .embeddingStore(embeddingStore)
                .build();


        // Initialize the document ingestion process


        // Configure the assistant (RAG with content retriever)
        assistant = AiServices.builder(Assistant.class)
                .chatLanguageModel(llm) // Pass the actual LLM
                .chatMemory(MessageWindowChatMemory.withMaxMessages(10)) // Memory management
                .contentRetriever(EmbeddingStoreContentRetriever.from(embeddingStore)) // RAG setup
                .build();
    }
    public String chat(String query) {
        return llm.generate(query);
    }
    public void embeddDocument() {
        List<Document> documents = FileSystemDocumentLoader.loadDocumentsRecursively("documents");
        embeddingStoreIngestor.ingest(documents);
    }
    public String chatWithRag(String query) {
        return assistant.chat(query);
    }
}
