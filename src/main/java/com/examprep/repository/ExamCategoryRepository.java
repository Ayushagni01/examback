package com.examprep.repository;

import com.examprep.entity.ExamCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExamCategoryRepository extends JpaRepository<ExamCategory, Long> {
    Optional<ExamCategory> findBySlug(String slug);
    List<ExamCategory> findByParentCategoryIsNullAndIsActiveTrue();
    List<ExamCategory> findByParentCategoryIdAndIsActiveTrue(Long parentId);
    List<ExamCategory> findByIsActiveTrueOrderBySortOrderAsc();
}
