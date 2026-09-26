package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.GameRepository
import com.example.model.AppNotification
import com.example.model.NotificationType
import com.example.ui.components.TopBar
import com.example.ui.theme.*

@Composable
fun NotificationsScreen(
    repository: GameRepository,
    onNavigateToSocial: () -> Unit,
    onViewMatchResult: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)

    val notifications by repository.notifications.collectAsState()
    var filterUnreadOnly by remember { mutableStateOf(false) }

    val displayedNotifications = remember(notifications, filterUnreadOnly) {
        if (filterUnreadOnly) notifications.filter { !it.isRead } else notifications
    }

    Scaffold(
        containerColor = BackgroundDark,
        topBar = {
            TopBar(
                title = "NOTIFICATIONS",
                onBack = onBack,
                actions = {
                    IconButton(
                        onClick = { repository.markNotificationsRead() },
                        modifier = Modifier.size(44.dp).testTag("mark_all_read_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DoneAll,
                            contentDescription = "Mark all read",
                            tint = NeonCyan
                        )
                    }
                }
            )
        },
        modifier = modifier.testTag("notifications_screen")
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Stay updated with challenge alerts, streaks and rewards",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        modifier = Modifier.weight(1f)
                    )

                    FilterChip(
                        selected = filterUnreadOnly,
                        onClick = { filterUnreadOnly = !filterUnreadOnly },
                        label = { Text(if (filterUnreadOnly) "Unread" else "All") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NeonCyan,
                            selectedLabelColor = BackgroundDark
                        ),
                        modifier = Modifier.testTag("filter_unread_chip")
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            if (displayedNotifications.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "🔔", fontSize = 42.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "NO NOTIFICATIONS",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                items(displayedNotifications) { notification ->
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = CardSurface,
                        border = BorderStroke(1.dp, if (!notification.isRead) NeonCyan.copy(alpha = 0.5f) else CardSurfaceBorder),
                        modifier = Modifier.fillMaxWidth().testTag("notification_item_${notification.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(CardSurfaceElevated)
                            ) {
                                Text(text = notification.iconEmoji, fontSize = 22.sp)
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = notification.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = notification.timeAgo,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextMuted
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = notification.message,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary,
                                    lineHeight = 20.sp
                                )

                                // Social Action affordance if applicable
                                when (notification.type) {
                                    NotificationType.FRIEND_REQUEST,
                                    NotificationType.FRIEND_CHALLENGE,
                                    NotificationType.CHALLENGE_RECEIVED -> {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        OutlinedButton(
                                            onClick = onNavigateToSocial,
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyan),
                                            border = BorderStroke(1.dp, NeonCyan),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.testTag("notification_social_action_${notification.id}")
                                        ) {
                                            Text("VIEW IN SOCIAL HUB", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    NotificationType.MATCH_COMPLETED -> {
                                        val matchId = notification.actionData ?: ""
                                        if (matchId.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(10.dp))
                                            OutlinedButton(
                                                onClick = { onViewMatchResult(matchId) },
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonGreen),
                                                border = BorderStroke(1.dp, NeonGreen),
                                                shape = RoundedCornerShape(10.dp),
                                                modifier = Modifier.testTag("notification_match_result_action_${notification.id}")
                                            ) {
                                                Text("VIEW SCORECARD", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                    else -> Unit
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
