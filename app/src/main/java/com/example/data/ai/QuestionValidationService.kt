package com.example.data.ai

import com.example.model.GameCategory
import com.example.model.Question
import com.example.model.QuestionValidationResult
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sqrt

class QuestionValidationService {

    companion object {
        const val MIN_QUALITY_SCORE = 70
        const val DUPLICATE_SIMILARITY_THRESHOLD = 0.82f
        const val JACCARD_SIMILARITY_THRESHOLD = 0.85f

        private val PROHIBITED_KEYWORDS = listOf(
            "password", "credit card", "social security", "ssn",
            "bomb", "weapon", "kill yourself", "suicide", "terrorism",
            "explicit", "porn", "nude", "casino hack", "illegal drug",
            "cocaine", "heroin", "methamphetamine"
        )
    }

    /**
     * Validates a question against all structural, mathematical, safety,
     * duplicate, and quality scoring rules.
     */
    fun validate(
        question: Question,
        existingQuestions: Collection<Question> = emptyList()
    ): QuestionValidationResult {
        val failureReasons = mutableListOf<String>()
        var qualityScore = 100

        // 1. Structural Checks
        val trimmedPrompt = question.questionText.trim()
        if (trimmedPrompt.isBlank()) {
            failureReasons.add("Question text is empty or blank")
        } else if (trimmedPrompt.length < 8) {
            failureReasons.add("Question text is too short (${trimmedPrompt.length} chars, min 8)")
        } else if (trimmedPrompt.length > 350) {
            failureReasons.add("Question text is too long (${trimmedPrompt.length} chars, max 350)")
        }

        // 2. Answer Options
        val options = question.answerOptions.map { it.trim() }
        if (options.size < 2) {
            failureReasons.add("Must have at least 2 answer options (found ${options.size})")
        } else if (options.size > 6) {
            failureReasons.add("Too many answer options (${options.size}, max 6)")
        }

        if (options.any { it.isBlank() }) {
            failureReasons.add("One or more answer options are blank")
        }

        val uniqueNormalizedOptions = options.map { it.lowercase(Locale.ROOT) }.toSet()
        if (uniqueNormalizedOptions.size != options.size) {
            failureReasons.add("Answer options contain duplicate choices")
        }

        // 3. Correct Answer
        val trimmedCorrect = question.correctAnswer.trim()
        if (trimmedCorrect.isBlank()) {
            failureReasons.add("Correct answer is empty")
        } else if (!options.contains(trimmedCorrect)) {
            failureReasons.add("Correct answer '$trimmedCorrect' is not present in answer options")
        }

        // 4. Explanation
        val trimmedExplanation = question.explanation.trim()
        if (trimmedExplanation.isBlank()) {
            failureReasons.add("Explanation is missing or blank")
        } else if (trimmedExplanation.length < 6) {
            failureReasons.add("Explanation is too short (${trimmedExplanation.length} chars, min 6)")
        }

        // 5. Category Verification
        val validCategoryIds = GameCategory.values().map { it.id }
        if (question.categoryId !in validCategoryIds) {
            failureReasons.add("Invalid categoryId: '${question.categoryId}'")
        }

        // 6. Points & Time Limits
        if (question.points !in 50..500) {
            failureReasons.add("Points (${question.points}) must be between 50 and 500")
        }
        if (question.timeLimit !in 5..60) {
            failureReasons.add("Time limit (${question.timeLimit}s) must be between 5 and 60 seconds")
        }

        // 7. Safety & Child-Safe Filter
        val fullTextForSafety = "$trimmedPrompt $trimmedExplanation ${options.joinToString(" ")}".lowercase(Locale.ROOT)
        for (badWord in PROHIBITED_KEYWORDS) {
            if (fullTextForSafety.contains(badWord)) {
                failureReasons.add("Question contains prohibited or unsafe content: '$badWord'")
                break
            }
        }

        // 8. Mathematical Verification
        val (mathPassed, mathError) = verifyMathematicalCorrectness(trimmedPrompt, trimmedCorrect, question.categoryId)
        if (!mathPassed && mathError != null) {
            failureReasons.add(mathError)
        }

        // 9. Duplicate Detection
        val (isDuplicate, dupSimilarity, dupMsg) = checkDuplicate(question, existingQuestions)
        if (isDuplicate && dupMsg != null) {
            failureReasons.add(dupMsg)
        }

        // 10. Quality Scoring
        if (trimmedPrompt.length < 15) qualityScore -= 10
        if (trimmedPrompt.length > 220) qualityScore -= 10
        if (options.size != 4) qualityScore -= 10
        if (trimmedExplanation.length < 15) qualityScore -= 15
        if (!trimmedPrompt.endsWith("?") && !trimmedPrompt.endsWith(":") && !trimmedPrompt.endsWith("=")) {
            qualityScore -= 5
        }

        // Check option length balance
        if (options.isNotEmpty()) {
            val minLen = options.minOf { it.length }
            val maxLen = options.maxOf { it.length }
            if (maxLen > 30 && minLen < 5 && (maxLen.toFloat() / maxOf(minLen, 1)) > 5f) {
                qualityScore -= 10
            }
        }

        qualityScore = qualityScore.coerceIn(0, 100)
        if (qualityScore < MIN_QUALITY_SCORE) {
            failureReasons.add("Quality score ($qualityScore) below minimum threshold ($MIN_QUALITY_SCORE)")
        }

        return QuestionValidationResult(
            isValid = failureReasons.isEmpty(),
            qualityScore = qualityScore,
            failureReasons = failureReasons,
            mathVerificationPassed = mathPassed,
            duplicateSimilarity = dupSimilarity
        )
    }

