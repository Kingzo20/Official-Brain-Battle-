package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.data.GameScoringConfig
import com.example.data.competitive.AchievementEngine
import com.example.model.CloudGameHistoryRecord
import com.example.model.GameHistoryItem
import com.example.model.Question
import com.example.model.QuestionReport
import com.example.model.UserProfile
import com.example.model.UserSettings
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class LocalStorageRepository(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("brain_battle_local_store", Context.MODE_PRIVATE)

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val historyListType = Types.newParameterizedType(List::class.java, CloudGameHistoryRecord::class.java)
    private val historyAdapter = moshi.adapter<List<CloudGameHistoryRecord>>(historyListType)

    private val gameHistoryListType = Types.newParameterizedType(List::class.java, GameHistoryItem::class.java)
    private val gameHistoryAdapter = moshi.adapter<List<GameHistoryItem>>(gameHistoryListType)

    private val questionsListType = Types.newParameterizedType(List::class.java, Question::class.java)
    private val questionsAdapter = moshi.adapter<List<Question>>(questionsListType)

    private val reportsListType = Types.newParameterizedType(List::class.java, QuestionReport::class.java)
    private val reportsAdapter = moshi.adapter<List<QuestionReport>>(reportsListType)

    fun getActiveSessionUid(): String? {
        val uid = prefs.getString("active_session_uid", null) ?: prefs.getString("user_uid", null)
        return if (uid.isNullOrBlank()) null else uid
    }

    fun setActiveSessionUid(uid: String?) {
        if (uid.isNullOrBlank()) {
            prefs.edit().remove("active_session_uid").remove("user_uid").commit()
        } else {
            prefs.edit().putString("active_session_uid", uid).putString("user_uid", uid).commit()
        }
    }

    private fun prefixFor(uid: String): String = if (uid.isBlank()) "user_" else "user_${uid}_"

    fun loadProfile(explicitUid: String? = null): UserProfile {
        val targetUid = explicitUid ?: getActiveSessionUid() ?: ""
        if (targetUid.isBlank() || targetUid == "guest") {
            // Clean unauthenticated / guest profile
            return UserProfile(
                uid = "",
                playerId = "",
                username = "Guest",
                displayName = "Guest",
                email = "",
                avatarEmoji = "🧠",
                level = 1,
                currentXp = 0,
                nextLevelXp = 100,
                gamesPlayed = 0,
                totalScore = 0,
                bestScore = 0,
                currentStreak = 0,
                bestStreak = 0,
                totalXp = 0,
                accuracyPercentage = 0,
                achievementsUnlocked = 0,
                totalAchievements = 20,
                isPremium = false,
                country = "Global",
                countryFlag = "🌐",
                language = "en",
                selectedTitle = "Rookie",
                unlockedTitles = listOf("Rookie"),
                challengesPlayed = 0,
                challengesWon = 0,
                challengesLost = 0,
                challengesDraw = 0,
                emailVerified = false,
                provider = "password"
            )
        }

        val prefix = prefixFor(targetUid)

        // Check if profile exists for this uid
        val hasData = prefs.contains("${prefix}username") || prefs.contains("${prefix}total_xp")
        if (!hasData && prefs.contains("user_username") && prefs.getString("user_uid", "") == targetUid) {
            // Migrate from legacy single-user store if matching uid
            migrateLegacyProfileToUid(targetUid)
        }

        val username = prefs.getString("${prefix}username", "Player") ?: "Player"
        val displayName = prefs.getString("${prefix}display_name", username) ?: username
        val email = prefs.getString("${prefix}email", "") ?: ""
        val avatar = prefs.getString("${prefix}avatar", "🧠") ?: "🧠"
        val totalXp = prefs.getInt("${prefix}total_xp", 0)
        val gamesPlayed = prefs.getInt("${prefix}games_played", 0)
        val totalScore = prefs.getInt("${prefix}total_score", 0)
        val bestScore = prefs.getInt("${prefix}best_score", 0)
        val currentStreak = prefs.getInt("${prefix}current_streak", 0)
        val bestStreak = prefs.getInt("${prefix}best_streak", 0)
        val lastDaily = prefs.getString("${prefix}last_daily", null)
        val country = prefs.getString("${prefix}country", "Global") ?: "Global"
        val countryFlag = prefs.getString("${prefix}country_flag", "🌐") ?: "🌐"
        val language = prefs.getString("${prefix}language", "en") ?: "en"

        val levelInfo = GameScoringConfig.calculateLevelInfo(totalXp)

        val selectedTitle = prefs.getString("${prefix}selected_title", "Rookie") ?: "Rookie"
        val unlockedTitlesSet = prefs.getStringSet("${prefix}unlocked_titles", setOf("Rookie")) ?: setOf("Rookie")

        // Player ID generated ONCE per user
        var playerId = prefs.getString("${prefix}player_id", "") ?: ""
        if (playerId.isBlank()) {
            val randomHex = (100000..999999).random()
            playerId = "BB-$randomHex"
            prefs.edit().putString("${prefix}player_id", playerId).apply()
        }

        val emailVerified = prefs.getBoolean("${prefix}email_verified", false)
        val provider = prefs.getString("${prefix}provider", "password") ?: "password"
        val photoUrl = prefs.getString("${prefix}photo_url", null)
        val lastLoginAt = prefs.getLong("${prefix}last_login_at", System.currentTimeMillis())
        val createdAt = prefs.getLong("${prefix}created_at", System.currentTimeMillis())

        return UserProfile(
            uid = targetUid,
            playerId = playerId,
            username = username,
            displayName = displayName,
            email = email,
            photoUrl = photoUrl,
            avatarEmoji = avatar,
            level = levelInfo.level,
            currentXp = levelInfo.currentXpInLevel,
            nextLevelXp = levelInfo.xpRequiredForNextLevel,
            gamesPlayed = gamesPlayed,
            totalScore = totalScore,
            bestScore = bestScore,
            currentStreak = currentStreak,
            bestStreak = bestStreak,
            totalXp = totalXp,
            accuracyPercentage = prefs.getInt("${prefix}accuracy", 0),
            achievementsUnlocked = prefs.getInt("${prefix}achievements", 0),
            totalAchievements = 20,
            isPremium = prefs.getBoolean("${prefix}is_premium", false),
            lastDailyCompletedDate = lastDaily,
            country = country,
            countryFlag = countryFlag,
            language = language,
            selectedTitle = selectedTitle,
            unlockedTitles = unlockedTitlesSet.toList(),
            challengesPlayed = prefs.getInt("${prefix}challenges_played", 0),
            challengesWon = prefs.getInt("${prefix}challenges_won", 0),
            challengesLost = prefs.getInt("${prefix}challenges_lost", 0),
            challengesDraw = prefs.getInt("${prefix}challenges_draw", 0),
            emailVerified = emailVerified,
            provider = provider,
            role = prefs.getString("${prefix}role", "CUSTOMER / PIONEER") ?: "CUSTOMER / PIONEER",
            lastLoginAt = lastLoginAt,
            createdAt = createdAt
        )
    }

    private fun migrateLegacyProfileToUid(uid: String) {
        val prefix = prefixFor(uid)
        prefs.edit().apply {
            putString("${prefix}username", prefs.getString("user_username", "Player"))
            putString("${prefix}display_name", prefs.getString("user_display_name", "Player"))
            putString("${prefix}email", prefs.getString("user_email", ""))
            putString("${prefix}avatar", prefs.getString("user_avatar", "🧠"))
            putInt("${prefix}total_xp", prefs.getInt("user_total_xp", 0))
            putInt("${prefix}games_played", prefs.getInt("user_games_played", 0))
            putInt("${prefix}total_score", prefs.getInt("user_total_score", 0))
            putInt("${prefix}best_score", prefs.getInt("user_best_score", 0))
            putInt("${prefix}current_streak", prefs.getInt("user_current_streak", 0))
            putInt("${prefix}best_streak", prefs.getInt("user_best_streak", 0))
            putString("${prefix}player_id", prefs.getString("user_player_id", ""))
            apply()
        }
    }

    fun saveProfile(profile: UserProfile) {
        val uid = profile.uid
        val prefix = prefixFor(uid)

        prefs.edit().apply {
            if (uid.isNotBlank()) {
                putString("active_session_uid", uid)
            }
            putString("${prefix}uid", profile.uid)
            putString("${prefix}player_id", profile.playerId)
            putString("${prefix}username", profile.username)
            putString("${prefix}display_name", profile.displayName)
            putString("${prefix}email", profile.email)
            putString("${prefix}avatar", profile.avatarEmoji)
            putInt("${prefix}total_xp", profile.totalXp)
            putInt("${prefix}games_played", profile.gamesPlayed)
            putInt("${prefix}total_score", profile.totalScore)
            putInt("${prefix}best_score", profile.bestScore)
            putInt("${prefix}current_streak", profile.currentStreak)
            putInt("${prefix}best_streak", profile.bestStreak)
            putInt("${prefix}accuracy", profile.accuracyPercentage)
            putString("${prefix}last_daily", profile.lastDailyCompletedDate)
            putString("${prefix}country", profile.country)
            putString("${prefix}country_flag", profile.countryFlag)
            putString("${prefix}language", profile.language)
            putString("${prefix}selected_title", profile.selectedTitle)
            putStringSet("${prefix}unlocked_titles", profile.unlockedTitles.toSet())
            putInt("${prefix}challenges_played", profile.challengesPlayed)
            putInt("${prefix}challenges_won", profile.challengesWon)
            putInt("${prefix}challenges_lost", profile.challengesLost)
            putInt("${prefix}challenges_draw", profile.challengesDraw)
            putBoolean("${prefix}email_verified", profile.emailVerified)
            putString("${prefix}provider", profile.provider)
            putString("${prefix}photo_url", profile.photoUrl)
            putString("${prefix}role", profile.role)
            putLong("${prefix}last_login_at", profile.lastLoginAt)
            if (profile.createdAt > 0) {
                putLong("${prefix}created_at", profile.createdAt)
            }
            commit()
        }
    }

    fun loadUnlockedAchievementIds(uid: String? = null): Set<String> {
        val targetUid = uid ?: getActiveSessionUid() ?: ""
        val prefix = prefixFor(targetUid)
        return prefs.getStringSet("${prefix}unlocked_achievement_ids", setOf(AchievementEngine.ID_FIRST_BATTLE))
            ?: setOf(AchievementEngine.ID_FIRST_BATTLE)
    }

    fun saveUnlockedAchievementIds(ids: Set<String>, uid: String? = null) {
        val targetUid = uid ?: getActiveSessionUid() ?: ""
        val prefix = prefixFor(targetUid)
        prefs.edit().putStringSet("${prefix}unlocked_achievement_ids", ids).apply()
    }

    fun loadGameHistory(uid: String? = null): List<GameHistoryItem> {
        val targetUid = uid ?: getActiveSessionUid() ?: ""
        val prefix = prefixFor(targetUid)
        val json = prefs.getString("${prefix}game_history", null) ?: return emptyList()
        return try {
            gameHistoryAdapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveGameHistory(history: List<GameHistoryItem>, uid: String? = null) {
        val targetUid = uid ?: getActiveSessionUid() ?: ""
        val prefix = prefixFor(targetUid)
        try {
            prefs.edit().putString("${prefix}game_history", gameHistoryAdapter.toJson(history)).apply()
        } catch (e: Exception) {
            // Ignore
        }
    }

    /**
     * Terminates the active session without deleting the user's data!
     * The stored profile, stats, XP, level, and achievements for this UID are permanently retained.
     */
    fun clearActiveSessionData() {
        prefs.edit().apply {
            remove("active_session_uid")
            remove("user_uid")
            commit()
        }
    }

    fun hasProfileData(uid: String): Boolean {
        if (uid.isBlank()) return false
        val prefix = prefixFor(uid)
        return prefs.contains("${prefix}username") || prefs.contains("${prefix}email")
    }

    fun deleteProfile(uid: String) {
        if (uid.isBlank()) return
        val prefix = prefixFor(uid)
        val editor = prefs.edit()
        prefs.all.keys.filter { it.startsWith(prefix) }.forEach { key ->
            editor.remove(key)
        }
        editor.commit()
    }

    fun hasCompletedOnboarding(): Boolean {
        return prefs.getBoolean("has_completed_onboarding", false)
    }

    fun setOnboardingCompleted(completed: Boolean) {
        prefs.edit().putBoolean("has_completed_onboarding", completed).commit()
    }

    fun loadSettings(): UserSettings {
        return UserSettings(
            soundEffects = prefs.getBoolean("settings_sound", true),
            music = prefs.getBoolean("settings_music", true),
            vibration = prefs.getBoolean("settings_vibrate", true),
            timerSpeedNormal = prefs.getBoolean("settings_timer_speed", true),
            darkTheme = prefs.getBoolean("settings_dark", true),
            animationsEnabled = prefs.getBoolean("settings_animations", true),
            dailyReminder = prefs.getBoolean("settings_daily_reminder", true),
            streakReminder = prefs.getBoolean("settings_streak_reminder", true),
            achievementAlerts = prefs.getBoolean("settings_achievement_alerts", true),
            updatedAt = prefs.getLong("settings_updated_at", 0L)
        )
    }

    fun saveSettings(settings: UserSettings) {
        prefs.edit().apply {
            putBoolean("settings_sound", settings.soundEffects)
            putBoolean("settings_music", settings.music)
            putBoolean("settings_vibrate", settings.vibration)
            putBoolean("settings_timer_speed", settings.timerSpeedNormal)
            putBoolean("settings_dark", settings.darkTheme)
            putBoolean("settings_animations", settings.animationsEnabled)
            putBoolean("settings_daily_reminder", settings.dailyReminder)
            putBoolean("settings_streak_reminder", settings.streakReminder)
            putBoolean("settings_achievement_alerts", settings.achievementAlerts)
            putLong("settings_updated_at", System.currentTimeMillis())
            apply()
        }
    }

    // Pending Offline Sync Queue
    fun getPendingSyncQueue(): List<CloudGameHistoryRecord> {
        val json = prefs.getString("pending_sync_queue", null) ?: return emptyList()
        return try {
            historyAdapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun addPendingSync(record: CloudGameHistoryRecord) {
        val current = getPendingSyncQueue().toMutableList()
        if (current.none { it.gameId == record.gameId }) {
            current.add(record)
            try {
                prefs.edit().putString("pending_sync_queue", historyAdapter.toJson(current)).apply()
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    fun removePendingSync(gameIds: Set<String>) {
        val current = getPendingSyncQueue().filterNot { it.gameId in gameIds }
        try {
            prefs.edit().putString("pending_sync_queue", historyAdapter.toJson(current)).apply()
        } catch (e: Exception) {
            // Ignore
        }
    }

    // AI Approved Questions Cache
    fun loadApprovedQuestions(): List<Question> {
        val json = prefs.getString("cached_approved_questions", null) ?: return emptyList()
        return try {
            questionsAdapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveApprovedQuestions(questions: List<Question>) {
        try {
            prefs.edit().putString("cached_approved_questions", questionsAdapter.toJson(questions)).apply()
        } catch (e: Exception) {
            // Ignore
        }
    }

    // Question Reports
    fun loadQuestionReports(): List<QuestionReport> {
        val json = prefs.getString("cached_question_reports", null) ?: return emptyList()
        return try {
            reportsAdapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveQuestionReports(reports: List<QuestionReport>) {
        try {
            prefs.edit().putString("cached_question_reports", reportsAdapter.toJson(reports)).apply()
        } catch (e: Exception) {
            // Ignore
        }
    }

    // Disabled Question IDs
    fun loadDisabledQuestionIds(): Set<String> {
        return prefs.getStringSet("disabled_question_ids", emptySet()) ?: emptySet()
    }

    fun saveDisabledQuestionIds(ids: Set<String>) {
        prefs.edit().putStringSet("disabled_question_ids", ids).apply()
    }
}
