package com.example.data

import com.example.model.Difficulty
import com.example.model.GameCategory
import com.example.model.Question
import java.util.Calendar
import java.util.TimeZone
import kotlin.random.Random

data class QuestionSelectionResult(
    val questions: List<Question>,
    val wasExhaustedAndRecycled: Boolean = false
)

object QuestionSelectionService {

    /**
     * Maps the active player level to progressive difficulty tiers:
     * - Level 1–5: EASY
     * - Level 6–10: MEDIUM
     * - Level 11+: HARD (with EXTREME)
     */
    fun getDifficultyTierForLevel(level: Int): Difficulty {
        return when {
            level <= 5 -> Difficulty.EASY
            level <= 10 -> Difficulty.MEDIUM
            else -> Difficulty.HARD
        }
    }

    /**
     * Randomizes the order of answer options while preserving correct answer integrity.
     */
    fun randomizeOptions(question: Question): Question {
        return question.copy(answerOptions = question.answerOptions.shuffled())
    }

    /**
     * Retrieves questions for a specific category with strict history tracking and level-based progression.
     * - Strictly excludes all previously seen question IDs (filterNot { it.id in excludeIds }).
     * - Serves completely unseen questions matching the player's level tier.
     * - Only resets or recycles questions after the player has completely exhausted the category pool.
     */
    fun getQuestionsForCategoryWithHistory(
        category: GameCategory,
        difficulty: Difficulty? = null,
        playerLevel: Int = 1,
        count: Int = 10,
        excludeIds: Set<String> = emptySet(),
        pool: List<Question>? = null
    ): QuestionSelectionResult {
        val dedicatedPool = when (category) {
            GameCategory.MATH -> QuestionBank.mathQuestions
            GameCategory.LOGIC -> QuestionBank.logicQuestions
            GameCategory.SCIENCE -> QuestionBank.scienceQuestions
            GameCategory.GEOGRAPHY -> QuestionBank.geographyQuestions
            GameCategory.KNOWLEDGE -> QuestionBank.knowledgeQuestions
            GameCategory.WORDS -> QuestionBank.wordsQuestions
            GameCategory.MEMORY -> QuestionBank.memoryQuestions
            GameCategory.PATTERNS -> QuestionBank.patternQuestions
            GameCategory.RIDDLES -> QuestionBank.riddleQuestions
            GameCategory.TECHNOLOGY -> QuestionBank.technologyQuestions
            GameCategory.NUMBERS -> QuestionBank.numbersQuestions
            GameCategory.SPEED -> QuestionBank.speedQuestions
            GameCategory.DAILY -> pool ?: QuestionBank.allQuestions
        }

        // Strict category isolation: questions must strictly match this category ID
        val categoryPool = if (category == GameCategory.DAILY) {
            dedicatedPool
        } else {
            val approvedFromPool = pool?.filter { it.categoryId.equals(category.id, ignoreCase = true) } ?: emptyList()
            (dedicatedPool + approvedFromPool).distinctBy { it.id }.filter { it.categoryId.equals(category.id, ignoreCase = true) }
        }

        // Progressive difficulty tier matching the active player level
        val targetDifficulty = difficulty ?: getDifficultyTierForLevel(playerLevel)

        // 1. Strictly exclude all previously seen question IDs
        var unseenPool = categoryPool.filterNot { it.id in excludeIds }
        var wasExhausted = false

        // 2. Only reset or recycle questions after the player has completely exhausted the category pool
        if (unseenPool.size < count) {
            wasExhausted = true
            unseenPool = categoryPool
        }

        // 3. Level-based question progression: prioritize questions matching active difficulty tier
        val tierQuestions = unseenPool.filter {
            if (targetDifficulty == Difficulty.HARD) {
                it.difficulty == Difficulty.HARD || it.difficulty == Difficulty.EXTREME
            } else {
                it.difficulty == targetDifficulty
            }
        }.shuffled()

        val selected = tierQuestions.take(count).toMutableList()

        // If tier questions are fewer than count, draw remaining unseen questions from the same category
        if (selected.size < count) {
            val adjacentUnseen = unseenPool
                .filterNot { q -> selected.any { it.id == q.id } }
                .shuffled()
            selected.addAll(adjacentUnseen.take(count - selected.size))
        }

        // If still fewer than count (e.g. initial count requested > unseen), fill from recycled category pool
        if (selected.size < count) {
            val filler = categoryPool
                .filterNot { q -> selected.any { it.id == q.id } }
                .shuffled()
            selected.addAll(filler.take(count - selected.size))
        }

        // Shuffle questions and randomize answer option order
        val finalQuestions = selected.shuffled().map { randomizeOptions(it) }
        return QuestionSelectionResult(
            questions = finalQuestions,
            wasExhaustedAndRecycled = wasExhausted
        )
    }

