package com.example.data.competitive

import android.content.Context
import android.content.SharedPreferences
import com.example.model.Difficulty
import com.example.model.GameCategory
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

data class PersonalBestEvaluation(
    val isOverallBest: Boolean,
    val isModeBest: Boolean,
    val isCategoryBest: Boolean,
    val isDifficultyBest: Boolean,
    val previousOverallBest: Int,
    val previousModeBest: Int,
    val previousCategoryBest: Int,
    val previousDifficultyBest: Int
) {
    val isAnyNewPersonalBest: Boolean
        get() = isOverallBest || isModeBest || isCategoryBest || isDifficultyBest
}

class PersonalBestService(context: Context? = null) {

    private val prefs: SharedPreferences? = context?.getSharedPreferences("brain_battle_personal_bests", Context.MODE_PRIVATE)

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val mapType = Types.newParameterizedType(Map::class.java, String::class.java, Any::class.java)

    private var inMemoryRecord: PersonalBestRecord = loadRecord()

    private fun loadRecord(): PersonalBestRecord {
        val overall = prefs?.getInt("pb_overall", 0) ?: 0
        val modeMap = loadIntMap("pb_modes")
        val catMap = loadIntMap("pb_categories")
        val diffMap = loadIntMap("pb_difficulties")
        val updated = prefs?.getLong("pb_updated_at", System.currentTimeMillis()) ?: System.currentTimeMillis()

        return PersonalBestRecord(
            overallBest = overall,
            modeBests = modeMap,
            categoryBests = catMap,
            difficultyBests = diffMap,
            lastUpdated = updated
        )
    }

    private fun loadIntMap(key: String): Map<String, Int> {
        val json = prefs?.getString(key, null) ?: return emptyMap()
        return try {
            val raw = moshi.adapter<Map<String, Number>>(mapType).fromJson(json) ?: return emptyMap()
            raw.mapValues { it.value.toInt() }
        } catch (e: Exception) {
            emptyMap()
        }
    }

    private fun saveRecord(record: PersonalBestRecord) {
        inMemoryRecord = record
        prefs?.edit()?.apply {
            putInt("pb_overall", record.overallBest)
            try {
                putString("pb_modes", moshi.adapter<Map<String, Int>>(Types.newParameterizedType(Map::class.java, String::class.java, Int::class.javaObjectType)).toJson(record.modeBests))
                putString("pb_categories", moshi.adapter<Map<String, Int>>(Types.newParameterizedType(Map::class.java, String::class.java, Int::class.javaObjectType)).toJson(record.categoryBests))
                putString("pb_difficulties", moshi.adapter<Map<String, Int>>(Types.newParameterizedType(Map::class.java, String::class.java, Int::class.javaObjectType)).toJson(record.difficultyBests))
            } catch (e: Exception) {
                // Ignore
            }
            putLong("pb_updated_at", record.lastUpdated)
            apply()
        }
    }

    fun getRecord(): PersonalBestRecord = inMemoryRecord

    fun getOverallBest(): Int = inMemoryRecord.overallBest

    fun getModeBest(mode: String): Int = inMemoryRecord.modeBests[mode] ?: 0

    fun getCategoryBest(categoryId: String): Int = inMemoryRecord.categoryBests[categoryId] ?: 0

    fun getDifficultyBest(difficulty: String): Int = inMemoryRecord.difficultyBests[difficulty] ?: 0

    /**
     * Initializes personal best record from UserProfile if no records exist yet.
     */
    fun syncWithInitialProfile(initialBestScore: Int) {
        if (inMemoryRecord.overallBest == 0 && initialBestScore > 0) {
            val updated = inMemoryRecord.copy(
                overallBest = initialBestScore,
                lastUpdated = System.currentTimeMillis()
            )
            saveRecord(updated)
        }
    }

    /**
     * Evaluates a game score against actual historical bests.
     * Updates record in-place if new bests are achieved.
     */
    fun evaluateAndRecord(
        score: Int,
        gameMode: String,
        categoryId: String,
        difficulty: String
    ): PersonalBestEvaluation {
        val current = inMemoryRecord

        val prevOverall = current.overallBest
        val prevMode = current.modeBests[gameMode] ?: 0
        val prevCategory = current.categoryBests[categoryId] ?: 0
        val prevDifficulty = current.difficultyBests[difficulty] ?: 0

        val isOverallBest = score > prevOverall
        val isModeBest = score > prevMode
        val isCategoryBest = score > prevCategory
        val isDifficultyBest = score > prevDifficulty

        val newOverall = if (isOverallBest) score else prevOverall
        val newModes = current.modeBests.toMutableMap()
        if (isModeBest) newModes[gameMode] = score

        val newCategories = current.categoryBests.toMutableMap()
        if (isCategoryBest) newCategories[categoryId] = score

        val newDifficulties = current.difficultyBests.toMutableMap()
        if (isDifficultyBest) newDifficulties[difficulty] = score

        if (isOverallBest || isModeBest || isCategoryBest || isDifficultyBest) {
            val updated = current.copy(
                overallBest = newOverall,
                modeBests = newModes,
                categoryBests = newCategories,
                difficultyBests = newDifficulties,
                lastUpdated = System.currentTimeMillis()
            )
            saveRecord(updated)
        }

        return PersonalBestEvaluation(
            isOverallBest = isOverallBest,
            isModeBest = isModeBest,
            isCategoryBest = isCategoryBest,
            isDifficultyBest = isDifficultyBest,
            previousOverallBest = prevOverall,
            previousModeBest = prevMode,
            previousCategoryBest = prevCategory,
            previousDifficultyBest = prevDifficulty
        )
    }
}
