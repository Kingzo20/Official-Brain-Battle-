package com.example.ui.screens.jamb

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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

// Theme colors for JAMB UTME Nigerian green accent
private val JambGreen = Color(0xFF008751) // Official Nigerian flag green
private val JambGreenLight = Color(0xFF00C853)
private val JambGold = Color(0xFFFFB300)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JambSetupScreen(
    initialSubject: JambSubject = JambSubject.ENGLISH,
    initialExamType: JambExamType = JambExamType.STANDARD_TEST,
    onStartExam: (JambSubject, JambExamType) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedSubject by remember { mutableStateOf(initialSubject) }
    var selectedExamType by remember { mutableStateOf(initialExamType) }

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
                                text = "Features an authentic Nigerian CBT question palette, accurate timed simulation, full detailed answer rationales, and automatic no-repeat question filtering.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }

            // Section 1: Subject Selection
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "1. SELECT SUBJECT",
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

            // Section 2: Exam Mode Selection
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "2. CHOOSE EXAM TYPE & DURATION",
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
                                color = if (isSelected) NeonCyan.copy(alpha = 0.12f) else CardSurface,
                                border = BorderStroke(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) NeonCyan else CardSurfaceBorder
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
                                            .background(if (isSelected) NeonCyan.copy(alpha = 0.2f) else CardSurfaceElevated)
                                    ) {
                                        Text(text = examType.iconEmoji, fontSize = 22.sp)
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = examType.title,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = TextPrimary
                                            )
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
                                                color = if (isSelected) NeonCyan else TextMuted,
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
                                                color = if (isSelected) NeonCyan else TextMuted,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { selectedExamType = examType },
                                        colors = RadioButtonDefaults.colors(
                                            selectedColor = NeonCyan,
                                            unselectedColor = TextMuted
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Section 3: Official CBT Instructions Card
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
                            "Question Palette: Green = Answered, Orange = Flagged for Review, Gray = Unanswered.",
                            "Direct Jump: Tap any question number in the drawer/palette to navigate immediately.",
                            "Timer Alert: Countdown ticks down in real-time. Exam auto-submits when time expires.",
                            "Answer Review: Full step-by-step explanations and score breakdown appear right after submission."
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
                Button(
                    onClick = { onStartExam(selectedSubject, selectedExamType) },
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
                            text = "START ${selectedSubject.shortName} CBT (${selectedExamType.questionCount} QS)",
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
