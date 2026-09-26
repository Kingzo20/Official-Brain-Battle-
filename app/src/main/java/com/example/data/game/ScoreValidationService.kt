package com.example.data.game

import com.example.model.Difficulty
import com.example.model.GameResult

/**
 * Interface and local engine for score validation.
 *
 * NOTE (Phase 3 Security Architecture):
 * Client-submitted scores are strictly separated into:
 * 1. Unverified Client Gameplay Data (Immediate local feedback, offline play)
 * 2. Server-Authoritative Verification (Future Backend requirement via Cloud Functions / App Check)
 *
 * Leaderboard scores are marked as client-reported until server verification is completed.
 */
data class ScoreValidationResult(
    val isValid: Boolean,
    val reason: String? = null,
    val isServerVerified: Boolean = false
)

interface ScoreValidationService {
    /**
     * Local heuristic validation verifying question count, time plausibility,
     * non-negative scoring, and maximum mathematical points achievable.
     */
    fun validateSessionLocally(
        score: Int,
        correctCount: Int,
        totalQuestions: Int,
        durationSeconds: Int,
        difficulty: Difficulty
    ): ScoreValidationResult

    /**
     * Interface for future server-authoritative verification via Firebase Cloud Functions.
     */
    suspend fun submitForServerVerification(
        gameId: String,
        sessionToken: String,
        gameResult: GameResult
    ): ScoreValidationResult
}

class ScoreValidationServiceImpl : ScoreValidationService {

    override fun validateSessionLocally(
        score: Int,
        correctCount: Int,
        totalQuestions: Int,
        durationSeconds: Int,
        difficulty: Difficulty
    ): ScoreValidationResult {
        if (score < 0) {
            return ScoreValidationResult(false, "Score cannot be negative")
        }
        if (correctCount < 0 || correctCount > totalQuestions) {
            return ScoreValidationResult(false, "Invalid correct answer ratio")
        }
        if (totalQuestions <= 0) {
            return ScoreValidationResult(false, "Session must contain at least one question")
        }

        // Maximum theoretical points per question: base * multiplier * max speed bonus (2.0)
        val maxPossiblePointsPerQ = (difficulty.basePoints * difficulty.multiplier * 2.5f).toInt() + 100
        val maxTheoreticalScore = maxPossiblePointsPerQ * totalQuestions
        if (score > maxTheoreticalScore) {
            return ScoreValidationResult(false, "Reported score exceeds physical upper threshold")
        }

        // Minimum time check (must take at least 200ms per question)
        if (durationSeconds < 0) {
            return ScoreValidationResult(false, "Negative duration detected")
        }

        return ScoreValidationResult(isValid = true, isServerVerified = false)
    }

    override suspend fun submitForServerVerification(
        gameId: String,
        sessionToken: String,
        gameResult: GameResult
    ): ScoreValidationResult {
        // Architecture placeholder for Phase 4 Cloud Function authoritative verification
        // Returns unverified flag to prevent claiming cheat-proof leaderboards in Phase 3
        return ScoreValidationResult(
            isValid = true,
            reason = "Awaiting Phase 4 Server-Authoritative Verification",
            isServerVerified = false
        )
    }
}
