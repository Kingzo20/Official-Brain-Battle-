package com.example.data

import com.example.model.Difficulty

object GameScoringConfig {

    fun getBasePoints(difficulty: Difficulty): Int = when (difficulty) {
        Difficulty.EASY -> 100
        Difficulty.MEDIUM -> 150
        Difficulty.HARD -> 200
        Difficulty.EXTREME -> 300
    }

    fun getBaseXP(difficulty: Difficulty): Int = when (difficulty) {
        Difficulty.EASY -> 10
        Difficulty.MEDIUM -> 15
        Difficulty.HARD -> 20
        Difficulty.EXTREME -> 30
    }

    /**
     * Speed bonus based on seconds taken vs question time limit.
     */
    fun calculateSpeedBonus(timeTakenSeconds: Float, timeLimitSeconds: Int): Int {
        if (timeLimitSeconds <= 0) return 0
        val ratio = (timeTakenSeconds / timeLimitSeconds.toFloat()).coerceIn(0f, 1f)
        return when {
            ratio <= 0.25f -> 50 // ultra fast answer (<25% time)
            ratio <= 0.50f -> 30 // fast answer (<50% time)
            ratio <= 0.75f -> 15 // moderately fast (<75% time)
            else -> 0
        }
    }

    /**
     * Streak bonus: increases with consecutive correct answers up to a cap.
     */
    fun calculateStreakBonus(currentStreak: Int): Int {
        if (currentStreak <= 1) return 0
        return ((currentStreak - 1) * 10).coerceAtMost(100)
    }

    /**
     * Total score calculation for a single correct question
     */
    fun calculateQuestionScore(
        difficulty: Difficulty,
        timeTakenSeconds: Float,
        timeLimitSeconds: Int,
        currentStreak: Int
    ): ScoreBreakdown {
        val basePoints = getBasePoints(difficulty)
        val speedBonus = calculateSpeedBonus(timeTakenSeconds, timeLimitSeconds)
        val streakBonus = calculateStreakBonus(currentStreak)
        val totalPoints = (basePoints + speedBonus + streakBonus).coerceAtLeast(0)
        val baseXP = getBaseXP(difficulty)

        return ScoreBreakdown(
            basePoints = basePoints,
            speedBonus = speedBonus,
            streakBonus = streakBonus,
            totalPoints = totalPoints,
            xpEarned = baseXP
        )
    }

    /**
     * Level thresholds based on quadratic progression:
     * Level 1: 0 - 99 XP
     * Level 2: 100 - 249 XP
     * Level 3: 250 - 449 XP
     * Level 4: 450 - 699 XP
     * Level 5: 700 - 999 XP
     * Level N requires 50 * N * (N - 1) total cumulative XP.
     */
    fun calculateLevelInfo(totalXp: Int): LevelInfo {
        var level = 1
        while (getXpThresholdForLevel(level + 1) <= totalXp) {
            level++
        }
        val currentLevelFloorXp = getXpThresholdForLevel(level)
        val nextLevelTargetXp = getXpThresholdForLevel(level + 1)
        val progressInLevel = (totalXp - currentLevelFloorXp).coerceAtLeast(0)
        val xpNeededForNext = (nextLevelTargetXp - currentLevelFloorXp).coerceAtLeast(1)

        return LevelInfo(
            level = level,
            currentXpInLevel = progressInLevel,
            xpRequiredForNextLevel = xpNeededForNext,
            totalCumulativeXp = totalXp
        )
    }

    private fun getXpThresholdForLevel(level: Int): Int {
        if (level <= 1) return 0
        return 50 * level * (level - 1)
    }
}

data class ScoreBreakdown(
    val basePoints: Int,
    val speedBonus: Int,
    val streakBonus: Int,
    val totalPoints: Int,
    val xpEarned: Int
)

data class LevelInfo(
    val level: Int,
    val currentXpInLevel: Int,
    val xpRequiredForNextLevel: Int,
    val totalCumulativeXp: Int
)
