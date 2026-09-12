package com.atul.groupchatsearch.service;

import com.atul.groupchatsearch.dto.SearchResult;
import com.atul.groupchatsearch.entity.ChatMessage;
import com.atul.groupchatsearch.repository.ChatMessageRepository;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SearchService {

    private final ChatMessageRepository chatMessageRepository;
    private final EmbeddingModel embeddingModel;
    private final String[] KNOWN_USERS = {"Priya", "Rahul", "Amit", "Neha", "Rohan", "Anjali", "Vikash", "Sneha"};

    public SearchService(ChatMessageRepository chatMessageRepository, EmbeddingModel embeddingModel) {
        this.chatMessageRepository = chatMessageRepository;
        this.embeddingModel = embeddingModel;
    }

    public List<SearchResult> getAllMessages() {
        return chatMessageRepository.findAll(Sort.by(Sort.Direction.ASC, "timestamp"))
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<SearchResult> searchChat(String query, int limit) {
        float[] queryEmbedding = embeddingModel.embed(query);
        String embeddingString = Arrays.toString(queryEmbedding);

        String targetSender = extractSenderFromQuery(query);
        boolean isRecentSearch = query.toLowerCase().contains("last month") || query.toLowerCase().contains("recent");

        List<ChatMessage> entities;
        if (targetSender != null) {
            entities = chatMessageRepository.findSimilarMessagesBySender(targetSender, embeddingString, limit);
        } else if (isRecentSearch) {
            LocalDateTime endDate = LocalDateTime.now();
            LocalDateTime startDate = endDate.minusDays(30);
            entities = chatMessageRepository.findSimilarMessagesByTimeRange(startDate, endDate, embeddingString, limit);
        } else {
            entities = chatMessageRepository.findSimilarMessages(embeddingString, limit);
        }

        // Map entities to DTOs to avoid sending the 1536-dimension vector to the frontend
        return entities.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    private String extractSenderFromQuery(String query) {
        String lowerQuery = query.toLowerCase();
        for (String user : KNOWN_USERS) {
            if (lowerQuery.contains(user.toLowerCase())) {
                return user;
            }
        }
        return null;
    }

    private SearchResult mapToDto(ChatMessage message) {
        return new SearchResult(
                message.getId(),
                message.getSenderName(),
                message.getContent(),
                message.getTimestamp()
        );
    }
}