package com.examprep.repository;

import com.examprep.entity.TestAttempt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TestAttemptRepository extends JpaRepository<TestAttempt, Long> {

    Optional<TestAttempt> findByUserIdAndTestSeriesIdAndStatus(
            Long userId, Long testSeriesId, TestAttempt.AttemptStatus status);

    Page<TestAttempt> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    List<TestAttempt> findByTestSeriesIdAndStatus(Long testSeriesId, TestAttempt.AttemptStatus status);

    long countByTestSeriesIdAndStatus(Long testSeriesId, TestAttempt.AttemptStatus status);

    @Query("SELECT COUNT(ta) FROM TestAttempt ta WHERE ta.testSeries.id = :testSeriesId " +
           "AND ta.status = 'SUBMITTED' AND ta.score > :score")
    long countByTestSeriesIdAndScoreGreaterThan(
            @Param("testSeriesId") Long testSeriesId,
            @Param("score") Double score);

    @Query("SELECT AVG(ta.score) FROM TestAttempt ta WHERE ta.testSeries.id = :testSeriesId " +
           "AND ta.status = 'SUBMITTED'")
    Double findAvgScoreByTestSeriesId(@Param("testSeriesId") Long testSeriesId);

    @Query("SELECT MAX(ta.score) FROM TestAttempt ta WHERE ta.testSeries.id = :testSeriesId " +
           "AND ta.status = 'SUBMITTED'")
    Double findTopScoreByTestSeriesId(@Param("testSeriesId") Long testSeriesId);
}
