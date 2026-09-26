package com.example.data.ai

import com.example.model.Difficulty
import com.example.model.GameHistoryItem

class AdaptiveDifficultyEngine {

    companion object {
        const val HIGH_ACCURACY_THRESHOLD = 80
        const val LOW_ACCURACY_THRESHOLD = 50
        const val FAST_RESPONSE_TIME_SECONDS = 8.0f
        const val MINIMUM_GAMES_FOR_ADAPTATION = 2
    }

    /**
     * Evaluates recent games to determine the player's current adaptive difficulty.
     * Guaranteed to change gradually (one level at a time) and gracefully.
     */
    fun calculateAdaptiveDifficulty(
        recentGames: List<GameHistoryItem>,
        currentDifficulty: Difficulty = Difficulty.MEDIUM
    ): Pair<Difficulty, String> {
        if (recentGames.size < MINIMUM_GAMES_FOR_ADAPTATION) {
            return Pair(currentDifficulty, "Collecting initial gameplay data (Need at least $MINIMUM_GAMES_FOR_ADAPTATION games)")
        }

        // Evaluate up to the 5 most recent games
        val evaluationWindow = recentGames.take(5)
        val totalCorrect = evaluationWindow.sumOf { it.correctAnswers }
        val totalIncorrect = evaluationWindow.sumOf { it.incorrectAnswers }
        val totalQuestions = totalCorrect + totalIncorrect

        val rollingAccuracy = if (totalQuestions > 0) {
            (totalCorrect * 100) / totalQuestions
        } else {
            evaluationWindow.map { it.accuracy }.average().toInt()
        }

        val avgDurationSeconds = if (totalQuestions > 0) {
            evaluationWindow.sumOf { it.durationSeconds }.toFloat() / totalQuestions
        } else {
            10.0f
        }

        return when {
            rollingAccuracy >= HIGH_ACCURACY_THRESHOLD && avgDurationSeconds <= FAST_RESPONSE_TIME_SECONDS -> {
                val nextDifficulty = when (currentDifficulty) {
                    Difficulty.EASY -> Difficulty.MEDIUM
                    Difficulty.MEDIUM -> Difficulty.HARD
                    Difficulty.HARD -> Difficulty.EXTREME
                    Difficulty.EXTREME -> Difficulty.EXTREME
                }
                val reason = if (nextDifficulty != currentDifficulty) {
                    "Promoted to ${nextDifficulty.title} due to excellent ${rollingAccuracy}% accuracy and rapid answers (${String.format("%.1f", avgDurationSeconds)}s/Q)."
                } else {
                    "Maintained at Peak ${Difficulty.EXTREME.title} level ($rollingAccuracy% accuracy)."
                }
                Pair(nextDifficulty, reason)
            }

            rollingAccuracy <= LOW_ACCURACY_THRESHOLD -> {
                val nextDifficulty = when (currentDifficulty) {
                    Difficulty.EXTREME -> Difficulty.HARD
                    Difficulty.HARD -> Difficulty.MEDIUM
                    Difficulty.MEDIUM -> Difficulty.EASY
                    Difficulty.EASY -> Difficulty.EASY
                }
                val reason = if (nextDifficulty != currentDifficulty) {
                    "Adjusted to ${nextDifficulty.title} to reinforce fundamentals after ${rollingAccuracy}% recent accuracy."
                } else {
                    "Maintained at ${Difficulty.EASY.title} for relaxed practice."
                }
                Pair(nextDifficulty, reason)
            }

            else -> {
                Pair(
                    currentDifficulty,
                    "Optimal balance maintained at ${currentDifficulty.title} (${rollingAccuracy}% accuracy over recent sessions)."
                )
            }
        }
    }
}
