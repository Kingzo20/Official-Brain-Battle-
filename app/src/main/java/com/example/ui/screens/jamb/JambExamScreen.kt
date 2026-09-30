package com.example.ui.screens.jamb

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.jamb.JambQuestionRepository
import com.example.model.jamb.JambExamResult
import com.example.model.jamb.JambExamType
import com.example.model.jamb.JambQuestion
import com.example.model.jamb.JambSubject
import com.example.ui.theme.*
import kotlinx.coroutines.delay

private val JambGreen = Color(0xFF008751)
private val JambGreenLight = Color(0xFF00C853)
private val JambFlagAmber = Color(0xFFFF9100)
private val JambUnansweredGray = Color(0xFF424242)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JambExamScreen(
    subject: JambSubject,
    examType: JambExamType,
    jambRepository: JambQuestionRepository,
    currentUserId: String = "",
    onExamFinished: (JambExamResult) -> Unit,
    onExitExam: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Load questions using no-repeat logic
    val questions = remember(subject, examType) {
        jambRepository.getQuestions(subject = subject, examType = examType, uid = currentUserId)
    }

    var currentQuestionIndex by remember { mutableStateOf(0) }
    val userAnswers = remember { mutableStateMapOf<Int, String>() }
    val flaggedQuestionIndices = remember { mutableStateListOf<Int>() }

    // Timer logic
    val totalTimeSeconds = examType.durationSeconds
    var remainingSeconds by remember { mutableStateOf(totalTimeSeconds) }
    var isTimerActive by remember { mutableStateOf(true) }
    var timeSpentSeconds by remember { mutableStateOf(0) }

    // Dialog & UI states
    var showSubmitConfirmationDialog by remember { mutableStateOf(false) }
    var showExitConfirmationDialog by remember { mutableStateOf(false) }
    var showPaletteSheet by remember { mutableStateOf(false) }
    var isTimeExpired by remember { mutableStateOf(false) }
    var isPassageExpanded by remember { mutableStateOf(true) }

    val currentQuestion = questions.getOrNull(currentQuestionIndex)

    // Countdown effect
    LaunchedEffect(isTimerActive) {
        while (isTimerActive && remainingSeconds > 0) {
            delay(1000L)
            remainingSeconds--
            timeSpentSeconds++
            if (remainingSeconds <= 0) {
                isTimerActive = false
                isTimeExpired = true
            }
        }
    }

    fun submitExamNow() {
        isTimerActive = false
        val finalResult = jambRepository.calculateResult(
            subject = subject,
            examType = examType,
            questions = questions,
            userAnswers = userAnswers.toMap(),
            timeSpentSeconds = timeSpentSeconds,
            flaggedIndices = flaggedQuestionIndices.toSet()
        )
        // Record answered question history
        jambRepository.recordExamQuestionsAnswered(
            questions = questions,
            subject = subject,
            uid = currentUserId
        )
        onExamFinished(finalResult)
    }

    // Auto submit on time expiration
    LaunchedEffect(isTimeExpired) {
        if (isTimeExpired) {
            submitExamNow()
        }
    }

    // Intercept back button to confirm exit
    BackHandler {
        showExitConfirmationDialog = true
    }

    Scaffold(
        containerColor = BackgroundDark,
        topBar = {
            Surface(
                color = CardSurface,
                border = BorderStroke(1.dp, CardSurfaceBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("jamb_cbt_top_bar")
            ) {
                Column(
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Subject Tag & Icon
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(text = subject.iconEmoji, fontSize = 20.sp)
                            Column {
                                Text(
                                    text = subject.shortName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = JambGreenLight
                                )
                                Text(
                                    text = "Q ${currentQuestionIndex + 1} of ${questions.size}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary
                                )
                            }
                        }

                        // Countdown Timer Badge
                        val isLowTime = remainingSeconds <= 300 // Under 5 minutes
                        val hours = remainingSeconds / 3600
                        val minutes = (remainingSeconds % 3600) / 60
                        val seconds = remainingSeconds % 60
                        val timerText = if (hours > 0) {
                            String.format("%02d:%02d:%02d", hours, minutes, seconds)
                        } else {
                            String.format("%02d:%02d", minutes, seconds)
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isLowTime) NeonRed.copy(alpha = 0.2f) else CardSurfaceElevated,
                            border = BorderStroke(
                                1.5.dp,
                                if (isLowTime) NeonRed else JambGreenLight.copy(alpha = 0.6f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = "Timer",
                                    tint = if (isLowTime) NeonRed else TextPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = timerText,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Black,
                                    color = if (isLowTime) NeonRed else TextPrimary
                                )
                            }
                        }

                        // Submit Button
                        Button(
                            onClick = { showSubmitConfirmationDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NeonRed.copy(alpha = 0.85f)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("jamb_submit_exam_top_button")
                        ) {
                            Text(
                                text = "SUBMIT",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            // Authentic CBT Bottom Navigation Bar
            Surface(
                color = CardSurface,
                border = BorderStroke(1.dp, CardSurfaceBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("jamb_bottom_navigation")
            ) {
                Column(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Previous Button
                        OutlinedButton(
                            onClick = {
                                if (currentQuestionIndex > 0) currentQuestionIndex--
                            },
                            enabled = currentQuestionIndex > 0,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, if (currentQuestionIndex > 0) TextSecondary else CardSurfaceBorder),
                            modifier = Modifier.testTag("jamb_btn_previous")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Previous",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "PREV",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Flag for Review Button
                        val isFlagged = flaggedQuestionIndices.contains(currentQuestionIndex)
                        Button(
                            onClick = {
                                if (isFlagged) {
                                    flaggedQuestionIndices.remove(currentQuestionIndex)
                                } else {
                                    flaggedQuestionIndices.add(currentQuestionIndex)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isFlagged) JambFlagAmber else CardSurfaceElevated
                            ),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(
                                1.dp,
                                if (isFlagged) JambFlagAmber else CardSurfaceBorder
                            ),
                            modifier = Modifier.testTag("jamb_btn_flag")
                        ) {
                            Icon(
                                imageVector = if (isFlagged) Icons.Default.Flag else Icons.Default.OutlinedFlag,
                                contentDescription = "Flag",
                                tint = if (isFlagged) Color.Black else JambFlagAmber,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isFlagged) "FLAGGED" else "FLAG",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isFlagged) Color.Black else TextPrimary
                            )
                        }

                        // Palette Toggle Button
                        OutlinedButton(
                            onClick = { showPaletteSheet = true },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, JambGreenLight),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = JambGreenLight
                            ),
                            modifier = Modifier.testTag("jamb_btn_palette")
                        ) {
                            Icon(
                                imageVector = Icons.Default.GridView,
                                contentDescription = "Palette",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${userAnswers.size}/${questions.size}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Next / Submit Button
                        val isLastQuestion = currentQuestionIndex == questions.size - 1
                        Button(
                            onClick = {
                                if (isLastQuestion) {
                                    showSubmitConfirmationDialog = true
                                } else {
                                    currentQuestionIndex++
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isLastQuestion) NeonGreen else JambGreenLight
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("jamb_btn_next")
                        ) {
                            Text(
                                text = if (isLastQuestion) "FINISH" else "NEXT",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Black,
                                color = Color.Black
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = if (isLastQuestion) Icons.Default.Check else Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Next",
                                tint = Color.Black,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        },
        modifier = modifier.testTag("jamb_exam_screen")
    ) { innerPadding ->
        if (questions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = JambGreenLight)
            }
        } else if (currentQuestion != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Question Header Card
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = CardSurface,
                    border = BorderStroke(1.dp, CardSurfaceBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = JambGreen.copy(alpha = 0.25f),
                                border = BorderStroke(1.dp, JambGreenLight)
                            ) {
                                Text(
                                    text = "QUESTION ${currentQuestionIndex + 1}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Black,
                                    color = JambGreenLight,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            Text(
                                text = currentQuestion.year,
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                        }

                        if (flaggedQuestionIndices.contains(currentQuestionIndex)) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = JambFlagAmber.copy(alpha = 0.2f),
                                border = BorderStroke(1.dp, JambFlagAmber)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Flag,
                                        contentDescription = "Flagged",
                                        tint = JambFlagAmber,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = "FLAGGED",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = JambFlagAmber,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // Comprehension Passage (if question contains one)
                currentQuestion.passage?.let { passageText ->
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = CardSurfaceElevated,
                        border = BorderStroke(1.dp, CardSurfaceBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { isPassageExpanded = !isPassageExpanded },
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MenuBook,
                                        contentDescription = null,
                                        tint = NeonCyan,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "READING PASSAGE",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = NeonCyan
                                    )
                                }
                                Icon(
                                    imageVector = if (isPassageExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Toggle passage",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            AnimatedVisibility(visible = isPassageExpanded) {
                                Column {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = passageText,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextPrimary,
                                        lineHeight = 22.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // Question Stem
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = CardSurface,
                    border = BorderStroke(1.dp, CardSurfaceBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = currentQuestion.questionText,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        lineHeight = 24.sp,
                        modifier = Modifier
                            .padding(18.dp)
                            .testTag("jamb_question_text")
                    )
                }

                // Multiple-Choice Options: A, B, C, D
                val options = listOf(
                    "A" to currentQuestion.optionA,
                    "B" to currentQuestion.optionB,
                    "C" to currentQuestion.optionC,
                    "D" to currentQuestion.optionD
                )

                val selectedOption = userAnswers[currentQuestionIndex]

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    options.forEach { (optionKey, optionText) ->
                        val isChosen = selectedOption.equals(optionKey, ignoreCase = true)

                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (isChosen) JambGreen.copy(alpha = 0.25f) else CardSurface,
                            border = BorderStroke(
                                width = if (isChosen) 2.dp else 1.dp,
                                color = if (isChosen) JambGreenLight else CardSurfaceBorder
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    userAnswers[currentQuestionIndex] = optionKey
                                }
                                .testTag("jamb_option_${optionKey}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(14.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                // Option letter pill
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isChosen) JambGreenLight else CardSurfaceElevated
                                        )
                                        .border(
                                            1.dp,
                                            if (isChosen) JambGreenLight else CardSurfaceBorder,
                                            CircleShape
                                        )
                                ) {
                                    Text(
                                        text = optionKey,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Black,
                                        color = if (isChosen) Color.Black else TextSecondary
                                    )
                                }

                                // Option text
                                Text(
                                    text = optionText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isChosen) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isChosen) TextPrimary else TextSecondary,
                                    lineHeight = 20.sp,
                                    modifier = Modifier.weight(1f)
                                )

                                if (isChosen) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Selected",
                                        tint = JambGreenLight,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    // Question Palette BottomSheet / Dialog
    if (showPaletteSheet) {
        ModalBottomSheet(
            onDismissRequest = { showPaletteSheet = false },
            containerColor = CardSurfaceElevated,
            dragHandle = { BottomSheetDefaults.DragHandle() },
            modifier = Modifier.testTag("jamb_palette_sheet")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "QUESTION PALETTE",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary
                    )
                    Text(
                        text = "${userAnswers.size}/${questions.size} Answered",
                        style = MaterialTheme.typography.labelMedium,
                        color = JambGreenLight,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Palette Legend
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(modifier = Modifier.size(12.dp).background(JambGreenLight, RoundedCornerShape(3.dp)))
                        Text("Answered", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(modifier = Modifier.size(12.dp).background(JambFlagAmber, RoundedCornerShape(3.dp)))
                        Text("Flagged", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(modifier = Modifier.size(12.dp).background(JambUnansweredGray, RoundedCornerShape(3.dp)))
                        Text("Unanswered", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    }
                }

                Divider(color = CardSurfaceBorder)

                // Grid of questions
                LazyVerticalGrid(
                    columns = GridCells.Fixed(5),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 320.dp)
                ) {
                    itemsIndexed(questions) { index, _ ->
                        val isAnswered = userAnswers.containsKey(index)
                        val isFlagged = flaggedQuestionIndices.contains(index)
                        val isCurrent = index == currentQuestionIndex

                        val boxColor = when {
                            isFlagged -> JambFlagAmber
                            isAnswered -> JambGreenLight
                            else -> JambUnansweredGray
                        }

                        val textColor = when {
                            isFlagged -> Color.Black
                            isAnswered -> Color.Black
                            else -> TextSecondary
                        }

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(boxColor)
                                .border(
                                    width = if (isCurrent) 2.5.dp else 0.dp,
                                    color = if (isCurrent) Color.White else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    currentQuestionIndex = index
                                    showPaletteSheet = false
                                }
                                .testTag("jamb_palette_q_${index + 1}")
                        ) {
                            Text(
                                text = "${index + 1}",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Black,
                                color = textColor
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }

    // Submit Exam Confirmation Dialog
    if (showSubmitConfirmationDialog) {
        val total = questions.size
        val answered = userAnswers.size
        val unanswered = total - answered
        val flagged = flaggedQuestionIndices.size

        AlertDialog(
            onDismissRequest = { showSubmitConfirmationDialog = false },
            title = {
                Text(
                    text = "Submit CBT Examination?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Are you sure you want to finish and submit your test? Here is your current progress:",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = CardSurfaceElevated,
                        border = BorderStroke(1.dp, CardSurfaceBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Answered Questions:", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                Text("$answered / $total", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = JambGreenLight)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Unanswered Questions:", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                Text("$unanswered", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = if (unanswered > 0) NeonAmber else TextSecondary)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Flagged for Review:", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                Text("$flagged", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = if (flagged > 0) JambFlagAmber else TextSecondary)
                            }
                        }
                    }

                    if (unanswered > 0) {
                        Text(
                            text = "⚠️ Warning: You still have $unanswered unanswered question(s). Unanswered questions receive zero marks.",
                            style = MaterialTheme.typography.bodySmall,
                            color = NeonAmber,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSubmitConfirmationDialog = false
                        submitExamNow()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = JambGreenLight),
                    modifier = Modifier.testTag("jamb_confirm_submit_button")
                ) {
                    Text("CONFIRM & SUBMIT", fontWeight = FontWeight.Black, color = Color.Black)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showSubmitConfirmationDialog = false },
                    modifier = Modifier.testTag("jamb_cancel_submit_button")
                ) {
                    Text("CONTINUE TEST", color = TextSecondary)
                }
            },
            containerColor = CardSurface,
            shape = RoundedCornerShape(18.dp)
        )
    }

    // Exit Exam Dialog
    if (showExitConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { showExitConfirmationDialog = false },
            title = {
                Text("Abandon Exam?", fontWeight = FontWeight.Black)
            },
            text = {
                Text(
                    "Exiting will terminate this CBT drill without saving your exam score. Do you wish to leave or continue?",
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showExitConfirmationDialog = false
                        isTimerActive = false
                        onExitExam()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonRed)
                ) {
                    Text("EXIT TEST", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitConfirmationDialog = false }) {
                    Text("RESUME", color = TextPrimary)
                }
            },
            containerColor = CardSurface,
            shape = RoundedCornerShape(18.dp)
        )
    }
}
