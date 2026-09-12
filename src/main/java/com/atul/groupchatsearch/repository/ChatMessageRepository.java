package com.atul.groupchatsearch.repository;

import com.atul.groupchatsearch.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    // 1. Exact or Partial Text Match (Standard JPA)
    List<ChatMessage> findByContentContainingIgnoreCase(String content);

    /*
     * 2. Pure Vector Similarity Search
     * Uses pgvector's <=> operator for Cosine Distance.
     * We pass the vector as a formatted String (e.g., "[0.1, 0.2, ...]") so Postgres parses it natively.
     */
    @Query(value = "SELECT * FROM chat_messages ORDER BY embedding <=> cast(:embedding as vector) LIMIT :limit", nativeQuery = true)
    List<ChatMessage> findSimilarMessages(
            @Param("embedding") String embeddingString,
            @Param("limit") int limit
    );

    /*
     * 3. Hybrid Search: Filter by Sender + Vector Similarity
     */
    @Query(value = "SELECT * FROM chat_messages WHERE sender_name = :senderName ORDER BY embedding <=> cast(:embedding as vector) LIMIT :limit", nativeQuery = true)
    List<ChatMessage> findSimilarMessagesBySender(
            @Param("senderName") String senderName,
            @Param("embedding") String embeddingString,
            @Param("limit") int limit
    );

    /*
     * 4. Hybrid Search: Filter by Time Range + Vector Similarity
     */
    @Query(value = "SELECT * FROM chat_messages WHERE timestamp >= :startDate AND timestamp <= :endDate ORDER BY embedding <=> cast(:embedding as vector) LIMIT :limit", nativeQuery = true)
    List<ChatMessage> findSimilarMessagesByTimeRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("embedding") String embeddingString,
            @Param("limit") int limit
    );
}