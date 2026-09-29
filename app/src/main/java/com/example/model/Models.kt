package com.example.model

enum class Difficulty(val title: String, val description: String, val multiplier: Float, val basePoints: Int, val baseXP: Int) {
    EASY("Easy", "For beginners. Relaxed timer and straightforward questions.", 1.0f, 100, 10),
    MEDIUM("Medium", "For regular players. Balanced speed and thought challenge.", 1.5f, 150, 15),
    HARD("Hard", "For experienced players. Tougher puzzles and faster clock.", 2.0f, 200, 20),
    EXTREME("Extreme", "For advanced brain masters. High speed, complex logic.", 2.5f, 300, 30)
}

enum class GameCategory(
    val id: String,
    val title: String,
    val iconEmoji: String,
    val shortDescription: String,
    val defaultDifficulty: Difficulty,
    val xpReward: Int
) {
    MATH("math", "Mathematics", "🧮", "Calculation & math problems", Difficulty.MEDIUM, 150),
    LOGIC("logic", "Logic", "🧩", "Logical reasoning & deduction puzzles", Difficulty.HARD, 200),
    SCIENCE("science", "Science", "🧪", "General science, physics, biology & chemistry", Difficulty.HARD, 190),
    GEOGRAPHY("geography", "Geography", "🗺️", "Countries, locations, capitals & landmarks", Difficulty.MEDIUM, 175),
    KNOWLEDGE("knowledge", "General Knowledge", "🌍", "Trivia, world history & broad domain facts", Difficulty.MEDIUM, 175),
    WORDS("words", "Word & Vocabulary", "🔤", "Vocabulary, word definitions & word problems", Difficulty.EASY, 140),
    MEMORY("memory", "Memory", "🧠", "Memory-based question & recall gameplay", Difficulty.MEDIUM, 180),
    PATTERNS("patterns", "Patterns", "🔷", "Sequences & visual/numeric pattern recognition", Difficulty.MEDIUM, 180),
    RIDDLES("riddles", "Riddles", "💡", "Brain teasers, clever wordplay & riddles", Difficulty.HARD, 200),
    TECHNOLOGY("technology", "Technology", "💻", "Tech, computing, AI & modern systems", Difficulty.HARD, 200),

    // Backward-compatibility aliases
    NUMBERS("numbers", "Number Puzzle", "🔢", "Sequences, missing digits & matrices", Difficulty.MEDIUM, 160),
    SPEED("speed", "Speed Challenge", "⚡", "Rapid-fire reflexes under time pressure", Difficulty.HARD, 220),
    DAILY("daily", "Daily Challenge", "🔥", "Curated daily test for max streak bonus", Difficulty.MEDIUM, 250);

    val icon: String get() = iconEmoji

    companion object {
        val ALL_10_CATEGORIES: List<GameCategory> = listOf(
            MATH, LOGIC, SCIENCE, GEOGRAPHY, KNOWLEDGE,
            WORDS, MEMORY, PATTERNS, RIDDLES, TECHNOLOGY
        )
    }
}

enum class GameModeType(
    val id: String,
    val title: String,
    val description: String,
    val difficulty: String,
    val reward: String,
    val iconEmoji: String
) {
    QUICK_BATTLE("quick", "QUICK BATTLE", "10 questions under per-question timer pressure. Competitive leaderboard updates.", "Competitive", "+200 XP • Ranked", "⚡"),
    TARGETED_PRACTICE("practice", "PRACTICE", "Untimed or relaxed learning with detailed answer explanations after each question.", "Learning", "Skills & Explanations", "🧠"),
    ENDLESS_MODE("endless", "ENDLESS", "Continuous questions scaling from Easy to Hard. Ends when you run out of lives.", "Scaling", "+15 XP / Question", "♾️"),
    DAILY_CHALLENGE("daily", "DAILY CHALLENGE", "Fixed daily seed question set. Exactly one attempt permitted per calendar day.", "Daily Seed", "+250 XP + Daily Streak", "🔥"),
    FRIEND_CHALLENGE("friend", "FRIEND CHALLENGE", "Head-to-head match using custom room code challenge system.", "Versus", "+150 XP • Direct Match", "👥"),
    CATEGORY_BATTLE("category", "CATEGORY BATTLE", "Target specific mental skills across 10 dedicated category pools.", "Custom", "+180 XP", "🎯"),
    TOURNAMENTS("tournaments", "TOURNAMENTS", "Compete on the weekly leaderboard bracket for grand champion trophies.", "Competitive", "Trophies & Badges", "🏆"),
    SIXTY_SECOND_RUSH("rush", "60-SECOND RUSH", "Answer as many rapid questions as you can before the clock expires!", "Speed", "+200 XP", "⏱️");
}

enum class QuestionSource {
    CURATED,
    AI_GENERATED,
    IMPORTED,
    SYSTEM
}

enum class ValidationStatus {
    PENDING,
    APPROVED,
    REJECTED,
    REPORTED,
    DISABLED
}

