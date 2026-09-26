package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.window.Dialog
import com.example.data.social.*
import com.example.model.Difficulty
import com.example.model.GameCategory
import com.example.ui.theme.*

@Composable
fun PublicProfileDialog(
    profile: PlayerPublicProfile,
    onDismiss: () -> Unit,
    onSendChallenge: () -> Unit,
    onAddFriend: () -> Unit,
    onRemoveFriend: () -> Unit,
    onBlockUser: () -> Unit,
    onReportUser: () -> Unit
) {
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = CardSurfaceElevated,
            border = BorderStroke(1.dp, CardSurfaceBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("public_profile_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "PLAYER PROFILE",
                        style = MaterialTheme.typography.labelLarge,
                        color = NeonCyan,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp).testTag("close_profile_dialog")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Avatar and Online badge
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(CardSurface)
                        .border(2.dp, NeonCyan, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = profile.avatarEmoji, fontSize = 42.sp)
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Username & Title
                Text(
                    text = profile.username,
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = profile.playerTitle,
                    style = MaterialTheme.typography.labelMedium,
                    color = NeonAmber,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Player ID with copy button (Safe: does not expose Firebase UID or email)
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = CardSurface,
                    border = BorderStroke(1.dp, CardSurfaceBorder),
                    modifier = Modifier.clickable {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Player ID", profile.playerId))
                        Toast.makeText(context, "Copied ID: ${profile.playerId}", Toast.LENGTH_SHORT).show()
                    }.testTag("profile_player_id_tag")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = profile.playerId,
                            style = MaterialTheme.typography.bodyMedium,
                            color = NeonCyan,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            Icons.Default.ContentCopy,
                            contentDescription = "Copy Player ID",
                            tint = TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Stats Grid
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = CardSurface,
                    border = BorderStroke(1.dp, CardSurfaceBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            StatItem(label = "LEVEL", value = "${profile.level}", color = NeonCyan)
                            StatItem(label = "COUNTRY", value = "${profile.countryFlag} ${profile.country}", color = TextPrimary)
                            StatItem(label = "BEST", value = "${profile.bestScore}", color = NeonGreen)
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = CardSurfaceBorder, thickness = 1.dp)
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            StatItem(label = "CHALLENGES", value = "${profile.challengesPlayed}", color = TextPrimary)
                            StatItem(label = "WINS", value = "${profile.challengesWon}", color = NeonAmber)
                            val winRate = if (profile.challengesPlayed > 0) (profile.challengesWon * 100) / profile.challengesPlayed else 0
                            StatItem(label = "WIN RATE", value = "$winRate%", color = NeonGreen)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Primary Actions
                if (!profile.isBlocked) {
                    PrimaryButton(
                        text = "CHALLENGE PLAYER",
                        onClick = onSendChallenge,
                        icon = Icons.Default.SportsEsports,
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "dialog_challenge_player_button"
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    if (profile.isFriend) {
                        OutlinedButton(
                            onClick = onRemoveFriend,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonRed),
                            border = BorderStroke(1.dp, NeonRed.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth().testTag("dialog_remove_friend_button")
                        ) {
                            Icon(Icons.Default.PersonRemove, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("REMOVE FRIEND")
                        }
                    } else if (profile.hasPendingRequest) {
                        OutlinedButton(
                            onClick = {},
                            enabled = false,
                            modifier = Modifier.fillMaxWidth().testTag("dialog_request_pending_button")
                        ) {
                            Text("REQUEST PENDING")
                        }
                    } else {
                        OutlinedButton(
                            onClick = onAddFriend,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonGreen),
                            border = BorderStroke(1.dp, NeonGreen),
                            modifier = Modifier.fillMaxWidth().testTag("dialog_add_friend_button")
                        ) {
                            Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("ADD AS FRIEND")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Safety Options (Block / Report)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(
                        onClick = onBlockUser,
                        colors = ButtonDefaults.textButtonColors(contentColor = TextMuted),
                        modifier = Modifier.testTag("dialog_block_player_button")
                    ) {
                        Icon(Icons.Default.Block, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (profile.isBlocked) "UNBLOCK" else "BLOCK", fontSize = 12.sp)
                    }

                    TextButton(
                        onClick = onReportUser,
                        colors = ButtonDefaults.textButtonColors(contentColor = NeonRed.copy(alpha = 0.8f)),
                        modifier = Modifier.testTag("dialog_report_player_button")
                    ) {
                        Icon(Icons.Default.Report, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("REPORT", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = TextMuted)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, style = MaterialTheme.typography.titleMedium, color = color, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun CreateChallengeDialog(
    friendsList: List<Friend>,
    selectedFriend: Friend? = null,
    onDismiss: () -> Unit,
    onCreateChallenge: (
        opponent: Friend?,
        category: GameCategory,
        difficulty: Difficulty,
        questionCount: Int
    ) -> Unit
) {
    var targetFriend by remember { mutableStateOf(selectedFriend) }
    var selectedCategory by remember { mutableStateOf(GameCategory.MATH) }
    var selectedDifficulty by remember { mutableStateOf(Difficulty.MEDIUM) }
    var selectedQuestionCount by remember { mutableIntStateOf(5) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = CardSurfaceElevated,
            border = BorderStroke(1.dp, CardSurfaceBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .testTag("create_challenge_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "NEW CHALLENGE",
                        style = MaterialTheme.typography.headlineSmall,
                        color = TextPrimary,
                        fontWeight = FontWeight.Black
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Opponent Selection
                Text(text = "OPPONENT", style = MaterialTheme.typography.labelLarge, color = NeonCyan, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Open room code option
                    FilterChip(
                        selected = targetFriend == null,
                        onClick = { targetFriend = null },
                        label = { Text("🌐 Open Room Code") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NeonCyan,
                            selectedLabelColor = BackgroundDark
                        ),
                        modifier = Modifier.testTag("chip_open_room_code")
                    )

                    // Friends options
                    friendsList.forEach { friend ->
                        FilterChip(
                            selected = targetFriend?.userId == friend.userId,
                            onClick = { targetFriend = friend },
                            label = { Text("${friend.avatarEmoji} ${friend.username}") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NeonCyan,
                                selectedLabelColor = BackgroundDark
                            ),
                            modifier = Modifier.testTag("chip_friend_${friend.userId}")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Category Selection
                Text(text = "CATEGORY", style = MaterialTheme.typography.labelLarge, color = NeonCyan, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GameCategory.values().forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text("${cat.iconEmoji} ${cat.title}") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NeonCyan,
                                selectedLabelColor = BackgroundDark
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Difficulty Selection
                Text(text = "DIFFICULTY", style = MaterialTheme.typography.labelLarge, color = NeonCyan, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Difficulty.values().forEach { diff ->
                        val isSelected = selectedDifficulty == diff
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) NeonCyan.copy(alpha = 0.2f) else CardSurface,
                            border = BorderStroke(1.dp, if (isSelected) NeonCyan else CardSurfaceBorder),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedDifficulty = diff }
                        ) {
                            Text(
                                text = diff.title,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isSelected) NeonCyan else TextSecondary,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 10.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Question Count Selection
                Text(text = "QUESTION COUNT", style = MaterialTheme.typography.labelLarge, color = NeonCyan, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(5, 10, 15).forEach { count ->
                        val isSelected = selectedQuestionCount == count
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) NeonCyan.copy(alpha = 0.2f) else CardSurface,
                            border = BorderStroke(1.dp, if (isSelected) NeonCyan else CardSurfaceBorder),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedQuestionCount = count }
                        ) {
                            Text(
                                text = "$count Questions",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isSelected) NeonCyan else TextSecondary,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 10.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                PrimaryButton(
                    text = if (targetFriend != null) "SEND CHALLENGE" else "GENERATE ROOM CODE",
                    onClick = {
                        onCreateChallenge(targetFriend, selectedCategory, selectedDifficulty, selectedQuestionCount)
                    },
                    icon = Icons.Default.SportsEsports,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "dialog_confirm_create_challenge"
                )
            }
        }
    }
}

@Composable
fun JoinChallengeCodeDialog(
    onDismiss: () -> Unit,
    onJoinCode: (String) -> Unit
) {
    var code by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = CardSurfaceElevated,
            border = BorderStroke(1.dp, CardSurfaceBorder),
            modifier = Modifier.fillMaxWidth().testTag("join_challenge_code_dialog")
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "ENTER ROOM CODE",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Enter the 6-character battle code shared by your friend (e.g. BB7X92)",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = code,
                    onValueChange = {
                        code = it.uppercase()
                        errorMsg = null
                    },
                    placeholder = { Text("BB7X92", color = TextMuted) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = CardSurfaceBorder,
                        focusedContainerColor = CardSurface,
                        unfocusedContainerColor = CardSurface
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("join_code_text_field")
                )

                if (errorMsg != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = errorMsg ?: "", color = NeonRed, style = MaterialTheme.typography.bodySmall)
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("CANCEL")
                    }

                    Button(
                        onClick = {
                            if (code.trim().length >= 4) {
                                onJoinCode(code.trim().uppercase())
                            } else {
                                errorMsg = "Code must be at least 4 characters"
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = BackgroundDark),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).testTag("join_code_submit_button")
                    ) {
                        Text("JOIN BATTLE", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ShareChallengeDialog(
    challenge: FriendChallengeRecord,
    onDismiss: () -> Unit,
    onStartMatchNow: () -> Unit
) {
    val context = LocalContext.current
    val shareLink = "https://brainbattle.game/challenge/${challenge.challengeCode}"

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = CardSurfaceElevated,
            border = BorderStroke(1.dp, CardSurfaceBorder),
            modifier = Modifier.fillMaxWidth().testTag("share_challenge_dialog")
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "⚔️ CHALLENGE READY!",
                    style = MaterialTheme.typography.headlineSmall,
                    color = NeonCyan,
                    fontWeight = FontWeight.Black
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Share this room code with your friend. Both of you will answer the exact same questions under identical battle rules!",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Big Code Display
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = CardSurface,
                    border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth().clickable {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Battle Code", challenge.challengeCode))
                        Toast.makeText(context, "Copied Room Code: ${challenge.challengeCode}", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "ROOM CODE", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = challenge.challengeCode,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = NeonAmber,
                            letterSpacing = 2.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Tap to copy code", style = MaterialTheme.typography.labelSmall, color = NeonCyan)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Share Button (Native Android Share Intent)
                OutlinedButton(
                    onClick = {
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, "Battle me in Brain Battle! Use Room Code: ${challenge.challengeCode}\nor join directly: $shareLink")
                            type = "text/plain"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, "Share Challenge Code")
                        context.startActivity(shareIntent)
                    },
                    modifier = Modifier.fillMaxWidth().testTag("native_share_challenge_button")
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("SHARE LINK & CODE")
                }

                Spacer(modifier = Modifier.height(12.dp))

                PrimaryButton(
                    text = "PLAY YOUR TURN NOW",
                    onClick = onStartMatchNow,
                    icon = Icons.Default.SportsEsports,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "start_challenge_turn_now"
                )
            }
        }
    }
}

@Composable
fun ReportUserDialog(
    targetUsername: String,
    onDismiss: () -> Unit,
    onSubmitReport: (reason: UserReportReason, details: String) -> Unit
) {
    var selectedReason by remember { mutableStateOf(UserReportReason.SPAM) }
    var details by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = CardSurfaceElevated,
            border = BorderStroke(1.dp, CardSurfaceBorder),
            modifier = Modifier.fillMaxWidth().testTag("report_user_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "REPORT PLAYER",
                    style = MaterialTheme.typography.titleLarge,
                    color = NeonRed,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Reporting $targetUsername. Reports are reviewed by safety moderators.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(text = "REASON", style = MaterialTheme.typography.labelMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                UserReportReason.values().forEach { reason ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedReason = reason }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedReason == reason,
                            onClick = { selectedReason = reason },
                            colors = RadioButtonDefaults.colors(selectedColor = NeonRed)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = reason.label, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(text = "ADDITIONAL DETAILS (OPTIONAL)", style = MaterialTheme.typography.labelMedium, color = TextPrimary)
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = details,
                    onValueChange = { details = it },
                    placeholder = { Text("Describe the issue...", color = TextMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = CardSurfaceBorder,
                        focusedContainerColor = CardSurface,
                        unfocusedContainerColor = CardSurface
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("CANCEL")
                    }

                    Button(
                        onClick = { onSubmitReport(selectedReason, details) },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonRed, contentColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).testTag("submit_report_button")
                    ) {
                        Text("SUBMIT")
                    }
                }
            }
        }
    }
}

@Composable
fun SocialPrivacyDialog(
    settings: SocialPrivacySettings,
    onDismiss: () -> Unit,
    onSaveSettings: (SocialPrivacySettings) -> Unit
) {
    var allowReqs by remember { mutableStateOf(settings.allowFriendRequests) }
    var allowChallenges by remember { mutableStateOf(settings.allowChallengeRequests) }
    var showActivity by remember { mutableStateOf(settings.showActivity) }
    var showCountry by remember { mutableStateOf(settings.showCountry) }
    var showStats by remember { mutableStateOf(settings.showProfileStatistics) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = CardSurfaceElevated,
            border = BorderStroke(1.dp, CardSurfaceBorder),
            modifier = Modifier.fillMaxWidth().testTag("social_privacy_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "PRIVACY & SOCIAL SETTINGS",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                PrivacyToggleRow(
                    title = "Allow Friend Requests",
                    description = "Let other players send you friend requests",
                    checked = allowReqs,
                    onCheckedChange = { allowReqs = it }
                )

                PrivacyToggleRow(
                    title = "Allow Direct Challenges",
                    description = "Let friends send battle challenges directly",
                    checked = allowChallenges,
                    onCheckedChange = { allowChallenges = it }
                )

                PrivacyToggleRow(
                    title = "Show Friend Activity",
                    description = "Broadcast achievements and wins to friends",
                    checked = showActivity,
                    onCheckedChange = { showActivity = it }
                )

                PrivacyToggleRow(
                    title = "Display Country Flag",
                    description = "Show country affiliation on rankings",
                    checked = showCountry,
                    onCheckedChange = { showCountry = it }
                )

                PrivacyToggleRow(
                    title = "Public Statistics",
                    description = "Allow other players to view win/loss records",
                    checked = showStats,
                    onCheckedChange = { showStats = it }
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("CANCEL")
                    }

                    Button(
                        onClick = {
                            onSaveSettings(
                                SocialPrivacySettings(
                                    allowFriendRequests = allowReqs,
                                    allowChallengeRequests = allowChallenges,
                                    showActivity = showActivity,
                                    showCountry = showCountry,
                                    showProfileStatistics = showStats
                                )
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = BackgroundDark),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).testTag("save_privacy_settings_button")
                    ) {
                        Text("SAVE", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun PrivacyToggleRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(text = title, style = MaterialTheme.typography.bodyMedium, color = TextPrimary, fontWeight = FontWeight.SemiBold)
            Text(text = description, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = BackgroundDark, checkedTrackColor = NeonCyan)
        )
    }
}
