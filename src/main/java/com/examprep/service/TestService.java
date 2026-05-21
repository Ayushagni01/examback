package com.examprep.service;

import com.examprep.dto.request.TestDTOs;
import com.examprep.dto.response.TestResultDTO;
import com.examprep.entity.*;
import com.examprep.exception.BadRequestException;
import com.examprep.exception.ResourceNotFoundException;
import com.examprep.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TestService {

    private final TestSeriesRepository testSeriesRepository;
    private final TestAttemptRepository attemptRepository;
    private final QuestionRepository questionRepository;

    /**
     * Get all test series with filters
     */
    public Page<TestSeries> getTestSeries(Long examId, String type, String accessType, int page, int size) {
        TestSeries.TestType testType = type != null ? TestSeries.TestType.valueOf(type.toUpperCase()) : null;
        TestSeries.AccessType access = accessType != null ? TestSeries.AccessType.valueOf(accessType.toUpperCase()) : null;
        return testSeriesRepository.findWithFilters(examId, testType, access,
                PageRequest.of(page, size, Sort.by("createdAt").descending()));
    }

    /**
     * Start a test — creates an attempt and returns test questions (without correct answers)
     */
    @Transactional
    public TestResultDTO.ActiveTestDTO startTest(Long testSeriesId, User user) {
        TestSeries testSeries = testSeriesRepository.findById(testSeriesId)
                .orElseThrow(() -> new ResourceNotFoundException("TestSeries", "id", testSeriesId));

        // Check if premium
        if (testSeries.getAccessType() == TestSeries.AccessType.PREMIUM &&
                user.getSubscriptionType() == User.SubscriptionType.FREE) {
            throw new BadRequestException("This test requires Prepp+ subscription.");
        }

        // Check if already in progress
        Optional<TestAttempt> existingAttempt = attemptRepository.findByUserIdAndTestSeriesIdAndStatus(
                user.getId(), testSeriesId, TestAttempt.AttemptStatus.IN_PROGRESS);

        TestAttempt attempt = existingAttempt.orElseGet(() -> {
            TestAttempt newAttempt = TestAttempt.builder()
                    .user(user)
                    .testSeries(testSeries)
                    .startedAt(LocalDateTime.now())
                    .totalMarks(testSeries.getTotalMarks())
                    .unattemptedCount(testSeries.getTotalQuestions())
                    .build();
            return attemptRepository.save(newAttempt);
        });

        // Build questions list (no correct answers)
        List<TestResultDTO.TestQuestionDTO> questions = testSeries.getQuestions().stream()
                .sorted(Comparator.comparingInt(TestSeriesQuestion::getQuestionOrder))
                .map(tsq -> buildTestQuestionDTO(tsq))
                .collect(Collectors.toList());

        // Restore saved responses if resuming
        Map<Long, String> savedResponses = new HashMap<>();
        Map<Long, Boolean> markedReview = new HashMap<>();
        attempt.getResponses().forEach(resp -> {
            if (resp.getSelectedOption() != null) {
                savedResponses.put(resp.getQuestion().getId(), resp.getSelectedOption().name());
            }
            markedReview.put(resp.getQuestion().getId(), resp.getIsMarkedReview());
        });

        return TestResultDTO.ActiveTestDTO.builder()
                .attemptId(attempt.getId())
                .testSeriesId(testSeriesId)
                .testTitle(testSeries.getTitle())
                .durationMinutes(testSeries.getDurationMinutes())
                .startedAt(attempt.getStartedAt())
                .questions(questions)
                .savedResponses(savedResponses)
                .markedReview(markedReview)
                .build();
    }

    /**
     * Auto-save a response during test
     */
    @Transactional
    public void saveResponse(Long attemptId, User user, TestDTOs.SaveResponseRequest request) {
        TestAttempt attempt = getAttemptForUser(attemptId, user);
        if (attempt.getStatus() != TestAttempt.AttemptStatus.IN_PROGRESS) {
            throw new BadRequestException("Test already submitted.");
        }

        Question question = questionRepository.findById(request.getQuestionId())
                .orElseThrow(() -> new ResourceNotFoundException("Question", "id", request.getQuestionId()));

        // Find or create response
        AttemptResponse response = attempt.getResponses().stream()
                .filter(r -> r.getQuestion().getId().equals(request.getQuestionId()))
                .findFirst()
                .orElseGet(() -> {
                    AttemptResponse r = AttemptResponse.builder()
                            .attempt(attempt)
                            .question(question)
                            .questionOrder(request.getQuestionId().intValue())
                            .build();
                    attempt.getResponses().add(r);
                    return r;
                });

        response.setSelectedOption(request.getSelectedOption());
        response.setIsMarkedReview(request.getIsMarkedReview());
        response.setIsVisited(true);
        response.setTimeSpentSeconds(request.getTimeSpentSeconds());

        attemptRepository.save(attempt);
    }

    /**
     * Submit the test and calculate results
     */
    @Transactional
    public TestResultDTO.TestResultResponse submitTest(Long attemptId, User user) {
        TestAttempt attempt = getAttemptForUser(attemptId, user);
        if (attempt.getStatus() != TestAttempt.AttemptStatus.IN_PROGRESS) {
            throw new BadRequestException("Test already submitted.");
        }

        TestSeries testSeries = attempt.getTestSeries();

        // Calculate score
        int correct = 0, wrong = 0, unattempted = 0;
        double score = 0.0;

        for (AttemptResponse response : attempt.getResponses()) {
            if (response.getSelectedOption() == null) continue;

            Question question = response.getQuestion();
            if (response.getSelectedOption() == question.getCorrectOption()) {
                response.setIsCorrect(true);
                double marks = question.getMarks() != null ? question.getMarks() : 1.0;
                response.setMarksObtained(marks);
                score += marks;
                correct++;
            } else {
                response.setIsCorrect(false);
                double negMarks = question.getNegativeMarks() != null ? question.getNegativeMarks() : 0.25;
                response.setMarksObtained(-negMarks);
                score -= negMarks;
                wrong++;
            }
        }

        unattempted = testSeries.getTotalQuestions() - correct - wrong;
        int totalAttempted = correct + wrong;
        double accuracy = totalAttempted > 0 ? ((double) correct / totalAttempted) * 100 : 0;
        int timeTaken = (int) java.time.Duration.between(attempt.getStartedAt(), LocalDateTime.now()).getSeconds();

        // Update attempt
        attempt.setScore(Math.max(score, 0));
        attempt.setCorrectCount(correct);
        attempt.setWrongCount(wrong);
        attempt.setUnattemptedCount(unattempted);
        attempt.setAccuracyPct(Math.round(accuracy * 100.0) / 100.0);
        attempt.setTimeTakenSeconds(timeTaken);
        attempt.setSubmittedAt(LocalDateTime.now());
        attempt.setStatus(TestAttempt.AttemptStatus.SUBMITTED);

        // Increment attempt count
        testSeries.setAttemptCount(testSeries.getAttemptCount() + 1);

        TestAttempt saved = attemptRepository.save(attempt);

        // Calculate rank and percentile
        long totalSubmitted = attemptRepository.countByTestSeriesIdAndStatus(
                testSeries.getId(), TestAttempt.AttemptStatus.SUBMITTED);
        long betterThan = attemptRepository.countByTestSeriesIdAndScoreGreaterThan(
                testSeries.getId(), saved.getScore());
        long rank = betterThan + 1;
        double percentile = totalSubmitted > 1 ?
                ((double)(totalSubmitted - rank) / (totalSubmitted - 1)) * 100 : 100.0;

        saved.setAllIndiaRank(rank);
        saved.setPercentile(Math.round(percentile * 100.0) / 100.0);
        attemptRepository.save(saved);

        // Get comparison stats
        Double topperScore = attemptRepository.findTopScoreByTestSeriesId(testSeries.getId());
        Double avgScore = attemptRepository.findAvgScoreByTestSeriesId(testSeries.getId());

        // Build sectional analysis
        List<TestResultDTO.SectionAnalysis> sectionAnalysis = buildSectionAnalysis(attempt);

        return TestResultDTO.TestResultResponse.builder()
                .attemptId(saved.getId())
                .testSeriesId(testSeries.getId())
                .testTitle(testSeries.getTitle())
                .score(saved.getScore())
                .totalMarks(testSeries.getTotalMarks())
                .correctCount(correct)
                .wrongCount(wrong)
                .unattemptedCount(unattempted)
                .totalQuestions(testSeries.getTotalQuestions())
                .accuracyPct(saved.getAccuracyPct())
                .percentile(saved.getPercentile())
                .allIndiaRank(rank)
                .totalAttempts(totalSubmitted)
                .timeTakenSeconds(timeTaken)
                .topperScore(topperScore)
                .averageScore(avgScore)
                .sectionAnalysis(sectionAnalysis)
                .status("SUBMITTED")
                .submittedAt(saved.getSubmittedAt())
                .build();
    }

    /**
     * Get question-wise review after submission
     */
    public List<TestResultDTO.QuestionReviewItem> getReview(Long attemptId, User user) {
        TestAttempt attempt = getAttemptForUser(attemptId, user);
        if (attempt.getStatus() != TestAttempt.AttemptStatus.SUBMITTED) {
            throw new BadRequestException("Test not yet submitted.");
        }

        return attempt.getResponses().stream()
                .sorted(Comparator.comparingInt(r -> r.getQuestionOrder() != null ? r.getQuestionOrder() : 0))
                .map(this::buildReviewItem)
                .collect(Collectors.toList());
    }

    // ---- Private helpers ----

    private TestAttempt getAttemptForUser(Long attemptId, User user) {
        TestAttempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new ResourceNotFoundException("Attempt", "id", attemptId));
        if (!attempt.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("Access denied to this attempt.");
        }
        return attempt;
    }

    private TestResultDTO.TestQuestionDTO buildTestQuestionDTO(TestSeriesQuestion tsq) {
        Question q = tsq.getQuestion();
        return TestResultDTO.TestQuestionDTO.builder()
                .questionId(q.getId())
                .questionOrder(tsq.getQuestionOrder())
                .section(tsq.getSection())
                .questionTextEn(q.getQuestionTextEn())
                .optionAEn(q.getOptionAEn())
                .optionBEn(q.getOptionBEn())
                .optionCEn(q.getOptionCEn())
                .optionDEn(q.getOptionDEn())
                .questionTextHi(q.getQuestionTextHi())
                .optionAHi(q.getOptionAHi())
                .optionBHi(q.getOptionBHi())
                .optionCHi(q.getOptionCHi())
                .optionDHi(q.getOptionDHi())
                .questionImageUrl(q.getQuestionImageUrl())
                .marks(tsq.getMarks())
                .difficulty(q.getDifficulty() != null ? q.getDifficulty().name() : "MEDIUM")
                .build();
    }

    private TestResultDTO.QuestionReviewItem buildReviewItem(AttemptResponse resp) {
        Question q = resp.getQuestion();
        return TestResultDTO.QuestionReviewItem.builder()
                .questionOrder(resp.getQuestionOrder())
                .questionTextEn(q.getQuestionTextEn())
                .questionTextHi(q.getQuestionTextHi())
                .questionImageUrl(q.getQuestionImageUrl())
                .optionAEn(q.getOptionAEn())
                .optionBEn(q.getOptionBEn())
                .optionCEn(q.getOptionCEn())
                .optionDEn(q.getOptionDEn())
                .correctOption(q.getCorrectOption().name())
                .selectedOption(resp.getSelectedOption() != null ? resp.getSelectedOption().name() : null)
                .isCorrect(resp.getIsCorrect())
                .isMarkedReview(resp.getIsMarkedReview())
                .isVisited(resp.getIsVisited())
                .explanationEn(q.getExplanationEn())
                .explanationHi(q.getExplanationHi())
                .timeSpentSeconds(resp.getTimeSpentSeconds())
                .marksObtained(resp.getMarksObtained())
                .difficulty(q.getDifficulty() != null ? q.getDifficulty().name() : "MEDIUM")
                .build();
    }

    private List<TestResultDTO.SectionAnalysis> buildSectionAnalysis(TestAttempt attempt) {
        Map<String, List<AttemptResponse>> bySection = attempt.getResponses().stream()
                .collect(Collectors.groupingBy(r ->
                        (r.getQuestion() != null && r.getQuestion().getSubject() != null)
                                ? r.getQuestion().getSubject() : "General"));

        return bySection.entrySet().stream().map(entry -> {
            String section = entry.getKey();
            List<AttemptResponse> responses = entry.getValue();
            int correct = (int) responses.stream().filter(r -> Boolean.TRUE.equals(r.getIsCorrect())).count();
            int wrong = (int) responses.stream()
                    .filter(r -> r.getSelectedOption() != null && Boolean.FALSE.equals(r.getIsCorrect())).count();
            int unattempted = (int) responses.stream().filter(r -> r.getSelectedOption() == null).count();
            double sectionScore = responses.stream()
                    .mapToDouble(r -> r.getMarksObtained() != null ? r.getMarksObtained() : 0)
                    .sum();
            double totalMarks = responses.stream()
                    .mapToDouble(r -> r.getQuestion().getMarks() != null ? r.getQuestion().getMarks() : 1.0)
                    .sum();
            int attempted = correct + wrong;
            double accuracy = attempted > 0 ? ((double) correct / attempted) * 100 : 0;

            return TestResultDTO.SectionAnalysis.builder()
                    .section(section)
                    .totalQuestions(responses.size())
                    .correct(correct)
                    .wrong(wrong)
                    .unattempted(unattempted)
                    .score(Math.round(sectionScore * 100.0) / 100.0)
                    .totalMarks(totalMarks)
                    .accuracyPct(Math.round(accuracy * 100.0) / 100.0)
                    .build();
        }).collect(Collectors.toList());
    }
}
