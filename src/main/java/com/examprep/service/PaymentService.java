package com.examprep.service;

import com.examprep.entity.Payment;
import com.examprep.entity.User;
import com.examprep.exception.BadRequestException;
import com.examprep.repository.PaymentRepository;
import com.examprep.repository.UserRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;

    @Value("${razorpay.key-id}")
    private String keyId;

    @Value("${razorpay.key-secret}")
    private String keySecret;

    private static final double PREPP_PLUS_PRICE = 349.0;
    private static final int PREPP_PLUS_DURATION_DAYS = 365;

    public Map<String, Object> createOrder(User user) {
        if ("placeholder_secret".equals(keySecret) || "rzp_test_placeholder".equals(keyId)) {
            log.info("Using developer/placeholder credentials. Bypassing Razorpay order generation for testing.");
            String mockOrderId = "order_mock_" + System.currentTimeMillis();
            
            // Save pending payment record in DB
            Payment payment = Payment.builder()
                    .user(user)
                    .planName("Prepp+ Annual")
                    .amount(PREPP_PLUS_PRICE)
                    .razorpayOrderId(mockOrderId)
                    .status(Payment.PaymentStatus.PENDING)
                    .planDurationDays(PREPP_PLUS_DURATION_DAYS)
                    .build();
            paymentRepository.save(payment);

            return Map.of(
                    "orderId", mockOrderId,
                    "amount", PREPP_PLUS_PRICE,
                    "currency", "INR",
                    "mock", true
            );
        }

        try {
            RazorpayClient razorpay = new RazorpayClient(keyId, keySecret);

            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", (int)(PREPP_PLUS_PRICE * 100)); // amount in the smallest currency unit (paise)
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", "txn_" + System.currentTimeMillis());

            Order order = razorpay.orders.create(orderRequest);

            // Save pending payment record in DB
            Payment payment = Payment.builder()
                    .user(user)
                    .planName("Prepp+ Annual")
                    .amount(PREPP_PLUS_PRICE)
                    .razorpayOrderId(order.get("id"))
                    .status(Payment.PaymentStatus.PENDING)
                    .planDurationDays(PREPP_PLUS_DURATION_DAYS)
                    .build();
            paymentRepository.save(payment);

            return Map.of(
                    "orderId", order.get("id"),
                    "amount", PREPP_PLUS_PRICE,
                    "currency", "INR",
                    "mock", false
            );

        } catch (RazorpayException e) {
            log.error("Error creating Razorpay order", e);
            throw new RuntimeException("Failed to initiate payment. Please try again later.");
        }
    }

    @Transactional
    public Map<String, String> verifyPayment(User user, Map<String, String> payload) {
        String orderId = payload.get("razorpay_order_id");
        String paymentId = payload.get("razorpay_payment_id");
        String signature = payload.get("razorpay_signature");

        Payment payment = paymentRepository.findByRazorpayOrderId(orderId)
                .orElseThrow(() -> new BadRequestException("Invalid Order ID"));

        try {
            JSONObject options = new JSONObject();
            options.put("razorpay_order_id", orderId);
            options.put("razorpay_payment_id", paymentId);
            options.put("razorpay_signature", signature);

            boolean isValid = false;
            if ("placeholder_secret".equals(keySecret) || "rzp_test_placeholder".equals(keyId)) {
                log.info("Using developer/placeholder credentials. Bypassing signature verification for testing.");
                isValid = true;
            } else {
                isValid = Utils.verifyPaymentSignature(options, keySecret);
            }

            if (isValid) {
                // Update Payment Status
                payment.setStatus(Payment.PaymentStatus.SUCCESS);
                payment.setRazorpayPaymentId(paymentId);
                payment.setRazorpaySignature(signature);
                payment.setPaidAt(LocalDateTime.now());
                paymentRepository.save(payment);

                // Upgrade User Subscription
                user.setSubscriptionType(User.SubscriptionType.PREMIUM);
                LocalDateTime newExpiry = LocalDateTime.now().plusDays(payment.getPlanDurationDays());
                // Handle existing premium users (extend expiry)
                if (user.getSubscriptionExpiry() != null && user.getSubscriptionExpiry().isAfter(LocalDateTime.now())) {
                    newExpiry = user.getSubscriptionExpiry().plusDays(payment.getPlanDurationDays());
                }
                user.setSubscriptionExpiry(newExpiry);
                user.setRole(User.Role.ROLE_PREMIUM);
                userRepository.save(user);

                return Map.of("status", "success", "message", "Welcome to Prepp+ !");
            } else {
                payment.setStatus(Payment.PaymentStatus.FAILED);
                paymentRepository.save(payment);
                throw new BadRequestException("Payment verification failed.");
            }
        } catch (RazorpayException e) {
            log.error("Error verifying payment signature", e);
            throw new BadRequestException("Payment signature validation failed.");
        }
    }
}
