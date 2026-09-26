package com.example

import com.example.data.GameScoringConfig
import com.example.data.QuestionBank
import com.example.data.QuestionSelectionService
import com.example.model.Difficulty
import com.example.model.GameCategory
import org.junit.Assert.*
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class ExampleUnitTest {

    @Test
    fun questionBank_hasAtLeast160ValidQuestions() {
        val all = QuestionBank.allQuestions
        assertTrue("QuestionBank must have at least 160 questions", all.size >= 160)

        // Verify each question has unique ID, options include correct answer, and explanation exists
        val ids = mutableSetOf<String>()
        all.forEach { q ->
            assertFalse("Question ID must be unique: ${q.id}", ids.contains(q.id))
            ids.add(q.id)

            assertTrue("Question options must contain the correct answer: '${q.questionText}'", q.answerOptions.contains(q.correctAnswer))
            assertTrue("Question must have at least 2 options: '${q.questionText}'", q.answerOptions.size >= 2)
            assertTrue("Question explanation must not be empty", q.explanation.isNotBlank())
            assertTrue("Question timeLimit must be positive", q.timeLimit > 0)
        }
    }

    @Test
    fun questionBank_eachCategoryHasAtLeast20Questions() {
        val categories = listOf(
            QuestionBank.mathQuestions,
            QuestionBank.numbersQuestions,
            QuestionBank.logicQuestions,
            QuestionBank.wordsQuestions,
            QuestionBank.knowledgeQuestions,
            QuestionBank.scienceQuestions,
            QuestionBank.patternQuestions,
            QuestionBank.speedQuestions
        )

        categories.forEach { list ->
            assertTrue("Each category must have at least 20 questions, got ${list.size}", list.size >= 20)
        }
    }

    @Test
    fun scoringConfig_neverProducesNegativePoints() {
        val score = GameScoringConfig.calculateQuestionScore(
            difficulty = Difficulty.EASY,
            timeTakenSeconds = 100f,
            timeLimitSeconds = 10,
            currentStreak = 0
        )

        assertTrue("Score must not be negative", score.totalPoints >= 0)
        assertTrue("XP must not be negative", score.xpEarned >= 0)
    }

    @Test
    fun scoringConfig_rewardsSpeedAndStreak() {
        val fastScore = GameScoringConfig.calculateQuestionScore(
            difficulty = Difficulty.HARD,
            timeTakenSeconds = 1.2f,
            timeLimitSeconds = 15,
            currentStreak = 5
        )

        val slowScore = GameScoringConfig.calculateQuestionScore(
            difficulty = Difficulty.HARD,
            timeTakenSeconds = 14.5f,
            timeLimitSeconds = 15,
            currentStreak = 0
        )

        assertTrue("Fast answer with streak must score higher than slow answer", fastScore.totalPoints > slowScore.totalPoints)
        assertTrue("Fast answer must earn speed bonus", fastScore.speedBonus > 0)
        assertTrue("Streak answer must earn streak bonus", fastScore.streakBonus > 0)
    }

    @Test
    fun scoringConfig_levelProgressionIsConsistent() {
        val level1 = GameScoringConfig.calculateLevelInfo(0)
        assertEquals(1, level1.level)
        assertEquals(0, level1.currentXpInLevel)

        val level2 = GameScoringConfig.calculateLevelInfo(150)
        assertTrue("150 XP should be at least Level 2", level2.level >= 2)
    }

    @Test
    fun questionSelection_dailyChallengeIsDeterministic() {
        val cal1 = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            set(2026, Calendar.SEPTEMBER, 20)
        }
        val cal2 = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            set(2026, Calendar.SEPTEMBER, 20)
        }

        val set1 = QuestionSelectionService.getDailyChallengeQuestions(cal1)
        val set2 = QuestionSelectionService.getDailyChallengeQuestions(cal2)

        assertEquals(10, set1.size)
        assertEquals(10, set2.size)

        // Same date seed produces identical question order
        for (i in 0 until 10) {
            assertEquals("Daily questions must match for same UTC date", set1[i].id, set2[i].id)
        }
    }

    @Test
    fun questionSelection_categoryReturnsRequestedCountWithoutCrashing() {
        val mathQuestions = QuestionSelectionService.getQuestionsForCategory(
            category = GameCategory.MATH,
            difficulty = Difficulty.MEDIUM,
            count = 10
        )
        assertEquals(10, mathQuestions.size)
    }

    @Test
    fun scoreValidation_catchesNegativeScoresAndAnomalies() {
        val validator = com.example.data.game.ScoreValidationServiceImpl()

        // Valid payload
        val validResult = validator.validateSessionLocally(
            score = 750,
            correctCount = 8,
            totalQuestions = 10,
            durationSeconds = 45,
            difficulty = Difficulty.MEDIUM
        )
        assertTrue("Normal game session should be valid", validResult.isValid)

        // Negative score
        val negResult = validator.validateSessionLocally(
            score = -100,
            correctCount = 5,
            totalQuestions = 10,
            durationSeconds = 40,
            difficulty = Difficulty.MEDIUM
        )
        assertFalse("Negative scores must fail validation", negResult.isValid)

        // Impossible correct answers
        val impossibleRatio = validator.validateSessionLocally(
            score = 500,
            correctCount = 15,
            totalQuestions = 10,
            durationSeconds = 40,
            difficulty = Difficulty.MEDIUM
        )
        assertFalse("Correct count exceeding total questions must fail", impossibleRatio.isValid)

        // Unreasonable astronomical score
        val astronomicalScore = validator.validateSessionLocally(
            score = 999999,
            correctCount = 10,
            totalQuestions = 10,
            durationSeconds = 40,
            difficulty = Difficulty.MEDIUM
        )
        assertFalse("Astronomical score above theoretical ceiling must fail", astronomicalScore.isValid)
    }

    @Test
    fun usernameFormat_validatesCorrectly() {
        val regex = Regex("^[a-zA-Z0-9_]{3,20}$")
        assertTrue("Kingzo_99 should be valid", regex.matches("Kingzo_99"))
        assertTrue("Dev123 should be valid", regex.matches("Dev123"))
        assertFalse("Too short should fail", regex.matches("ab"))
        assertFalse("Spaces should fail", regex.matches("hello world"))
        assertFalse("Special chars should fail", regex.matches("user@name!"))
    }

    // ==========================================
    // Phase 4: AI & Intelligence Layer Tests
    // ==========================================

    @Test
    fun phase4_questionValidation_validatesOptionsAndMath() {
        val validator = com.example.data.ai.QuestionValidationService()

        val validQ = com.example.model.Question(
            id = "test_1",
            categoryId = "math",
            difficulty = Difficulty.MEDIUM,
            questionText = "What is 15 * 4?",
            answerOptions = listOf("50", "60", "70", "80"),
            correctAnswer = "60",
            explanation = "15 multiplied by 4 equals 60."
        )
        val validResult = validator.validate(validQ)
        assertTrue("Legitimate math question must pass validation: ${validResult.failureReasons}", validResult.isValid)

        // Math mismatch check: 15 * 4 is 60, not 55
        val wrongMathQ = validQ.copy(correctAnswer = "55", answerOptions = listOf("50", "55", "70", "80"))
        val wrongMathResult = validator.validate(wrongMathQ)
        assertFalse("Deterministic math mismatch must be flagged", wrongMathResult.isValid)

        // Missing correct answer in options
        val missingOptionQ = validQ.copy(answerOptions = listOf("10", "20", "30", "40"))
        val missingOptionResult = validator.validate(missingOptionQ)
        assertFalse("Options not containing correct answer must fail", missingOptionResult.isValid)
    }

    @Test
    fun phase4_questionValidation_detectsDuplicates() {
        val validator = com.example.data.ai.QuestionValidationService()
        val pool = listOf(
            com.example.model.Question(
                id = "p_1",
                categoryId = "knowledge",
                difficulty = Difficulty.EASY,
                questionText = "What is the capital city of France?",
                answerOptions = listOf("Paris", "Lyon", "Marseille", "Nice"),
                correctAnswer = "Paris",
                explanation = "Paris is the capital of France."
            )
        )

        val duplicateQ = com.example.model.Question(
            id = "new_q",
            categoryId = "knowledge",
            difficulty = Difficulty.EASY,
            questionText = "What is the capital city of France?",
            answerOptions = listOf("Paris", "Bordeaux", "Toulouse", "Nantes"),
            correctAnswer = "Paris",
            explanation = "Paris is France's capital."
        )

        val (isDup, _, _) = validator.checkDuplicate(duplicateQ, pool)
        assertTrue("Near-identical question must be flagged as duplicate", isDup)
    }

    @Test
    fun phase4_adaptiveDifficulty_adjustsBasedOnPlayerSkill() {
        val engine = com.example.data.ai.AdaptiveDifficultyEngine()

        // High accuracy player (> 80%) with fast answers on EASY difficulty should be upgraded to MEDIUM
        val highGames = listOf(
            com.example.model.GameHistoryItem(
                id = "g1", date = "Today", gameMode = "rush", category = "math", difficulty = "EASY",
                score = 500, accuracy = 90, xp = 150, durationSeconds = 12, correctAnswers = 9, incorrectAnswers = 1, bestStreak = 5
            ),
            com.example.model.GameHistoryItem(
                id = "g2", date = "Today", gameMode = "rush", category = "math", difficulty = "EASY",
                score = 520, accuracy = 100, xp = 160, durationSeconds = 10, correctAnswers = 10, incorrectAnswers = 0, bestStreak = 10
            )
        )
        val (upgraded, _) = engine.calculateAdaptiveDifficulty(highGames, Difficulty.EASY)
        assertEquals("Mastery performance on EASY should graduate player to MEDIUM", Difficulty.MEDIUM, upgraded)

        // Low accuracy player (< 50%) on HARD difficulty should step down to MEDIUM
        val strugglingGames = listOf(
            com.example.model.GameHistoryItem(
                id = "g3", date = "Today", gameMode = "rush", category = "logic", difficulty = "HARD",
                score = 100, accuracy = 30, xp = 30, durationSeconds = 30, correctAnswers = 3, incorrectAnswers = 7, bestStreak = 1
            ),
            com.example.model.GameHistoryItem(
                id = "g4", date = "Today", gameMode = "rush", category = "logic", difficulty = "HARD",
                score = 80, accuracy = 20, xp = 20, durationSeconds = 35, correctAnswers = 2, incorrectAnswers = 8, bestStreak = 1
            )
        )
        val (downgraded, _) = engine.calculateAdaptiveDifficulty(strugglingGames, Difficulty.HARD)
        assertEquals("Struggling player on HARD should be assisted by moving to MEDIUM", Difficulty.MEDIUM, downgraded)
    }

    @Test
    fun phase4_performanceAnalytics_generatesBrainReportAndRecommendation() {
        val engine = com.example.data.ai.AdaptiveDifficultyEngine()
        val analytics = com.example.data.ai.PlayerPerformanceAnalytics(engine)

        val profile = com.example.model.UserProfile(
            uid = "user_test",
            displayName = "DrMath",
            totalScore = 4500,
            gamesPlayed = 8,
            bestScore = 800,
            accuracyPercentage = 82
        )

        val history = listOf(
            com.example.model.GameHistoryItem(
                id = "h1", date = "Today", gameMode = "category", category = "math", difficulty = "HARD",
                score = 800, accuracy = 100, xp = 200, durationSeconds = 25, correctAnswers = 10, incorrectAnswers = 0, bestStreak = 10
            ),
            com.example.model.GameHistoryItem(
                id = "h2", date = "Yesterday", gameMode = "category", category = "words", difficulty = "MEDIUM",
                score = 250, accuracy = 40, xp = 60, durationSeconds = 30, correctAnswers = 4, incorrectAnswers = 6, bestStreak = 2
            )
        )

        val performance = analytics.analyzePerformance(profile, history)
        assertNotNull("Brain report must be generated for active player", performance.brainReport)
        assertTrue("Brain report title must be descriptive", performance.brainReport!!.title.isNotBlank())
        assertTrue("Strongest domain should reflect performance", performance.brainReport!!.strongestDomain.isNotBlank())

        val rec = analytics.getPersonalizedRecommendation(performance, profile.gamesPlayed)
        assertNotNull("Personalized recommendation should be computed", rec)
        assertEquals("Recommendation must offer 5 targeted questions", 5, rec!!.questionCount)
    }

    @Test
    fun phase4_questionAdminService_quarantinesFlaggedQuestions() {
        val validator = com.example.data.ai.QuestionValidationService()
        val aiService = com.example.data.ai.AiQuestionServiceImpl()
        val adminService = com.example.data.ai.QuestionAdminService(null, validator, aiService)

        val targetQ = QuestionBank.allQuestions.first()

        // Initially in approved pool
        assertTrue(adminService.getAllApprovedPool().any { it.id == targetQ.id })

        // Submit 3 reports
        adminService.reportQuestion(com.example.model.QuestionReport(questionId = targetQ.id, reason = com.example.model.ReportReason.WRONG_ANSWER))
        adminService.reportQuestion(com.example.model.QuestionReport(questionId = targetQ.id, reason = com.example.model.ReportReason.AMBIGUOUS_QUESTION))
        adminService.reportQuestion(com.example.model.QuestionReport(questionId = targetQ.id, reason = com.example.model.ReportReason.TYPO))

        // Should now be quarantined and omitted from active pool
        assertFalse("Quarantined question must be excluded from approved pool", adminService.getAllApprovedPool().any { it.id == targetQ.id })
    }
}
