package com.atul.groupchatsearch.dto;

import java.time.LocalDateTime;

public class SearchResult {
    private Long id;
    private String senderName;
    private String content;
    private LocalDateTime timestamp;

    public SearchResult() {}

    public SearchResult(Long id, String senderName, String content, LocalDateTime timestamp) {
        this.id = id;
        this.senderName = senderName;
        this.content = content;
        this.timestamp = timestamp;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}