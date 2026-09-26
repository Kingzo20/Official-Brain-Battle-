package com.example.ui.screens

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.app.Activity
import android.widget.Toast
import com.example.ads.AdConfig
import com.example.ads.AdMobManager
import com.example.ads.BannerAdView
import com.example.model.GameResult
import com.example.ui.components.PrimaryButton
import com.example.ui.components.ScoreCard
import com.example.ui.components.SecondaryButton
import com.example.ui.theme.*

@Composable
fun GameResultScreen(
    result: GameResult,
    onPlayAgain: () -> Unit,
    onNextChallenge: () -> Unit,
    onBackToHome: () -> Unit,
    onViewMatchResult: (() -> Unit)? = null,
    onRewardEarnedDoubleXp: ((bonusXp: Int) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity
    BackHandler(onBack = onBackToHome)

    var doubleXpClaimed by remember { mutableStateOf(false) }
    var showAdDialog by remember { mutableStateOf(false) }
    var adFeedbackNotice by remember { mutableStateOf<String?>(null) }

    val effectiveXp = if (doubleXpClaimed) result.xpEarned * 2 else result.xpEarned

    Scaffold(
        containerColor = BackgroundDark,
        modifier = modifier.testTag("game_result_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Trophy / Star Badge
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(88.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(listOf(NeonGold, CardSurface))
                    )
            ) {
                Text(
                    text = if (result.accuracyPercentage >= 70) "🏆" else "⚡",
                    fontSize = 44.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (result.accuracyPercentage >= 80) "GREAT JOB!" else "CHALLENGE COMPLETED",
                style = MaterialTheme.typography.displayMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                textAlign = TextAlign.Center
            )

            Text(
                text = "${result.categoryTitle} • Speed & Precision",
                style = MaterialTheme.typography.bodyMedium,
                color = NeonCyan,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Score Highlight Card
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = CardSurface,
                border = BorderStroke(1.5.dp, NeonGold.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "FINAL SCORE",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        letterSpacing = 1.5.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${result.score}",
                        style = MaterialTheme.typography.displayLarge,
                        color = NeonGold,
                        fontWeight = FontWeight.Black
                    )
                    if (result.isNewPersonalBest) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            color = NeonGold.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, NeonGold)
                        ) {
                            Text(
                                text = "🌟 NEW PERSONAL BEST! 🏆",
                                color = NeonGold,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4 Stats Grid Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ScoreCard(
                    label = "Correct",
                    value = "${result.correctCount} / ${result.totalQuestions}",
                    icon = Icons.Default.CheckCircle,
                    color = NeonGreen,
                    modifier = Modifier.weight(1f)
                )
                ScoreCard(
                    label = "Accuracy",
                    value = "${result.accuracyPercentage}%",
                    icon = Icons.Default.TrackChanges,
                    color = NeonCyan,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ScoreCard(
                    label = "Time",
                    value = "${result.timeSpentSeconds}s",
                    icon = Icons.Default.Timer,
                    color = NeonViolet,
                    modifier = Modifier.weight(1f)
                )
                ScoreCard(
                    label = "Streak",
                    value = "${result.currentStreakDays} days 🔥",
                    icon = Icons.Default.LocalFireDepartment,
                    color = NeonAmber,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // XP Earned Banner & 2x XP Ad card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = CardSurfaceElevated,
                border = BorderStroke(1.dp, CardSurfaceBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "XP EARNED",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                        Text(
                            text = "+$effectiveXp XP",
                            style = MaterialTheme.typography.titleLarge,
                            color = NeonGreen,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    if (!doubleXpClaimed) {
                        Button(
                            onClick = { showAdDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonViolet),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("double_xp_ad_button")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.SmartDisplay, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("2x XP Video", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        Surface(
                            color = NeonGreen.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "2x MULTIPLIER APPLIED",
                                color = NeonGreen,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (onViewMatchResult != null) {
                Button(
                    onClick = onViewMatchResult,
                    colors = ButtonDefaults.buttonColors(containerColor = NeonAmber, contentColor = BackgroundDark),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().height(52.dp).testTag("results_view_scorecard_button")
                ) {
                    Icon(Icons.Default.EmojiEvents, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("VIEW HEAD-TO-HEAD SCORECARD", fontWeight = FontWeight.Black, letterSpacing = 0.5.sp)
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Action Buttons
            PrimaryButton(
                text = "PLAY AGAIN",
                onClick = onPlayAgain,
                icon = Icons.Default.Replay,
                modifier = Modifier.fillMaxWidth(),
                testTag = "results_play_again_button"
            )

            Spacer(modifier = Modifier.height(10.dp))

            SecondaryButton(
                text = "NEXT CHALLENGE",
                onClick = onNextChallenge,
                icon = Icons.AutoMirrored.Filled.ArrowForward,
                modifier = Modifier.fillMaxWidth(),
                testTag = "results_next_challenge_button"
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "🧠 I just scored ${result.score} points with ${result.accuracyPercentage}% accuracy on Brain Battle! Think Fast. Play Smart. Can you beat my score?"
                            )
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share Brain Battle Score"))
                    },
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, CardSurfaceBorder),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("results_share_button")
                ) {
                    Icon(Icons.Default.Share, contentDescription = "Share", tint = NeonCyan)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("SHARE", color = NeonCyan, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onBackToHome,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, CardSurfaceBorder),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("results_home_button")
                ) {
                    Icon(Icons.Default.Home, contentDescription = "Home", tint = TextPrimary)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("HOME", color = TextPrimary, fontWeight = FontWeight.Bold)
                }
            }

            // Ad feedback banner
            if (adFeedbackNotice != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (doubleXpClaimed) NeonGreen.copy(alpha = 0.15f) else NeonAmber.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, if (doubleXpClaimed) NeonGreen else NeonAmber),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = adFeedbackNotice ?: "",
                        color = if (doubleXpClaimed) NeonGreen else NeonAmber,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Non-intrusive Test Banner Ad at bottom of results
            BannerAdView(modifier = Modifier.padding(top = 8.dp))
        }
    }

    if (showAdDialog) {
        AlertDialog(
            onDismissRequest = { showAdDialog = false },
            title = {
                Text(
                    text = "Bonus 2x XP Reward",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = {
                Text(
                    text = "Watch a quick sponsor clip to double your battle XP from +${result.xpEarned} to +${result.xpEarned * 2} XP!",
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showAdDialog = false
                        if (activity != null) {
                            AdMobManager.showRewardedAd(
                                activity = activity,
                                onRewardEarned = { _, _ ->
                                    doubleXpClaimed = true
                                    onRewardEarnedDoubleXp?.invoke(result.xpEarned)
                                    adFeedbackNotice = "SUCCESS! Double XP granted (+${result.xpEarned * 2} XP total)."
                                },
                                onAdClosed = { earned ->
                                    if (!earned) {
                                        adFeedbackNotice = "Ad closed before completion. Reward was not granted."
                                    }
                                },
                                onAdFailed = { error ->
                                    adFeedbackNotice = error
                                }
                            )
                        } else {
                            adFeedbackNotice = "Device display context unavailable."
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen, contentColor = BackgroundDark)
                ) {
                    Text("Watch Clip (+${result.xpEarned} XP)", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAdDialog = false }) {
                    Text("Not Now", color = TextMuted)
                }
            },
            containerColor = CardSurface,
            shape = RoundedCornerShape(18.dp)
        )
    }
}
