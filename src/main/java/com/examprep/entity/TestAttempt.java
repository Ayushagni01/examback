package com.examprep.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "test_attempts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestAttempt extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"passwordHash"})
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_series_id", nullable = false)
    @JsonIgnoreProperties({"questions", "attempts", "exam"})
    private TestSeries testSeries;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "score")
    @Builder.Default
    private Double score = 0.0;

    @Column(name = "total_marks")
    private Double totalMarks;

    @Column(name = "accuracy_pct")
    @Builder.Default
    private Double accuracyPct = 0.0;

    @Column(name = "percentile")
    @Builder.Default
    private Double percentile = 0.0;

    @Column(name = "all_india_rank")
    private Long allIndiaRank;

    @Column(name = "time_taken_seconds")
    private Integer timeTakenSeconds;

    @Column(name = "correct_count")
    @Builder.Default
    private Integer correctCount = 0;

    @Column(name = "wrong_count")
    @Builder.Default
    private Integer wrongCount = 0;

    @Column(name = "unattempted_count")
    @Builder.Default
    private Integer unattemptedCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private AttemptStatus status = AttemptStatus.IN_PROGRESS;

    @Column(name = "language_used")
    @Builder.Default
    private String languageUsed = "English";

    @OneToMany(mappedBy = "attempt", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @JsonIgnoreProperties({"attempt"})
    private List<AttemptResponse> responses = new ArrayList<>();

    public enum AttemptStatus {
        IN_PROGRESS, SUBMITTED, ABANDONED
    }
}
