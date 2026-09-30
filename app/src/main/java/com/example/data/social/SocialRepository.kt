package com.example.data.social

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.data.competitive.AchievementEngine
import com.example.data.competitive.LeaderboardQueryResult
import com.example.data.competitive.ScoreValidationStatus
import com.example.data.firestore.FirestoreRepository
import com.example.model.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.security.SecureRandom
import java.util.Locale

class SocialRepository(
    private val context: Context? = null,
    private val firestoreRepo: FirestoreRepository?,
    private val currentProfileProvider: () -> UserProfile,
    private val onRewardXp: (Int) -> Unit,
    private val onNotificationCreated: (AppNotification) -> Unit,
    private val onAchievementUnlocked: (String) -> Unit
) {
    private val tag = "SocialRepository"
    private val prefs: SharedPreferences? = context?.getSharedPreferences("brain_battle_social_prefs", Context.MODE_PRIVATE)

    private val firestore: FirebaseFirestore? = try {
        if (firestoreRepo?.isAvailable == true) FirebaseFirestore.getInstance() else null
    } catch (e: Exception) {
        null
    }

    // State flows for UI observation
    private val _friends = MutableStateFlow<List<Friend>>(emptyList())
    val friends: StateFlow<List<Friend>> = _friends.asStateFlow()

    private val _incomingRequests = MutableStateFlow<List<FriendRequest>>(emptyList())
    val incomingRequests: StateFlow<List<FriendRequest>> = _incomingRequests.asStateFlow()

    private val _outgoingRequests = MutableStateFlow<List<FriendRequest>>(emptyList())
    val outgoingRequests: StateFlow<List<FriendRequest>> = _outgoingRequests.asStateFlow()

    private val _blockedUsers = MutableStateFlow<List<BlockedUser>>(emptyList())
    val blockedUsers: StateFlow<List<BlockedUser>> = _blockedUsers.asStateFlow()

    private val _myChallenges = MutableStateFlow<List<FriendChallengeRecord>>(emptyList())
    val myChallenges: StateFlow<List<FriendChallengeRecord>> = _myChallenges.asStateFlow()

    private val _friendActivity = MutableStateFlow<List<FriendActivityItem>>(emptyList())
    val friendActivity: StateFlow<List<FriendActivityItem>> = _friendActivity.asStateFlow()

    private val _privacySettings = MutableStateFlow(loadPrivacySettings())
    val privacySettings: StateFlow<SocialPrivacySettings> = _privacySettings.asStateFlow()

    private val _challengeStats = MutableStateFlow(loadChallengeStats())
    val challengeStats: StateFlow<ChallengeStatistics> = _challengeStats.asStateFlow()

    init {
        loadCachedData()
    }

    fun getPlayerId(): String {
        var id = prefs?.getString("public_player_id", null)
        if (id.isNullOrBlank()) {
            val uid = currentProfileProvider().uid
            id = generatePlayerId(uid)
            prefs?.edit()?.putString("public_player_id", id)?.apply()
        }
        return id
    }

    private fun generatePlayerId(seed: String): String {
        val chars = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ" // Base32 unambiguous
        val random = SecureRandom()
        val suffix = (1..6).map { chars[random.nextInt(chars.length)] }.joinToString("")
        return "BB-$suffix"
    }

    private fun loadPrivacySettings(): SocialPrivacySettings {
        return SocialPrivacySettings(
            allowFriendRequests = prefs?.getBoolean("priv_allow_friend_reqs", true) ?: true,
            allowChallengeRequests = prefs?.getBoolean("priv_allow_challenges", true) ?: true,
            showActivity = prefs?.getBoolean("priv_show_activity", true) ?: true,
            showCountry = prefs?.getBoolean("priv_show_country", true) ?: true,
            showProfileStatistics = prefs?.getBoolean("priv_show_stats", true) ?: true
        )
    }

    suspend fun updatePrivacySettings(settings: SocialPrivacySettings) = withContext(Dispatchers.IO) {
        _privacySettings.value = settings
        prefs?.edit()?.apply {
            putBoolean("priv_allow_friend_reqs", settings.allowFriendRequests)
            putBoolean("priv_allow_challenges", settings.allowChallengeRequests)
            putBoolean("priv_show_activity", settings.showActivity)
            putBoolean("priv_show_country", settings.showCountry)
            putBoolean("priv_show_stats", settings.showProfileStatistics)
            apply()
        }
        val currentUid = currentProfileProvider().uid
        if (firestore != null && currentUid.isNotBlank()) {
            try {
                firestore.collection("users").document(currentUid).set(
                    mapOf("socialPrivacySettings" to mapOf(
                        "allowFriendRequests" to settings.allowFriendRequests,
                        "allowChallengeRequests" to settings.allowChallengeRequests,
                        "showActivity" to settings.showActivity,
                        "showCountry" to settings.showCountry,
                        "showProfileStatistics" to settings.showProfileStatistics
                    )),
                    SetOptions.merge()
                ).await()
            } catch (e: Exception) {
                Log.w(tag, "Failed to save privacy settings to Firestore: ${e.message}")
            }
        }
    }

    private fun loadChallengeStats(): ChallengeStatistics {
        val played = prefs?.getInt("cs_played", 0) ?: 0
        val wins = prefs?.getInt("cs_wins", 0) ?: 0
        val losses = prefs?.getInt("cs_losses", 0) ?: 0
        val draws = prefs?.getInt("cs_draws", 0) ?: 0
        val winRate = if (played > 0) (wins * 100) / played else 0
        val bestScore = prefs?.getInt("cs_best_score", 0) ?: 0
        val streak = prefs?.getInt("cs_streak", 0) ?: 0
        return ChallengeStatistics(
            totalPlayed = played,
            wins = wins,
            losses = losses,
            draws = draws,
            winRatePercentage = winRate,
            bestScore = bestScore,
            currentWinStreak = streak
        )
    }

    private fun loadCachedData() {
        // Load blocked users
        val blockedSet = prefs?.getStringSet("blocked_users_set", emptySet()) ?: emptySet()
        _blockedUsers.value = blockedSet.map { entry ->
            val parts = entry.split("|")
            BlockedUser(
                blockedUid = parts.getOrNull(0) ?: "",
                username = parts.getOrNull(1) ?: "Blocked Player",
                avatarEmoji = parts.getOrNull(2) ?: "👤",
                blockedAt = parts.getOrNull(3)?.toLongOrNull() ?: System.currentTimeMillis()
            )
        }
    }

    // ==========================================
    // FIND & SEARCH PLAYERS
    // ==========================================

    suspend fun searchPlayers(query: String): List<PlayerPublicProfile> = withContext(Dispatchers.IO) {
        val cleanQuery = query.trim()
        if (cleanQuery.isBlank()) return@withContext emptyList()

        val currentUser = currentProfileProvider()
        val currentUid = currentUser.uid
        val currentPid = getPlayerId()

        // Prevent searching for yourself
        if (cleanQuery.equals(currentUser.username, ignoreCase = true) ||
            cleanQuery.equals(currentPid, ignoreCase = true)) {
            return@withContext emptyList()
        }

        val results = mutableListOf<PlayerPublicProfile>()

        if (firestore != null) {
            try {
                // If it looks like a Player ID (e.g. BB-XXXXXX)
                if (cleanQuery.startsWith("BB-", ignoreCase = true)) {
                    val snapshot = firestore.collection("users")
                        .whereEqualTo("playerId", cleanQuery.uppercase(Locale.ROOT))
                        .limit(5)
                        .get()
                        .await()
                    for (doc in snapshot.documents) {
                        if (doc.id == currentUid) continue
                        val profile = docToPublicProfile(doc.id, doc.data ?: emptyMap())
                        if (profile != null) results.add(profile)
                    }
                } else {
                    // Search by username prefix
                    val snapshot = firestore.collection("users")
                        .whereGreaterThanOrEqualTo("username", cleanQuery)
                        .whereLessThanOrEqualTo("username", cleanQuery + "\uf8ff")
                        .limit(10)
                        .get()
                        .await()
                    for (doc in snapshot.documents) {
                        if (doc.id == currentUid) continue
                        val profile = docToPublicProfile(doc.id, doc.data ?: emptyMap())
                        if (profile != null) results.add(profile)
                    }
                }
            } catch (e: Exception) {
                Log.w(tag, "Firestore search failed, checking local directory: ${e.message}")
            }
        }

        // Filter out blocked users
        val blockedIds = _blockedUsers.value.map { it.blockedUid }.toSet()
        results.filterNot { it.uid in blockedIds }
    }

    private fun docToPublicProfile(uid: String, data: Map<String, Any>): PlayerPublicProfile? {
        val username = data["username"] as? String ?: (data["displayName"] as? String) ?: return null
        val stats = data["statistics"] as? Map<*, *>
        val privMap = data["socialPrivacySettings"] as? Map<*, *>
        val isFriend = _friends.value.any { it.userId == uid }
        val isBlocked = _blockedUsers.value.any { it.blockedUid == uid }
        val hasPending = _outgoingRequests.value.any { it.receiverId == uid && it.status == FriendRequestStatus.PENDING }

        return PlayerPublicProfile(
            uid = uid,
            playerId = data["playerId"] as? String ?: "BB-${uid.take(6).uppercase(Locale.ROOT)}",
            username = username,
            displayName = data["displayName"] as? String ?: username,
            avatarEmoji = data["avatarEmoji"] as? String ?: "🧠",
            level = (data["level"] as? Long)?.toInt() ?: 1,
            totalXp = (data["xp"] as? Long)?.toInt() ?: 0,
            playerTitle = data["selectedTitle"] as? String ?: "Rookie",
            country = data["country"] as? String ?: "Global",
            countryFlag = data["countryFlag"] as? String ?: "🌐",
            bestScore = (data["bestScore"] as? Long)?.toInt() ?: 0,
            totalGamesPlayed = (data["totalGames"] as? Long)?.toInt() ?: 0,
            accuracyPercentage = (stats?.get("overallAccuracy") as? Long)?.toInt() ?: 0,
            achievementsCount = (data["achievementsUnlocked"] as? Long)?.toInt() ?: 0,
            globalRank = (data["globalRank"] as? Long)?.toInt(),
            challengesPlayed = (data["challengesPlayed"] as? Long)?.toInt() ?: 0,
            challengesWon = (data["challengesWon"] as? Long)?.toInt() ?: 0,
            challengesLost = (data["challengesLost"] as? Long)?.toInt() ?: 0,
            challengesDraw = (data["challengesDraw"] as? Long)?.toInt() ?: 0,
            isFriend = isFriend,
            isBlocked = isBlocked,
            hasPendingRequest = hasPending,
            privacySettings = SocialPrivacySettings(
                allowFriendRequests = privMap?.get("allowFriendRequests") as? Boolean ?: true,
                allowChallengeRequests = privMap?.get("allowChallengeRequests") as? Boolean ?: true,
                showActivity = privMap?.get("showActivity") as? Boolean ?: true,
                showCountry = privMap?.get("showCountry") as? Boolean ?: true,
                showProfileStatistics = privMap?.get("showProfileStatistics") as? Boolean ?: true
            )
        )
    }

    // ==========================================
    // FRIEND REQUESTS & FRIENDSHIP
    // ==========================================

    sealed class FriendRequestResult {
        object Success : FriendRequestResult()
        data class Error(val message: String) : FriendRequestResult()
    }

    suspend fun sendFriendRequest(
        targetUserId: String,
        targetUsername: String,
        targetAvatar: String = "👤"
    ): FriendRequest? = withContext(Dispatchers.IO) {
        val dummyProfile = PlayerPublicProfile(
            uid = targetUserId,
            playerId = "BB-${targetUserId.take(6).uppercase(Locale.ROOT)}",
            username = targetUsername,
            avatarEmoji = targetAvatar,
            playerTitle = "Player",
            level = 1,
            totalXp = 0,
            bestScore = 0,
            totalGamesPlayed = 0,
            accuracyPercentage = 0,
            achievementsCount = 0,
            isFriend = false,
            isBlocked = false,
            hasPendingRequest = false
        )
        val res = sendFriendRequest(dummyProfile)
        if (res is FriendRequestResult.Success) {
            _outgoingRequests.value.lastOrNull { it.receiverId == targetUserId }
        } else null
    }

    suspend fun sendFriendRequest(targetProfile: PlayerPublicProfile): FriendRequestResult = withContext(Dispatchers.IO) {
        val currentUser = currentProfileProvider()
        val currentUid = currentUser.uid

        // 1. Prevent sending request to yourself
        if (targetProfile.uid == currentUid || targetProfile.username.equals(currentUser.username, ignoreCase = true)) {
            return@withContext FriendRequestResult.Error("You cannot send a friend request to yourself.")
        }

        // 2. Prevent sending to blocked user
        if (_blockedUsers.value.any { it.blockedUid == targetProfile.uid }) {
            return@withContext FriendRequestResult.Error("Cannot send request to a blocked player. Unblock them first.")
        }

        // 3. Prevent duplicate friendship
        if (_friends.value.any { it.userId == targetProfile.uid }) {
            return@withContext FriendRequestResult.Error("You are already friends with this player.")
        }

        // 4. Prevent duplicate pending requests
        if (_outgoingRequests.value.any { it.receiverId == targetProfile.uid && it.status == FriendRequestStatus.PENDING }) {
            return@withContext FriendRequestResult.Error("A friend request is already pending.")
        }

        // 5. Check target's privacy settings
        if (!targetProfile.privacySettings.allowFriendRequests) {
            return@withContext FriendRequestResult.Error("${targetProfile.username} is not accepting friend requests right now.")
        }

        val requestId = "freq_${System.currentTimeMillis()}_${(100..999).random()}"
        val request = FriendRequest(
            id = requestId,
            senderId = currentUid,
            senderUsername = currentUser.username,
            senderPlayerId = getPlayerId(),
            senderAvatarEmoji = currentUser.avatarEmoji,
            senderLevel = currentUser.level,
            senderTitle = currentUser.selectedTitle,
            receiverId = targetProfile.uid,
            receiverUsername = targetProfile.username,
            status = FriendRequestStatus.PENDING,
            createdAt = System.currentTimeMillis()
        )

        // Store in outgoing state
        val updatedOutgoing = _outgoingRequests.value.toMutableList()
        updatedOutgoing.add(request)
        _outgoingRequests.value = updatedOutgoing

        // Firestore write if available
        if (firestore != null && currentUid.isNotBlank()) {
            try {
                val data = hashMapOf(
                    "id" to request.id,
                    "senderId" to request.senderId,
                    "senderUsername" to request.senderUsername,
                    "senderPlayerId" to request.senderPlayerId,
                    "senderAvatarEmoji" to request.senderAvatarEmoji,
                    "senderLevel" to request.senderLevel,
                    "senderTitle" to request.senderTitle,
                    "receiverId" to request.receiverId,
                    "receiverUsername" to request.receiverUsername,
                    "status" to request.status.name,
                    "createdAt" to request.createdAt
                )
                firestore.collection("friendRequests").document(requestId).set(data).await()
            } catch (e: Exception) {
                Log.w(tag, "Failed to write friend request to Firestore: ${e.message}")
            }
        }

        return@withContext FriendRequestResult.Success
    }

    suspend fun acceptFriendRequest(request: FriendRequest) = withContext(Dispatchers.IO) {
        val currentUser = currentProfileProvider()
        val currentUid = currentUser.uid

        val isSender = request.senderId == currentUid
        val friendUid = if (isSender) request.receiverId else request.senderId
        val friendUsername = if (isSender) request.receiverUsername else request.senderUsername
        val friendAvatar = if (isSender) "🧠" else request.senderAvatarEmoji
        val friendLevel = if (isSender) 1 else request.senderLevel
        val friendTitle = if (isSender) "Rookie" else request.senderTitle
        val friendPlayerId = if (isSender) "BB-${friendUid.take(6).uppercase(Locale.ROOT)}" else request.senderPlayerId

        // Add to friends
        val newFriend = Friend(
            userId = friendUid,
            username = friendUsername,
            displayName = friendUsername,
            avatarEmoji = friendAvatar,
            level = friendLevel,
            playerTitle = friendTitle,
            playerId = friendPlayerId,
            status = OnlineStatus.ONLINE,
            addedAt = System.currentTimeMillis()
        )

        val updatedFriends = _friends.value.toMutableList()
        if (updatedFriends.none { it.userId == newFriend.userId }) {
            updatedFriends.add(newFriend)
            _friends.value = updatedFriends
        }

        // Update requests
        _incomingRequests.value = _incomingRequests.value.filterNot { it.id == request.id }
        _outgoingRequests.value = _outgoingRequests.value.filterNot { it.id == request.id }

        // Trigger notification
        onNotificationCreated(
            AppNotification(
                id = "notif_fa_${System.currentTimeMillis()}",
                title = "🤝 Friend Added!",
                message = "You are now friends with ${request.senderUsername}!",
                timeAgo = "Just now",
                iconEmoji = "🤝",
                type = NotificationType.FRIEND_ACCEPTED
            )
        )

        // Check FRIEND_MAKER achievement if user has >= 5 friends
        if (_friends.value.size >= 5) {
            onAchievementUnlocked("FRIEND_MAKER")
        }

        // Firestore updates
        if (firestore != null && currentUid.isNotBlank()) {
            try {
                firestore.collection("friendRequests").document(request.id).update("status", FriendRequestStatus.ACCEPTED.name).await()

                // Add to current user's friends subcollection
                firestore.collection("users").document(currentUid)
                    .collection("friends").document(request.senderId)
                    .set(mapOf(
                        "userId" to request.senderId,
                        "username" to request.senderUsername,
                        "avatarEmoji" to request.senderAvatarEmoji,
                        "level" to request.senderLevel,
                        "playerTitle" to request.senderTitle,
                        "playerId" to request.senderPlayerId,
                        "addedAt" to System.currentTimeMillis()
                    )).await()

                // Add current user to sender's friends subcollection
                firestore.collection("users").document(request.senderId)
                    .collection("friends").document(currentUid)
                    .set(mapOf(
                        "userId" to currentUid,
                        "username" to currentUser.username,
                        "avatarEmoji" to currentUser.avatarEmoji,
                        "level" to currentUser.level,
                        "playerTitle" to currentUser.selectedTitle,
                        "playerId" to getPlayerId(),
                        "addedAt" to System.currentTimeMillis()
                    )).await()
            } catch (e: Exception) {
                Log.w(tag, "Firestore accept friend request error: ${e.message}")
            }
        }
    }

    suspend fun acceptFriendRequest(requestId: String): Boolean = withContext(Dispatchers.IO) {
        val req = _incomingRequests.value.firstOrNull { it.id == requestId }
            ?: _outgoingRequests.value.firstOrNull { it.id == requestId }
        if (req != null) {
            acceptFriendRequest(req)
            true
        } else false
    }

    suspend fun declineFriendRequest(requestId: String): Boolean = withContext(Dispatchers.IO) {
        _incomingRequests.value = _incomingRequests.value.filterNot { it.id == requestId }
        if (firestore != null) {
            try {
                firestore.collection("friendRequests").document(requestId).update("status", FriendRequestStatus.DECLINED.name).await()
            } catch (e: Exception) {
                Log.w(tag, "Firestore decline request error: ${e.message}")
            }
        }
        true
    }

    suspend fun cancelFriendRequest(requestId: String) = withContext(Dispatchers.IO) {
        _outgoingRequests.value = _outgoingRequests.value.filterNot { it.id == requestId }
        if (firestore != null) {
            try {
                firestore.collection("friendRequests").document(requestId).update("status", FriendRequestStatus.CANCELLED.name).await()
            } catch (e: Exception) {
                Log.w(tag, "Firestore cancel request error: ${e.message}")
            }
        }
    }

    suspend fun removeFriend(friendUid: String): Boolean = withContext(Dispatchers.IO) {
        val currentUid = currentProfileProvider().uid
        _friends.value = _friends.value.filterNot { it.userId == friendUid }

        if (firestore != null && currentUid.isNotBlank()) {
            try {
                firestore.collection("users").document(currentUid)
                    .collection("friends").document(friendUid).delete().await()
                firestore.collection("users").document(friendUid)
                    .collection("friends").document(currentUid).delete().await()
            } catch (e: Exception) {
                Log.w(tag, "Firestore remove friend error: ${e.message}")
            }
        }
        true
    }

    // ==========================================
    // BLOCKING & UNBLOCKING & REPORTING
    // ==========================================

    fun isUserBlocked(uid: String): Boolean = _blockedUsers.value.any { it.blockedUid == uid }

    suspend fun blockUser(targetUid: String, targetUsername: String, avatarEmoji: String = "👤") = withContext(Dispatchers.IO) {
        val currentUid = currentProfileProvider().uid
        if (targetUid == currentUid) return@withContext

        // 1. Remove from friends if friend
        removeFriend(targetUid)

        // 2. Remove any pending requests
        _incomingRequests.value = _incomingRequests.value.filterNot { it.senderId == targetUid }
        _outgoingRequests.value = _outgoingRequests.value.filterNot { it.receiverId == targetUid }

        // 3. Cancel any pending challenges
        _myChallenges.value = _myChallenges.value.map { ch ->
            if (ch.opponentId == targetUid || ch.creatorId == targetUid) {
                ch.copy(status = ChallengeStatus.CANCELLED)
            } else ch
        }

        // 4. Add to blocked list
        val newBlocked = BlockedUser(
            blockedUid = targetUid,
            username = targetUsername,
            avatarEmoji = avatarEmoji,
            blockedAt = System.currentTimeMillis()
        )
        val updated = _blockedUsers.value.filterNot { it.blockedUid == targetUid } + newBlocked
        _blockedUsers.value = updated

        // Persist blocked in prefs
        val set = updated.map { "${it.blockedUid}|${it.username}|${it.avatarEmoji}|${it.blockedAt}" }.toSet()
        prefs?.edit()?.putStringSet("blocked_users_set", set)?.apply()

        // Persist in Firestore
        if (firestore != null && currentUid.isNotBlank()) {
            try {
                firestore.collection("users").document(currentUid)
                    .collection("blocked").document(targetUid)
                    .set(mapOf(
                        "blockedUid" to targetUid,
                        "username" to targetUsername,
                        "avatarEmoji" to avatarEmoji,
                        "blockedAt" to System.currentTimeMillis()
                    )).await()
            } catch (e: Exception) {
                Log.w(tag, "Firestore block user error: ${e.message}")
            }
        }
    }

    suspend fun unblockUser(blockedUid: String) = withContext(Dispatchers.IO) {
        val currentUid = currentProfileProvider().uid
        val updated = _blockedUsers.value.filterNot { it.blockedUid == blockedUid }
        _blockedUsers.value = updated

        val set = updated.map { "${it.blockedUid}|${it.username}|${it.avatarEmoji}|${it.blockedAt}" }.toSet()
        prefs?.edit()?.putStringSet("blocked_users_set", set)?.apply()

        if (firestore != null && currentUid.isNotBlank()) {
            try {
                firestore.collection("users").document(currentUid)
                    .collection("blocked").document(blockedUid).delete().await()
            } catch (e: Exception) {
                Log.w(tag, "Firestore unblock user error: ${e.message}")
            }
        }
    }

    suspend fun reportUser(
        targetUid: String,
        targetUsername: String,
        reason: UserReportReason,
        details: String
    ): Boolean = withContext(Dispatchers.IO) {
        val currentUid = currentProfileProvider().uid
        val report = UserReport(
            reportedUserId = targetUid,
            reportedUsername = targetUsername,
            reportedByUserId = currentUid,
            reason = reason,
            details = details,
            timestamp = System.currentTimeMillis()
        )

        if (firestore != null) {
            try {
                firestore.collection("reports").document(report.id).set(mapOf(
                    "id" to report.id,
                    "reportedUserId" to report.reportedUserId,
                    "reportedUsername" to report.reportedUsername,
                    "reportedByUserId" to report.reportedByUserId,
                    "reason" to report.reason.name,
                    "details" to report.details,
                    "timestamp" to report.timestamp
                )).await()
                return@withContext true
            } catch (e: Exception) {
                Log.w(tag, "Firestore save report error: ${e.message}")
            }
        }
        return@withContext true
    }

    // ==========================================
    // CHALLENGE SYSTEM & FAIR QUESTION MATCHES
    // ==========================================

    fun generateChallengeCode(): String {
        val chars = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ"
        val random = SecureRandom()
        val randomChars = (1..6).map { chars[random.nextInt(chars.length)] }.joinToString("")
        return "BB$randomChars"
    }

    fun generateShareableChallengeText(challenge: FriendChallengeRecord): String {
        return "⚔️ Challenge me on Brain Battle!\n" +
               "Challenge Code: ${challenge.challengeCode}\n" +
               "Category: ${challenge.category.uppercase(Locale.ROOT)} | Difficulty: ${challenge.difficulty}\n" +
               "Tap to accept: brainbattle://challenge/${challenge.challengeCode}"
    }

    fun recordNotification(notification: AppNotification) {
        onNotificationCreated(notification)
    }

    suspend fun createChallenge(
        opponentId: String = "",
        opponentName: String = "",
        opponentAvatar: String = "👥",
        category: String,
        gameMode: String = "friend_challenge",
        difficulty: String,
        questionCount: Int = 5,
        timeLimitSeconds: Int = 120,
        questionIds: List<String> = emptyList()
    ): FriendChallengeRecord = withContext(Dispatchers.IO) {
        val currentUser = currentProfileProvider()
        val currentUid = currentUser.uid

        val challengeId = "chal_${System.currentTimeMillis()}_${(100..999).random()}"
        val code = generateChallengeCode()
        val expirationMillis = System.currentTimeMillis() + 24 * 60 * 60 * 1000L // 24 hours

        val record = FriendChallengeRecord(
            challengeId = challengeId,
            challengeCode = code,
            creatorId = currentUid,
            creatorName = currentUser.username,
            creatorAvatar = currentUser.avatarEmoji,
            opponentId = opponentId,
            opponentName = opponentName,
            opponentAvatar = opponentAvatar,
            category = category,
            gameMode = gameMode,
            difficulty = difficulty,
            questionCount = questionCount,
            timeLimitSeconds = timeLimitSeconds,
            status = ChallengeStatus.SENT,
            createdAt = System.currentTimeMillis(),
            expiresAt = expirationMillis,
            questionSetId = "qset_${System.currentTimeMillis()}",
            questionIds = questionIds
        )

        val updated = _myChallenges.value.toMutableList()
        updated.add(0, record)
        _myChallenges.value = updated

        // Dispatches notification if opponent is a specific friend
        if (opponentName.isNotBlank()) {
            onNotificationCreated(
                AppNotification(
                    id = "notif_cs_${System.currentTimeMillis()}",
                    title = "⚔️ Challenge Sent!",
                    message = "Challenge sent to $opponentName! Room Code: $code",
                    timeAgo = "Just now",
                    iconEmoji = "⚔️",
                    type = NotificationType.FRIEND_CHALLENGE,
                    actionData = challengeId
                )
            )
        }

        // Firestore sync
        if (firestore != null) {
            try {
                val data = hashMapOf(
                    "challengeId" to record.challengeId,
                    "challengeCode" to record.challengeCode,
                    "creatorId" to record.creatorId,
                    "creatorName" to record.creatorName,
                    "creatorAvatar" to record.creatorAvatar,
                    "opponentId" to record.opponentId,
                    "opponentName" to record.opponentName,
                    "opponentAvatar" to record.opponentAvatar,
                    "category" to record.category,
                    "gameMode" to record.gameMode,
                    "difficulty" to record.difficulty,
                    "questionCount" to record.questionCount,
                    "timeLimitSeconds" to record.timeLimitSeconds,
                    "rules" to record.rules,
                    "status" to record.status.name,
                    "createdAt" to record.createdAt,
                    "expiresAt" to record.expiresAt,
                    "questionSetId" to record.questionSetId,
                    "questionIds" to record.questionIds,
                    "version" to record.version
                )
                firestore.collection("challenges").document(challengeId).set(data).await()
            } catch (e: Exception) {
                Log.w(tag, "Firestore create challenge error: ${e.message}")
            }
        }

        return@withContext record
    }

    suspend fun getChallengeById(challengeId: String): FriendChallengeRecord? = withContext(Dispatchers.IO) {
        val local = _myChallenges.value.firstOrNull { it.challengeId == challengeId }
        if (local != null) return@withContext local
        if (firestore != null) {
            try {
                val doc = firestore.collection("challenges").document(challengeId).get().await()
                if (doc.exists() && doc.data != null) {
                    docToChallengeRecord(doc.id, doc.data!!)
                } else null
            } catch (e: Exception) {
                null
            }
        } else null
    }

    suspend fun getChallengeByCode(code: String): FriendChallengeRecord? = withContext(Dispatchers.IO) {
        val cleanCode = code.trim().uppercase(Locale.ROOT)
        // Check local list first
        val localMatch = _myChallenges.value.firstOrNull { it.challengeCode.equals(cleanCode, ignoreCase = true) }
        if (localMatch != null) {
            if (localMatch.isExpired && localMatch.status != ChallengeStatus.COMPLETED) {
                return@withContext localMatch.copy(status = ChallengeStatus.EXPIRED)
            }
            return@withContext localMatch
        }

        // Query Firestore
        if (firestore != null) {
            try {
                val snapshot = firestore.collection("challenges")
                    .whereEqualTo("challengeCode", cleanCode)
                    .limit(1)
                    .get()
                    .await()
                val doc = snapshot.documents.firstOrNull() ?: return@withContext null
                val record = docToChallengeRecord(doc.id, doc.data ?: emptyMap())
                if (record != null) {
                    if (record.isExpired && record.status != ChallengeStatus.COMPLETED) {
                        return@withContext record.copy(status = ChallengeStatus.EXPIRED)
                    }
                    return@withContext record
                }
            } catch (e: Exception) {
                Log.w(tag, "Firestore get challenge by code error: ${e.message}")
            }
        }

        return@withContext null
    }

    suspend fun acceptChallenge(challengeId: String): FriendChallengeRecord? = withContext(Dispatchers.IO) {
        val currentUser = currentProfileProvider()
        val currentUid = currentUser.uid

        _myChallenges.value = _myChallenges.value.map { ch ->
            if (ch.challengeId == challengeId) {
                ch.copy(
                    status = ChallengeStatus.ACCEPTED,
                    acceptedAt = System.currentTimeMillis(),
                    opponentId = if (ch.opponentId.isBlank()) currentUid else ch.opponentId,
                    opponentName = if (ch.opponentName.isBlank()) currentUser.username else ch.opponentName,
                    opponentAvatar = if (ch.opponentAvatar == "👥") currentUser.avatarEmoji else ch.opponentAvatar
                )
            } else ch
        }

        if (firestore != null) {
            try {
                firestore.collection("challenges").document(challengeId).set(
                    mapOf(
                        "status" to ChallengeStatus.ACCEPTED.name,
                        "acceptedAt" to System.currentTimeMillis(),
                        "opponentId" to currentUid,
                        "opponentName" to currentUser.username,
                        "opponentAvatar" to currentUser.avatarEmoji
                    ),
                    SetOptions.merge()
                ).await()
            } catch (e: Exception) {
                Log.w(tag, "Firestore accept challenge error: ${e.message}")
            }
        }

        return@withContext _myChallenges.value.firstOrNull { it.challengeId == challengeId }
    }

    suspend fun declineChallenge(challengeId: String): Boolean = withContext(Dispatchers.IO) {
        _myChallenges.value = _myChallenges.value.map { ch ->
            if (ch.challengeId == challengeId) ch.copy(status = ChallengeStatus.DECLINED) else ch
        }
        if (firestore != null) {
            try {
                firestore.collection("challenges").document(challengeId).update("status", ChallengeStatus.DECLINED.name).await()
            } catch (e: Exception) {
                Log.w(tag, "Firestore decline challenge error: ${e.message}")
            }
        }
        return@withContext true
    }

    suspend fun startChallenge(challengeId: String): FriendChallengeRecord? = withContext(Dispatchers.IO) {
        val existing = _myChallenges.value.firstOrNull { it.challengeId == challengeId } ?: return@withContext null
        val updated = existing.copy(
            status = ChallengeStatus.IN_PROGRESS,
            startedAt = System.currentTimeMillis()
        )
        _myChallenges.value = _myChallenges.value.map { if (it.challengeId == challengeId) updated else it }
        if (firestore != null) {
            try {
                firestore.collection("challenges").document(challengeId).update(
                    "status", ChallengeStatus.IN_PROGRESS.name,
                    "startedAt", updated.startedAt
                ).await()
            } catch (e: Exception) {
                Log.w(tag, "Firestore startChallenge error: ${e.message}")
            }
        }
        return@withContext updated
    }

    suspend fun submitChallengeResult(
        challengeId: String,
        score: Int,
        accuracy: Int,
        timeSpentSeconds: Int
    ): FriendChallengeRecord? = withContext(Dispatchers.IO) {
        val currentUser = currentProfileProvider()
        val existing = _myChallenges.value.firstOrNull { it.challengeId == challengeId } ?: return@withContext null
        val isCreator = existing.creatorId == currentUser.uid || existing.creatorName == currentUser.username
        val result = MatchPlayerResult(
            score = score,
            accuracy = accuracy,
            durationSeconds = timeSpentSeconds,
            completedAt = System.currentTimeMillis()
        )
        submitChallengeResult(challengeId, isCreator, result)
    }

    suspend fun submitChallengeResult(
        challengeId: String,
        isCreator: Boolean,
        result: MatchPlayerResult
    ): FriendChallengeRecord? = withContext(Dispatchers.IO) {
        val currentUser = currentProfileProvider()
        val currentUid = currentUser.uid

        val existing = _myChallenges.value.firstOrNull { it.challengeId == challengeId } ?: return@withContext null

        val updatedCreatorResult = if (isCreator) result else existing.creatorResult
        val updatedOpponentResult = if (!isCreator) result else existing.opponentResult

        // Check if both players have now submitted results
        val isBothCompleted = updatedCreatorResult != null && updatedOpponentResult != null

        var winnerId: String? = existing.winnerId
        var challengeStatus = existing.status

        if (isBothCompleted) {
            challengeStatus = ChallengeStatus.COMPLETED
            val cRes = updatedCreatorResult!!
            val oRes = updatedOpponentResult!!

            // Fair Tiebreaker Rules:
            // 1. Higher score
            // 2. Higher accuracy
            // 3. Faster completion time
            // 4. Draw
            winnerId = when {
                cRes.score > oRes.score -> existing.creatorId
                oRes.score > cRes.score -> existing.opponentId
                cRes.accuracy > oRes.accuracy -> existing.creatorId
                oRes.accuracy > cRes.accuracy -> existing.opponentId
                cRes.durationSeconds < oRes.durationSeconds -> existing.creatorId
                oRes.durationSeconds < cRes.durationSeconds -> existing.opponentId
                else -> "draw"
            }

            // Award XP to current player
            val isUserWinner = winnerId == currentUid
            val isDraw = winnerId == "draw"
            val xpToAward = 150 + (if (isUserWinner) 100 else if (isDraw) 50 else 0)
            onRewardXp(xpToAward)

            // Update stats
            recordCompletedChallengeStats(isWin = isUserWinner, isDraw = isDraw, score = result.score)

            // Social Achievements Evaluation
            onAchievementUnlocked("FIRST_CHALLENGE")
            val currentWins = _challengeStats.value.wins
            if (currentWins >= 5) onAchievementUnlocked("CHALLENGE_WIN_5")
            if (currentWins >= 10) onAchievementUnlocked("CHALLENGE_WIN_10")
            if (_challengeStats.value.totalPlayed >= 10) onAchievementUnlocked("SOCIAL_BRAIN")

            val opponentId = if (isCreator) existing.opponentId else existing.creatorId
            val matchesAgainstRival = _myChallenges.value.count {
                it.status == ChallengeStatus.COMPLETED &&
                ((it.creatorId == opponentId && it.opponentId == currentUid) || (it.creatorId == currentUid && it.opponentId == opponentId))
            }
            if (matchesAgainstRival >= 2) {
                onAchievementUnlocked("RIVAL")
            }

            // Opponent name
            val opponentName = if (isCreator) existing.opponentName else existing.creatorName

            // Record activity feed
            addActivityItem(
                FriendActivityItem(
                    userId = currentUid,
                    username = currentUser.username,
                    avatarEmoji = currentUser.avatarEmoji,
                    activityType = FriendActivityType.CHALLENGE_WIN,
                    title = if (isUserWinner) "Victory over $opponentName!" else if (isDraw) "Draw against $opponentName!" else "Battle completed!",
                    description = "Scored ${result.score} pts with ${result.accuracy}% accuracy in ${existing.category.uppercase(Locale.ROOT)}!"
                )
            )

            // Notification
            val notifTitle = if (isUserWinner) "🏆 You Won the Challenge!" else if (isDraw) "🤝 Challenge Tied!" else "Match Completed"
            onNotificationCreated(
                AppNotification(
                    id = "notif_mc_${System.currentTimeMillis()}",
                    title = notifTitle,
                    message = "Final result available for match with $opponentName!",
                    timeAgo = "Just now",
                    iconEmoji = if (isUserWinner) "🏆" else "⚔️",
                    type = NotificationType.MATCH_COMPLETED,
                    actionData = challengeId
                )
            )
        } else {
            // One player finished, waiting for the other
            challengeStatus = ChallengeStatus.IN_PROGRESS
        }

        val updatedRecord = existing.copy(
            creatorResult = updatedCreatorResult,
            opponentResult = updatedOpponentResult,
            status = challengeStatus,
            winnerId = winnerId,
            completedAt = if (isBothCompleted) System.currentTimeMillis() else existing.completedAt
        )

        _myChallenges.value = _myChallenges.value.map { if (it.challengeId == challengeId) updatedRecord else it }

        // Firestore update
        if (firestore != null) {
            try {
                val data = mutableMapOf<String, Any>(
                    "status" to updatedRecord.status.name
                )
                if (updatedCreatorResult != null) {
                    data["creatorResult"] = mapOf(
                        "score" to updatedCreatorResult.score,
                        "correctCount" to updatedCreatorResult.correctCount,
                        "totalQuestions" to updatedCreatorResult.totalQuestions,
                        "accuracy" to updatedCreatorResult.accuracy,
                        "durationSeconds" to updatedCreatorResult.durationSeconds,
                        "completedAt" to updatedCreatorResult.completedAt,
                        "xpEarned" to updatedCreatorResult.xpEarned
                    )
                }
                if (updatedOpponentResult != null) {
                    data["opponentResult"] = mapOf(
                        "score" to updatedOpponentResult.score,
                        "correctCount" to updatedOpponentResult.correctCount,
                        "totalQuestions" to updatedOpponentResult.totalQuestions,
                        "accuracy" to updatedOpponentResult.accuracy,
                        "durationSeconds" to updatedOpponentResult.durationSeconds,
                        "completedAt" to updatedOpponentResult.completedAt,
                        "xpEarned" to updatedOpponentResult.xpEarned
                    )
                }
                if (winnerId != null) data["winnerId"] = winnerId
                if (updatedRecord.completedAt != null) data["completedAt"] = updatedRecord.completedAt!!

                firestore.collection("challenges").document(challengeId).set(data, SetOptions.merge()).await()
            } catch (e: Exception) {
                Log.w(tag, "Firestore submit result error: ${e.message}")
            }
        }

        return@withContext updatedRecord
    }

    private fun recordCompletedChallengeStats(isWin: Boolean, isDraw: Boolean, score: Int) {
        val current = _challengeStats.value
        val newPlayed = current.totalPlayed + 1
        val newWins = current.wins + (if (isWin) 1 else 0)
        val newLosses = current.losses + (if (!isWin && !isDraw) 1 else 0)
        val newDraws = current.draws + (if (isDraw) 1 else 0)
        val newRate = if (newPlayed > 0) (newWins * 100) / newPlayed else 0
        val newBest = maxOf(current.bestScore, score)
        val newStreak = if (isWin) current.currentWinStreak + 1 else 0

        val newStats = ChallengeStatistics(
            totalPlayed = newPlayed,
            wins = newWins,
            losses = newLosses,
            draws = newDraws,
            winRatePercentage = newRate,
            bestScore = newBest,
            currentWinStreak = newStreak
        )
        _challengeStats.value = newStats
        prefs?.edit()?.apply {
            putInt("cs_played", newPlayed)
            putInt("cs_wins", newWins)
            putInt("cs_losses", newLosses)
            putInt("cs_draws", newDraws)
            putInt("cs_best_score", newBest)
            putInt("cs_streak", newStreak)
            apply()
        }
    }

    suspend fun createRematchChallenge(
        previousChallenge: FriendChallengeRecord,
        newQuestionIds: List<String> = emptyList()
    ): FriendChallengeRecord = createRematch(previousChallenge, newQuestionIds)

    suspend fun createRematch(
        previousChallenge: FriendChallengeRecord,
        newQuestionIds: List<String>
    ): FriendChallengeRecord {
        val currentUser = currentProfileProvider()
        val opponentId = if (previousChallenge.creatorId == currentUser.uid) previousChallenge.opponentId else previousChallenge.creatorId
        val opponentName = if (previousChallenge.creatorId == currentUser.uid) previousChallenge.opponentName else previousChallenge.creatorName
        val opponentAvatar = if (previousChallenge.creatorId == currentUser.uid) previousChallenge.opponentAvatar else previousChallenge.creatorAvatar

        return createChallenge(
            opponentId = opponentId,
            opponentName = opponentName,
            opponentAvatar = opponentAvatar,
            category = previousChallenge.category,
            gameMode = previousChallenge.gameMode,
            difficulty = previousChallenge.difficulty,
            questionCount = previousChallenge.questionCount,
            timeLimitSeconds = previousChallenge.timeLimitSeconds,
            questionIds = newQuestionIds
        )
    }

    fun addActivityItem(item: FriendActivityItem) {
        if (!_privacySettings.value.showActivity) return
        val list = _friendActivity.value.toMutableList()
        list.add(0, item)
        if (list.size > 20) list.removeAt(list.size - 1)
        _friendActivity.value = list
    }

    fun getFriendsLeaderboardEntries(currentUser: UserProfile): LeaderboardQueryResult {
        val currentEntry = LeaderboardEntry(
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

        val friendEntries = _friends.value.map { f ->
            LeaderboardEntry(
                rank = 0,
                username = f.username,
                avatarEmoji = f.avatarEmoji,
                score = f.bestScore,
                level = f.level,
                countryFlag = f.countryFlag,
                isCurrentUser = false,
                userId = f.userId,
                xp = f.level * 250,
                country = f.country,
                playerTitle = f.playerTitle
            )
        }

        if (_friends.value.isEmpty()) {
            return LeaderboardQueryResult(
                entries = if (currentUser.bestScore > 0) listOf(currentEntry) else emptyList(),
                currentUserEntry = if (currentUser.bestScore > 0) currentEntry else null,
                totalCount = if (currentUser.bestScore > 0) 1 else 0,
                periodId = "all_time_friends",
                emptyMessage = "No friends to rank yet."
            )
        }

        val allEntries = (friendEntries + currentEntry)
            .sortedByDescending { it.score }
            .mapIndexed { idx, entry -> entry.copy(rank = idx + 1) }

        val userRank = allEntries.indexOfFirst { it.isCurrentUser } + 1
        val finalUserEntry = if (userRank > 0) allEntries[userRank - 1] else currentEntry

        return LeaderboardQueryResult(
            entries = allEntries,
            currentUserEntry = finalUserEntry,
            totalCount = allEntries.size,
            periodId = "all_time_friends",
            emptyMessage = null
        )
    }

    private fun docToChallengeRecord(id: String, data: Map<String, Any>): FriendChallengeRecord? {
        val code = data["challengeCode"] as? String ?: return null
        val crMap = data["creatorResult"] as? Map<*, *>
        val opMap = data["opponentResult"] as? Map<*, *>

        val crResult = if (crMap != null) {
            MatchPlayerResult(
                score = (crMap["score"] as? Long)?.toInt() ?: 0,
                correctCount = (crMap["correctCount"] as? Long)?.toInt() ?: 0,
                totalQuestions = (crMap["totalQuestions"] as? Long)?.toInt() ?: 0,
                accuracy = (crMap["accuracy"] as? Long)?.toInt() ?: 0,
                durationSeconds = (crMap["durationSeconds"] as? Long)?.toInt() ?: 0,
                completedAt = (crMap["completedAt"] as? Long) ?: 0L,
                xpEarned = (crMap["xpEarned"] as? Long)?.toInt() ?: 0
            )
        } else null

        val opResult = if (opMap != null) {
            MatchPlayerResult(
                score = (opMap["score"] as? Long)?.toInt() ?: 0,
                correctCount = (opMap["correctCount"] as? Long)?.toInt() ?: 0,
                totalQuestions = (opMap["totalQuestions"] as? Long)?.toInt() ?: 0,
                accuracy = (opMap["accuracy"] as? Long)?.toInt() ?: 0,
                durationSeconds = (opMap["durationSeconds"] as? Long)?.toInt() ?: 0,
                completedAt = (opMap["completedAt"] as? Long) ?: 0L,
                xpEarned = (opMap["xpEarned"] as? Long)?.toInt() ?: 0
            )
        } else null

        val statusName = data["status"] as? String ?: ChallengeStatus.SENT.name
        val status = try { ChallengeStatus.valueOf(statusName) } catch (e: Exception) { ChallengeStatus.SENT }

        @Suppress("UNCHECKED_CAST")
        val qIds = (data["questionIds"] as? List<String>) ?: emptyList()

        return FriendChallengeRecord(
            challengeId = id,
            challengeCode = code,
            creatorId = data["creatorId"] as? String ?: "",
            creatorName = data["creatorName"] as? String ?: "Challenger",
            creatorAvatar = data["creatorAvatar"] as? String ?: "🧠",
            opponentId = data["opponentId"] as? String ?: "",
            opponentName = data["opponentName"] as? String ?: "",
            opponentAvatar = data["opponentAvatar"] as? String ?: "👥",
            category = data["category"] as? String ?: GameCategory.MATH.id,
            gameMode = data["gameMode"] as? String ?: "friend_challenge",
            difficulty = data["difficulty"] as? String ?: Difficulty.MEDIUM.name,
            questionCount = (data["questionCount"] as? Long)?.toInt() ?: 5,
            timeLimitSeconds = (data["timeLimitSeconds"] as? Long)?.toInt() ?: 60,
            rules = data["rules"] as? String ?: "Standard Battle: Highest score wins.",
            status = status,
            createdAt = (data["createdAt"] as? Long) ?: System.currentTimeMillis(),
            acceptedAt = data["acceptedAt"] as? Long,
            startedAt = data["startedAt"] as? Long,
            completedAt = data["completedAt"] as? Long,
            expiresAt = (data["expiresAt"] as? Long) ?: (System.currentTimeMillis() + 24 * 3600 * 1000L),
            questionSetId = data["questionSetId"] as? String ?: "qset",
            questionIds = qIds,
            creatorResult = crResult,
            opponentResult = opResult,
            winnerId = data["winnerId"] as? String,
            version = (data["version"] as? Long)?.toInt() ?: 1
        )
    }
}
