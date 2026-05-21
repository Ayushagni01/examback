package com.examprep.repository;

import com.examprep.entity.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    @Query("SELECT q FROM Question q WHERE q.isActive = true AND " +
           "(:subject IS NULL OR q.subject = :subject) AND " +
           "(:topic IS NULL OR q.topic = :topic) AND " +
           "(:difficulty IS NULL OR q.difficulty = :difficulty)")
    Page<Question> findWithFilters(
            @Param("subject") String subject,
            @Param("topic") String topic,
            @Param("difficulty") Question.Difficulty difficulty,
            Pageable pageable);

    Page<Question> findBySubjectAndIsActiveTrue(String subject, Pageable pageable);
    Page<Question> findByTopicAndIsActiveTrue(String topic, Pageable pageable);
    long countByIsActiveTrue();
}
