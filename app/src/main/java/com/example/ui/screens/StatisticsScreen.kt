package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.GameRepository
import com.example.model.Difficulty
import com.example.model.GameCategory
import com.example.ui.components.PrimaryButton
import com.example.ui.components.ScoreCard
import com.example.ui.theme.*
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    repository: GameRepository,
    onNavigateBack: () -> Unit,
    onStartPractice: (GameCategory, Difficulty) -> Unit,
    modifier: Modifier = Modifier
) {
    val userProfile by repository.userProfile.collectAsState()
    val performance by repository.performanceProfile.collectAsState()
    val recommendation by repository.personalizedRecommendation.collectAsState()

    Scaffold(
        containerColor = BackgroundDark,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "BRAIN REPORT & STATS",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = "Cognitive analytics & performance tracking",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("statistics_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundDark)
            )
        },
        modifier = modifier.testTag("statistics_screen")
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Brain Report Card
            performance.brainReport?.let { report ->
                item {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = CardSurface,
                        border = BorderStroke(1.5.dp, NeonCyan.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            NeonCyan.copy(alpha = 0.12f),
                                            CardSurface
                                        )
                                    )
                                )
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(text = "🧠", fontSize = 24.sp)
                                    Text(
                                        text = report.title.uppercase(Locale.ROOT),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Black,
                                        color = NeonCyan,
                                        letterSpacing = 1.sp
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = NeonViolet.copy(alpha = 0.25f),
                                    border = BorderStroke(1.dp, NeonViolet)
                                ) {
                                    Text(
                                        text = "ADAPTIVE: ${report.recommendedDifficulty.title.uppercase(Locale.ROOT)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = NeonViolet,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Text(
                                text = report.cognitiveStyle,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary
                            )

                            HorizontalDivider(color = CardSurfaceBorder)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("STRONGEST DOMAIN", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                    Text(
                                        text = "🌟 ${report.strongestDomain}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = NeonGreen
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("PRACTICE FOCUS", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                    Text(
                                        text = "🎯 ${report.practiceFocus}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = NeonAmber
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("SPEED PROFILE", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                    Text(
                                        text = report.averageSpeedText,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextPrimary
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("CONSISTENCY", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                    Text(
                                        text = report.consistencyText,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextPrimary
                                    )
                                }
                            }

                            if (performance.recommendationReason.isNotBlank()) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = BackgroundDark.copy(alpha = 0.6f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "Adaptive System Insight: ${performance.recommendationReason}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextSecondary,
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Quick Stats Grid
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ScoreCard(
                        label = "Accuracy",
                        value = "${userProfile.accuracyPercentage}%",
                        icon = Icons.Default.CheckCircle,
                        color = NeonCyan,
                        modifier = Modifier.weight(1f)
                    )
                    ScoreCard(
                        label = "Games Played",
                        value = "${userProfile.gamesPlayed}",
                        icon = Icons.Default.SportsEsports,
                        color = NeonGreen,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ScoreCard(
                        label = "Best Score",
                        value = "${userProfile.bestScore}",
                        icon = Icons.Default.EmojiEvents,
                        color = NeonGold,
                        modifier = Modifier.weight(1f)
                    )
                    ScoreCard(
                        label = "Avg Response",
                        value = "${String.format(Locale.US, "%.1f", performance.averageAnswerTimeSeconds)}s",
                        icon = Icons.Default.Timer,
                        color = NeonViolet,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Personalized Recommendation Banner
            recommendation?.let { rec ->
                item {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = CardSurface,
                        border = BorderStroke(1.dp, NeonPink.copy(alpha = 0.6f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .background(Brush.horizontalGradient(listOf(NeonPink.copy(alpha = 0.12f), CardSurface)))
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
                                    color = NeonPink.copy(alpha = 0.2f),
                                    border = BorderStroke(1.dp, NeonPink)
                                ) {
                                    Text(
                                        text = rec.badge,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = NeonPink,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                                Text(
                                    text = "${rec.questionCount} Questions • ${rec.difficulty.title}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary
                                )
                            }

                            Text(
                                text = rec.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = rec.rationale,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )

                            PrimaryButton(
                                text = "PRACTICE NOW (${rec.category.title})",
                                onClick = { onStartPractice(rec.category, rec.difficulty) },
                                icon = Icons.Default.PlayArrow,
                                modifier = Modifier.fillMaxWidth(),
                                testTag = "statistics_start_practice_button"
                            )
                        }
                    }
                }
            }

            // Category Mastery Section Header
            item {
                Text(
                    text = "CATEGORY MASTERY",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
            }

            // Category Cards
            val categories = GameCategory.values().filter { it != GameCategory.DAILY }
            items(categories) { category ->
                val perf = performance.categoryStats[category.id]
                val answered = perf?.questionsAnswered ?: 0
                val accuracy = perf?.accuracyPercentage ?: 0

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = CardSurface,
                    border = BorderStroke(1.dp, CardSurfaceBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(CardSurfaceElevated)
                                ) {
                                    Text(text = category.iconEmoji, fontSize = 18.sp)
                                }
                                Column {
                                    Text(
                                        text = category.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = if (answered > 0) "$answered answered" else "Not played yet",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextSecondary
                                    )
                                }
                            }

                            Text(
                                text = if (answered > 0) "$accuracy%" else "—",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = when {
                                    accuracy >= 80 -> NeonGreen
                                    accuracy >= 55 -> NeonAmber
                                    answered > 0 -> NeonRed
                                    else -> TextSecondary
                                }
                            )
                        }

                        // Progress Bar
                        LinearProgressIndicator(
                            progress = { if (answered > 0) accuracy / 100f else 0f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = when {
                                accuracy >= 80 -> NeonGreen
                                accuracy >= 55 -> NeonAmber
                                else -> NeonCyan
                            },
                            trackColor = BackgroundDark
                        )
                    }
                }
            }

            // Difficulty Breakdown Header
            item {
                Text(
                    text = "DIFFICULTY ACCURACY",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Difficulty.values().forEach { diff ->
                        val acc = performance.difficultyStats[diff.title] ?: 0
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = CardSurface,
                            border = BorderStroke(1.dp, CardSurfaceBorder),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = diff.title,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = when (diff) {
                                        Difficulty.EASY -> DifficultyEasy
                                        Difficulty.MEDIUM -> DifficultyMedium
                                        Difficulty.HARD -> DifficultyHard
                                        Difficulty.EXTREME -> DifficultyExtreme
                                    },
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (acc > 0) "$acc%" else "—",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Black,
                                    color = TextPrimary
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
