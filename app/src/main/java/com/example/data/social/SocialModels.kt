package com.example.data.social

import com.example.model.Difficulty
import com.example.model.GameCategory

enum class OnlineStatus(val label: String, val badgeColor: Long) {
    ONLINE("Online", 0xFF00E676),
    PLAYING("In Match", 0xFFFFD700),
    OFFLINE("Offline", 0xFF8A99AD)
}

data class Friend(
    val userId: String,
    val username: String,
    val displayName: String = username,
    val avatarEmoji: String = "🧠",
    val level: Int = 1,
    val playerTitle: String = "Rookie",
    val playerId: String = "",
    val status: OnlineStatus = OnlineStatus.ONLINE,
    val addedAt: Long = System.currentTimeMillis(),
    val country: String = "Global",
    val countryFlag: String = "🌐",
    val bestScore: Int = 0,
    val challengesPlayed: Int = 0,
    val challengesWon: Int = 0
)

enum class FriendRequestStatus {
    PENDING,
    ACCEPTED,
    DECLINED,
    CANCELLED,
    BLOCKED
}

data class FriendRequest(
    val id: String,
    val senderId: String,
    val senderUsername: String,
    val senderPlayerId: String,
    val senderAvatarEmoji: String = "🧠",
    val senderLevel: Int = 1,
    val senderTitle: String = "Rookie",
    val receiverId: String,
    val receiverUsername: String = "",
    val status: FriendRequestStatus = FriendRequestStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis()
)

data class BlockedUser(
    val blockedUid: String,
    val username: String,
    val avatarEmoji: String = "👤",
    val blockedAt: Long = System.currentTimeMillis()
)

data class SocialPrivacySettings(
    val allowFriendRequests: Boolean = true,
    val allowChallengeRequests: Boolean = true,
    val showActivity: Boolean = true,
    val showCountry: Boolean = true,
    val showProfileStatistics: Boolean = true
)

data class PlayerPublicProfile(
    val uid: String,
    val playerId: String,
    val username: String,
    val displayName: String = username,
    val avatarEmoji: String = "🧠",
    val level: Int = 1,
    val totalXp: Int = 0,
    val playerTitle: String = "Rookie",
    val country: String = "Global",
    val countryFlag: String = "🌐",
    val bestScore: Int = 0,
    val totalGamesPlayed: Int = 0,
    val accuracyPercentage: Int = 0,
    val achievementsCount: Int = 0,
    val globalRank: Int? = null,
    val challengesPlayed: Int = 0,
    val challengesWon: Int = 0,
    val challengesLost: Int = 0,
    val challengesDraw: Int = 0,
    val isFriend: Boolean = false,
    val isBlocked: Boolean = false,
    val hasPendingRequest: Boolean = false,
    val privacySettings: SocialPrivacySettings = SocialPrivacySettings()
)

enum class ChallengeStatus {
    CREATED,
    SENT,
    ACCEPTED,
    DECLINED,
    EXPIRED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED,
    ABANDONED
}

data class MatchPlayerResult(
    val score: Int = 0,
    val correctCount: Int = 0,
    val totalQuestions: Int = 0,
    val accuracy: Int = 0,
    val durationSeconds: Int = 0,
    val completedAt: Long = System.currentTimeMillis(),
    val xpEarned: Int = 0
)

data class FriendChallengeRecord(
    val challengeId: String,
    val challengeCode: String,
    val creatorId: String,
    val creatorName: String,
    val creatorAvatar: String = "🧠",
    val opponentId: String = "",
    val opponentName: String = "",
    val opponentAvatar: String = "👥",
    val category: String = GameCategory.MATH.id,
    val gameMode: String = "friend_challenge",
    val difficulty: String = Difficulty.MEDIUM.name,
    val questionCount: Int = 5,
    val timeLimitSeconds: Int = 60,
    val rules: String = "Standard Battle: Highest score wins. Tiebreaker: Accuracy, then speed.",
    val status: ChallengeStatus = ChallengeStatus.SENT,
    val createdAt: Long = System.currentTimeMillis(),
    val acceptedAt: Long? = null,
    val startedAt: Long? = null,
    val completedAt: Long? = null,
    val expiresAt: Long = System.currentTimeMillis() + 24 * 60 * 60 * 1000L, // 24-hour expiration
    val questionSetId: String = "qset_${System.currentTimeMillis()}",
    val questionIds: List<String> = emptyList(),
    val creatorResult: MatchPlayerResult? = null,
    val opponentResult: MatchPlayerResult? = null,
    val winnerId: String? = null, // null if undecided, "draw" if tied, or winner's UID
    val version: Int = 1
) {
    val isExpired: Boolean get() = status == ChallengeStatus.EXPIRED || (status != ChallengeStatus.COMPLETED && System.currentTimeMillis() > expiresAt)
}

enum class UserReportReason(val label: String) {
    SPAM("Spam or unwanted advertising"),
    HARASSMENT("Harassment or bullying"),
    INAPPROPRIATE_PROFILE("Inappropriate username or avatar"),
    ABUSE("Cheating or abusive behavior"),
    OTHER("Other issue")
}

data class UserReport(
    val id: String = "rep_${System.currentTimeMillis()}",
    val reportedUserId: String,
    val reportedUsername: String,
    val reportedByUserId: String,
    val reason: UserReportReason,
    val details: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

enum class FriendActivityType {
    ACHIEVEMENT,
    PERSONAL_BEST,
    LEVEL_UP,
    CHALLENGE_WIN,
    MATCH_PLAYED
}

data class FriendActivityItem(
    val id: String = "act_${System.currentTimeMillis()}",
    val userId: String,
    val username: String,
    val avatarEmoji: String = "🧠",
    val activityType: FriendActivityType,
    val title: String,
    val description: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class ChallengeStatistics(
    val totalPlayed: Int = 0,
    val wins: Int = 0,
    val losses: Int = 0,
    val draws: Int = 0,
    val winRatePercentage: Int = 0,
    val bestScore: Int = 0,
    val currentWinStreak: Int = 0
)
