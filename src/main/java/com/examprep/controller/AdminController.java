package com.examprep.controller;

import com.examprep.dto.request.TestDTOs;
import com.examprep.entity.*;
import com.examprep.repository.*;
import com.examprep.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Admin management APIs")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

    private final AdminService adminService;
    private final ExamCategoryRepository categoryRepository;
    private final ExamRepository examRepository;
    private final TestSeriesRepository testSeriesRepository;
    private final QuestionRepository questionRepository;
    private final CurrentAffairsRepository currentAffairsRepository;
    private final NewsArticleRepository newsArticleRepository;

    // ============ DASHBOARD ============

    @GetMapping("/stats")
    @Operation(summary = "Get dashboard statistics")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(adminService.getDashboardStats());
    }

    // ============ EXAM CATEGORIES ============

    @GetMapping("/categories")
    @Operation(summary = "Get all categories")
    public ResponseEntity<List<ExamCategory>> getCategories() {
        return ResponseEntity.ok(categoryRepository.findAll(Sort.by("sortOrder")));
    }

    @PostMapping("/categories")
    @Operation(summary = "Create exam category")
    public ResponseEntity<ExamCategory> createCategory(@RequestBody ExamCategory category) {
        return ResponseEntity.ok(adminService.createCategory(category));
    }

    @PutMapping("/categories/{id}")
    @Operation(summary = "Update exam category")
    public ResponseEntity<ExamCategory> updateCategory(@PathVariable Long id, @RequestBody ExamCategory category) {
        return ResponseEntity.ok(adminService.updateCategory(id, category));
    }

    @DeleteMapping("/categories/{id}")
    @Operation(summary = "Delete exam category")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        adminService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    // ============ EXAMS ============

    @GetMapping("/exams")
    @Operation(summary = "Get all exams paginated")
    public ResponseEntity<Page<Exam>> getExams(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(examRepository.findAll(PageRequest.of(page, size, Sort.by("name"))));
    }

    @PostMapping("/exams")
    @Operation(summary = "Create exam")
    public ResponseEntity<Exam> createExam(@RequestBody Exam exam) {
        return ResponseEntity.ok(adminService.createExam(exam));
    }

    @PutMapping("/exams/{id}")
    @Operation(summary = "Update exam")
    public ResponseEntity<Exam> updateExam(@PathVariable Long id, @RequestBody Exam exam) {
        return ResponseEntity.ok(adminService.updateExam(id, exam));
    }

    @DeleteMapping("/exams/{id}")
    @Operation(summary = "Delete exam")
    public ResponseEntity<Void> deleteExam(@PathVariable Long id) {
        adminService.deleteExam(id);
        return ResponseEntity.noContent().build();
    }

    // ============ TEST SERIES ============

    @GetMapping("/test-series")
    @Operation(summary = "Get all test series paginated")
    public ResponseEntity<Page<TestSeries>> getTestSeries(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(testSeriesRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))));
    }

    @PostMapping("/test-series")
    @Operation(summary = "Create test series")
    public ResponseEntity<TestSeries> createTestSeries(@Valid @RequestBody TestDTOs.CreateTestSeriesRequest request) {
        return ResponseEntity.ok(adminService.createTestSeries(request));
    }

    @PutMapping("/test-series/{id}")
    @Operation(summary = "Update test series")
    public ResponseEntity<TestSeries> updateTestSeries(@PathVariable Long id, @RequestBody TestDTOs.CreateTestSeriesRequest request) {
        return ResponseEntity.ok(adminService.updateTestSeries(id, request));
    }

    @DeleteMapping("/test-series/{id}")
    @Operation(summary = "Delete test series")
    public ResponseEntity<Void> deleteTestSeries(@PathVariable Long id) {
        adminService.deleteTestSeries(id);
        return ResponseEntity.noContent().build();
    }

    // ============ QUESTIONS ============

    @GetMapping("/questions")
    @Operation(summary = "Get all questions paginated")
    public ResponseEntity<Page<Question>> getQuestions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(questionRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))));
    }

    @PostMapping("/questions")
    @Operation(summary = "Create a standalone question")
    public ResponseEntity<Question> createQuestion(@Valid @RequestBody TestDTOs.CreateQuestionRequest request) {
        return ResponseEntity.ok(adminService.createQuestion(request));
    }

    @PostMapping("/test-series/{id}/questions")
    @Operation(summary = "Add questions to a test series")
    public ResponseEntity<Map<String, String>> addQuestions(
            @PathVariable Long id,
            @RequestParam(required = false) String section,
            @Valid @RequestBody List<TestDTOs.CreateQuestionRequest> requests) {
        adminService.addQuestionsToTestSeries(id, requests, section);
        return ResponseEntity.ok(Map.of("message", requests.size() + " questions added to test series " + id));
    }

    // ============ CURRENT AFFAIRS ============

    @GetMapping("/current-affairs")
    @Operation(summary = "Get all current affairs paginated")
    public ResponseEntity<Page<CurrentAffairs>> getCurrentAffairs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(currentAffairsRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "publishedDate"))));
    }

    @PostMapping("/current-affairs")
    @Operation(summary = "Create current affairs article")
    public ResponseEntity<CurrentAffairs> createCurrentAffairs(@RequestBody CurrentAffairs article) {
        return ResponseEntity.ok(adminService.createCurrentAffairs(article));
    }

    @PutMapping("/current-affairs/{id}")
    @Operation(summary = "Update current affairs article")
    public ResponseEntity<CurrentAffairs> updateCurrentAffairs(@PathVariable Long id, @RequestBody CurrentAffairs article) {
        return ResponseEntity.ok(adminService.updateCurrentAffairs(id, article));
    }

    @DeleteMapping("/current-affairs/{id}")
    @Operation(summary = "Delete current affairs article")
    public ResponseEntity<Void> deleteCurrentAffairs(@PathVariable Long id) {
        adminService.deleteCurrentAffairs(id);
        return ResponseEntity.noContent().build();
    }

    // ============ NEWS ============

    @GetMapping("/news")
    @Operation(summary = "Get all news paginated")
    public ResponseEntity<Page<NewsArticle>> getNews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(newsArticleRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "publishedDate"))));
    }

    @PostMapping("/news")
    @Operation(summary = "Create news article")
    public ResponseEntity<NewsArticle> createNews(@RequestBody NewsArticle article) {
        return ResponseEntity.ok(adminService.createNews(article));
    }

    @PutMapping("/news/{id}")
    @Operation(summary = "Update news article")
    public ResponseEntity<NewsArticle> updateNews(@PathVariable Long id, @RequestBody NewsArticle article) {
        return ResponseEntity.ok(adminService.updateNews(id, article));
    }

    @DeleteMapping("/news/{id}")
    @Operation(summary = "Delete news article")
    public ResponseEntity<Void> deleteNews(@PathVariable Long id) {
        adminService.deleteNews(id);
        return ResponseEntity.noContent().build();
    }
}
