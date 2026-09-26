package com.example.ads

import android.app.Activity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AdMobTestPanel(
    onRewardEarned: (rewardType: String, amount: Int) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity

    val isInitialized by AdMobManager.isInitialized.collectAsState()
    val isInterstitialLoaded by AdMobManager.isInterstitialLoaded.collectAsState()
    val isRewardedLoaded by AdMobManager.isRewardedLoaded.collectAsState()
    val adLogs by AdMobManager.adLogs.collectAsState()

    var statusMessage by remember { mutableStateOf<String?>(null) }
    var rewardGrantedNotice by remember { mutableStateOf<String?>(null) }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = CardSurface,
        border = BorderStroke(1.dp, NeonViolet.copy(alpha = 0.5f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("admob_test_panel")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (isInitialized) NeonGreen else NeonAmber)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ADMOB TEST LAB",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Black
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (AdConfig.IS_TEST_MODE) NeonAmber.copy(alpha = 0.15f) else NeonGreen.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, if (AdConfig.IS_TEST_MODE) NeonAmber.copy(alpha = 0.4f) else NeonGreen.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = if (AdConfig.IS_TEST_MODE) "TEST ADS ONLY" else "PRODUCTION ACTIVE",
                        color = if (AdConfig.IS_TEST_MODE) NeonAmber else NeonGreen,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Text(
                text = "Mode: ${AdConfig.MODE_NAME}",
                style = MaterialTheme.typography.bodySmall,
                color = if (AdConfig.IS_TEST_MODE) NeonAmber else NeonGreen,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
            )

            // Live status indicators
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatusBadge(
                    label = "Banner",
                    isReady = isInitialized,
                    modifier = Modifier.weight(1f)
                )
                StatusBadge(
                    label = "Interstitial",
                    isReady = isInterstitialLoaded,
                    modifier = Modifier.weight(1f)
                )
                StatusBadge(
                    label = "Rewarded",
                    isReady = isRewardedLoaded,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action triggers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Interstitial trigger
                OutlinedButton(
                    onClick = {
                        if (activity != null) {
                            statusMessage = "Opening test interstitial..."
                            AdMobManager.showInterstitial(activity) {
                                statusMessage = "Interstitial flow completed / dismissed"
                            }
                        } else {
                            statusMessage = "Activity unavailable"
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, NeonCyan),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("test_show_interstitial_button")
                ) {
                    Icon(Icons.Default.Fullscreen, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Interstitial", color = NeonCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                // Rewarded trigger
                Button(
                    onClick = {
                        if (activity != null) {
                            statusMessage = "Launching test rewarded ad..."
                            AdMobManager.showRewardedAd(
                                activity = activity,
                                onRewardEarned = { type, amount ->
                                    rewardGrantedNotice = "VERIFIED REWARD: $type (x$amount) granted!"
                                    onRewardEarned(type, amount)
                                },
                                onAdClosed = { earned ->
                                    statusMessage = if (earned) "Ad closed. Reward was earned." else "Ad closed early. No reward granted."
                                },
                                onAdFailed = { err ->
                                    statusMessage = "Ad failed: $err"
                                }
                            )
                        } else {
                            statusMessage = "Activity unavailable"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen, contentColor = BackgroundDark),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("test_show_rewarded_button")
                ) {
                    Icon(Icons.Default.CardGiftcard, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Rewarded", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Status message
            if (statusMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = statusMessage ?: "",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }

            // Reward notice
            if (rewardGrantedNotice != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = NeonGreen.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, NeonGreen),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = rewardGrantedNotice ?: "",
                        color = NeonGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Configured Ad Units list
            Text(
                text = if (AdConfig.IS_TEST_MODE) "OFFICIAL TEST AD UNITS:" else "ACTIVE PRODUCTION AD UNITS:",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            AdUnitRow("Banner", AdConfig.activeBannerId)
            AdUnitRow("Interstitial", AdConfig.activeInterstitialId)
            AdUnitRow("Rewarded", AdConfig.activeRewardedId)

            // Event Logs Preview
            if (adLogs.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "LATEST AD EVENTS (DEBUG):",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(BackgroundDark)
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val sdf = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
                    adLogs.take(4).forEach { log ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = sdf.format(Date(log.timestamp)),
                                color = TextMuted,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "[${log.tag}]",
                                color = if (log.isSuccess) NeonCyan else NeonRed,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = log.message,
                                color = TextPrimary,
                                fontSize = 10.sp,
                                maxLines = 1,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(
    label: String,
    isReady: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = CardSurfaceElevated,
        border = BorderStroke(1.dp, if (isReady) NeonGreen.copy(alpha = 0.5f) else CardSurfaceBorder),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                fontSize = 11.sp,
                color = TextSecondary,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(if (isReady) NeonGreen else NeonRed)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isReady) "READY" else "WAITING",
                    fontSize = 10.sp,
                    color = if (isReady) NeonGreen else TextMuted,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun AdUnitRow(label: String, id: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$label: ",
            color = TextSecondary,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = id,
            color = NeonCyan,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}
