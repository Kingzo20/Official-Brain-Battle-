package com.example.ui.screens.jamb

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.model.jamb.JambExamType
import com.example.model.jamb.JambSubject
import com.example.ui.theme.*

private val JambGreen = Color(0xFF008751) // Official Nigerian flag green
private val JambGreenLight = Color(0xFF00C853)
private val JambGold = Color(0xFFFFB300)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JambSetupScreen(
    initialSubject: JambSubject = JambSubject.ENGLISH,
    initialExamType: JambExamType = JambExamType.STANDARD_TEST,
    onStartExam: (JambSubject, JambExamType, List<JambSubject>, Boolean) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedSubject by remember { mutableStateOf(initialSubject) }
    var selectedExamType by remember { mutableStateOf(initialExamType) }
    var enable8Keypad by remember { mutableStateOf(true) }
    val selectedElectives = remember {
        mutableStateListOf(JambSubject.MATHEMATICS, JambSubject.PHYSICS, JambSubject.CHEMISTRY)
    }

    val is4SubjectMock = selectedExamType == JambExamType.FULL_4_SUBJECT_MOCK

    Scaffold(
        containerColor = BackgroundDark,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "JAMB CBT SIMULATOR",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = JambGreen.copy(alpha = 0.25f),
                                border = BorderStroke(1.dp, JambGreenLight)
                            ) {
                                Text(
                                    text = "UTME",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = JambGreenLight,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Authentic Nigerian UTME Exam Engine",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("jamb_setup_back_button")
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
        modifier = modifier.testTag("jamb_setup_screen")
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Hero Banner
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = CardSurface,
                    border = BorderStroke(1.5.dp, JambGreenLight.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        JambGreen.copy(alpha = 0.20f),
                                        CardSurface
                                    )
                                )
                            )
                            .padding(18.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(text = "🇳🇬", fontSize = 24.sp)
                                    Text(
                                        text = "OFFICIAL UTME CBT DRILLS",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = JambGreenLight,
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 1.sp
                                    )
                                }
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

                            Text(
                                text = "Train With Verified UTME Past Questions",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = TextPrimary
                            )

                            Text(
                                text = "Authentic CBT test center experience with official 8-keypad navigation (A, B, C, D, N, P, R, S), full 4-subject mock (180 Qs), on-screen calculator, and 400-mark aggregate predictor.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }

            // Section 1: Exam Mode Selection
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "1. CHOOSE EXAM TYPE",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        JambExamType.values().forEach { examType ->
                            val isSelected = selectedExamType == examType
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (isSelected) {
                                    if (examType == JambExamType.FULL_4_SUBJECT_MOCK) JambGreen.copy(alpha = 0.18f) else NeonCyan.copy(alpha = 0.12f)
                                } else CardSurface,
                                border = BorderStroke(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) {
                                        if (examType == JambExamType.FULL_4_SUBJECT_MOCK) JambGreenLight else NeonCyan
                                    } else CardSurfaceBorder
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedExamType = examType }
                                    .testTag("jamb_exam_type_${examType.id}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                if (isSelected) {
                                                    if (examType == JambExamType.FULL_4_SUBJECT_MOCK) JambGreenLight.copy(alpha = 0.2f) else NeonCyan.copy(alpha = 0.2f)
                                                } else CardSurfaceElevated
                                            )
                                    ) {
                                        Text(text = examType.iconEmoji, fontSize = 22.sp)
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = examType.title,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = TextPrimary
                                            )
                                            if (examType == JambExamType.FULL_4_SUBJECT_MOCK) {
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = JambGold.copy(alpha = 0.2f),
                                                    border = BorderStroke(1.dp, JambGold)
                                                ) {
                                                    Text(
                                                        text = "400 MARKS",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = JambGold,
                                                        fontWeight = FontWeight.Black,
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                    )
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = examType.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextSecondary,
                                            lineHeight = 16.sp
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                            Text(
                                                text = "⏱️ ${examType.durationMinutes} Minutes",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (isSelected) JambGreenLight else TextMuted,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "•",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = TextMuted
                                            )
                                            Text(
                                                text = "📊 ${examType.questionCount} Questions",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (isSelected) JambGreenLight else TextMuted,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { selectedExamType = examType },
                                        colors = RadioButtonDefaults.colors(
                                            selectedColor = if (examType == JambExamType.FULL_4_SUBJECT_MOCK) JambGreenLight else NeonCyan,
                                            unselectedColor = TextMuted
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Section 2: Subject Selection
            if (!is4SubjectMock) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "2. SELECT SUBJECT",
                                style = MaterialTheme.typography.titleSmall,
                                color = TextPrimary,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "${selectedSubject.title} (${selectedSubject.shortName})",
                                style = MaterialTheme.typography.labelSmall,
                                color = JambGreenLight,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            JambSubject.ALL_SUBJECTS.chunked(2).forEach { rowSubjects ->
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    rowSubjects.forEach { subject ->
                                        val isSelected = selectedSubject == subject
                                        Surface(
                                            shape = RoundedCornerShape(14.dp),
                                            color = if (isSelected) JambGreen.copy(alpha = 0.22f) else CardSurface,
                                            border = BorderStroke(
                                                width = if (isSelected) 2.dp else 1.dp,
                                                color = if (isSelected) JambGreenLight else CardSurfaceBorder
                                            ),
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable { selectedSubject = subject }
                                                .testTag("jamb_subject_${subject.id}")
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(12.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                Text(
                                                    text = subject.iconEmoji,
                                                    fontSize = 22.sp
                                                )
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = subject.title,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                                        color = if (isSelected) TextPrimary else TextSecondary,
                                                        maxLines = 1
                                                    )
                                                    Text(
                                                        text = subject.shortName,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = if (isSelected) JambGreenLight else TextMuted,
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                }
                                                if (isSelected) {
                                                    Icon(
                                                        imageVector = Icons.Default.CheckCircle,
                                                        contentDescription = "Selected",
                                                        tint = JambGreenLight,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // 4-Subject Mock: Compulsory English + 3 Electives Selector
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "2. 4-SUBJECT COMBINATION",
                                style = MaterialTheme.typography.titleSmall,
                                color = TextPrimary,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "${selectedElectives.size} of 3 Electives Chosen",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (selectedElectives.size == 3) JambGreenLight else NeonAmber,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Compulsory Subject Card: Use of English
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = JambGreen.copy(alpha = 0.18f),
                            border = BorderStroke(1.5.dp, JambGreenLight),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(text = "📖", fontSize = 24.sp)
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(
                                            text = "Use of English (ENG)",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Black,
                                            color = TextPrimary
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = JambGreenLight.copy(alpha = 0.25f),
                                            border = BorderStroke(1.dp, JambGreenLight)
                                        ) {
                                            Text(
                                                text = "COMPULSORY",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = JambGreenLight,
                                                fontWeight = FontWeight.Black,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "60 Questions • Standard UTME paper for all candidates",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Compulsory",
                                    tint = JambGreenLight,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Text(
                            text = "SELECT 3 ELECTIVES FOR YOUR DEGREE / FACULTY:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary
                        )

                        // Elective selection cards
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            JambSubject.ELECTIVE_SUBJECTS.chunked(2).forEach { rowSubjects ->
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    rowSubjects.forEach { elective ->
                                        val isChosen = selectedElectives.contains(elective)
                                        Surface(
                                            shape = RoundedCornerShape(14.dp),
                                            color = if (isChosen) NeonCyan.copy(alpha = 0.16f) else CardSurface,
                                            border = BorderStroke(
                                                width = if (isChosen) 2.dp else 1.dp,
                                                color = if (isChosen) NeonCyan else CardSurfaceBorder
                                            ),
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable {
                                                    if (isChosen) {
                                                        if (selectedElectives.size > 1) {
                                                            selectedElectives.remove(elective)
                                                        }
                                                    } else {
                                                        if (selectedElectives.size < 3) {
                                                            selectedElectives.add(elective)
                                                        } else {
                                                            // replace last
                                                            selectedElectives.removeAt(2)
                                                            selectedElectives.add(elective)
                                                        }
                                                    }
                                                }
                                                .testTag("jamb_elective_${elective.id}")
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(12.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Text(text = elective.iconEmoji, fontSize = 20.sp)
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = elective.shortName,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (isChosen) TextPrimary else TextSecondary
                                                    )
                                                    Text(
                                                        text = elective.title,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = TextMuted,
                                                        maxLines = 1
                                                    )
                                                }
                                                Checkbox(
                                                    checked = isChosen,
                                                    onCheckedChange = { checked ->
                                                        if (checked) {
                                                            if (selectedElectives.size < 3) {
                                                                selectedElectives.add(elective)
                                                            } else {
                                                                selectedElectives.removeAt(2)
                                                                selectedElectives.add(elective)
                                                            }
                                                        } else {
                                                            if (selectedElectives.size > 1) {
                                                                selectedElectives.remove(elective)
                                                            }
                                                        }
                                                    },
                                                    colors = CheckboxDefaults.colors(
                                                        checkedColor = NeonCyan,
                                                        uncheckedColor = TextMuted
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Section 3: Official 8-Key Navigation Keypad Option
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = CardSurface,
                    border = BorderStroke(1.2.dp, if (enable8Keypad) JambGreenLight else CardSurfaceBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(CardSurfaceElevated)
                        ) {
                            Text(text = "⌨️", fontSize = 22.sp)
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Official JAMB 8-Keypad Mode",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Simulate real CBT center hardware buttons (A, B, C, D, N, P, R, S) on-screen & via external keyboards.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                lineHeight = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Switch(
                            checked = enable8Keypad,
                            onCheckedChange = { enable8Keypad = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = JambGreenLight,
                                checkedTrackColor = JambGreen.copy(alpha = 0.4f)
                            ),
                            modifier = Modifier.testTag("jamb_toggle_8keypad")
                        )
                    }
                }
            }

            // Section 4: Official CBT Test Guidelines Card
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
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = NeonAmber,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "OFFICIAL CBT TEST GUIDELINES",
                                style = MaterialTheme.typography.labelMedium,
                                color = NeonAmber,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        val rules = listOf(
                            "8-Keypad Navigation: [A, B, C, D] selects options, [N] next, [P] previous, [R] reverse/deselect, [S] submit.",
                            "On-Screen Calculator: Tap the calculator icon in the top bar anytime during Math/Physics/Chem questions.",
                            "Multi-Subject Tabs: For 4-subject mocks, switch freely between subjects without losing your answers.",
                            "Aggregate Predictor: Instant breakdown out of 400 marks with Nigerian university cutoff analysis."
                        )

                        rules.forEach { rule ->
                            Row(
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(text = "•", color = TextSecondary, fontWeight = FontWeight.Black)
                                Text(
                                    text = rule,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }
            }

            // Action Button
            item {
                val canStart = !is4SubjectMock || selectedElectives.size == 3
                Button(
                    onClick = {
                        onStartExam(selectedSubject, selectedExamType, selectedElectives.toList(), enable8Keypad)
                    },
                    enabled = canStart,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("jamb_start_exam_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = JambGreenLight
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (is4SubjectMock) {
                                "START 4-SUBJECT UTME MOCK (180 QS • 120 MINS)"
                            } else {
                                "START ${selectedSubject.shortName} CBT (${selectedExamType.questionCount} QS)"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = Color.Black
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
