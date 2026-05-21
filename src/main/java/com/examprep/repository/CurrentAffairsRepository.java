package com.examprep.repository;

import com.examprep.entity.CurrentAffairs;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CurrentAffairsRepository extends JpaRepository<CurrentAffairs, Long> {
    Page<CurrentAffairs> findByIsActiveTrueOrderByPublishedDateDesc(Pageable pageable);
    List<CurrentAffairs> findByPublishedDateAndIsActiveTrue(LocalDate date);
    Page<CurrentAffairs> findByCategoryAndIsActiveTrueOrderByPublishedDateDesc(
            CurrentAffairs.Category category, Pageable pageable);
    List<CurrentAffairs> findByPublishedDateBetweenAndIsActiveTrue(LocalDate start, LocalDate end);
}
