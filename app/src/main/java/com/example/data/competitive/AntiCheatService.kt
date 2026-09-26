package com.example.data.competitive

import com.example.model.Difficulty
import java.util.concurrent.ConcurrentHashMap

data class AntiCheatValidationResult(
    val status: ScoreValidationStatus,
    val isEligibleForLeaderboard: Boolean,
    val flagReasons: List<String> = emptyList()
)

class AntiCheatService {

    // In-memory set of processed game IDs for duplicate protection
    private val processedGameIds = ConcurrentHashMap.newKeySet<String>()

    /**
     * Checks if this gameId has already been recorded / submitted.
     * Returns true if newly added, false if already processed (duplicate).
     */
    fun markGameProcessed(gameId: String): Boolean {
        if (gameId.isBlank()) return false
        return processedGameIds.add(gameId)
    }

    fun isGameProcessed(gameId: String): Boolean {
        return processedGameIds.contains(gameId)
    }

    /**
     * Evaluates gameplay session plausibility without accusing the user or displaying internal flags.
     */
    fun validateSession(
        gameId: String,
        score: Int,
        correctCount: Int,
        totalQuestions: Int,
        durationSeconds: Int,
        difficulty: Difficulty
    ): AntiCheatValidationResult {
        val flags = mutableListOf<String>()

        // 1. Basic sanity
        if (score < 0) {
            return AntiCheatValidationResult(
                status = ScoreValidationStatus.REJECTED,
                isEligibleForLeaderboard = false,
                flagReasons = listOf("Negative score submitted")
            )
        }

        if (totalQuestions <= 0 || correctCount < 0 || correctCount > totalQuestions) {
            return AntiCheatValidationResult(
                status = ScoreValidationStatus.REJECTED,
                isEligibleForLeaderboard = false,
                flagReasons = listOf("Impossible question/answer ratio")
            )
        }

        // 2. Physical Timing Checks
        // Minimum plausible response time: at least 0.25s per question
        val minPlausibleDurationSeconds = (totalQuestions * 0.25f).toInt()
        if (durationSeconds < minPlausibleDurationSeconds && totalQuestions >= 5) {
            flags.add("Unusually fast completion time: ${durationSeconds}s for $totalQuestions questions")
        }

        // 3. Mathematical Upper Bound Check
        val maxPointsPerQ = (difficulty.basePoints * difficulty.multiplier * 2.5f).toInt() + 150
        val maxTheoreticalScore = maxPointsPerQ * totalQuestions
        if (score > maxTheoreticalScore) {
            flags.add("Score ($score) exceeds upper theoretical maximum ($maxTheoreticalScore)")
        }

        // 4. Duplicate Check
        if (processedGameIds.contains(gameId)) {
            flags.add("Duplicate game session detected ($gameId)")
        }

        return if (flags.isNotEmpty()) {
            AntiCheatValidationResult(
                status = ScoreValidationStatus.FLAGGED,
                isEligibleForLeaderboard = false,
                flagReasons = flags
            )
        } else {
            AntiCheatValidationResult(
                status = ScoreValidationStatus.VALIDATED,
                isEligibleForLeaderboard = true
            )
        }
    }
}
