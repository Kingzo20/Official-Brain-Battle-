package com.example.data.competitive

import com.example.model.UserProfile

class TitleManager {

    companion object {
        val ALL_TITLES = listOf(
            PlayerTitle("title_rookie", "Rookie", "Begin your competitive journey in Brain Battle.", 1),
            PlayerTitle("title_quick_thinker", "Quick Thinker", "Think fast under pressure and solve challenges rapidly.", 3, AchievementEngine.ID_SPEED_DEMON),
            PlayerTitle("title_puzzle_solver", "Puzzle Solver", "Master tactical puzzle questions across categories.", 5),
            PlayerTitle("title_logic_master", "Logic Master", "Demonstrate superior analytical and deduction skills.", 7, AchievementEngine.ID_LOGIC_MASTER),
            PlayerTitle("title_brain_champion", "Brain Champion", "Reach Level 10 and showcase cognitive mastery.", 10, AchievementEngine.ID_BRAIN_MASTER),
            PlayerTitle("title_century_veteran", "Century Veteran", "Complete 100 competitive battles.", 12, AchievementEngine.ID_CENTURY),
            PlayerTitle("title_grand_prodigy", "Grand Prodigy", "Ascend to elite competitive heights (Level 15+).", 15)
        )
    }

    /**
     * Compute unlocked status for all titles given the current profile and unlocked achievements.
     */
    fun getAvailableTitles(profile: UserProfile, unlockedAchievementIds: Set<String>): List<PlayerTitle> {
        return ALL_TITLES.map { title ->
            val levelMet = profile.level >= title.requiredLevel
            val achievementMet = title.requiredAchievementId == null || title.requiredAchievementId in unlockedAchievementIds
            val isUnlocked = levelMet || achievementMet || title.id == "title_rookie"
            title.copy(isUnlocked = isUnlocked)
        }
    }

    /**
     * Check if equipping a title is valid.
     */
    fun canEquipTitle(titleName: String, profile: UserProfile, unlockedAchievementIds: Set<String>): Boolean {
        val title = ALL_TITLES.find { it.title.equals(titleName, ignoreCase = true) } ?: return false
        val levelMet = profile.level >= title.requiredLevel
        val achievementMet = title.requiredAchievementId == null || title.requiredAchievementId in unlockedAchievementIds
        return levelMet || achievementMet || title.id == "title_rookie"
    }
}
