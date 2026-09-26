package com.example.data.competitive

import com.example.model.Achievement
import com.example.model.GameCategory
import com.example.model.GameResult
import com.example.model.UserProfile

/**
 * Achievement Evaluation and Progression Engine
 */
class AchievementEngine {

    companion object {
        const val ID_FIRST_BATTLE = "FIRST_BATTLE"
        const val ID_PERFECT_SCORE = "PERFECT_SCORE"
        const val ID_STREAK_5 = "STREAK_5"
        const val ID_STREAK_10 = "STREAK_10"
        const val ID_STREAK_25 = "STREAK_25"
        const val ID_SPEED_DEMON = "SPEED_DEMON"
        const val ID_BRAIN_MASTER = "BRAIN_MASTER"
        const val ID_CENTURY = "CENTURY"
        const val ID_THOUSAND_POINTS = "THOUSAND_POINTS"
        const val ID_DAILY_WARRIOR = "DAILY_WARRIOR"
        const val ID_MATH_MASTER = "MATH_MASTER"
        const val ID_LOGIC_MASTER = "LOGIC_MASTER"
        const val ID_SCIENCE_MASTER = "SCIENCE_MASTER"
        const val ID_WORD_MASTER = "WORD_MASTER"

        // Social & Challenge Achievements
        const val ID_FIRST_CHALLENGE = "FIRST_CHALLENGE"
        const val ID_CHALLENGE_WIN_5 = "CHALLENGE_WIN_5"
        const val ID_CHALLENGE_WIN_10 = "CHALLENGE_WIN_10"
        const val ID_FRIEND_MAKER = "FRIEND_MAKER"
        const val ID_RIVAL = "RIVAL"
        const val ID_SOCIAL_BRAIN = "SOCIAL_BRAIN"

        val STARTER_ACHIEVEMENT_DEFINITIONS = listOf(
            Achievement(
                id = ID_FIRST_BATTLE,
                iconEmoji = "⚔️",
                name = "First Battle",
                description = "Complete your first Brain Battle game.",
                currentProgress = 0,
                targetProgress = 1,
                isUnlocked = false,
                xpBonus = 25,
                category = AchievementCategory.GETTING_STARTED,
                rarity = AchievementRarity.COMMON
            ),
            Achievement(
                id = ID_PERFECT_SCORE,
                iconEmoji = "🎯",
                name = "Perfect Score",
                description = "Complete a game with 100% accuracy.",
                currentProgress = 0,
                targetProgress = 1,
                isUnlocked = false,
                xpBonus = 100,
                category = AchievementCategory.ACCURACY,
                rarity = AchievementRarity.UNCOMMON
            ),
            Achievement(
                id = ID_STREAK_5,
                iconEmoji = "🔥",
                name = "Streak 5",
                description = "Get 5 correct answers consecutively.",
                currentProgress = 0,
                targetProgress = 5,
                isUnlocked = false,
                xpBonus = 50,
                category = AchievementCategory.STREAKS,
                rarity = AchievementRarity.COMMON
            ),
            Achievement(
                id = ID_STREAK_10,
                iconEmoji = "⚡",
                name = "Streak 10",
                description = "Get 10 correct answers consecutively.",
                currentProgress = 0,
                targetProgress = 10,
                isUnlocked = false,
                xpBonus = 150,
                category = AchievementCategory.STREAKS,
                rarity = AchievementRarity.UNCOMMON
            ),
            Achievement(
                id = ID_STREAK_25,
                iconEmoji = "🌟",
                name = "Streak 25",
                description = "Get 25 correct answers consecutively.",
                currentProgress = 0,
                targetProgress = 25,
                isUnlocked = false,
                xpBonus = 300,
                category = AchievementCategory.STREAKS,
                rarity = AchievementRarity.RARE
            ),
            Achievement(
                id = ID_SPEED_DEMON,
                iconEmoji = "⏱️",
                name = "Speed Demon",
                description = "Complete a game with an average response time under 3 seconds per question.",
                currentProgress = 0,
                targetProgress = 1,
                isUnlocked = false,
                xpBonus = 100,
                category = AchievementCategory.SPEED,
                rarity = AchievementRarity.RARE
            ),
            Achievement(
                id = ID_BRAIN_MASTER,
                iconEmoji = "🧠",
                name = "Brain Master",
                description = "Reach Level 10 across any game modes.",
                currentProgress = 0,
                targetProgress = 10,
                isUnlocked = false,
                xpBonus = 500,
                category = AchievementCategory.PROGRESSION,
                rarity = AchievementRarity.EPIC
            ),
            Achievement(
                id = ID_CENTURY,
                iconEmoji = "💯",
                name = "Century",
                description = "Complete 100 games.",
                currentProgress = 0,
                targetProgress = 100,
                isUnlocked = false,
                xpBonus = 400,
                category = AchievementCategory.PROGRESSION,
                rarity = AchievementRarity.EPIC
            ),
            Achievement(
                id = ID_THOUSAND_POINTS,
                iconEmoji = "👑",
                name = "Thousand Points",
                description = "Score 1,000 or more points in a single game.",
                currentProgress = 0,
                targetProgress = 1000,
                isUnlocked = false,
                xpBonus = 150,
                category = AchievementCategory.COMPETITIVE,
                rarity = AchievementRarity.UNCOMMON
            ),
            Achievement(
                id = ID_DAILY_WARRIOR,
                iconEmoji = "📅",
                name = "Daily Warrior",
                description = "Complete 7 Daily Challenges.",
                currentProgress = 0,
                targetProgress = 7,
                isUnlocked = false,
                xpBonus = 250,
                category = AchievementCategory.SPECIAL,
                rarity = AchievementRarity.RARE
            ),
            Achievement(
                id = ID_MATH_MASTER,
                iconEmoji = "🧮",
                name = "Math Master",
                description = "Answer 50 Math questions correctly.",
                currentProgress = 0,
                targetProgress = 50,
                isUnlocked = false,
                xpBonus = 200,
                category = AchievementCategory.CATEGORIES,
                rarity = AchievementRarity.UNCOMMON
            ),
            Achievement(
                id = ID_LOGIC_MASTER,
                iconEmoji = "💡",
                name = "Logic Master",
                description = "Answer 50 Logic questions correctly.",
                currentProgress = 0,
                targetProgress = 50,
                isUnlocked = false,
                xpBonus = 200,
                category = AchievementCategory.CATEGORIES,
                rarity = AchievementRarity.UNCOMMON
            ),
            Achievement(
                id = ID_SCIENCE_MASTER,
                iconEmoji = "🔬",
                name = "Science Master",
                description = "Answer 50 Science questions correctly.",
                currentProgress = 0,
                targetProgress = 50,
                isUnlocked = false,
                xpBonus = 200,
                category = AchievementCategory.CATEGORIES,
                rarity = AchievementRarity.UNCOMMON
            ),
            Achievement(
                id = ID_WORD_MASTER,
                iconEmoji = "📖",
                name = "Word Master",
                description = "Answer 50 Word & Language questions correctly.",
                currentProgress = 0,
                targetProgress = 50,
                isUnlocked = false,
                xpBonus = 200,
                category = AchievementCategory.CATEGORIES,
                rarity = AchievementRarity.UNCOMMON
            ),
            Achievement(
                id = ID_FIRST_CHALLENGE,
                iconEmoji = "⚔️",
                name = "First Challenge",
                description = "Complete your first friend challenge match.",
                currentProgress = 0,
                targetProgress = 1,
                isUnlocked = false,
                xpBonus = 150,
                category = AchievementCategory.COMPETITIVE,
                rarity = AchievementRarity.COMMON
            ),
            Achievement(
                id = ID_CHALLENGE_WIN_5,
                iconEmoji = "🏅",
                name = "Duelist",
                description = "Win 5 friend challenges.",
                currentProgress = 0,
                targetProgress = 5,
                isUnlocked = false,
                xpBonus = 250,
                category = AchievementCategory.COMPETITIVE,
                rarity = AchievementRarity.RARE
            ),
            Achievement(
                id = ID_CHALLENGE_WIN_10,
                iconEmoji = "👑",
                name = "Challenge Master",
                description = "Win 10 friend challenges.",
                currentProgress = 0,
                targetProgress = 10,
                isUnlocked = false,
                xpBonus = 500,
                category = AchievementCategory.COMPETITIVE,
                rarity = AchievementRarity.EPIC
            ),
            Achievement(
                id = ID_FRIEND_MAKER,
                iconEmoji = "👥",
                name = "Friend Maker",
                description = "Connect with 5 or more friends.",
                currentProgress = 0,
                targetProgress = 5,
                isUnlocked = false,
                xpBonus = 200,
                category = AchievementCategory.SPECIAL,
                rarity = AchievementRarity.UNCOMMON
            ),
            Achievement(
                id = ID_RIVAL,
                iconEmoji = "🥊",
                name = "True Rival",
                description = "Complete repeated challenge matches with a rival.",
                currentProgress = 0,
                targetProgress = 3,
                isUnlocked = false,
                xpBonus = 300,
                category = AchievementCategory.COMPETITIVE,
                rarity = AchievementRarity.RARE
            ),
            Achievement(
                id = ID_SOCIAL_BRAIN,
                iconEmoji = "🌐",
                name = "Social Brain",
                description = "Complete 10 total social matches.",
                currentProgress = 0,
                targetProgress = 10,
                isUnlocked = false,
                xpBonus = 350,
                category = AchievementCategory.SPECIAL,
                rarity = AchievementRarity.RARE
            )
        )
    }

