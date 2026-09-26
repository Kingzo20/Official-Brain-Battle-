package com.example.data.ai

import com.example.model.*
import java.util.Locale

class PlayerPerformanceAnalytics(
    private val adaptiveDifficultyEngine: AdaptiveDifficultyEngine = AdaptiveDifficultyEngine()
) {

    /**
     * Computes the complete player performance profile from history and profile stats.
     */
    fun analyzePerformance(
        userProfile: UserProfile,
        gameHistory: List<GameHistoryItem>
    ): PlayerPerformanceProfile {
        val totalGames = userProfile.gamesPlayed

        // Group history by category title/id
        val categoryStatsMap = mutableMapOf<String, CategoryPerformance>()

        for (category in GameCategory.values()) {
            if (category == GameCategory.DAILY) continue

            // Find games matching this category
            val catGames = gameHistory.filter {
                it.category.equals(category.title, ignoreCase = true) ||
                        it.category.equals(category.id, ignoreCase = true) ||
                        it.category.contains(category.title.take(4), ignoreCase = true)
            }

            val answered = catGames.sumOf { it.correctAnswers + it.incorrectAnswers }
            val correct = catGames.sumOf { it.correctAnswers }
            val accuracy = if (answered > 0) (correct * 100) / answered else 0
            val totalSeconds = catGames.sumOf { it.durationSeconds }
            val avgTime = if (answered > 0) totalSeconds.toFloat() / answered else 0f

            categoryStatsMap[category.id] = CategoryPerformance(
                category = category,
                questionsAnswered = answered,
                correctAnswers = correct,
                accuracyPercentage = accuracy,
                averageResponseTimeSeconds = avgTime,
                lastPlayedTimestamp = if (catGames.isNotEmpty()) System.currentTimeMillis() else 0L
            )
        }

        // Active categories that have at least one question answered
        val playedCategories = categoryStatsMap.values.filter { it.questionsAnswered > 0 }

        val strongestCategory = if (playedCategories.isNotEmpty()) {
            playedCategories.maxByOrNull { it.accuracyPercentage }?.category
        } else {
            GameCategory.MATH
        }

        val weakestCategory = if (playedCategories.isNotEmpty()) {
            playedCategories.minByOrNull { it.accuracyPercentage }?.category
        } else {
            GameCategory.LOGIC
        }

        // Adaptive difficulty computation
        val (adaptiveDiff, diffReason) = adaptiveDifficultyEngine.calculateAdaptiveDifficulty(gameHistory)

        // Difficulty stats breakdown from history
        val difficultyStats = mutableMapOf<String, Int>()
        Difficulty.values().forEach { diff ->
            val gamesWithDiff = gameHistory.filter { it.difficulty.equals(diff.title, ignoreCase = true) }
            val diffAccuracy = if (gamesWithDiff.isNotEmpty()) {
                val c = gamesWithDiff.sumOf { it.correctAnswers }
                val total = gamesWithDiff.sumOf { it.correctAnswers + it.incorrectAnswers }
                if (total > 0) (c * 100) / total else 0
            } else 0
            difficultyStats[diff.title] = diffAccuracy
        }

        val overallAccuracy = userProfile.accuracyPercentage
        val avgAnswerTime = if (userProfile.totalQuestionsAnswered > 0) {
            userProfile.timePlayedSeconds.toFloat() / userProfile.totalQuestionsAnswered
        } else 8.5f

        // Generate Brain Report
        val brainReport = generateBrainReport(
            userProfile = userProfile,
            strongest = strongestCategory,
            weakest = weakestCategory,
            adaptiveDifficulty = adaptiveDiff,
            avgAnswerTime = avgAnswerTime
        )

        return PlayerPerformanceProfile(
            overallAccuracy = overallAccuracy,
            averageAnswerTimeSeconds = avgAnswerTime,
            totalGamesEvaluated = totalGames,
            currentAdaptiveDifficulty = adaptiveDiff,
            recommendedCategory = weakestCategory ?: GameCategory.MATH,
            recommendationReason = diffReason,
            categoryStats = categoryStatsMap,
            difficultyStats = difficultyStats,
            recentAccuracies = gameHistory.take(10).map { it.accuracy },
            strongestCategory = strongestCategory,
            weakestCategory = weakestCategory,
            brainReport = brainReport
        )
    }

    /**
     * Generates a personalized practice recommendation card for the Home screen.
     */
    fun getPersonalizedRecommendation(
        profile: PlayerPerformanceProfile,
        totalGamesPlayed: Int
    ): PersonalizedRecommendation {
        if (totalGamesPlayed == 0) {
            return PersonalizedRecommendation(
                id = "rec_onboarding",
                title = "Start Your Brain Journey",
                subtitle = "Complete your first challenge to unlock personalized cognitive analytics.",
                category = GameCategory.MATH,
                difficulty = Difficulty.EASY,
                questionCount = 5,
                badge = "🌱 STARTER",
                rationale = "Begin with Math Challenge to establish your baseline cognitive profile."
            )
        }

        val targetCategory = profile.weakestCategory ?: GameCategory.LOGIC
        val targetPerformance = profile.categoryStats[targetCategory.id]

        return if (targetPerformance != null && targetPerformance.questionsAnswered > 0) {
            PersonalizedRecommendation(
                id = "rec_improve_${targetCategory.id}",
                title = "Improve Your ${targetCategory.title}",
                subtitle = "Boost your ${targetCategory.title} accuracy with 5 targeted questions.",
                category = targetCategory,
                difficulty = profile.currentAdaptiveDifficulty,
                questionCount = 5,
                badge = "🎯 TARGETED PRACTICE",
                rationale = "Your recent accuracy in ${targetCategory.title} is ${targetPerformance.accuracyPercentage}%. Targeted practice will accelerate your mastery."
            )
        } else {
            val exploreCategory = GameCategory.values()
                .filter { it != GameCategory.DAILY }
                .firstOrNull { profile.categoryStats[it.id]?.questionsAnswered == 0 }
                ?: GameCategory.SCIENCE

            PersonalizedRecommendation(
                id = "rec_explore_${exploreCategory.id}",
                title = "Explore ${exploreCategory.title}",
                subtitle = "Expand your mental breadth by testing a fresh category.",
                category = exploreCategory,
                difficulty = profile.currentAdaptiveDifficulty,
                questionCount = 5,
                badge = "✨ EXPAND HORIZONS",
                rationale = "Diversifying mental exercises enhances neural plasticity across different problem-solving domains."
            )
        }
    }

    private fun generateBrainReport(
        userProfile: UserProfile,
        strongest: GameCategory?,
        weakest: GameCategory?,
        adaptiveDifficulty: Difficulty,
        avgAnswerTime: Float
    ): BrainReport {
        val accuracy = userProfile.accuracyPercentage
        val speedText = when {
            avgAnswerTime <= 5.0f -> "Rapid (${String.format(Locale.US, "%.1f", avgAnswerTime)}s avg)"
            avgAnswerTime <= 10.0f -> "Balanced (${String.format(Locale.US, "%.1f", avgAnswerTime)}s avg)"
            else -> "Deliberate (${String.format(Locale.US, "%.1f", avgAnswerTime)}s avg)"
        }

        val style = when {
            accuracy >= 80 && avgAnswerTime <= 7.0f -> "Rapid & High Precision Specialist"
            accuracy >= 80 -> "Methodical & Highly Accurate Solver"
            avgAnswerTime <= 6.0f -> "Intuitive Quick-Response Player"
            else -> "Steady Exploratory Learner"
        }

        val consistency = when {
            userProfile.currentStreak >= 7 -> "Exceptional (7+ Day Streak Active)"
            userProfile.currentStreak >= 3 -> "Consistent (${userProfile.currentStreak} Day Streak)"
            else -> "Building Momentum (${userProfile.currentStreak} Days)"
        }

        return BrainReport(
            title = "Cognitive Profile Summary",
            cognitiveStyle = style,
            strongestDomain = strongest?.title ?: "General Problem Solving",
            practiceFocus = weakest?.title ?: "Logical Reasoning",
            accuracyRateText = "$accuracy% Overall Accuracy",
            averageSpeedText = speedText,
            consistencyText = consistency,
            recommendedDifficulty = adaptiveDifficulty
        )
    }
}
