package com.examprep.dto.request;

import com.examprep.entity.Question;
import com.examprep.entity.TestSeries;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

public class TestDTOs {

    @Data
    public static class SaveResponseRequest {
        @NotNull
        private Long questionId;

        private Question.CorrectOption selectedOption;

        private Boolean isMarkedReview = false;

        private Integer timeSpentSeconds = 0;
    }

    @Data
    public static class SubmitAttemptRequest {
        private String languageUsed = "English";
    }

    @Data
    public static class CreateTestSeriesRequest {
        @NotBlank
        private String title;

        private String description;

        @NotNull
        private TestSeries.TestType type;

        @NotNull
        @Positive
        private Integer totalQuestions;

        @NotNull
        @Positive
        private Double totalMarks;

        @NotNull
        @Positive
        private Integer durationMinutes;

        private Double negativeMarking = 0.25;

        private String languages = "English,Hindi";

        private TestSeries.AccessType accessType = TestSeries.AccessType.FREE;

        private Long examId;

        private Boolean isLiveTest = false;

        private String liveStartAt;

        private String liveEndAt;
    }

    @Data
    public static class CreateQuestionRequest {
        @NotBlank
        private String questionTextEn;
        private String questionTextHi;
        private String questionImageUrl;

        @NotBlank
        private String optionAEn;
        @NotBlank
        private String optionBEn;
        @NotBlank
        private String optionCEn;
        @NotBlank
        private String optionDEn;

        private String optionAHi;
        private String optionBHi;
        private String optionCHi;
        private String optionDHi;

        @NotNull
        private Question.CorrectOption correctOption;

        private String explanationEn;
        private String explanationHi;

        private Question.Difficulty difficulty = Question.Difficulty.MEDIUM;

        private String subject;
        private String topic;
        private String tags;

        private Double marks = 1.0;
        private Double negativeMarks = 0.25;
    }
}