data class Question(
    val id: String,
    val categoryId: String,
    val difficulty: Difficulty,
    val questionText: String,
    val answerOptions: List<String>,
    val correctAnswer: String,
    val explanation: String,
    val points: Int = 100,
    val timeLimit: Int = 15,
    val tags: List<String> = emptyList(),
    val imageRef: String? = null,
    val source: QuestionSource = QuestionSource.CURATED,
    val validationStatus: ValidationStatus = ValidationStatus.APPROVED,
    val qualityScore: Int = 100,
    val createdAt: Long = System.currentTimeMillis()
) {
    // Backward compatibility property for existing UI references
    val prompt: String get() = questionText
    val options: List<String> get() = answerOptions
    val correctIndex: Int get() = answerOptions.indexOf(correctAnswer).coerceAtLeast(0)
}

enum class GameStatus {
    READY,
    PLAYING,
    PAUSED,
    ANSWERED,
    COMPLETED,
    GAME_OVER,
    ABANDONED
}

data class GameHistoryItem(
    val id: String,
    val date: String,
    val gameMode: String,
    val category: String,
    val difficulty: String,
    val score: Int,
    val accuracy: Int,
    val xp: Int,
    val durationSeconds: Int,
    val correctAnswers: Int,
    val incorrectAnswers: Int,
    val bestStreak: Int
)

data class GameResult(
    val score: Int,
    val correctCount: Int,
    val totalQuestions: Int,
    val accuracyPercentage: Int,
    val timeSpentSeconds: Int,
    val xpEarned: Int,
    val currentStreakDays: Int,
    val categoryTitle: String,
    val isNewPersonalBest: Boolean = false,
    val bestAnswerStreak: Int = 0,
    val gameMode: String = "category",
    val previousPersonalBest: Int = 0,
    val newlyUnlockedAchievements: List<Achievement> = emptyList(),
    val didLevelUp: Boolean = false,
    val newLevel: Int = 1,
    val newTitle: String? = null
)

data class Achievement(
    val id: String,
    val iconEmoji: String,
    val name: String,
    val description: String,
    val currentProgress: Int,
    val targetProgress: Int,
    val isUnlocked: Boolean,
    val xpBonus: Int,
    val category: com.example.data.competitive.AchievementCategory = com.example.data.competitive.AchievementCategory.GETTING_STARTED,
    val rarity: com.example.data.competitive.AchievementRarity = com.example.data.competitive.AchievementRarity.COMMON,
    val unlockedAt: Long? = null
)

data class LeaderboardEntry(
    val rank: Int,
    val username: String,
    val avatarEmoji: String,
    val score: Int,
    val level: Int,
    val countryFlag: String,
    val isCurrentUser: Boolean = false,
    val userId: String = "",
    val xp: Int = 0,
    val country: String = "",
    val accuracy: Int = 0,
    val durationSeconds: Int = 0,
    val validationStatus: com.example.data.competitive.ScoreValidationStatus = com.example.data.competitive.ScoreValidationStatus.VALIDATED,
    val playerTitle: String = ""
)

enum class LeaderboardTab {
    GLOBAL, COUNTRY, FRIENDS
}

data class Tournament(
    val id: String,
    val name: String,
    val participantCount: Int,
    val startTime: String,
    val endTime: String,
    val entryRequirement: String,
    val prizeRewardInfo: String,
    val rules: String,
    val status: String,
    val topLeaderboard: List<LeaderboardEntry>
)

data class FriendChallenge(
    val id: String,
    val code: String,
    val challengerName: String,
    val targetScore: Int,
    val category: String,
    val status: String,
    val createdAt: String
)

data class AppNotification(
    val id: String,
    val title: String,
    val message: String,
    val timeAgo: String,
    val iconEmoji: String,
    val type: NotificationType,
    val isRead: Boolean = false,
    val actionData: String? = null,
    val timestampMillis: Long = System.currentTimeMillis()
)

enum class NotificationType {
    DAILY_CHALLENGE,
    ACHIEVEMENT,
    STREAK,
    TOURNAMENT,
    FRIEND_CHALLENGE,
    SYSTEM,
    FRIEND_REQUEST,
    FRIEND_ACCEPTED,
    CHALLENGE_RECEIVED,
    CHALLENGE_ACCEPTED,
    CHALLENGE_DECLINED,
    CHALLENGE_EXPIRED,
    MATCH_COMPLETED,
    REMATCH_REQUEST,
    RANK_CHANGE
}

