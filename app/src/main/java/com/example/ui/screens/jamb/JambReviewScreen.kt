package com.example.ui.screens.jamb

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.jamb.JambExamResult
import com.example.model.jamb.JambQuestion
import com.example.ui.theme.*

private val JambGreenLight = Color(0xFF00C853)
private val JambFlagAmber = Color(0xFFFF9100)

private enum class ReviewFilter(val title: String) {
    ALL("All"),
    INCORRECT("Incorrect"),
    CORRECT("Correct"),
    UNANSWERED("Unanswered"),
    FLAGGED("Flagged")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JambReviewScreen(
    result: JambExamResult,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf(ReviewFilter.ALL) }
    var selectedQuestionIndex by remember { mutableStateOf(0) }

    // Filter questions based on selected filter
    val filteredIndices = remember(selectedFilter, result) {
        result.questions.indices.filter { idx ->
            val userChoice = result.userAnswers[idx]
            val correctKey = result.questions[idx].correctOption
            when (selectedFilter) {
                ReviewFilter.ALL -> true
                ReviewFilter.CORRECT -> userChoice != null && userChoice.equals(correctKey, ignoreCase = true)
                ReviewFilter.INCORRECT -> userChoice != null && !userChoice.equals(correctKey, ignoreCase = true)
                ReviewFilter.UNANSWERED -> userChoice == null
                ReviewFilter.FLAGGED -> result.flaggedQuestionIndices.contains(idx)
            }
        }
    }

    // Keep active question index in bounds of filtered list
    val currentQuestionIdx = if (filteredIndices.contains(selectedQuestionIndex)) {
        selectedQuestionIndex
    } else {
        filteredIndices.firstOrNull() ?: 0
    }

    val currentQuestion = result.questions.getOrNull(currentQuestionIdx)

