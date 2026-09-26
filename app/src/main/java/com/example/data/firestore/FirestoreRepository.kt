package com.example.data.firestore

import android.content.Context
import android.util.Log
import com.example.model.CloudGameHistoryRecord
import com.example.model.LeaderboardEntry
import com.example.model.UserProfile
import com.example.model.UserSettings
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FirestoreRepository(
    private val context: Context
) {
    private val tag = "FirestoreRepository"

    private val firestore: FirebaseFirestore? = try {
        if (FirebaseApp.getApps(context).isNotEmpty() || FirebaseApp.initializeApp(context) != null) {
            FirebaseFirestore.getInstance()
        } else {
            null
        }
    } catch (e: Exception) {
        Log.w(tag, "Firestore not initialized: ${e.message}")
        null
    }

    val isAvailable: Boolean get() = firestore != null

    suspend fun getUserProfile(uid: String): UserProfile? = withContext(Dispatchers.IO) {
        if (firestore == null || uid.isBlank()) return@withContext null
        try {
            val doc = firestore.collection("users").document(uid).get().await()
            if (!doc.exists()) return@withContext null

            val settingsMap = doc.get("settings") as? Map<*, *>
            val statsMap = doc.get("statistics") as? Map<*, *>

            val totalXp = (doc.getLong("xp") ?: doc.getLong("totalXp") ?: 0L).toInt()
            val gamesPlayed = (doc.getLong("totalGames") ?: doc.getLong("gamesPlayed") ?: 0L).toInt()
            val totalScore = (doc.getLong("totalScore") ?: 0L).toInt()
            val bestScore = (doc.getLong("bestScore") ?: 0L).toInt()
            val currentStreak = (doc.getLong("currentStreak") ?: 0L).toInt()
            val bestStreak = (doc.getLong("bestStreak") ?: 0L).toInt()
            val level = (doc.getLong("level") ?: 1L).toInt()

            UserProfile(
                uid = uid,
                username = doc.getString("username") ?: "Player",
                displayName = doc.getString("displayName") ?: (doc.getString("username") ?: "Player"),
                email = doc.getString("email") ?: "",
                photoUrl = doc.getString("photoUrl"),
                avatarEmoji = doc.getString("avatarEmoji") ?: "🧠",
                level = level,
                totalXp = totalXp,
                gamesPlayed = gamesPlayed,
                totalScore = totalScore,
                bestScore = bestScore,
                currentStreak = currentStreak,
                bestStreak = bestStreak,
                lastDailyCompletedDate = doc.getString("lastDailyChallengeDate"),
                dailyChallengeCompleted = doc.getBoolean("dailyChallengeCompleted") ?: false,
                createdAt = doc.getLong("createdAt") ?: 0L,
                updatedAt = doc.getLong("updatedAt") ?: 0L,
                lastActiveAt = doc.getLong("lastActiveAt") ?: 0L,
                country = doc.getString("country") ?: "Global",
                countryFlag = doc.getString("countryFlag") ?: "🌐",
                language = doc.getString("language") ?: "en",
                accuracyPercentage = (statsMap?.get("overallAccuracy") as? Long)?.toInt() ?: 0,
                totalQuestionsAnswered = (statsMap?.get("totalQuestionsAnswered") as? Long)?.toInt() ?: 0,
                correctAnswers = (statsMap?.get("correctAnswers") as? Long)?.toInt() ?: 0,
                incorrectAnswers = (statsMap?.get("incorrectAnswers") as? Long)?.toInt() ?: 0,
                timePlayedSeconds = (statsMap?.get("timePlayedSeconds") as? Long)?.toInt() ?: 0,
                playerId = doc.getString("playerId") ?: "",
                emailVerified = doc.getBoolean("emailVerified") ?: false,
                provider = doc.getString("provider") ?: "password",
                lastLoginAt = doc.getLong("lastLoginAt") ?: 0L
            )
        } catch (e: Exception) {
            Log.e(tag, "Error fetching profile for $uid", e)
            null
        }
    }

    suspend fun saveUserProfile(profile: UserProfile): Boolean = withContext(Dispatchers.IO) {
        if (firestore == null || profile.uid.isBlank()) return@withContext false
        try {
            val data = hashMapOf(
                "uid" to profile.uid,
                "username" to profile.username,
                "displayName" to profile.displayName,
                "email" to profile.email,
                "photoUrl" to profile.photoUrl,
                "avatarEmoji" to profile.avatarEmoji,
                "level" to profile.level,
                "xp" to profile.totalXp,
                "totalGames" to profile.gamesPlayed,
                "totalScore" to profile.totalScore,
                "bestScore" to profile.bestScore,
                "currentStreak" to profile.currentStreak,
                "bestStreak" to profile.bestStreak,
                "lastDailyChallengeDate" to profile.lastDailyCompletedDate,
                "dailyChallengeCompleted" to profile.dailyChallengeCompleted,
                "createdAt" to if (profile.createdAt > 0) profile.createdAt else System.currentTimeMillis(),
                "updatedAt" to System.currentTimeMillis(),
                "lastActiveAt" to System.currentTimeMillis(),
                "country" to profile.country,
                "countryFlag" to profile.countryFlag,
                "language" to profile.language,
                "playerId" to profile.playerId,
                "emailVerified" to profile.emailVerified,
                "provider" to profile.provider,
                "lastLoginAt" to if (profile.lastLoginAt > 0) profile.lastLoginAt else System.currentTimeMillis(),
                "statistics" to hashMapOf(
                    "totalGames" to profile.gamesPlayed,
                    "completedGames" to profile.gamesPlayed,
                    "totalQuestionsAnswered" to profile.totalQuestionsAnswered,
                    "correctAnswers" to profile.correctAnswers,
                    "incorrectAnswers" to profile.incorrectAnswers,
                    "overallAccuracy" to profile.accuracyPercentage,
                    "totalXp" to profile.totalXp,
                    "totalScore" to profile.totalScore,
                    "bestScore" to profile.bestScore,
                    "bestAnswerStreak" to profile.bestStreak,
                    "currentDailyStreak" to profile.currentStreak,
                    "longestDailyStreak" to profile.bestStreak,
                    "timePlayedSeconds" to profile.timePlayedSeconds
                )
            )

            firestore.collection("users").document(profile.uid)
                .set(data, SetOptions.merge())
                .await()
            true
        } catch (e: Exception) {
            Log.e(tag, "Error saving profile for ${profile.uid}", e)
            false
        }
    }

    suspend fun updateProfileFields(uid: String, fields: Map<String, Any>): Boolean = withContext(Dispatchers.IO) {
        if (firestore == null || uid.isBlank()) return@withContext false
        try {
            val updated = fields.toMutableMap()
            updated["updatedAt"] = System.currentTimeMillis()
            firestore.collection("users").document(uid)
                .set(updated, SetOptions.merge())
                .await()
            true
        } catch (e: Exception) {
            Log.e(tag, "Error updating profile fields for $uid", e)
            false
        }
    }

    suspend fun saveGameHistory(uid: String, record: CloudGameHistoryRecord): Boolean = withContext(Dispatchers.IO) {
        if (firestore == null || uid.isBlank()) return@withContext false
        try {
            val docId = if (record.gameId.isNotBlank()) record.gameId else "game_${System.currentTimeMillis()}"
            val data = hashMapOf(
                "gameId" to docId,
                "mode" to record.mode,
                "category" to record.category,
                "difficulty" to record.difficulty,
                "score" to record.score,
                "xpEarned" to record.xpEarned,
                "correctAnswers" to record.correctAnswers,
                "incorrectAnswers" to record.incorrectAnswers,
                "totalQuestions" to record.totalQuestions,
                "accuracy" to record.accuracy,
                "duration" to record.duration,
                "bestAnswerStreak" to record.bestAnswerStreak,
                "completedAt" to if (record.completedAt > 0) record.completedAt else System.currentTimeMillis(),
                "isVerified" to record.isVerified
            )
            firestore.collection("users")
                .document(uid)
                .collection("gameHistory")
                .document(docId)
                .set(data, SetOptions.merge())
                .await()
            true
        } catch (e: Exception) {
            Log.e(tag, "Error saving game history for $uid", e)
            false
        }
    }

    suspend fun getGameHistory(uid: String, limit: Int = 20): List<CloudGameHistoryRecord> = withContext(Dispatchers.IO) {
        if (firestore == null || uid.isBlank()) return@withContext emptyList()
        try {
            val snapshot = firestore.collection("users")
                .document(uid)
                .collection("gameHistory")
                .orderBy("completedAt", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                CloudGameHistoryRecord(
                    gameId = doc.getString("gameId") ?: doc.id,
                    mode = doc.getString("mode") ?: "",
                    category = doc.getString("category") ?: "",
                    difficulty = doc.getString("difficulty") ?: "",
                    score = (doc.getLong("score") ?: 0L).toInt(),
                    xpEarned = (doc.getLong("xpEarned") ?: 0L).toInt(),
                    correctAnswers = (doc.getLong("correctAnswers") ?: 0L).toInt(),
                    incorrectAnswers = (doc.getLong("incorrectAnswers") ?: 0L).toInt(),
                    totalQuestions = (doc.getLong("totalQuestions") ?: 0L).toInt(),
                    accuracy = (doc.getLong("accuracy") ?: 0L).toInt(),
                    duration = (doc.getLong("duration") ?: 0L).toInt(),
                    bestAnswerStreak = (doc.getLong("bestAnswerStreak") ?: 0L).toInt(),
                    completedAt = doc.getLong("completedAt") ?: 0L,
                    isVerified = doc.getBoolean("isVerified") ?: false
                )
            }
        } catch (e: Exception) {
            Log.e(tag, "Error getting game history for $uid", e)
            emptyList()
        }
    }

    suspend fun saveSettings(uid: String, settings: UserSettings): Boolean = withContext(Dispatchers.IO) {
        if (firestore == null || uid.isBlank()) return@withContext false
        try {
            val data = hashMapOf(
                "settings" to hashMapOf(
                    "soundEffects" to settings.soundEffects,
                    "music" to settings.music,
                    "vibration" to settings.vibration,
                    "timerSpeedNormal" to settings.timerSpeedNormal,
                    "darkTheme" to settings.darkTheme,
                    "animationsEnabled" to settings.animationsEnabled,
                    "dailyReminder" to settings.dailyReminder,
                    "streakReminder" to settings.streakReminder,
                    "achievementAlerts" to settings.achievementAlerts,
                    "updatedAt" to System.currentTimeMillis()
                ),
                "updatedAt" to System.currentTimeMillis()
            )
            firestore.collection("users").document(uid)
                .set(data, SetOptions.merge())
                .await()
            true
        } catch (e: Exception) {
            Log.e(tag, "Error saving settings for $uid", e)
            false
        }
    }

    suspend fun recordDailyChallenge(uid: String, dateId: String, score: Int, xp: Int): Boolean = withContext(Dispatchers.IO) {
        if (firestore == null || uid.isBlank()) return@withContext false
        try {
            val data = hashMapOf(
                "dateId" to dateId,
                "score" to score,
                "xpEarned" to xp,
                "completed" to true,
                "completedAt" to System.currentTimeMillis()
            )
            firestore.collection("users")
                .document(uid)
                .collection("dailyChallenges")
                .document(dateId)
                .set(data, SetOptions.merge())
                .await()

            firestore.collection("users")
                .document(uid)
                .set(
                    hashMapOf(
                        "lastDailyChallengeDate" to dateId,
                        "dailyChallengeCompleted" to true,
                        "updatedAt" to System.currentTimeMillis()
                    ),
                    SetOptions.merge()
                )
                .await()
            true
        } catch (e: Exception) {
            Log.e(tag, "Error recording daily challenge for $uid", e)
            false
        }
    }

    suspend fun unlockAchievement(uid: String, achievementId: String, name: String, xpBonus: Int): Boolean = withContext(Dispatchers.IO) {
        if (firestore == null || uid.isBlank()) return@withContext false
        try {
            val data = hashMapOf(
                "id" to achievementId,
                "name" to name,
                "xpBonus" to xpBonus,
                "isUnlocked" to true,
                "unlockedAt" to System.currentTimeMillis()
            )
            firestore.collection("users")
                .document(uid)
                .collection("achievements")
                .document(achievementId)
                .set(data, SetOptions.merge())
                .await()
            true
        } catch (e: Exception) {
            Log.e(tag, "Error unlocking achievement for $uid", e)
            false
        }
    }
}
