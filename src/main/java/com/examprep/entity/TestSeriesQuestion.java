package com.examprep.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "test_series_questions",
       uniqueConstraints = @UniqueConstraint(columnNames = {"test_series_id", "question_order"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestSeriesQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_series_id", nullable = false)
    @JsonIgnore
    private TestSeries testSeries;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    @JsonIgnoreProperties({"createdBy"})
    private Question question;

    @Column(name = "section")
    private String section;

    @Column(name = "marks")
    @Builder.Default
    private Double marks = 1.0;

    @Column(name = "question_order")
    private Integer questionOrder;
}
