package com.example.data.ai

import com.example.BuildConfig
import com.example.data.DedicatedCategoryQuestionPools
import com.example.model.Difficulty
import com.example.model.GameCategory
import com.example.model.Question
import com.example.model.QuestionSource
import com.example.model.ValidationStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.random.Random

interface AiQuestionService {
    suspend fun generateBatch(category: GameCategory, difficulty: Difficulty, count: Int = 5): List<Question>
    suspend fun generateExplanation(question: Question, selectedAnswer: String): String
}

class AiQuestionServiceImpl(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()
) : AiQuestionService {

    companion object {
        private const val MODEL_NAME = "gemini-3.5-flash"
        private const val GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent"
    }

    override suspend fun generateBatch(
        category: GameCategory,
        difficulty: Difficulty,
        count: Int
    ): List<Question> = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Throwable) {
            ""
        }

        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val cloudQuestions = callGeminiGenerateQuestions(apiKey, category, difficulty, count)
                if (cloudQuestions.isNotEmpty()) {
                    return@withContext cloudQuestions
                }
            } catch (e: Exception) {
                // Fall through to offline procedural generator
            }
        }

        // Offline / Fallback Procedural AI Generation
        generateProceduralBatch(category, difficulty, count)
    }

    override suspend fun generateExplanation(
        question: Question,
        selectedAnswer: String
    ): String = withContext(Dispatchers.IO) {
        val isCorrect = selectedAnswer.trim().equals(question.correctAnswer.trim(), ignoreCase = true)
        if (isCorrect) {
            return@withContext "${question.explanation}\n\n💡 Key Insight: You accurately identified the correct rule: '${question.correctAnswer}'."
        }

        return@withContext "${question.explanation}\n\n⚠️ Note on '$selectedAnswer': While '$selectedAnswer' might seem plausible, the precise answer is '${question.correctAnswer}'."
    }

    private fun callGeminiGenerateQuestions(
        apiKey: String,
        category: GameCategory,
        difficulty: Difficulty,
        count: Int
    ): List<Question> {
        val prompt = """
            You are an educational quiz engine for a mobile brain puzzle game called Brain Battle.
            Generate $count unique, clear, high-quality multiple choice questions.
            Category: ${category.title} (ID: ${category.id})
            Difficulty: ${difficulty.title}
            
            Strict rules:
            1. Return a JSON array of objects.
            2. Each object MUST have:
               - "questionText": concise and engaging text ending with '?'
               - "answerOptions": an array of exactly 4 distinct strings
               - "correctAnswer": string that exactly matches one of the answerOptions
               - "explanation": educational explanation explaining why the answer is correct
               - "points": integer between 100 and 300
               - "timeLimit": integer between 10 and 20
               - "tags": array of 1-3 strings
            3. Do not include markdown code blocks (such as ```json). Output ONLY the raw JSON array.
        """.trimIndent()

        val jsonBody = JSONObject().apply {
            val contents = JSONArray().apply {
                val contentObj = JSONObject().apply {
                    val parts = JSONArray().apply {
                        put(JSONObject().apply { put("text", prompt) })
                    }
                    put("parts", parts)
                }
                put(contentObj)
            }
            put("contents", contents)
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.7)
                put("responseMimeType", "application/json")
            })
        }

        val request = Request.Builder()
            .url("$GEMINI_API_URL?key=$apiKey")
            .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) return emptyList()

        val responseString = response.body?.string() ?: return emptyList()
        val jsonRoot = JSONObject(responseString)
        val candidates = jsonRoot.optJSONArray("candidates") ?: return emptyList()
        if (candidates.length() == 0) return emptyList()

        val content = candidates.getJSONObject(0).optJSONObject("content") ?: return emptyList()
        val parts = content.optJSONArray("parts") ?: return emptyList()
        if (parts.length() == 0) return emptyList()

        val text = parts.getJSONObject(0).optString("text", "").trim()
        val cleanJson = text.removePrefix("```json").removePrefix("```").removeSuffix("```").trim()

        val questionsArray = JSONArray(cleanJson)
        val resultList = mutableListOf<Question>()

        for (i in 0 until questionsArray.length()) {
            val obj = questionsArray.getJSONObject(i)
            val questionText = obj.getString("questionText")
            val optionsJson = obj.getJSONArray("answerOptions")
            val options = mutableListOf<String>()
            for (j in 0 until optionsJson.length()) {
                options.add(optionsJson.getString(j))
            }
            val correctAnswer = obj.getString("correctAnswer")
            val explanation = obj.optString("explanation", "The correct answer is $correctAnswer.")
            val points = obj.optInt("points", difficulty.basePoints)
            val timeLimit = obj.optInt("timeLimit", 15)

            resultList.add(
                Question(
                    id = "ai_${System.currentTimeMillis()}_$i",
                    categoryId = category.id,
                    difficulty = difficulty,
                    questionText = questionText,
                    answerOptions = options,
                    correctAnswer = correctAnswer,
                    explanation = explanation,
                    points = points,
                    timeLimit = timeLimit,
                    source = QuestionSource.AI_GENERATED,
                    validationStatus = ValidationStatus.PENDING,
                    qualityScore = 90
                )
            )
        }

        return resultList
    }

    /**
     * Intelligent procedural question generator for seamless offline support.
     * Guarantees mathematically correct and educational questions every time.
     */
    fun generateProceduralBatch(
        category: GameCategory,
        difficulty: Difficulty,
        count: Int
    ): List<Question> {
        val list = mutableListOf<Question>()
        for (i in 0 until count) {
            val q = when (category) {
                GameCategory.MATH -> generateMathQuestion(difficulty)
                GameCategory.NUMBERS -> generateNumbersQuestion(difficulty)
                GameCategory.LOGIC -> generateLogicQuestion(difficulty)
                GameCategory.WORDS -> generateWordsQuestion(difficulty)
                GameCategory.SCIENCE -> generateScienceQuestion(difficulty)
                GameCategory.KNOWLEDGE -> generateKnowledgeQuestion(difficulty)
                GameCategory.PATTERNS -> generatePatternsQuestion(difficulty)
                GameCategory.SPEED -> generateSpeedQuestion(difficulty)
                GameCategory.DAILY -> generateMathQuestion(difficulty)
                GameCategory.GEOGRAPHY -> DedicatedCategoryQuestionPools.geographyQuestions
                    .filter { it.difficulty == difficulty }
                    .ifEmpty { DedicatedCategoryQuestionPools.geographyQuestions }
                    .random()
                GameCategory.MEMORY -> DedicatedCategoryQuestionPools.memoryQuestions
                    .filter { it.difficulty == difficulty }
                    .ifEmpty { DedicatedCategoryQuestionPools.memoryQuestions }
                    .random()
                GameCategory.RIDDLES -> DedicatedCategoryQuestionPools.riddleQuestions
                    .filter { it.difficulty == difficulty }
                    .ifEmpty { DedicatedCategoryQuestionPools.riddleQuestions }
                    .random()
                GameCategory.TECHNOLOGY -> DedicatedCategoryQuestionPools.technologyQuestions
                    .filter { it.difficulty == difficulty }
                    .ifEmpty { DedicatedCategoryQuestionPools.technologyQuestions }
                    .random()
            }
            list.add(q)
        }
        return list
    }

    private fun generateMathQuestion(difficulty: Difficulty): Question {
        val random = Random.Default
        val id = "ai_math_${System.currentTimeMillis()}_${random.nextInt(1000, 9999)}"

        return when (difficulty) {
            Difficulty.EASY -> {
                val a = random.nextInt(12, 49)
                val b = random.nextInt(11, 49)
                val correct = a + b
                val options = listOf(
                    correct.toString(),
                    (correct + 2).toString(),
                    (correct - 2).toString(),
                    (correct + 10).toString()
                ).shuffled()

                Question(
                    id = id,
                    categoryId = GameCategory.MATH.id,
                    difficulty = difficulty,
                    questionText = "What is $a + $b?",
                    answerOptions = options,
                    correctAnswer = correct.toString(),
                    explanation = "$a + $b = $correct.",
                    points = 100,
                    timeLimit = 15,
                    source = QuestionSource.AI_GENERATED,
                    validationStatus = ValidationStatus.PENDING
                )
            }
            Difficulty.MEDIUM -> {
                val a = random.nextInt(12, 28)
                val b = random.nextInt(4, 9)
                val correct = a * b
                val options = listOf(
                    correct.toString(),
                    (correct + b).toString(),
                    (correct - b).toString(),
                    (correct + 12).toString()
                ).shuffled()

                Question(
                    id = id,
                    categoryId = GameCategory.MATH.id,
                    difficulty = difficulty,
                    questionText = "What is $a × $b?",
                    answerOptions = options,
                    correctAnswer = correct.toString(),
                    explanation = "$a multiplied by $b equals $correct.",
                    points = 150,
                    timeLimit = 14,
                    source = QuestionSource.AI_GENERATED,
                    validationStatus = ValidationStatus.PENDING
                )
            }
            Difficulty.HARD, Difficulty.EXTREME -> {
                // Algebra: solve for x: ax + b = c
                val x = random.nextInt(4, 15)
                val a = random.nextInt(2, 6)
                val b = random.nextInt(5, 25)
                val c = (a * x) + b

                val options = listOf(
                    x.toString(),
                    (x + 2).toString(),
                    (x - 1).toString(),
                    (x + 4).toString()
                ).shuffled()

                Question(
                    id = id,
                    categoryId = GameCategory.MATH.id,
                    difficulty = difficulty,
                    questionText = "Solve for x: ${a}x + $b = $c?",
                    answerOptions = options,
                    correctAnswer = x.toString(),
                    explanation = "Subtract $b from both sides: ${a}x = ${c - b}. Divide by $a: x = $x.",
                    points = 250,
                    timeLimit = 12,
                    source = QuestionSource.AI_GENERATED,
                    validationStatus = ValidationStatus.PENDING
                )
            }
        }
    }

    private fun generateNumbersQuestion(difficulty: Difficulty): Question {
        val random = Random.Default
        val id = "ai_num_${System.currentTimeMillis()}_${random.nextInt(1000, 9999)}"

        val start = random.nextInt(3, 12)
        val step = random.nextInt(3, 7)
        val s1 = start
        val s2 = start + step
        val s3 = s2 + step
        val s4 = s3 + step
        val correct = s4 + step

        val options = listOf(
            correct.toString(),
            (correct + 2).toString(),
            (correct - step).toString(),
            (correct + step).toString()
        ).shuffled()

        return Question(
            id = id,
            categoryId = GameCategory.NUMBERS.id,
            difficulty = difficulty,
            questionText = "What comes next in the sequence: $s1, $s2, $s3, $s4, ...?",
            answerOptions = options,
            correctAnswer = correct.toString(),
            explanation = "Each successive term increases by +$step. $s4 + $step = $correct.",
            points = 150,
            timeLimit = 15,
            source = QuestionSource.AI_GENERATED,
            validationStatus = ValidationStatus.PENDING
        )
    }

    private fun generateLogicQuestion(difficulty: Difficulty): Question {
        val id = "ai_log_${System.currentTimeMillis()}_${Random.nextInt(1000, 9999)}"
        val logicPuzzles = listOf(
            Triple(
                "If all Bloomers are Florals, and all Florals are Plants, what must be true?",
                listOf("All Bloomers are Plants", "All Plants are Bloomers", "No Bloomers are Plants", "Some Plants are not Florals"),
                "All Bloomers are Plants"
            ),
            Triple(
                "A doctor gives you 3 pills and tells you to take one every 30 minutes. How long will the pills last?",
                listOf("60 minutes", "90 minutes", "30 minutes", "120 minutes"),
                "60 minutes"
            ),
            Triple(
                "If five machines take 5 minutes to make 5 widgets, how many minutes would 100 machines take to make 100 widgets?",
                listOf("5 minutes", "100 minutes", "20 minutes", "1 minute"),
                "5 minutes"
            ),
            Triple(
                "Mary's father has 5 daughters: Nana, Nene, Nini, Nono. What is the name of the fifth daughter?",
                listOf("Mary", "Nunu", "Nina", "Nora"),
                "Mary"
            )
        )
        val chosen = logicPuzzles.random()
        return Question(
            id = id,
            categoryId = GameCategory.LOGIC.id,
            difficulty = difficulty,
            questionText = chosen.first,
            answerOptions = chosen.second.shuffled(),
            correctAnswer = chosen.third,
            explanation = "By logical deduction: '${chosen.third}' is the required solution.",
            points = 180,
            timeLimit = 15,
            source = QuestionSource.AI_GENERATED,
            validationStatus = ValidationStatus.PENDING
        )
    }

    private fun generateWordsQuestion(difficulty: Difficulty): Question {
        val id = "ai_wrd_${System.currentTimeMillis()}_${Random.nextInt(1000, 9999)}"
        val wordPuzzles = listOf(
            Triple(
                "Which word is an exact antonym of 'EPHEMERAL'?",
                listOf("Permanent", "Fleeting", "Brief", "Transient"),
                "Permanent"
            ),
            Triple(
                "Which word is a synonym for 'LUCID'?",
                listOf("Clear", "Murky", "Confused", "Dark"),
                "Clear"
            ),
            Triple(
                "Which of the following is an anagram of 'LISTEN'?",
                listOf("SILENT", "TINSEL", "ENLIST", "ALL OF THESE"),
                "ALL OF THESE"
            ),
            Triple(
                "What is the antonym of 'BENEVOLENT'?",
                listOf("Malevolent", "Generous", "Kind", "Altruistic"),
                "Malevolent"
            )
        )
        val chosen = wordPuzzles.random()
        return Question(
            id = id,
            categoryId = GameCategory.WORDS.id,
            difficulty = difficulty,
            questionText = chosen.first,
            answerOptions = chosen.second.shuffled(),
            correctAnswer = chosen.third,
            explanation = "'${chosen.third}' matches the exact lexical definition.",
            points = 140,
            timeLimit = 15,
            source = QuestionSource.AI_GENERATED,
            validationStatus = ValidationStatus.PENDING
        )
    }

    private fun generateScienceQuestion(difficulty: Difficulty): Question {
        val id = "ai_sci_${System.currentTimeMillis()}_${Random.nextInt(1000, 9999)}"
        val sciencePuzzles = listOf(
            Triple(
                "What is the chemical symbol for Gold?",
                listOf("Au", "Ag", "Fe", "Cu"),
                "Au"
            ),
            Triple(
                "Which planet is known as the Red Planet?",
                listOf("Mars", "Venus", "Jupiter", "Saturn"),
                "Mars"
            ),
            Triple(
                "What is the primary gas found in the Earth's atmosphere?",
                listOf("Nitrogen", "Oxygen", "Carbon Dioxide", "Argon"),
                "Nitrogen"
            ),
            Triple(
                "What organelle is commonly known as the powerhouse of the cell?",
                listOf("Mitochondria", "Nucleus", "Ribosome", "Chloroplast"),
                "Mitochondria"
            )
        )
        val chosen = sciencePuzzles.random()
        return Question(
            id = id,
            categoryId = GameCategory.SCIENCE.id,
            difficulty = difficulty,
            questionText = chosen.first,
            answerOptions = chosen.second.shuffled(),
            correctAnswer = chosen.third,
            explanation = "Scientific fact: '${chosen.third}' is the accurate scientific designation.",
            points = 180,
            timeLimit = 14,
            source = QuestionSource.AI_GENERATED,
            validationStatus = ValidationStatus.PENDING
        )
    }

    private fun generateKnowledgeQuestion(difficulty: Difficulty): Question {
        val id = "ai_kno_${System.currentTimeMillis()}_${Random.nextInt(1000, 9999)}"
        val facts = listOf(
            Triple(
                "What is the capital city of Australia?",
                listOf("Canberra", "Sydney", "Melbourne", "Brisbane"),
                "Canberra"
            ),
            Triple(
                "Which is the longest river in the world?",
                listOf("Nile", "Amazon", "Yangtze", "Mississippi"),
                "Nile"
            ),
            Triple(
                "In which year did the Apollo 11 moon landing take place?",
                listOf("1969", "1965", "1972", "1959"),
                "1969"
            ),
            Triple(
                "Who painted the Mona Lisa?",
                listOf("Leonardo da Vinci", "Michelangelo", "Vincent van Gogh", "Pablo Picasso"),
                "Leonardo da Vinci"
            )
        )
        val chosen = facts.random()
        return Question(
            id = id,
            categoryId = GameCategory.KNOWLEDGE.id,
            difficulty = difficulty,
            questionText = chosen.first,
            answerOptions = chosen.second.shuffled(),
            correctAnswer = chosen.third,
            explanation = "General knowledge: '${chosen.third}' is historically verified.",
            points = 150,
            timeLimit = 14,
            source = QuestionSource.AI_GENERATED,
            validationStatus = ValidationStatus.PENDING
        )
    }

    private fun generatePatternsQuestion(difficulty: Difficulty): Question {
        val id = "ai_pat_${System.currentTimeMillis()}_${Random.nextInt(1000, 9999)}"
        val patterns = listOf(
            Triple(
                "Complete the symbol pattern: △, □, ⬠, ⬡, ...?",
                listOf("Heptagon (7 sides)", "Octagon (8 sides)", "Triangle (3 sides)", "Circle"),
                "Heptagon (7 sides)"
            ),
            Triple(
                "If North rotates 90° clockwise, it faces East. What does South-West face after 180° rotation?",
                listOf("North-East", "North-West", "South-East", "North"),
                "North-East"
            ),
            Triple(
                "Look at the letter sequence: A, C, F, J, ... What letter comes next?",
                listOf("O", "N", "P", "M"),
                "O"
            )
        )
        val chosen = patterns.random()
        return Question(
            id = id,
            categoryId = GameCategory.PATTERNS.id,
            difficulty = difficulty,
            questionText = chosen.first,
            answerOptions = chosen.second.shuffled(),
            correctAnswer = chosen.third,
            explanation = "Pattern analysis: '${chosen.third}' continues the systematic increment.",
            points = 175,
            timeLimit = 15,
            source = QuestionSource.AI_GENERATED,
            validationStatus = ValidationStatus.PENDING
        )
    }

    private fun generateSpeedQuestion(difficulty: Difficulty): Question {
        val random = Random.Default
        val id = "ai_spd_${System.currentTimeMillis()}_${random.nextInt(1000, 9999)}"
        val a = random.nextInt(15, 60)
        val b = random.nextInt(15, 60)
        val correct = a + b
        val options = listOf(
            correct.toString(),
            (correct + 10).toString(),
            (correct - 10).toString(),
            (correct + 2).toString()
        ).shuffled()

        return Question(
            id = id,
            categoryId = GameCategory.SPEED.id,
            difficulty = difficulty,
            questionText = "RAPID: $a + $b = ?",
            answerOptions = options,
            correctAnswer = correct.toString(),
            explanation = "$a + $b = $correct.",
            points = 200,
            timeLimit = 7,
            source = QuestionSource.AI_GENERATED,
            validationStatus = ValidationStatus.PENDING
        )
    }
}
