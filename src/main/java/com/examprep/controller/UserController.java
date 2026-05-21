package com.examprep.controller;

import com.examprep.entity.User;
import com.examprep.entity.TestAttempt;
import com.examprep.repository.TestAttemptRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
@Tag(name = "User Dashboard", description = "User specific data, history, bookmarks")
public class UserController {

    private final TestAttemptRepository attemptRepository;

    @GetMapping("/attempts")
    @Operation(summary = "Get user's test history", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Page<TestAttempt>> getAttemptHistory(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        return ResponseEntity.ok(attemptRepository.findByUserIdOrderByCreatedAtDesc(
                user.getId(), PageRequest.of(page, size)));
    }
}
