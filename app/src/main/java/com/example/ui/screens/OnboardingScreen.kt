package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.PrimaryButton
import com.example.ui.theme.*

data class OnboardingStep(
    val title: String,
    val description: String,
    val iconEmoji: String,
    val buttonText: String
)

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentStepIndex by remember { mutableIntStateOf(0) }

    val steps = remember {
        listOf(
            OnboardingStep(
                title = "Train Your Brain",
                description = "Challenge yourself with fun puzzles, quizzes and brain games.",
                iconEmoji = "🧠🧩",
                buttonText = "NEXT"
            ),
            OnboardingStep(
                title = "Challenge Yourself",
                description = "Improve your speed, logic, memory and problem-solving skills.",
                iconEmoji = "⚡🎯",
                buttonText = "NEXT"
            ),
            OnboardingStep(
                title = "Battle. Score. Improve.",
                description = "Earn XP, build streaks, unlock achievements and climb the leaderboard.",
                iconEmoji = "🏆🔥",
                buttonText = "GET STARTED"
            )
        )
    }

    val step = steps[currentStepIndex]

    Scaffold(
        containerColor = BackgroundDark,
        modifier = modifier.testTag("onboarding_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top bar with Skip button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                if (currentStepIndex < steps.size - 1) {
                    TextButton(
                        onClick = onComplete,
                        modifier = Modifier.testTag("onboarding_skip_button")
                    ) {
                        Text(
                            text = "Skip",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextSecondary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.height(48.dp))
                }
            }

            // Center Content
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(32.dp),
                    color = CardSurface,
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, CardSurfaceBorder),
                    modifier = Modifier.size(140.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        if (currentStepIndex == 0) {
                            Image(
                                painter = painterResource(id = R.drawable.img_brain_battle_logo),
                                contentDescription = "Brain Battle Official Logo",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(32.dp)),
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            Text(text = step.iconEmoji, fontSize = 48.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(36.dp))

                Text(
                    text = step.title,
                    style = MaterialTheme.typography.displayMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = step.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // Bottom bar with indicators and Next/Get Started button
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Indicator dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    steps.indices.forEach { index ->
                        val isSelected = index == currentStepIndex
                        Box(
                            modifier = Modifier
                                .size(if (isSelected) 24.dp else 8.dp, 8.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) NeonCyan else CardSurfaceBorder)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                PrimaryButton(
                    text = step.buttonText,
                    onClick = {
                        if (currentStepIndex < steps.size - 1) {
                            currentStepIndex++
                        } else {
                            onComplete()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "onboarding_action_button"
                )
            }
        }
    }
}
