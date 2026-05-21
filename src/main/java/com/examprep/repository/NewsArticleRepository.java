package com.examprep.repository;

import com.examprep.entity.NewsArticle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NewsArticleRepository extends JpaRepository<NewsArticle, Long> {
    Page<NewsArticle> findByIsActiveTrueOrderByPublishedDateDesc(Pageable pageable);
    Page<NewsArticle> findByCategoryAndIsActiveTrueOrderByPublishedDateDesc(
            NewsArticle.Category category, Pageable pageable);
    Page<NewsArticle> findByExamIdAndIsActiveTrueOrderByPublishedDateDesc(Long examId, Pageable pageable);
}
