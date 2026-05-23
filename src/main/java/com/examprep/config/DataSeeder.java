package com.examprep.config;

import com.examprep.entity.*;
import com.examprep.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ExamCategoryRepository categoryRepository;
    private final ExamRepository examRepository;
    private final TestSeriesRepository testSeriesRepository;
    private final QuestionRepository questionRepository;
    private final CurrentAffairsRepository currentAffairsRepository;
    private final NewsArticleRepository newsArticleRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (testSeriesRepository.findBySlug("upsc-cse-prelims-mock-1").isEmpty()) {
            log.info("UPSC CSE Prelims Mock 1 not found. Seeding now...");
            seedUpscMockTest();
        } else {
            testSeriesRepository.findBySlug("upsc-cse-prelims-mock-1").ifPresent(test -> {
                if (test.getAccessType() != TestSeries.AccessType.PREMIUM) {
                    test.setAccessType(TestSeries.AccessType.PREMIUM);
                    testSeriesRepository.save(test);
                    log.info("Updated UPSC Mock Test to PREMIUM for testing.");
                }
            });
            testSeriesRepository.findBySlug("ssc-cgl-mock-1").ifPresent(test -> {
                if (test.getAccessType() != TestSeries.AccessType.PREMIUM) {
                    test.setAccessType(TestSeries.AccessType.PREMIUM);
                    testSeriesRepository.save(test);
                    log.info("Updated SSC Mock Test to PREMIUM for testing.");
                }
            });
        }

        if (userRepository.count() > 0) {
            log.info("Database already seeded. Skipping.");
            return;
        }
        log.info("Seeding database with initial data...");
        seedAdminUser();
        List<ExamCategory> cats = seedCategories();
        List<Exam> exams = seedExams(cats);
        seedTestSeriesWithQuestions(exams);
        seedCurrentAffairs();
        seedNews();
        log.info("Database seeding completed!");
    }

    private void seedAdminUser() {
        userRepository.save(User.builder()
                .name("Admin").phone("+919999999999").role(User.Role.ROLE_ADMIN)
                .subscriptionType(User.SubscriptionType.PREMIUM).build());
        log.info("Admin user created: +919999999999");
    }

    private List<ExamCategory> seedCategories() {
        List<ExamCategory> cats = new ArrayList<>();
        String[][] data = {
            {"SSC", "ssc", "Staff Selection Commission exams"},
            {"Banking", "banking", "Banking sector recruitment exams"},
            {"UPSC", "upsc", "Union Public Service Commission exams"},
            {"Railways", "railways", "Indian Railways recruitment exams"},
            {"State PSC", "state-psc", "State Public Service Commission exams"}
        };
        int order = 1;
        for (String[] d : data) {
            final int currentOrder = order++;
            cats.add(categoryRepository.findBySlug(d[1]).orElseGet(() -> 
                categoryRepository.save(ExamCategory.builder()
                    .name(d[0]).slug(d[1]).description(d[2]).sortOrder(currentOrder).build())
            ));
        }
        return cats;
    }

    private List<Exam> seedExams(List<ExamCategory> cats) {
        List<Exam> exams = new ArrayList<>();
        // SSC CGL
        exams.add(examRepository.findBySlug("ssc-cgl").orElseGet(() -> 
            examRepository.save(Exam.builder().name("SSC CGL").slug("ssc-cgl").fullName("Combined Graduate Level Examination")
                .description("Staff Selection Commission CGL for Group B and C posts").conductingBody("SSC").category(cats.get(0))
                .isFeatured(true).examDate(LocalDate.of(2026, 8, 15)).vacancyCount(8000).build())
        ));
        // SSC CHSL
        exams.add(examRepository.findBySlug("ssc-chsl").orElseGet(() -> 
            examRepository.save(Exam.builder().name("SSC CHSL").slug("ssc-chsl").fullName("Combined Higher Secondary Level")
                .description("For LDC, JSA, PA, SA and DEO posts").conductingBody("SSC").category(cats.get(0))
                .examDate(LocalDate.of(2026, 9, 10)).vacancyCount(4500).build())
        ));
        // Banking
        exams.add(examRepository.findBySlug("ibps-po").orElseGet(() -> 
            examRepository.save(Exam.builder().name("IBPS PO").slug("ibps-po").fullName("Institute of Banking Personnel Selection - PO")
                .description("Probationary Officer recruitment for public sector banks").conductingBody("IBPS").category(cats.get(1))
                .isFeatured(true).examDate(LocalDate.of(2026, 10, 5)).vacancyCount(3500).build())
        ));
        // SBI PO
        exams.add(examRepository.findBySlug("sbi-po").orElseGet(() -> 
            examRepository.save(Exam.builder().name("SBI PO").slug("sbi-po").fullName("State Bank of India PO")
                .description("SBI Probationary Officer recruitment").conductingBody("SBI").category(cats.get(1))
                .isFeatured(true).examDate(LocalDate.of(2026, 11, 20)).vacancyCount(2000).build())
        ));
        // UPSC
        exams.add(examRepository.findBySlug("upsc-cse").orElseGet(() -> 
            examRepository.save(Exam.builder().name("UPSC CSE").slug("upsc-cse").fullName("Civil Services Examination")
                .description("India's premier civil services examination for IAS, IPS, IFS").conductingBody("UPSC").category(cats.get(2))
                .isFeatured(true).examDate(LocalDate.of(2026, 6, 1)).vacancyCount(1000).build())
        ));
        // Railways
        exams.add(examRepository.findBySlug("rrb-ntpc").orElseGet(() -> 
            examRepository.save(Exam.builder().name("RRB NTPC").slug("rrb-ntpc").fullName("Non-Technical Popular Categories")
                .description("Railway recruitment for various non-technical posts").conductingBody("RRB").category(cats.get(3))
                .isFeatured(true).examDate(LocalDate.of(2026, 7, 15)).vacancyCount(35000).build())
        ));
        return exams;
    }

    private void seedTestSeriesWithQuestions(List<Exam> exams) {
        // SSC CGL Mock
        TestSeries sscMock = testSeriesRepository.findBySlug("ssc-cgl-mock-1").orElseGet(() -> 
            testSeriesRepository.save(TestSeries.builder()
                .title("SSC CGL Tier 1 Full Mock Test 1").slug("ssc-cgl-mock-1")
                .description("Complete 100-question mock test based on latest SSC CGL pattern")
                .type(TestSeries.TestType.FULL_MOCK).totalQuestions(25).totalMarks(50.0)
                .durationMinutes(60).negativeMarking(0.50).exam(exams.get(0))
                .accessType(TestSeries.AccessType.PREMIUM).build())
        );
        if (sscMock.getQuestions().isEmpty()) {
            addSSCQuestions(sscMock);
        }

        // IBPS PO Mock
        TestSeries ibpsMock = testSeriesRepository.findBySlug("ibps-po-mock-1").orElseGet(() -> 
            testSeriesRepository.save(TestSeries.builder()
                .title("IBPS PO Prelims Mock Test 1").slug("ibps-po-mock-1")
                .description("Practice test based on IBPS PO Prelims pattern")
                .type(TestSeries.TestType.FULL_MOCK).totalQuestions(25).totalMarks(25.0)
                .durationMinutes(30).negativeMarking(0.25).exam(exams.get(2)).build())
        );
        if (ibpsMock.getQuestions().isEmpty()) {
            addBankingQuestions(ibpsMock);
        }

        // Free sectional
        TestSeries gkTest = testSeriesRepository.findBySlug("gk-quick-test").orElseGet(() -> 
            testSeriesRepository.save(TestSeries.builder()
                .title("General Knowledge Quick Test").slug("gk-quick-test")
                .description("15-question GK quiz covering national and international affairs")
                .type(TestSeries.TestType.TOPIC_WISE).totalQuestions(15).totalMarks(15.0)
                .durationMinutes(15).negativeMarking(0.0).accessType(TestSeries.AccessType.FREE).build())
        );
        if (gkTest.getQuestions().isEmpty()) {
            addGKQuestions(gkTest);
        }
    }

    private void addSSCQuestions(TestSeries ts) {
        String[][] qs = {
            {"What is the capital of India?", "Mumbai", "New Delhi", "Kolkata", "Chennai", "B", "New Delhi has been the capital of India since 1911.", "General Awareness"},
            {"Which planet is known as the Red Planet?", "Venus", "Mars", "Jupiter", "Saturn", "B", "Mars appears red due to iron oxide on its surface.", "General Awareness"},
            {"If 2x + 5 = 15, what is x?", "3", "5", "7", "10", "B", "2x = 10, so x = 5.", "Quantitative Aptitude"},
            {"What is 15% of 200?", "20", "25", "30", "35", "C", "15/100 × 200 = 30.", "Quantitative Aptitude"},
            {"Find the odd one out: 2, 5, 10, 17, __(28)?", "24", "26", "28", "30", "B", "Differences are 3, 5, 7, 9. So next is 17+9=26.", "General Intelligence"},
            {"Which river is the longest in India?", "Yamuna", "Godavari", "Ganga", "Brahmaputra", "C", "The Ganga is approximately 2,525 km long.", "General Awareness"},
            {"A train 120m long passes a pole in 12 seconds. What is its speed?", "36 km/h", "10 km/h", "30 km/h", "40 km/h", "A", "Speed = 120/12 = 10 m/s = 36 km/h.", "Quantitative Aptitude"},
            {"Choose the synonym of 'Abundant':", "Scarce", "Plentiful", "Meager", "Sparse", "B", "Abundant means existing in large quantities.", "English Comprehension"},
            {"Who wrote the Indian National Anthem?", "Bankim Chandra", "Rabindranath Tagore", "Sarojini Naidu", "Subhash Bose", "B", "Jana Gana Mana was written by Rabindranath Tagore.", "General Awareness"},
            {"What is the SI unit of force?", "Joule", "Pascal", "Newton", "Watt", "C", "Force is measured in Newtons (N).", "General Awareness"},
            {"If a:b = 2:3 and b:c = 4:5, find a:c.", "8:15", "6:10", "4:5", "2:5", "A", "a:b:c = 8:12:15, so a:c = 8:15.", "Quantitative Aptitude"},
            {"Which article of the Constitution abolishes untouchability?", "Article 14", "Article 15", "Article 17", "Article 19", "C", "Article 17 abolishes untouchability.", "General Awareness"},
            {"Choose the antonym of 'Benevolent':", "Kind", "Malevolent", "Generous", "Caring", "B", "Malevolent means having evil intentions.", "English Comprehension"},
            {"In a mirror image, which direction does left become?", "Up", "Down", "Right", "Same", "C", "In a mirror, left and right are reversed.", "General Intelligence"},
            {"The HCF of 12 and 18 is:", "2", "3", "6", "9", "C", "Factors of 12: 1,2,3,4,6,12. Factors of 18: 1,2,3,6,9,18. HCF = 6.", "Quantitative Aptitude"},
            {"Which gas is most abundant in Earth's atmosphere?", "Oxygen", "Carbon Dioxide", "Nitrogen", "Argon", "C", "Nitrogen makes up about 78% of the atmosphere.", "General Awareness"},
            {"Fill in the blank: She ___ to school every day.", "go", "goes", "going", "gone", "B", "Third person singular present tense uses 'goes'.", "English Comprehension"},
            {"What is the square root of 144?", "10", "11", "12", "14", "C", "12 × 12 = 144.", "Quantitative Aptitude"},
            {"Who is known as the Father of the Indian Constitution?", "Mahatma Gandhi", "Jawaharlal Nehru", "B.R. Ambedkar", "Sardar Patel", "C", "Dr. B.R. Ambedkar chaired the drafting committee.", "General Awareness"},
            {"Complete the series: 3, 6, 12, 24, ?", "36", "48", "30", "42", "B", "Each number doubles: 24 × 2 = 48.", "General Intelligence"},
            {"Profit % when CP=80, SP=100?", "20%", "25%", "15%", "30%", "B", "Profit = 20, Profit% = (20/80)×100 = 25%.", "Quantitative Aptitude"},
            {"Which vitamin is produced by sunlight?", "Vitamin A", "Vitamin B", "Vitamin C", "Vitamin D", "D", "Sunlight triggers Vitamin D synthesis in the skin.", "General Awareness"},
            {"Choose the correctly spelled word:", "Accomodate", "Accommodate", "Acommodate", "Acomodate", "B", "The correct spelling has double 'c' and double 'm'.", "English Comprehension"},
            {"If APPLE is coded as 50, what is BAT?", "23", "25", "27", "29", "A", "A=1,P=16,P=16,L=12,E=5=50. B=2,A=1,T=20=23.", "General Intelligence"},
            {"The largest desert in India is:", "Kutch", "Thar", "Ladakh", "Spiti", "B", "The Thar Desert covers ~200,000 sq km in Rajasthan.", "General Awareness"},
        };
        addQuestionBatch(ts, qs);
    }

    private void addBankingQuestions(TestSeries ts) {
        String[][] qs = {
            {"What does RBI stand for?", "Reserve Bank of India", "Rural Bank of India", "Regional Bank of India", "Revenue Bank of India", "A", "RBI is the central banking institution of India.", "General Awareness"},
            {"Current repo rate is set by which body?", "SEBI", "RBI MPC", "Finance Ministry", "NITI Aayog", "B", "The Monetary Policy Committee of RBI sets the repo rate.", "General Awareness"},
            {"Simple interest on Rs.5000 at 10% for 2 years?", "800", "900", "1000", "1100", "C", "SI = 5000×10×2/100 = 1000.", "Quantitative Aptitude"},
            {"NEFT stands for?", "National Electronic Fund Transfer", "New Electronic Fund Transfer", "National E-Fund Transfer", "None of the above", "A", "NEFT enables bank-to-bank electronic fund transfers.", "General Awareness"},
            {"A boat goes 12 km upstream in 3 hrs and downstream in 2 hrs. Speed of stream?", "1 km/h", "2 km/h", "1.5 km/h", "3 km/h", "A", "Upstream=4, downstream=6. Stream=(6-4)/2=1 km/h.", "Quantitative Aptitude"},
            {"Which of these is not a public sector bank?", "SBI", "PNB", "HDFC", "BOB", "C", "HDFC Bank is a private sector bank.", "General Awareness"},
            {"What is 3/8 as a percentage?", "35%", "37.5%", "38%", "40%", "B", "3/8 × 100 = 37.5%.", "Quantitative Aptitude"},
            {"GDP stands for?", "Gross Domestic Product", "General Domestic Product", "Gross Direct Product", "Grand Domestic Product", "A", "GDP measures the total economic output of a country.", "General Awareness"},
            {"Average of 10, 20, 30, 40, 50?", "25", "30", "35", "40", "B", "Sum=150, Average=150/5=30.", "Quantitative Aptitude"},
            {"Choose the error: 'He don't know the answer.'", "He", "don't", "know", "answer", "B", "Correct usage: 'He doesn't know the answer.'", "English"},
            {"What is compound interest on Rs.1000 at 10% for 2 years?", "200", "210", "220", "250", "B", "CI = 1000(1.1²-1) = 1000×0.21 = 210.", "Quantitative Aptitude"},
            {"KYC stands for?", "Know Your Customer", "Keep Your Cash", "Know Your Credit", "Key Your Code", "A", "KYC is a banking regulation for customer verification.", "General Awareness"},
            {"If 6 men do work in 10 days, 10 men do it in?", "4 days", "5 days", "6 days", "8 days", "C", "6×10 = 10×x; x = 6 days.", "Quantitative Aptitude"},
            {"Fill in: Neither Ram ___ Shyam was present.", "or", "and", "nor", "but", "C", "Neither...nor is the correct correlative conjunction.", "English"},
            {"Which organisation regulates stock markets in India?", "RBI", "SEBI", "IRDA", "NABARD", "B", "SEBI regulates the securities market in India.", "General Awareness"},
            {"Ratio of boys to girls is 3:2. If total 50, how many boys?", "20", "25", "30", "35", "C", "Boys = (3/5)×50 = 30.", "Quantitative Aptitude"},
            {"SLR stands for?", "Standard Liquidity Ratio", "Statutory Liquidity Ratio", "Special Lending Rate", "Simple Liquidity Rate", "B", "SLR is the minimum percentage of deposits a bank must hold.", "General Awareness"},
            {"A number increased by 20% becomes 60. The number is?", "48", "50", "52", "55", "B", "x × 1.2 = 60, x = 50.", "Quantitative Aptitude"},
            {"Choose the synonym of 'Prudent':", "Reckless", "Careful", "Hasty", "Foolish", "B", "Prudent means acting with care and thought.", "English"},
            {"What is the full form of IFSC code?", "Indian Financial System Code", "International Finance System Code", "Indian Fund System Code", "Internal Financial System Code", "A", "IFSC is used for electronic payment systems like NEFT.", "General Awareness"},
            {"Perimeter of a rectangle with l=10, b=5?", "25", "30", "50", "15", "B", "Perimeter = 2(l+b) = 2(15) = 30.", "Quantitative Aptitude"},
            {"MUDRA bank was launched in which year?", "2014", "2015", "2016", "2017", "B", "MUDRA Bank was launched on April 8, 2015.", "General Awareness"},
            {"Time taken by Rs.400 to become Rs.480 at 5% SI?", "2 years", "3 years", "4 years", "5 years", "C", "SI=80, T=80×100/(400×5) = 4 years.", "Quantitative Aptitude"},
            {"Identify the voice: 'The cake was eaten by her.'", "Active", "Passive", "Imperative", "Exclamatory", "B", "The subject receives the action, making it passive voice.", "English"},
            {"NABARD stands for?", "National Bank for Agriculture and Rural Development", "National Board of Agriculture", "New Agricultural Bank of Development", "None", "A", "NABARD is an apex regulatory body for rural finance.", "General Awareness"},
        };
        addQuestionBatch(ts, qs);
    }

    private void addGKQuestions(TestSeries ts) {
        String[][] qs = {
            {"Which is the largest continent by area?", "Africa", "North America", "Asia", "Europe", "C", "Asia covers about 44.58 million km².", "General Knowledge"},
            {"Who invented the telephone?", "Thomas Edison", "Alexander Graham Bell", "Nikola Tesla", "Guglielmo Marconi", "B", "Bell patented the telephone in 1876.", "General Knowledge"},
            {"Which country has the largest population?", "India", "USA", "China", "Indonesia", "A", "India surpassed China as the most populous nation in 2023.", "General Knowledge"},
            {"What is the currency of Japan?", "Won", "Yuan", "Yen", "Ringgit", "C", "The Japanese Yen (¥) is the official currency.", "General Knowledge"},
            {"Mount Everest is located in which mountain range?", "Alps", "Andes", "Himalayas", "Rockies", "C", "Everest is in the Himalayas on the Nepal-Tibet border.", "General Knowledge"},
            {"Which organ pumps blood in the human body?", "Liver", "Lungs", "Brain", "Heart", "D", "The heart pumps about 5 liters of blood per minute.", "General Knowledge"},
            {"Who painted the Mona Lisa?", "Michelangelo", "Leonardo da Vinci", "Raphael", "Rembrandt", "B", "Da Vinci painted it between 1503 and 1519.", "General Knowledge"},
            {"Which planet is closest to the Sun?", "Venus", "Earth", "Mercury", "Mars", "C", "Mercury orbits at about 58 million km from the Sun.", "General Knowledge"},
            {"The Olympic Games are held every how many years?", "2", "3", "4", "5", "C", "The modern Olympics occur every 4 years.", "General Knowledge"},
            {"What is H2O commonly known as?", "Hydrogen peroxide", "Water", "Heavy water", "Salt water", "B", "H2O is the chemical formula for water.", "General Knowledge"},
            {"Which Indian city is called the 'Silicon Valley of India'?", "Mumbai", "Hyderabad", "Bengaluru", "Pune", "C", "Bengaluru is India's IT hub.", "General Knowledge"},
            {"Who was the first person to walk on the Moon?", "Buzz Aldrin", "Neil Armstrong", "Yuri Gagarin", "John Glenn", "B", "Armstrong walked on the Moon on July 20, 1969.", "General Knowledge"},
            {"Which is the smallest country in the world?", "Monaco", "Vatican City", "San Marino", "Liechtenstein", "B", "Vatican City covers only 0.44 km².", "General Knowledge"},
            {"What does DNA stand for?", "Deoxyribonucleic Acid", "Dinitrogen Acid", "Dynamic Nuclear Acid", "Dual Nucleotide Array", "A", "DNA carries genetic instructions for all living organisms.", "General Knowledge"},
            {"Which festival is known as the 'Festival of Lights'?", "Holi", "Diwali", "Eid", "Christmas", "B", "Diwali celebrates the victory of light over darkness.", "General Knowledge"},
        };
        addQuestionBatch(ts, qs);
    }

    private void addQuestionBatch(TestSeries ts, String[][] qs) {
        int order = ts.getQuestions().size() + 1;
        for (String[] q : qs) {
            Question question = questionRepository.save(Question.builder()
                    .questionTextEn(q[0]).optionAEn(q[1]).optionBEn(q[2]).optionCEn(q[3]).optionDEn(q[4])
                    .correctOption(Question.CorrectOption.valueOf(q[5]))
                    .explanationEn(q[6]).subject(q[7]).marks(ts.getTotalMarks() / ts.getTotalQuestions())
                    .negativeMarks(ts.getNegativeMarking()).difficulty(Question.Difficulty.MEDIUM).build());
            ts.getQuestions().add(TestSeriesQuestion.builder()
                    .testSeries(ts).question(question).section(q[7])
                    .marks(ts.getTotalMarks() / ts.getTotalQuestions()).questionOrder(order++).build());
        }
        ts.setTotalQuestions(ts.getQuestions().size());
        testSeriesRepository.save(ts);
    }

    private void seedCurrentAffairs() {
        currentAffairsRepository.save(CurrentAffairs.builder().title("India Successfully Launches Chandrayaan-4 Mission")
                .content("ISRO successfully launched Chandrayaan-4 from Sriharikota. The mission aims to bring lunar soil samples back to Earth, making India the fourth country to achieve this feat.")
                .summary("ISRO's Chandrayaan-4 launched for lunar sample return mission").category(CurrentAffairs.Category.SCIENCE)
                .publishedDate(LocalDate.now()).viewCount(15000L).build());
        currentAffairsRepository.save(CurrentAffairs.builder().title("Union Budget 2026-27: Key Highlights for Government Job Aspirants")
                .content("Finance Minister presented the Union Budget with increased allocation for education, defence, and infrastructure. New tax slabs announced with higher exemption limits.")
                .summary("Budget 2026-27 highlights including education and defence spending").category(CurrentAffairs.Category.ECONOMY)
                .publishedDate(LocalDate.now().minusDays(1)).viewCount(25000L).build());
        currentAffairsRepository.save(CurrentAffairs.builder().title("India Wins Thomas Cup for the Second Time")
                .content("Indian badminton team defeated China to win the Thomas Cup for the second time in history, cementing India's position in world badminton.")
                .summary("Indian badminton team clinches Thomas Cup title").category(CurrentAffairs.Category.SPORTS)
                .publishedDate(LocalDate.now().minusDays(2)).viewCount(12000L).build());
        currentAffairsRepository.save(CurrentAffairs.builder().title("PM Modi Visits France for Strategic Partnership Talks")
                .content("Prime Minister Narendra Modi visited France to strengthen bilateral ties in defence, technology, and climate change cooperation.")
                .summary("India-France strategic partnership strengthened during PM's visit").category(CurrentAffairs.Category.INTERNATIONAL)
                .publishedDate(LocalDate.now().minusDays(3)).viewCount(9000L).build());
        currentAffairsRepository.save(CurrentAffairs.builder().title("New National Education Policy Implementation Update")
                .content("The Ministry of Education released updated guidelines for NEP 2020 implementation, including new curriculum framework and semester system for schools.")
                .summary("NEP 2020 implementation guidelines updated by Education Ministry").category(CurrentAffairs.Category.NATIONAL)
                .publishedDate(LocalDate.now().minusDays(4)).viewCount(18000L).build());
    }

    private void seedNews() {
        newsArticleRepository.save(NewsArticle.builder().title("SSC CGL 2026 Notification Released - Apply Before July 31")
                .content("SSC has released the official notification for CGL 2026. Approximately 8,000 vacancies across Group B and C posts. Online application starts June 1.")
                .summary("SSC CGL 2026 notification out with 8000+ vacancies").category(NewsArticle.Category.NOTIFICATION)
                .publishedDate(LocalDate.now()).build());
        newsArticleRepository.save(NewsArticle.builder().title("IBPS PO 2025 Final Result Declared")
                .content("IBPS has declared the final results for PO 2025 recruitment. Candidates can check their results on the official website.")
                .summary("IBPS PO 2025 final results available on official website").category(NewsArticle.Category.RESULT)
                .publishedDate(LocalDate.now().minusDays(1)).build());
        newsArticleRepository.save(NewsArticle.builder().title("RRB NTPC Admit Card 2026 Released for Phase 1")
                .content("Railway Recruitment Board has released admit cards for NTPC Phase 1 exam. Download from the regional RRB website using your registration number.")
                .summary("Download RRB NTPC Phase 1 admit cards from regional websites").category(NewsArticle.Category.ADMIT_CARD)
                .publishedDate(LocalDate.now().minusDays(2)).build());
        newsArticleRepository.save(NewsArticle.builder().title("SBI PO 2026 - 2000 Vacancies Announced")
                .content("State Bank of India has announced 2000 vacancies for Probationary Officer posts. Online registration begins next month.")
                .summary("SBI announces 2000 PO vacancies for 2026 recruitment").category(NewsArticle.Category.VACANCY)
                .publishedDate(LocalDate.now().minusDays(3)).build());
        newsArticleRepository.save(NewsArticle.builder().title("UPSC CSE 2025 Answer Key Released")
                .content("UPSC has released the official answer key for Civil Services Preliminary Examination 2025. Candidates can raise objections within 7 days.")
                .summary("Official UPSC CSE Prelims 2025 answer key available").category(NewsArticle.Category.ANSWER_KEY)
                .publishedDate(LocalDate.now().minusDays(5)).build());
    }

    private void seedUpscMockTest() {
        Exam upscExam = examRepository.findBySlug("upsc-cse").orElseGet(() -> {
            ExamCategory upscCat = categoryRepository.findBySlug("upsc").orElseGet(() -> 
                categoryRepository.save(ExamCategory.builder()
                    .name("UPSC").slug("upsc").description("Union Public Service Commission exams").sortOrder(3).build())
            );
            return examRepository.save(Exam.builder()
                .name("UPSC CSE").slug("upsc-cse").fullName("Civil Services Examination")
                .description("India's premier civil services examination for IAS, IPS, IFS")
                .conductingBody("UPSC").category(upscCat)
                .isFeatured(true).examDate(LocalDate.of(2026, 6, 1)).vacancyCount(1000).build());
        });

        TestSeries upscMock = TestSeries.builder()
                .title("UPSC CSE Prelims Mock Test 1").slug("upsc-cse-prelims-mock-1")
                .description("Sample UPSC mock test covering history, polity, geography, economy, and environment.")
                .type(TestSeries.TestType.FULL_MOCK).totalQuestions(5).totalMarks(10.0)
                .durationMinutes(20).negativeMarking(0.33)
                .exam(upscExam).accessType(TestSeries.AccessType.PREMIUM)
                .questions(new java.util.ArrayList<>())
                .build();

        upscMock = testSeriesRepository.save(upscMock);

        String[][] qs = {
            {
                "Which of the following is correct regarding the Joint Session of Parliament?",
                "President presides over it",
                "Speaker of Lok Sabha presides over it",
                "Vice-President presides over it",
                "Prime Minister presides over it",
                "B",
                "Speaker of Lok Sabha presides over the joint sitting of Parliament under Article 108.",
                "Indian Polity",
                "संसद के संयुक्त सत्र के संबंध में निम्नलिखित में से कौन सा कथन सही है?",
                "राष्ट्रपति इसकी अध्यक्षता करते हैं।",
                "लोकसभा अध्यक्ष इसकी अध्यक्षता करते हैं।",
                "उपराष्ट्रपति इसकी अध्यक्षता करते हैं।",
                "प्रधान मंत्री इसकी अध्यक्षता करते हैं।",
                "अनुच्छेद 108 के तहत संसद की संयुक्त बैठक की अध्यक्षता लोकसभा अध्यक्ष करते हैं।"
            },
            {
                "Which of the following lines passes through India?",
                "Equator",
                "Tropic of Capricorn",
                "Tropic of Cancer",
                "Arctic Circle",
                "C",
                "The Tropic of Cancer passes through 8 states in India: Gujarat, Rajasthan, Madhya Pradesh, Chhattisgarh, Jharkhand, West Bengal, Tripura, and Mizoram.",
                "Geography",
                "निम्नलिखित में से कौन सी रेखा भारत से होकर गुजरती है?",
                "भूमध्य रेखा",
                "मकर रेखा",
                "कर्क रेखा",
                "आर्कटिक वृत्त",
                "कर्क रेखा भारत के 8 राज्यों (गुजरात, राजस्थान, मध्य प्रदेश, छत्तीसगढ़, झारखंड, पश्चिम बंगाल, त्रिपुरा और मिजोरम) से होकर गुजरती है।"
            },
            {
                "Who was the founder of the Maurya Empire?",
                "Chandragupta Maurya",
                "Ashoka",
                "Bindusara",
                "Chandragupta II",
                "A",
                "Chandragupta Maurya founded the Maurya Empire in 322 BCE after defeating Dhanananda.",
                "History",
                "मौर्य साम्राज्य के संस्थापक कौन थे?",
                "चन्द्रगुप्त मौर्य",
                "अशोक",
                "बिन्दुसार",
                "चन्द्रगुप्त द्वितीय",
                "चन्द्रगुप्त मौर्य ने धनानंद को हराकर 322 ईसा पूर्व में मौर्य साम्राज्य की स्थापना की थी।"
            },
            {
                "Kyoto Protocol is associated with which of the following?",
                "Ozone depletion",
                "Greenhouse gases emission reduction",
                "Wetland conservation",
                "Biodiversity conservation",
                "B",
                "Kyoto Protocol is an international treaty aimed at reducing greenhouse gas emissions to combat global warming.",
                "Environment & Ecology",
                "क्योटो प्रोटोकॉल निम्नलिखित में से किससे संबंधित है?",
                "ओजोन परत का क्षरण",
                "ग्रीनहाउस गैसों के उत्सर्जन में कमी",
                "आर्द्रभूमि संरक्षण",
                "जैव विविधता संरक्षण",
                "क्योटो प्रोटोकॉल एक अंतर्राष्ट्रीय संधि है जिसका उद्देश्य ग्लोबल वार्मिंग से निपटने के लिए ग्रीनहाउस गैस उत्सर्जन को कम करना है।"
            },
            {
                "What is the primary objective of monetary policy in India?",
                "To reduce unemployment",
                "To maintain price stability while keeping growth in mind",
                "To regulate foreign trade",
                "To design tax structures",
                "B",
                "The primary objective of the Monetary Policy Committee (MPC) is to maintain price stability while keeping in mind the objective of growth.",
                "Indian Economy",
                "भारत में मौद्रिक नीति का प्राथमिक उद्देश्य क्या है?",
                "बेरोजगारी कम करना",
                "विकास को ध्यान में रखते हुए मूल्य स्थिरता बनाए रखना",
                "विदेशी व्यापार को विनियमित करना",
                "कर संरचना तैयार करना",
                "मौद्रिक नीति समिति (MPC) का प्राथमिक उद्देश्य विकास को ध्यान में रखते हुए मूल्य स्थिरता बनाए रखना है।"
            }
        };

        int order = 1;
        for (String[] q : qs) {
            Question question = questionRepository.save(Question.builder()
                    .questionTextEn(q[0]).optionAEn(q[1]).optionBEn(q[2]).optionCEn(q[3]).optionDEn(q[4])
                    .correctOption(Question.CorrectOption.valueOf(q[5]))
                    .explanationEn(q[6]).subject(q[7]).marks(2.0).negativeMarks(0.66)
                    .questionTextHi(q[8]).optionAHi(q[9]).optionBHi(q[10]).optionCHi(q[11]).optionDHi(q[12])
                    .explanationHi(q[13])
                    .difficulty(Question.Difficulty.MEDIUM).build());
            
            upscMock.getQuestions().add(TestSeriesQuestion.builder()
                    .testSeries(upscMock).question(question).section(q[7])
                    .marks(2.0).questionOrder(order++).build());
        }

        testSeriesRepository.save(upscMock);
        log.info("UPSC mock test seeded successfully with 5 questions!");
    }
}
