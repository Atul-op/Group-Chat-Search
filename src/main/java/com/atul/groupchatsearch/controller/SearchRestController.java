package com.atul.groupchatsearch.controller;

import com.atul.groupchatsearch.dto.SearchRequest;
import com.atul.groupchatsearch.dto.SearchResult;
import com.atul.groupchatsearch.service.DataGenerationService;
import com.atul.groupchatsearch.service.SearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class SearchRestController {

    private final DataGenerationService dataGenerationService;
    private final SearchService searchService;

    public SearchRestController(DataGenerationService dataGenerationService, SearchService searchService) {
        this.dataGenerationService = dataGenerationService;
        this.searchService = searchService;
    }

    @PostMapping("/generate-data")
    public ResponseEntity<String> generateData() {
        dataGenerationService.generateAndIngestData();
        return ResponseEntity.ok("Data generation and ingestion completed successfully.");
    }

    @PostMapping("/search")
    public ResponseEntity<List<SearchResult>> search(@RequestBody SearchRequest request) {
        List<SearchResult> results = searchService.searchChat(request.getQuery(), request.getLimit());
        return ResponseEntity.ok(results);
    }

    @GetMapping("/messages")
    public ResponseEntity<List<SearchResult>> getAllMessages() {
        List<SearchResult> messages = searchService.getAllMessages();
        return ResponseEntity.ok(messages);
    }
}