package com.example.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

/**
 * Question History Tracker
 * Persists all answered question IDs per user and per category using SharedPreferences.
 * Supports:
 * - Excluding previously seen question IDs so every round serves unseen questions
 * - Detecting when a category pool is exhausted and auto-resetting the category history
 * - Resetting/clearing history
 */
class QuestionHistoryTracker(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("brain_battle_question_history", Context.MODE_PRIVATE)

    private fun safeUserKey(uid: String): String = if (uid.isBlank()) "guest" else uid.replace("[^a-zA-Z0-9_]".toRegex(), "_")

    private fun categoryKey(uid: String, categoryId: String): String {
        return "answered_${safeUserKey(uid)}_${categoryId.lowercase()}"
    }

    private fun globalKey(uid: String): String {
        return "answered_${safeUserKey(uid)}_global_all"
    }

    /**
     * Gets all answered question IDs for a given user and category (or all categories if categoryId is null).
     */
    fun getAnsweredQuestionIds(uid: String = "", categoryId: String? = null): Set<String> {
        val key = if (categoryId != null) categoryKey(uid, categoryId) else globalKey(uid)
        return prefs.getStringSet(key, emptySet()) ?: emptySet()
    }

    /**
     * Records a single answered question ID.
     */
    fun markQuestionAnswered(questionId: String, categoryId: String, uid: String = "") {
        markQuestionsAnswered(listOf(questionId), categoryId, uid)
    }

    /**
     * Records multiple answered question IDs in a batch.
     */
    fun markQuestionsAnswered(questionIds: Collection<String>, categoryId: String, uid: String = "") {
        if (questionIds.isEmpty()) return

        val catKey = categoryKey(uid, categoryId)
        val globKey = globalKey(uid)

        val currentCat = (prefs.getStringSet(catKey, emptySet()) ?: emptySet()).toMutableSet()
        currentCat.addAll(questionIds)

        val currentGlob = (prefs.getStringSet(globKey, emptySet()) ?: emptySet()).toMutableSet()
        currentGlob.addAll(questionIds)

        prefs.edit()
            .putStringSet(catKey, currentCat)
            .putStringSet(globKey, currentGlob)
            .apply()

        Log.d("QuestionHistoryTracker", "Recorded ${questionIds.size} answered questions for user=$uid, category=$categoryId. Total in category now=${currentCat.size}")
    }

    /**
     * Checks if the available question pool in a category has been exhausted by the user.
     */
    fun isCategoryExhausted(categoryId: String, totalAvailableInCategory: Int, uid: String = "", neededCount: Int = 10): Boolean {
        val answered = getAnsweredQuestionIds(uid, categoryId)
        val remaining = totalAvailableInCategory - answered.size
        return remaining < neededCount
    }

    /**
     * Resets the answered history for a specific category when exhausted.
     */
    fun resetCategoryHistory(categoryId: String, uid: String = "") {
        val catKey = categoryKey(uid, categoryId)
        prefs.edit().remove(catKey).apply()
        Log.i("QuestionHistoryTracker", "Reset answered history for exhausted category: $categoryId (user=$uid)")
    }

    /**
     * Clears all question history for a specific user.
     */
    fun clearAllHistoryForUser(uid: String = "") {
        val userPrefix = "answered_${safeUserKey(uid)}_"
        val editor = prefs.edit()
        prefs.all.keys.filter { it.startsWith(userPrefix) }.forEach {
            editor.remove(it)
        }
        editor.apply()
    }
}
