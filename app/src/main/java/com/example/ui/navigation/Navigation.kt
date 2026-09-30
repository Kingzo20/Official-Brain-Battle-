package com.example.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Auth : Screen("auth")
    object Home : Screen("home")
    object Play : Screen("play")
    object CategorySelection : Screen("category_selection")
    object DifficultySelection : Screen("difficulty_selection")
    object Game : Screen("game")
    object Results : Screen("results")
    object Leaderboard : Screen("leaderboard")
    object Achievements : Screen("achievements")
    object Profile : Screen("profile")
    object Settings : Screen("settings")
    object FriendChallenge : Screen("friend_challenge")
    object Tournament : Screen("tournament")
    object Notifications : Screen("notifications")
    object Statistics : Screen("statistics")
    object Social : Screen("social")
    data class MatchResult(val challengeId: String) : Screen("match_result")
    object JambSetup : Screen("jamb_setup")
    object JambExam : Screen("jamb_exam")
    object JambResult : Screen("jamb_result")
    object JambReview : Screen("jamb_review")
}

enum class BottomNavTab(
    val title: String,
    val icon: ImageVector,
    val screen: Screen,
    val testTag: String
) {
    HOME("Home", Icons.Default.Home, Screen.Home, "bottom_nav_home"),
    PLAY("Play", Icons.Default.SportsEsports, Screen.Play, "bottom_nav_play"),
    LEADERBOARD("Ranking", Icons.Default.Leaderboard, Screen.Leaderboard, "bottom_nav_leaderboard"),
    SOCIAL("Social", Icons.Default.People, Screen.Social, "bottom_nav_social"),
    PROFILE("Profile", Icons.Default.Person, Screen.Profile, "bottom_nav_profile")
}

@Composable
fun BrainBattleBottomBar(
    currentScreen: Screen,
    onTabSelected: (BottomNavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    // Only show bottom navigation on main app tabs
    val isMainTabScreen = when (currentScreen) {
        Screen.Home, Screen.Play, Screen.Leaderboard, Screen.Social, Screen.Achievements, Screen.Profile -> true
        else -> false
    }

    if (!isMainTabScreen) return

    Surface(
        color = CardSurface,
        tonalElevation = 8.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, CardSurfaceBorder),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("brain_battle_bottom_nav")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(vertical = 8.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavTab.values().forEach { tab ->
                val isSelected = currentScreen == tab.screen
                val tabColor = if (isSelected) NeonCyan else TextMuted

                IconButton(
                    onClick = { onTabSelected(tab) },
                    modifier = Modifier
                        .testTag(tab.testTag)
                        .size(52.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.title,
                            tint = tabColor,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = tab.title,
                            style = MaterialTheme.typography.labelSmall,
                            color = tabColor,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}
