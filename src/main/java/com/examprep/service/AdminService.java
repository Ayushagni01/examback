package com.examprep.service;

import com.examprep.dto.request.TestDTOs;
import com.examprep.entity.*;
import com.examprep.exception.BadRequestException;
import com.examprep.exception.ResourceNotFoundException;
import com.examprep.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final ExamCategoryRepository categoryRepository;
    private final ExamRepository examRepository;
    private final TestSeriesRepository testSeriesRepository;
    private final QuestionRepository questionRepository;
    private final CurrentAffairsRepository currentAffairsRepository;
    private final NewsArticleRepository newsArticleRepository;
    private final UserRepository userRepository;
    private final TestAttemptRepository attemptRepository;

    // ============ STATS ============

    public Map<String, Object> getDashboardStats() {
        return Map.of(
                "totalUsers", userRepository.count(),
                "totalExams", examRepository.count(),
                "totalTestSeries", testSeriesRepository.count(),
                "totalQuestions", questionRepository.count(),
                "totalAttempts", attemptRepository.count(),
                "totalCurrentAffairs", currentAffairsRepository.count(),
                "totalNews", newsArticleRepository.count()
        );
    }

    // ============ EXAM CATEGORIES ============

    @Transactional
    public ExamCategory createCategory(ExamCategory category) {
        category.setSlug(slugify(category.getName()));
        return categoryRepository.save(category);
    }

    @Transactional
    public ExamCategory updateCategory(Long id, ExamCategory updated) {
        ExamCategory cat = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ExamCategory", "id", id));
        if (updated.getName() != null) { cat.setName(updated.getName()); cat.setSlug(slugify(updated.getName())); }
        if (updated.getDescription() != null) cat.setDescription(updated.getDescription());
        if (updated.getIconUrl() != null) cat.setIconUrl(updated.getIconUrl());
        if (updated.getSortOrder() != null) cat.setSortOrder(updated.getSortOrder());
        if (updated.getIsActive() != null) cat.setIsActive(updated.getIsActive());
        return categoryRepository.save(cat);
    }

    @Transactional
    public void deleteCategory(Long id) {
        categoryRepository.deleteById(id);
    }

    // ============ EXAMS ============

    @Transactional
    public Exam createExam(Exam exam) {
        exam.setSlug(slugify(exam.getName()));
        return examRepository.save(exam);
    }

    @Transactional
    public Exam updateExam(Long id, Exam updated) {
        Exam exam = examRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exam", "id", id));
        if (updated.getName() != null) { exam.setName(updated.getName()); exam.setSlug(slugify(updated.getName())); }
        if (updated.getFullName() != null) exam.setFullName(updated.getFullName());
        if (updated.getDescription() != null) exam.setDescription(updated.getDescription());
        if (updated.getConductingBody() != null) exam.setConductingBody(updated.getConductingBody());
        if (updated.getIsActive() != null) exam.setIsActive(updated.getIsActive());
        if (updated.getIsFeatured() != null) exam.setIsFeatured(updated.getIsFeatured());
        return examRepository.save(exam);
    }

    @Transactional
    public void deleteExam(Long id) {
        examRepository.deleteById(id);
    }

    // ============ TEST SERIES ============

    @Transactional
    public TestSeries createTestSeries(TestDTOs.CreateTestSeriesRequest request) {
        TestSeries ts = TestSeries.builder()
                .title(request.getTitle())
                .slug(slugify(request.getTitle()))
                .description(request.getDescription())
                .type(request.getType())
                .totalQuestions(request.getTotalQuestions())
                .totalMarks(request.getTotalMarks())
                .durationMinutes(request.getDurationMinutes())
                .negativeMarking(request.getNegativeMarking())
                .languages(request.getLanguages())
                .accessType(request.getAccessType())
                .isLiveTest(request.getIsLiveTest())
                .build();

        if (request.getExamId() != null) {
            Exam exam = examRepository.findById(request.getExamId())
                    .orElseThrow(() -> new ResourceNotFoundException("Exam", "id", request.getExamId()));
            ts.setExam(exam);
        }

        if (request.getLiveStartAt() != null) ts.setLiveStartAt(LocalDateTime.parse(request.getLiveStartAt()));
        if (request.getLiveEndAt() != null) ts.setLiveEndAt(LocalDateTime.parse(request.getLiveEndAt()));

        return testSeriesRepository.save(ts);
    }

    @Transactional
    public TestSeries updateTestSeries(Long id, TestDTOs.CreateTestSeriesRequest request) {
        TestSeries ts = testSeriesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TestSeries", "id", id));
        if (request.getTitle() != null) { ts.setTitle(request.getTitle()); ts.setSlug(slugify(request.getTitle())); }
        if (request.getDescription() != null) ts.setDescription(request.getDescription());
        if (request.getType() != null) ts.setType(request.getType());
        if (request.getTotalQuestions() != null) ts.setTotalQuestions(request.getTotalQuestions());
        if (request.getTotalMarks() != null) ts.setTotalMarks(request.getTotalMarks());
        if (request.getDurationMinutes() != null) ts.setDurationMinutes(request.getDurationMinutes());
        if (request.getAccessType() != null) ts.setAccessType(request.getAccessType());
        return testSeriesRepository.save(ts);
    }

    @Transactional
    public void deleteTestSeries(Long id) {
        testSeriesRepository.deleteById(id);
    }

    // ============ QUESTIONS ============

    @Transactional
    public Question createQuestion(TestDTOs.CreateQuestionRequest request) {
        Question q = Question.builder()
                .questionTextEn(request.getQuestionTextEn())
                .questionTextHi(request.getQuestionTextHi())
                .questionImageUrl(request.getQuestionImageUrl())
                .optionAEn(request.getOptionAEn())
                .optionBEn(request.getOptionBEn())
                .optionCEn(request.getOptionCEn())
                .optionDEn(request.getOptionDEn())
                .optionAHi(request.getOptionAHi())
                .optionBHi(request.getOptionBHi())
                .optionCHi(request.getOptionCHi())
                .optionDHi(request.getOptionDHi())
                .correctOption(request.getCorrectOption())
                .explanationEn(request.getExplanationEn())
                .explanationHi(request.getExplanationHi())
                .difficulty(request.getDifficulty())
                .subject(request.getSubject())
                .topic(request.getTopic())
                .tags(request.getTags())
                .marks(request.getMarks())
                .negativeMarks(request.getNegativeMarks())
                .build();
        return questionRepository.save(q);
    }

    @Transactional
    public void addQuestionsToTestSeries(Long testSeriesId, List<TestDTOs.CreateQuestionRequest> requests, String section) {
        TestSeries ts = testSeriesRepository.findById(testSeriesId)
                .orElseThrow(() -> new ResourceNotFoundException("TestSeries", "id", testSeriesId));

        AtomicInteger order = new AtomicInteger(ts.getQuestions().size() + 1);

        for (TestDTOs.CreateQuestionRequest request : requests) {
            Question q = createQuestion(request);
            TestSeriesQuestion tsq = TestSeriesQuestion.builder()
                    .testSeries(ts)
                    .question(q)
                    .section(section != null ? section : request.getSubject())
                    .marks(request.getMarks())
                    .questionOrder(order.getAndIncrement())
                    .build();
            ts.getQuestions().add(tsq);
        }

        ts.setTotalQuestions(ts.getQuestions().size());
        testSeriesRepository.save(ts);
    }

    // ============ CURRENT AFFAIRS ============

    @Transactional
    public CurrentAffairs createCurrentAffairs(CurrentAffairs article) {
        if (article.getPublishedDate() == null) article.setPublishedDate(LocalDate.now());
        return currentAffairsRepository.save(article);
    }

    @Transactional
    public CurrentAffairs updateCurrentAffairs(Long id, CurrentAffairs updated) {
        CurrentAffairs ca = currentAffairsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CurrentAffairs", "id", id));
        if (updated.getTitle() != null) ca.setTitle(updated.getTitle());
        if (updated.getContent() != null) ca.setContent(updated.getContent());
        if (updated.getSummary() != null) ca.setSummary(updated.getSummary());
        if (updated.getCategory() != null) ca.setCategory(updated.getCategory());
        if (updated.getIsActive() != null) ca.setIsActive(updated.getIsActive());
        return currentAffairsRepository.save(ca);
    }

    @Transactional
    public void deleteCurrentAffairs(Long id) {
        currentAffairsRepository.deleteById(id);
    }

    // ============ NEWS ============

    @Transactional
    public NewsArticle createNews(NewsArticle article) {
        if (article.getPublishedDate() == null) article.setPublishedDate(LocalDate.now());
        return newsArticleRepository.save(article);
    }

    @Transactional
    public NewsArticle updateNews(Long id, NewsArticle updated) {
        NewsArticle news = newsArticleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("NewsArticle", "id", id));
        if (updated.getTitle() != null) news.setTitle(updated.getTitle());
        if (updated.getContent() != null) news.setContent(updated.getContent());
        if (updated.getSummary() != null) news.setSummary(updated.getSummary());
        if (updated.getCategory() != null) news.setCategory(updated.getCategory());
        if (updated.getIsActive() != null) news.setIsActive(updated.getIsActive());
        return newsArticleRepository.save(news);
    }

    @Transactional
    public void deleteNews(Long id) {
        newsArticleRepository.deleteById(id);
    }

    // ============ HELPER ============

    private String slugify(String text) {
        if (text == null) return "";
        return text.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("[\\s]+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }
}
