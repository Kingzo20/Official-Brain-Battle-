package com.example.data.jamb

import android.content.Context
import com.example.data.QuestionHistoryTracker
import com.example.model.jamb.JambExamResult
import com.example.model.jamb.JambExamType
import com.example.model.jamb.JambQuestion
import com.example.model.jamb.JambSubject
import java.util.UUID

class JambQuestionRepository(
    private val context: Context? = null,
    private val questionHistoryTracker: QuestionHistoryTracker? = null
) {
    private val tracker = questionHistoryTracker ?: context?.let { QuestionHistoryTracker(it) }

    fun getQuestions(
        subject: JambSubject,
        examType: JambExamType,
        uid: String = ""
    ): List<JambQuestion> {
        val count = examType.questionCount
        val fullSubjectPool = getPoolForSubject(subject)

        val subjectKey = "jamb_${subject.id}"
        val answeredIds = tracker?.getAnsweredQuestionIds(uid = uid, categoryId = subjectKey) ?: emptySet()

        // Filter out previously answered questions
        var unseen = fullSubjectPool.filterNot { it.id in answeredIds }

        // If remaining pool is smaller than requested exam size, recycle history
        if (unseen.size < count) {
            tracker?.resetCategoryHistory(categoryId = subjectKey, uid = uid)
            unseen = fullSubjectPool
        }

        val selected = unseen.shuffled()
        val result = mutableListOf<JambQuestion>()
        var cycle = 0
        while (result.size < count && fullSubjectPool.isNotEmpty()) {
            val poolToTakeFrom = if (cycle == 0) selected else fullSubjectPool.shuffled()
            for (q in poolToTakeFrom) {
                if (result.size >= count) break
                val uniqueId = if (cycle == 0) q.id else "${q.id}_c${cycle}_${result.size}"
                result.add(q.copy(id = uniqueId, questionNumber = result.size + 1))
            }
            cycle++
        }
        return result.mapIndexed { idx, q -> q.copy(questionNumber = idx + 1) }
    }

    fun recordExamQuestionsAnswered(
        questions: List<JambQuestion>,
        subject: JambSubject,
        uid: String = ""
    ) {
        val subjectKey = "jamb_${subject.id}"
        tracker?.markQuestionsAnswered(
            questionIds = questions.map { it.id.substringBefore("_c") },
            categoryId = subjectKey,
            uid = uid
        )
    }

    fun calculateResult(
        subject: JambSubject,
        examType: JambExamType,
        questions: List<JambQuestion>,
        userAnswers: Map<Int, String>,
        timeSpentSeconds: Int,
        flaggedIndices: Set<Int> = emptySet()
    ): JambExamResult {
        var correctCount = 0
        var incorrectCount = 0

        questions.forEachIndexed { index, question ->
            val chosen = userAnswers[index]
            if (chosen != null) {
                if (chosen.equals(question.correctOption, ignoreCase = true)) {
                    correctCount++
                } else {
                    incorrectCount++
                }
            }
        }

        val totalQuestions = questions.size
        val attemptedCount = userAnswers.size
        val unansweredCount = (totalQuestions - attemptedCount).coerceAtLeast(0)
        val percentageScore = if (totalQuestions > 0) (correctCount.toFloat() / totalQuestions.toFloat()) * 100f else 0f

        // Scaled to 100 for single paper
        val scaledScore = ((correctCount.toFloat() / totalQuestions.coerceAtLeast(1).toFloat()) * 100).toInt()

        val avgTime = if (totalQuestions > 0) timeSpentSeconds.toFloat() / totalQuestions.toFloat() else 0f

        return JambExamResult(
            resultId = "jamb_res_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}",
            subject = subject,
            examType = examType,
            totalQuestions = totalQuestions,
            attemptedCount = attemptedCount,
            correctCount = correctCount,
            incorrectCount = incorrectCount,
            unansweredCount = unansweredCount,
            percentageScore = percentageScore,
            scaledScore = scaledScore,
            totalTimeSpentSeconds = timeSpentSeconds,
            averageTimePerQuestionSeconds = avgTime,
            questions = questions,
            userAnswers = userAnswers,
            flaggedQuestionIndices = flaggedIndices
        )
    }

    fun getPoolForSubject(subject: JambSubject): List<JambQuestion> {
        return when (subject) {
            JambSubject.ENGLISH -> JambPastQuestionsBank.englishQuestions
            JambSubject.MATHEMATICS -> JambPastQuestionsBank.mathematicsQuestions
            JambSubject.PHYSICS -> JambPastQuestionsBank.physicsQuestions
            JambSubject.CHEMISTRY -> JambPastQuestionsBank.chemistryQuestions
            JambSubject.BIOLOGY -> JambPastQuestionsBank.biologyQuestions
            JambSubject.ECONOMICS -> JambPastQuestionsBank.economicsQuestions
            JambSubject.GOVERNMENT -> JambPastQuestionsBank.governmentQuestions
            JambSubject.LITERATURE -> JambPastQuestionsBank.literatureQuestions
        }
    }
}
