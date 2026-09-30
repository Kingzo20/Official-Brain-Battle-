package com.example.ui.screens

import android.app.Activity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ads.AdConfig
import com.example.ads.AdMobManager
import com.example.ads.BannerAdView
import com.example.data.GameRepository
import com.example.model.Difficulty
import com.example.model.GameCategory
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun HomeScreen(
    repository: GameRepository,
    onPlayDailyChallenge: () -> Unit,
    onQuickPlaySelected: (GameCategory) -> Unit,
    onNotificationsClick: () -> Unit,
    onProfileClick: () -> Unit,
    onViewAllAchievements: () -> Unit,
    onNavigateToStatistics: () -> Unit = {},
    onStartPractice: (GameCategory, Difficulty) -> Unit = { _, _ -> },
    onLaunchJambCbt: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val userProfile by repository.userProfile.collectAsState()
    val notifications by repository.notifications.collectAsState()
    val recommendation by repository.personalizedRecommendation.collectAsState()
    val syncState = repository.syncService?.syncState?.collectAsState()?.value ?: com.example.model.SyncState.IDLE
    val unreadCount = notifications.count { !it.isRead }
    val achievements = repository.getAchievements().take(3)
    val isDailyCompleted = repository.isDailyChallengeCompletedToday()
    val context = LocalContext.current
    val activity = context as? Activity

    var showBonusDialog by remember { mutableStateOf(false) }
    var bonusClaimedNotice by remember { mutableStateOf<String?>(null) }

    Scaffold(
        containerColor = BackgroundDark,
        modifier = modifier.testTag("home_screen")
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                // Top Profile Header
                ProfileHeader(
                    username = userProfile.displayName.ifBlank { userProfile.username },
                    level = userProfile.level,
                    currentXp = userProfile.currentXp,
                    nextLevelXp = userProfile.nextLevelXp,
                    avatarEmoji = userProfile.avatarEmoji,
                    onAvatarClick = onProfileClick,
                    onNotificationClick = onNotificationsClick,
                    unreadNotificationCount = unreadCount,
                    syncStatusText = "${syncState.badge} ${syncState.label}"
                )
            }

            // Prominent Daily Challenge Card
            item {
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = CardSurface,
                    border = BorderStroke(1.5.dp, NeonAmber.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("home_daily_challenge_card")
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        CardSurface,
                                        NeonAmber.copy(alpha = 0.08f)
                                    )
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = NeonAmber.copy(alpha = 0.2f),
                                    border = BorderStroke(1.dp, NeonAmber.copy(alpha = 0.4f))
                                ) {
                                    Text(
                                        text = "🔥 DAILY CHALLENGE",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = NeonAmber,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.weight(1f))
                                Text(
                                    text = "Streak: ${userProfile.currentStreak} Days 🔥",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = NeonAmber,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "Today's Brain Battle",
                                style = MaterialTheme.typography.titleLarge,
                                color = TextPrimary,
                                fontWeight = FontWeight.Black
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Questions", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                    Text("10", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                                }
                                Column {
                                    Text("Time", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                    Text("60 seconds", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                                }
                                Column {
                                    Text("Reward", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                    Text("+250 XP", style = MaterialTheme.typography.titleMedium, color = NeonGreen, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            PrimaryButton(
                                text = if (isDailyCompleted) "PLAY AGAIN (COMPLETED ✓)" else "PLAY NOW",
                                onClick = onPlayDailyChallenge,
                                icon = if (isDailyCompleted) Icons.Default.Replay else Icons.Default.PlayArrow,
                                modifier = Modifier.fillMaxWidth(),
                                testTag = "home_play_daily_button"
                            )
                        }
                    }
                }
            }

            // Prominent JAMB CBT Exam Mode Card
            item {
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = CardSurface,
                    border = BorderStroke(1.5.dp, Color(0xFF00C853).copy(alpha = 0.8f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("home_jamb_cbt_card")
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        CardSurface,
                                        Color(0xFF008751).copy(alpha = 0.16f),
                                        CardSurfaceElevated
                                    )
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF008751).copy(alpha = 0.25f),
                                    border = BorderStroke(1.dp, Color(0xFF00C853))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(text = "🇳🇬", fontSize = 14.sp)
                                        Text(
                                            text = "JAMB UTME CBT SIMULATOR",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFF00C853),
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.weight(1f))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = NeonAmber.copy(alpha = 0.2f),
                                    border = BorderStroke(1.dp, NeonAmber)
                                ) {
                                    Text(
                                        text = "PAST QUESTIONS",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = NeonAmber,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "Authentic CBT Exam Engine",
                                style = MaterialTheme.typography.titleLarge,
                                color = TextPrimary,
                                fontWeight = FontWeight.Black
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "8 UTME Subjects • Authentic Question Palette • Real-time Countdown Timer • In-Depth Explanations",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                lineHeight = 18.sp
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Subjects", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                    Text("8 Papers", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                                }
                                Column {
                                    Text("Exam Modes", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                    Text("Drill / Standard / Full", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                                }
                                Column {
                                    Text("No-Repeat", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                    Text("Active ✓", style = MaterialTheme.typography.titleMedium, color = Color(0xFF00C853), fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = onLaunchJambCbt,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("home_launch_jamb_button"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF00C853)
                                )
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.School,
                                        contentDescription = null,
                                        tint = Color.Black,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "LAUNCH JAMB CBT EXAM MODE",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Black,
                                        color = Color.Black
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Recommended For You (AI-Powered Adaptive Practice)
            recommendation?.let { rec ->
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "RECOMMENDED FOR YOU",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "AI ADAPTIVE",
                                style = MaterialTheme.typography.labelSmall,
                                color = NeonCyan,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = CardSurface,
                            border = BorderStroke(1.2.dp, NeonCyan.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(NeonCyan.copy(alpha = 0.10f), CardSurface)
                                        )
                                    )
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = NeonCyan.copy(alpha = 0.2f),
                                        border = BorderStroke(1.dp, NeonCyan)
                                    ) {
                                        Text(
                                            text = rec.badge,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = NeonCyan,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                    Text(
                                        text = "${rec.category.title} • ${rec.difficulty.title}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextSecondary
                                    )
                                }

                                Text(
                                    text = rec.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = TextPrimary
                                )
                                Text(
                                    text = rec.rationale,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    PrimaryButton(
                                        text = "PRACTICE NOW",
                                        onClick = { onStartPractice(rec.category, rec.difficulty) },
                                        icon = Icons.Default.Bolt,
                                        modifier = Modifier.weight(1.2f),
                                        testTag = "home_recommended_practice_button"
                                    )
                                    SecondaryButton(
                                        text = "REPORT",
                                        onClick = onNavigateToStatistics,
                                        icon = Icons.Default.Insights,
                                        modifier = Modifier.weight(0.8f),
                                        testTag = "home_view_brain_report_button"
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Quick Play Section
            item {
                Column {
                    Text(
                        text = "QUICK PLAY",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    val quickPlayItems = listOf(
                        Pair(GameCategory.MATH, "Math"),
                        Pair(GameCategory.LOGIC, "Logic"),
                        Pair(GameCategory.NUMBERS, "Numbers"),
                        Pair(GameCategory.WORDS, "Words"),
                        Pair(GameCategory.KNOWLEDGE, "Knowledge"),
                        Pair(GameCategory.SCIENCE, "Science")
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            quickPlayItems.take(3).forEach { (category, title) ->
                                QuickPlayCard(
                                    emoji = category.iconEmoji,
                                    title = title,
                                    onClick = { onQuickPlaySelected(category) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            quickPlayItems.drop(3).forEach { (category, title) ->
                                QuickPlayCard(
                                    emoji = category.iconEmoji,
                                    title = title,
                                    onClick = { onQuickPlaySelected(category) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            // Your Progress Section
            item {
                Column {
                    Text(
                        text = "YOUR PROGRESS",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ScoreCard(
                            label = "Games Played",
                            value = "${userProfile.gamesPlayed}",
                            icon = Icons.Default.SportsEsports,
                            color = NeonCyan,
                            modifier = Modifier.weight(1f)
                        )
                        ScoreCard(
                            label = "Best Score",
                            value = "${userProfile.bestScore}",
                            icon = Icons.Default.EmojiEvents,
                            color = NeonGold,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ScoreCard(
                            label = "Current Streak",
                            value = "${userProfile.currentStreak} 🔥",
                            icon = Icons.Default.LocalFireDepartment,
                            color = NeonAmber,
                            modifier = Modifier.weight(1f)
                        )
                        ScoreCard(
                            label = "Total XP",
                            value = "${userProfile.totalXp}",
                            icon = Icons.Default.Bolt,
                            color = NeonViolet,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Daily Sponsor Bonus (Rewarded Ad)
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = CardSurface,
                    border = BorderStroke(1.dp, NeonViolet.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("sponsor_bonus_card")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = NeonViolet.copy(alpha = 0.2f),
                            modifier = Modifier.size(46.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.CardGiftcard, contentDescription = null, tint = NeonViolet)
                            }
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "SPONSOR BONUS",
                                style = MaterialTheme.typography.titleSmall,
                                color = TextPrimary,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "Watch a sponsor clip to earn +${AdConfig.REWARD_BONUS_XP_AMOUNT} XP!",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                            if (bonusClaimedNotice != null) {
                                Text(
                                    text = bonusClaimedNotice ?: "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = NeonGreen,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Button(
                            onClick = { showBonusDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonViolet),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("claim_sponsor_bonus_button")
                        ) {
                            Text("CLAIM", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }

            // Recent Achievements
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "RECENT ACHIEVEMENTS",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "View All",
                            color = NeonCyan,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable(onClick = onViewAllAchievements)
                                .padding(4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        achievements.forEach { achievement ->
                            AchievementCard(achievement = achievement)
                        }
                    }
                }
            }

            // Non-intrusive Test Banner Ad at bottom of HomeScreen
            item {
                BannerAdView(modifier = Modifier.padding(top = 8.dp))
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    if (showBonusDialog) {
        AlertDialog(
            onDismissRequest = { showBonusDialog = false },
            title = {
                Text(
                    text = "Sponsor Bonus Clip",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = {
                Text(
                    text = "Watch an official AdMob test clip to earn +${AdConfig.REWARD_BONUS_XP_AMOUNT} XP!",
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showBonusDialog = false
                        if (activity != null) {
                            AdMobManager.showRewardedAd(
                                activity = activity,
                                onRewardEarned = { _, _ ->
                                    repository.awardXp(AdConfig.REWARD_BONUS_XP_AMOUNT)
                                    bonusClaimedNotice = "+${AdConfig.REWARD_BONUS_XP_AMOUNT} XP added to your balance!"
                                },
                                onAdClosed = { earned ->
                                    if (!earned) {
                                        bonusClaimedNotice = "Ad closed before reward confirmation."
                                    }
                                },
                                onAdFailed = { err ->
                                    bonusClaimedNotice = err
                                }
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen, contentColor = BackgroundDark)
                ) {
                    Text("Watch Video", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBonusDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            },
            containerColor = CardSurface,
            shape = RoundedCornerShape(18.dp)
        )
    }
}
