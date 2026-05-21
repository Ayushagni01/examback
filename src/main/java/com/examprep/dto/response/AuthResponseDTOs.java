package com.examprep.dto.response;

import com.examprep.entity.User;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

public class AuthResponseDTOs {

    @Data
    @Builder
    public static class AuthResponse {
        private String accessToken;
        private String refreshToken;
        private String tokenType;
        private UserInfo user;

        public static AuthResponse of(String accessToken, String refreshToken, User user) {
            return AuthResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .user(UserInfo.from(user))
                    .build();
        }
    }

    @Data
    @Builder
    public static class UserInfo {
        private Long id;
        private String name;
        private String phone;
        private String email;
        private String city;
        private String educationStatus;
        private String profilePicUrl;
        private String subscriptionType;
        private LocalDateTime subscriptionExpiry;
        private String role;

        public static UserInfo from(User user) {
            return UserInfo.builder()
                    .id(user.getId())
                    .name(user.getName())
                    .phone(user.getPhone())
                    .email(user.getEmail())
                    .city(user.getCity())
                    .educationStatus(user.getEducationStatus())
                    .profilePicUrl(user.getProfilePicUrl())
                    .subscriptionType(user.getSubscriptionType().name())
                    .subscriptionExpiry(user.getSubscriptionExpiry())
                    .role(user.getRole().name())
                    .build();
        }
    }

    @Data
    @Builder
    public static class OtpResponse {
        private String message;
        private String phone;
        private boolean otpSent;
        private String otp;
    }
}
