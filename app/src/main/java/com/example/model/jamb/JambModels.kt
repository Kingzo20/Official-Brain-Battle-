package com.example.model.jamb

enum class JambSubject(
    val id: String,
    val title: String,
    val shortName: String,
    val iconEmoji: String,
    val description: String,
    val questionCountInFullExam: Int = 40
) {
    ENGLISH(
        id = "english",
        title = "Use of English",
        shortName = "ENG",
        iconEmoji = "📖",
        description = "Grammar, comprehension passages, registers, idioms, antonyms & synonyms",
        questionCountInFullExam = 60
    ),
    MATHEMATICS(
        id = "mathematics",
        title = "Mathematics",
        shortName = "MTH",
        iconEmoji = "📐",
        description = "Algebra, trigonometry, surds, matrices, statistics, calculus & coordinate geometry",
        questionCountInFullExam = 40
    ),
    PHYSICS(
        id = "physics",
        title = "Physics",
        shortName = "PHY",
        iconEmoji = "⚡",
        description = "Mechanics, thermal physics, waves, optics, electromagnetism & modern physics",
        questionCountInFullExam = 40
    ),
    CHEMISTRY(
        id = "chemistry",
        title = "Chemistry",
        shortName = "CHM",
        iconEmoji = "🧪",
        description = "Gas laws, stoichiometry, periodic trends, redox, organic chemistry & solutions",
        questionCountInFullExam = 40
    ),
    BIOLOGY(
        id = "biology",
        title = "Biology",
        shortName = "BIO",
        iconEmoji = "🧬",
        description = "Cell structure, physiology, heredity, genetics, ecology & evolutionary adaptation",
        questionCountInFullExam = 40
    ),
    ECONOMICS(
        id = "economics",
        title = "Economics",
        shortName = "ECO",
        iconEmoji = "📈",
        description = "Demand & supply, elasticity, national income, fiscal/monetary policies & international trade",
        questionCountInFullExam = 40
    ),
    GOVERNMENT(
        id = "government",
        title = "Government",
        shortName = "GOV",
        iconEmoji = "🏛️",
        description = "Political ideologies, Nigerian constitutional history, federalism & international organizations",
        questionCountInFullExam = 40
    ),
    LITERATURE(
        id = "literature",
        title = "Literature-in-English",
        shortName = "LIT",
        iconEmoji = "🎭",
        description = "Literary devices, dramatic irony, poetry analysis, African & non-African drama and prose",
        questionCountInFullExam = 40
    );

    companion object {
        val ALL_SUBJECTS = listOf(
            ENGLISH, MATHEMATICS, PHYSICS, CHEMISTRY,
            BIOLOGY, ECONOMICS, GOVERNMENT, LITERATURE
        )

        fun fromId(id: String): JambSubject {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: ENGLISH
        }
    }
}

enum class JambExamType(
    val id: String,
    val title: String,
    val questionCount: Int,
    val durationMinutes: Int,
    val description: String,
    val iconEmoji: String
) {
    QUICK_DRILL(
        id = "quick_drill",
        title = "Quick Drill (15 Questions)",
        questionCount = 15,
        durationMinutes = 15,
        description = "15 rapid questions in 15 minutes. Ideal for quick topic testing and daily drills.",
        iconEmoji = "⚡"
    ),
    STANDARD_TEST(
        id = "standard_test",
        title = "Standard Test (40 Questions)",
        questionCount = 40,
        durationMinutes = 40,
        description = "Standard 40-question UTME paper in 40 minutes with comprehensive topic coverage.",
        iconEmoji = "📝"
    ),
    FULL_SIMULATION(
        id = "full_simulation",
        title = "Full CBT Simulation",
        questionCount = 60,
        durationMinutes = 60,
        description = "Full examination simulation under strict official UTME CBT clock conditions.",
        iconEmoji = "🎯"
    );

    val durationSeconds: Int get() = durationMinutes * 60
}

data class JambQuestion(
    val id: String,
    val subject: JambSubject,
    val year: String = "UTME Past Question",
    val questionNumber: Int = 1,
    val passage: String? = null,
    val questionText: String,
    val optionA: String,
    val optionB: String,
    val optionC: String,
    val optionD: String,
    val correctOption: String, // "A", "B", "C", or "D"
    val explanation: String,
    val topic: String = "General"
) {
    fun getOptionText(optionKey: String): String {
        return when (optionKey.uppercase()) {
            "A" -> optionA
            "B" -> optionB
            "C" -> optionC
            "D" -> optionD
            else -> ""
        }
    }

    val correctOptionText: String get() = getOptionText(correctOption)
}

data class JambExamResult(
    val resultId: String,
    val subject: JambSubject,
    val examType: JambExamType,
    val totalQuestions: Int,
    val attemptedCount: Int,
    val correctCount: Int,
    val incorrectCount: Int,
    val unansweredCount: Int,
    val percentageScore: Float,
    val scaledScore: Int, // e.g. scaled out of 100 for this paper, or 400 equivalent
    val totalTimeSpentSeconds: Int,
    val averageTimePerQuestionSeconds: Float,
    val questions: List<JambQuestion>,
    val userAnswers: Map<Int, String>, // question index -> option chosen ("A", "B", "C", "D")
    val flaggedQuestionIndices: Set<Int> = emptySet(),
    val completedAtTimestamp: Long = System.currentTimeMillis()
) {
    val performanceTier: String get() = when {
        percentageScore >= 80f -> "EXCELLENT • ADMISSION READY"
        percentageScore >= 65f -> "VERY GOOD • HIGH CHANCE"
        percentageScore >= 50f -> "CREDIT PASS • AVERAGE"
        else -> "NEEDS REVISION • PRACTICE MORE"
    }

    val gradeColorHex: Long get() = when {
        percentageScore >= 70f -> 0xFF00E676 // NeonGreen
        percentageScore >= 50f -> 0xFFFFB300 // NeonAmber
        else -> 0xFFFF4365 // NeonRed
    }
}