    /**
     * Retrieves questions for a specific category and difficulty.
     * Strictly enforces category routing: questions will ONLY be chosen from the requested topic.
     * Randomizes order upon session start.
     */
    fun getQuestionsForCategory(
        category: GameCategory,
        difficulty: Difficulty?,
        count: Int = 10,
        excludeIds: Set<String> = emptySet(),
        pool: List<Question>? = null
    ): List<Question> {
        return getQuestionsForCategoryWithHistory(
            category = category,
            difficulty = difficulty,
            playerLevel = 1,
            count = count,
            excludeIds = excludeIds,
            pool = pool
        ).questions
    }

    /**
     * Retrieves targeted practice questions for personalized challenges.
     */
    fun getPracticeQuestions(
        category: GameCategory,
        difficulty: Difficulty,
        count: Int = 10,
        pool: List<Question>? = null
    ): List<Question> {
        return getQuestionsForCategory(
            category = category,
            difficulty = difficulty,
            count = count,
            pool = pool
        )
    }

    /**
     * Quick Battle: exactly 10 questions with per-question timer pressure and competitive scoring.
     */
    fun getQuickBattleQuestions(
        category: GameCategory? = null,
        difficulty: Difficulty? = null,
        count: Int = 10,
        pool: List<Question>? = null
    ): List<Question> {
        return if (category != null && category != GameCategory.DAILY) {
            getQuestionsForCategory(category = category, difficulty = difficulty, count = count, pool = pool)
        } else {
            val basePool = pool ?: QuestionBank.allQuestions
            basePool.shuffled().take(count).map { randomizeOptions(it) }
        }
    }

    /**
     * Deterministic Daily Challenge question set based on UTC calendar day.
     * Guarantees 10 mixed category questions with mixed difficulty that are identical
     * for all players globally on the same calendar day.
     */
    fun getDailyChallengeQuestions(calendar: Calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))): List<Question> {
        val year = calendar.get(Calendar.YEAR)
        val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
        val dateSeed = (year * 1000L + dayOfYear)
        val deterministicRandom = Random(dateSeed)

        // Select 1 question each from all 10 main categories
        val categories = listOf(
            QuestionBank.mathQuestions,
            QuestionBank.logicQuestions,
            QuestionBank.scienceQuestions,
            QuestionBank.geographyQuestions,
            QuestionBank.knowledgeQuestions,
            QuestionBank.wordsQuestions,
            QuestionBank.memoryQuestions,
            QuestionBank.patternQuestions,
            QuestionBank.riddleQuestions,
            QuestionBank.technologyQuestions
        )

        val selected = mutableListOf<Question>()

        // Pick one from each of the 10 categories deterministically
        categories.forEach { pool ->
            if (pool.isNotEmpty()) {
                val index = deterministicRandom.nextInt(pool.size)
                selected.add(pool[index])
            }
        }

        // If fewer than 10, fill deterministically from allQuestions
        if (selected.size < 10) {
            val remainingPool = QuestionBank.allQuestions
                .filter { q -> selected.none { it.id == q.id } }
                .shuffled(deterministicRandom)
            selected.addAll(remainingPool.take(10 - selected.size))
        }

        // Shuffle the final 10 in a deterministic order for consistent player experience
        return selected.shuffled(deterministicRandom).map { randomizeOptions(it) }
    }

    /**
     * Get a broad shuffled pool for Rush / Quick mode.
     */
    fun getRushQuestions(count: Int = 30, pool: List<Question>? = null): List<Question> {
        val basePool = pool ?: QuestionBank.allQuestions
        return basePool.shuffled().take(count).map { randomizeOptions(it) }
    }

    /**
     * Endless Mode questions ordered by scaling difficulty:
     * Starts with EASY, then scales to MEDIUM, then HARD, then EXTREME.
     */
    fun getEndlessPool(count: Int = 100, pool: List<Question>? = null): List<Question> {
        val basePool = pool ?: QuestionBank.allQuestions
        val easy = basePool.filter { it.difficulty == Difficulty.EASY }.shuffled()
        val medium = basePool.filter { it.difficulty == Difficulty.MEDIUM }.shuffled()
        val hard = basePool.filter { it.difficulty == Difficulty.HARD }.shuffled()
        val extreme = basePool.filter { it.difficulty == Difficulty.EXTREME }.shuffled()

        // Progressive sequence: Easy -> Medium -> Hard -> Extreme
        val progressive = (easy + medium + hard + extreme).take(count)
        return progressive.map { randomizeOptions(it) }
    }
}
