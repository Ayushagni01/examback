package com.examprep.repository;

import com.examprep.entity.Exam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExamRepository extends JpaRepository<Exam, Long> {
    Optional<Exam> findBySlug(String slug);
    List<Exam> findByCategoryIdAndIsActiveTrue(Long categoryId);
    List<Exam> findByIsFeaturedTrueAndIsActiveTrueOrderByExamDateAsc();

    @Query("SELECT e FROM Exam e WHERE e.isActive = true AND e.examDate >= :today ORDER BY e.examDate ASC")
    List<Exam> findUpcomingExams(@Param("today") LocalDate today);

    @Query("SELECT e FROM Exam e WHERE e.isActive = true AND " +
           "(LOWER(e.name) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(e.fullName) LIKE LOWER(CONCAT('%', :q, '%')))")
    Page<Exam> searchExams(@Param("q") String query, Pageable pageable);

    Page<Exam> findByCategoryIdAndIsActiveTrue(Long categoryId, Pageable pageable);
}
