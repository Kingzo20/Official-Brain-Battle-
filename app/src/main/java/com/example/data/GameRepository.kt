package com.example.data

import android.content.Context
import android.util.Log
import com.example.data.ai.*
import com.example.data.auth.AuthResult
import com.example.data.auth.AuthService
import com.example.data.auth.FirebaseAuthServiceImpl
import com.example.data.firestore.FirestoreRepository
import com.example.data.game.ScoreValidationService
import com.example.data.game.ScoreValidationServiceImpl
import com.example.data.local.LocalStorageRepository
import com.example.data.sync.SyncService
import com.example.data.competitive.*
import com.example.data.social.SocialRepository
import com.example.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class GameRepository(context: Context? = null) {

    private val appContext = context?.applicationContext
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    val localRepo: LocalStorageRepository? = appContext?.let { LocalStorageRepository(it) }
    val firestoreRepo: FirestoreRepository? = appContext?.let { FirestoreRepository(it) }
    val authService: AuthService? = appContext?.let { FirebaseAuthServiceImpl(it, coroutineScope) }
    val syncService: SyncService? = if (appContext != null && authService != null && firestoreRepo != null && localRepo != null) {
        SyncService(appContext, authService, firestoreRepo, localRepo, coroutineScope)
    } else null
    val scoreValidation: ScoreValidationService = ScoreValidationServiceImpl()

    // Phase 4: AI & Intelligence Layer Services
    val questionValidationService = QuestionValidationService()
    val aiQuestionService: AiQuestionService = AiQuestionServiceImpl()
    val questionAdminService = QuestionAdminService(localRepo, questionValidationService, aiQuestionService)
    val adaptiveDifficultyEngine = AdaptiveDifficultyEngine()
    val playerPerformanceAnalytics = PlayerPerformanceAnalytics(adaptiveDifficultyEngine)

    // Phase 5: Competitive Progression Services
    val antiCheatService = AntiCheatService()
    val achievementEngine = AchievementEngine()
    val personalBestService = PersonalBestService(appContext)
    val titleManager = TitleManager()
    val leaderboardService = LeaderboardService(appContext ?: context, firestoreRepo, antiCheatService)

    val socialRepository: SocialRepository = SocialRepository(
        context = appContext ?: context,
        firestoreRepo = firestoreRepo,
        currentProfileProvider = { _userProfile.value },
        onRewardXp = { xp -> awardXp(xp) },
        onNotificationCreated = { notif ->
            _notifications.update { listOf(notif) + it }
        },
        onAchievementUnlocked = { achId ->
            unlockAchievementById(achId)
        }
    )

    private val _unlockedAchievementIds = MutableStateFlow<Set<String>>(localRepo?.loadUnlockedAchievementIds() ?: setOf(AchievementEngine.ID_FIRST_BATTLE))
    val unlockedAchievementIds: StateFlow<Set<String>> = _unlockedAchievementIds.asStateFlow()

    private val _userProfile = MutableStateFlow(localRepo?.loadProfile() ?: UserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _userSettings = MutableStateFlow(localRepo?.loadSettings() ?: UserSettings())
    val userSettings: StateFlow<UserSettings> = _userSettings.asStateFlow()

    private val _lastGameResult = MutableStateFlow<GameResult?>(null)
    val lastGameResult: StateFlow<GameResult?> = _lastGameResult.asStateFlow()

    private val _notifications = MutableStateFlow(createInitialNotifications())
    val notifications: StateFlow<List<AppNotification>> = _notifications.asStateFlow()

    private val _friendChallenges = MutableStateFlow(createInitialFriendChallenges())
    val friendChallenges: StateFlow<List<FriendChallenge>> = _friendChallenges.asStateFlow()

    private val _gameHistory = MutableStateFlow<List<GameHistoryItem>>(loadInitialHistory())
    val gameHistory: StateFlow<List<GameHistoryItem>> = _gameHistory.asStateFlow()

    private val _performanceProfile = MutableStateFlow(
        playerPerformanceAnalytics.analyzePerformance(_userProfile.value, _gameHistory.value)
    )
    val performanceProfile: StateFlow<PlayerPerformanceProfile> = _performanceProfile.asStateFlow()

    private val _personalizedRecommendation = MutableStateFlow<PersonalizedRecommendation?>(
        playerPerformanceAnalytics.getPersonalizedRecommendation(_performanceProfile.value, _userProfile.value.gamesPlayed)
    )
    val personalizedRecommendation: StateFlow<PersonalizedRecommendation?> = _personalizedRecommendation.asStateFlow()

    init {
        // Ensure playerId is set
        if (_userProfile.value.playerId.isBlank()) {
            val pid = socialRepository.getPlayerId()
            _userProfile.update { it.copy(playerId = pid) }
            localRepo?.saveProfile(_userProfile.value)
        }

        // Observe auth changes to keep profile in sync
        authService?.let { auth ->
            coroutineScope.launch {
                auth.currentUser.collect { user ->
                    if (user != null) {
                        // 1. Load user's scoped local profile
                        val localLoaded = localRepo?.loadProfile(user.uid)
                        val baseProfile = (localLoaded ?: UserProfile(uid = user.uid)).copy(
                            uid = user.uid,
                            username = if (user.username.isNotBlank()) user.username else (localLoaded?.username ?: "Player"),
                            displayName = if (user.displayName.isNotBlank()) user.displayName else (localLoaded?.displayName ?: user.username),
                            email = if (user.email.isNotBlank()) user.email else (localLoaded?.email ?: ""),
                            emailVerified = user.isEmailVerified,
                            provider = user.providerId,
                            lastLoginAt = System.currentTimeMillis()
                        )
                        _userProfile.value = baseProfile

                        // 2. Load user's scoped achievements and history
                        val userAchievements = localRepo?.loadUnlockedAchievementIds(user.uid)
                            ?: setOf(AchievementEngine.ID_FIRST_BATTLE)
                        _unlockedAchievementIds.value = userAchievements

                        val userHistory = localRepo?.loadGameHistory(user.uid) ?: emptyList()
                        _gameHistory.value = userHistory

                        // 3. If Firestore is active, fetch cloud authoritative profile and merge
                        if (firestoreRepo != null && user.uid.isNotBlank()) {
                            try {
                                val cloudProfile = firestoreRepo.getUserProfile(user.uid)
                                if (cloudProfile != null) {
                                    val merged = baseProfile.copy(
                                        totalXp = maxOf(baseProfile.totalXp, cloudProfile.totalXp),
                                        level = maxOf(baseProfile.level, cloudProfile.level),
                                        gamesPlayed = maxOf(baseProfile.gamesPlayed, cloudProfile.gamesPlayed),
                                        totalScore = maxOf(baseProfile.totalScore, cloudProfile.totalScore),
                                        bestScore = maxOf(baseProfile.bestScore, cloudProfile.bestScore),
                                        currentStreak = maxOf(baseProfile.currentStreak, cloudProfile.currentStreak),
                                        bestStreak = maxOf(baseProfile.bestStreak, cloudProfile.bestStreak),
                                        totalQuestionsAnswered = maxOf(baseProfile.totalQuestionsAnswered, cloudProfile.totalQuestionsAnswered),
                                        correctAnswers = maxOf(baseProfile.correctAnswers, cloudProfile.correctAnswers),
                                        timePlayedSeconds = maxOf(baseProfile.timePlayedSeconds, cloudProfile.timePlayedSeconds),
                                        playerId = if (cloudProfile.playerId.isNotBlank()) cloudProfile.playerId else baseProfile.playerId
                                    )
                                    _userProfile.value = merged
                                    localRepo?.saveProfile(merged)
                                }
                            } catch (e: Exception) {
                                Log.w("GameRepository", "Cloud profile fetch error: ${e.message}")
                            }
                        }

                        localRepo?.saveProfile(_userProfile.value)
                        refreshPerformanceAnalytics()

                        // Trigger cloud sync
                        syncService?.syncNow()
                    } else {
                        // User signed out: reset to clean unauthenticated state
                        _userProfile.value = UserProfile(
                            uid = "",
                            username = "Guest",
                            displayName = "Guest",
                            email = "",
                            totalXp = 0,
                            level = 1,
                            gamesPlayed = 0,
                            bestScore = 0,
                            currentStreak = 0,
                            emailVerified = false
                        )
                        _unlockedAchievementIds.value = setOf(AchievementEngine.ID_FIRST_BATTLE)
                        _gameHistory.value = emptyList()
                        refreshPerformanceAnalytics()
                    }
                }
            }
        }
    }

    private fun loadInitialHistory(): List<GameHistoryItem> {
        return listOf(
            GameHistoryItem("h1", "Today", "Category Battle", "Math Challenge", "Medium", 840, 80, 160, 52, 8, 2, 5),
            GameHistoryItem("h2", "Yesterday", "Daily Challenge", "Daily Challenge", "Medium", 920, 90, 250, 48, 9, 1, 7),
            GameHistoryItem("h3", "2 days ago", "60-Second Rush", "Speed Challenge", "Hard", 1140, 85, 200, 60, 12, 2, 8)
        )
    }

    fun updateSettings(transform: (UserSettings) -> UserSettings) {
        val updated = transform(_userSettings.value).copy(updatedAt = System.currentTimeMillis())
        _userSettings.value = updated
        localRepo?.saveSettings(updated)

        val uid = _userProfile.value.uid
        if (uid.isNotBlank() && firestoreRepo != null) {
            coroutineScope.launch {
                firestoreRepo.saveSettings(uid, updated)
            }
        }
    }

    fun updateProfile(
        username: String,
        avatarEmoji: String,
        displayName: String = username,
        country: String = _userProfile.value.country,
        countryFlag: String = _userProfile.value.countryFlag,
        language: String = _userProfile.value.language
    ) {
        val updated = _userProfile.value.copy(
            username = username.trim(),
            displayName = displayName.trim().ifBlank { username.trim() },
            avatarEmoji = avatarEmoji,
            country = country,
            countryFlag = countryFlag,
            language = language,
            updatedAt = System.currentTimeMillis()
        )
        _userProfile.value = updated
        localRepo?.saveProfile(updated)

        val uid = updated.uid
        if (uid.isNotBlank() && firestoreRepo != null) {
            coroutineScope.launch {
                syncService?.setSavingState()
                val success = firestoreRepo.saveUserProfile(updated)
                if (success) {
                    syncService?.setIdleState()
                }
            }
        }
    }

    suspend fun signOut(): AuthResult<Unit> {
        val result = authService?.signOut() ?: AuthResult.Success(Unit)
        localRepo?.clearActiveSessionData()
        _userProfile.value = UserProfile(
            uid = "",
            username = "Guest",
            displayName = "Guest",
            email = "",
            totalXp = 0,
            level = 1,
            gamesPlayed = 0,
            bestScore = 0,
            currentStreak = 0
        )
        return result
    }

    suspend fun deleteAccount(): AuthResult<Unit> {
        val result = authService?.deleteAccount() ?: AuthResult.Success(Unit)
        localRepo?.clearActiveSessionData()
        _userProfile.value = UserProfile(
            uid = "",
            username = "Guest",
            displayName = "Guest",
            email = "",
            totalXp = 0,
            level = 1,
            gamesPlayed = 0,
            bestScore = 0,
            currentStreak = 0
        )
        return result
    }

    fun isDailyChallengeCompletedToday(): Boolean {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        return _userProfile.value.lastDailyCompletedDate == todayStr || _userProfile.value.dailyChallengeCompleted
    }

    fun recordGameFinished(
        score: Int,
        correctCount: Int,
        totalQuestions: Int,
        timeSpentSeconds: Int,
        categoryTitle: String,
        gameMode: String = "category",
        bestStreak: Int = 0
    ): GameResult {
        val gameId = "game_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}"

        // Anti-cheat & plausibility evaluation
        val antiCheat = antiCheatService.validateSession(
            gameId = gameId,
            score = score,
            correctCount = correctCount,
            totalQuestions = totalQuestions,
            durationSeconds = timeSpentSeconds,
            difficulty = Difficulty.MEDIUM
        )

        val validScore = if (antiCheat.status != ScoreValidationStatus.REJECTED) score else 0
        val accuracy = if (totalQuestions > 0) (correctCount * 100) / totalQuestions else 0

        // Base XP Calculation
        var earnedXp = (correctCount * 20) + (validScore / 12)
        if (gameMode == GameModeType.DAILY_CHALLENGE.id) earnedXp += 100
        if (gameMode == GameModeType.SIXTY_SECOND_RUSH.id) earnedXp += 75
        if (accuracy == 100) earnedXp += 50

        val current = _userProfile.value
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

        // Personal Best Evaluation (real historical comparison)
        val pbEval = personalBestService.evaluateAndRecord(
            score = validScore,
            gameMode = gameMode,
            categoryId = categoryTitle,
            difficulty = "Medium"
        )
        val isPersonalBest = pbEval.isAnyNewPersonalBest

        // Achievement Evaluation (real progress & unlock check)
        val categoryCounts = _performanceProfile.value.categoryStats.mapValues { it.value.questionsAnswered }
        val (newlyUnlocked, achievementBonusXp) = achievementEngine.evaluateGameSession(
            sessionScore = validScore,
            correctCount = correctCount,
            totalQuestions = totalQuestions,
            durationSeconds = timeSpentSeconds,
            categoryTitle = categoryTitle,
            gameMode = gameMode,
            bestAnswerStreak = bestStreak,
            profile = current,
            unlockedAchievementIds = _unlockedAchievementIds.value,
            categoryCorrectCounts = categoryCounts
        )

        // Save newly unlocked achievements
        if (newlyUnlocked.isNotEmpty()) {
            val updatedUnlockedIds = _unlockedAchievementIds.value.toMutableSet()
            newlyUnlocked.forEach { a ->
                updatedUnlockedIds.add(a.id)
                // Add notification
                _notifications.update { notifs ->
                    listOf(
                        AppNotification(
                            id = "ach_${a.id}_${System.currentTimeMillis()}",
                            title = "🏆 Achievement Unlocked: ${a.name}!",
                            message = "${a.description} (+${a.xpBonus} XP)",
                            timeAgo = "Just now",
                            iconEmoji = a.iconEmoji,
                            type = NotificationType.ACHIEVEMENT
                        )
                    ) + notifs
                }
            }
            _unlockedAchievementIds.value = updatedUnlockedIds
            localRepo?.saveUnlockedAchievementIds(updatedUnlockedIds)
        }

        val totalEarnedXp = earnedXp + achievementBonusXp

        // Streak logic
        val newStreak = if (gameMode == GameModeType.DAILY_CHALLENGE.id) {
            if (current.lastDailyCompletedDate != todayStr) current.currentStreak + 1 else current.currentStreak
        } else {
            current.currentStreak
        }

        val newGamesPlayed = current.gamesPlayed + 1
        val newBestScore = maxOf(current.bestScore, validScore)
        val newBestStreak = maxOf(current.bestStreak, bestStreak)
        val newTotalXp = current.totalXp + totalEarnedXp
        val newTotalScore = current.totalScore + validScore

        val prevLevel = current.level
        val newLevelInfo = GameScoringConfig.calculateLevelInfo(newTotalXp)
        val didLevelUp = newLevelInfo.level > prevLevel

        // Title unlocked check
        val availableTitles = titleManager.getAvailableTitles(current.copy(level = newLevelInfo.level), _unlockedAchievementIds.value)
        val newlyUnlockedTitle = availableTitles.firstOrNull { it.isUnlocked && it.title !in current.unlockedTitles }?.title

        val updatedUnlockedTitles = (current.unlockedTitles + listOfNotNull(newlyUnlockedTitle)).distinct()

        val updatedProfile = current.copy(
            level = newLevelInfo.level,
            currentXp = newLevelInfo.currentXpInLevel,
            nextLevelXp = newLevelInfo.xpRequiredForNextLevel,
            gamesPlayed = newGamesPlayed,
            totalScore = newTotalScore,
            bestScore = newBestScore,
            currentStreak = newStreak,
            bestStreak = newBestStreak,
            totalXp = newTotalXp,
            accuracyPercentage = if (newGamesPlayed > 0) ((current.accuracyPercentage * (newGamesPlayed - 1)) + accuracy) / newGamesPlayed else accuracy,
            achievementsUnlocked = _unlockedAchievementIds.value.size,
            totalAchievements = 14,
            lastDailyCompletedDate = if (gameMode == GameModeType.DAILY_CHALLENGE.id) todayStr else current.lastDailyCompletedDate,
            dailyChallengeCompleted = if (gameMode == GameModeType.DAILY_CHALLENGE.id) true else current.dailyChallengeCompleted,
            totalQuestionsAnswered = current.totalQuestionsAnswered + totalQuestions,
            correctAnswers = current.correctAnswers + correctCount,
            incorrectAnswers = current.incorrectAnswers + (totalQuestions - correctCount).coerceAtLeast(0),
            timePlayedSeconds = current.timePlayedSeconds + timeSpentSeconds,
            unlockedTitles = updatedUnlockedTitles,
            updatedAt = System.currentTimeMillis()
        )

        _userProfile.value = updatedProfile
        localRepo?.saveProfile(updatedProfile)

        val cloudRecord = CloudGameHistoryRecord(
            gameId = gameId,
            mode = gameMode,
            category = categoryTitle,
            difficulty = "Medium",
            score = validScore,
            xpEarned = totalEarnedXp,
            correctAnswers = correctCount,
            incorrectAnswers = (totalQuestions - correctCount).coerceAtLeast(0),
            totalQuestions = totalQuestions,
            accuracy = accuracy,
            duration = timeSpentSeconds,
            bestAnswerStreak = bestStreak,
            completedAt = System.currentTimeMillis(),
            isVerified = antiCheat.status == ScoreValidationStatus.VALIDATED
        )

        // Add to offline pending sync queue (idempotent)
        localRepo?.addPendingSync(cloudRecord)

        // Asynchronously update Firestore & submit to competitive Leaderboard
        coroutineScope.launch {
            if (current.uid.isNotBlank() && firestoreRepo != null) {
                firestoreRepo.saveUserProfile(updatedProfile)
                if (gameMode == GameModeType.DAILY_CHALLENGE.id) {
                    firestoreRepo.recordDailyChallenge(current.uid, todayStr, validScore, totalEarnedXp)
                }
                newlyUnlocked.forEach { a ->
                    firestoreRepo.unlockAchievement(current.uid, a.id, a.name, a.xpBonus)
                }
            }

            val isPracticeMode = gameMode == GameModeType.TARGETED_PRACTICE.id || gameMode == "practice"
            if (!isPracticeMode && antiCheat.isEligibleForLeaderboard && validScore > 0) {
                leaderboardService.submitGameScore(
                    gameId = gameId,
                    profile = updatedProfile,
                    score = validScore,
                    xpEarned = totalEarnedXp,
                    accuracy = accuracy,
                    durationSeconds = timeSpentSeconds,
                    category = categoryTitle,
                    gameMode = gameMode
                )
            }

            syncService?.syncNow()
        }

        if (didLevelUp) {
            _notifications.update { notifs ->
                listOf(
                    AppNotification(
                        id = "lvl_${newLevelInfo.level}_${System.currentTimeMillis()}",
                        title = "🎉 Level Up! You reached Level ${newLevelInfo.level}!",
                        message = "Next milestone: ${newLevelInfo.xpRequiredForNextLevel} XP",
                        timeAgo = "Just now",
                        iconEmoji = "⭐",
                        type = NotificationType.ACHIEVEMENT
                    )
                ) + notifs
            }
        }

        val result = GameResult(
            score = validScore,
            correctCount = correctCount,
            totalQuestions = totalQuestions,
            accuracyPercentage = accuracy,
            timeSpentSeconds = timeSpentSeconds,
            xpEarned = totalEarnedXp,
            currentStreakDays = newStreak,
            categoryTitle = categoryTitle,
            isNewPersonalBest = isPersonalBest,
            bestAnswerStreak = bestStreak,
            gameMode = gameMode,
            previousPersonalBest = if (pbEval.isOverallBest) pbEval.previousOverallBest else pbEval.previousModeBest,
            newlyUnlockedAchievements = newlyUnlocked,
            didLevelUp = didLevelUp,
            newLevel = newLevelInfo.level,
            newTitle = newlyUnlockedTitle
        )
        _lastGameResult.value = result

        // Record into local UI history
        val historyEntry = GameHistoryItem(
            id = gameId,
            date = "Just now",
            gameMode = gameMode,
            category = categoryTitle,
            difficulty = "Active",
            score = validScore,
            accuracy = accuracy,
            xp = totalEarnedXp,
            durationSeconds = timeSpentSeconds,
            correctAnswers = correctCount,
            incorrectAnswers = (totalQuestions - correctCount).coerceAtLeast(0),
            bestStreak = bestStreak
        )
        _gameHistory.update { listOf(historyEntry) + it }
        if (updatedProfile.uid.isNotBlank()) {
            localRepo?.saveGameHistory(_gameHistory.value, updatedProfile.uid)
        }
        refreshPerformanceAnalytics()

        return result
    }

    fun refreshPerformanceAnalytics() {
        val profile = _userProfile.value
        val history = _gameHistory.value
        val perf = playerPerformanceAnalytics.analyzePerformance(profile, history)
        _performanceProfile.value = perf
        _personalizedRecommendation.value = playerPerformanceAnalytics.getPersonalizedRecommendation(perf, profile.gamesPlayed)
    }

    fun addFriendChallenge(code: String, targetScore: Int, category: String) {
        val newChallenge = FriendChallenge(
            id = "c_${System.currentTimeMillis()}",
            code = code,
            challengerName = _userProfile.value.username,
            targetScore = targetScore,
            category = category,
            status = "Waiting for Friend",
            createdAt = "Just now"
        )
        _friendChallenges.update { listOf(newChallenge) + it }
    }

    fun markNotificationsRead() {
        _notifications.update { list ->
            list.map { it.copy(isRead = true) }
        }
    }

    fun markNotificationAsRead(id: String) {
        _notifications.update { list ->
            list.map { if (it.id == id) it.copy(isRead = true) else it }
        }
    }

    fun awardXp(xpBonus: Int) {
        val current = _userProfile.value
        val newXp = current.totalXp + xpBonus
        val levelInfo = GameScoringConfig.calculateLevelInfo(newXp)
        val didLevelUp = levelInfo.level > current.level
        val updated = current.copy(
            totalXp = newXp,
            level = levelInfo.level,
            currentXp = levelInfo.currentXpInLevel,
            nextLevelXp = levelInfo.xpRequiredForNextLevel
        )
        _userProfile.value = updated
        localRepo?.saveProfile(updated)
        if (didLevelUp) {
            _notifications.update { notifs ->
                listOf(
                    AppNotification(
                        id = "lvl_${levelInfo.level}_${System.currentTimeMillis()}",
                        title = "🎉 Level Up! You reached Level ${levelInfo.level}!",
                        message = "Next milestone: ${levelInfo.xpRequiredForNextLevel} XP",
                        timeAgo = "Just now",
                        iconEmoji = "⭐",
                        type = NotificationType.ACHIEVEMENT
                    )
                ) + notifs
            }
        }
    }

    fun unlockAchievementById(achId: String) {
        if (achId in _unlockedAchievementIds.value) return
        val currentSet = _unlockedAchievementIds.value + achId
        _unlockedAchievementIds.value = currentSet
        localRepo?.saveUnlockedAchievementIds(currentSet, _userProfile.value.uid)
        val def = AchievementEngine.STARTER_ACHIEVEMENT_DEFINITIONS.firstOrNull { it.id == achId }
        if (def != null) {
            awardXp(def.xpBonus)
            _notifications.update { notifs ->
                listOf(
                    AppNotification(
                        id = "ach_${achId}_${System.currentTimeMillis()}",
                        title = "🏆 Achievement Unlocked: ${def.name}!",
                        message = "${def.description} (+${def.xpBonus} XP)",
                        timeAgo = "Just now",
                        iconEmoji = def.iconEmoji,
                        type = NotificationType.ACHIEVEMENT
                    )
                ) + notifs
            }
        }
    }

    fun getQuestionsFor(category: GameCategory, difficulty: Difficulty): List<Question> {
        return QuestionSelectionService.getQuestionsForCategory(
            category = category,
            difficulty = difficulty,
            count = 10,
            pool = questionAdminService.getAllApprovedPool()
        )
    }

    fun getPracticeQuestions(category: GameCategory, difficulty: Difficulty, count: Int = 5): List<Question> {
        return QuestionSelectionService.getPracticeQuestions(
            category = category,
            difficulty = difficulty,
            count = count,
            pool = questionAdminService.getAllApprovedPool()
        )
    }

    fun getDailyQuestions(): List<Question> {
        return QuestionSelectionService.getDailyChallengeQuestions()
    }

    fun getQuickBattleQuestions(category: GameCategory? = null, difficulty: Difficulty? = null): List<Question> {
        return QuestionSelectionService.getQuickBattleQuestions(
            category = category,
            difficulty = difficulty,
            count = 10,
            pool = questionAdminService.getAllApprovedPool()
        )
    }

    fun getRushQuestions(): List<Question> {
        return QuestionSelectionService.getRushQuestions(30, questionAdminService.getAllApprovedPool())
    }

    fun getEndlessQuestions(): List<Question> {
        return QuestionSelectionService.getEndlessPool(40, questionAdminService.getAllApprovedPool())
    }

    fun reportQuestion(report: QuestionReport) {
        questionAdminService.reportQuestion(report)
    }

    suspend fun getExplanation(question: Question, selectedAnswer: String): String {
        return aiQuestionService.generateExplanation(question, selectedAnswer)
    }

    fun getAchievements(): List<Achievement> {
        val user = _userProfile.value
        val categoryCounts = _performanceProfile.value.categoryStats.mapValues { it.value.questionsAnswered }
        return achievementEngine.computeAchievements(
            profile = user,
            unlockedAchievementIds = _unlockedAchievementIds.value,
            categoryCorrectCounts = categoryCounts
        )
    }

    fun getPersonalBests(): PersonalBestRecord {
        return personalBestService.getRecord()
    }

    fun getAvailableTitles(): List<PlayerTitle> {
        return titleManager.getAvailableTitles(_userProfile.value, _unlockedAchievementIds.value)
    }

    fun equipTitle(titleName: String): Boolean {
        if (titleManager.canEquipTitle(titleName, _userProfile.value, _unlockedAchievementIds.value)) {
            val updated = _userProfile.value.copy(
                selectedTitle = titleName,
                updatedAt = System.currentTimeMillis()
            )
            _userProfile.value = updated
            localRepo?.saveProfile(updated)
            coroutineScope.launch {
                firestoreRepo?.updateProfileFields(updated.uid, mapOf("selectedTitle" to titleName))
            }
            return true
        }
        return false
    }

    suspend fun getCompetitiveLeaderboard(
        scope: LeaderboardScope,
        period: LeaderboardPeriod,
        categoryFilter: GameCategory? = null,
        modeFilter: GameModeType? = null,
        forceRefresh: Boolean = false
    ): LeaderboardQueryResult {
        if (scope == LeaderboardScope.FRIENDS) {
            return socialRepository.getFriendsLeaderboardEntries(_userProfile.value)
        }
        return leaderboardService.getLeaderboard(
            scope = scope,
            period = period,
            categoryFilter = categoryFilter,
            modeFilter = modeFilter,
            currentUser = _userProfile.value,
            forceRefresh = forceRefresh
        )
    }

    fun getTournaments(): List<Tournament> {
        return listOf(
            Tournament(
                id = "tourney_grand_1",
                name = "Weekly Grand Brain Championship",
                participantCount = 1,
                startTime = "Today, 10:00 UTC",
                endTime = "Ends in 2 days 6h",
                entryRequirement = "Free (Level 3+)",
                prizeRewardInfo = "Grand Trophy + 5,000 XP + Master Badge",
                rules = "10 rounds of mixed Rapid questions. Highest cumulative score wins.",
                status = "ACTIVE",
                topLeaderboard = if (_userProfile.value.bestScore > 0) listOf(
                    LeaderboardEntry(
                        rank = 1,
                        username = _userProfile.value.username,
                        avatarEmoji = _userProfile.value.avatarEmoji,
                        score = _userProfile.value.bestScore,
                        level = _userProfile.value.level,
                        countryFlag = _userProfile.value.countryFlag,
                        isCurrentUser = true,
                        playerTitle = _userProfile.value.selectedTitle
                    )
                ) else emptyList()
            ),
            Tournament(
                id = "tourney_math_rush",
                name = "Math Rush Weekend Sprint",
                participantCount = 0,
                startTime = "Friday, 18:00 UTC",
                endTime = "Starts in 1 day",
                entryRequirement = "Open to All",
                prizeRewardInfo = "Gold Math Badge + 2,500 XP",
                rules = "Rapid arithmetic under 45-second timers. One retry allowed.",
                status = "UPCOMING",
                topLeaderboard = emptyList()
            )
        )
    }

    private fun createInitialFriendChallenges(): List<FriendChallenge> {
        return emptyList()
    }

    private fun createInitialNotifications(): List<AppNotification> {
        return listOf(
            AppNotification("n1", "🔥 Welcome to Brain Battle!", "Begin your competitive progression journey. Complete games to earn XP, level up, and unlock titles.", "Just now", "🧠", NotificationType.DAILY_CHALLENGE)
        )
    }

    fun getLeaderboard(tab: LeaderboardTab): List<LeaderboardEntry> {
        val user = _userProfile.value
        if (user.bestScore <= 0) return emptyList()

        return when (tab) {
            LeaderboardTab.GLOBAL, LeaderboardTab.COUNTRY -> listOf(
                LeaderboardEntry(
                    rank = 1,
                    username = user.username,
                    avatarEmoji = user.avatarEmoji,
                    score = user.bestScore,
                    level = user.level,
                    countryFlag = user.countryFlag,
                    isCurrentUser = true,
                    userId = user.uid,
                    xp = user.totalXp,
                    country = user.country,
                    playerTitle = user.selectedTitle
                )
            )
            LeaderboardTab.FRIENDS -> {
                socialRepository.getFriendsLeaderboardEntries(user).entries
            }
        }
    }
}
