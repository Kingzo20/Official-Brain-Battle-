package com.example.data.competitive

import com.example.model.Difficulty
import com.example.model.GameCategory
import com.example.model.GameModeType
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Competitive Leaderboard Scopes
 */
enum class LeaderboardScope(val displayName: String) {
    GLOBAL("Global"),
    COUNTRY("Country"),
    FRIENDS("Friends"),
    CATEGORY("Category"),
    GAME_MODE("Game Mode")
}

/**
 * Time Periods for Leaderboards
 */
enum class LeaderboardPeriod(val displayName: String) {
    WEEKLY("Weekly"),
    MONTHLY("Monthly"),
    ALL_TIME("All-Time")
}

/**
 * Score Validation and Anti-Cheat Status
 */
enum class ScoreValidationStatus {
    PENDING,
    VALIDATED,
    FLAGGED,
    REJECTED
}

/**
 * Achievement Rarity
 */
enum class AchievementRarity(val label: String, val colorHex: Long) {
    COMMON("Common", 0xFF9E9E9E),
    UNCOMMON("Uncommon", 0xFF00E5FF),
    RARE("Rare", 0xFF7C4DFF),
    EPIC("Epic", 0xFFFFD700),
    LEGENDARY("Legendary", 0xFFFF0055)
}

/**
 * Achievement Categories
 */
enum class AchievementCategory(val title: String) {
    GETTING_STARTED("Getting Started"),
    STREAKS("Streaks"),
    SPEED("Speed"),
    ACCURACY("Accuracy"),
    CATEGORIES("Categories"),
    GAME_MODES("Game Modes"),
    PROGRESSION("Progression"),
    COMPETITIVE("Competitive"),
    SPECIAL("Special")
}

/**
 * Player Titles earned through competitive milestones
 */
data class PlayerTitle(
    val id: String,
    val title: String,
    val description: String,
    val requiredLevel: Int = 1,
    val requiredAchievementId: String? = null,
    val isUnlocked: Boolean = false
)

/**
 * Competitive Rank Tiers based on XP and competitive rating
 */
enum class RankTier(val displayName: String, val badgeEmoji: String, val minXp: Int, val colorHex: Long) {
    BRONZE("Bronze", "🥉", 0, 0xFFCD7F32),
    SILVER("Silver", "🥈", 500, 0xFFC0C0C0),
    GOLD("Gold", "🥇", 1500, 0xFFFFD700),
    PLATINUM("Platinum", "💎", 3500, 0xFF00E5FF),
    DIAMOND("Diamond", "💠", 7000, 0xFF7C4DFF),
    MASTER("Master", "👑", 12000, 0xFFFF0055),
    GRANDMASTER("Grandmaster", "⚡", 20000, 0xFFFFD700);

    companion object {
        fun fromXp(xp: Int): RankTier {
            return values().reversed().firstOrNull { xp >= it.minXp } ?: BRONZE
        }
    }
}

/**
 * Personal Best Record breakdown
 */
data class PersonalBestRecord(
    val overallBest: Int = 0,
    val modeBests: Map<String, Int> = emptyMap(),
    val categoryBests: Map<String, Int> = emptyMap(),
    val difficultyBests: Map<String, Int> = emptyMap(),
    val lastUpdated: Long = System.currentTimeMillis()
)

/**
 * Rank Movement Tracking
 */
data class RankMovementInfo(
    val previousRank: Int?,
    val currentRank: Int,
    val delta: Int, // positive = improved (+5 ranks), 0 = unchanged, negative = dropped
    val period: LeaderboardPeriod,
    val message: String
)

/**
 * Season Information
 */
data class SeasonInfo(
    val seasonNumber: Int = 1,
    val name: String = "Season 1: Synapse Awakening",
    val startDate: String = "2026-09-01",
    val endDate: String = "2026-11-30",
    val isActive: Boolean = true
)

/**
 * Period Helper providing uniform period identifiers:
 * Weekly: e.g. "2026-W39"
 * Monthly: e.g. "2026-09"
 * All-Time: "ALL_TIME"
 */
object PeriodHelper {

    fun getCurrentWeeklyPeriodId(date: Date = Date()): String {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.US)
        cal.firstDayOfWeek = Calendar.MONDAY
        cal.time = date
        val year = cal.get(Calendar.YEAR)
        val week = cal.get(Calendar.WEEK_OF_YEAR)
        return String.format(Locale.US, "%04d-W%02d", year, week)
    }

    fun getCurrentMonthlyPeriodId(date: Date = Date()): String {
        val sdf = SimpleDateFormat("yyyy-MM", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(date)
    }

    fun getAllTimePeriodId(): String = "ALL_TIME"

    fun getPeriodId(period: LeaderboardPeriod, date: Date = Date()): String {
        return when (period) {
            LeaderboardPeriod.WEEKLY -> getCurrentWeeklyPeriodId(date)
            LeaderboardPeriod.MONTHLY -> getCurrentMonthlyPeriodId(date)
            LeaderboardPeriod.ALL_TIME -> getAllTimePeriodId()
        }
    }

    fun getPreviousPeriodId(period: LeaderboardPeriod): String {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.US)
        return when (period) {
            LeaderboardPeriod.WEEKLY -> {
                cal.firstDayOfWeek = Calendar.MONDAY
                cal.add(Calendar.WEEK_OF_YEAR, -1)
                val year = cal.get(Calendar.YEAR)
                val week = cal.get(Calendar.WEEK_OF_YEAR)
                String.format(Locale.US, "%04d-W%02d", year, week)
            }
            LeaderboardPeriod.MONTHLY -> {
                cal.add(Calendar.MONTH, -1)
                val sdf = SimpleDateFormat("yyyy-MM", Locale.US)
                sdf.timeZone = TimeZone.getTimeZone("UTC")
                sdf.format(cal.time)
            }
            LeaderboardPeriod.ALL_TIME -> "ALL_TIME"
        }
    }
}
