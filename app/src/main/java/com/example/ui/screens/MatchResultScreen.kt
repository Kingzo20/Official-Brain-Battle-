package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.example.data.GameRepository
import com.example.data.social.ChallengeStatus
import com.example.data.social.FriendChallengeRecord
import com.example.ui.components.PrimaryButton
import com.example.ui.components.TopBar
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun MatchResultScreen(
    challengeId: String,
    repository: GameRepository,
    onRematch: (FriendChallengeRecord) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val socialRepo = repository.socialRepository
    val userProfile by repository.userProfile.collectAsState()

    var challenge by remember { mutableStateOf<FriendChallengeRecord?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(challengeId) {
        val found = socialRepo.getChallengeById(challengeId)
        challenge = found
        isLoading = false
    }

    Scaffold(
        containerColor = BackgroundDark,
        topBar = {
            TopBar(
                title = "MATCH RESULTS",
                onBack = onBack
            )
        },
        modifier = modifier.testTag("match_result_screen")
    ) { innerPadding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = NeonCyan)
            }
        } else if (challenge == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = "⚠️", fontSize = 48.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "MATCH NOT FOUND",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                PrimaryButton(text = "BACK TO SOCIAL", onClick = onBack, testTag = "match_not_found_back")
            }
        } else {
            val record = challenge!!
            val isUserCreator = record.creatorId == userProfile.uid || record.creatorName == userProfile.username

            val userResult = if (isUserCreator) record.creatorResult else record.opponentResult
            val opponentResult = if (isUserCreator) record.opponentResult else record.creatorResult

            val userScore = userResult?.score ?: 0
            val userAcc = userResult?.accuracy ?: 0
            val userTime = userResult?.durationSeconds ?: 0

            val opponentName = if (isUserCreator) record.opponentName.ifBlank { "Opponent" } else record.creatorName
            val opponentAvatar = if (isUserCreator) record.opponentAvatar else record.creatorAvatar
            val opponentScore = opponentResult?.score ?: 0
            val opponentAcc = opponentResult?.accuracy ?: 0
            val opponentTime = opponentResult?.durationSeconds ?: 0

            val opponentCompleted = opponentResult != null
            val isBothCompleted = record.status == ChallengeStatus.COMPLETED

            // Determine Outcome
            val (outcomeText, outcomeEmoji, outcomeColor) = remember(isBothCompleted, opponentCompleted, userScore, opponentScore, userTime, opponentTime) {
                if (!isBothCompleted) {
                    Triple("WAITING FOR OPPONENT", "⏳", NeonAmber)
                } else if (userScore > opponentScore) {
                    Triple("VICTORY! 🏆", "👑", NeonGreen)
                } else if (userScore < opponentScore) {
                    Triple("DEFEAT", "💔", NeonRed)
                } else {
                    // Score tie: tiebreak by faster completion time
                    if (userTime < opponentTime && userTime > 0) {
                        Triple("VICTORY ON TIME! ⚡", "👑", NeonGreen)
                    } else if (userTime > opponentTime && opponentTime > 0) {
                        Triple("DEFEAT ON TIME", "💔", NeonRed)
                    } else {
                        Triple("TIED BATTLE! 🤝", "🤝", NeonCyan)
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(10.dp))

                // Outcome Banner
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = outcomeColor.copy(alpha = 0.15f),
                    border = BorderStroke(2.dp, outcomeColor),
                    modifier = Modifier.fillMaxWidth().testTag("match_outcome_banner")
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = outcomeEmoji, fontSize = 42.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = outcomeText,
                            style = MaterialTheme.typography.headlineMedium,
                            color = outcomeColor,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "ROOM CODE: ${record.challengeCode} • ${record.category.uppercase()}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Side-by-Side Comparison Card
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = CardSurface,
                    border = BorderStroke(1.dp, CardSurfaceBorder),
                    modifier = Modifier.fillMaxWidth().testTag("head_to_head_card")
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Player (You)
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(CardSurfaceElevated)
                                        .border(2.dp, NeonCyan, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = userProfile.avatarEmoji, fontSize = 28.sp)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "YOU",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = NeonCyan,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = userProfile.username,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }

                            // VS Badge
                            Surface(
                                shape = CircleShape,
                                color = CardSurfaceElevated,
                                border = BorderStroke(1.dp, CardSurfaceBorder),
                                modifier = Modifier.padding(horizontal = 8.dp)
                            ) {
                                Text(
                                    text = "VS",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = NeonAmber,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }

                            // Opponent
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(CardSurfaceElevated)
                                        .border(2.dp, if (opponentCompleted) NeonAmber else TextMuted, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = opponentAvatar, fontSize = 28.sp)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = opponentName.uppercase(),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (opponentCompleted) "Finished" else "Waiting...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (opponentCompleted) NeonGreen else NeonAmber
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                        HorizontalDivider(color = CardSurfaceBorder, thickness = 1.dp)
                        Spacer(modifier = Modifier.height(16.dp))

                        // Score Comparison Row
                        StatComparisonRow(
                            label = "FINAL SCORE",
                            userValue = "$userScore",
                            opponentValue = if (opponentCompleted) "$opponentScore" else "--",
                            isUserWinner = isBothCompleted && userScore > opponentScore
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Accuracy Comparison Row
                        StatComparisonRow(
                            label = "ACCURACY",
                            userValue = "$userAcc%",
                            opponentValue = if (opponentCompleted) "$opponentAcc%" else "--",
                            isUserWinner = isBothCompleted && userAcc > opponentAcc
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Time Spent Row
                        StatComparisonRow(
                            label = "TIME SPENT",
                            userValue = "${userTime}s",
                            opponentValue = if (opponentCompleted) "${opponentTime}s" else "--",
                            isUserWinner = isBothCompleted && userTime < opponentTime && userTime > 0
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // XP Breakdown Card
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = CardSurface,
                    border = BorderStroke(1.dp, CardSurfaceBorder),
                    modifier = Modifier.fillMaxWidth().testTag("xp_rewards_card")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "COMPETITIVE REWARDS",
                            style = MaterialTheme.typography.labelMedium,
                            color = NeonAmber,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Match Participation XP", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                            Text("+150 XP", color = NeonCyan, fontWeight = FontWeight.Bold)
                        }

                        if (isBothCompleted && userScore > opponentScore) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Victory Bonus XP", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                                Text("+100 XP", color = NeonGreen, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action Buttons
                PrimaryButton(
                    text = "REMATCH WITH SAME SETTINGS",
                    onClick = {
                        coroutineScope.launch {
                            val rematch = socialRepo.createRematchChallenge(record)
                            onRematch(rematch)
                        }
                    },
                    icon = Icons.Default.Refresh,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "rematch_button"
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = {
                        val shareText = "I played a Brain Battle challenge with $opponentName!\nScore: $userScore vs $opponentScore\nOutcome: $outcomeText\nJoin the battle on Brain Battle!"
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, shareText)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Share Match Results"))
                    },
                    modifier = Modifier.fillMaxWidth().testTag("share_match_result_button")
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("SHARE RESULTS")
                }

                Spacer(modifier = Modifier.height(10.dp))

                TextButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("back_to_social_button")
                ) {
                    Text("BACK TO SOCIAL HUB", color = TextSecondary)
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun StatComparisonRow(
    label: String,
    userValue: String,
    opponentValue: String,
    isUserWinner: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = userValue,
            style = MaterialTheme.typography.titleMedium,
            color = if (isUserWinner) NeonGreen else NeonCyan,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1.2f),
            textAlign = TextAlign.Center
        )

        Text(
            text = opponentValue,
            style = MaterialTheme.typography.titleMedium,
            color = if (!isUserWinner && opponentValue != "--") NeonAmber else TextSecondary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
    }
}
