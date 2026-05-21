package com.examprep.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "attempt_responses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttemptResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attempt_id", nullable = false)
    @JsonIgnore
    private TestAttempt attempt;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "question_id", nullable = false)
    @JsonIgnoreProperties({"createdBy"})
    private Question question;

    @Enumerated(EnumType.STRING)
    @Column(name = "selected_option")
    private Question.CorrectOption selectedOption;

    @Column(name = "is_correct")
    @Builder.Default
    private Boolean isCorrect = false;

    @Column(name = "is_marked_review")
    @Builder.Default
    private Boolean isMarkedReview = false;

    @Column(name = "is_visited")
    @Builder.Default
    private Boolean isVisited = false;

    @Column(name = "time_spent_seconds")
    @Builder.Default
    private Integer timeSpentSeconds = 0;

    @Column(name = "marks_obtained")
    @Builder.Default
    private Double marksObtained = 0.0;

    @Column(name = "question_order")
    private Integer questionOrder;
}
