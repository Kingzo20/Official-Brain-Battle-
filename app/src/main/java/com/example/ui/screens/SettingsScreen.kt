package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ads.AdMobTestPanel
import com.example.data.GameRepository
import com.example.model.SyncState
import com.example.ui.components.ConfirmDialog
import com.example.ui.components.TopBar
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    repository: GameRepository,
    onBack: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)
    val coroutineScope = rememberCoroutineScope()

    val userSettings by repository.userSettings.collectAsState()
    val syncState = repository.syncService?.syncState?.collectAsState()?.value ?: SyncState.IDLE
    val isOnline = repository.syncService?.isOnline?.collectAsState()?.value ?: true

    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var infoDialogMessage by remember { mutableStateOf<Pair<String, String>?>(null) }
    var isOperating by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = BackgroundDark,
        topBar = {
            TopBar(
                title = "SETTINGS",
                onBack = onBack
            )
        },
        modifier = modifier.testTag("settings_screen")
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // Cloud Data & Sync Section
            item {
                SettingsSectionHeader(title = "CLOUD BACKEND & SYNC")
            }
            item {
                SettingsCard {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = CardSurfaceElevated,
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(text = syncState.badge, fontSize = 20.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Cloud Synchronization",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Status: ${syncState.label} • ${if (isOnline) "Connected" else "Offline"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (syncState == SyncState.ERROR) NeonRed else TextSecondary
                            )
                        }
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    repository.syncService?.syncNow()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = BackgroundDark),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("Sync Now", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Gameplay Settings
            item {
                SettingsSectionHeader(title = "GAMEPLAY")
            }
            item {
                SettingsCard {
                    SettingsToggleRow(
                        title = "Sound Effects",
                        subtitle = "Audio feedback for correct & wrong answers",
                        icon = Icons.AutoMirrored.Filled.VolumeUp,
                        checked = userSettings.soundEffects,
                        onCheckedChange = { checked ->
                            repository.updateSettings { it.copy(soundEffects = checked) }
                        }
                    )
                    HorizontalDivider(color = CardSurfaceBorder, thickness = 0.8.dp)
                    SettingsToggleRow(
                        title = "Background Music",
                        subtitle = "Subtle focus beats during quiz rounds",
                        icon = Icons.Default.MusicNote,
                        checked = userSettings.music,
                        onCheckedChange = { checked ->
                            repository.updateSettings { it.copy(music = checked) }
                        }
                    )
                    HorizontalDivider(color = CardSurfaceBorder, thickness = 0.8.dp)
                    SettingsToggleRow(
                        title = "Haptic Vibration",
                        subtitle = "Tactile feedback when answering questions",
                        icon = Icons.Default.Vibration,
                        checked = userSettings.vibration,
                        onCheckedChange = { checked ->
                            repository.updateSettings { it.copy(vibration = checked) }
                        }
                    )
                    HorizontalDivider(color = CardSurfaceBorder, thickness = 0.8.dp)
                    SettingsToggleRow(
                        title = "Normal Timer Pace",
                        subtitle = "Standard 60-second challenge duration",
                        icon = Icons.Default.Timer,
                        checked = userSettings.timerSpeedNormal,
                        onCheckedChange = { checked ->
                            repository.updateSettings { it.copy(timerSpeedNormal = checked) }
                        }
                    )
                }
            }

            // Appearance Settings
            item {
                SettingsSectionHeader(title = "APPEARANCE")
            }
            item {
                SettingsCard {
                    SettingsToggleRow(
                        title = "Dark Theme",
                        subtitle = "High contrast cyber dark palette",
                        icon = Icons.Default.DarkMode,
                        checked = userSettings.darkTheme,
                        onCheckedChange = { checked ->
                            repository.updateSettings { it.copy(darkTheme = checked) }
                        }
                    )
                    HorizontalDivider(color = CardSurfaceBorder, thickness = 0.8.dp)
                    SettingsToggleRow(
                        title = "Motion & Animations",
                        subtitle = "Enable smooth score count and card transitions",
                        icon = Icons.Default.Animation,
                        checked = userSettings.animationsEnabled,
                        onCheckedChange = { checked ->
                            repository.updateSettings { it.copy(animationsEnabled = checked) }
                        }
                    )
                }
            }

            // Notification Settings
            item {
                SettingsSectionHeader(title = "NOTIFICATIONS")
            }
            item {
                SettingsCard {
                    SettingsToggleRow(
                        title = "Daily Challenge Reminders",
                        subtitle = "Daily morning prompt to keep your streak",
                        icon = Icons.Default.LocalFireDepartment,
                        checked = userSettings.dailyReminder,
                        onCheckedChange = { checked ->
                            repository.updateSettings { it.copy(dailyReminder = checked) }
                        }
                    )
                    HorizontalDivider(color = CardSurfaceBorder, thickness = 0.8.dp)
                    SettingsToggleRow(
                        title = "Streak Alerts",
                        subtitle = "Notify before your streak resets at midnight",
                        icon = Icons.Default.NotificationsActive,
                        checked = userSettings.streakReminder,
                        onCheckedChange = { checked ->
                            repository.updateSettings { it.copy(streakReminder = checked) }
                        }
                    )
                    HorizontalDivider(color = CardSurfaceBorder, thickness = 0.8.dp)
                    SettingsToggleRow(
                        title = "Achievement Notifications",
                        subtitle = "Instant alert when unlocking badges & trophies",
                        icon = Icons.Default.MilitaryTech,
                        checked = userSettings.achievementAlerts,
                        onCheckedChange = { checked ->
                            repository.updateSettings { it.copy(achievementAlerts = checked) }
                        }
                    )
                }
            }

            // Help & About
            item {
                SettingsSectionHeader(title = "HELP & LEGAL")
            }
            item {
                SettingsCard {
                    SettingsNavRow(
                        title = "How to Play & Scoring Guide",
                        subtitle = "Learn base points, streak bonuses and time multipliers",
                        icon = Icons.AutoMirrored.Filled.HelpOutline,
                        onClick = {
                            infoDialogMessage = Pair(
                                "How to Play Brain Battle",
                                "• Answer each question before the countdown runs out.\n• Faster answers earn higher speed multipliers (up to 2.0x).\n• Consecutive correct answers build your Answer Streak bonus.\n• Daily Challenges award huge bonus XP and build your persistent daily streak!"
                            )
                        }
                    )
                    HorizontalDivider(color = CardSurfaceBorder, thickness = 0.8.dp)
                    SettingsNavRow(
                        title = "Privacy Policy",
                        subtitle = "How your profile and score telemetry are handled",
                        icon = Icons.Default.Security,
                        onClick = {
                            infoDialogMessage = Pair(
                                "Privacy & Security Policy",
                                "Brain Battle stores your profile, high scores, and gameplay stats securely in Google Cloud Firestore.\nPasswords are never stored in plain text and are securely managed by Firebase Authentication.\nYour private account details are never shared with third parties."
                            )
                        }
                    )
                    HorizontalDivider(color = CardSurfaceBorder, thickness = 0.8.dp)
                    SettingsNavRow(
                        title = "Terms of Service",
                        subtitle = "Community fair play standards",
                        icon = Icons.Default.Description,
                        onClick = {
                            infoDialogMessage = Pair(
                                "Terms of Service",
                                "By playing Brain Battle, you agree to fair-play rules on leaderboards and in tournaments.\nExploits or automated bots will result in leaderboard entry disqualification."
                            )
                        }
                    )
                    HorizontalDivider(color = CardSurfaceBorder, thickness = 0.8.dp)
                    SettingsNavRow(
                        title = "About Brain Battle",
                        subtitle = "Version 1.0.0 • Official Brain Training & Puzzle Game",
                        icon = Icons.Default.Info,
                        onClick = {
                            infoDialogMessage = Pair(
                                "About Brain Battle",
                                "BRAIN BATTLE\nVersion 1.0.0 (Official Release)\n\nThink Fast. Play Smart. Beat Your Best.\n\nBrain Battle is an official brain-training and competitive puzzle game featuring adaptive challenges, real-time multiplayer duels, and personalized cognitive analytics."
                            )
                        }
                    )
                }
            }

            // AdMob Testing & Debug Lab
            item {
                SettingsSectionHeader(title = "ADMOB TEST INTEGRATION")
            }
            item {
                AdMobTestPanel(
                    onRewardEarned = { _, amount ->
                        repository.awardXp(amount * 50)
                    }
                )
            }

            // Account Actions
            item {
                SettingsSectionHeader(title = "ACCOUNT ACTIONS")
            }
            item {
                SettingsCard {
                    SettingsNavRow(
                        title = "Log Out",
                        subtitle = "Sign out from this device",
                        icon = Icons.AutoMirrored.Filled.Logout,
                        titleColor = NeonAmber,
                        onClick = { showLogoutDialog = true }
                    )
                    HorizontalDivider(color = CardSurfaceBorder, thickness = 0.8.dp)
                    SettingsNavRow(
                        title = "Delete Account",
                        subtitle = "Permanently remove your stats and cloud profile",
                        icon = Icons.Default.DeleteForever,
                        titleColor = NeonRed,
                        onClick = { showDeleteAccountDialog = true }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (showLogoutDialog) {
        ConfirmDialog(
            title = "Log Out?",
            message = "Are you sure you want to sign out of Brain Battle? Your progress will remain securely saved in the cloud.",
            confirmText = "Log Out",
            dismissText = "Cancel",
            onConfirm = {
                showLogoutDialog = false
                isOperating = true
                coroutineScope.launch {
                    repository.signOut()
                    isOperating = false
                    onLogout()
                }
            },
            onDismiss = { showLogoutDialog = false }
        )
    }

    if (showDeleteAccountDialog) {
        ConfirmDialog(
            title = "Delete Account Permanently?",
            message = "This action is irreversible. All high scores, daily streaks, unlocked achievements, and profile statistics will be deleted in accordance with data privacy regulations.",
            confirmText = "Delete Permanently",
            dismissText = "Keep Account",
            onConfirm = {
                showDeleteAccountDialog = false
                isOperating = true
                coroutineScope.launch {
                    repository.deleteAccount()
                    isOperating = false
                    onLogout()
                }
            },
            onDismiss = { showDeleteAccountDialog = false }
        )
    }

    if (infoDialogMessage != null) {
        val isAboutDialog = infoDialogMessage?.first == "About Brain Battle"
        AlertDialog(
            onDismissRequest = { infoDialogMessage = null },
            icon = if (isAboutDialog) {
                {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = BackgroundDark,
                        border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.5f)),
                        modifier = Modifier.size(64.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_brain_battle_logo),
                            contentDescription = "Brain Battle Official Logo",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            } else null,
            title = {
                Text(
                    text = infoDialogMessage?.first ?: "",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = {
                Text(
                    text = infoDialogMessage?.second ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    lineHeight = 22.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { infoDialogMessage = null },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = BackgroundDark)
                ) {
                    Text("Close", fontWeight = FontWeight.Bold)
                }
            },
            containerColor = CardSurface,
            shape = RoundedCornerShape(18.dp)
        )
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = NeonCyan,
        fontWeight = FontWeight.Black,
        letterSpacing = 1.2.sp,
        modifier = Modifier.padding(start = 4.dp, top = 6.dp)
    )
}

@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = CardSurface,
        border = BorderStroke(1.dp, CardSurfaceBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(content = content)
    }
}

@Composable
private fun SettingsToggleRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = NeonCyan,
                checkedTrackColor = NeonCyan.copy(alpha = 0.35f),
                uncheckedThumbColor = TextMuted,
                uncheckedTrackColor = CardSurfaceBorder
            )
        )
    }
}

@Composable
private fun SettingsNavRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    titleColor: Color = TextPrimary,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = titleColor, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, color = titleColor, fontWeight = FontWeight.Bold)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextMuted)
    }
}
