package com.example.ads

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.ui.theme.CardSurface
import com.example.ui.theme.CardSurfaceBorder
import com.example.ui.theme.TextMuted
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

@Composable
fun BannerAdView(
    modifier: Modifier = Modifier,
    adUnitId: String = AdConfig.activeBannerId,
    showTestLabel: Boolean = AdConfig.IS_TEST_MODE
) {
    if (AdMobManager.isUserPremium) {
        // Premium users experience an ad-free interface
        return
    }

    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var isLoaded by remember { mutableStateOf(false) }
    var isFailed by remember { mutableStateOf(false) }
    var currentAdView by remember { mutableStateOf<AdView?>(null) }

    // Lifecycle observer to pause/resume/destroy adView
    DisposableEffect(lifecycleOwner, currentAdView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> currentAdView?.resume()
                Lifecycle.Event.ON_PAUSE -> currentAdView?.pause()
                Lifecycle.Event.ON_DESTROY -> currentAdView?.destroy()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            currentAdView?.destroy()
        }
    }

    // Gracefully collapses to zero height when failed or not loaded
    AnimatedVisibility(
        visible = isLoaded && !isFailed,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
                .testTag("banner_ad_wrapper"),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (showTestLabel) {
                Text(
                    text = "TEST ADVERTISEMENT",
                    color = TextMuted,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(CardSurface)
                    .border(0.5.dp, CardSurfaceBorder, RoundedCornerShape(8.dp))
                    .padding(2.dp),
                contentAlignment = Alignment.Center
            ) {
                AndroidView(
                    modifier = Modifier
                        .wrapContentSize()
                        .testTag("banner_ad_view"),
                    factory = { ctx ->
                        AdView(ctx).apply {
                            setAdUnitId(adUnitId)
                            val screenWidth = configuration.screenWidthDp
                            val adSize = try {
                                AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(ctx, screenWidth)
                            } catch (e: Exception) {
                                AdSize.BANNER
                            }
                            setAdSize(adSize)

                            adListener = object : AdListener() {
                                override fun onAdLoaded() {
                                    isLoaded = true
                                    isFailed = false
                                    AdMobManager.log("BANNER", "Banner loaded successfully (${adSize.width}x${adSize.height})", true)
                                }

                                override fun onAdFailedToLoad(error: LoadAdError) {
                                    isLoaded = false
                                    isFailed = true
                                    AdMobManager.log("BANNER", "Banner failed: ${error.message} (code ${error.code})", false)
                                }

                                override fun onAdOpened() {
                                    AdMobManager.log("BANNER", "Banner ad opened full screen", true)
                                }

                                override fun onAdClosed() {
                                    AdMobManager.log("BANNER", "Banner closed", true)
                                }
                            }

                            loadAd(AdRequest.Builder().build())
                            currentAdView = this
                        }
                    },
                    update = {
                        // View is running
                    }
                )
            }
        }
    }
}
