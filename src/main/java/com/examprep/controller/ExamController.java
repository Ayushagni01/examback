package com.examprep.controller;

import com.examprep.entity.Exam;
import com.examprep.entity.ExamCategory;
import com.examprep.exception.ResourceNotFoundException;
import com.examprep.repository.ExamCategoryRepository;
import com.examprep.repository.ExamRepository;
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
@RequestMapping("/api/v1/exams")
@RequiredArgsConstructor
@Tag(name = "Exams", description = "Exam categories, details, and upcoming exams")
public class ExamController {

    private final ExamCategoryRepository categoryRepository;
    private final ExamRepository examRepository;

    @GetMapping("/categories")
    @Operation(summary = "Get all top-level exam categories (for mega menu)")
    public ResponseEntity<List<ExamCategory>> getTopCategories() {
        return ResponseEntity.ok(categoryRepository.findByParentCategoryIsNullAndIsActiveTrue());
    }

    @GetMapping("/categories/all")
    @Operation(summary = "Get all exam categories ordered by sort order")
    public ResponseEntity<List<ExamCategory>> getAllCategories() {
        return ResponseEntity.ok(categoryRepository.findByIsActiveTrueOrderBySortOrderAsc());
    }

    @GetMapping("/categories/{slug}")
    @Operation(summary = "Get category by slug with sub-categories and exams")
    public ResponseEntity<ExamCategory> getCategoryBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("ExamCategory", "slug", slug)));
    }

    @GetMapping("/{slug}")
    @Operation(summary = "Get exam detail by slug")
    public ResponseEntity<Exam> getExamBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(examRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Exam", "slug", slug)));
    }

    @GetMapping
    @Operation(summary = "Get all exams with pagination and category filter")
    public ResponseEntity<Page<Exam>> getExams(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        if (categoryId != null) {
            return ResponseEntity.ok(examRepository.findByCategoryIdAndIsActiveTrue(
                    categoryId, PageRequest.of(page, size, Sort.by("name"))));
        }
        return ResponseEntity.ok(examRepository.findAll(
                PageRequest.of(page, size, Sort.by("name"))));
    }

    @GetMapping("/search")
    @Operation(summary = "Search exams by name")
    public ResponseEntity<Page<Exam>> searchExams(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(examRepository.searchExams(q,
                PageRequest.of(page, size)));
    }

    @GetMapping("/upcoming")
    @Operation(summary = "Get upcoming exam dates")
    public ResponseEntity<List<Exam>> getUpcomingExams() {
        return ResponseEntity.ok(examRepository.findUpcomingExams(LocalDate.now()));
    }

    @GetMapping("/featured")
    @Operation(summary = "Get featured/trending exams")
    public ResponseEntity<List<Exam>> getFeaturedExams() {
        return ResponseEntity.ok(examRepository.findByIsFeaturedTrueAndIsActiveTrueOrderByExamDateAsc());
    }
}