data class UserProfile(
    val uid: String = "",
    val playerId: String = "",
    val username: String = "Kingzo",
    val displayName: String = "Kingzo",
    val email: String = "kingzo@brainbattle.game",
    val photoUrl: String? = null,
    val avatarEmoji: String = "🧠",
    val level: Int = 1,
    val currentXp: Int = 0,
    val nextLevelXp: Int = 100,
    val gamesPlayed: Int = 0,
    val totalScore: Int = 0,
    val bestScore: Int = 0,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val totalXp: Int = 0,
    val accuracyPercentage: Int = 0,
    val achievementsUnlocked: Int = 0,
    val totalAchievements: Int = 10,
    val isPremium: Boolean = false,
    val lastDailyCompletedDate: String? = null,
    val dailyChallengeCompleted: Boolean = false,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val lastActiveAt: Long = 0L,
    val country: String = "Global",
    val countryFlag: String = "🌐",
    val language: String = "en",
    val totalQuestionsAnswered: Int = 0,
    val correctAnswers: Int = 0,
    val incorrectAnswers: Int = 0,
    val timePlayedSeconds: Int = 0,
    val selectedTitle: String = "Rookie",
    val unlockedTitles: List<String> = listOf("Rookie"),
    val countryRank: Int? = null,
    val globalRank: Int? = null,
    val challengesPlayed: Int = 0,
    val challengesWon: Int = 0,
    val challengesLost: Int = 0,
    val challengesDraw: Int = 0,
    val emailVerified: Boolean = false,
    val provider: String = "password",
    val role: String = "CUSTOMER / PIONEER",
    val lastLoginAt: Long = 0L
)

data class UserSettings(
    val soundEffects: Boolean = true,
    val music: Boolean = true,
    val vibration: Boolean = true,
    val timerSpeedNormal: Boolean = true,
    val darkTheme: Boolean = true,
    val animationsEnabled: Boolean = true,
    val dailyReminder: Boolean = true,
    val streakReminder: Boolean = true,
    val achievementAlerts: Boolean = true,
    val updatedAt: Long = 0L
)

enum class SyncState(val label: String, val badge: String) {
    IDLE("Saved", "☁️"),
    SAVING("Saving...", "⏳"),
    SYNCING("Syncing...", "🔄"),
    OFFLINE("Offline", "📡"),
    SYNC_COMPLETED("Synced", "✓"),
    ERROR("Sync failed", "⚠️")
}

data class CloudGameHistoryRecord(
    val gameId: String = "",
    val mode: String = "",
    val category: String = "",
    val difficulty: String = "",
    val score: Int = 0,
    val xpEarned: Int = 0,
    val correctAnswers: Int = 0,
    val incorrectAnswers: Int = 0,
    val totalQuestions: Int = 0,
    val accuracy: Int = 0,
    val duration: Int = 0,
    val bestAnswerStreak: Int = 0,
    val completedAt: Long = 0L,
    val isVerified: Boolean = false
)

data class PendingSyncItem(
    val id: String,
    val gameRecord: CloudGameHistoryRecord,
    val timestamp: Long = System.currentTimeMillis()
)

data class QuestionValidationResult(
    val isValid: Boolean,
    val qualityScore: Int,
    val failureReasons: List<String> = emptyList(),
    val mathVerificationPassed: Boolean = true,
    val duplicateSimilarity: Float = 0f
)

data class CategoryPerformance(
    val category: GameCategory,
    val questionsAnswered: Int = 0,
    val correctAnswers: Int = 0,
    val accuracyPercentage: Int = 0,
    val averageResponseTimeSeconds: Float = 0f,
    val lastPlayedTimestamp: Long = 0L
)

data class BrainReport(
    val title: String,
    val cognitiveStyle: String,
    val strongestDomain: String,
    val practiceFocus: String,
    val accuracyRateText: String,
    val averageSpeedText: String,
    val consistencyText: String,
    val recommendedDifficulty: Difficulty
)

data class PlayerPerformanceProfile(
    val overallAccuracy: Int = 0,
    val averageAnswerTimeSeconds: Float = 0f,
    val totalGamesEvaluated: Int = 0,
    val currentAdaptiveDifficulty: Difficulty = Difficulty.MEDIUM,
    val recommendedCategory: GameCategory? = null,
    val recommendationReason: String = "",
    val categoryStats: Map<String, CategoryPerformance> = emptyMap(),
    val difficultyStats: Map<String, Int> = emptyMap(),
    val recentAccuracies: List<Int> = emptyList(),
    val strongestCategory: GameCategory? = null,
    val weakestCategory: GameCategory? = null,
    val brainReport: BrainReport? = null
)

enum class ReportReason(val label: String) {
    WRONG_ANSWER("Wrong answer"),
    AMBIGUOUS_QUESTION("Ambiguous question"),
    TYPO("Typo or formatting issue"),
    OFFENSIVE_CONTENT("Inappropriate or offensive"),
    DUPLICATE_QUESTION("Duplicate question"),
    OTHER("Other issue")
}

data class QuestionReport(
    val id: String = "rep_${System.currentTimeMillis()}",
    val questionId: String,
    val questionText: String = "",
    val reason: ReportReason,
    val details: String = "",
    val reportedAt: Long = System.currentTimeMillis(),
    val reportedByUserId: String = ""
)

data class PersonalizedRecommendation(
    val id: String,
    val title: String,
    val subtitle: String,
    val category: GameCategory,
    val difficulty: Difficulty,
    val questionCount: Int = 5,
    val badge: String = "🎯 FOR YOU",
    val rationale: String
)

