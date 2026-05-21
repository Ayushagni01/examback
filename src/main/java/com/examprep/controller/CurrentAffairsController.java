package com.examprep.controller;

import com.examprep.entity.CurrentAffairs;
import com.examprep.repository.CurrentAffairsRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/current-affairs")
@RequiredArgsConstructor
@Tag(name = "Current Affairs", description = "Daily updates and news")
public class CurrentAffairsController {

    private final CurrentAffairsRepository repository;

    @GetMapping
    @Operation(summary = "Get current affairs paginated")
    public ResponseEntity<Page<CurrentAffairs>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String category) {
        
        if (category != null) {
            try {
                CurrentAffairs.Category cat = CurrentAffairs.Category.valueOf(category.toUpperCase());
                return ResponseEntity.ok(repository.findByCategoryAndIsActiveTrueOrderByPublishedDateDesc(
                        cat, PageRequest.of(page, size)));
            } catch (IllegalArgumentException e) {
                // Ignore invalid category and fallback
            }
        }
        
        return ResponseEntity.ok(repository.findByIsActiveTrueOrderByPublishedDateDesc(
                PageRequest.of(page, size)));
    }

    @GetMapping("/daily")
    @Operation(summary = "Get daily current affairs")
    public ResponseEntity<List<CurrentAffairs>> getDaily(
            @RequestParam(required = false) String date) {
        
        LocalDate targetDate = date != null ? LocalDate.parse(date) : LocalDate.now();
        return ResponseEntity.ok(repository.findByPublishedDateAndIsActiveTrue(targetDate));
    }
}
