package com.examprep.repository;

import com.examprep.entity.TestSeries;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TestSeriesRepository extends JpaRepository<TestSeries, Long> {
    Optional<TestSeries> findBySlug(String slug);
    Page<TestSeries> findByExamIdAndIsActiveTrue(Long examId, Pageable pageable);
    Page<TestSeries> findByExamIdAndTypeAndIsActiveTrue(Long examId, TestSeries.TestType type, Pageable pageable);
    Page<TestSeries> findByAccessTypeAndIsActiveTrue(TestSeries.AccessType accessType, Pageable pageable);

    @Query("SELECT ts FROM TestSeries ts WHERE ts.isLiveTest = true AND ts.isActive = true " +
           "AND ts.liveStartAt > :now ORDER BY ts.liveStartAt ASC")
    List<TestSeries> findUpcomingLiveTests(@Param("now") LocalDateTime now);

    @Query("SELECT ts FROM TestSeries ts WHERE ts.isActive = true AND " +
           "(:examId IS NULL OR ts.exam.id = :examId) AND " +
           "(:type IS NULL OR ts.type = :type) AND " +
           "(:accessType IS NULL OR ts.accessType = :accessType)")
    Page<TestSeries> findWithFilters(
            @Param("examId") Long examId,
            @Param("type") TestSeries.TestType type,
            @Param("accessType") TestSeries.AccessType accessType,
            Pageable pageable);
}
