package com.example.data.competitive

import android.content.Context
import android.util.Log
import com.example.data.firestore.FirestoreRepository
import com.example.model.GameCategory
import com.example.model.GameModeType
import com.example.model.LeaderboardEntry
import com.example.model.UserProfile
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Date

data class LeaderboardQueryResult(
    val entries: List<LeaderboardEntry>,
    val currentUserEntry: LeaderboardEntry?,
    val totalCount: Int,
    val periodId: String,
    val isFromCache: Boolean = false,
    val lastUpdatedTimestamp: Long = System.currentTimeMillis(),
    val emptyMessage: String? = null
)

class LeaderboardService(
    private val context: Context? = null,
    private val firestoreRepo: FirestoreRepository?,
    private val antiCheatService: AntiCheatService
) {
    private val tag = "LeaderboardService"

    // In-memory cache: key = "$scope-$period-$filterId" -> Pair<timestamp, LeaderboardQueryResult>
    private val cache = mutableMapOf<String, Pair<Long, LeaderboardQueryResult>>()
    private val cacheTtlMs = 60_000L // 60 seconds

    // Local historical user ranks: key = "$scope-$period" -> previous rank
    private val userHistoricalRanks = mutableMapOf<String, Int>()

    /**
     * Submit a completed game score to the leaderboard system.
     * Guaranteed duplicate protection using gameId idempotency.
     */
    suspend fun submitGameScore(
        gameId: String,
        profile: UserProfile,
        score: Int,
        xpEarned: Int,
        accuracy: Int,
        durationSeconds: Int,
        category: String,
        gameMode: String,
        completedAt: Long = System.currentTimeMillis()
    ): Boolean = withContext(Dispatchers.IO) {
        if (profile.uid.isBlank() || score <= 0) return@withContext false

        // Idempotency / Duplicate protection check
        val isFirstTime = antiCheatService.markGameProcessed(gameId)
        if (!isFirstTime) {
            Log.w(tag, "Duplicate game result submission rejected: $gameId")
            return@withContext false
        }

        // Invalidate in-memory cache on fresh submission
        cache.clear()

        // Submit to Firestore if available
        if (firestoreRepo != null && firestoreRepo.isAvailable) {
            try {
                val db = FirebaseFirestore.getInstance()
                val periods = listOf(
                    LeaderboardPeriod.ALL_TIME to PeriodHelper.getAllTimePeriodId(),
                    LeaderboardPeriod.WEEKLY to PeriodHelper.getCurrentWeeklyPeriodId(),
                    LeaderboardPeriod.MONTHLY to PeriodHelper.getCurrentMonthlyPeriodId()
                )

                for ((period, periodId) in periods) {
                    val entryDocRef = db.collection("leaderboards")
                        .document(periodId)
                        .collection("entries")
                        .document(profile.uid)

                    val existingDoc = entryDocRef.get().await()
                    val existingBestScore = (existingDoc.getLong("score") ?: 0L).toInt()

                    // For leaderboards, we update if the current session score is greater than or equal to existing best
                    // or cumulative for all-time where appropriate
                    val newScore = if (existingDoc.exists()) {
                        maxOf(existingBestScore, score)
                    } else {
                        score
                    }

                    val entryData = hashMapOf(
                        "userId" to profile.uid,
                        "username" to profile.username,
                        "avatarEmoji" to profile.avatarEmoji,
                        "score" to newScore,
                        "level" to profile.level,
                        "xp" to profile.totalXp,
                        "country" to profile.country,
                        "countryFlag" to profile.countryFlag,
                        "accuracy" to accuracy,
                        "durationSeconds" to durationSeconds,
                        "category" to category,
                        "gameMode" to gameMode,
                        "playerTitle" to profile.selectedTitle,
                        "validationStatus" to ScoreValidationStatus.VALIDATED.name,
                        "updatedAt" to completedAt
                    )

                    entryDocRef.set(entryData, SetOptions.merge()).await()
                }
                return@withContext true
            } catch (e: Exception) {
                Log.e(tag, "Failed to submit score to Firestore", e)
                return@withContext false
            }
        }

        return@withContext true
    }

    /**
     * Fetch real leaderboard entries with tie-breaking, pagination, and user ranking.
     * Strictly NO fake players.
     */
    suspend fun getLeaderboard(
        scope: LeaderboardScope,
        period: LeaderboardPeriod,
        categoryFilter: GameCategory? = null,
        modeFilter: GameModeType? = null,
        currentUser: UserProfile,
        forceRefresh: Boolean = false,
        pageLimit: Int = 50
    ): LeaderboardQueryResult = withContext(Dispatchers.IO) {
        val filterId = when (scope) {
            LeaderboardScope.CATEGORY -> categoryFilter?.id ?: "all_cat"
            LeaderboardScope.GAME_MODE -> modeFilter?.id ?: "all_mode"
            else -> "none"
        }
        val cacheKey = "${scope.name}_${period.name}_${filterId}_${currentUser.country}"

        if (!forceRefresh) {
            val cached = cache[cacheKey]
            if (cached != null && (System.currentTimeMillis() - cached.first) < cacheTtlMs) {
                return@withContext cached.second
            }
        }

        val periodId = PeriodHelper.getPeriodId(period)

        // Check for specific scope empty condition prerequisites
        if (scope == LeaderboardScope.COUNTRY && (currentUser.country.isBlank() || currentUser.country.equals("Global", ignoreCase = true))) {
            return@withContext LeaderboardQueryResult(
                entries = emptyList(),
                currentUserEntry = null,
                totalCount = 0,
                periodId = periodId,
                emptyMessage = "Select your country in Profile to join country rankings."
            )
        }

        if (scope == LeaderboardScope.FRIENDS) {
            // Real friends check: if no friends exist
            return@withContext LeaderboardQueryResult(
                entries = if (currentUser.bestScore > 0) listOf(
                    LeaderboardEntry(
                        rank = 1,
                        username = currentUser.username,
                        avatarEmoji = currentUser.avatarEmoji,
                        score = currentUser.bestScore,
                        level = currentUser.level,
                        countryFlag = currentUser.countryFlag,
                        isCurrentUser = true,
                        userId = currentUser.uid,
                        xp = currentUser.totalXp,
                        country = currentUser.country,
                        playerTitle = currentUser.selectedTitle
                    )
                ) else emptyList(),
                currentUserEntry = if (currentUser.bestScore > 0) LeaderboardEntry(
                    rank = 1,
                    username = currentUser.username,
                    avatarEmoji = currentUser.avatarEmoji,
                    score = currentUser.bestScore,
                    level = currentUser.level,
                    countryFlag = currentUser.countryFlag,
                    isCurrentUser = true,
                    userId = currentUser.uid,
                    xp = currentUser.totalXp,
                    country = currentUser.country,
                    playerTitle = currentUser.selectedTitle
                ) else null,
                totalCount = if (currentUser.bestScore > 0) 1 else 0,
                periodId = periodId,
                emptyMessage = "Add friends to compare your scores."
            )
        }

        // Query Firestore if available
        if (firestoreRepo != null && firestoreRepo.isAvailable) {
            try {
                val db = FirebaseFirestore.getInstance()
                var query: Query = db.collection("leaderboards")
                    .document(periodId)
                    .collection("entries")
                    .orderBy("score", Query.Direction.DESCENDING)

                if (scope == LeaderboardScope.COUNTRY) {
                    query = query.whereEqualTo("country", currentUser.country)
                }

                val snapshot = query.limit(pageLimit.toLong()).get().await()

                if (snapshot.isEmpty) {
                    val result = if (currentUser.bestScore > 0) {
                        val singleUserEntry = LeaderboardEntry(
                            rank = 1,
                            username = currentUser.username,
                            avatarEmoji = currentUser.avatarEmoji,
                            score = currentUser.bestScore,
                            level = currentUser.level,
                            countryFlag = currentUser.countryFlag,
                            isCurrentUser = true,
                            userId = currentUser.uid,
                            xp = currentUser.totalXp,
                            country = currentUser.country,
                            playerTitle = currentUser.selectedTitle
                        )
                        LeaderboardQueryResult(
                            entries = listOf(singleUserEntry),
                            currentUserEntry = singleUserEntry,
                            totalCount = 1,
                            periodId = periodId
                        )
                    } else {
                        LeaderboardQueryResult(
                            entries = emptyList(),
                            currentUserEntry = null,
                            totalCount = 0,
                            periodId = periodId,
                            emptyMessage = "Not enough players yet."
                        )
                    }
                    cache[cacheKey] = Pair(System.currentTimeMillis(), result)
                    return@withContext result
                }

                // Process docs and assign ranks with consistent tie-breaking:
                // Higher score -> higher accuracy -> faster time -> older timestamp
                var rawEntries = snapshot.documents.mapNotNull { doc ->
                    val uid = doc.getString("userId") ?: doc.id
                    val score = (doc.getLong("score") ?: 0L).toInt()
                    val level = (doc.getLong("level") ?: 1L).toInt()
                    val xp = (doc.getLong("xp") ?: 0L).toInt()
                    val accuracy = (doc.getLong("accuracy") ?: 0L).toInt()
                    val duration = (doc.getLong("durationSeconds") ?: 0L).toInt()
                    val username = doc.getString("username") ?: "Player"
                    val avatar = doc.getString("avatarEmoji") ?: "🧠"
                    val country = doc.getString("country") ?: "Global"
                    val countryFlag = doc.getString("countryFlag") ?: "🌐"
                    val title = doc.getString("playerTitle") ?: ""
                    val updatedAt = doc.getLong("updatedAt") ?: 0L

                    LeaderboardEntry(
                        rank = 0,
                        username = username,
                        avatarEmoji = avatar,
                        score = score,
                        level = level,
                        countryFlag = countryFlag,
                        isCurrentUser = (uid == currentUser.uid) || (username.equals(currentUser.username, ignoreCase = true)),
                        userId = uid,
                        xp = xp,
                        country = country,
                        accuracy = accuracy,
                        durationSeconds = duration,
                        playerTitle = title
                    )
                }

                // Sort using deterministic tie-breaking
                val sorted = rawEntries.sortedWith(
                    compareByDescending<LeaderboardEntry> { it.score }
                        .thenByDescending { it.accuracy }
                        .thenBy { if (it.durationSeconds > 0) it.durationSeconds else 9999 }
                )

                // Assign 1-indexed ranks
                val rankedEntries = sorted.mapIndexed { index, entry ->
                    entry.copy(rank = index + 1)
                }

                val userEntry = rankedEntries.find { it.isCurrentUser }
                    ?: if (currentUser.bestScore > 0) {
                        LeaderboardEntry(
                            rank = rankedEntries.size + 1,
                            username = currentUser.username,
                            avatarEmoji = currentUser.avatarEmoji,
                            score = currentUser.bestScore,
                            level = currentUser.level,
                            countryFlag = currentUser.countryFlag,
                            isCurrentUser = true,
                            userId = currentUser.uid,
                            xp = currentUser.totalXp,
                            country = currentUser.country,
                            playerTitle = currentUser.selectedTitle
                        )
                    } else null

                val result = LeaderboardQueryResult(
                    entries = rankedEntries,
                    currentUserEntry = userEntry,
                    totalCount = rankedEntries.size,
                    periodId = periodId,
                    emptyMessage = if (rankedEntries.isEmpty()) "Not enough players yet." else null
                )

                // Track rank movement
                if (userEntry != null) {
                    userHistoricalRanks["$scope-$period"] = userEntry.rank
                }

                cache[cacheKey] = Pair(System.currentTimeMillis(), result)
                return@withContext result
            } catch (e: Exception) {
                Log.w(tag, "Failed to fetch from Firestore, falling back to local user entry: ${e.message}")
            }
        }

        // Local / Offline fallback with real user data
        val localUserEntry = if (currentUser.bestScore > 0) {
            LeaderboardEntry(
                rank = 1,
                username = currentUser.username,
                avatarEmoji = currentUser.avatarEmoji,
                score = currentUser.bestScore,
                level = currentUser.level,
                countryFlag = currentUser.countryFlag,
                isCurrentUser = true,
                userId = currentUser.uid,
                xp = currentUser.totalXp,
                country = currentUser.country,
                playerTitle = currentUser.selectedTitle
            )
        } else null

        val localResult = LeaderboardQueryResult(
            entries = if (localUserEntry != null) listOf(localUserEntry) else emptyList(),
            currentUserEntry = localUserEntry,
            totalCount = if (localUserEntry != null) 1 else 0,
            periodId = periodId,
            isFromCache = true,
            emptyMessage = if (localUserEntry == null) "Not enough players yet." else null
        )

        return@withContext localResult
    }

    /**
     * Compute rank movement comparing current rank with prior historical rank.
     */
    fun calculateRankMovement(scope: LeaderboardScope, period: LeaderboardPeriod, currentRank: Int): RankMovementInfo? {
        val key = "$scope-$period"
        val previous = userHistoricalRanks[key] ?: return null
        val delta = previous - currentRank // positive = improved

        val message = when {
            delta > 0 -> "Your ${period.displayName.lowercase()} rank improved from #$previous to #$currentRank."
            delta < 0 -> "Your ${period.displayName.lowercase()} rank moved from #$previous to #$currentRank."
            else -> "Your ${period.displayName.lowercase()} rank is unchanged at #$currentRank."
        }

        return RankMovementInfo(
            previousRank = previous,
            currentRank = currentRank,
            delta = delta,
            period = period,
            message = message
        )
    }
}