    /**
     * Deterministic mathematical verification for arithmetic, percentages, and roots.
     */
    fun verifyMathematicalCorrectness(
        prompt: String,
        statedAnswer: String,
        categoryId: String
    ): Pair<Boolean, String?> {
        // Only evaluate if question appears to be an arithmetic formula
        val additionRegex = Regex("""(?:What\s+is\s+)?(-?\d+)\s*\+\s*(-?\d+)(?:\s*=\s*\?)?""", RegexOption.IGNORE_CASE)
        val subtractionRegex = Regex("""(?:What\s+is\s+)?(-?\d+)\s*-\s*(-?\d+)(?:\s*=\s*\?)?""", RegexOption.IGNORE_CASE)
        val multiplicationRegex = Regex("""(?:What\s+is\s+)?(-?\d+)\s*[×*]\s*(-?\d+)(?:\s*=\s*\?)?""", RegexOption.IGNORE_CASE)
        val divisionRegex = Regex("""(?:What\s+is\s+)?(-?\d+)\s*[÷/]\s*(-?\d+)(?:\s*=\s*\?)?""", RegexOption.IGNORE_CASE)
        val percentageRegex = Regex("""(?:What\s+is\s+)?(\d+(?:\.\d+)?)\s*%\s+of\s+(\d+(?:\.\d+)?)(?:\s*=\s*\?)?""", RegexOption.IGNORE_CASE)
        val sqrtRegex = Regex("""(?:What\s+is\s+(?:the\s+)?)?[Ss]quare\s+root\s+of\s+(\d+)(?:\s*=\s*\?)?""", RegexOption.IGNORE_CASE)

        val statedNumber = statedAnswer.trim().toDoubleOrNull() ?: return Pair(true, null)

        val cleanPrompt = prompt.trim()

        additionRegex.find(cleanPrompt)?.let { match ->
            val a = match.groupValues[1].toDoubleOrNull()
            val b = match.groupValues[2].toDoubleOrNull()
            if (a != null && b != null) {
                val expected = a + b
                return if (abs(expected - statedNumber) < 0.001) {
                    Pair(true, null)
                } else {
                    Pair(false, "Mathematical verification failed: '$cleanPrompt' evaluates to ${expected.toIntOrNull() ?: expected}, but stated answer was '$statedAnswer'")
                }
            }
        }

        subtractionRegex.find(cleanPrompt)?.let { match ->
            val a = match.groupValues[1].toDoubleOrNull()
            val b = match.groupValues[2].toDoubleOrNull()
            if (a != null && b != null) {
                val expected = a - b
                return if (abs(expected - statedNumber) < 0.001) {
                    Pair(true, null)
                } else {
                    Pair(false, "Mathematical verification failed: '$cleanPrompt' evaluates to ${expected.toIntOrNull() ?: expected}, but stated answer was '$statedAnswer'")
                }
            }
        }

        multiplicationRegex.find(cleanPrompt)?.let { match ->
            val a = match.groupValues[1].toDoubleOrNull()
            val b = match.groupValues[2].toDoubleOrNull()
            if (a != null && b != null) {
                val expected = a * b
                return if (abs(expected - statedNumber) < 0.001) {
                    Pair(true, null)
                } else {
                    Pair(false, "Mathematical verification failed: '$cleanPrompt' evaluates to ${expected.toIntOrNull() ?: expected}, but stated answer was '$statedAnswer'")
                }
            }
        }

        divisionRegex.find(cleanPrompt)?.let { match ->
            val a = match.groupValues[1].toDoubleOrNull()
            val b = match.groupValues[2].toDoubleOrNull()
            if (a != null && b != null && b != 0.0) {
                val expected = a / b
                return if (abs(expected - statedNumber) < 0.01) {
                    Pair(true, null)
                } else {
                    Pair(false, "Mathematical verification failed: '$cleanPrompt' evaluates to ${expected.toIntOrNull() ?: expected}, but stated answer was '$statedAnswer'")
                }
            }
        }

        percentageRegex.find(cleanPrompt)?.let { match ->
            val pct = match.groupValues[1].toDoubleOrNull()
            val base = match.groupValues[2].toDoubleOrNull()
            if (pct != null && base != null) {
                val expected = (pct * base) / 100.0
                return if (abs(expected - statedNumber) < 0.01) {
                    Pair(true, null)
                } else {
                    Pair(false, "Mathematical verification failed: '$cleanPrompt' evaluates to ${expected.toIntOrNull() ?: expected}, but stated answer was '$statedAnswer'")
                }
            }
        }

        sqrtRegex.find(cleanPrompt)?.let { match ->
            val num = match.groupValues[1].toDoubleOrNull()
            if (num != null && num >= 0) {
                val expected = sqrt(num)
                return if (abs(expected - statedNumber) < 0.01) {
                    Pair(true, null)
                } else {
                    Pair(false, "Mathematical verification failed: '$cleanPrompt' evaluates to ${expected.toIntOrNull() ?: expected}, but stated answer was '$statedAnswer'")
                }
            }
        }

        // Not a simple matched math formula: treat as passed
        return Pair(true, null)
    }

