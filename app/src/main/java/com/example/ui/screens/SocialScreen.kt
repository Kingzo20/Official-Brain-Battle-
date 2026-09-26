package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.GameRepository
import com.example.data.social.*
import com.example.model.Difficulty
import com.example.model.GameCategory
import com.example.ui.components.*
import com.example.ui.theme.*
import kotlinx.coroutines.launch

enum class SocialTab(val title: String, val icon: String) {
    FRIENDS("Friends", "👥"),
    REQUESTS("Requests", "📩"),
    FIND("Find", "🔍"),
    ACTIVITY("Activity", "⚡"),
    BLOCKED("Blocked", "🚫")
}

@Composable
fun SocialScreen(
    repository: GameRepository,
    onStartChallengeGame: (challenge: FriendChallengeRecord) -> Unit,
    onViewMatchResult: (challengeId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val socialRepo = repository.socialRepository

    val friends by socialRepo.friends.collectAsState()
    val incomingRequests by socialRepo.incomingRequests.collectAsState()
    val outgoingRequests by socialRepo.outgoingRequests.collectAsState()
    val blockedUsers by socialRepo.blockedUsers.collectAsState()
    val activityFeed by socialRepo.friendActivity.collectAsState()
    val privacySettings by socialRepo.privacySettings.collectAsState()
    val myChallenges by socialRepo.myChallenges.collectAsState()
    val userProfile by repository.userProfile.collectAsState()

    var selectedTab by remember { mutableStateOf(SocialTab.FRIENDS) }

    // Dialog states
    var profileToView by remember { mutableStateOf<PlayerPublicProfile?>(null) }
    var showCreateChallengeDialog by remember { mutableStateOf(false) }
    var challengeTargetFriend by remember { mutableStateOf<Friend?>(null) }
    var createdChallengeToShare by remember { mutableStateOf<FriendChallengeRecord?>(null) }
    var showJoinCodeDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var userToReport by remember { mutableStateOf<Pair<String, String>?>(null) } // uid, username
    var friendToRemove by remember { mutableStateOf<Friend?>(null) }

    // Search state
    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    var searchResults by remember { mutableStateOf<List<PlayerPublicProfile>>(emptyList()) }
    var hasSearched by remember { mutableStateOf(false) }
    var bannerFeedback by remember { mutableStateOf<String?>(null) }

    val userPlayerId = remember(userProfile.playerId) {
        if (userProfile.playerId.isNotBlank()) userProfile.playerId else socialRepo.getPlayerId()
    }

    Scaffold(
        containerColor = BackgroundDark,
        modifier = modifier.testTag("social_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Top Header: Title and Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "SOCIAL HUB",
                        style = MaterialTheme.typography.displayMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Connect, challenge friends & compete head-to-head",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = { showJoinCodeDialog = true },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(CardSurface)
                            .border(1.dp, CardSurfaceBorder, CircleShape)
                            .size(42.dp)
                            .testTag("open_join_code_dialog_button")
                    ) {
                        Icon(Icons.Default.Pin, contentDescription = "Enter Code", tint = NeonCyan, modifier = Modifier.size(20.dp))
                    }

                    IconButton(
                        onClick = { showPrivacyDialog = true },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(CardSurface)
                            .border(1.dp, CardSurfaceBorder, CircleShape)
                            .size(42.dp)
                            .testTag("open_privacy_settings_button")
                    ) {
                        Icon(Icons.Default.Tune, contentDescription = "Privacy Settings", tint = TextPrimary, modifier = Modifier.size(20.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Unique Player ID Banner (Strictly does not expose UID or email)
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = CardSurface,
                border = BorderStroke(1.dp, CardSurfaceBorder),
                modifier = Modifier.fillMaxWidth().testTag("user_player_id_banner")
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(CardSurfaceElevated)
                                .border(1.dp, NeonCyan, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = userProfile.avatarEmoji, fontSize = 24.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = "YOUR PLAYER ID", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                            Text(
                                text = userPlayerId,
                                style = MaterialTheme.typography.titleMedium,
                                color = NeonCyan,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("Player ID", userPlayerId))
                                Toast.makeText(context, "Copied Player ID: $userPlayerId", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(36.dp).testTag("copy_player_id_button")
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy ID", tint = TextMuted, modifier = Modifier.size(18.dp))
                        }

                        IconButton(
                            onClick = {
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, "Add me on Brain Battle! My Player ID is: $userPlayerId")
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Share Player ID"))
                            },
                            modifier = Modifier.size(36.dp).testTag("share_player_id_button")
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Share ID", tint = NeonCyan, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }

            if (bannerFeedback != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = CardSurfaceElevated,
                    border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = bannerFeedback ?: "", style = MaterialTheme.typography.bodySmall, color = NeonCyan)
                        IconButton(onClick = { bannerFeedback = null }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, contentDescription = null, tint = TextMuted, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Navigation Tabs
            ScrollableTabRow(
                selectedTabIndex = selectedTab.ordinal,
                containerColor = Color.Transparent,
                divider = {},
                edgePadding = 0.dp,
                indicator = {}
            ) {
                SocialTab.values().forEach { tab ->
                    val isSelected = selectedTab == tab
                    val badgeCount = when (tab) {
                        SocialTab.REQUESTS -> incomingRequests.size
                        SocialTab.FRIENDS -> friends.size
                        else -> 0
                    }

                    Tab(
                        selected = isSelected,
                        onClick = { selectedTab = tab },
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) NeonCyan.copy(alpha = 0.15f) else CardSurface)
                            .border(1.dp, if (isSelected) NeonCyan else CardSurfaceBorder, RoundedCornerShape(12.dp))
                            .testTag("social_tab_${tab.name.lowercase()}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = tab.icon, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = tab.title,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isSelected) NeonCyan else TextSecondary,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                            if (badgeCount > 0) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = if (tab == SocialTab.REQUESTS) NeonAmber else CardSurfaceElevated,
                                    shape = CircleShape
                                ) {
                                    Text(
                                        text = "$badgeCount",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (tab == SocialTab.REQUESTS) BackgroundDark else TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Content Area based on Selected Tab
            when (selectedTab) {
                SocialTab.FRIENDS -> {
                    FriendsTabContent(
                        friends = friends,
                        activeChallenges = myChallenges,
                        onChallengeFriend = { friend ->
                            challengeTargetFriend = friend
                            showCreateChallengeDialog = true
                        },
                        onViewProfile = { friend ->
                            coroutineScope.launch {
                                val searchList = socialRepo.searchPlayers(friend.playerId)
                                val p = searchList.firstOrNull() ?: PlayerPublicProfile(
                                    uid = friend.userId,
                                    playerId = friend.playerId,
                                    username = friend.username,
                                    displayName = friend.displayName,
                                    avatarEmoji = friend.avatarEmoji,
                                    level = friend.level,
                                    playerTitle = friend.playerTitle,
                                    country = friend.country,
                                    countryFlag = friend.countryFlag,
                                    bestScore = friend.bestScore,
                                    challengesPlayed = friend.challengesPlayed,
                                    challengesWon = friend.challengesWon,
                                    isFriend = true
                                )
                                profileToView = p
                            }
                        },
                        onRemoveFriend = { friend -> friendToRemove = friend },
                        onBlockFriend = { friend ->
                            coroutineScope.launch {
                                socialRepo.blockUser(friend.userId, friend.username, friend.avatarEmoji)
                                bannerFeedback = "Blocked ${friend.username}"
                            }
                        },
                        onViewMatchResult = onViewMatchResult,
                        onCreateChallengeClick = {
                            challengeTargetFriend = null
                            showCreateChallengeDialog = true
                        }
                    )
                }

                SocialTab.REQUESTS -> {
                    RequestsTabContent(
                        incoming = incomingRequests,
                        outgoing = outgoingRequests,
                        onAccept = { req ->
                            coroutineScope.launch {
                                socialRepo.acceptFriendRequest(req)
                                bannerFeedback = "Accepted friend request from ${req.senderUsername}!"
                            }
                        },
                        onDecline = { reqId ->
                            coroutineScope.launch {
                                socialRepo.declineFriendRequest(reqId)
                                bannerFeedback = "Declined friend request"
                            }
                        },
                        onCancel = { reqId ->
                            coroutineScope.launch {
                                socialRepo.cancelFriendRequest(reqId)
                                bannerFeedback = "Cancelled friend request"
                            }
                        }
                    )
                }

                SocialTab.FIND -> {
                    FindPlayersTabContent(
                        searchQuery = searchQuery,
                        onQueryChange = { searchQuery = it },
                        isSearching = isSearching,
                        hasSearched = hasSearched,
                        searchResults = searchResults,
                        onPerformSearch = {
                            if (searchQuery.isNotBlank()) {
                                isSearching = true
                                hasSearched = true
                                coroutineScope.launch {
                                    val res = socialRepo.searchPlayers(searchQuery)
                                    searchResults = res
                                    isSearching = false
                                }
                            }
                        },
                        onSendFriendRequest = { target ->
                            coroutineScope.launch {
                                val result = socialRepo.sendFriendRequest(target)
                                when (result) {
                                    is SocialRepository.FriendRequestResult.Success -> {
                                        bannerFeedback = "Friend request sent to ${target.username}!"
                                        // Update local status
                                        searchResults = searchResults.map { if (it.uid == target.uid) it.copy(hasPendingRequest = true) else it }
                                    }
                                    is SocialRepository.FriendRequestResult.Error -> {
                                        bannerFeedback = result.message
                                    }
                                }
                            }
                        },
                        onViewProfile = { profileToView = it }
                    )
                }

                SocialTab.ACTIVITY -> {
                    ActivityFeedTabContent(activityFeed = activityFeed)
                }

                SocialTab.BLOCKED -> {
                    BlockedUsersTabContent(
                        blockedList = blockedUsers,
                        onUnblock = { blockedUser ->
                            coroutineScope.launch {
                                socialRepo.unblockUser(blockedUser.blockedUid)
                                bannerFeedback = "Unblocked ${blockedUser.username}"
                            }
                        }
                    )
                }
            }
        }
    }

    // ==========================================
    // DIALOGS & OVERLAYS
    // ==========================================

    if (profileToView != null) {
        val prof = profileToView!!
        PublicProfileDialog(
            profile = prof,
            onDismiss = { profileToView = null },
            onSendChallenge = {
                val friend = friends.firstOrNull { it.userId == prof.uid }
                profileToView = null
                challengeTargetFriend = friend
                showCreateChallengeDialog = true
            },
            onAddFriend = {
                coroutineScope.launch {
                    val result = socialRepo.sendFriendRequest(prof)
                    when (result) {
                        is SocialRepository.FriendRequestResult.Success -> {
                            bannerFeedback = "Friend request sent to ${prof.username}!"
                            profileToView = prof.copy(hasPendingRequest = true)
                        }
                        is SocialRepository.FriendRequestResult.Error -> {
                            bannerFeedback = result.message
                        }
                    }
                }
            },
            onRemoveFriend = {
                val friend = friends.firstOrNull { it.userId == prof.uid }
                if (friend != null) {
                    friendToRemove = friend
                    profileToView = null
                }
            },
            onBlockUser = {
                coroutineScope.launch {
                    socialRepo.blockUser(prof.uid, prof.username, prof.avatarEmoji)
                    bannerFeedback = "Blocked ${prof.username}"
                    profileToView = null
                }
            },
            onReportUser = {
                userToReport = Pair(prof.uid, prof.username)
                profileToView = null
            }
        )
    }

    if (showCreateChallengeDialog) {
        CreateChallengeDialog(
            friendsList = friends,
            selectedFriend = challengeTargetFriend,
            onDismiss = { showCreateChallengeDialog = false },
            onCreateChallenge = { targetFriend, category, difficulty, questionCount ->
                showCreateChallengeDialog = false
                coroutineScope.launch {
                    // Pull deterministic question set
                    val questions = repository.getQuestionsFor(category, difficulty).take(questionCount)
                    val qIds = questions.map { it.id }

                    val challenge = socialRepo.createChallenge(
                        opponentId = targetFriend?.userId ?: "",
                        opponentName = targetFriend?.username ?: "",
                        opponentAvatar = targetFriend?.avatarEmoji ?: "👥",
                        category = category.id,
                        difficulty = difficulty.name,
                        questionCount = questionCount,
                        questionIds = qIds
                    )
                    createdChallengeToShare = challenge
                }
            }
        )
    }

    if (createdChallengeToShare != null) {
        ShareChallengeDialog(
            challenge = createdChallengeToShare!!,
            onDismiss = { createdChallengeToShare = null },
            onStartMatchNow = {
                val chal = createdChallengeToShare!!
                createdChallengeToShare = null
                onStartChallengeGame(chal)
            }
        )
    }

    if (showJoinCodeDialog) {
        JoinChallengeCodeDialog(
            onDismiss = { showJoinCodeDialog = false },
            onJoinCode = { code ->
                showJoinCodeDialog = false
                coroutineScope.launch {
                    val challenge = socialRepo.getChallengeByCode(code)
                    if (challenge != null) {
                        if (challenge.isExpired) {
                            bannerFeedback = "Challenge $code has expired."
                        } else {
                            socialRepo.acceptChallenge(challenge.challengeId)
                            onStartChallengeGame(challenge)
                        }
                    } else {
                        bannerFeedback = "No challenge found with code: $code"
                    }
                }
            }
        )
    }

    if (showPrivacyDialog) {
        SocialPrivacyDialog(
            settings = privacySettings,
            onDismiss = { showPrivacyDialog = false },
            onSaveSettings = { updated ->
                showPrivacyDialog = false
                coroutineScope.launch {
                    socialRepo.updatePrivacySettings(updated)
                    bannerFeedback = "Privacy settings saved."
                }
            }
        )
    }

    if (userToReport != null) {
        val (targetUid, targetUsername) = userToReport!!
        ReportUserDialog(
            targetUsername = targetUsername,
            onDismiss = { userToReport = null },
            onSubmitReport = { reason, details ->
                userToReport = null
                coroutineScope.launch {
                    socialRepo.reportUser(targetUid, targetUsername, reason, details)
                    bannerFeedback = "Thank you. Your report has been submitted."
                }
            }
        )
    }

    if (friendToRemove != null) {
        val friend = friendToRemove!!
        AlertDialog(
            onDismissRequest = { friendToRemove = null },
            title = { Text("Remove Friend", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to remove ${friend.username} from your friends list?", color = TextSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        val fid = friend.userId
                        friendToRemove = null
                        coroutineScope.launch {
                            socialRepo.removeFriend(fid)
                            bannerFeedback = "Removed friend."
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonRed, contentColor = Color.White),
                    modifier = Modifier.testTag("confirm_remove_friend_button")
                ) {
                    Text("REMOVE")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { friendToRemove = null }) {
                    Text("CANCEL")
                }
            },
            containerColor = CardSurfaceElevated
        )
    }
}

// ==========================================
// SUB-TAB CONTENTS
// ==========================================

@Composable
private fun FriendsTabContent(
    friends: List<Friend>,
    activeChallenges: List<FriendChallengeRecord>,
    onChallengeFriend: (Friend) -> Unit,
    onViewProfile: (Friend) -> Unit,
    onRemoveFriend: (Friend) -> Unit,
    onBlockFriend: (Friend) -> Unit,
    onViewMatchResult: (String) -> Unit,
    onCreateChallengeClick: () -> Unit
) {
    if (friends.isEmpty() && activeChallenges.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "👥", fontSize = 48.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "NO FRIENDS ADDED YET",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Search for players by Username or Player ID in the Find tab to start competing!",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            PrimaryButton(
                text = "CREATE OPEN CHALLENGE",
                onClick = onCreateChallengeClick,
                icon = Icons.Default.SportsEsports,
                modifier = Modifier.widthIn(max = 260.dp),
                testTag = "empty_friends_create_challenge_button"
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize().testTag("friends_list_column"),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Active challenges header if any
            if (activeChallenges.isNotEmpty()) {
                item {
                    Text(
                        text = "ACTIVE BATTLES & CHALLENGES",
                        style = MaterialTheme.typography.labelLarge,
                        color = NeonAmber,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }

                items(activeChallenges) { challenge ->
                    ChallengeCard(challenge = challenge, onViewMatchResult = onViewMatchResult)
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "MY FRIENDS (${friends.size})",
                        style = MaterialTheme.typography.labelLarge,
                        color = NeonCyan,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }
            }

            items(friends) { friend ->
                FriendRowCard(
                    friend = friend,
                    onChallenge = { onChallengeFriend(friend) },
                    onViewProfile = { onViewProfile(friend) },
                    onRemove = { onRemoveFriend(friend) },
                    onBlock = { onBlockFriend(friend) }
                )
            }
        }
    }
}

@Composable
private fun FriendRowCard(
    friend: Friend,
    onChallenge: () -> Unit,
    onViewProfile: () -> Unit,
    onRemove: () -> Unit,
    onBlock: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = CardSurface,
        border = BorderStroke(1.dp, CardSurfaceBorder),
        modifier = Modifier.fillMaxWidth().testTag("friend_card_${friend.userId}")
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar & Online indicator
            Box {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(CardSurfaceElevated)
                        .border(1.dp, CardSurfaceBorder, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = friend.avatarEmoji, fontSize = 24.sp)
                }
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(Color(friend.status.badgeColor))
                        .border(2.dp, CardSurface, CircleShape)
                        .align(Alignment.BottomEnd)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = friend.username,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Lv.${friend.level}", style = MaterialTheme.typography.labelSmall, color = NeonCyan)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = friend.playerTitle, style = MaterialTheme.typography.labelSmall, color = NeonAmber)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "•", color = TextMuted, fontSize = 10.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = friend.status.label, style = MaterialTheme.typography.labelSmall, color = Color(friend.status.badgeColor))
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onChallenge,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(NeonCyan.copy(alpha = 0.15f))
                        .border(1.dp, NeonCyan.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                        .testTag("challenge_friend_${friend.userId}")
                ) {
                    Icon(Icons.Default.SportsEsports, contentDescription = "Challenge", tint = NeonCyan, modifier = Modifier.size(20.dp))
                }

                Box {
                    IconButton(onClick = { showMenu = true }, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Options", tint = TextMuted)
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(CardSurfaceElevated)
                    ) {
                        DropdownMenuItem(
                            text = { Text("View Profile") },
                            onClick = {
                                showMenu = false
                                onViewProfile()
                            },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = TextPrimary) }
                        )
                        DropdownMenuItem(
                            text = { Text("Remove Friend", color = NeonRed) },
                            onClick = {
                                showMenu = false
                                onRemove()
                            },
                            leadingIcon = { Icon(Icons.Default.PersonRemove, contentDescription = null, tint = NeonRed) }
                        )
                        DropdownMenuItem(
                            text = { Text("Block Player", color = NeonRed) },
                            onClick = {
                                showMenu = false
                                onBlock()
                            },
                            leadingIcon = { Icon(Icons.Default.Block, contentDescription = null, tint = NeonRed) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChallengeCard(
    challenge: FriendChallengeRecord,
    onViewMatchResult: (String) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = CardSurface,
        border = BorderStroke(1.dp, if (challenge.status == ChallengeStatus.COMPLETED) NeonGreen.copy(alpha = 0.4f) else NeonAmber.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth().testTag("challenge_card_${challenge.challengeId}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "⚔️", fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "ROOM ${challenge.challengeCode}",
                        style = MaterialTheme.typography.titleSmall,
                        color = NeonCyan,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }

                Surface(
                    color = if (challenge.status == ChallengeStatus.COMPLETED) NeonGreen.copy(alpha = 0.2f) else NeonAmber.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = challenge.status.name,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (challenge.status == ChallengeStatus.COMPLETED) NeonGreen else NeonAmber,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${challenge.creatorName} vs ${if (challenge.opponentName.isNotBlank()) challenge.opponentName else "Open Opponent"}",
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = "${challenge.category.uppercase()} • ${challenge.difficulty} • ${challenge.questionCount} Questions",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )

            if (challenge.status == ChallengeStatus.COMPLETED) {
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = { onViewMatchResult(challenge.challengeId) },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyan),
                    border = BorderStroke(1.dp, NeonCyan),
                    modifier = Modifier.fillMaxWidth().testTag("view_results_${challenge.challengeId}")
                ) {
                    Text("VIEW MATCH RESULTS & SCORECARD", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun RequestsTabContent(
    incoming: List<FriendRequest>,
    outgoing: List<FriendRequest>,
    onAccept: (FriendRequest) -> Unit,
    onDecline: (String) -> Unit,
    onCancel: (String) -> Unit
) {
    if (incoming.isEmpty() && outgoing.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "📩", fontSize = 48.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "NO PENDING REQUESTS",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "When players add you using your Player ID, requests will appear here.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize().testTag("requests_list_column"),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (incoming.isNotEmpty()) {
                item {
                    Text(
                        text = "INCOMING REQUESTS (${incoming.size})",
                        style = MaterialTheme.typography.labelLarge,
                        color = NeonCyan,
                        fontWeight = FontWeight.Black
                    )
                }

                items(incoming) { req ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = CardSurface,
                        border = BorderStroke(1.dp, CardSurfaceBorder),
                        modifier = Modifier.fillMaxWidth().testTag("incoming_request_${req.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(CardSurfaceElevated)
                                    .border(1.dp, CardSurfaceBorder, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = req.senderAvatarEmoji, fontSize = 24.sp)
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = req.senderUsername, style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                                Text(text = "ID: ${req.senderPlayerId} • Lv.${req.senderLevel}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                IconButton(
                                    onClick = { onAccept(req) },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(NeonGreen.copy(alpha = 0.2f))
                                        .testTag("accept_request_${req.id}")
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = "Accept", tint = NeonGreen, modifier = Modifier.size(18.dp))
                                }

                                IconButton(
                                    onClick = { onDecline(req.id) },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(NeonRed.copy(alpha = 0.2f))
                                        .testTag("decline_request_${req.id}")
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Decline", tint = NeonRed, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }

            if (outgoing.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "SENT REQUESTS (${outgoing.size})",
                        style = MaterialTheme.typography.labelLarge,
                        color = TextMuted,
                        fontWeight = FontWeight.Black
                    )
                }

                items(outgoing) { req ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = CardSurface,
                        border = BorderStroke(1.dp, CardSurfaceBorder),
                        modifier = Modifier.fillMaxWidth().testTag("outgoing_request_${req.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(text = req.receiverUsername, style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                                Text(text = "Status: PENDING", style = MaterialTheme.typography.labelSmall, color = NeonAmber)
                            }

                            OutlinedButton(
                                onClick = { onCancel(req.id) },
                                modifier = Modifier.testTag("cancel_request_${req.id}")
                            ) {
                                Text("CANCEL", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FindPlayersTabContent(
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    isSearching: Boolean,
    hasSearched: Boolean,
    searchResults: List<PlayerPublicProfile>,
    onPerformSearch: () -> Unit,
    onSendFriendRequest: (PlayerPublicProfile) -> Unit,
    onViewProfile: (PlayerPublicProfile) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Search Input Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onQueryChange,
                placeholder = { Text("Search Username or BB-XXXXXX...", color = TextMuted) },
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted) },
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonCyan,
                    unfocusedBorderColor = CardSurfaceBorder,
                    focusedContainerColor = CardSurface,
                    unfocusedContainerColor = CardSurface
                ),
                modifier = Modifier.weight(1f).testTag("find_players_search_input")
            )

            Button(
                onClick = onPerformSearch,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = BackgroundDark),
                modifier = Modifier.height(56.dp).testTag("perform_search_button")
            ) {
                Text("SEARCH", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isSearching) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = NeonCyan)
            }
        } else if (hasSearched && searchResults.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = "🔍", fontSize = 48.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "NO PLAYER FOUND",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Check the username or Player ID spelling. Make sure your friend has registered their account.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
            }
        } else if (searchResults.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().testTag("search_results_column"),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(searchResults) { profile ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = CardSurface,
                        border = BorderStroke(1.dp, CardSurfaceBorder),
                        modifier = Modifier.fillMaxWidth().testTag("search_profile_card_${profile.uid}")
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(CardSurfaceElevated)
                                    .border(1.dp, CardSurfaceBorder, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = profile.avatarEmoji, fontSize = 24.sp)
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = profile.username,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${profile.playerId} • Lv.${profile.level} • ${profile.playerTitle}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                IconButton(
                                    onClick = { onViewProfile(profile) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(Icons.Default.Info, contentDescription = "Info", tint = TextMuted)
                                }

                                if (profile.isFriend) {
                                    Surface(
                                        color = NeonGreen.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = "FRIENDS",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = NeonGreen,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                        )
                                    }
                                } else if (profile.hasPendingRequest) {
                                    Surface(
                                        color = NeonAmber.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = "PENDING",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = NeonAmber,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                        )
                                    }
                                } else {
                                    IconButton(
                                        onClick = { onSendFriendRequest(profile) },
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(NeonCyan.copy(alpha = 0.2f))
                                            .testTag("send_request_button_${profile.uid}")
                                    ) {
                                        Icon(Icons.Default.PersonAdd, contentDescription = "Add Friend", tint = NeonCyan, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ActivityFeedTabContent(activityFeed: List<FriendActivityItem>) {
    if (activityFeed.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "⚡", fontSize = 48.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "NO RECENT ACTIVITY",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "When your friends unlock achievements, set new records, or win matches, updates will appear here.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize().testTag("activity_feed_column"),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(activityFeed) { item ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = CardSurface,
                    border = BorderStroke(1.dp, CardSurfaceBorder),
                    modifier = Modifier.fillMaxWidth().testTag("activity_item_${item.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = item.avatarEmoji, fontSize = 28.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = item.title, style = MaterialTheme.typography.titleSmall, color = TextPrimary, fontWeight = FontWeight.Bold)
                            Text(text = item.description, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BlockedUsersTabContent(
    blockedList: List<BlockedUser>,
    onUnblock: (BlockedUser) -> Unit
) {
    if (blockedList.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "🛡️", fontSize = 48.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "NO BLOCKED PLAYERS",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Players you block will no longer be able to message, challenge, or view your private activity.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize().testTag("blocked_users_column"),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(blockedList) { user ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = CardSurface,
                    border = BorderStroke(1.dp, CardSurfaceBorder),
                    modifier = Modifier.fillMaxWidth().testTag("blocked_card_${user.blockedUid}")
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = user.avatarEmoji, fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(text = user.username, style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { onUnblock(user) },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyan),
                            border = BorderStroke(1.dp, NeonCyan),
                            modifier = Modifier.testTag("unblock_button_${user.blockedUid}")
                        ) {
                            Text("UNBLOCK", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
