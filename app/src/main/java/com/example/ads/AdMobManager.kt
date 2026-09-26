package com.example.ads

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.atomic.AtomicBoolean

data class AdLogEntry(
    val timestamp: Long = System.currentTimeMillis(),
    val tag: String,
    val message: String,
    val isSuccess: Boolean
)

/**
 * Singleton AdMob Manager for Brain Battle.
 *
 * Runs exclusively in DEVELOPMENT / TEST ADS MODE with official test ad units.
 * Ad operations are non-blocking, isolated from core gameplay, and resilient to network loss.
 */
object AdMobManager {

    private const val TAG = "BrainBattle_AdMob"

    private val initializationStarted = AtomicBoolean(false)

    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()

    private val _isInterstitialLoaded = MutableStateFlow(false)
    val isInterstitialLoaded: StateFlow<Boolean> = _isInterstitialLoaded.asStateFlow()

    private val _isRewardedLoaded = MutableStateFlow(false)
    val isRewardedLoaded: StateFlow<Boolean> = _isRewardedLoaded.asStateFlow()

    private val _adLogs = MutableStateFlow<List<AdLogEntry>>(emptyList())
    val adLogs: StateFlow<List<AdLogEntry>> = _adLogs.asStateFlow()

    private var interstitialAd: InterstitialAd? = null
    private var rewardedAd: RewardedAd? = null

    private var isInterstitialLoading = false
    private var isRewardedLoading = false

    private var interstitialRetryCount = 0
    private var rewardedRetryCount = 0

    private var lastInterstitialShowTime = 0L

    /**
     * Premium architecture status flag.
     * When true, banner ads are omitted and interstitial ads are suppressed.
     */
    var isUserPremium: Boolean = false

    private fun postDelayed(delayMillis: Long, action: () -> Unit) {
        try {
            Handler(Looper.getMainLooper()).postDelayed(action, delayMillis)
        } catch (e: Throwable) {
            // Fallback for JVM test environments
            action()
        }
    }

    // Testing simulation delegate (for unit tests where Google Play Services is absent)
    var testSimulationMode: Boolean = false
    var testSimulatedRewardEarned: Boolean = true

    /**
     * Initializes Google Mobile Ads SDK exactly once.
     * Safe to invoke multiple times; subsequent calls are ignored.
     */
    fun initialize(context: Context) {
        if (!initializationStarted.compareAndSet(false, true)) {
            log("INIT", "AdMob SDK initialization already invoked; ignoring duplicate call.", true)
            return
        }

        try {
            log("INIT", "Initializing Google Mobile Ads SDK (${AdConfig.MODE_NAME})...", true)

            // Configure test device request configuration
            val configuration = RequestConfiguration.Builder()
                .setTestDeviceIds(listOf(AdRequest.DEVICE_ID_EMULATOR))
                .build()
            MobileAds.setRequestConfiguration(configuration)

            MobileAds.initialize(context.applicationContext) { status ->
                _isInitialized.value = true
                val map = status.adapterStatusMap
                log("INIT", "AdMob SDK Initialized. Active Adapters: ${map.size}", true)

                // Preload interstitial and rewarded ads asynchronously after initialization
                if (!isUserPremium) {
                    preloadInterstitial(context.applicationContext)
                }
                preloadRewarded(context.applicationContext)
            }
        } catch (e: Exception) {
            log("INIT", "AdMob initialization encountered error: ${e.localizedMessage}", false)
            // Even if MobileAds throws (e.g. in test JVM or missing GMS), mark initialized to allow app flow
            _isInitialized.value = true
        }
    }

