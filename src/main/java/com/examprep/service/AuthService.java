package com.examprep.service;

import com.examprep.dto.request.AuthDTOs;
import com.examprep.dto.response.AuthResponseDTOs;
import com.examprep.entity.User;
import com.examprep.exception.BadRequestException;
import com.examprep.exception.ResourceNotFoundException;
import com.examprep.repository.UserRepository;
import com.examprep.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final StringRedisTemplate redisTemplate;
    private static final String OTP_PREFIX = "otp:";
    private static final int OTP_EXPIRY_MINUTES = 5;

    // In-memory fallbacks when Redis is not running
    private final ConcurrentHashMap<String, String> inMemoryStore = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Integer> inMemoryLimitStore = new ConcurrentHashMap<>();

    private String getRedisVal(String key) {
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.warn("Redis query failed. Falling back to memory storage. Error: {}", e.getMessage());
            return inMemoryStore.get(key);
        }
    }

    private void setRedisVal(String key, String value, Duration duration) {
        try {
            redisTemplate.opsForValue().set(key, value, duration);
        } catch (Exception e) {
            log.warn("Redis save failed. Falling back to memory storage. Error: {}", e.getMessage());
            inMemoryStore.put(key, value);
        }
    }

    private void deleteRedisVal(String key) {
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.warn("Redis delete failed. Falling back to memory storage. Error: {}", e.getMessage());
            inMemoryStore.remove(key);
        }
    }

    private Long incrementLimit(String key) {
        try {
            return redisTemplate.opsForValue().increment(key);
        } catch (Exception e) {
            int newVal = inMemoryLimitStore.merge(key, 1, Integer::sum);
            return (long) newVal;
        }
    }

    private void expireLimit(String key, Duration duration) {
        try {
            redisTemplate.expire(key, duration);
        } catch (Exception e) {
            // No-op for in-memory limit store
        }
    }

    /**
     * Send OTP to phone number.
     * For development, OTP is stored in Redis and logged.
     * In production, integrate MSG91/Twilio to SMS the OTP.
     */
    public AuthResponseDTOs.OtpResponse sendOtp(String phone) {
        // Check rate limit (Redis key: otp:limit:{phone})
        String limitKey = "otp:limit:" + phone;
        String limitCount = getRedisVal(limitKey);
        if (limitCount != null && Integer.parseInt(limitCount) >= 3) {
            throw new BadRequestException("Too many OTP requests. Please try again after 15 minutes.");
        }

        // Generate 6-digit OTP
        String otp = generateOtp();

        // Store OTP in Redis/Memory with 5-minute expiry
        String otpKey = OTP_PREFIX + phone;
        setRedisVal(otpKey, otp, Duration.ofMinutes(OTP_EXPIRY_MINUTES));

        // Increment rate limit counter
        incrementLimit(limitKey);
        expireLimit(limitKey, Duration.ofMinutes(15));

        // TODO: In production, send SMS via MSG91 or Twilio
        log.info("OTP for {} : {} (DEV MODE - remove in production)", phone, otp);

        return AuthResponseDTOs.OtpResponse.builder()
                .message("OTP sent successfully to " + maskPhone(phone))
                .phone(phone)
                .otpSent(true)
                .otp(otp) // For dev mode
                .build();
    }

    /**
     * Verify OTP and return JWT tokens.
     * Creates user account if first login.
     */
    @Transactional
    public AuthResponseDTOs.AuthResponse verifyOtp(AuthDTOs.VerifyOtpRequest request) {
        String otpKey = OTP_PREFIX + request.getPhone();
        String storedOtp = getRedisVal(otpKey);

        if (storedOtp == null) {
            throw new BadRequestException("OTP expired. Please request a new OTP.");
        }
        if (!storedOtp.equals(request.getOtp())) {
            throw new BadRequestException("Invalid OTP. Please try again.");
        }

        // OTP verified - delete it
        deleteRedisVal(otpKey);

        // Find or create user
        User user = userRepository.findByPhone(request.getPhone())
                .orElseGet(() -> createNewUser(request));

        // Generate JWT tokens
        String accessToken = jwtUtil.generateAccessToken(user.getPhone(), user.getRole().name());
        String refreshToken = jwtUtil.generateRefreshToken(user.getPhone());

        // Store refresh token in Redis
        setRedisVal(
                "refresh:" + user.getPhone(),
                refreshToken,
                Duration.ofDays(7)
        );

        return AuthResponseDTOs.AuthResponse.of(accessToken, refreshToken, user);
    }

    /**
     * Refresh access token using valid refresh token
     */
    public AuthResponseDTOs.AuthResponse refreshToken(String refreshToken) {
        if (!jwtUtil.isTokenValid(refreshToken) || jwtUtil.isTokenExpired(refreshToken)) {
            throw new BadRequestException("Invalid or expired refresh token.");
        }

        String phone = jwtUtil.extractPhone(refreshToken);

        // Validate refresh token matches Redis store
        String storedToken = getRedisVal("refresh:" + phone);
        if (!refreshToken.equals(storedToken)) {
            throw new BadRequestException("Refresh token mismatch. Please login again.");
        }

        User user = userRepository.findByPhone(phone)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String newAccessToken = jwtUtil.generateAccessToken(user.getPhone(), user.getRole().name());
        String newRefreshToken = jwtUtil.generateRefreshToken(user.getPhone());

        setRedisVal("refresh:" + phone, newRefreshToken, Duration.ofDays(7));

        return AuthResponseDTOs.AuthResponse.of(newAccessToken, newRefreshToken, user);
    }

    @Transactional
    public AuthResponseDTOs.UserInfo updateProfile(Long userId, AuthDTOs.UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (request.getName() != null) user.setName(request.getName());
        if (request.getEmail() != null) user.setEmail(request.getEmail());
        if (request.getCity() != null) user.setCity(request.getCity());
        if (request.getEducationStatus() != null) user.setEducationStatus(request.getEducationStatus());
        if (request.getProfilePicUrl() != null) user.setProfilePicUrl(request.getProfilePicUrl());

        return AuthResponseDTOs.UserInfo.from(userRepository.save(user));
    }

    private User createNewUser(AuthDTOs.VerifyOtpRequest request) {
        return userRepository.save(User.builder()
                .phone(request.getPhone())
                .name(request.getName() != null ? request.getName() : "User" + request.getPhone().substring(6))
                .email(request.getEmail())
                .city(request.getCity())
                .educationStatus(request.getEducationStatus())
                .build());
    }

    private String generateOtp() {
        SecureRandom random = new SecureRandom();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    private String maskPhone(String phone) {
        return phone.substring(0, 4) + "******" + phone.substring(10);
    }
}

