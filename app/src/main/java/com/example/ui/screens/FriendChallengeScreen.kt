package com.example.ui.screens

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.GameRepository
import com.example.model.FriendChallenge
import com.example.ui.components.PrimaryButton
import com.example.ui.components.TopBar
import com.example.ui.theme.*

@Composable
fun FriendChallengeScreen(
    repository: GameRepository,
    onPlayChallenge: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current

    val challenges by repository.friendChallenges.collectAsState()
    var inputCode by remember { mutableStateOf("") }
    var showCreateDialog by remember { mutableStateOf(false) }
    var challengeFeedback by remember { mutableStateOf<String?>(null) }

    Scaffold(
        containerColor = BackgroundDark,
        topBar = {
            TopBar(
                title = "FRIEND CHALLENGE",
                onBack = onBack
            )
        },
        modifier = modifier.testTag("friend_challenge_screen")
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "BATTLE WITH FRIENDS",
                    style = MaterialTheme.typography.displayMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Challenge your friends to beat your high score or join their custom room code",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }

            // Enter Code Card
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = CardSurface,
                    border = BorderStroke(1.dp, CardSurfaceBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "JOIN WITH ROOM CODE",
                            style = MaterialTheme.typography.titleMedium,
                            color = NeonCyan,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = inputCode,
                                onValueChange = { inputCode = it.uppercase() },
                                placeholder = { Text("e.g. BB-8924", color = TextMuted) },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NeonCyan,
                                    unfocusedBorderColor = CardSurfaceBorder,
                                    focusedContainerColor = CardSurfaceElevated,
                                    unfocusedContainerColor = CardSurfaceElevated
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("friend_challenge_code_input")
                            )

                            Button(
                                onClick = {
                                    if (inputCode.isNotBlank()) {
                                        onPlayChallenge(inputCode)
                                    } else {
                                        challengeFeedback = "Please enter a valid challenge code"
                                    }
                                },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = BackgroundDark),
                                modifier = Modifier
                                    .height(56.dp)
                                    .testTag("join_friend_challenge_button")
                            ) {
                                Text("JOIN", fontWeight = FontWeight.Bold)
                            }
                        }

                        if (challengeFeedback != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = challengeFeedback ?: "", color = NeonRed, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            // Create New Challenge Action
            item {
                PrimaryButton(
                    text = "CREATE NEW CHALLENGE",
                    onClick = { showCreateDialog = true },
                    icon = Icons.Default.Add,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "create_challenge_button"
                )
            }

            // Challenge History
            item {
                Text(
                    text = "CHALLENGE HISTORY",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
            }

            items(challenges) { challenge ->
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = CardSurface,
                    border = BorderStroke(1.dp, CardSurfaceBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = challenge.code,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = NeonCyan,
                                    fontWeight = FontWeight.Black
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    color = if (challenge.status == "Completed") NeonGreen.copy(alpha = 0.2f) else NeonAmber.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = challenge.status.uppercase(),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (challenge.status == "Completed") NeonGreen else NeonAmber,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${challenge.challengerName} • ${challenge.category}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimary
                            )
                            Text(
                                text = "Target Score: ${challenge.targetScore} pts • ${challenge.createdAt}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }

                        IconButton(
                            onClick = {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(
                                        Intent.EXTRA_TEXT,
                                        "⚔️ Brain Battle Friend Challenge! Use room code ${challenge.code} to beat my score of ${challenge.targetScore} in ${challenge.category}!"
                                    )
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share Challenge Code"))
                            }
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Share", tint = NeonCyan)
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    if (showCreateDialog) {
        var selectedCategory by remember { mutableStateOf("Math Challenge") }
        val generatedCode = remember { "BB-${(1000..9999).random()}" }

        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = {
                Text("Create Friend Challenge", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Your generated challenge code is:", color = TextSecondary)
                    Text(
                        text = generatedCode,
                        style = MaterialTheme.typography.headlineMedium,
                        color = NeonCyan,
                        fontWeight = FontWeight.Black
                    )
                    Text("Category: Math Challenge", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                    Text("Target: Beat your current best score", color = TextSecondary)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        repository.addFriendChallenge(generatedCode, 980, selectedCategory)
                        showCreateDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = BackgroundDark)
                ) {
                    Text("Create & Copy", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            },
            containerColor = CardSurface,
            shape = RoundedCornerShape(18.dp)
        )
    }
}
