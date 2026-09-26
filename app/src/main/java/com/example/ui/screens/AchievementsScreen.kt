package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.GameRepository
import com.example.data.competitive.AchievementRarity
import com.example.ui.components.AchievementCard
import com.example.ui.theme.*

@Composable
fun AchievementsScreen(
    repository: GameRepository,
    modifier: Modifier = Modifier
) {
    val userProfile by repository.userProfile.collectAsState()
    val unlockedIds by repository.unlockedAchievementIds.collectAsState()

    val achievements = remember(userProfile, unlockedIds) {
        repository.getAchievements()
    }

    val unlockedCount = achievements.count { it.isUnlocked }
    val totalCount = achievements.size
    val totalXpEarned = achievements.filter { it.isUnlocked }.sumOf { it.xpBonus }

    var selectedFilter by remember { mutableStateOf("ALL") }

    val filteredAchievements = remember(achievements, selectedFilter) {
        when (selectedFilter) {
            "UNLOCKED" -> achievements.filter { it.isUnlocked }
            "LOCKED" -> achievements.filter { !it.isUnlocked }
            "COMMON" -> achievements.filter { it.rarity == AchievementRarity.COMMON }
            "UNCOMMON" -> achievements.filter { it.rarity == AchievementRarity.UNCOMMON }
            "RARE" -> achievements.filter { it.rarity == AchievementRarity.RARE }
            "EPIC" -> achievements.filter { it.rarity == AchievementRarity.EPIC }
            "LEGENDARY" -> achievements.filter { it.rarity == AchievementRarity.LEGENDARY }
            else -> achievements
        }
    }

    Scaffold(
        containerColor = BackgroundDark,
        modifier = modifier.testTag("achievements_screen")
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "ACHIEVEMENTS",
                    style = MaterialTheme.typography.displayMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Earn badges, unlock player titles, and collect bonus XP",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Progress Banner Card
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = CardSurface,
                    border = BorderStroke(1.dp, CardSurfaceBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "$unlockedCount of $totalCount Badges Unlocked",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = NeonGold,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "$totalXpEarned XP gained from badges",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = NeonGreen.copy(alpha = 0.2f),
                                border = BorderStroke(1.dp, NeonGreen.copy(alpha = 0.5f))
                            ) {
                                Text(
                                    text = "${if (totalCount > 0) (unlockedCount * 100) / totalCount else 0}%",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = NeonGreen,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        LinearProgressIndicator(
                            progress = { if (totalCount > 0) unlockedCount.toFloat() / totalCount else 0f },
                            color = NeonGold,
                            trackColor = CardSurfaceElevated,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Filter Chips
                val filters = listOf("ALL", "UNLOCKED", "LOCKED", "COMMON", "UNCOMMON", "RARE", "EPIC", "LEGENDARY")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    filters.forEach { filter ->
                        val isSelected = selectedFilter == filter
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedFilter = filter },
                            label = {
                                Text(
                                    text = filter,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NeonCyan.copy(alpha = 0.2f),
                                selectedLabelColor = NeonCyan,
                                containerColor = CardSurface,
                                labelColor = TextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) NeonCyan else CardSurfaceBorder
                            ),
                            modifier = Modifier.testTag("filter_chip_${filter.lowercase()}")
                        )
                    }
                }
            }

            items(filteredAchievements, key = { it.id }) { achievement ->
                AchievementCard(achievement = achievement)
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
