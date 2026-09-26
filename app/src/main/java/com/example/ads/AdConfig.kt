package com.example.ads

/**
 * Brain Battle AdMob Configuration.
 *
 * SAFE DEVELOPMENT / TEST MODE (Phase 7C Active)
 *
 * CRITICAL SAFETY RULES:
 * 1. IS_TEST_MODE is true for all development, gameplay changes, and feature builds.
 * 2. Active resolvers point strictly to official Google Mobile Ads test units during development.
 * 3. Real Brain Battle production IDs are preserved in ProductionAdUnits and never requested during test mode.
 * 4. Production release mechanism: Simply change IS_TEST_MODE to false for official release.
 */
object AdConfig {
    const val IS_TEST_MODE: Boolean = true
    const val MODE_NAME: String = "DEVELOPMENT / TEST MODE"

    // Official Google AdMob Test Ad Units (For development & testing)
    const val TEST_BANNER_AD_UNIT_ID: String = "ca-app-pub-3940256099942544/9214589741"
    const val TEST_INTERSTITIAL_AD_UNIT_ID: String = "ca-app-pub-3940256099942544/1033173712"
    const val TEST_REWARDED_AD_UNIT_ID: String = "ca-app-pub-3940256099942544/5224354917"
    const val TEST_APP_ID: String = "ca-app-pub-3940256099942544~3347511713"

    /**
     * Official Production Ad Units (Brain Battle).
     * Preserved safely and isolated until official production release.
     */
    object ProductionAdUnits {
        const val ADMOB_APP_ID: String = "ca-app-pub-3706501062745480~3010464180"
        const val ADMOB_BANNER_ID: String = "ca-app-pub-3706501062745480/5475002118"
        const val ADMOB_INTERSTITIAL_ID: String = "ca-app-pub-3706501062745480/4161920445"
        const val ADMOB_REWARDED_ID: String = "ca-app-pub-3706501062745480/5028279558"
    }

    // Active Ad Unit Resolvers (Points to official test units when IS_TEST_MODE is true)
    val activeAppId: String
        get() = if (IS_TEST_MODE) TEST_APP_ID else ProductionAdUnits.ADMOB_APP_ID

    val activeBannerId: String
        get() = if (IS_TEST_MODE) TEST_BANNER_AD_UNIT_ID else ProductionAdUnits.ADMOB_BANNER_ID

    val activeInterstitialId: String
        get() = if (IS_TEST_MODE) TEST_INTERSTITIAL_AD_UNIT_ID else ProductionAdUnits.ADMOB_INTERSTITIAL_ID

    val activeRewardedId: String
        get() = if (IS_TEST_MODE) TEST_REWARDED_AD_UNIT_ID else ProductionAdUnits.ADMOB_REWARDED_ID

    // Backward-compatible accessors
    val BANNER_AD_UNIT_ID: String
        get() = activeBannerId
    val INTERSTITIAL_AD_UNIT_ID: String
        get() = activeInterstitialId
    val REWARDED_AD_UNIT_ID: String
        get() = activeRewardedId
    val SAMPLE_APP_ID: String
        get() = activeAppId

    // Frequency Controls & Policies
    // Minimum time between interstitial ads in seconds (prevents aggressive ad spam)
    const val MIN_INTERSTITIAL_INTERVAL_SECONDS: Long = 30L

    // Retry configuration for asynchronous preloading
    const val MAX_RETRY_ATTEMPTS: Int = 3
    const val BASE_RETRY_DELAY_MS: Long = 5000L

    // In-game Rewards Configuration
    const val REWARD_BONUS_XP_AMOUNT: Int = 50
    const val REWARD_EXTRA_HINTS_AMOUNT: Int = 1
    const val REWARD_DOUBLE_XP_MULTIPLIER: Int = 2
}
