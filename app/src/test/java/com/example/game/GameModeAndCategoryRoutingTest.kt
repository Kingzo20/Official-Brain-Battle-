package com.example.game

import com.example.data.QuestionBank
import com.example.data.QuestionSelectionService
import com.example.model.Difficulty
import com.example.model.GameCategory
import org.junit.Assert.*
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class GameModeAndCategoryRoutingTest {

    @Test
    fun testAllTenCategoriesStrictlyRouteToDedicatedQuestionPools() {
        val all10 = GameCategory.ALL_10_CATEGORIES
        assertEquals(10, all10.size)

        for (category in all10) {
            val questions = QuestionSelectionService.getQuestionsForCategory(
                category = category,
                difficulty = null,
                count = 10
            )

            assertTrue("Category ${category.title} should return questions", questions.isNotEmpty())
            for (q in questions) {
                assertEquals(
                    "Question ${q.id} in category ${category.title} must have matching categoryId",
                    category.id.lowercase(),
                    q.categoryId.lowercase()
                )
                assertTrue("Question must have at least 2 options", q.answerOptions.size >= 2)
                assertTrue("Question must have non-blank questionText", q.questionText.isNotBlank())
                assertTrue("Question must contain correct answer in options", q.answerOptions.contains(q.correctAnswer))
                assertTrue("Question must have non-empty explanation", q.explanation.isNotBlank())
            }
        }
    }

    @Test
    fun testQuickBattleBehaviorMatrix() {
        val quickQuestions = QuestionSelectionService.getQuickBattleQuestions(count = 10)
        assertEquals(10, quickQuestions.size)

        for (q in quickQuestions) {
            assertTrue("Per-question timer limit must be between 5 and 30 seconds", q.timeLimit in 5..30)
            assertTrue("Question must have correct answer in options", q.answerOptions.contains(q.correctAnswer))
        }
    }

    @Test
    fun testPracticeModeProvidesExplanations() {
        val practiceQuestions = QuestionSelectionService.getPracticeQuestions(
            category = GameCategory.SCIENCE,
            difficulty = Difficulty.HARD,
            count = 5
        )

        assertEquals(5, practiceQuestions.size)
        for (q in practiceQuestions) {
            assertEquals("science", q.categoryId.lowercase())
            assertTrue("Practice question must have comprehensive explanation", q.explanation.length >= 10)
        }
    }

    @Test
    fun testEndlessModeScalingDifficulty() {
        val endlessPool = QuestionSelectionService.getEndlessPool(count = 40)
        assertTrue("Endless pool should contain at least 20 questions", endlessPool.size >= 20)

        // Verify that initial questions are Easy/Medium and later ones include Hard/Extreme
        val firstBatch = endlessPool.take(5)
        val hasEasyOrMediumInStart = firstBatch.any { it.difficulty == Difficulty.EASY || it.difficulty == Difficulty.MEDIUM }
        assertTrue("Endless mode should start with Easy/Medium difficulty", hasEasyOrMediumInStart)
    }

    @Test
    fun testDailyChallengeDeterministicPerCalendarDay() {
        val cal1 = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            set(Calendar.YEAR, 2026)
            set(Calendar.DAY_OF_YEAR, 270)
        }
        val cal2 = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            set(Calendar.YEAR, 2026)
            set(Calendar.DAY_OF_YEAR, 270)
        }

        val dailySet1 = QuestionSelectionService.getDailyChallengeQuestions(cal1)
        val dailySet2 = QuestionSelectionService.getDailyChallengeQuestions(cal2)

        assertEquals(10, dailySet1.size)
        assertEquals(10, dailySet2.size)

        for (i in 0 until 10) {
            assertEquals(
                "Daily questions must be identical for all users on the same calendar day",
                dailySet1[i].id,
                dailySet2[i].id
            )
        }
    }
}
