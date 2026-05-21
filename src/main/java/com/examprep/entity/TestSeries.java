package com.examprep.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "test_series")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestSeries extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "slug", unique = true, nullable = false)
    private String slug;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private TestType type;

    @Column(name = "total_questions", nullable = false)
    private Integer totalQuestions;

    @Column(name = "total_marks", nullable = false)
    private Double totalMarks;

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @Column(name = "negative_marking")
    @Builder.Default
    private Double negativeMarking = 0.25;

    @Column(name = "languages")
    @Builder.Default
    private String languages = "English,Hindi";

    @Enumerated(EnumType.STRING)
    @Column(name = "access_type", nullable = false)
    @Builder.Default
    private AccessType accessType = AccessType.FREE;

    @Column(name = "attempt_count")
    @Builder.Default
    private Long attemptCount = 0L;

    @Column(name = "avg_rating")
    @Builder.Default
    private Double avgRating = 0.0;

    @Column(name = "is_live_test")
    @Builder.Default
    private Boolean isLiveTest = false;

    @Column(name = "live_start_at")
    private LocalDateTime liveStartAt;

    @Column(name = "live_end_at")
    private LocalDateTime liveEndAt;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id")
    @JsonIgnoreProperties({"testSeries", "category"})
    private Exam exam;

    @OneToMany(mappedBy = "testSeries", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @JsonIgnore
    private List<TestSeriesQuestion> questions = new ArrayList<>();

    @OneToMany(mappedBy = "testSeries", cascade = CascadeType.ALL)
    @Builder.Default
    @JsonIgnore
    private List<TestAttempt> attempts = new ArrayList<>();

    public enum TestType {
        FULL_MOCK, SECTIONAL, TOPIC_WISE, PREVIOUS_YEAR
    }

    public enum AccessType {
        FREE, PREMIUM
    }
}
