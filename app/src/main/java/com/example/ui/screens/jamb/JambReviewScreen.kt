package com.example.ui.screens.jamb

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.example.model.jamb.JambMultiSubjectExamResult
import com.example.model.jamb.JambQuestion
import com.example.model.jamb.JambSubject
import com.example.ui.theme.*

private val JambGreen = Color(0xFF008751)
private val JambGreenLight = Color(0xFF00C853)
private val JambFlagAmber = Color(0xFFFF9100)
private val JambGold = Color(0xFFFFB300)

private enum class AccuracyFilter(val title: String) {
    ALL("All"),
    CORRECT("Correct"),
    WRONG("Wrong"),
    OMITTED("Omitted"),
    FLAGGED("Flagged")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JambReviewScreen(
    singleResult: JambExamResult? = null,
    multiResult: JambMultiSubjectExamResult? = null,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isMulti = multiResult != null

    // Subjects available for review
    val availableSubjects = remember(isMulti, singleResult, multiResult) {
        if (isMulti) multiResult!!.subjectResults.keys.toList()
        else listOfNotNull(singleResult?.subject)
    }

    var activeSubjectIndex by remember { mutableStateOf(0) }
    val currentSubject = availableSubjects.getOrElse(activeSubjectIndex) { availableSubjects.firstOrNull() ?: JambSubject.ENGLISH }

    // Active result for currently chosen subject
    val activeResult: JambExamResult? = if (isMulti) {
        multiResult?.subjectResults?.get(currentSubject)
    } else {
        singleResult
    }

    var selectedAccuracyFilter by remember { mutableStateOf(AccuracyFilter.ALL) }
    var selectedTopicFilter by remember { mutableStateOf("All Topics") }
    var selectedQuestionIndex by remember { mutableStateOf(0) }
    var isExplanationExpanded by remember { mutableStateOf(true) }

    // Distinct topics in this subject's questions
    val availableTopics = remember(activeResult) {
        val topics = activeResult?.questions?.map { it.topic }?.distinct() ?: emptyList()
        listOf("All Topics") + topics
    }

    // Filter questions
    val filteredIndices = remember(selectedAccuracyFilter, selectedTopicFilter, activeResult) {
        if (activeResult == null) emptyList()
        else {
            activeResult.questions.indices.filter { idx ->
                val q = activeResult.questions[idx]
                val userChoice = activeResult.userAnswers[idx]
                val isCorrect = userChoice != null && userChoice.equals(q.correctOption, ignoreCase = true)
                val isWrong = userChoice != null && !userChoice.equals(q.correctOption, ignoreCase = true)
                val isOmitted = userChoice == null
                val isFlagged = activeResult.flaggedQuestionIndices.contains(idx)

                val matchesAccuracy = when (selectedAccuracyFilter) {
                    AccuracyFilter.ALL -> true
                    AccuracyFilter.CORRECT -> isCorrect
                    AccuracyFilter.WRONG -> isWrong
                    AccuracyFilter.OMITTED -> isOmitted
                    AccuracyFilter.FLAGGED -> isFlagged
                }

                val matchesTopic = selectedTopicFilter == "All Topics" || q.topic == selectedTopicFilter

                matchesAccuracy && matchesTopic
            }
        }
    }

    val currentQuestionIdx = if (filteredIndices.contains(selectedQuestionIndex)) {
        selectedQuestionIndex
    } else {
        filteredIndices.firstOrNull() ?: 0
    }

    val currentQuestion = activeResult?.questions?.getOrNull(currentQuestionIdx)

    Scaffold(
        containerColor = BackgroundDark,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "AI STEP-BY-STEP REVIEW",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = TextPrimary
                            )
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = NeonCyan.copy(alpha = 0.2f),
                                border = BorderStroke(1.dp, NeonCyan)
                            ) {
                                Text(
                                    text = "AI TUTOR",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = NeonCyan,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "${currentSubject.title} • Score: ${activeResult?.scaledScore ?: 0}/100",
                            style = MaterialTheme.typography.labelSmall,
                            color = JambGreenLight
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("jamb_review_back_button")) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundDark)
            )
        },
        bottomBar = {
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
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous", modifier = Modifier.size(16.dp))
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
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next", tint = Color.Black, modifier = Modifier.size(16.dp))
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
            // If 4-Subject Mock: Subject Selector Row
            if (isMulti && availableSubjects.size > 1) {
                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth().testTag("jamb_review_subject_tabs")
                    ) {
                        itemsIndexed(availableSubjects) { idx, subj ->
                            val isSelected = idx == activeSubjectIndex
                            val subRes = multiResult?.subjectResults?.get(subj)
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) JambGreenLight.copy(alpha = 0.2f) else CardSurface,
                                border = BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) JambGreenLight else CardSurfaceBorder),
                                modifier = Modifier.clickable {
                                    activeSubjectIndex = idx
                                    selectedQuestionIndex = 0
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(text = subj.iconEmoji, fontSize = 16.sp)
                                    Text(
                                        text = "${subj.shortName} (${subRes?.scaledScore ?: 0}/100)",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                        color = if (isSelected) JambGreenLight else TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Accuracy Filter Pills Row: All, Correct, Wrong, Omitted, Flagged
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().testTag("jamb_review_accuracy_filters")
                ) {
                    items(AccuracyFilter.values().size) { idx ->
                        val filter = AccuracyFilter.values()[idx]
                        val isSelected = selectedAccuracyFilter == filter
                        val count = when (filter) {
                            AccuracyFilter.ALL -> activeResult?.totalQuestions ?: 0
                            AccuracyFilter.CORRECT -> activeResult?.correctCount ?: 0
                            AccuracyFilter.WRONG -> activeResult?.incorrectCount ?: 0
                            AccuracyFilter.OMITTED -> activeResult?.unansweredCount ?: 0
                            AccuracyFilter.FLAGGED -> activeResult?.flaggedQuestionIndices?.size ?: 0
                        }

                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) NeonCyan.copy(alpha = 0.2f) else CardSurface,
                            border = BorderStroke(1.dp, if (isSelected) NeonCyan else CardSurfaceBorder),
                            modifier = Modifier
                                .clickable { selectedAccuracyFilter = filter }
                                .testTag("jamb_filter_${filter.name.lowercase()}")
                        ) {
                            Text(
                                text = "${filter.title} ($count)",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.SemiBold,
                                color = if (isSelected) NeonCyan else TextSecondary,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                            )
                        }
                    }
                }
            }

            // Topic / Syllabus Filter Row
            if (availableTopics.size > 2) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(imageVector = Icons.Default.FilterList, contentDescription = null, tint = TextMuted, modifier = Modifier.size(14.dp))
                            Text(text = "SYLLABUS TOPIC FILTER:", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontWeight = FontWeight.Bold)
                        }
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth().testTag("jamb_review_topic_filters")
                        ) {
                            items(availableTopics) { topicName ->
                                val isSelected = selectedTopicFilter == topicName
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) JambGold.copy(alpha = 0.2f) else CardSurfaceElevated,
                                    border = BorderStroke(1.dp, if (isSelected) JambGold else CardSurfaceBorder),
                                    modifier = Modifier.clickable { selectedTopicFilter = topicName }
                                ) {
                                    Text(
                                        text = topicName,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Normal,
                                        color = if (isSelected) JambGold else TextSecondary,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                    )
                                }
                            }
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
                        val userChoice = activeResult?.userAnswers?.get(qIdx)
                        val correctKey = activeResult?.questions?.getOrNull(qIdx)?.correctOption
                        val isCorrect = userChoice != null && userChoice.equals(correctKey, ignoreCase = true)
                        val isOmitted = userChoice == null

                        val boxBg = when {
                            isCorrect -> JambGreenLight
                            isOmitted -> CardSurfaceElevated
                            else -> NeonRed
                        }

                        val textColor = when {
                            isCorrect -> Color.Black
                            isOmitted -> TextSecondary
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
                        modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
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
                val userChoice = activeResult?.userAnswers?.get(currentQuestionIdx)
                val correctKey = currentQuestion.correctOption
                val isCorrect = userChoice != null && userChoice.equals(correctKey, ignoreCase = true)
                val isOmitted = userChoice == null

                // Status Banner: Correct, Wrong, or Omitted
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = when {
                            isCorrect -> JambGreenLight.copy(alpha = 0.15f)
                            isOmitted -> CardSurfaceElevated
                            else -> NeonRed.copy(alpha = 0.15f)
                        },
                        border = BorderStroke(
                            1.dp,
                            when {
                                isCorrect -> JambGreenLight
                                isOmitted -> CardSurfaceBorder
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
                                        isOmitted -> Icons.Default.HelpOutline
                                        else -> Icons.Default.Cancel
                                    },
                                    contentDescription = null,
                                    tint = when {
                                        isCorrect -> JambGreenLight
                                        isOmitted -> TextMuted
                                        else -> NeonRed
                                    },
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = when {
                                        isCorrect -> "YOUR ANSWER: OPTION $userChoice (CORRECT ✓)"
                                        isOmitted -> "QUESTION WAS OMITTED (0 MARKS)"
                                        else -> "YOUR ANSWER: OPTION $userChoice (WRONG ✗)"
                                    },
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Black,
                                    color = when {
                                        isCorrect -> JambGreenLight
                                        isOmitted -> TextSecondary
                                        else -> NeonRed
                                    }
                                )
                            }

                            Text(text = currentQuestion.topic, style = MaterialTheme.typography.labelSmall, color = TextMuted)
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
                                    text = "READING PASSAGE / NOVEL EXCERPT",
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
                                        text = "${currentSubject.shortName} • Q ${currentQuestionIdx + 1}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Black,
                                        color = JambGreenLight,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Text(text = currentQuestion.year, style = MaterialTheme.typography.labelSmall, color = TextMuted)
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
                                                text = "✓ Official Correct Key",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = JambGreenLight,
                                                fontWeight = FontWeight.Bold
                                            )
                                        } else if (isUserSelected) {
                                            Text(
                                                text = "✗ Your Selection",
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

                // AI "Explain Step-by-Step" Expander Card
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = CardSurfaceElevated,
                        border = BorderStroke(1.5.dp, NeonCyan.copy(alpha = 0.7f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("jamb_ai_explanation_card")
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { isExplanationExpanded = !isExplanationExpanded },
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(imageVector = Icons.Default.Psychology, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(22.dp))
                                    Text(
                                        text = "EXPLAIN STEP-BY-STEP",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Black,
                                        color = NeonCyan,
                                        letterSpacing = 1.sp
                                    )
                                }
                                Icon(
                                    imageVector = if (isExplanationExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Toggle",
                                    tint = TextSecondary
                                )
                            }

                            AnimatedVisibility(visible = isExplanationExpanded) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Divider(color = CardSurfaceBorder)

                                    // Formulas / Grammatical Rule Card (if present)
                                    val formulaOrRule = currentQuestion.formulaOrRuleUsed
                                        ?: when (currentSubject) {
                                            JambSubject.MATHEMATICS -> "Formulas: Algebraic Factorization / Calculus / Trigonometric identities"
                                            JambSubject.PHYSICS -> "Laws: Newton's Laws / Kinematics / Thermodynamics"
                                            JambSubject.CHEMISTRY -> "Concepts: Stoichiometry / Periodic Trends / Gas Laws"
                                            JambSubject.ENGLISH -> "Rules: Grammatical Concord / Lexis & Structure / Figurative Language"
                                            JambSubject.BIOLOGY -> "Principles: Mendelian Genetics / Cellular Respiration / Taxonomy"
                                            JambSubject.ECONOMICS -> "Models: Price Elasticity / Micro & Macroeconomic Balance"
                                            JambSubject.GOVERNMENT -> "Theories: Constitutionalism / Separation of Powers / Public Admin"
                                            JambSubject.LITERATURE -> "Devices: Poetic Devices / Dramatic Irony / Prose Structure"
                                            JambSubject.CRK_IRS -> "Scriptural Reference & Monotheistic Ethical Law"
                                        }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = NeonGold.copy(alpha = 0.12f),
                                        border = BorderStroke(1.dp, NeonGold.copy(alpha = 0.5f))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.Rule, contentDescription = null, tint = NeonGold, modifier = Modifier.size(16.dp))
                                            Text(
                                                text = formulaOrRule,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = NeonGold
                                            )
                                        }
                                    }

                                    // Step by step breakdown
                                    val stepByStep = currentQuestion.stepByStepExplanation
                                        ?: "Step 1: Analyze question stem and parameters.\nStep 2: Apply the governing UTME syllabus rule/formula.\nStep 3: Eliminate distracting options.\nConclusion: Option ${currentQuestion.correctOption} is verified correct: ${currentQuestion.explanation}"

                                    Text(
                                        text = stepByStep,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextPrimary,
                                        lineHeight = 22.sp
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = JambGreen.copy(alpha = 0.15f),
                                        border = BorderStroke(1.dp, JambGreenLight.copy(alpha = 0.4f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Text(text = "RATIONALE SUMMARY:", style = MaterialTheme.typography.labelSmall, color = JambGreenLight, fontWeight = FontWeight.Bold)
                                            Text(text = currentQuestion.explanation, style = MaterialTheme.typography.bodySmall, color = TextSecondary, lineHeight = 18.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}
