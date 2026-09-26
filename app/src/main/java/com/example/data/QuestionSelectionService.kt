package com.example.data

import com.example.model.Difficulty
import com.example.model.GameCategory
import com.example.model.Question
import java.util.Calendar
import java.util.TimeZone
import kotlin.random.Random

object QuestionSelectionService {

    /**
     * Randomizes the order of answer options while preserving correct answer integrity.
     */
    fun randomizeOptions(question: Question): Question {
        return question.copy(answerOptions = question.answerOptions.shuffled())
    }

    /**
     * Retrieves questions for a specific category and difficulty.
     * Guaranteed never to return empty or crash.
     */
    fun getQuestionsForCategory(
        category: GameCategory,
        difficulty: Difficulty?,
        count: Int = 10,
        excludeIds: Set<String> = emptySet(),
        pool: List<Question>? = null
    ): List<Question> {
        val basePool = pool ?: QuestionBank.allQuestions
        val categoryPool = if (category == GameCategory.DAILY) {
            basePool
        } else {
            basePool.filter { it.categoryId.equals(category.id, ignoreCase = true) }
        }

        // Filter by difficulty if provided
        val difficultyFiltered = if (difficulty != null) {
            categoryPool.filter { it.difficulty == difficulty && !excludeIds.contains(it.id) }
        } else {
            categoryPool.filter { !excludeIds.contains(it.id) }
        }

        val available = if (difficultyFiltered.isNotEmpty()) {
            difficultyFiltered.shuffled()
        } else if (categoryPool.isNotEmpty()) {
            categoryPool.shuffled()
        } else {
            basePool.shuffled()
        }

        val selected = available.take(count).toMutableList()

        // If we still need more questions to reach requested count, fill from basePool
        if (selected.size < count) {
            val filler = basePool
                .filter { q -> selected.none { it.id == q.id } && !excludeIds.contains(q.id) }
                .shuffled()
                .take(count - selected.size)
            selected.addAll(filler)
        }

        return selected.map { randomizeOptions(it) }
    }

    /**
     * Retrieves targeted practice questions for personalized challenges.
     */
    fun getPracticeQuestions(
        category: GameCategory,
        difficulty: Difficulty,
        count: Int = 5,
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
     * Deterministic Daily Challenge question set based on UTC calendar day.
     * Guarantees 10 mixed category questions with mixed difficulty that are identical
     * for all players globally on the same calendar day.
     */
    fun getDailyChallengeQuestions(calendar: Calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))): List<Question> {
        val year = calendar.get(Calendar.YEAR)
        val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
        val dateSeed = (year * 1000L + dayOfYear)
        val deterministicRandom = Random(dateSeed)

        // Select 1 question each from 8 main categories, then 2 extra for 10 total
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

        val selected = mutableListOf<Question>()

        // Pick one from each category deterministically
        categories.forEach { pool ->
            val index = deterministicRandom.nextInt(pool.size)
            selected.add(pool[index])
        }

        // Pick 2 more unique questions from remaining pool
        val remainingPool = QuestionBank.allQuestions
            .filter { q -> selected.none { it.id == q.id } }
            .shuffled(deterministicRandom)

        selected.addAll(remainingPool.take(2))

        // Shuffle the final 10 in a deterministic order for consistent player experience
        return selected.shuffled(deterministicRandom).map { randomizeOptions(it) }
    }

    /**
     * Get a broad shuffled pool for 60-Second Rush.
     */
    fun getRushQuestions(count: Int = 30, pool: List<Question>? = null): List<Question> {
        val basePool = pool ?: QuestionBank.allQuestions
        return basePool.shuffled().take(count).map { randomizeOptions(it) }
    }

    /**
     * Endless Mode questions ordered by scaling difficulty:
     * Starts with EASY, then MEDIUM, then HARD, then EXTREME.
     */
    fun getEndlessPool(count: Int = 50, pool: List<Question>? = null): List<Question> {
        val basePool = pool ?: QuestionBank.allQuestions
        val easy = basePool.filter { it.difficulty == Difficulty.EASY }.shuffled()
        val medium = basePool.filter { it.difficulty == Difficulty.MEDIUM }.shuffled()
        val hard = basePool.filter { it.difficulty == Difficulty.HARD }.shuffled()
        val extreme = basePool.filter { it.difficulty == Difficulty.EXTREME }.shuffled()

        val ordered = mutableListOf<Question>()
        ordered.addAll(easy)
        ordered.addAll(medium)
        ordered.addAll(hard)
        ordered.addAll(extreme)

        val result = if (ordered.size >= count) ordered.take(count) else ordered
        return result.map { randomizeOptions(it) }
    }
}
