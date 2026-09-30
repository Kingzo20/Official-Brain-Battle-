package com.example.ui.screens.jamb

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.jamb.JambExamResult
import com.example.ui.theme.*

private val JambGreenLight = Color(0xFF00C853)
private val JambFlagAmber = Color(0xFFFF9100)

@Composable
fun JambResultScreen(
    result: JambExamResult,
    onReviewAnswers: () -> Unit,
    onRetakeExam: () -> Unit,
    onBackToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        containerColor = BackgroundDark,
        modifier = modifier.testTag("jamb_result_screen")
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(12.dp))
                // Performance Score Hero Card
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = CardSurface,
                    border = BorderStroke(2.dp, Color(result.gradeColorHex)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("jamb_result_score_card")
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color(result.gradeColorHex).copy(alpha = 0.15f),
                                        CardSurface
                                    )
                                )
                            )
                            .padding(22.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Subject Code Badge
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = CardSurfaceElevated,
                                border = BorderStroke(1.dp, CardSurfaceBorder)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(text = result.subject.iconEmoji, fontSize = 16.sp)
                                    Text(
                                        text = "${result.subject.title.uppercase()} • ${result.examType.title.uppercase()}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = TextSecondary
                                    )
                                }
                            }

                            // Big Scaled Score
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "${result.scaledScore}",
                                    style = MaterialTheme.typography.displayLarge,
                                    fontWeight = FontWeight.Black,
                                    color = Color(result.gradeColorHex),
                                    fontSize = 64.sp
                                )
                                Text(
                                    text = " / 100",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = TextMuted,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )
                            }

                            // Percentage & Equivalent
                            Text(
                                text = "Scaled UTME Score: ${result.percentageScore.toInt()}% (${result.correctCount} of ${result.totalQuestions} Correct)",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimary,
                                fontWeight = FontWeight.SemiBold
                            )

                            // Performance Tier Pill
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(result.gradeColorHex).copy(alpha = 0.2f),
                                border = BorderStroke(1.dp, Color(result.gradeColorHex))
                            ) {
                                Text(
                                    text = result.performanceTier,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Black,
                                    color = Color(result.gradeColorHex),
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Metric Breakdown Grid
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "EXAMINATION METRICS BREAKDOWN",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary,
                        letterSpacing = 1.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricCard(
                            label = "Correct",
                            value = "${result.correctCount}",
                            icon = Icons.Default.CheckCircle,
                            color = JambGreenLight,
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            label = "Incorrect",
                            value = "${result.incorrectCount}",
                            icon = Icons.Default.Cancel,
                            color = NeonRed,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricCard(
                            label = "Unanswered",
                            value = "${result.unansweredCount}",
                            icon = Icons.Default.HelpOutline,
                            color = TextMuted,
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            label = "Flagged",
                            value = "${result.flaggedQuestionIndices.size}",
                            icon = Icons.Default.Flag,
                            color = JambFlagAmber,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val mins = result.totalTimeSpentSeconds / 60
                        val secs = result.totalTimeSpentSeconds % 60
                        MetricCard(
                            label = "Total Time",
                            value = String.format("%02d:%02d", mins, secs),
                            icon = Icons.Default.Timer,
                            color = NeonCyan,
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            label = "Avg / Question",
                            value = "${result.averageTimePerQuestionSeconds.toInt()}s",
                            icon = Icons.Default.Speed,
                            color = NeonGold,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // UTME Recommendation & Feedback
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = CardSurfaceElevated,
                    border = BorderStroke(1.dp, CardSurfaceBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ADMISSION INSIGHT",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = NeonCyan
                            )
                        }

                        val feedbackText = when {
                            result.percentageScore >= 80f ->
                                "Outstanding mastery! A score of ${result.scaledScore}/100 puts you in the top percentile of UTME candidates for competitive programs (Medicine, Law, Engineering, Computer Science)."
                            result.percentageScore >= 65f ->
                                "Solid performance! You are on track for a high UTME aggregate. Review missed questions to tighten speed and eliminate careless errors."
                            result.percentageScore >= 50f ->
                                "Passing credit achieved. Targeted revision of high-yield topics is recommended to push your aggregate above 70%."
                            else ->
                                "Needs consistent drills. Tap 'Review Answers' below to study detailed explanations for every question and learn the core principles."
                        }

                        Text(
                            text = feedbackText,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            lineHeight = 20.sp
                        )
                    }
                }
            }

            // Action Buttons
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Primary Review Button
                    Button(
                        onClick = onReviewAnswers,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("jamb_review_answers_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonCyan
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "REVIEW ANSWERS & EXPLANATIONS",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Black,
                                color = Color.Black
                            )
                        }
                    }

                    // Retake Drill Button
                    OutlinedButton(
                        onClick = onRetakeExam,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("jamb_retake_exam_button"),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.5.dp, JambGreenLight),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = JambGreenLight
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Replay,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "START NEW CBT DRILL",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Back to Dashboard Button
                    TextButton(
                        onClick = onBackToHome,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("jamb_back_home_button")
                    ) {
                        Text(
                            text = "BACK TO HOME DASHBOARD",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun MetricCard(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = CardSurface,
        border = BorderStroke(1.dp, CardSurfaceBorder),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(color.copy(alpha = 0.15f))
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(18.dp)
                )
            }
            Column {
                Text(text = label, style = MaterialTheme.typography.labelSmall, color = TextMuted)
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = color
                )
            }
        }
    }
}