    /**
     * Compute current achievement progress list based on real player profile,
     * unlocked IDs, and category statistics.
     */
    fun computeAchievements(
        profile: UserProfile,
        unlockedAchievementIds: Set<String>,
        categoryCorrectCounts: Map<String, Int> = emptyMap(),
        unlockedTimestamps: Map<String, Long> = emptyMap()
    ): List<Achievement> {
        return STARTER_ACHIEVEMENT_DEFINITIONS.map { def ->
            val wasUnlocked = def.id in unlockedAchievementIds
            val progress = calculateRealProgress(def.id, profile, categoryCorrectCounts)
            val isNowUnlocked = wasUnlocked || progress >= def.targetProgress

            def.copy(
                currentProgress = if (isNowUnlocked) def.targetProgress else progress.coerceAtMost(def.targetProgress),
                isUnlocked = isNowUnlocked,
                unlockedAt = unlockedTimestamps[def.id] ?: if (isNowUnlocked) System.currentTimeMillis() else null
            )
        }
    }

    /**
     * Calculate exact numerical progress for each achievement ID.
     */
    private fun calculateRealProgress(
        achievementId: String,
        profile: UserProfile,
        categoryCorrectCounts: Map<String, Int>
    ): Int {
        return when (achievementId) {
            ID_FIRST_BATTLE -> minOf(profile.gamesPlayed, 1)
            ID_PERFECT_SCORE -> if (profile.accuracyPercentage == 100 && profile.gamesPlayed > 0) 1 else 0
            ID_STREAK_5 -> minOf(profile.bestStreak, 5)
            ID_STREAK_10 -> minOf(profile.bestStreak, 10)
            ID_STREAK_25 -> minOf(profile.bestStreak, 25)
            ID_SPEED_DEMON -> if (profile.gamesPlayed > 0 && profile.timePlayedSeconds > 0 &&
                (profile.totalQuestionsAnswered > 0 && (profile.timePlayedSeconds.toFloat() / profile.totalQuestionsAnswered) <= 3.0f)) 1 else 0
            ID_BRAIN_MASTER -> minOf(profile.level, 10)
            ID_CENTURY -> minOf(profile.gamesPlayed, 100)
            ID_THOUSAND_POINTS -> minOf(profile.bestScore, 1000)
            ID_DAILY_WARRIOR -> minOf(profile.currentStreak, 7)
            ID_MATH_MASTER -> minOf(categoryCorrectCounts[GameCategory.MATH.id] ?: (profile.correctAnswers / 8), 50)
            ID_LOGIC_MASTER -> minOf(categoryCorrectCounts[GameCategory.LOGIC.id] ?: (profile.correctAnswers / 8), 50)
            ID_SCIENCE_MASTER -> minOf(categoryCorrectCounts[GameCategory.SCIENCE.id] ?: (profile.correctAnswers / 8), 50)
            ID_WORD_MASTER -> minOf(categoryCorrectCounts[GameCategory.WORDS.id] ?: (profile.correctAnswers / 8), 50)
            ID_FIRST_CHALLENGE -> minOf(profile.challengesPlayed, 1)
            ID_CHALLENGE_WIN_5 -> minOf(profile.challengesWon, 5)
            ID_CHALLENGE_WIN_10 -> minOf(profile.challengesWon, 10)
            ID_FRIEND_MAKER -> 0
            ID_RIVAL -> minOf(profile.challengesPlayed, 3)
            ID_SOCIAL_BRAIN -> minOf(profile.challengesPlayed, 10)
            else -> 0
        }
    }

