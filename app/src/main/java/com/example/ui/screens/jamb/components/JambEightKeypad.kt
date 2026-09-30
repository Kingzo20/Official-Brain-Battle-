package com.example.ui.screens.jamb.components

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

private val JambGreenLight = Color(0xFF00C853)
private val JambKeySubmit = Color(0xFFE53935)
private val JambKeyReverse = Color(0xFFFFB300)
private val JambKeyNav = Color(0xFF0288D1)

@Composable
fun JambEightKeypad(
    selectedOption: String?,
    onSelectOption: (String) -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onReverse: () -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color(0xFF1E222D),
        border = BorderStroke(1.5.dp, JambGreenLight.copy(alpha = 0.5f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("jamb_8key_keypad")
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Keypad Header Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = JambGreenLight.copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, JambGreenLight)
                    ) {
                        Text(
                            text = "8-KEYPAD",
                            style = MaterialTheme.typography.labelSmall,
                            color = JambGreenLight,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                        text = "Official JAMB Keyboard Layout",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Text(
                    text = "A B C D • P N R S",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Top Row: Option Keys [A] [B] [C] [D]
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("A", "B", "C", "D").forEach { opt ->
                    val isChosen = selectedOption.equals(opt, ignoreCase = true)
                    KeypadButton(
                        keyLabel = opt,
                        subLabel = "Option $opt",
                        isSelected = isChosen,
                        activeColor = JambGreenLight,
                        onClick = { onSelectOption(opt) },
                        modifier = Modifier.weight(1f),
                        testTag = "jamb_8key_$opt"
                    )
                }
            }

            // Bottom Row: Navigation & Control Keys [P] [N] [R] [S]
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // [P] - Previous
                KeypadButton(
                    keyLabel = "P",
                    subLabel = "Previous",
                    isSelected = false,
                    activeColor = JambKeyNav,
                    onClick = onPrevious,
                    modifier = Modifier.weight(1f),
                    testTag = "jamb_8key_P"
                )

                // [N] - Next
                KeypadButton(
                    keyLabel = "N",
                    subLabel = "Next",
                    isSelected = false,
                    activeColor = JambKeyNav,
                    onClick = onNext,
                    modifier = Modifier.weight(1f),
                    testTag = "jamb_8key_N"
                )

                // [R] - Reverse (Deselect)
                KeypadButton(
                    keyLabel = "R",
                    subLabel = "Reverse",
                    isSelected = false,
                    activeColor = JambKeyReverse,
                    onClick = onReverse,
                    modifier = Modifier.weight(1f),
                    testTag = "jamb_8key_R"
                )

                // [S] - Submit
                KeypadButton(
                    keyLabel = "S",
                    subLabel = "Submit",
                    isSelected = false,
                    activeColor = JambKeySubmit,
                    onClick = onSubmit,
                    modifier = Modifier.weight(1f),
                    testTag = "jamb_8key_S"
                )
            }
        }
    }
}

@Composable
private fun KeypadButton(
    keyLabel: String,
    subLabel: String,
    isSelected: Boolean,
    activeColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) activeColor else CardSurfaceElevated,
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) activeColor else CardSurfaceBorder
        ),
        modifier = modifier
            .height(58.dp)
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = keyLabel,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = if (isSelected) Color.Black else TextPrimary
            )
            Text(
                text = subLabel,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isSelected) Color.Black.copy(alpha = 0.8f) else TextMuted
            )
        }
    }
}
