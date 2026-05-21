package com.examprep.controller;

import com.examprep.entity.User;
import com.examprep.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Razorpay payment integration for Prepp+ subscription")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create-order")
    @Operation(summary = "Create Razorpay Order for Prepp+ Subscription", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Map<String, Object>> createOrder(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(paymentService.createOrder(user));
    }

    @PostMapping("/verify")
    @Operation(summary = "Verify Razorpay Payment Signature", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Map<String, String>> verifyPayment(
            @AuthenticationPrincipal User user,
            @RequestBody Map<String, String> payload) {
        return ResponseEntity.ok(paymentService.verifyPayment(user, payload));
    }
}
