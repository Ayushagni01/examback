package com.examprep.controller;

import com.examprep.entity.NewsArticle;
import com.examprep.repository.NewsArticleRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/news")
@RequiredArgsConstructor
@Tag(name = "News & Notifications", description = "Admit cards, results, vacancies")
public class NewsController {

    private final NewsArticleRepository repository;

    @GetMapping
    @Operation(summary = "Get all news paginated")
    public ResponseEntity<Page<NewsArticle>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Long examId) {
            
        if (examId != null) {
            return ResponseEntity.ok(repository.findByExamIdAndIsActiveTrueOrderByPublishedDateDesc(
                    examId, PageRequest.of(page, size)));
        }
        
        if (category != null) {
            try {
                NewsArticle.Category cat = NewsArticle.Category.valueOf(category.toUpperCase());
                return ResponseEntity.ok(repository.findByCategoryAndIsActiveTrueOrderByPublishedDateDesc(
                        cat, PageRequest.of(page, size)));
            } catch (IllegalArgumentException e) {
                // Ignore invalid category
            }
        }
        
        return ResponseEntity.ok(repository.findByIsActiveTrueOrderByPublishedDateDesc(
                PageRequest.of(page, size)));
    }
}