    Scaffold(
        containerColor = BackgroundDark,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "ANSWER EXPLANATIONS",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = TextPrimary
                        )
                        Text(
                            text = "${result.subject.title} • Score: ${result.scaledScore}/100",
                            style = MaterialTheme.typography.labelSmall,
                            color = JambGreenLight
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("jamb_review_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundDark
                )
            )
        },
        bottomBar = {
            // Next / Prev bottom navigator
            Surface(
                color = CardSurface,
                border = BorderStroke(1.dp, CardSurfaceBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val currentPosInFiltered = filteredIndices.indexOf(currentQuestionIdx)

                    OutlinedButton(
                        onClick = {
                            if (currentPosInFiltered > 0) {
                                selectedQuestionIndex = filteredIndices[currentPosInFiltered - 1]
                            }
                        },
                        enabled = currentPosInFiltered > 0,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("jamb_review_prev_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Previous",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("PREV", fontWeight = FontWeight.Bold)
                    }

                    Text(
                        text = if (filteredIndices.isNotEmpty()) {
                            "Question ${currentPosInFiltered + 1} of ${filteredIndices.size}"
                        } else "0 of 0",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary
                    )

                    Button(
                        onClick = {
                            if (currentPosInFiltered < filteredIndices.size - 1) {
                                selectedQuestionIndex = filteredIndices[currentPosInFiltered + 1]
                            }
                        },
                        enabled = currentPosInFiltered < filteredIndices.size - 1,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                        modifier = Modifier.testTag("jamb_review_next_button")
                    ) {
                        Text("NEXT", fontWeight = FontWeight.Bold, color = Color.Black)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Next",
                            tint = Color.Black,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        },
        modifier = modifier.testTag("jamb_review_screen")
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Filter Pills Row
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(ReviewFilter.values().size) { idx ->
                        val filter = ReviewFilter.values()[idx]
                        val isSelected = selectedFilter == filter
                        val count = when (filter) {
                            ReviewFilter.ALL -> result.totalQuestions
                            ReviewFilter.CORRECT -> result.correctCount
                            ReviewFilter.INCORRECT -> result.incorrectCount
                            ReviewFilter.UNANSWERED -> result.unansweredCount
                            ReviewFilter.FLAGGED -> result.flaggedQuestionIndices.size
                        }

                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) NeonCyan.copy(alpha = 0.2f) else CardSurface,
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) NeonCyan else CardSurfaceBorder
                            ),
                            modifier = Modifier
                                .clickable { selectedFilter = filter }
                                .testTag("jamb_filter_${filter.name.lowercase()}")
                        ) {
                            Text(
                                text = "${filter.title} ($count)",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.SemiBold,
                                color = if (isSelected) NeonCyan else TextSecondary,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            // Question Quick Jump Palette Row
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    itemsIndexed(filteredIndices) { _, qIdx ->
                        val isCurrent = qIdx == currentQuestionIdx
                        val userChoice = result.userAnswers[qIdx]
                        val correctKey = result.questions[qIdx].correctOption
                        val isCorrect = userChoice != null && userChoice.equals(correctKey, ignoreCase = true)
                        val isUnanswered = userChoice == null

                        val boxBg = when {
                            isCorrect -> JambGreenLight
                            isUnanswered -> CardSurfaceElevated
                            else -> NeonRed
                        }

                        val textColor = when {
                            isCorrect -> Color.Black
                            isUnanswered -> TextSecondary
                            else -> Color.White
                        }

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(boxBg)
                                .border(
                                    width = if (isCurrent) 2.5.dp else 1.dp,
                                    color = if (isCurrent) Color.White else CardSurfaceBorder,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedQuestionIndex = qIdx }
                                .testTag("jamb_review_q_${qIdx + 1}")
                        ) {
                            Text(
                                text = "${qIdx + 1}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Black,
                                color = textColor
                            )
                        }
                    }
                }
            }

            if (currentQuestion == null) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No questions found matching this filter.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            } else {
                val userChoice = result.userAnswers[currentQuestionIdx]
                val correctKey = currentQuestion.correctOption
                val isCorrect = userChoice != null && userChoice.equals(correctKey, ignoreCase = true)
                val isUnanswered = userChoice == null

                // Status Banner
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = when {
                            isCorrect -> JambGreenLight.copy(alpha = 0.15f)
                            isUnanswered -> CardSurfaceElevated
                            else -> NeonRed.copy(alpha = 0.15f)
                        },
                        border = BorderStroke(
                            1.dp,
                            when {
                                isCorrect -> JambGreenLight
                                isUnanswered -> CardSurfaceBorder
                                else -> NeonRed
                            }
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = when {
                                        isCorrect -> Icons.Default.CheckCircle
                                        isUnanswered -> Icons.Default.HelpOutline
                                        else -> Icons.Default.Cancel
                                    },
                                    contentDescription = null,
                                    tint = when {
                                        isCorrect -> JambGreenLight
                                        isUnanswered -> TextMuted
                                        else -> NeonRed
                                    },
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = when {
                                        isCorrect -> "YOUR ANSWER: OPTION $userChoice (CORRECT ✓)"
                                        isUnanswered -> "QUESTION WAS UNANSWERED (0 MARKS)"
                                        else -> "YOUR ANSWER: OPTION $userChoice (INCORRECT ✗)"
                                    },
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Black,
                                    color = when {
                                        isCorrect -> JambGreenLight
                                        isUnanswered -> TextSecondary
                                        else -> NeonRed
                                    }
                                )
                            }

                            Text(
                                text = currentQuestion.topic,
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                        }
                    }
                }

                // Comprehension Passage (if any)
                currentQuestion.passage?.let { passage ->
                    item {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = CardSurfaceElevated,
                            border = BorderStroke(1.dp, CardSurfaceBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "READING PASSAGE",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = NeonCyan
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = passage,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary,
                                    lineHeight = 20.sp
                                )
                            }
                        }
                    }
                }

                // Question Stem Card
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = CardSurface,
                        border = BorderStroke(1.dp, CardSurfaceBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = JambGreenLight.copy(alpha = 0.2f),
                                    border = BorderStroke(1.dp, JambGreenLight)
                                ) {
                                    Text(
                                        text = "Q ${currentQuestionIdx + 1}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Black,
                                        color = JambGreenLight,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Text(
                                    text = currentQuestion.year,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = currentQuestion.questionText,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary,
                                lineHeight = 24.sp
                            )
                        }
                    }
                }

                // Options with visual key status
                val options = listOf(
                    "A" to currentQuestion.optionA,
                    "B" to currentQuestion.optionB,
                    "C" to currentQuestion.optionC,
                    "D" to currentQuestion.optionD
                )

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        options.forEach { (key, text) ->
                            val isCorrectOption = key.equals(correctKey, ignoreCase = true)
                            val isUserSelected = key.equals(userChoice, ignoreCase = true)

                            val (borderColor, bgColor) = when {
                                isCorrectOption -> JambGreenLight to JambGreenLight.copy(alpha = 0.18f)
                                isUserSelected && !isCorrectOption -> NeonRed to NeonRed.copy(alpha = 0.18f)
                                else -> CardSurfaceBorder to CardSurface
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = bgColor,
                                border = BorderStroke(if (isCorrectOption || isUserSelected) 2.dp else 1.dp, borderColor),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(
                                                when {
                                                    isCorrectOption -> JambGreenLight
                                                    isUserSelected -> NeonRed
                                                    else -> CardSurfaceElevated
                                                }
                                            )
                                    ) {
                                        Text(
                                            text = key,
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Black,
                                            color = if (isCorrectOption || isUserSelected) Color.Black else TextSecondary
                                        )
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = text,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (isCorrectOption || isUserSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isCorrectOption) TextPrimary else TextSecondary,
                                            lineHeight = 20.sp
                                        )
                                        if (isCorrectOption) {
                                            Text(
                                                text = "✓ Official Correct Answer",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = JambGreenLight,
                                                fontWeight = FontWeight.Bold
                                            )
                                        } else if (isUserSelected) {
                                            Text(
                                                text = "✗ Your Answer",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = NeonRed,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Detailed Explanation Card
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = CardSurfaceElevated,
                        border = BorderStroke(1.5.dp, NeonCyan.copy(alpha = 0.6f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Lightbulb,
                                    contentDescription = null,
                                    tint = NeonGold,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "DETAILED EXPLANATION & UTME RATIONALE",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Black,
                                    color = NeonCyan,
                                    letterSpacing = 1.sp
                                )
                            }

                            Text(
                                text = currentQuestion.explanation,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimary,
                                lineHeight = 22.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}
