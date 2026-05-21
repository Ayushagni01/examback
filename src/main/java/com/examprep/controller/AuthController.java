package com.examprep.controller;

import com.examprep.dto.request.AuthDTOs;
import com.examprep.dto.response.AuthResponseDTOs;
import com.examprep.entity.User;
import com.examprep.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "OTP based authentication APIs")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/send-otp")
    @Operation(summary = "Send OTP to mobile number")
    public ResponseEntity<AuthResponseDTOs.OtpResponse> sendOtp(
            @Valid @RequestBody AuthDTOs.SendOtpRequest request) {
        return ResponseEntity.ok(authService.sendOtp(request.getPhone()));
    }

    @PostMapping("/verify-otp")
    @Operation(summary = "Verify OTP and get JWT tokens")
    public ResponseEntity<AuthResponseDTOs.AuthResponse> verifyOtp(
            @Valid @RequestBody AuthDTOs.VerifyOtpRequest request) {
        return ResponseEntity.ok(authService.verifyOtp(request));
    }

    @PostMapping("/refresh-token")
    @Operation(summary = "Refresh access token using refresh token")
    public ResponseEntity<AuthResponseDTOs.AuthResponse> refreshToken(
            @Valid @RequestBody AuthDTOs.RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refreshToken(request.getRefreshToken()));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user profile")
    public ResponseEntity<AuthResponseDTOs.UserInfo> getMe(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(AuthResponseDTOs.UserInfo.from(user));
    }

    @PutMapping("/me")
    @Operation(summary = "Update profile")
    public ResponseEntity<AuthResponseDTOs.UserInfo> updateProfile(
            @AuthenticationPrincipal User user,
            @RequestBody AuthDTOs.UpdateProfileRequest request) {
        return ResponseEntity.ok(authService.updateProfile(user.getId(), request));
    }
}
