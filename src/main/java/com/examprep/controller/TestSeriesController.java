package com.examprep.controller;

import com.examprep.dto.request.TestDTOs;
import com.examprep.dto.response.TestResultDTO;
import com.examprep.entity.TestSeries;
import com.examprep.entity.User;
import com.examprep.exception.ResourceNotFoundException;
import com.examprep.repository.TestSeriesRepository;
import com.examprep.service.TestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/test-series")
@RequiredArgsConstructor
@Tag(name = "Test Series", description = "Mock tests, live tests, and PYPs")
public class TestSeriesController {

    private final TestSeriesRepository testSeriesRepository;
    private final TestService testService;

    @GetMapping
    @Operation(summary = "Get test series with filters")
    public ResponseEntity<Page<TestSeries>> getTestSeries(
            @RequestParam(required = false) Long examId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String accessType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        return ResponseEntity.ok(testService.getTestSeries(examId, type, accessType, page, size));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get test series detail by ID")
    public ResponseEntity<TestSeries> getTestSeriesById(@PathVariable Long id) {
        return ResponseEntity.ok(testSeriesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TestSeries", "id", id)));
    }

    @GetMapping("/live")
    @Operation(summary = "Get upcoming live tests")
    public ResponseEntity<List<TestSeries>> getUpcomingLiveTests() {
        return ResponseEntity.ok(testSeriesRepository.findUpcomingLiveTests(LocalDateTime.now()));
    }

    @PostMapping("/{id}/start")
    @Operation(summary = "Start a test (creates attempt)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<TestResultDTO.ActiveTestDTO> startTest(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(testService.startTest(id, user));
    }

    @PutMapping("/attempts/{attemptId}/response")
    @Operation(summary = "Save/update a response during test", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Map<String, String>> saveResponse(
            @PathVariable Long attemptId,
            @AuthenticationPrincipal User user,
            @RequestBody TestDTOs.SaveResponseRequest request) {
        testService.saveResponse(attemptId, user, request);
        return ResponseEntity.ok(Map.of("message", "Response saved"));
    }

    @PostMapping("/attempts/{attemptId}/submit")
    @Operation(summary = "Submit test and get results", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<TestResultDTO.TestResultResponse> submitTest(
            @PathVariable Long attemptId,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(testService.submitTest(attemptId, user));
    }

    @GetMapping("/attempts/{attemptId}/result")
    @Operation(summary = "Get test result by attempt ID", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<TestResultDTO.TestResultResponse> getResult(
            @PathVariable Long attemptId,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(testService.submitTest(attemptId, user));
    }

    @GetMapping("/attempts/{attemptId}/review")
    @Operation(summary = "Get question-wise review after submission", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<TestResultDTO.QuestionReviewItem>> getReview(
            @PathVariable Long attemptId,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(testService.getReview(attemptId, user));
    }
}
