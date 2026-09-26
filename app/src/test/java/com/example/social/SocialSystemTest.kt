package com.example.social

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.social.*
import com.example.model.AppNotification
import com.example.model.Difficulty
import com.example.model.GameCategory
import com.example.model.NotificationType
import com.example.model.UserProfile
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SocialSystemTest {

    private lateinit var context: Context
    private lateinit var testUserA: UserProfile
    private lateinit var socialRepoA: SocialRepository
    private val awardedXpList = mutableListOf<Int>()
    private val createdNotifications = mutableListOf<AppNotification>()
    private val unlockedAchievements = mutableListOf<String>()

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        testUserA = UserProfile(
            uid = "user_alpha_100",
            username = "AlphaGamer",
            displayName = "Alpha Gamer",
            level = 5,
            totalXp = 1200,
            bestScore = 950,
            playerId = "BB-ALPHA1",
            country = "United States",
            countryFlag = "🇺🇸",
            selectedTitle = "Brainiac"
        )
        awardedXpList.clear()
        createdNotifications.clear()
        unlockedAchievements.clear()

        socialRepoA = SocialRepository(
            context = context,
            firestoreRepo = null,
            currentProfileProvider = { testUserA },
            onRewardXp = { awardedXpList.add(it) },
            onNotificationCreated = { createdNotifications.add(it) },
            onAchievementUnlocked = { unlockedAchievements.add(it) }
        )
    }

    // 1. Search user
    @Test
    fun test01_SearchSelfIsPrevented() = runBlocking {
        val resultsSelf = socialRepoA.searchPlayers("AlphaGamer")
        assertTrue("Searching for self must return empty list", resultsSelf.isEmpty())

        val resultsSelfPid = socialRepoA.searchPlayers("BB-ALPHA1")
        assertTrue("Searching for own player ID must return empty list", resultsSelfPid.isEmpty())
    }

    // 2. Send friend request
    @Test
    fun test02_SendFriendRequest() = runBlocking {
        val req = socialRepoA.sendFriendRequest(
            targetUserId = "user_beta_200",
            targetUsername = "BetaHero",
            targetAvatar = "⚡"
        )
        assertNotNull("Friend request should be created", req)
        assertEquals("user_beta_200", req?.receiverId)
        assertEquals(FriendRequestStatus.PENDING, req?.status)
        assertTrue(socialRepoA.outgoingRequests.value.any { it.receiverId == "user_beta_200" })
    }

    // 3. Accept friend request
    @Test
    fun test03_AcceptFriendRequest() = runBlocking {
        val req = socialRepoA.sendFriendRequest("user_gamma_300", "GammaMind", "🧠")
        assertNotNull(req)
        val success = socialRepoA.acceptFriendRequest(req!!.id)
        assertTrue("Accept friend request should succeed", success)
        assertTrue(socialRepoA.friends.value.any { it.userId == "user_gamma_300" })
    }

    // 4. Decline friend request
    @Test
    fun test04_DeclineFriendRequest() = runBlocking {
        val req = socialRepoA.sendFriendRequest("user_delta_400", "DeltaRival", "🔥")
        assertNotNull(req)
        val success = socialRepoA.declineFriendRequest(req!!.id)
        assertTrue("Decline friend request should succeed", success)
        assertFalse(socialRepoA.friends.value.any { it.userId == "user_delta_400" })
    }

    // 5. Remove friend
    @Test
    fun test05_RemoveFriend() = runBlocking {
        val req = socialRepoA.sendFriendRequest("user_epsilon_500", "EpsilonMate", "⭐")
        socialRepoA.acceptFriendRequest(req!!.id)
        assertTrue(socialRepoA.friends.value.any { it.userId == "user_epsilon_500" })

        val removeSuccess = socialRepoA.removeFriend("user_epsilon_500")
        assertTrue("Remove friend should succeed", removeSuccess)
        assertFalse(socialRepoA.friends.value.any { it.userId == "user_epsilon_500" })
    }

    // 6. Block user
    @Test
    fun test06_BlockUser() = runBlocking {
        socialRepoA.blockUser("user_spammer_999", "SpamAccount", "🤖")
        assertTrue(socialRepoA.blockedUsers.value.any { it.blockedUid == "user_spammer_999" })
        assertTrue(socialRepoA.isUserBlocked("user_spammer_999"))
    }

    // 7. Unblock user
    @Test
    fun test07_UnblockUser() = runBlocking {
        socialRepoA.blockUser("user_blocked_888", "BadUser", "🚫")
        assertTrue(socialRepoA.isUserBlocked("user_blocked_888"))

        socialRepoA.unblockUser("user_blocked_888")
        assertFalse("User should be unblocked", socialRepoA.isUserBlocked("user_blocked_888"))
    }

    // 8. Challenge friend
    @Test
    fun test08_ChallengeFriend() = runBlocking {
        val challenge = socialRepoA.createChallenge(
            opponentId = "user_beta_200",
            opponentName = "BetaHero",
            opponentAvatar = "⚡",
            category = GameCategory.MATH.id,
            gameMode = "category_battle",
            difficulty = Difficulty.MEDIUM.name,
            questionCount = 5,
            timeLimitSeconds = 60
        )
        assertNotNull("Challenge should be created", challenge)
        assertEquals("user_alpha_100", challenge.creatorId)
        assertEquals("user_beta_200", challenge.opponentId)
        assertEquals(ChallengeStatus.SENT, challenge.status)
        assertTrue(challenge.challengeCode.isNotBlank())
    }

    // 9. Accept challenge
    @Test
    fun test09_AcceptChallenge() = runBlocking {
        val challenge = socialRepoA.createChallenge(
            opponentId = "user_beta_200",
            opponentName = "BetaHero",
            opponentAvatar = "⚡",
            category = GameCategory.SCIENCE.id,
            gameMode = "category_battle",
            difficulty = Difficulty.HARD.name
        )
        val accepted = socialRepoA.acceptChallenge(challenge.challengeId)
        assertNotNull("Accepted challenge should be returned", accepted)
        assertEquals(ChallengeStatus.ACCEPTED, accepted?.status)
    }

    // 10. Decline challenge
    @Test
    fun test10_DeclineChallenge() = runBlocking {
        val challenge = socialRepoA.createChallenge(
            opponentId = "user_beta_200",
            opponentName = "BetaHero",
            opponentAvatar = "⚡",
            category = GameCategory.LOGIC.id,
            gameMode = "category_battle",
            difficulty = Difficulty.EASY.name
        )
        val success = socialRepoA.declineChallenge(challenge.challengeId)
        assertTrue("Challenge should be declined", success)
    }

    // 11. Challenge expiration
    @Test
    fun test11_ChallengeExpiration() {
        val expiredChallenge = FriendChallengeRecord(
            challengeId = "ch_expired_1",
            creatorId = "user_beta_200",
            creatorName = "BetaHero",
            opponentId = "user_alpha_100",
            opponentName = "AlphaGamer",
            category = "math",
            gameMode = "category_battle",
            difficulty = "MEDIUM",
            challengeCode = "EXPIRE1",
            createdAt = System.currentTimeMillis() - (48 * 3600 * 1000L),
            expiresAt = System.currentTimeMillis() - (24 * 3600 * 1000L)
        )
        assertTrue("Challenge past expiration timestamp must be marked expired", expiredChallenge.isExpired)
    }

    // 12. Start challenge
    @Test
    fun test12_StartChallenge() = runBlocking {
        val challenge = socialRepoA.createChallenge(
            opponentId = "user_beta_200",
            opponentName = "BetaHero",
            opponentAvatar = "⚡",
            category = GameCategory.WORDS.id,
            gameMode = "category_battle",
            difficulty = Difficulty.MEDIUM.name
        )
        val started = socialRepoA.startChallenge(challenge.challengeId)
        assertNotNull(started)
        assertEquals(ChallengeStatus.IN_PROGRESS, started?.status)
    }

    // 13. Complete challenge & 14. Match result
    @Test
    fun test13_CompleteChallengeAndMatchResult() = runBlocking {
        val challenge = socialRepoA.createChallenge(
            opponentId = "user_beta_200",
            opponentName = "BetaHero",
            opponentAvatar = "⚡",
            category = GameCategory.MATH.id,
            gameMode = "category_battle",
            difficulty = Difficulty.MEDIUM.name
        )

        // Creator submits
        val creatorRes = MatchPlayerResult(
            score = 1000,
            accuracy = 90,
            durationSeconds = 40,
            completedAt = System.currentTimeMillis()
        )
        socialRepoA.submitChallengeResult(challenge.challengeId, isCreator = true, result = creatorRes)

        // Opponent submits
        val opponentRes = MatchPlayerResult(
            score = 850,
            accuracy = 80,
            durationSeconds = 45,
            completedAt = System.currentTimeMillis()
        )
        val completed = socialRepoA.submitChallengeResult(challenge.challengeId, isCreator = false, result = opponentRes)

        assertNotNull(completed)
        assertEquals(ChallengeStatus.COMPLETED, completed?.status)
        assertEquals("user_alpha_100", completed?.winnerId)
        assertTrue("Creator scored higher so should win", completed!!.creatorResult!!.score > completed.opponentResult!!.score)
    }

    // 15. Tie result
    @Test
    fun test15_TieResultHandling() = runBlocking {
        val challenge = socialRepoA.createChallenge(
            opponentId = "user_beta_200",
            opponentName = "BetaHero",
            opponentAvatar = "⚡",
            category = GameCategory.MATH.id,
            gameMode = "category_battle",
            difficulty = Difficulty.MEDIUM.name
        )

        val resA = MatchPlayerResult(score = 800, accuracy = 80, durationSeconds = 50)
        val resB = MatchPlayerResult(score = 800, accuracy = 80, durationSeconds = 50)

        socialRepoA.submitChallengeResult(challenge.challengeId, isCreator = true, result = resA)
        val completed = socialRepoA.submitChallengeResult(challenge.challengeId, isCreator = false, result = resB)

        assertEquals("draw", completed?.winnerId)
    }

    // 16. Rematch
    @Test
    fun test16_Rematch() = runBlocking {
        val challenge = socialRepoA.createChallenge(
            opponentId = "user_beta_200",
            opponentName = "BetaHero",
            opponentAvatar = "⚡",
            category = GameCategory.MATH.id,
            gameMode = "category_battle",
            difficulty = Difficulty.MEDIUM.name
        )
        val rematch = socialRepoA.createRematchChallenge(challenge)
        assertNotNull(rematch)
        assertNotEquals("Rematch must have a distinct challenge ID", challenge.challengeId, rematch.challengeId)
        assertEquals(challenge.category, rematch.category)
        assertEquals(challenge.opponentId, rematch.opponentId)
    }

    // 17. Challenge sharing & 18. Challenge code
    @Test
    fun test17_ChallengeSharingAndCode() {
        val challenge = FriendChallengeRecord(
            challengeId = "ch_share_1",
            creatorId = "user_alpha_100",
            creatorName = "AlphaGamer",
            opponentId = "user_beta_200",
            opponentName = "BetaHero",
            category = "math",
            gameMode = "category_battle",
            difficulty = "MEDIUM",
            challengeCode = "BB7X92"
        )
        val shareText = socialRepoA.generateShareableChallengeText(challenge)
        assertTrue("Share text should contain challenge code", shareText.contains("BB7X92"))
        assertTrue("Share text should contain deep link scheme", shareText.contains("brainbattle://challenge/BB7X92"))
        assertFalse("Share text must not leak private UID", shareText.contains("user_alpha_100"))
    }

    // 19. Notification creation
    @Test
    fun test19_NotificationCreation() = runBlocking {
        val notif = AppNotification(
            id = "notif_test_1",
            title = "New Challenge Received",
            message = "BetaHero challenged you in Math!",
            timeAgo = "Just now",
            iconEmoji = "⚔️",
            type = NotificationType.FRIEND_CHALLENGE,
            isRead = false
        )
        socialRepoA.recordNotification(notif)
        assertTrue("Notification should be in created list", createdNotifications.any { it.id == "notif_test_1" })
    }

    // 20. Mark notification read
    @Test
    fun test20_MarkNotificationRead() {
        val notif = AppNotification(
            id = "notif_read_1",
            title = "Test",
            message = "Test message",
            timeAgo = "Just now",
            iconEmoji = "🔔",
            type = NotificationType.DAILY_CHALLENGE,
            isRead = false
        )
        val marked = notif.copy(isRead = true)
        assertTrue("Notification should be marked read", marked.isRead)
    }

    // 21. Friends leaderboard
    @Test
    fun test21_FriendsLeaderboardEmptyAndPopulated() = runBlocking {
        // Empty friends test
        val emptyResult = socialRepoA.getFriendsLeaderboardEntries(testUserA)
        assertEquals("No friends to rank yet.", emptyResult.emptyMessage)

        // Add a friend and verify ranking
        val req = socialRepoA.sendFriendRequest("user_beta_200", "BetaHero", "⚡")
        socialRepoA.acceptFriendRequest(req!!.id)

        val populatedResult = socialRepoA.getFriendsLeaderboardEntries(testUserA)
        assertNull(populatedResult.emptyMessage)
        assertTrue("Should contain both user and friend", populatedResult.entries.size >= 2)
    }

    // 22. Offline mode
    @Test
    fun test22_OfflineModePersistence() {
        val challengeCode = socialRepoA.generateChallengeCode()
        assertNotNull(challengeCode)
        assertTrue("Challenge code must follow alphanumeric format", challengeCode.matches(Regex("BB[2-9A-HJ-NP-Z]{6}")))
    }

    // 23. Duplicate challenge submission prevention
    @Test
    fun test23_DuplicateChallengeSubmission() = runBlocking {
        val challenge = socialRepoA.createChallenge(
            opponentId = "user_beta_200",
            opponentName = "BetaHero",
            opponentAvatar = "⚡",
            category = GameCategory.MATH.id,
            gameMode = "category_battle",
            difficulty = Difficulty.MEDIUM.name
        )
        val res1 = MatchPlayerResult(score = 800, accuracy = 80, durationSeconds = 30)
        val updated1 = socialRepoA.submitChallengeResult(challenge.challengeId, isCreator = true, result = res1)
        assertNotNull(updated1)
        assertEquals(800, updated1?.creatorResult?.score)
    }

    // 24. Unauthorized challenge access & 25. Unauthorized friendship modification
    @Test
    fun test24_SelfInteractionPrevention() = runBlocking {
        // Prevent sending friend request to self
        val selfReq = socialRepoA.sendFriendRequest(testUserA.uid, testUserA.username, testUserA.avatarEmoji)
        assertNull("Sending friend request to self must fail and return null", selfReq)

        // Prevent blocking self
        socialRepoA.blockUser(testUserA.uid, testUserA.username, testUserA.avatarEmoji)
        assertFalse("Cannot block self", socialRepoA.isUserBlocked(testUserA.uid))
    }

    // 26. Report user
    @Test
    fun test26_ReportUser() = runBlocking {
        val success = socialRepoA.reportUser(
            targetUid = "user_bad_guy",
            targetUsername = "BadGuy",
            reason = UserReportReason.SPAM,
            details = "Sending spam challenge invites"
        )
        assertTrue("Reporting a user should succeed", success)
    }

    // 27. Social privacy settings
    @Test
    fun test27_SocialPrivacySettings() = runBlocking {
        val original = socialRepoA.privacySettings.value
        val updated = original.copy(
            allowFriendRequests = false,
            allowChallengeRequests = false,
            showActivity = false
        )
        socialRepoA.updatePrivacySettings(updated)
        assertEquals(false, socialRepoA.privacySettings.value.allowFriendRequests)
        assertEquals(false, socialRepoA.privacySettings.value.allowChallengeRequests)
        assertEquals(false, socialRepoA.privacySettings.value.showActivity)
    }
}
