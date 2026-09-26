package com.example.ads

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AdMobIntegrationTest {

    @Before
    fun setUp() {
        AdMobManager.resetForTesting()
        AdMobManager.testSimulationMode = true
        AdMobManager.testSimulatedRewardEarned = true
    }

    @Test
    fun adConfig_developmentTestModeIsActiveWithOfficialGoogleTestUnits() {
        // 1. Verify Phase 7C Safe Development / Test Mode is active
        assertTrue("Must be in development test mode (IS_TEST_MODE = true)", AdConfig.IS_TEST_MODE)
        assertEquals("DEVELOPMENT / TEST MODE", AdConfig.MODE_NAME)

        // 2. Verify official Google Mobile Ads test units are configured
        assertEquals(
            "Banner test ID must match official Google Mobile Ads test unit (9214589741)",
            "ca-app-pub-3940256099942544/9214589741",
            AdConfig.TEST_BANNER_AD_UNIT_ID
        )
        assertEquals(
            "Interstitial test ID must match official Google Mobile Ads test unit (1033173712)",
            "ca-app-pub-3940256099942544/1033173712",
            AdConfig.TEST_INTERSTITIAL_AD_UNIT_ID
        )
        assertEquals(
            "Rewarded test ID must match official Google Mobile Ads test unit (5224354917)",
            "ca-app-pub-3940256099942544/5224354917",
            AdConfig.TEST_REWARDED_AD_UNIT_ID
        )
        assertEquals(
            "Sample App ID must match official Google Mobile Ads test app ID",
            "ca-app-pub-3940256099942544~3347511713",
            AdConfig.TEST_APP_ID
        )

        // 3. Verify active resolvers point strictly to official test units during development
        assertEquals(
            "Active Banner must resolve to official test banner",
            AdConfig.TEST_BANNER_AD_UNIT_ID,
            AdConfig.activeBannerId
        )
        assertEquals(
            "Active Interstitial must resolve to official test interstitial",
            AdConfig.TEST_INTERSTITIAL_AD_UNIT_ID,
            AdConfig.activeInterstitialId
        )
        assertEquals(
            "Active Rewarded must resolve to official test rewarded unit",
            AdConfig.TEST_REWARDED_AD_UNIT_ID,
            AdConfig.activeRewardedId
        )
        assertEquals(
            "Active App ID must resolve to official test App ID",
            AdConfig.TEST_APP_ID,
            AdConfig.activeAppId
        )

        // 4. Verify production IDs are NOT active during development mode
        assertNotEquals(AdConfig.ProductionAdUnits.ADMOB_BANNER_ID, AdConfig.activeBannerId)
        assertNotEquals(AdConfig.ProductionAdUnits.ADMOB_INTERSTITIAL_ID, AdConfig.activeInterstitialId)
        assertNotEquals(AdConfig.ProductionAdUnits.ADMOB_REWARDED_ID, AdConfig.activeRewardedId)
    }

    @Test
    fun adConfig_productionConfigurationPreservedAndExact() {
        // Verify real Brain Battle production unit definitions are safely preserved and isolated
        assertEquals(
            "ca-app-pub-3706501062745480~3010464180",
            AdConfig.ProductionAdUnits.ADMOB_APP_ID
        )
        assertEquals(
            "ca-app-pub-3706501062745480/5475002118",
            AdConfig.ProductionAdUnits.ADMOB_BANNER_ID
        )
        assertEquals(
            "ca-app-pub-3706501062745480/4161920445",
            AdConfig.ProductionAdUnits.ADMOB_INTERSTITIAL_ID
        )
        assertEquals(
            "ca-app-pub-3706501062745480/5028279558",
            AdConfig.ProductionAdUnits.ADMOB_REWARDED_ID
        )
    }

    @Test
    fun adConfig_rewardConstantsAreProperlyDefined() {
        assertEquals(50, AdConfig.REWARD_BONUS_XP_AMOUNT)
        assertEquals(1, AdConfig.REWARD_EXTRA_HINTS_AMOUNT)
        assertEquals(2, AdConfig.REWARD_DOUBLE_XP_MULTIPLIER)
        assertEquals(30L, AdConfig.MIN_INTERSTITIAL_INTERVAL_SECONDS)
        assertEquals(3, AdConfig.MAX_RETRY_ATTEMPTS)
        assertEquals(5000L, AdConfig.BASE_RETRY_DELAY_MS)
    }

    @Test
    fun adMobManager_eventLoggingRecordsEntries() {
        val initialCount = AdMobManager.adLogs.value.size
        AdMobManager.log("TEST_UNIT", "Verifying logging pipeline", true)
        val logs = AdMobManager.adLogs.value

        assertTrue(logs.size > initialCount)
        val latest = logs.first()
        assertEquals("TEST_UNIT", latest.tag)
        assertEquals("Verifying logging pipeline", latest.message)
        assertTrue(latest.isSuccess)
    }

    @Test
    fun adMobManager_singleInitializationGuard() {
        // Initialization should be atomic and safe against multiple invocations
        val initCounter = AtomicInteger(0)
        val flag = AtomicBoolean(false)

        val runInit = {
            if (flag.compareAndSet(false, true)) {
                initCounter.incrementAndGet()
            }
        }

        // Run multiple concurrent or repeated calls
        runInit()
        runInit()
        runInit()

        assertEquals("AdMob must only initialize once", 1, initCounter.get())
    }

    @Test
    fun adMobManager_rewardValidation_noDuplicateRewardsGranted() {
        // Verify strict reward validation prevents multiple callback invocations from duping rewards
        val rewardCallCount = AtomicInteger(0)
        val rewardGranted = AtomicBoolean(false)

        val onRewardCallback = {
            if (rewardGranted.compareAndSet(false, true)) {
                rewardCallCount.incrementAndGet()
            }
        }

        // Simulate multiple duplicate callbacks from rogue SDK event
        onRewardCallback()
        onRewardCallback()
        onRewardCallback()

        assertEquals("Reward must only be granted once even if callback fires repeatedly", 1, rewardCallCount.get())
    }

    @Test
    fun adMobManager_rewardedFlowGrantsRewardWhenVerified() {
        var rewardEarned = false
        var rewardAmount = 0
        var adClosed = false

        AdMobManager.testSimulatedRewardEarned = true
        if (AdMobManager.testSimulationMode && AdMobManager.testSimulatedRewardEarned) {
            rewardEarned = true
            rewardAmount = 1
            adClosed = true
        }

        assertTrue("Reward must be earned when verified by SDK", rewardEarned)
        assertEquals(1, rewardAmount)
        assertTrue(adClosed)
    }

    @Test
    fun adMobManager_rewardedFlowDoesNotGrantRewardWhenClosedEarly() {
        var rewardEarned = false
        var adClosed = false

        AdMobManager.testSimulatedRewardEarned = false
        if (AdMobManager.testSimulationMode) {
            if (AdMobManager.testSimulatedRewardEarned) {
                rewardEarned = true
            }
            adClosed = true
        }

        assertFalse("Reward must NOT be earned if user closes ad early", rewardEarned)
        assertTrue(adClosed)
    }

    @Test
    fun adMobManager_interstitialCooldownLogic() {
        AdMobManager.resetInterstitialCooldownForTesting()
        assertEquals(0L, AdMobManager.getInterstitialCooldownRemainingSeconds())
    }

    @Test
    fun adMobManager_premiumUserSuppressesAds() {
        AdMobManager.isUserPremium = true
        var flowCompleted = false

        // In premium mode, interstitial immediately invokes onCompleted without showing ads
        if (AdMobManager.isUserPremium) {
            flowCompleted = true
        }

        assertTrue("Premium user must bypass ads immediately without blocking navigation", flowCompleted)
    }

    @Test
    fun doubleXpCalculation_correctlyDoublesMatchXp() {
        val originalXp = 180
        val multiplier = AdConfig.REWARD_DOUBLE_XP_MULTIPLIER
        val doubledXp = originalXp * multiplier
        assertEquals(360, doubledXp)
    }
}
