package com.example.ui.screens.jamb

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.jamb.JambBenchmarkData
import com.example.model.jamb.JambExamResult
import com.example.model.jamb.JambMultiSubjectExamResult
import com.example.model.jamb.UniversityDepartmentCutOff
import com.example.ui.theme.*

private val JambGreenLight = Color(0xFF00C853)
private val JambFlagAmber = Color(0xFFFF9100)
private val JambGold = Color(0xFFFFB300)

@Composable
fun JambResultScreen(
    singleResult: JambExamResult? = null,
    multiResult: JambMultiSubjectExamResult? = null,
    onReviewAnswers: () -> Unit,
    onRetakeExam: () -> Unit,
    onBackToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isMulti = multiResult != null

    val aggregateScore = if (isMulti) {
        multiResult!!.aggregateScoreOutOf400
    } else {
        // Project single subject out of 400
        ((singleResult?.scaledScore ?: 0) * 4).coerceIn(0, 400)
    }

    val totalCorrect = if (isMulti) multiResult!!.totalCorrect else (singleResult?.correctCount ?: 0)
    val totalQuestions = if (isMulti) multiResult!!.totalQuestions else (singleResult?.totalQuestions ?: 0)
    val totalTimeSeconds = if (isMulti) multiResult!!.totalTimeSpentSeconds else (singleResult?.totalTimeSpentSeconds ?: 0)

    val performanceTier = if (isMulti) {
        multiResult!!.performanceTier
    } else {
        singleResult?.performanceTier ?: "UTME DRILL COMPLETED"
    }

    val gradeColor = when {
        aggregateScore >= 280 -> Color(0xFF00E676)
        aggregateScore >= 220 -> Color(0xFFFFB300)
        else -> Color(0xFFFF4365)
    }

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
                    border = BorderStroke(2.dp, gradeColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("jamb_result_score_card")
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        gradeColor.copy(alpha = 0.15f),
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
                            // Subject Tag Badge
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
                                    Text(
                                        text = if (isMulti) "🏆" else (singleResult?.subject?.iconEmoji ?: "📖"),
                                        fontSize = 16.sp
                                    )
                                    Text(
                                        text = if (isMulti) {
                                            "FULL 4-SUBJECT UTME MOCK (180 QS)"
                                        } else {
                                            "${singleResult?.subject?.title?.uppercase()} • ${singleResult?.examType?.title?.uppercase()}"
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = TextSecondary
                                    )
                                }
                            }

                            // Big Scaled Score
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = if (isMulti) "$aggregateScore" else "${singleResult?.scaledScore ?: 0}",
                                    style = MaterialTheme.typography.displayLarge,
                                    fontWeight = FontWeight.Black,
                                    color = gradeColor,
                                    fontSize = 64.sp
                                )
                                Text(
                                    text = if (isMulti) " / 400" else " / 100",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = TextMuted,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )
                            }

                            // Percentage & Performance Tier
                            Text(
                                text = if (isMulti) {
                                    "Total Aggregate: $aggregateScore / 400 ($totalCorrect of $totalQuestions Correct)"
                                } else {
                                    "Scaled Score: ${singleResult?.percentageScore?.toInt()}% ($totalCorrect of $totalQuestions Correct)"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimary,
                                fontWeight = FontWeight.SemiBold
                            )

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = gradeColor.copy(alpha = 0.2f),
                                border = BorderStroke(1.dp, gradeColor)
                            ) {
                                Text(
                                    text = performanceTier,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Black,
                                    color = gradeColor,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            // If 4-Subject Mock: Show individual paper cards
            if (isMulti && multiResult != null) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "4-SUBJECT BREAKDOWN (SCALED TO 100 EACH)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Black,
                            color = TextPrimary,
                            letterSpacing = 1.sp
                        )

                        multiResult.subjectResults.forEach { (subj, subResult) ->
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = CardSurface,
                                border = BorderStroke(1.dp, CardSurfaceBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(14.dp)
                                        .fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Text(text = subj.iconEmoji, fontSize = 24.sp)
                                        Column {
                                            Text(
                                                text = subj.title,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = TextPrimary
                                            )
                                            Text(
                                                text = "${subResult.correctCount}/${subResult.totalQuestions} Correct • ${subResult.unansweredCount} Omitted",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = TextSecondary
                                            )
                                        }
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "${subResult.scaledScore}",
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Black,
                                            color = Color(subResult.gradeColorHex)
                                        )
                                        Text(
                                            text = "/ 100",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextMuted
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // General Metrics Breakdown
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "EXAMINATION METRICS BREAKDOWN",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary,
                        letterSpacing = 1.sp
                    )

                    val correctCnt = if (isMulti) multiResult!!.totalCorrect else (singleResult?.correctCount ?: 0)
                    val wrongCnt = if (isMulti) multiResult!!.totalIncorrect else (singleResult?.incorrectCount ?: 0)
                    val omittedCnt = if (isMulti) multiResult!!.totalUnanswered else (singleResult?.unansweredCount ?: 0)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricCard(
                            label = "Correct",
                            value = "$correctCnt",
                            icon = Icons.Default.CheckCircle,
                            color = JambGreenLight,
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            label = "Wrong",
                            value = "$wrongCnt",
                            icon = Icons.Default.Cancel,
                            color = NeonRed,
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            label = "Omitted",
                            value = "$omittedCnt",
                            icon = Icons.Default.HelpOutline,
                            color = TextMuted,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val mins = totalTimeSeconds / 60
                        val secs = totalTimeSeconds % 60
                        MetricCard(
                            label = "Total Time",
                            value = String.format("%02d:%02d", mins, secs),
                            icon = Icons.Default.Timer,
                            color = NeonCyan,
                            modifier = Modifier.weight(1f)
                        )
                        val avgSec = if (totalQuestions > 0) totalTimeSeconds / totalQuestions else 0
                        MetricCard(
                            label = "Avg / Question",
                            value = "${avgSec}s",
                            icon = Icons.Default.Speed,
                            color = NeonGold,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // UTME Departmental Cut-off Predictor & Benchmark
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "DEPARTMENTAL ADMISSION PREDICTOR",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Black,
                            color = TextPrimary,
                            letterSpacing = 1.sp
                        )
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = JambGold.copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, JambGold)
                        ) {
                            Text(
                                text = "NIGERIAN CUT-OFFS",
                                style = MaterialTheme.typography.labelSmall,
                                color = JambGold,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    // Cutoff Cards
                    val cutoffs = JambBenchmarkData.OFFICIAL_CUTOFFS
                    cutoffs.forEach { cutoff ->
                        val isQualified = aggregateScore >= cutoff.minimumScore
                        val isHighlyCompetitive = aggregateScore >= cutoff.competitiveScore

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isQualified) CardSurface else CardSurfaceElevated,
                            border = BorderStroke(
                                width = if (isHighlyCompetitive) 1.5.dp else 1.dp,
                                color = if (isHighlyCompetitive) JambGreenLight else if (isQualified) NeonCyan else CardSurfaceBorder
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(text = cutoff.iconEmoji, fontSize = 20.sp)
                                        Column {
                                            Text(
                                                text = cutoff.courseName,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = TextPrimary
                                            )
                                            Text(
                                                text = "${cutoff.faculty} • Min Cut-off: ${cutoff.minimumScore} | Safe: ${cutoff.competitiveScore}+",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = TextSecondary
                                            )
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = when {
                                            isHighlyCompetitive -> JambGreenLight.copy(alpha = 0.2f)
                                            isQualified -> NeonCyan.copy(alpha = 0.2f)
                                            else -> NeonRed.copy(alpha = 0.2f)
                                        },
                                        border = BorderStroke(
                                            1.dp,
                                            when {
                                                isHighlyCompetitive -> JambGreenLight
                                                isQualified -> NeonCyan
                                                else -> NeonRed
                                            }
                                        )
                                    ) {
                                        Text(
                                            text = when {
                                                isHighlyCompetitive -> "HIGH CHANCE ✓"
                                                isQualified -> "ELIGIBLE"
                                                else -> "NEED +${cutoff.minimumScore - aggregateScore} PTS"
                                            },
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Black,
                                            color = when {
                                                isHighlyCompetitive -> JambGreenLight
                                                isQualified -> NeonCyan
                                                else -> NeonRed
                                            },
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = cutoff.recommendation,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMuted,
                                    lineHeight = 16.sp
                                )
                            }
                        }
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
                                text = "EXPLAIN STEP-BY-STEP & REVIEW",
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
                            text = "START NEW CBT DRILL / MOCK",
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
            horizontalArrangement = Arrangement.spacedBy(8.dp)
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