    /**
     * Evaluates a completed game session and returns any NEWLY unlocked achievements.
     * Prevents duplicate unlock rewards.
     */
    fun evaluateGameSession(
        sessionScore: Int,
        correctCount: Int,
        totalQuestions: Int,
        durationSeconds: Int,
        categoryTitle: String,
        gameMode: String,
        bestAnswerStreak: Int,
        profile: UserProfile,
        unlockedAchievementIds: Set<String>,
        categoryCorrectCounts: Map<String, Int>
    ): Pair<List<Achievement>, Int> {
        val newlyUnlocked = mutableListOf<Achievement>()
        var totalXpBonus = 0

        val accuracy = if (totalQuestions > 0) (correctCount * 100) / totalQuestions else 0
        val avgTimePerQuestion = if (totalQuestions > 0) durationSeconds.toFloat() / totalQuestions else 99f

        for (def in STARTER_ACHIEVEMENT_DEFINITIONS) {
            if (def.id in unlockedAchievementIds) continue

            var conditionMet = false

            when (def.id) {
                ID_FIRST_BATTLE -> {
                    if (profile.gamesPlayed + 1 >= 1) conditionMet = true
                }
                ID_PERFECT_SCORE -> {
                    if (accuracy == 100 && totalQuestions >= 5) conditionMet = true
                }
                ID_STREAK_5 -> {
                    if (bestAnswerStreak >= 5 || profile.bestStreak >= 5) conditionMet = true
                }
                ID_STREAK_10 -> {
                    if (bestAnswerStreak >= 10 || profile.bestStreak >= 10) conditionMet = true
                }
                ID_STREAK_25 -> {
                    if (bestAnswerStreak >= 25 || profile.bestStreak >= 25) conditionMet = true
                }
                ID_SPEED_DEMON -> {
                    if (avgTimePerQuestion <= 3.0f && totalQuestions >= 5 && accuracy >= 70) conditionMet = true
                }
                ID_BRAIN_MASTER -> {
                    if (profile.level >= 10) conditionMet = true
                }
                ID_CENTURY -> {
                    if (profile.gamesPlayed + 1 >= 100) conditionMet = true
                }
                ID_THOUSAND_POINTS -> {
                    if (sessionScore >= 1000 || profile.bestScore >= 1000) conditionMet = true
                }
                ID_DAILY_WARRIOR -> {
                    if (gameMode == "daily_challenge" && profile.currentStreak + 1 >= 7) conditionMet = true
                }
                ID_MATH_MASTER -> {
                    val count = (categoryCorrectCounts[GameCategory.MATH.id] ?: 0) + (if (categoryTitle.contains("Math", ignoreCase = true)) correctCount else 0)
                    if (count >= 50) conditionMet = true
                }
                ID_LOGIC_MASTER -> {
                    val count = (categoryCorrectCounts[GameCategory.LOGIC.id] ?: 0) + (if (categoryTitle.contains("Logic", ignoreCase = true)) correctCount else 0)
                    if (count >= 50) conditionMet = true
                }
                ID_SCIENCE_MASTER -> {
                    val count = (categoryCorrectCounts[GameCategory.SCIENCE.id] ?: 0) + (if (categoryTitle.contains("Science", ignoreCase = true)) correctCount else 0)
                    if (count >= 50) conditionMet = true
                }
                ID_WORD_MASTER -> {
                    val count = (categoryCorrectCounts[GameCategory.WORDS.id] ?: 0) + (if (categoryTitle.contains("Word", ignoreCase = true)) correctCount else 0)
                    if (count >= 50) conditionMet = true
                }
            }

            if (conditionMet) {
                newlyUnlocked.add(
                    def.copy(
                        currentProgress = def.targetProgress,
                        isUnlocked = true,
                        unlockedAt = System.currentTimeMillis()
                    )
                )
                totalXpBonus += def.xpBonus
            }
        }

        return Pair(newlyUnlocked, totalXpBonus)
    }
}
