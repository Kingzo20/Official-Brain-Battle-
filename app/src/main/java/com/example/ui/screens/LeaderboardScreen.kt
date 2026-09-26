package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.GameRepository
import com.example.data.competitive.*
import com.example.model.GameCategory
import com.example.model.GameModeType
import com.example.ui.components.LeaderboardRow
import com.example.ui.components.PrimaryButton
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun LeaderboardScreen(
    repository: GameRepository,
    modifier: Modifier = Modifier,
    onNavigateToProfile: () -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    val userProfile by repository.userProfile.collectAsState()

    var selectedScope by remember { mutableStateOf(LeaderboardScope.GLOBAL) }
    var selectedPeriod by remember { mutableStateOf(LeaderboardPeriod.WEEKLY) }
    var selectedCategory by remember { mutableStateOf(GameCategory.MATH) }
    var selectedMode by remember { mutableStateOf(GameModeType.DAILY_CHALLENGE) }

    var queryResult by remember {
        mutableStateOf<LeaderboardQueryResult?>(null)
    }
    var isLoading by remember { mutableStateOf(true) }
    var lastUpdatedText by remember { mutableStateOf("Updated just now") }

    fun loadData(forceRefresh: Boolean = false) {
        isLoading = true
        coroutineScope.launch {
            val result = repository.getCompetitiveLeaderboard(
                scope = selectedScope,
                period = selectedPeriod,
                categoryFilter = if (selectedScope == LeaderboardScope.CATEGORY) selectedCategory else null,
                modeFilter = if (selectedScope == LeaderboardScope.GAME_MODE) selectedMode else null,
                forceRefresh = forceRefresh
            )
            queryResult = result
            isLoading = false
            lastUpdatedText = "Updated just now"
        }
    }

    LaunchedEffect(selectedScope, selectedPeriod, selectedCategory, selectedMode, userProfile) {
        loadData(forceRefresh = false)
    }

    Scaffold(
        containerColor = BackgroundDark,
        modifier = modifier.testTag("leaderboard_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Header with Refresh action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "LEADERBOARD",
                        style = MaterialTheme.typography.displayMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Competitive rankings & player standings",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }

                IconButton(
                    onClick = { loadData(forceRefresh = true) },
                    modifier = Modifier.testTag("leaderboard_refresh_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh Leaderboard",
                        tint = NeonCyan
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 1. Period Selector (Weekly, Monthly, All-Time)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardSurface, RoundedCornerShape(12.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                LeaderboardPeriod.values().forEach { period ->
                    val isSelected = selectedPeriod == period
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) NeonCyan.copy(alpha = 0.2f) else Color.Transparent)
                            .border(if (isSelected) 1.dp else 0.dp, if (isSelected) NeonCyan else Color.Transparent, RoundedCornerShape(8.dp))
                            .padding(vertical = 8.dp)
                            .testTag("period_tab_${period.name.lowercase()}")
                    ) {
                        TextButton(
                            onClick = { selectedPeriod = period },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = period.displayName,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) NeonCyan else TextSecondary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 2. Scope Selector (Global, Country, Friends, Category, Game Mode)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LeaderboardScope.values().forEach { scope ->
                    val isSelected = selectedScope == scope
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedScope = scope },
                        label = {
                            Text(
                                text = scope.displayName,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NeonViolet.copy(alpha = 0.35f),
                            selectedLabelColor = NeonCyan,
                            containerColor = CardSurface,
                            labelColor = TextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) NeonCyan else CardSurfaceBorder
                        ),
                        modifier = Modifier.testTag("scope_chip_${scope.name.lowercase()}")
                    )
                }
            }

            // 3. Sub-filter chips (if Category or Game Mode selected)
            if (selectedScope == LeaderboardScope.CATEGORY) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    GameCategory.values().forEach { cat ->
                        val isSelected = selectedCategory == cat
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) NeonGreen.copy(alpha = 0.2f) else CardSurfaceElevated,
                            border = BorderStroke(1.dp, if (isSelected) NeonGreen else CardSurfaceBorder),
                            modifier = Modifier.clip(RoundedCornerShape(20.dp))
                        ) {
                            TextButton(
                                onClick = { selectedCategory = cat },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "${cat.iconEmoji} ${cat.title}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSelected) NeonGreen else TextSecondary,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            } else if (selectedScope == LeaderboardScope.GAME_MODE) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    GameModeType.values().forEach { mode ->
                        val isSelected = selectedMode == mode
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) NeonGold.copy(alpha = 0.2f) else CardSurfaceElevated,
                            border = BorderStroke(1.dp, if (isSelected) NeonGold else CardSurfaceBorder),
                            modifier = Modifier.clip(RoundedCornerShape(20.dp))
                        ) {
                            TextButton(
                                onClick = { selectedMode = mode },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = mode.title,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSelected) NeonGold else TextSecondary,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Period Active Indicator & Refresh status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PERIOD: ${queryResult?.periodId ?: selectedPeriod.name}",
                    style = MaterialTheme.typography.labelSmall,
                    color = NeonAmber,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = lastUpdatedText,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Leaderboard content / Empty states / Loading
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                if (isLoading) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        CircularProgressIndicator(color = NeonCyan)
                    }
                } else {
                    val entries = queryResult?.entries ?: emptyList()
                    val emptyMsg = queryResult?.emptyMessage

                    if (emptyMsg != null || entries.isEmpty()) {
                        // Polished Empty State (Strictly NO fake players)
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = when (selectedScope) {
                                    LeaderboardScope.COUNTRY -> "🌍"
                                    LeaderboardScope.FRIENDS -> "👥"
                                    else -> "🏆"
                                },
                                fontSize = 48.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = emptyMsg ?: "Not enough players yet.",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = when (selectedScope) {
                                    LeaderboardScope.COUNTRY -> "Complete games and set your country in profile to join local rankings."
                                    LeaderboardScope.FRIENDS -> "Compare scores with friends once added. Fake or simulated accounts are never created."
                                    else -> "Play competitive games to set scores for this period."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                textAlign = TextAlign.Center
                            )

                            if (selectedScope == LeaderboardScope.COUNTRY) {
                                Spacer(modifier = Modifier.height(16.dp))
                                PrimaryButton(
                                    text = "SELECT COUNTRY IN PROFILE",
                                    onClick = onNavigateToProfile,
                                    icon = Icons.Default.Public,
                                    modifier = Modifier.widthIn(max = 300.dp),
                                    testTag = "leaderboard_go_to_profile_button"
                                )
                            } else if (selectedScope == LeaderboardScope.FRIENDS) {
                                Spacer(modifier = Modifier.height(16.dp))
                                OutlinedButton(
                                    onClick = onNavigateToProfile,
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyan),
                                    border = BorderStroke(1.dp, NeonCyan),
                                    modifier = Modifier.testTag("leaderboard_add_friends_button")
                                ) {
                                    Icon(Icons.Default.PersonAdd, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("ADD FRIENDS")
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(entries) { entry ->
                                LeaderboardRow(entry = entry)
                            }
                            item {
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }

            // Pinned Current User Rank Bar at Bottom
            val userEntry = queryResult?.currentUserEntry
            if (userEntry != null) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = CardSurfaceElevated,
                    border = BorderStroke(1.5.dp, NeonCyan),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .testTag("leaderboard_pinned_user_bar")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // User rank
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(NeonCyan.copy(alpha = 0.2f))
                                .border(1.dp, NeonCyan, CircleShape)
                        ) {
                            Text(
                                text = "#${userEntry.rank}",
                                style = MaterialTheme.typography.titleSmall,
                                color = NeonCyan,
                                fontWeight = FontWeight.Black
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        // Avatar
                        Text(text = userProfile.avatarEmoji, fontSize = 22.sp)

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = userProfile.displayName.ifBlank { userProfile.username },
                                    style = MaterialTheme.typography.titleSmall,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = userProfile.countryFlag, fontSize = 14.sp)
                            }
                            Text(
                                text = "YOU • ${userProfile.selectedTitle} • Lvl ${userProfile.level}",
                                style = MaterialTheme.typography.labelSmall,
                                color = NeonGreen
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${userEntry.score}",
                                style = MaterialTheme.typography.titleMedium,
                                color = NeonGold,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "PTS",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                        }
                    }
                }
            }
        }
    }
}