    /**
     * Checks if device is currently connected to active network.
     */
    fun isNetworkAvailable(context: Context): Boolean {
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                ?: return true
            val activeNetwork = cm.activeNetwork ?: return false
            val capabilities = cm.getNetworkCapabilities(activeNetwork) ?: return false
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } catch (e: Exception) {
            true // Default to true if unable to check
        }
    }

    // ==========================================
    // INTERSTITIAL ADS
    // ==========================================

    fun preloadInterstitial(context: Context) {
        if (isUserPremium) {
            log("INTERSTITIAL", "Preload skipped: user is Premium (ad-free).", true)
            return
        }

        if (isInterstitialLoading || interstitialAd != null) {
            return
        }

        if (testSimulationMode) {
            _isInterstitialLoaded.value = true
            log("INTERSTITIAL", "[TEST SIM] Interstitial loaded successfully", true)
            return
        }

        if (!isNetworkAvailable(context)) {
            log("INTERSTITIAL", "Skipping preload - device is offline", false)
            return
        }

        isInterstitialLoading = true
        val adUnitId = AdConfig.activeInterstitialId
        log("INTERSTITIAL", "Preloading Interstitial ad ($adUnitId)...", true)

        try {
            val adRequest = AdRequest.Builder().build()
            InterstitialAd.load(
                context.applicationContext,
                adUnitId,
                adRequest,
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(ad: InterstitialAd) {
                        interstitialAd = ad
                        isInterstitialLoading = false
                        interstitialRetryCount = 0
                        _isInterstitialLoaded.value = true
                        log("INTERSTITIAL", "Interstitial loaded successfully", true)
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        interstitialAd = null
                        isInterstitialLoading = false
                        _isInterstitialLoaded.value = false
                        log("INTERSTITIAL", "Interstitial failed to load: ${error.message} (code ${error.code})", false)

                        // Exponential backoff retry
                        if (interstitialRetryCount < AdConfig.MAX_RETRY_ATTEMPTS) {
                            interstitialRetryCount++
                            val delayMs = AdConfig.BASE_RETRY_DELAY_MS * interstitialRetryCount
                            postDelayed(delayMs) {
                                preloadInterstitial(context)
                            }
                        }
                    }
                }
            )
        } catch (e: Exception) {
            isInterstitialLoading = false
            log("INTERSTITIAL", "Exception loading interstitial: ${e.message}", false)
        }
    }

    /**
     * Calculates remaining cooldown time before next interstitial can be displayed.
     */
    fun getInterstitialCooldownRemainingSeconds(): Long {
        if (lastInterstitialShowTime == 0L) return 0L
        val elapsedSec = (System.currentTimeMillis() - lastInterstitialShowTime) / 1000L
        val remaining = AdConfig.MIN_INTERSTITIAL_INTERVAL_SECONDS - elapsedSec
        return if (remaining > 0L) remaining else 0L
    }

    /**
     * Resets cooldown timer (primarily for testing purposes).
     */
    fun resetInterstitialCooldownForTesting() {
        lastInterstitialShowTime = 0L
    }

    /**
     * Attempts to display an interstitial ad at natural transition points.
     * If ad is not ready, user is premium, ad is on cooldown, or fails,
     * [onCompleted] is called immediately without blocking gameplay flow.
     */
    fun showInterstitial(
        activity: Activity,
        onCompleted: () -> Unit
    ) {
        if (isUserPremium) {
            log("INTERSTITIAL", "User is premium. Interstitial skipped.", true)
            onCompleted()
            return
        }

        if (testSimulationMode) {
            log("INTERSTITIAL", "[TEST SIM] Showing simulated interstitial", true)
            lastInterstitialShowTime = System.currentTimeMillis()
            _isInterstitialLoaded.value = false
            onCompleted()
            return
        }

        val currentTime = System.currentTimeMillis()
        val elapsedSec = (currentTime - lastInterstitialShowTime) / 1000L
        if (elapsedSec < AdConfig.MIN_INTERSTITIAL_INTERVAL_SECONDS && lastInterstitialShowTime != 0L) {
            log("INTERSTITIAL", "Interstitial skipped due to cooldown ($elapsedSec/${AdConfig.MIN_INTERSTITIAL_INTERVAL_SECONDS}s)", true)
            onCompleted()
            return
        }

        val ad = interstitialAd
        if (ad == null) {
            log("INTERSTITIAL", "Interstitial not ready yet. Continuing flow.", true)
            onCompleted()
            preloadInterstitial(activity.applicationContext)
            return
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                log("INTERSTITIAL", "Interstitial displayed on screen", true)
                lastInterstitialShowTime = System.currentTimeMillis()
            }

            override fun onAdDismissedFullScreenContent() {
                log("INTERSTITIAL", "Interstitial dismissed by user. Continuing flow.", true)
                interstitialAd = null
                _isInterstitialLoaded.value = false
                onCompleted()
                preloadInterstitial(activity.applicationContext)
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                log("INTERSTITIAL", "Interstitial failed to show: ${adError.message}. Continuing.", false)
                interstitialAd = null
                _isInterstitialLoaded.value = false
                onCompleted()
                preloadInterstitial(activity.applicationContext)
            }
        }

        try {
            ad.show(activity)
        } catch (e: Exception) {
            log("INTERSTITIAL", "Exception showing interstitial: ${e.message}", false)
            interstitialAd = null
            _isInterstitialLoaded.value = false
            onCompleted()
        }
    }

    // ==========================================
    // REWARDED ADS
    // ==========================================

    fun preloadRewarded(context: Context) {
        if (isRewardedLoading || rewardedAd != null) {
            return
        }

        if (testSimulationMode) {
            _isRewardedLoaded.value = true
            log("REWARDED", "[TEST SIM] Rewarded ad loaded successfully", true)
            return
        }

        if (!isNetworkAvailable(context)) {
            log("REWARDED", "Skipping preload - device is offline", false)
            return
        }

        isRewardedLoading = true
        val adUnitId = AdConfig.activeRewardedId
        log("REWARDED", "Preloading Rewarded ad ($adUnitId)...", true)

        try {
            val adRequest = AdRequest.Builder().build()
            RewardedAd.load(
                context.applicationContext,
                adUnitId,
                adRequest,
                object : RewardedAdLoadCallback() {
                    override fun onAdLoaded(ad: RewardedAd) {
                        rewardedAd = ad
                        isRewardedLoading = false
                        rewardedRetryCount = 0
                        _isRewardedLoaded.value = true
                        log("REWARDED", "Rewarded ad loaded successfully", true)
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        rewardedAd = null
                        isRewardedLoading = false
                        _isRewardedLoaded.value = false
                        log("REWARDED", "Rewarded ad failed to load: ${error.message} (code ${error.code})", false)

                        if (rewardedRetryCount < AdConfig.MAX_RETRY_ATTEMPTS) {
                            rewardedRetryCount++
                            val delayMs = AdConfig.BASE_RETRY_DELAY_MS * rewardedRetryCount
                            postDelayed(delayMs) {
                                preloadRewarded(context)
                            }
                        }
                    }
                }
            )
        } catch (e: Exception) {
            isRewardedLoading = false
            log("REWARDED", "Exception loading rewarded ad: ${e.message}", false)
        }
    }

    /**
     * Shows a rewarded advertisement.
     *
     * Strict reward validation:
     * - [onRewardEarned] is ONLY invoked when the Google SDK confirms reward completion.
     * - Guarded by atomic boolean to prevent duplicate reward grants.
     * - If ad fails, closes early, or device is offline, reward is NOT granted.
     */
    fun showRewardedAd(
        activity: Activity,
        onRewardEarned: (rewardType: String, amount: Int) -> Unit,
        onAdClosed: (rewardEarned: Boolean) -> Unit,
        onAdFailed: (errorMessage: String) -> Unit
    ) {
        if (!isNetworkAvailable(activity)) {
            val offlineMsg = "You're offline. Check your connection and try again."
            log("REWARDED", offlineMsg, false)
            onAdFailed(offlineMsg)
            return
        }

        if (testSimulationMode) {
            log("REWARDED", "[TEST SIM] Triggering simulated rewarded flow", true)
            if (testSimulatedRewardEarned) {
                onRewardEarned("TEST_REWARD", 1)
                onAdClosed(true)
            } else {
                onAdClosed(false)
            }
            return
        }

        val ad = rewardedAd
        if (ad == null) {
            val msg = "Rewarded ad is not ready yet. Please try again in a moment."
            log("REWARDED", msg, false)
            onAdFailed(msg)
            preloadRewarded(activity.applicationContext)
            return
        }

        val rewardGranted = AtomicBoolean(false)
        val userEarnedReward = AtomicBoolean(false)

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                log("REWARDED", "Rewarded ad displayed on screen", true)
            }

            override fun onAdDismissedFullScreenContent() {
                log("REWARDED", "Rewarded ad dismissed. Reward earned: ${userEarnedReward.get()}", true)
                rewardedAd = null
                _isRewardedLoaded.value = false
                onAdClosed(userEarnedReward.get())
                preloadRewarded(activity.applicationContext)
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                log("REWARDED", "Rewarded ad failed to show: ${adError.message}", false)
                rewardedAd = null
                _isRewardedLoaded.value = false
                onAdFailed(adError.message)
                preloadRewarded(activity.applicationContext)
            }
        }

        try {
            ad.show(activity) { rewardItem ->
                userEarnedReward.set(true)
                if (rewardGranted.compareAndSet(false, true)) {
                    log("REWARDED", "Reward earned callback verified: ${rewardItem.type} x${rewardItem.amount}", true)
                    onRewardEarned(rewardItem.type, rewardItem.amount)
                }
            }
        } catch (e: Exception) {
            log("REWARDED", "Exception showing rewarded ad: ${e.message}", false)
            rewardedAd = null
            _isRewardedLoaded.value = false
            onAdFailed(e.message ?: "Failed to display rewarded ad")
        }
    }

    fun log(format: String, message: String, isSuccess: Boolean) {
        val entry = AdLogEntry(tag = format, message = message, isSuccess = isSuccess)
        try {
            Log.d(TAG, "[$format] $message")
        } catch (e: Throwable) {
            // Ignored in non-Android unit test runtime
        }
        _adLogs.value = (listOf(entry) + _adLogs.value).take(30)
    }

    /**
     * Resets state for testing purposes only.
     */
    fun resetForTesting() {
        interstitialAd = null
        rewardedAd = null
        isInterstitialLoading = false
        isRewardedLoading = false
        interstitialRetryCount = 0
        rewardedRetryCount = 0
        lastInterstitialShowTime = 0L
        isUserPremium = false
        _isInterstitialLoaded.value = false
        _isRewardedLoaded.value = false
        testSimulationMode = false
        testSimulatedRewardEarned = true
    }
}