    private fun Double.toIntOrNull(): Int? {
        return if (this == this.roundToInt().toDouble()) this.roundToInt() else null
    }

    /**
     * Checks if the question is an exact or near duplicate of any existing question.
     */
    fun checkDuplicate(
        candidate: Question,
        existingQuestions: Collection<Question>
    ): Triple<Boolean, Float, String?> {
        val normCandidate = normalizeText(candidate.questionText)

        for (existing in existingQuestions) {
            if (existing.id == candidate.id) continue

            val normExisting = normalizeText(existing.questionText)

            // Exact match
            if (normCandidate == normExisting) {
                return Triple(true, 1.0f, "Duplicate question: exactly matches existing question [${existing.id}]")
            }

            // Levenshtein similarity
            val levSim = calculateStringSimilarity(normCandidate, normExisting)
            if (levSim >= DUPLICATE_SIMILARITY_THRESHOLD) {
                return Triple(
                    true,
                    levSim,
                    "Near-duplicate question: ${String.format(Locale.US, "%.1f", levSim * 100)}% similarity to [${existing.id}]"
                )
            }

            // Token Jaccard similarity
            val jaccardSim = calculateJaccardSimilarity(candidate.questionText, existing.questionText)
            if (jaccardSim >= JACCARD_SIMILARITY_THRESHOLD) {
                return Triple(
                    true,
                    jaccardSim,
                    "Near-duplicate question: high word overlap (${String.format(Locale.US, "%.1f", jaccardSim * 100)}%) with [${existing.id}]"
                )
            }
        }

        return Triple(false, 0f, null)
    }

    fun normalizeText(input: String): String {
        return input.lowercase(Locale.ROOT)
            .replace(Regex("""[^a-z0-9\s]"""), "")
            .replace(Regex("""\s+"""), " ")
            .trim()
    }

    fun calculateStringSimilarity(s1: String, s2: String): Float {
        if (s1 == s2) return 1.0f
        val maxLen = max(s1.length, s2.length)
        if (maxLen == 0) return 1.0f
        val distance = levenshteinDistance(s1, s2)
        return (1.0f - (distance.toFloat() / maxLen)).coerceIn(0f, 1.0f)
    }

    fun calculateJaccardSimilarity(s1: String, s2: String): Float {
        val tokens1 = s1.lowercase(Locale.ROOT).split(Regex("""\W+""")).filter { it.isNotBlank() }.toSet()
        val tokens2 = s2.lowercase(Locale.ROOT).split(Regex("""\W+""")).filter { it.isNotBlank() }.toSet()
        if (tokens1.isEmpty() && tokens2.isEmpty()) return 1.0f
        val intersectionSize = tokens1.intersect(tokens2).size
        val unionSize = tokens1.union(tokens2).size
        return if (unionSize > 0) intersectionSize.toFloat() / unionSize else 0f
    }

    private fun levenshteinDistance(lhs: CharSequence, rhs: CharSequence): Int {
        var cost = IntArray(lhs.length + 1) { it }
        var newCost = IntArray(lhs.length + 1) { 0 }

        for (i in 1..rhs.length) {
            newCost[0] = i
            for (j in 1..lhs.length) {
                val match = if (lhs[j - 1] == rhs[i - 1]) 0 else 1
                val costReplace = cost[j - 1] + match
                val costInsert = cost[j] + 1
                val costDelete = newCost[j - 1] + 1
                newCost[j] = minOf(costInsert, costDelete, costReplace)
            }
            val swap = cost
            cost = newCost
            newCost = swap
        }
        return cost[lhs.length]
    }
}
