package com.example.data.ai

import com.example.data.QuestionBank
import com.example.data.local.LocalStorageRepository
import com.example.model.*
import java.util.concurrent.ConcurrentHashMap

class QuestionAdminService(
    private val localRepo: LocalStorageRepository? = null,
    private val validationService: QuestionValidationService = QuestionValidationService(),
    private val aiQuestionService: AiQuestionService = AiQuestionServiceImpl()
) {

    private val approvedQuestionsMap = ConcurrentHashMap<String, Question>()
    private val rejectedQuestionsMap = ConcurrentHashMap<String, Pair<Question, List<String>>>()
    private val questionReports = mutableListOf<QuestionReport>()
    private val disabledQuestionIds = mutableSetOf<String>()

    init {
        // Seed approved pool with curated question bank
        QuestionBank.allQuestions.forEach { q ->
            approvedQuestionsMap[q.id] = q.copy(
                source = QuestionSource.CURATED,
                validationStatus = ValidationStatus.APPROVED
            )
        }

        // Load cached approved questions from local store if available
        localRepo?.loadApprovedQuestions()?.forEach { q ->
            if (q.validationStatus == ValidationStatus.APPROVED) {
                approvedQuestionsMap[q.id] = q
            }
        }

        // Load disabled question IDs & reports
        localRepo?.loadDisabledQuestionIds()?.let {
            disabledQuestionIds.addAll(it)
        }
        localRepo?.loadQuestionReports()?.let {
            questionReports.addAll(it)
        }
    }

    /**
     * Validates a candidate question. If approved, stores it in the active question pool.
     */
    fun validateAndProcess(candidate: Question): QuestionValidationResult {
        val result = validationService.validate(candidate, approvedQuestionsMap.values)

        if (result.isValid) {
            val approved = candidate.copy(
                validationStatus = ValidationStatus.APPROVED,
                qualityScore = result.qualityScore
            )
            approvedQuestionsMap[approved.id] = approved
            saveApprovedToStorage()
        } else {
            val rejected = candidate.copy(
                validationStatus = ValidationStatus.REJECTED,
                qualityScore = result.qualityScore
            )
            rejectedQuestionsMap[rejected.id] = Pair(rejected, result.failureReasons)
        }

        return result
    }

    /**
     * Generates a batch of questions via AI and runs them through the validation pipeline.
     * Only questions meeting the strict criteria enter the playable pool.
     */
    suspend fun generateAndValidateBatch(
        category: GameCategory,
        difficulty: Difficulty,
        count: Int = 5
    ): Pair<List<Question>, List<Pair<Question, List<String>>>> {
        val candidates = aiQuestionService.generateBatch(category, difficulty, count)
        val approved = mutableListOf<Question>()
        val rejected = mutableListOf<Pair<Question, List<String>>>()

        for (candidate in candidates) {
            val result = validationService.validate(candidate, approvedQuestionsMap.values)
            if (result.isValid) {
                val q = candidate.copy(
                    validationStatus = ValidationStatus.APPROVED,
                    qualityScore = result.qualityScore
                )
                approvedQuestionsMap[q.id] = q
                approved.add(q)
            } else {
                val q = candidate.copy(
                    validationStatus = ValidationStatus.REJECTED,
                    qualityScore = result.qualityScore
                )
                rejected.add(Pair(q, result.failureReasons))
                rejectedQuestionsMap[q.id] = Pair(q, result.failureReasons)
            }
        }

        if (approved.isNotEmpty()) {
            saveApprovedToStorage()
        }

        return Pair(approved, rejected)
    }

    /**
     * Submits a player report for a question.
     * Automatically disables questions that exceed the report threshold.
     */
    fun reportQuestion(report: QuestionReport) {
        questionReports.add(report)
        localRepo?.saveQuestionReports(questionReports)

        // Count reports for this question
        val count = questionReports.count { it.questionId == report.questionId }
        if (count >= 2) {
            disableQuestion(report.questionId)
        }
    }

    /**
     * Disables a question, removing it immediately from all gameplay pools.
     */
    fun disableQuestion(questionId: String) {
        disabledQuestionIds.add(questionId)
        approvedQuestionsMap.remove(questionId)
        localRepo?.saveDisabledQuestionIds(disabledQuestionIds)
    }

    fun isQuestionDisabled(questionId: String): Boolean {
        return disabledQuestionIds.contains(questionId)
    }

    /**
     * Gets all currently active approved questions.
     */
    fun getApprovedQuestions(
        category: GameCategory? = null,
        difficulty: Difficulty? = null
    ): List<Question> {
        return approvedQuestionsMap.values
            .filter { !disabledQuestionIds.contains(it.id) }
            .filter { category == null || it.categoryId == category.id }
            .filter { difficulty == null || it.difficulty == difficulty }
    }

    fun getAllApprovedPool(): List<Question> {
        return approvedQuestionsMap.values.filter { !disabledQuestionIds.contains(it.id) }
    }

    fun getReportedQuestions(): List<QuestionReport> = questionReports.toList()

    fun getRejectedQuestions(): List<Pair<Question, List<String>>> = rejectedQuestionsMap.values.toList()

    private fun saveApprovedToStorage() {
        // Save dynamically generated approved questions (excluding original static bank to save storage)
        val generatedOnly = approvedQuestionsMap.values.filter { it.source == QuestionSource.AI_GENERATED }
        localRepo?.saveApprovedQuestions(generatedOnly)
    }
}
