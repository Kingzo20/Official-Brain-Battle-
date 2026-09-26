package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.example.data.GameRepository
import com.example.data.competitive.PlayerTitle
import com.example.data.competitive.RankTier
import com.example.model.SyncState
import com.example.ui.components.PrimaryButton
import com.example.ui.components.ScoreCard
import com.example.ui.components.SecondaryButton
import com.example.ui.components.XPProgressBar
import com.example.ui.theme.*

@Composable
fun ProfileScreen(
    repository: GameRepository,
    onNavigateToSettings: () -> Unit,
    onNavigateToAchievements: () -> Unit,
    onNavigateToStatistics: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val userProfile by repository.userProfile.collectAsState()
    val syncState = repository.syncService?.syncState?.collectAsState()?.value ?: SyncState.IDLE

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showPremiumDialog by remember { mutableStateOf(false) }
    var showTitleDialog by remember { mutableStateOf(false) }

    val personalBests = remember(userProfile) { repository.getPersonalBests() }
    val availableTitles = remember(userProfile) { repository.getAvailableTitles() }
    val rankTier = remember(userProfile.totalXp) { RankTier.fromXp(userProfile.totalXp) }

    var editedDisplayName by remember { mutableStateOf(userProfile.displayName) }
    var editedUsername by remember { mutableStateOf(userProfile.username) }
    var selectedAvatar by remember { mutableStateOf(userProfile.avatarEmoji) }
    var selectedCountry by remember { mutableStateOf(userProfile.country) }
    var selectedFlag by remember { mutableStateOf(userProfile.countryFlag) }
    var selectedLanguage by remember { mutableStateOf(userProfile.language) }

    val avatarOptions = listOf("🧠", "⚡", "👑", "🦊", "🚀", "💡", "🎯", "🤖")
    val countryOptions = listOf(
        Pair("Global", "🌐"),
        Pair("USA", "🇺🇸"),
        Pair("United Kingdom", "🇬🇧"),
        Pair("Nigeria", "🇳🇬"),
        Pair("Canada", "🇨🇦"),
        Pair("Germany", "🇩🇪"),
        Pair("Japan", "🇯🇵"),
        Pair("Brazil", "🇧🇷")
    )

    Scaffold(
        containerColor = BackgroundDark,
        modifier = modifier.testTag("profile_screen")
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "PLAYER PROFILE",
                            style = MaterialTheme.typography.displayMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Text(
                                text = "${syncState.badge} ${syncState.label}",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (syncState == SyncState.ERROR) NeonRed else NeonCyan
                            )
                        }
                    }

                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("profile_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = TextPrimary
                        )
                    }
                }
            }

            // Profile Card Header
            item {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = CardSurface,
                    border = BorderStroke(1.5.dp, CardSurfaceBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(84.dp)
                                .clip(CircleShape)
                                .background(Brush.radialGradient(listOf(NeonViolet, CardSurfaceElevated)))
                                .border(3.dp, NeonCyan, CircleShape)
                                .clickable {
                                    editedDisplayName = userProfile.displayName
                                    editedUsername = userProfile.username
                                    selectedAvatar = userProfile.avatarEmoji
                                    selectedCountry = userProfile.country
                                    selectedFlag = userProfile.countryFlag
                                    showEditProfileDialog = true
                                }
                        ) {
                            Text(text = userProfile.avatarEmoji, fontSize = 42.sp)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = userProfile.displayName.ifBlank { userProfile.username },
                            style = MaterialTheme.typography.headlineMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Black
                        )

                        Text(
                            text = "@${userProfile.username} • ${userProfile.countryFlag} ${userProfile.country}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMuted
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Rank Tier
                            Surface(
                                color = Color(rankTier.colorHex).copy(alpha = 0.2f),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, Color(rankTier.colorHex).copy(alpha = 0.6f))
                            ) {
                                Text(
                                    text = "${rankTier.badgeEmoji} ${rankTier.displayName.uppercase()}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(rankTier.colorHex),
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }

                            // Equipped Title (clickable to change)
                            Surface(
                                color = NeonViolet.copy(alpha = 0.25f),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, NeonViolet.copy(alpha = 0.5f)),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { showTitleDialog = true }
                                    .testTag("profile_title_selector")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "🎖️ ${userProfile.selectedTitle}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = NeonCyan,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "Change Title",
                                        tint = NeonCyan,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        XPProgressBar(
                            currentXp = userProfile.currentXp,
                            nextLevelXp = userProfile.nextLevelXp,
                            barHeight = 10.dp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Current XP: ${userProfile.currentXp}", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                            Text("Next Level: ${userProfile.nextLevelXp} XP", style = MaterialTheme.typography.labelSmall, color = NeonCyan)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        SecondaryButton(
                            text = "EDIT PROFILE",
                            onClick = {
                                editedDisplayName = userProfile.displayName
                                editedUsername = userProfile.username
                                selectedAvatar = userProfile.avatarEmoji
                                selectedCountry = userProfile.country
                                selectedFlag = userProfile.countryFlag
                                showEditProfileDialog = true
                            },
                            icon = Icons.Default.Edit,
                            modifier = Modifier.fillMaxWidth(),
                            testTag = "profile_edit_button"
                        )
                    }
                }
            }

            // Brain Battle Pro Card
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = CardSurface,
                    border = BorderStroke(1.5.dp, NeonGold.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showPremiumDialog = true }
                ) {
                    Row(
                        modifier = Modifier
                            .background(Brush.horizontalGradient(listOf(NeonViolet.copy(alpha = 0.2f), CardSurface)))
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "👑", fontSize = 32.sp)
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "BRAIN BATTLE PRO",
                                style = MaterialTheme.typography.titleMedium,
                                color = NeonGold,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "Ad-free experience, unlimited lives, and 2x XP booster.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = NeonGold)
                    }
                }
            }

            // Statistics Section
            item {
                Text(
                    text = "STATISTICS",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ScoreCard(
                        label = "Games Played",
                        value = "${userProfile.gamesPlayed}",
                        icon = Icons.Default.SportsEsports,
                        color = NeonCyan,
                        modifier = Modifier.weight(1f)
                    )
                    ScoreCard(
                        label = "Best Score",
                        value = "${userProfile.bestScore}",
                        icon = Icons.Default.EmojiEvents,
                        color = NeonGold,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ScoreCard(
                        label = "Current Streak",
                        value = "${userProfile.currentStreak} Days 🔥",
                        icon = Icons.Default.LocalFireDepartment,
                        color = NeonAmber,
                        modifier = Modifier.weight(1f)
                    )
                    ScoreCard(
                        label = "Overall Accuracy",
                        value = "${userProfile.accuracyPercentage}%",
                        icon = Icons.Default.TrackChanges,
                        color = NeonGreen,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ScoreCard(
                        label = "Total XP",
                        value = "${userProfile.totalXp}",
                        icon = Icons.Default.Bolt,
                        color = NeonBlue,
                        modifier = Modifier.weight(1f)
                    )
                    ScoreCard(
                        label = "Achievements",
                        value = "${userProfile.achievementsUnlocked} / ${userProfile.totalAchievements}",
                        icon = Icons.Default.MilitaryTech,
                        color = NeonViolet,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Competitive Personal Bests
            item {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = CardSurface,
                    border = BorderStroke(1.2.dp, NeonGold.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("🏆", fontSize = 20.sp)
                            Text(
                                text = "PERSONAL BESTS & RECORDS",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Black,
                                color = NeonGold
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            ScoreCard(
                                label = "High Score",
                                value = "${maxOf(personalBests.overallBest, userProfile.bestScore)}",
                                icon = Icons.Default.EmojiEvents,
                                color = NeonGold,
                                modifier = Modifier.weight(1f)
                            )
                            ScoreCard(
                                label = "Best Streak",
                                value = "${userProfile.bestStreak} In-A-Row",
                                icon = Icons.Default.FlashOn,
                                color = NeonCyan,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            ScoreCard(
                                label = "Daily Challenge PB",
                                value = "${personalBests.modeBests["daily_challenge"] ?: (if (userProfile.dailyChallengeCompleted) userProfile.bestScore else 0)}",
                                icon = Icons.Default.Today,
                                color = NeonAmber,
                                modifier = Modifier.weight(1f)
                            )
                            ScoreCard(
                                label = "60s Rush PB",
                                value = "${personalBests.modeBests["sixty_second_rush"] ?: 0}",
                                icon = Icons.Default.Timer,
                                color = NeonGreen,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = CardSurface,
                    border = BorderStroke(1.2.dp, NeonCyan.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .background(Brush.horizontalGradient(listOf(NeonCyan.copy(alpha = 0.12f), CardSurface)))
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("🧠", fontSize = 20.sp)
                            Text(
                                text = "BRAIN REPORT & COGNITIVE ANALYTICS",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Black,
                                color = NeonCyan
                            )
                        }
                        Text(
                            text = "Explore your adaptive difficulty, cognitive strength, domain mastery across all 8 skills, and speed profiles.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        PrimaryButton(
                            text = "VIEW FULL BRAIN REPORT",
                            onClick = onNavigateToStatistics,
                            icon = Icons.Default.Insights,
                            modifier = Modifier.fillMaxWidth(),
                            testTag = "profile_view_statistics_button"
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    // Edit Profile Dialog
    if (showEditProfileDialog) {
        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = {
                Text("Edit Profile", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Select Avatar:", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        avatarOptions.forEach { emoji ->
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (selectedAvatar == emoji) NeonCyan.copy(alpha = 0.3f) else CardSurfaceElevated)
                                    .border(if (selectedAvatar == emoji) 2.dp else 0.dp, NeonCyan, CircleShape)
                                    .clickable { selectedAvatar = emoji }
                            ) {
                                Text(text = emoji, fontSize = 18.sp)
                            }
                        }
                    }

                    OutlinedTextField(
                        value = editedDisplayName,
                        onValueChange = { editedDisplayName = it },
                        label = { Text("Display Name") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = CardSurfaceBorder,
                            focusedContainerColor = CardSurface,
                            unfocusedContainerColor = CardSurface
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editedUsername,
                        onValueChange = {
                            editedUsername = it.filter { ch -> ch.isLetterOrDigit() || ch == '_' }
                        },
                        label = { Text("Username (@handle)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = CardSurfaceBorder,
                            focusedContainerColor = CardSurface,
                            unfocusedContainerColor = CardSurface
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Country / Region for Competitive Standings:", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        countryOptions.forEach { (name, flag) ->
                            val isSelected = selectedCountry == name
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) NeonCyan.copy(alpha = 0.25f) else CardSurfaceElevated,
                                border = BorderStroke(1.dp, if (isSelected) NeonCyan else CardSurfaceBorder),
                                modifier = Modifier
                                    .clickable {
                                        selectedCountry = name
                                        selectedFlag = flag
                                    }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(text = flag, fontSize = 20.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = name,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isSelected) NeonCyan else TextPrimary,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editedUsername.isNotBlank() && editedUsername.length in 3..20) {
                            repository.updateProfile(
                                username = editedUsername.trim(),
                                avatarEmoji = selectedAvatar,
                                displayName = editedDisplayName.trim().ifBlank { editedUsername.trim() },
                                country = selectedCountry,
                                countryFlag = selectedFlag,
                                language = selectedLanguage
                            )
                        }
                        showEditProfileDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = BackgroundDark)
                ) {
                    Text("Save & Sync", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            },
            containerColor = CardSurface,
            shape = RoundedCornerShape(18.dp)
        )
    }

    // Premium Upgrade Dialog
    if (showPremiumDialog) {
        AlertDialog(
            onDismissRequest = { showPremiumDialog = false },
            title = {
                Text("👑 BRAIN BATTLE PRO", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = NeonGold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Unlock the full brain training potential:", color = TextSecondary)
                    Text("✓ Unlimited Lives in Endless Mode", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                    Text("✓ Permanent 2x XP Multiplier on all matches", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                    Text("✓ 100% Ad-Free uninterrupted gameplay", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                    Text("✓ Exclusive Golden Avatar frame & Champion Badge", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                    Text("✓ In-depth Cognitive Analytics breakdown", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                }
            },
            confirmButton = {
                Button(
                    onClick = { showPremiumDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGold, contentColor = BackgroundDark)
                ) {
                    Text("Upgrade for $4.99/mo", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPremiumDialog = false }) {
                    Text("Close", color = TextMuted)
                }
            },
            containerColor = CardSurface,
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Title Selector Dialog
    if (showTitleDialog) {
        AlertDialog(
            onDismissRequest = { showTitleDialog = false },
            title = {
                Text(
                    text = "Equip Player Title",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = TextPrimary
                )
            },
            text = {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(availableTitles) { title ->
                        val isEquipped = title.title == userProfile.selectedTitle
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isEquipped) NeonViolet.copy(alpha = 0.25f) else CardSurfaceElevated,
                            border = BorderStroke(
                                1.dp,
                                if (isEquipped) NeonCyan else if (title.isUnlocked) NeonGreen.copy(alpha = 0.5f) else CardSurfaceBorder
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = title.title,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (title.isUnlocked) TextPrimary else TextMuted
                                        )
                                        if (isEquipped) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = NeonCyan.copy(alpha = 0.2f)
                                            ) {
                                                Text(
                                                    text = "EQUIPPED",
                                                    color = NeonCyan,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        text = title.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (title.isUnlocked) TextSecondary else TextMuted
                                    )
                                }

                                if (title.isUnlocked && !isEquipped) {
                                    Button(
                                        onClick = {
                                            repository.equipTitle(title.title)
                                            showTitleDialog = false
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = BackgroundDark),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Text("EQUIP", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                    }
                                } else if (!title.isUnlocked) {
                                    Text(
                                        text = "🔒 Locked",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextMuted
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showTitleDialog = false }) {
                    Text("Close", color = NeonCyan)
                }
            },
            containerColor = CardSurface,
            shape = RoundedCornerShape(18.dp)
        )
    }
}
