package com.examprep.dto.response;

import com.examprep.entity.*;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class TestResultDTO {

    @Data
    @Builder
    public static class TestResultResponse {
        private Long attemptId;
        private Long testSeriesId;
        private String testTitle;

        // Score info
        private Double score;
        private Double totalMarks;
        private Integer correctCount;
        private Integer wrongCount;
        private Integer unattemptedCount;
        private Integer totalQuestions;

        // Performance metrics
        private Double accuracyPct;
        private Double percentile;
        private Long allIndiaRank;
        private Long totalAttempts;

        // Time
        private Integer timeTakenSeconds;

        // Comparisons
        private Double topperScore;
        private Double averageScore;

        // Sectional analysis
        private List<SectionAnalysis> sectionAnalysis;

        // Status
        private String status;
        private LocalDateTime submittedAt;
    }

    @Data
    @Builder
    public static class SectionAnalysis {
        private String section;
        private Integer totalQuestions;
        private Integer correct;
        private Integer wrong;
        private Integer unattempted;
        private Double score;
        private Double totalMarks;
        private Double accuracyPct;
    }

    @Data
    @Builder
    public static class QuestionReviewItem {
        private Integer questionOrder;
        private String section;
        private String questionTextEn;
        private String questionTextHi;
        private String questionImageUrl;

        private String optionAEn;
        private String optionBEn;
        private String optionCEn;
        private String optionDEn;

        private String correctOption;
        private String selectedOption;

        private Boolean isCorrect;
        private Boolean isMarkedReview;
        private Boolean isVisited;

        private String explanationEn;
        private String explanationHi;

        private Integer timeSpentSeconds;
        private Double marksObtained;
        private String difficulty;
    }

    @Data
    @Builder
    public static class TestSeriesDetailDTO {
        private Long id;
        private String title;
        private String slug;
        private String description;
        private String type;
        private Integer totalQuestions;
        private Double totalMarks;
        private Integer durationMinutes;
        private Double negativeMarking;
        private String languages;
        private String accessType;
        private Long attemptCount;
        private Double avgRating;
        private Boolean isLiveTest;
        private LocalDateTime liveStartAt;
        private LocalDateTime liveEndAt;
        private String examName;
        private String examSlug;
        private List<SectionInfo> sections;
    }

    @Data
    @Builder
    public static class SectionInfo {
        private String name;
        private Integer questionCount;
        private Double totalMarks;
    }

    @Data
    @Builder
    public static class ActiveTestDTO {
        private Long attemptId;
        private Long testSeriesId;
        private String testTitle;
        private Integer durationMinutes;
        private LocalDateTime startedAt;
        private List<TestQuestionDTO> questions;
        private Map<Long, String> savedResponses;  // questionId -> selectedOption
        private Map<Long, Boolean> markedReview;   // questionId -> isMarkedReview
    }

    @Data
    @Builder
    public static class TestQuestionDTO {
        private Long questionId;
        private Integer questionOrder;
        private String section;

        // English
        private String questionTextEn;
        private String optionAEn;
        private String optionBEn;
        private String optionCEn;
        private String optionDEn;

        // Hindi
        private String questionTextHi;
        private String optionAHi;
        private String optionBHi;
        private String optionCHi;
        private String optionDHi;

        private String questionImageUrl;
        private Double marks;
        private String difficulty;
        // Note: correctOption NOT included during active test
    }
}
