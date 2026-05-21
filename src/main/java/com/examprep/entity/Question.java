package com.examprep.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "questions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Question extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "question_text_en", nullable = false, columnDefinition = "TEXT")
    private String questionTextEn;

    @Column(name = "question_text_hi", columnDefinition = "TEXT")
    private String questionTextHi;

    @Column(name = "question_image_url")
    private String questionImageUrl;

    @Column(name = "option_a_en", nullable = false, columnDefinition = "TEXT")
    private String optionAEn;

    @Column(name = "option_b_en", nullable = false, columnDefinition = "TEXT")
    private String optionBEn;

    @Column(name = "option_c_en", nullable = false, columnDefinition = "TEXT")
    private String optionCEn;

    @Column(name = "option_d_en", nullable = false, columnDefinition = "TEXT")
    private String optionDEn;

    @Column(name = "option_a_hi", columnDefinition = "TEXT")
    private String optionAHi;

    @Column(name = "option_b_hi", columnDefinition = "TEXT")
    private String optionBHi;

    @Column(name = "option_c_hi", columnDefinition = "TEXT")
    private String optionCHi;

    @Column(name = "option_d_hi", columnDefinition = "TEXT")
    private String optionDHi;

    @Enumerated(EnumType.STRING)
    @Column(name = "correct_option", nullable = false)
    private CorrectOption correctOption;

    @Column(name = "explanation_en", columnDefinition = "TEXT")
    private String explanationEn;

    @Column(name = "explanation_hi", columnDefinition = "TEXT")
    private String explanationHi;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty", nullable = false)
    @Builder.Default
    private Difficulty difficulty = Difficulty.MEDIUM;

    @Column(name = "subject")
    private String subject;

    @Column(name = "topic")
    private String topic;

    @Column(name = "tags")
    private String tags;

    @Column(name = "marks")
    @Builder.Default
    private Double marks = 1.0;

    @Column(name = "negative_marks")
    @Builder.Default
    private Double negativeMarks = 0.25;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    @JsonIgnore
    private User createdBy;

    public enum CorrectOption {
        A, B, C, D
    }

    public enum Difficulty {
        EASY, MEDIUM, HARD
    }
}
