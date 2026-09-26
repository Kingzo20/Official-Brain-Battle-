package com.example.ui.screens

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ads.AdMobManager
import com.example.data.GameRepository
import com.example.data.GameScoringConfig
import com.example.data.social.FriendChallengeRecord
import com.example.model.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.util.AudioHapticHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun GameScreen(
    category: GameCategory,
    difficulty: Difficulty,
    gameMode: GameModeType = GameModeType.CATEGORY_BATTLE,
    activeChallenge: FriendChallengeRecord? = null,
    repository: GameRepository,
    onGameFinished: (GameResult) -> Unit,
    onExitGame: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val audioHaptic = remember { AudioHapticHelper(context) }
    val userSettings by repository.userSettings.collectAsState()

    var showExitDialog by remember { mutableStateOf(false) }
    var isPaused by remember { mutableStateOf(false) }

    // Intercept back button to show pause/exit confirmation
    BackHandler {
        if (!isPaused) {
            isPaused = true
        } else {
            showExitDialog = true
        }
    }

    // Load questions based on chosen game mode or active friend challenge
    val questions = remember(category, difficulty, gameMode, activeChallenge) {
        if (activeChallenge != null && activeChallenge.questionIds.isNotEmpty()) {
            val allApproved = repository.questionAdminService.getAllApprovedPool()
            val matched = activeChallenge.questionIds.mapNotNull { id -> allApproved.firstOrNull { it.id == id } }
            if (matched.isNotEmpty()) matched else repository.getQuestionsFor(category, difficulty).take(activeChallenge.questionCount)
        } else {
            when (gameMode) {
                GameModeType.DAILY_CHALLENGE -> repository.getDailyQuestions()
                GameModeType.SIXTY_SECOND_RUSH -> repository.getRushQuestions()
                GameModeType.ENDLESS_MODE -> repository.getEndlessQuestions()
                GameModeType.TARGETED_PRACTICE -> repository.getPracticeQuestions(category, difficulty, 5)
                else -> repository.getQuestionsFor(category, difficulty)
            }
        }
    }

    var currentQuestionIndex by remember { mutableIntStateOf(0) }
    var currentScore by remember { mutableIntStateOf(0) }
    var currentXpEarned by remember { mutableIntStateOf(0) }
    var correctAnswersCount by remember { mutableIntStateOf(0) }
    var incorrectAnswersCount by remember { mutableIntStateOf(0) }
    var currentStreak by remember { mutableIntStateOf(0) }
    var bestStreak by remember { mutableIntStateOf(0) }

    // Lives for Endless Mode (starts at 3)
    var livesRemaining by remember { mutableIntStateOf(3) }

    // Timers
    // Total timer for Daily (60s), Rush (60s). Per-round timer for others (60s total round or question timer)
    val totalGameTime = when (gameMode) {
        GameModeType.SIXTY_SECOND_RUSH -> 60
        GameModeType.DAILY_CHALLENGE -> 60
        GameModeType.ENDLESS_MODE -> 120
        GameModeType.TARGETED_PRACTICE -> 60
        else -> 60
    }
    var remainingSeconds by remember { mutableIntStateOf(totalGameTime) }
    var elapsedSeconds by remember { mutableIntStateOf(0) }

    // Answer interaction states
    var selectedAnswerIndex by remember { mutableStateOf<Int?>(null) }
    var isAnswerCorrect by remember { mutableStateOf<Boolean?>(null) }
    var feedbackMessage by remember { mutableStateOf<String?>(null) }
    var explanationText by remember { mutableStateOf<String?>(null) }
    var isProcessingAnswer by remember { mutableStateOf(false) }

    // Dialog states for AI Explanation and Reporting
    var showExplanationDialog by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }
    var reportSelectedReason by remember { mutableStateOf(ReportReason.WRONG_ANSWER) }
    var reportDetailsText by remember { mutableStateOf("") }
    var reportSuccessToast by remember { mutableStateOf<String?>(null) }

    // Track timestamp when question became active for speed bonus calculation
    var questionStartTimeMs by remember { mutableLongStateOf(System.currentTimeMillis()) }

    // 50:50 Lifeline (Rewarded Ad) states
    val activity = context as? Activity
    var eliminatedOptionIndices by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var showLifelineDialog by remember { mutableStateOf(false) }
    var lifelineNotice by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(currentQuestionIndex) {
        eliminatedOptionIndices = emptySet()
        lifelineNotice = null
    }

    val currentQuestion: Question? = questions.getOrNull(currentQuestionIndex)

    // Countdown Timer Loop
    LaunchedEffect(isPaused, isProcessingAnswer, remainingSeconds) {
        if (!isPaused && remainingSeconds > 0) {
            delay(1000)
            remainingSeconds -= 1
            elapsedSeconds += 1
            if (remainingSeconds <= 5 && remainingSeconds > 0) {
                audioHaptic.playCountdownSound(userSettings.soundEffects)
            }
        } else if (!isPaused && remainingSeconds <= 0 && !isProcessingAnswer) {
            // Time is up! Conclude game session
            val finalResult = repository.recordGameFinished(
                score = currentScore,
                correctCount = correctAnswersCount,
                totalQuestions = currentQuestionIndex + (if (selectedAnswerIndex != null) 1 else 0),
                timeSpentSeconds = totalGameTime,
                categoryTitle = when (gameMode) {
                    GameModeType.DAILY_CHALLENGE -> "Daily Challenge"
                    GameModeType.SIXTY_SECOND_RUSH -> "60-Second Rush"
                    GameModeType.ENDLESS_MODE -> "Endless Mode"
                    GameModeType.TARGETED_PRACTICE -> "Practice • ${category.title}"
                    else -> category.title
                },
                gameMode = gameMode.id,
                bestStreak = bestStreak
            )
            onGameFinished(finalResult)
        }
    }

    Scaffold(
        containerColor = BackgroundDark,
        modifier = modifier.testTag("game_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top HUD Bar
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Pause Button
                    IconButton(
                        onClick = { isPaused = true },
                        modifier = Modifier
                            .size(42.dp)
                            .testTag("game_pause_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PauseCircle,
                            contentDescription = "Pause Game",
                            tint = TextMuted,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    // Question counter or Mode info
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val headerText = when (gameMode) {
                            GameModeType.SIXTY_SECOND_RUSH -> "Q ${currentQuestionIndex + 1}"
                            GameModeType.ENDLESS_MODE -> "STREAK ${currentStreak} • Q ${currentQuestionIndex + 1}"
                            GameModeType.TARGETED_PRACTICE -> "PRACTICE ${currentQuestionIndex + 1} / ${questions.size}"
                            else -> "QUESTION ${currentQuestionIndex + 1} / ${questions.size}"
                        }
                        Text(
                            text = headerText,
                            style = MaterialTheme.typography.titleMedium,
                            color = NeonCyan,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        )

                        // If Endless Mode, display lives ❤️❤️❤️
                        if (gameMode == GameModeType.ENDLESS_MODE) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                repeat(3) { index ->
                                    val hasLife = index < livesRemaining
                                    Text(
                                        text = if (hasLife) "❤️" else "🖤",
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }

                    // Top Right: Report + Timer View
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(
                            onClick = {
                                isPaused = true
                                showReportDialog = true
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("game_report_question_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Flag,
                                contentDescription = "Report Question",
                                tint = TextMuted,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        TimerView(remainingSeconds = remainingSeconds, totalSeconds = totalGameTime)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Score, XP, and Streak Tracker Bar
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = CardSurface,
                    border = BorderStroke(1.dp, CardSurfaceBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Score: ",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                            Text(
                                text = "$currentScore",
                                style = MaterialTheme.typography.titleMedium,
                                color = NeonGold,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (currentStreak >= 2) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = NeonAmber.copy(alpha = 0.2f),
                                border = BorderStroke(1.dp, NeonAmber)
                            ) {
                                Text(
                                    text = "🔥 $currentStreak STREAK",
                                    color = NeonAmber,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "XP: ",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                            Text(
                                text = "+$currentXpEarned",
                                style = MaterialTheme.typography.titleMedium,
                                color = NeonGreen,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Question Card and Feedback Area
            if (currentQuestion != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(8.dp))

                    QuestionCard(
                        questionText = currentQuestion.questionText,
                        categoryTitle = when (gameMode) {
                            GameModeType.DAILY_CHALLENGE -> "Daily • ${currentQuestion.categoryId.uppercase()}"
                            GameModeType.SIXTY_SECOND_RUSH -> "Rush • ${currentQuestion.categoryId.uppercase()}"
                            GameModeType.ENDLESS_MODE -> "Endless • ${currentQuestion.difficulty.title}"
                            else -> category.title
                        }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Feedback Banner & Explanation
                    AnimatedVisibility(
                        visible = feedbackMessage != null,
                        enter = fadeIn(tween(150)) + expandVertically(),
                        exit = fadeOut(tween(150))
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isAnswerCorrect == true) NeonGreen.copy(alpha = 0.2f) else NeonRed.copy(alpha = 0.2f),
                                border = BorderStroke(1.dp, if (isAnswerCorrect == true) NeonGreen else NeonRed),
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                Text(
                                    text = feedbackMessage ?: "",
                                    color = if (isAnswerCorrect == true) NeonGreen else NeonRed,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                )
                            }

                            if (explanationText != null) {
                                Text(
                                    text = explanationText ?: "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                                )
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                TextButton(
                                    onClick = {
                                        isPaused = true
                                        showExplanationDialog = true
                                    },
                                    modifier = Modifier.testTag("game_why_explanation_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lightbulb,
                                        contentDescription = null,
                                        tint = NeonGold,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "WHY? (INSIGHT)",
                                        color = NeonGold,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                TextButton(
                                    onClick = {
                                        isPaused = true
                                        showReportDialog = true
                                    },
                                    modifier = Modifier.testTag("game_flag_question_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Flag,
                                        contentDescription = null,
                                        tint = TextMuted,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "REPORT",
                                        color = TextMuted,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                    }
                }

                // 50:50 Lifeline Button
                if (eliminatedOptionIndices.isEmpty() && !isProcessingAnswer && selectedAnswerIndex == null && currentQuestion.answerOptions.size >= 4) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = {
                                isPaused = true
                                showLifelineDialog = true
                            },
                            modifier = Modifier.testTag("game_5050_lifeline_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.FilterVintage,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "50:50 LIFELINE (AD)",
                                color = NeonCyan,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else if (lifelineNotice != null) {
                    Text(
                        text = lifelineNotice ?: "",
                        color = NeonGreen,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp)
                    )
                }

                // 4 Answer Buttons
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    currentQuestion.answerOptions.forEachIndexed { index, optionText ->
                        val isSelected = selectedAnswerIndex == index
                        val isThisOptionCorrect = optionText == currentQuestion.correctAnswer
                        val showAsRevealedCorrect = isProcessingAnswer && isAnswerCorrect == false && isThisOptionCorrect
                        val isEliminated = eliminatedOptionIndices.contains(index)

                        AnswerButton(
                            text = if (isEliminated) "— Eliminated —" else optionText,
                            isSelected = isSelected,
                            isCorrect = if (isSelected) isAnswerCorrect else null,
                            isRevealedCorrect = showAsRevealedCorrect,
                            enabled = !isProcessingAnswer && !isPaused && !isEliminated,
                            indexTag = "$index",
                            onClick = {
                                if (isProcessingAnswer || isPaused || isEliminated) return@AnswerButton
                                isProcessingAnswer = true
                                selectedAnswerIndex = index

                                val isCorrect = optionText == currentQuestion.correctAnswer
                                isAnswerCorrect = isCorrect
                                explanationText = currentQuestion.explanation

                                val timeTakenSec = ((System.currentTimeMillis() - questionStartTimeMs) / 1000f).coerceAtLeast(0.5f)

                                if (isCorrect) {
                                    val newStreakVal = currentStreak + 1
                                    currentStreak = newStreakVal
                                    bestStreak = maxOf(bestStreak, newStreakVal)
                                    correctAnswersCount++

                                    val breakdown = GameScoringConfig.calculateQuestionScore(
                                        difficulty = currentQuestion.difficulty,
                                        timeTakenSeconds = timeTakenSec,
                                        timeLimitSeconds = currentQuestion.timeLimit,
                                        currentStreak = newStreakVal
                                    )

                                    currentScore += breakdown.totalPoints
                                    currentXpEarned += breakdown.xpEarned

                                    val bonusNote = if (breakdown.speedBonus > 0) " (⚡+${breakdown.speedBonus} SPEED)" else ""
                                    feedbackMessage = "CORRECT! +${breakdown.totalPoints} PTS$bonusNote"

                                    audioHaptic.playCorrectSound(userSettings.soundEffects)
                                    audioHaptic.vibrate(userSettings.vibration, 30)
                                } else {
                                    currentStreak = 0
                                    incorrectAnswersCount++
                                    feedbackMessage = "INCORRECT"

                                    if (gameMode == GameModeType.ENDLESS_MODE) {
                                        livesRemaining -= 1
                                    }

                                    audioHaptic.playWrongSound(userSettings.soundEffects)
                                    audioHaptic.vibrate(userSettings.vibration, 80)
                                }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                }
            } else {
                LoadingStateView(message = "Preparing battle questions...")
            }
        }
    }

    // Auto-advance logic after feedback
    LaunchedEffect(isProcessingAnswer) {
        if (isProcessingAnswer) {
            val waitDelay = if (isAnswerCorrect == false) 1500L else 1200L
            delay(waitDelay)

            // Check if Endless Mode game over
            val isEndlessGameOver = gameMode == GameModeType.ENDLESS_MODE && livesRemaining <= 0
            val isLastQuestion = currentQuestionIndex >= questions.size - 1

            if (!isEndlessGameOver && !isLastQuestion) {
                currentQuestionIndex++
                selectedAnswerIndex = null
                isAnswerCorrect = null
                feedbackMessage = null
                explanationText = null
                questionStartTimeMs = System.currentTimeMillis()
                isProcessingAnswer = false
            } else {
                // Game Finished!
                val totalAnswered = correctAnswersCount + incorrectAnswersCount
                val finalResult = repository.recordGameFinished(
                    score = currentScore,
                    correctCount = correctAnswersCount,
                    totalQuestions = if (totalAnswered > 0) totalAnswered else questions.size,
                    timeSpentSeconds = elapsedSeconds.coerceAtLeast(1),
                    categoryTitle = when (gameMode) {
                        GameModeType.DAILY_CHALLENGE -> "Daily Challenge"
                        GameModeType.SIXTY_SECOND_RUSH -> "60-Second Rush"
                        GameModeType.ENDLESS_MODE -> "Endless Mode"
                        else -> category.title
                    },
                    gameMode = gameMode.id,
                    bestStreak = bestStreak
                )

                if (activeChallenge != null) {
                    val calcAccuracy = if (totalAnswered > 0) (correctAnswersCount * 100) / totalAnswered else 0
                    coroutineScope.launch {
                        repository.socialRepository.submitChallengeResult(
                            challengeId = activeChallenge.challengeId,
                            score = currentScore,
                            accuracy = calcAccuracy,
                            timeSpentSeconds = elapsedSeconds.coerceAtLeast(1)
                        )
                    }
                }

                onGameFinished(finalResult)
            }
        }
    }

    // Pause Dialog Overlay
    if (isPaused) {
        AlertDialog(
            onDismissRequest = { isPaused = false },
            containerColor = CardSurface,
            shape = RoundedCornerShape(22.dp),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.PauseCircle,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "GAME PAUSED",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Take a breath! Your score and battle progress are saved while paused.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Current Score:", color = TextMuted, style = MaterialTheme.typography.bodySmall)
                        Text("$currentScore pts", color = NeonGold, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Time Remaining:", color = TextMuted, style = MaterialTheme.typography.bodySmall)
                        Text("${remainingSeconds}s", color = NeonCyan, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                PrimaryButton(
                    text = "RESUME BATTLE",
                    onClick = {
                        isPaused = false
                        questionStartTimeMs = System.currentTimeMillis()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "pause_dialog_resume_button"
                )
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showExitDialog = true
                    },
                    border = BorderStroke(1.dp, NeonRed.copy(alpha = 0.5f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonRed),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("QUIT BATTLE", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Exit Confirmation Dialog
    if (showExitDialog) {
        ConfirmDialog(
            title = "Quit Current Battle?",
            message = "Leaving will forfeit your current round and any unsubmitted score for this session.",
            confirmText = "Quit Battle",
            dismissText = "Keep Playing",
            onConfirm = {
                showExitDialog = false
                isPaused = false
                onExitGame()
            },
            onDismiss = {
                showExitDialog = false
            }
        )
    }

    // Explanation Dialog
    if (showExplanationDialog && currentQuestion != null) {
        AlertDialog(
            onDismissRequest = {
                showExplanationDialog = false
                isPaused = false
            },
            containerColor = CardSurface,
            shape = RoundedCornerShape(20.dp),
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = NeonGold,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Question Insight",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "QUESTION",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = currentQuestion.questionText,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "CORRECT ANSWER",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        fontWeight = FontWeight.Bold
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = NeonGreen.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, NeonGreen)
                    ) {
                        Text(
                            text = "✓ ${currentQuestion.correctAnswer}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Black,
                            color = NeonGreen,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "EXPLANATION & REASONING",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        fontWeight = FontWeight.Bold
                    )
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = BackgroundDark,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = currentQuestion.explanation.ifBlank {
                                "The specified answer is mathematically and logically validated by the Brain Battle question engine."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            },
            confirmButton = {
                PrimaryButton(
                    text = "GOT IT",
                    onClick = {
                        showExplanationDialog = false
                        isPaused = false
                        questionStartTimeMs = System.currentTimeMillis()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "explanation_dialog_dismiss_button"
                )
            }
        )
    }

    // Report Question Dialog
    if (showReportDialog && currentQuestion != null) {
        AlertDialog(
            onDismissRequest = {
                showReportDialog = false
                isPaused = false
            },
            containerColor = CardSurface,
            shape = RoundedCornerShape(20.dp),
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ReportProblem,
                        contentDescription = null,
                        tint = NeonAmber,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Report Question",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Flag an issue with this question for algorithmic and admin review:",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = BackgroundDark,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "\"${currentQuestion.questionText}\"",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextPrimary,
                            modifier = Modifier.padding(8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    ReportReason.values().forEach { reason ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (reportSelectedReason == reason) NeonCyan.copy(alpha = 0.1f) else Color.Transparent
                                )
                                .clickable { reportSelectedReason = reason }
                                .padding(horizontal = 6.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = reportSelectedReason == reason,
                                onClick = { reportSelectedReason = reason },
                                colors = RadioButtonDefaults.colors(selectedColor = NeonCyan)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = reason.label,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextPrimary,
                                fontWeight = if (reportSelectedReason == reason) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }

                    OutlinedTextField(
                        value = reportDetailsText,
                        onValueChange = { reportDetailsText = it },
                        placeholder = { Text("Additional notes (optional)", color = TextMuted) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = CardSurfaceBorder
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                PrimaryButton(
                    text = "SUBMIT REPORT",
                    onClick = {
                        repository.reportQuestion(
                            QuestionReport(
                                id = "rep_${System.currentTimeMillis()}",
                                questionId = currentQuestion.id,
                                questionText = currentQuestion.questionText,
                                reason = reportSelectedReason,
                                reportedByUserId = repository.userProfile.value.uid.ifBlank { "anonymous" },
                                details = reportDetailsText
                            )
                        )
                        showReportDialog = false
                        isPaused = false
                        reportDetailsText = ""
                        questionStartTimeMs = System.currentTimeMillis()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "submit_report_button"
                )
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showReportDialog = false
                        isPaused = false
                        questionStartTimeMs = System.currentTimeMillis()
                    }
                ) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    if (showLifelineDialog) {
        AlertDialog(
            onDismissRequest = {
                showLifelineDialog = false
                isPaused = false
            },
            title = {
                Text(
                    text = "50:50 Lifeline Hint",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = {
                Text(
                    text = "Watch an official AdMob sponsor clip to eliminate 2 incorrect answer options from this question.",
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLifelineDialog = false
                        if (activity != null) {
                            AdMobManager.showRewardedAd(
                                activity = activity,
                                onRewardEarned = { _, _ ->
                                    if (currentQuestion != null) {
                                        val incorrectIndices = currentQuestion.answerOptions
                                            .mapIndexedNotNull { idx, opt ->
                                                if (opt != currentQuestion.correctAnswer) idx else null
                                            }
                                            .shuffled()
                                            .take(2)
                                            .toSet()
                                        eliminatedOptionIndices = incorrectIndices
                                        lifelineNotice = "50:50 Applied! 2 incorrect options removed."
                                    }
                                },
                                onAdClosed = {
                                    isPaused = false
                                },
                                onAdFailed = { err ->
                                    lifelineNotice = err
                                    isPaused = false
                                }
                            )
                        } else {
                            isPaused = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen, contentColor = BackgroundDark)
                ) {
                    Text("Watch Clip", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showLifelineDialog = false
                        isPaused = false
                    }
                ) {
                    Text("Cancel", color = TextMuted)
                }
            },
            containerColor = CardSurface,
            shape = RoundedCornerShape(18.dp)
        )
    }
}
