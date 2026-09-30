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
    ),
    CRK_IRS(
        id = "crk_irs",
        title = "Christian / Islamic Studies",
        shortName = "CRS",
        iconEmoji = "📜",
        description = "Biblical & Quranic moral teachings, leadership, covenant history & spiritual ethics",
        questionCountInFullExam = 40
    );

    companion object {
        val ALL_SUBJECTS = listOf(
            ENGLISH, MATHEMATICS, PHYSICS, CHEMISTRY,
            BIOLOGY, ECONOMICS, GOVERNMENT, LITERATURE, CRK_IRS
        )

        val ELECTIVE_SUBJECTS = listOf(
            MATHEMATICS, PHYSICS, CHEMISTRY,
            BIOLOGY, ECONOMICS, GOVERNMENT, LITERATURE, CRK_IRS
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
    ),
    FULL_4_SUBJECT_MOCK(
        id = "full_4_subject_mock",
        title = "Full 4-Subject Mock (180 Questions)",
        questionCount = 180,
        durationMinutes = 120,
        description = "Full 4-subject UTME simulation: English (60 Qs) + 3 Electives (40 Qs each) in 120 minutes with multi-subject switcher.",
        iconEmoji = "🏆"
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
    val topic: String = "General",
    val stepByStepExplanation: String? = null,
    val formulaOrRuleUsed: String? = null
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

data class UniversityDepartmentCutOff(
    val courseName: String,
    val faculty: String,
    val minimumScore: Int,
    val competitiveScore: Int,
    val tier: String,
    val recommendation: String,
    val iconEmoji: String
)

object JambBenchmarkData {
    val OFFICIAL_CUTOFFS = listOf(
        UniversityDepartmentCutOff(
            courseName = "Medicine & Surgery / Dentistry",
            faculty = "Health Sciences",
            minimumScore = 270,
            competitiveScore = 295,
            tier = "Tier 1: Ultra Competitive",
            recommendation = "Target 75%+ in English, Biology, Chemistry & Physics. Practice high-speed calculation drills.",
            iconEmoji = "🩺"
        ),
        UniversityDepartmentCutOff(
            courseName = "Pharmacy / Nursing Science",
            faculty = "Medical Sciences",
            minimumScore = 250,
            competitiveScore = 275,
            tier = "Tier 1: Highly Competitive",
            recommendation = "Maintain consistent scores above 65 in Chemistry and Biology.",
            iconEmoji = "💊"
        ),
        UniversityDepartmentCutOff(
            courseName = "Law / Jurisprudence",
            faculty = "Law",
            minimumScore = 250,
            competitiveScore = 280,
            tier = "Tier 1: Highly Competitive",
            recommendation = "Focus heavily on Use of English comprehension and Literature/Government accuracy.",
            iconEmoji = "⚖️"
        ),
        UniversityDepartmentCutOff(
            courseName = "Computer Science / AI / Software Eng.",
            faculty = "Computing & Info Tech",
            minimumScore = 240,
            competitiveScore = 270,
            tier = "Tier 1: Rapidly Rising",
            recommendation = "Strengthen Mathematics and Physics problem solving to comfortably clear 70+ in each.",
            iconEmoji = "💻"
        ),
        UniversityDepartmentCutOff(
            courseName = "Electrical / Mechanical / Civil Engineering",
            faculty = "Engineering",
            minimumScore = 230,
            competitiveScore = 260,
            tier = "Tier 2: Competitive",
            recommendation = "Master kinematic, circuit, and calculus formulas for fast recall under timed conditions.",
            iconEmoji = "⚙️"
        ),
        UniversityDepartmentCutOff(
            courseName = "Accounting / Banking & Finance",
            faculty = "Management Sciences",
            minimumScore = 220,
            competitiveScore = 250,
            tier = "Tier 2: Competitive",
            recommendation = "Ensure strong performance in Mathematics, Economics, and English registers.",
            iconEmoji = "📊"
        ),
        UniversityDepartmentCutOff(
            courseName = "Economics / Mass Communication",
            faculty = "Social Sciences",
            minimumScore = 210,
            competitiveScore = 240,
            tier = "Tier 3: Moderate",
            recommendation = "Review macro/micro economic graphs and grammar concord rules.",
            iconEmoji = "📈"
        ),
        UniversityDepartmentCutOff(
            courseName = "Sciences (Microbiology / Biochemistry)",
            faculty = "Natural Sciences",
            minimumScore = 200,
            competitiveScore = 230,
            tier = "Tier 3: Moderate",
            recommendation = "Focus on genetics, cell biology, and periodic table trends.",
            iconEmoji = "🔬"
        ),
        UniversityDepartmentCutOff(
            courseName = "Arts, Humanities & Education",
            faculty = "Arts / Education",
            minimumScore = 180,
            competitiveScore = 210,
            tier = "Tier 4: Standard Entry",
            recommendation = "A solid 50-60 in English plus your core subjects guarantees admission in top Federal institutions.",
            iconEmoji = "📚"
        )
    )
}

data class JambMultiSubjectExamResult(
    val resultId: String,
    val subjectResults: Map<JambSubject, JambExamResult>,
    val totalTimeSpentSeconds: Int,
    val completedAtTimestamp: Long = System.currentTimeMillis()
) {
    val totalQuestions: Int = subjectResults.values.sumOf { it.totalQuestions }
    val totalAttempted: Int = subjectResults.values.sumOf { it.attemptedCount }
    val totalCorrect: Int = subjectResults.values.sumOf { it.correctCount }
    val totalIncorrect: Int = subjectResults.values.sumOf { it.incorrectCount }
    val totalUnanswered: Int = subjectResults.values.sumOf { it.unansweredCount }

    // Scaled score out of 400 (sum of each subject paper scaled out of 100)
    val aggregateScoreOutOf400: Int = subjectResults.values.sumOf { it.scaledScore }
    val overallPercentageScore: Float = if (totalQuestions > 0) (totalCorrect.toFloat() / totalQuestions.toFloat()) * 100f else 0f

    val performanceTier: String get() = when {
        aggregateScoreOutOf400 >= 300 -> "EXCELLENT • NATIONAL MERIT ELIGIBLE"
        aggregateScoreOutOf400 >= 260 -> "VERY HIGH • TIER 1 ADMISSION READY"
        aggregateScoreOutOf400 >= 220 -> "GOOD • GENERAL ADMISSION SECURED"
        aggregateScoreOutOf400 >= 180 -> "PASS • TARGETED REVISION NEEDED"
        else -> "BELOW CUTOFF • INTENSIVE PRACTICE REQUIRED"
    }

    val eligibleDepartments: List<UniversityDepartmentCutOff>
        get() = JambBenchmarkData.OFFICIAL_CUTOFFS.filter { aggregateScoreOutOf400 >= it.minimumScore }

    val aspirationalDepartments: List<UniversityDepartmentCutOff>
        get() = JambBenchmarkData.OFFICIAL_CUTOFFS.filter { aggregateScoreOutOf400 < it.minimumScore }
}
