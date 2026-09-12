package com.atul.groupchatsearch.config;

import com.google.genai.Client;
import com.google.genai.types.EmbedContentResponse;
import com.google.genai.types.ContentEmbedding;
import org.jspecify.annotations.NonNull;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.embedding.EmbeddingResponseMetadata;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingOptions;
import org.springframework.ai.embedding.BatchingStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.List;
import java.util.ArrayList;

@Configuration
public class GeminiEmbeddingConfig {

    @Bean
    @Primary
    public EmbeddingModel geminiEmbeddingModel() {
        Client client = Client.builder().apiKey("GEMINI API KEY HERE").build();

        return new EmbeddingModel() {
            @Override
            public @NonNull EmbeddingResponse call(@NonNull EmbeddingRequest request) {
                List<String> inputs = request.getInstructions();
                try {
                    EmbedContentResponse response = client.models.embedContent("gemini-embedding-001", inputs, null);
                    List<ContentEmbedding> contentEmbeddings = response.embeddings().orElse(List.of());

                    List<Embedding> embeddingsList = new ArrayList<>();
                    for (int i = 0; i < contentEmbeddings.size(); i++) {
                        // Google GenAI SDK returns List<Float>
                        List<Float> floatsList = contentEmbeddings.get(i).values().orElse(List.of());
                        float[] floats = new float[floatsList.size()];
                        for (int j = 0; j < floatsList.size(); j++) {
                            floats[j] = floatsList.get(j);
                        }
                        embeddingsList.add(new Embedding(floats, i));
                    }
                    return new EmbeddingResponse(embeddingsList, new EmbeddingResponseMetadata());
                } catch (Exception e) {
                    throw new RuntimeException("Failed to generate embedding from Gemini API", e);
                }
            }

            @Override
            public int dimensions() {
                return 3072;
            }

            @Override
            public float[] embed(String text) {
                EmbeddingResponse response = this.call(new EmbeddingRequest(List.of(text), null));
                return response.getResult().getOutput();
            }

            @Override
            public float[] embed(Document document) {
                // Fixed getContent() to getText()
                return embed(document.getText() != null ? document.getText() : "");
            }

            @Override
            public List<float[]> embed(List<String> texts) {
                EmbeddingResponse response = this.call(new EmbeddingRequest(texts, null));
                return response.getResults().stream().map(Embedding::getOutput).toList();
            }

            @Override
            public String getEmbeddingContent(Document document) {
                return document.getText();
            }

            @Override
            public List<float[]> embed(List<Document> documents, EmbeddingOptions options, BatchingStrategy batchingStrategy) {
                List<String> texts = documents.stream().map(d -> d.getText() != null ? d.getText() : "").toList();
                return embed(texts);
            }

            @Override
            public EmbeddingResponse embedForResponse(List<String> texts) {
                return this.call(new EmbeddingRequest(texts, null));
            }
        };
    }
}