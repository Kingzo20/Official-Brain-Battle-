package com.example.ui.screens.jamb

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.jamb.JambQuestionRepository
import com.example.model.jamb.JambExamResult
import com.example.model.jamb.JambExamType
import com.example.model.jamb.JambMultiSubjectExamResult
import com.example.model.jamb.JambQuestion
import com.example.model.jamb.JambSubject
import com.example.ui.screens.jamb.components.JambCalculatorDialog
import com.example.ui.screens.jamb.components.JambEightKeypad
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
    electives: List<JambSubject> = emptyList(),
    initialEnable8Keypad: Boolean = true,
    jambRepository: JambQuestionRepository,
    currentUserId: String = "",
    onSingleExamFinished: (JambExamResult) -> Unit,
    onMultiExamFinished: (JambMultiSubjectExamResult) -> Unit,
    onExitExam: () -> Unit,
    modifier: Modifier = Modifier
) {
    val is4SubjectMock = examType == JambExamType.FULL_4_SUBJECT_MOCK

    // 4-Subject List
    val mockSubjects = remember(is4SubjectMock, electives) {
        if (is4SubjectMock) {
            listOf(JambSubject.ENGLISH) + electives.filter { it != JambSubject.ENGLISH }.take(3)
        } else {
            listOf(subject)
        }
    }

    var activeSubjectIndex by remember { mutableStateOf(0) }
    val currentSubject = mockSubjects.getOrElse(activeSubjectIndex) { mockSubjects.first() }

    // Load questions for each subject
    val questionsBySubject = remember(mockSubjects, examType) {
        if (is4SubjectMock) {
            jambRepository.getMultiSubjectQuestions(electives = electives, uid = currentUserId)
        } else {
            mapOf(subject to jambRepository.getQuestions(subject, examType, currentUserId))
        }
    }

    // Answers and flags per subject
    val answersBySubject = remember {
        mutableStateMapOf<JambSubject, MutableMap<Int, String>>().apply {
            mockSubjects.forEach { s -> put(s, mutableStateMapOf()) }
        }
    }

    val flagsBySubject = remember {
        mutableStateMapOf<JambSubject, MutableList<Int>>().apply {
            mockSubjects.forEach { s -> put(s, mutableStateListOf()) }
        }
    }

    var questionIndexInCurrentSubject by remember { mutableStateOf(0) }

    // Keypad and Calculator controls
    var show8Keypad by remember { mutableStateOf(initialEnable8Keypad) }
    var showCalculatorDialog by remember { mutableStateOf(false) }

    // Global Countdown Timer
    val totalTimeSeconds = examType.durationSeconds
    var remainingSeconds by remember { mutableStateOf(totalTimeSeconds) }
    var isTimerActive by remember { mutableStateOf(true) }
    var totalTimeSpentSeconds by remember { mutableStateOf(0) }

    // Dialog & UI states
    var showSubmitConfirmationDialog by remember { mutableStateOf(false) }
    var showExitConfirmationDialog by remember { mutableStateOf(false) }
    var showPaletteSheet by remember { mutableStateOf(false) }
    var isTimeExpired by remember { mutableStateOf(false) }
    var isPassageExpanded by remember { mutableStateOf(true) }

    val focusRequester = remember { FocusRequester() }

    val currentQuestions = questionsBySubject[currentSubject] ?: emptyList()
    val currentAnswers = answersBySubject.getOrPut(currentSubject) { mutableStateMapOf() }
    val currentFlags = flagsBySubject.getOrPut(currentSubject) { mutableStateListOf() }
    val currentQuestion = currentQuestions.getOrNull(questionIndexInCurrentSubject)
    val selectedOptionForCurrent = currentAnswers[questionIndexInCurrentSubject]

    // Hardware Keyboard listeners request focus on launch
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    // Countdown effect
    LaunchedEffect(isTimerActive) {
        while (isTimerActive && remainingSeconds > 0) {
            delay(1000L)
            remainingSeconds--
            totalTimeSpentSeconds++
            if (remainingSeconds <= 0) {
                isTimerActive = false
                isTimeExpired = true
            }
        }
    }

    fun submitExamNow() {
        isTimerActive = false
        if (is4SubjectMock) {
            // Record questions for all subjects
            questionsBySubject.forEach { (subj, qList) ->
                jambRepository.recordExamQuestionsAnswered(qList, subj, currentUserId)
            }
            val multiResult = jambRepository.calculateMultiSubjectResult(
                subjectQuestions = questionsBySubject,
                userAnswersBySubject = answersBySubject.mapValues { it.value.toMap() },
                flaggedBySubject = flagsBySubject.mapValues { it.value.toSet() },
                totalTimeSpentSeconds = totalTimeSpentSeconds
            )
            onMultiExamFinished(multiResult)
        } else {
            val singleQuestions = questionsBySubject[subject] ?: emptyList()
            val singleAnswers = currentAnswers.toMap()
            jambRepository.recordExamQuestionsAnswered(singleQuestions, subject, currentUserId)
            val singleResult = jambRepository.calculateResult(
                subject = subject,
                examType = examType,
                questions = singleQuestions,
                userAnswers = singleAnswers,
                timeSpentSeconds = totalTimeSpentSeconds,
                flaggedIndices = currentFlags.toSet()
            )
            onSingleExamFinished(singleResult)
        }
    }

    // Auto submit on time expiration
    LaunchedEffect(isTimeExpired) {
        if (isTimeExpired) {
            submitExamNow()
        }
    }

    // Handlers for 8-Keypad and physical keyboard bindings
    fun handleSelectOption(option: String) {
        currentAnswers[questionIndexInCurrentSubject] = option
    }

    fun handleReverse() {
        currentAnswers.remove(questionIndexInCurrentSubject)
    }

    fun handleNext() {
        if (questionIndexInCurrentSubject < currentQuestions.size - 1) {
            questionIndexInCurrentSubject++
        } else if (is4SubjectMock && activeSubjectIndex < mockSubjects.size - 1) {
            activeSubjectIndex++
            questionIndexInCurrentSubject = 0
        }
    }

    fun handlePrevious() {
        if (questionIndexInCurrentSubject > 0) {
            questionIndexInCurrentSubject--
        } else if (is4SubjectMock && activeSubjectIndex > 0) {
            activeSubjectIndex--
            val prevQuestions = questionsBySubject[mockSubjects[activeSubjectIndex]] ?: emptyList()
            questionIndexInCurrentSubject = (prevQuestions.size - 1).coerceAtLeast(0)
        }
    }

    // Intercept back button to confirm exit
    BackHandler {
        showExitConfirmationDialog = true
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .focusRequester(focusRequester)
            .focusable()
            .onKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyUp) {
                    when (keyEvent.key) {
                        Key.A -> { handleSelectOption("A"); true }
                        Key.B -> { handleSelectOption("B"); true }
                        Key.C -> { handleSelectOption("C"); true }
                        Key.D -> { handleSelectOption("D"); true }
                        Key.N -> { handleNext(); true }
                        Key.P -> { handlePrevious(); true }
                        Key.R -> { handleReverse(); true }
                        Key.S -> { showSubmitConfirmationDialog = true; true }
                        else -> false
                    }
                } else false
            }
            .testTag("jamb_exam_screen")
    ) {
        Scaffold(
            containerColor = BackgroundDark,
            topBar = {
                Surface(
                    color = CardSurface,
                    border = BorderStroke(1.dp, CardSurfaceBorder),
                    modifier = Modifier.fillMaxWidth().testTag("jamb_cbt_top_bar")
                ) {
                    Column(
                        modifier = Modifier
                            .statusBarsPadding()
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        // Top Info Row: Subject & Clock & Calculator & Submit
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
                                Text(text = currentSubject.iconEmoji, fontSize = 20.sp)
                                Column {
                                    Text(
                                        text = currentSubject.shortName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Black,
                                        color = JambGreenLight
                                    )
                                    Text(
                                        text = "Q ${questionIndexInCurrentSubject + 1} of ${currentQuestions.size}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextSecondary
                                    )
                                }
                            }

                            // Middle Action: Calculator & 8-Key toggle
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                // Calculator Button (JAMB On-Screen Calculator)
                                OutlinedButton(
                                    onClick = { showCalculatorDialog = true },
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.8f)),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyan),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    modifier = Modifier.testTag("jamb_calc_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Calculate,
                                        contentDescription = "Calculator",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("CALC", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                }

                                // 8-Keypad Toggle Button
                                IconButton(
                                    onClick = { show8Keypad = !show8Keypad },
                                    modifier = Modifier
                                        .size(34.dp)
                                        .testTag("jamb_toggle_keypad_icon")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Keyboard,
                                        contentDescription = "Toggle 8-Keypad",
                                        tint = if (show8Keypad) JambGreenLight else TextMuted
                                    )
                                }
                            }

                            // Countdown Timer Badge
                            val isLowTime = remainingSeconds <= 300 // Under 5 mins
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
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Timer,
                                        contentDescription = "Timer",
                                        tint = if (isLowTime) NeonRed else TextPrimary,
                                        modifier = Modifier.size(14.dp)
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
                                colors = ButtonDefaults.buttonColors(containerColor = NeonRed.copy(alpha = 0.9f)),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("jamb_submit_exam_top_button")
                            ) {
                                Text(
                                    text = "SUBMIT",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            }
                        }

                        // Multi-Subject Navigation Tabs (If 4-Subject Mock)
                        if (is4SubjectMock) {
                            Spacer(modifier = Modifier.height(8.dp))
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth().testTag("jamb_multi_subject_tabs")
                            ) {
                                itemsIndexed(mockSubjects) { idx, subj ->
                                    val isTabActive = idx == activeSubjectIndex
                                    val subjAnswers = answersBySubject[subj]?.size ?: 0
                                    val subjTotal = questionsBySubject[subj]?.size ?: 0

                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isTabActive) JambGreen.copy(alpha = 0.25f) else CardSurfaceElevated,
                                        border = BorderStroke(
                                            width = if (isTabActive) 2.dp else 1.dp,
                                            color = if (isTabActive) JambGreenLight else CardSurfaceBorder
                                        ),
                                        modifier = Modifier
                                            .clickable {
                                                activeSubjectIndex = idx
                                                questionIndexInCurrentSubject = 0
                                            }
                                            .testTag("jamb_subject_tab_${subj.id}")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(text = subj.iconEmoji, fontSize = 14.sp)
                                            Text(
                                                text = "${subj.shortName} ($subjAnswers/$subjTotal)",
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = if (isTabActive) FontWeight.Black else FontWeight.Bold,
                                                color = if (isTabActive) JambGreenLight else TextSecondary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            bottomBar = {
                // Bottom Area: Official 8-Keypad (if enabled) + Traditional Nav Buttons
                Surface(
                    color = CardSurface,
                    border = BorderStroke(1.dp, CardSurfaceBorder),
                    modifier = Modifier.fillMaxWidth().testTag("jamb_bottom_navigation")
                ) {
                    Column(
                        modifier = Modifier
                            .navigationBarsPadding()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Official JAMB 8-Keypad
                        AnimatedVisibility(visible = show8Keypad) {
                            JambEightKeypad(
                                selectedOption = selectedOptionForCurrent,
                                onSelectOption = { opt -> handleSelectOption(opt) },
                                onPrevious = { handlePrevious() },
                                onNext = { handleNext() },
                                onReverse = { handleReverse() },
                                onSubmit = { showSubmitConfirmationDialog = true }
                            )
                        }

                        // Bottom Actions: Previous, Flag, Palette, Next
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Previous Button
                            val canGoPrev = questionIndexInCurrentSubject > 0 || (is4SubjectMock && activeSubjectIndex > 0)
                            OutlinedButton(
                                onClick = { handlePrevious() },
                                enabled = canGoPrev,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, if (canGoPrev) TextSecondary else CardSurfaceBorder),
                                modifier = Modifier.testTag("jamb_btn_previous")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Previous",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("PREV", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            }

                            // Flag for Review Button
                            val isFlagged = currentFlags.contains(questionIndexInCurrentSubject)
                            Button(
                                onClick = {
                                    if (isFlagged) {
                                        currentFlags.remove(questionIndexInCurrentSubject)
                                    } else {
                                        currentFlags.add(questionIndexInCurrentSubject)
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
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = JambGreenLight),
                                modifier = Modifier.testTag("jamb_btn_palette")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.GridView,
                                    contentDescription = "Palette",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${currentAnswers.size}/${currentQuestions.size}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Next / Submit Button
                            val isAbsoluteLastQuestion = if (is4SubjectMock) {
                                activeSubjectIndex == mockSubjects.size - 1 && questionIndexInCurrentSubject == currentQuestions.size - 1
                            } else {
                                questionIndexInCurrentSubject == currentQuestions.size - 1
                            }

                            Button(
                                onClick = {
                                    if (isAbsoluteLastQuestion) {
                                        showSubmitConfirmationDialog = true
                                    } else {
                                        handleNext()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isAbsoluteLastQuestion) NeonGreen else JambGreenLight
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("jamb_btn_next")
                            ) {
                                Text(
                                    text = if (isAbsoluteLastQuestion) "FINISH" else "NEXT",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Black,
                                    color = Color.Black
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = if (isAbsoluteLastQuestion) Icons.Default.Check else Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "Next",
                                    tint = Color.Black,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        ) { innerPadding ->
            if (currentQuestions.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
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
                                        text = "${currentSubject.shortName} • Q ${questionIndexInCurrentSubject + 1}",
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

                            if (currentFlags.contains(questionIndexInCurrentSubject)) {
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

                    // Reading Passage Card (if applicable)
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

                    // Options A, B, C, D
                    val options = listOf(
                        "A" to currentQuestion.optionA,
                        "B" to currentQuestion.optionB,
                        "C" to currentQuestion.optionC,
                        "D" to currentQuestion.optionD
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        options.forEach { (optionKey, optionText) ->
                            val isChosen = selectedOptionForCurrent.equals(optionKey, ignoreCase = true)

                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (isChosen) JambGreen.copy(alpha = 0.25f) else CardSurface,
                                border = BorderStroke(
                                    width = if (isChosen) 2.dp else 1.dp,
                                    color = if (isChosen) JambGreenLight else CardSurfaceBorder
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { handleSelectOption(optionKey) }
                                    .testTag("jamb_option_${optionKey}")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(14.dp)
                                        .fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
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

        // On-Screen Basic Calculator Dialog Modal
        if (showCalculatorDialog) {
            JambCalculatorDialog(onDismissRequest = { showCalculatorDialog = false })
        }

        // Question Palette Modal Sheet
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
                            text = "${currentSubject.title.uppercase()} PALETTE",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = TextPrimary
                        )
                        Text(
                            text = "${currentAnswers.size}/${currentQuestions.size} Answered",
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

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(5),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 320.dp)
                    ) {
                        itemsIndexed(currentQuestions) { index, _ ->
                            val isAnswered = currentAnswers.containsKey(index)
                            val isFlagged = currentFlags.contains(index)
                            val isCurrent = index == questionIndexInCurrentSubject

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
                                        questionIndexInCurrentSubject = index
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
            val totalQuestions = if (is4SubjectMock) {
                questionsBySubject.values.sumOf { it.size }
            } else {
                currentQuestions.size
            }

            val totalAnswered = if (is4SubjectMock) {
                answersBySubject.values.sumOf { it.size }
            } else {
                currentAnswers.size
            }

            val totalUnanswered = (totalQuestions - totalAnswered).coerceAtLeast(0)
            val totalFlagged = if (is4SubjectMock) {
                flagsBySubject.values.sumOf { it.size }
            } else {
                currentFlags.size
            }

            AlertDialog(
                onDismissRequest = { showSubmitConfirmationDialog = false },
                title = {
                    Text(
                        text = if (is4SubjectMock) "Submit 4-Subject UTME Mock?" else "Submit CBT Examination?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Are you sure you want to finish and submit your exam? Here is your total progress summary:",
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
                                    Text("Total Answered:", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                    Text("$totalAnswered / $totalQuestions", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = JambGreenLight)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Unanswered Questions:", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                    Text("$totalUnanswered", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = if (totalUnanswered > 0) NeonAmber else TextSecondary)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Flagged for Review:", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                    Text("$totalFlagged", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = if (totalFlagged > 0) JambFlagAmber else TextSecondary)
                                }
                            }
                        }

                        if (totalUnanswered > 0) {
                            Text(
                                text = "⚠️ Warning: You have $totalUnanswered unanswered question(s). Unanswered questions yield 0 marks.",
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
                title = { Text("Abandon Exam?", fontWeight = FontWeight.Black) },
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
}
