package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.GameRepository
import com.example.model.Tournament
import com.example.ui.components.LeaderboardRow
import com.example.ui.components.PrimaryButton
import com.example.ui.components.TopBar
import com.example.ui.theme.*

@Composable
fun TournamentScreen(
    repository: GameRepository,
    onJoinTournament: (Tournament) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)

    val tournaments = remember { repository.getTournaments() }
    val mainTournament = tournaments.firstOrNull()
    var joinedState by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = BackgroundDark,
        topBar = {
            TopBar(
                title = "TOURNAMENTS",
                onBack = onBack
            )
        },
        modifier = modifier.testTag("tournament_screen")
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "COMPETITIVE ARENA",
                    style = MaterialTheme.typography.displayMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Weekly brackets with top player rewards, trophies and titles",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }

            if (mainTournament != null) {
                item {
                    // Featured Tournament Banner Card
                    Surface(
                        shape = RoundedCornerShape(22.dp),
                        color = CardSurface,
                        border = BorderStroke(1.5.dp, NeonGold.copy(alpha = 0.6f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            CardSurface,
                                            NeonGold.copy(alpha = 0.08f)
                                        )
                                    )
                                )
                                .padding(20.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Surface(
                                    color = NeonGreen.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(6.dp),
                                    border = BorderStroke(1.dp, NeonGreen.copy(alpha = 0.4f))
                                ) {
                                    Text(
                                        text = mainTournament.status,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = NeonGreen,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                                Text(
                                    text = mainTournament.endTime,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = NeonGold,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = mainTournament.name,
                                style = MaterialTheme.typography.titleLarge,
                                color = TextPrimary,
                                fontWeight = FontWeight.Black
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Grid of Info
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Participants", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                    Text("${mainTournament.participantCount}", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                                }
                                Column {
                                    Text("Entry", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                    Text(mainTournament.entryRequirement, style = MaterialTheme.typography.titleMedium, color = NeonCyan, fontWeight = FontWeight.Bold)
                                }
                                Column {
                                    Text("Prizes", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                    Text("5,000 XP + Trophy", style = MaterialTheme.typography.titleMedium, color = NeonGold, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = "Rules: ${mainTournament.rules}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                lineHeight = 18.sp
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            PrimaryButton(
                                text = if (joinedState) "ENTER TOURNAMENT MATCH" else "JOIN TOURNAMENT",
                                onClick = {
                                    joinedState = true
                                    onJoinTournament(mainTournament)
                                },
                                icon = Icons.Default.EmojiEvents,
                                modifier = Modifier.fillMaxWidth(),
                                testTag = "join_tournament_button"
                            )
                        }
                    }
                }

                // Tournament Leaderboard Section
                item {
                    Text(
                        text = "LIVE BRACKET LEADERS",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                }

                items(mainTournament.topLeaderboard) { entry ->
                    LeaderboardRow(entry = entry)
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}
