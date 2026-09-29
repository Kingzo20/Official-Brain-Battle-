package com.example.data

import com.example.model.Difficulty
import com.example.model.Question

object QuestionBank {

    val mathQuestions: List<Question> = listOf(
        Question(
            id = "math_01",
            categoryId = "math",
            difficulty = Difficulty.EASY,
            questionText = "What is 15 + 28?",
            answerOptions = listOf("41", "43", "45", "42"),
            correctAnswer = "43",
            explanation = "15 + 28 = 43.",
            points = 100,
            timeLimit = 15
        ),
        Question(
            id = "math_02",
            categoryId = "math",
            difficulty = Difficulty.EASY,
            questionText = "What is 8 × 9?",
            answerOptions = listOf("64", "72", "81", "74"),
            correctAnswer = "72",
            explanation = "8 multiplied by 9 equals 72.",
            points = 100,
            timeLimit = 12
        ),
        Question(
            id = "math_03",
            categoryId = "math",
            difficulty = Difficulty.EASY,
            questionText = "What is 144 ÷ 12?",
            answerOptions = listOf("11", "12", "13", "14"),
            correctAnswer = "12",
            explanation = "12 squared is 144, so 144 divided by 12 is 12.",
            points = 100,
            timeLimit = 12
        ),
        Question(
            id = "math_04",
            categoryId = "math",
            difficulty = Difficulty.EASY,
            questionText = "Which number is an odd number?",
            answerOptions = listOf("24", "38", "47", "56"),
            correctAnswer = "47",
            explanation = "47 cannot be divided by 2 without a remainder, making it an odd number.",
            points = 100,
            timeLimit = 10
        ),
        Question(
            id = "math_05",
            categoryId = "math",
            difficulty = Difficulty.EASY,
            questionText = "What is 25% of 80?",
            answerOptions = listOf("15", "20", "25", "30"),
            correctAnswer = "20",
            explanation = "25% is one quarter. 80 ÷ 4 = 20.",
            points = 100,
            timeLimit = 15
        ),
        Question(
            id = "math_06",
            categoryId = "math",
            difficulty = Difficulty.MEDIUM,
            questionText = "What is 17 × 4?",
            answerOptions = listOf("58", "64", "68", "72"),
            correctAnswer = "68",
            explanation = "10 × 4 = 40, and 7 × 4 = 28. 40 + 28 = 68.",
            points = 150,
            timeLimit = 15
        ),
        Question(
            id = "math_07",
            categoryId = "math",
            difficulty = Difficulty.MEDIUM,
            questionText = "Solve for x: 3x - 7 = 14",
            answerOptions = listOf("5", "7", "8", "6"),
            correctAnswer = "7",
            explanation = "3x = 14 + 7 = 21, so x = 21 ÷ 3 = 7.",
            points = 150,
            timeLimit = 15
        ),
        Question(
            id = "math_08",
            categoryId = "math",
            difficulty = Difficulty.MEDIUM,
            questionText = "What is the square root of 225?",
            answerOptions = listOf("13", "14", "15", "16"),
            correctAnswer = "15",
            explanation = "15 × 15 = 225.",
            points = 150,
            timeLimit = 12
        ),
        Question(
            id = "math_09",
            categoryId = "math",
            difficulty = Difficulty.MEDIUM,
            questionText = "What is 15% of 300?",
            answerOptions = listOf("35", "40", "45", "50"),
            correctAnswer = "45",
            explanation = "10% of 300 is 30, and 5% is 15. 30 + 15 = 45.",
            points = 150,
            timeLimit = 15
        ),
        Question(
            id = "math_10",
            categoryId = "math",
            difficulty = Difficulty.MEDIUM,
            questionText = "If a pizza is cut into 8 equal slices and you eat 3, what percentage remains?",
            answerOptions = listOf("50%", "62.5%", "37.5%", "75%"),
            correctAnswer = "62.5%",
            explanation = "5 slices out of 8 remain: 5 ÷ 8 = 0.625 or 62.5%.",
            points = 150,
            timeLimit = 15
        ),
        Question(
            id = "math_11",
            categoryId = "math",
            difficulty = Difficulty.MEDIUM,
            questionText = "What is the perimeter of a rectangle with length 14 and width 6?",
            answerOptions = listOf("40", "44", "84", "38"),
            correctAnswer = "40",
            explanation = "Perimeter = 2 × (length + width) = 2 × (14 + 6) = 2 × 20 = 40.",
            points = 150,
            timeLimit = 15
        ),
        Question(
            id = "math_12",
            categoryId = "math",
            difficulty = Difficulty.HARD,
            questionText = "What is 13 × 17?",
            answerOptions = listOf("211", "221", "231", "217"),
            correctAnswer = "221",
            explanation = "13 × 17 = 13 × (20 - 3) = 260 - 39 = 221.",
            points = 200,
            timeLimit = 18
        ),
        Question(
            id = "math_13",
            categoryId = "math",
            difficulty = Difficulty.HARD,
            questionText = "Solve: 4x + 12 = 2x + 28. What is x?",
            answerOptions = listOf("6", "8", "10", "4"),
            correctAnswer = "8",
            explanation = "4x - 2x = 28 - 12 => 2x = 16 => x = 8.",
            points = 200,
            timeLimit = 15
        ),
        Question(
            id = "math_14",
            categoryId = "math",
            difficulty = Difficulty.HARD,
            questionText = "What is 2 to the power of 8?",
            answerOptions = listOf("128", "256", "512", "64"),
            correctAnswer = "256",
            explanation = "2^8 = 256.",
            points = 200,
            timeLimit = 12
        ),
        Question(
            id = "math_15",
            categoryId = "math",
            difficulty = Difficulty.HARD,
            questionText = "What is the cube root of 512?",
            answerOptions = listOf("6", "7", "8", "9"),
            correctAnswer = "8",
            explanation = "8 × 8 × 8 = 64 × 8 = 512.",
            points = 200,
            timeLimit = 15
        ),
        Question(
            id = "math_16",
            categoryId = "math",
            difficulty = Difficulty.HARD,
            questionText = "A coat priced at $120 is on sale for 30% off. What is the final price?",
            answerOptions = listOf("$78", "$84", "$90", "$96"),
            correctAnswer = "$84",
            explanation = "30% of $120 is $36. $120 - $36 = $84.",
            points = 200,
            timeLimit = 18
        ),
        Question(
            id = "math_17",
            categoryId = "math",
            difficulty = Difficulty.EXTREME,
            questionText = "If log10(x) = 3, what is the value of x?",
            answerOptions = listOf("30", "300", "1000", "100"),
            correctAnswer = "1000",
            explanation = "10^3 = 1000, so x = 1000.",
            points = 300,
            timeLimit = 15
        ),
        Question(
            id = "math_18",
            categoryId = "math",
            difficulty = Difficulty.EXTREME,
            questionText = "What is the sum of angles inside a hexagon?",
            answerOptions = listOf("540°", "720°", "900°", "360°"),
            correctAnswer = "720°",
            explanation = "(n - 2) × 180° = (6 - 2) × 180° = 4 × 180° = 720°.",
            points = 300,
            timeLimit = 18
        ),
        Question(
            id = "math_19",
            categoryId = "math",
            difficulty = Difficulty.EXTREME,
            questionText = "What is 15% of 60% of 500?",
            answerOptions = listOf("35", "45", "50", "60"),
            correctAnswer = "45",
            explanation = "60% of 500 = 300. 15% of 300 = 45.",
            points = 300,
            timeLimit = 20
        ),
        Question(
            id = "math_20",
            categoryId = "math",
            difficulty = Difficulty.EXTREME,
            questionText = "If 3^(x-1) = 81, what is x?",
            answerOptions = listOf("4", "5", "6", "3"),
            correctAnswer = "5",
            explanation = "81 = 3^4, so x - 1 = 4, meaning x = 5.",
            points = 300,
            timeLimit = 15
        )
    )

    val numbersQuestions: List<Question> = listOf(
        Question(
            id = "num_01",
            categoryId = "numbers",
            difficulty = Difficulty.EASY,
            questionText = "Complete the sequence: 5, 10, 15, 20, ___",
            answerOptions = listOf("22", "25", "30", "28"),
            correctAnswer = "25",
            explanation = "The sequence increases by 5 each step: 20 + 5 = 25.",
            points = 100,
            timeLimit = 10
        ),
        Question(
            id = "num_02",
            categoryId = "numbers",
            difficulty = Difficulty.EASY,
            questionText = "What number completes the pattern: 2, 4, 8, 16, ___?",
            answerOptions = listOf("24", "28", "32", "36"),
            correctAnswer = "32",
            explanation = "Each number doubles: 16 × 2 = 32.",
            points = 100,
            timeLimit = 10
        ),
        Question(
            id = "num_03",
            categoryId = "numbers",
            difficulty = Difficulty.EASY,
            questionText = "Find the missing number: 100, 90, 80, 70, ___",
            answerOptions = listOf("65", "60", "55", "50"),
            correctAnswer = "60",
            explanation = "Each step decreases by 10: 70 - 10 = 60.",
            points = 100,
            timeLimit = 10
        ),
        Question(
            id = "num_04",
            categoryId = "numbers",
            difficulty = Difficulty.EASY,
            questionText = "Which number is a multiple of both 3 and 4?",
            answerOptions = listOf("15", "18", "24", "28"),
            correctAnswer = "24",
            explanation = "24 = 3 × 8 and 24 = 4 × 6.",
            points = 100,
            timeLimit = 12
        ),
        Question(
            id = "num_05",
            categoryId = "numbers",
            difficulty = Difficulty.EASY,
            questionText = "Complete the pattern: 1, 4, 9, 16, ___",
            answerOptions = listOf("20", "24", "25", "36"),
            correctAnswer = "25",
            explanation = "These are squares: 1^2, 2^2, 3^2, 4^2, 5^2 = 25.",
            points = 100,
            timeLimit = 12
        ),
        Question(
            id = "num_06",
            categoryId = "numbers",
            difficulty = Difficulty.MEDIUM,
            questionText = "Find the missing number: 3, 6, 11, 18, 27, ___",
            answerOptions = listOf("36", "38", "37", "40"),
            correctAnswer = "38",
            explanation = "The differences increase by 2: +3, +5, +7, +9, so +11: 27 + 11 = 38.",
            points = 150,
            timeLimit = 15
        ),
        Question(
            id = "num_07",
            categoryId = "numbers",
            difficulty = Difficulty.MEDIUM,
            questionText = "Which number does NOT belong: 11, 13, 17, 19, 21, 23?",
            answerOptions = listOf("13", "19", "21", "23"),
            correctAnswer = "21",
            explanation = "21 is composite (3 × 7); all other numbers in the set are prime.",
            points = 150,
            timeLimit = 15
        ),
        Question(
            id = "num_08",
            categoryId = "numbers",
            difficulty = Difficulty.MEDIUM,
            questionText = "Complete the sequence: 2, 6, 18, 54, ___",
            answerOptions = listOf("108", "162", "144", "172"),
            correctAnswer = "162",
            explanation = "Each term is multiplied by 3: 54 × 3 = 162.",
            points = 150,
            timeLimit = 15
        ),
        Question(
            id = "num_09",
            categoryId = "numbers",
            difficulty = Difficulty.MEDIUM,
            questionText = "What is the missing number: 4, 9, 19, 39, ___?",
            answerOptions = listOf("69", "79", "89", "78"),
            correctAnswer = "79",
            explanation = "The rule is (n × 2) + 1: (39 × 2) + 1 = 78 + 1 = 79.",
            points = 150,
            timeLimit = 15
        ),
        Question(
            id = "num_10",
            categoryId = "numbers",
            difficulty = Difficulty.MEDIUM,
            questionText = "Sequence: 80, 40, 20, 10, ___",
            answerOptions = listOf("0", "2", "5", "8"),
            correctAnswer = "5",
            explanation = "Each step is divided by 2: 10 ÷ 2 = 5.",
            points = 150,
            timeLimit = 10
        ),
        Question(
            id = "num_11",
            categoryId = "numbers",
            difficulty = Difficulty.MEDIUM,
            questionText = "Find the missing number in the pair: (3, 9), (4, 16), (5, 25), (6, ___)",
            answerOptions = listOf("30", "32", "36", "40"),
            correctAnswer = "36",
            explanation = "The second number is the square of the first: 6^2 = 36.",
            points = 150,
            timeLimit = 12
        ),
        Question(
            id = "num_12",
            categoryId = "numbers",
            difficulty = Difficulty.HARD,
            questionText = "Fibonacci progression: 1, 1, 2, 3, 5, 8, 13, 21, ___",
            answerOptions = listOf("32", "34", "36", "38"),
            correctAnswer = "34",
            explanation = "Each number is the sum of the two preceding numbers: 13 + 21 = 34.",
            points = 200,
            timeLimit = 15
        ),
        Question(
            id = "num_13",
            categoryId = "numbers",
            difficulty = Difficulty.HARD,
            questionText = "Complete the sequence: 7, 14, 28, 56, 112, ___",
            answerOptions = listOf("214", "224", "232", "248"),
            correctAnswer = "224",
            explanation = "Multiply by 2 each step: 112 × 2 = 224.",
            points = 200,
            timeLimit = 12
        ),
        Question(
            id = "num_14",
            categoryId = "numbers",
            difficulty = Difficulty.HARD,
            questionText = "What comes next: 1, 8, 27, 64, 125, ___?",
            answerOptions = listOf("196", "216", "225", "256"),
            correctAnswer = "216",
            explanation = "These are cubes: 1^3, 2^3, 3^3, 4^3, 5^3, 6^3 = 216.",
            points = 200,
            timeLimit = 15
        ),
        Question(
            id = "num_15",
            categoryId = "numbers",
            difficulty = Difficulty.HARD,
            questionText = "Find the missing number: 2, 3, 7, 16, 32, ___",
            answerOptions = listOf("48", "53", "57", "64"),
            correctAnswer = "57",
            explanation = "The differences are square numbers: +1 (+1^2), +4 (+2^2), +9 (+3^2), +16 (+4^2), next is +25 (+5^2): 32 + 25 = 57.",
            points = 200,
            timeLimit = 18
        ),
        Question(
            id = "num_16",
            categoryId = "numbers",
            difficulty = Difficulty.HARD,
            questionText = "Sequence: 10, 12, 16, 24, 40, ___",
            answerOptions = listOf("64", "72", "80", "56"),
            correctAnswer = "72",
            explanation = "Differences double each step: +2, +4, +8, +16, so next is +32: 40 + 32 = 72.",
            points = 200,
            timeLimit = 15
        ),
        Question(
            id = "num_17",
            categoryId = "numbers",
            difficulty = Difficulty.EXTREME,
            questionText = "Sequence: 2, 6, 12, 20, 30, 42, ___",
            answerOptions = listOf("52", "54", "56", "58"),
            correctAnswer = "56",
            explanation = "Each term is n × (n + 1): 1×2, 2×3, 3×4, 4×5, 5×6, 6×7, next is 7×8 = 56.",
            points = 300,
            timeLimit = 18
        ),
        Question(
            id = "num_18",
            categoryId = "numbers",
            difficulty = Difficulty.EXTREME,
            questionText = "Missing number: 3, 5, 9, 17, 33, ___",
            answerOptions = listOf("49", "65", "67", "55"),
            correctAnswer = "65",
            explanation = "Differences are powers of 2: +2, +4, +8, +16, next is +32: 33 + 32 = 65.",
            points = 300,
            timeLimit = 15
        ),
        Question(
            id = "num_19",
            categoryId = "numbers",
            difficulty = Difficulty.EXTREME,
            questionText = "Sequence: 1, 2, 6, 24, 120, ___",
            answerOptions = listOf("240", "480", "720", "600"),
            correctAnswer = "720",
            explanation = "Factorials: 1!, 2!, 3!, 4!, 5!, 6! = 720.",
            points = 300,
            timeLimit = 15
        ),
        Question(
            id = "num_20",
            categoryId = "numbers",
            difficulty = Difficulty.EXTREME,
            questionText = "Sequence: 31, 28, 31, 30, 31, 30, 31, 31, ___",
            answerOptions = listOf("28", "30", "31", "29"),
            correctAnswer = "30",
            explanation = "Days in each month of the year from January onwards: Jan(31), Feb(28), Mar(31), Apr(30), May(31), Jun(30), Jul(31), Aug(31), Sep is 30.",
            points = 300,
            timeLimit = 20
        )
    )

    val logicQuestions: List<Question> = listOf(
        Question(
            id = "logic_01",
            categoryId = "logic",
            difficulty = Difficulty.EASY,
            questionText = "All dogs are animals. A beagle is a dog. Therefore:",
            answerOptions = listOf("A beagle is an animal", "All animals are beagles", "A beagle is a cat", "Some dogs are not animals"),
            correctAnswer = "A beagle is an animal",
            explanation = "By deductive reasoning, if all dogs are animals and a beagle is a dog, a beagle must be an animal.",
            points = 100,
            timeLimit = 12
        ),
        Question(
            id = "logic_02",
            categoryId = "logic",
            difficulty = Difficulty.EASY,
            questionText = "If you overtake the person in second place in a race, what position are you in?",
            answerOptions = listOf("1st place", "2nd place", "3rd place", "Tied for 1st"),
            correctAnswer = "2nd place",
            explanation = "You took the spot of the runner who was in second, so you are now in second place.",
            points = 100,
            timeLimit = 10
        ),
        Question(
            id = "logic_03",
            categoryId = "logic",
            difficulty = Difficulty.EASY,
            questionText = "Mary's father has 5 daughters: Nana, Nene, Nini, Nono, and who is the 5th?",
            answerOptions = listOf("Nunu", "Mary", "Nina", "Nora"),
            correctAnswer = "Mary",
            explanation = "The riddle states at the beginning: 'Mary's father has 5 daughters', so the 5th daughter is Mary.",
            points = 100,
            timeLimit = 10
        ),
        Question(
            id = "logic_04",
            categoryId = "logic",
            difficulty = Difficulty.EASY,
            questionText = "Some pens are blue. All blue items are colored. Therefore:",
            answerOptions = listOf("Some pens are colored", "All pens are colored", "No pens are colored", "All items are pens"),
            correctAnswer = "Some pens are colored",
            explanation = "The pens that are blue must also be colored.",
            points = 100,
            timeLimit = 12
        ),
        Question(
            id = "logic_05",
            categoryId = "logic",
            difficulty = Difficulty.EASY,
            questionText = "A farmer has 17 sheep and all but 9 die. How many sheep are left alive?",
            answerOptions = listOf("8", "9", "0", "17"),
            correctAnswer = "9",
            explanation = "'All but 9 die' means exactly 9 survived.",
            points = 100,
            timeLimit = 10
        ),
        Question(
            id = "logic_06",
            categoryId = "logic",
            difficulty = Difficulty.MEDIUM,
            questionText = "If today is Wednesday, what day of the week will it be in 100 days?",
            answerOptions = listOf("Thursday", "Friday", "Saturday", "Sunday"),
            correctAnswer = "Friday",
            explanation = "100 ÷ 7 = 14 with a remainder of 2 days. Wednesday + 2 days = Friday.",
            points = 150,
            timeLimit = 18
        ),
        Question(
            id = "logic_07",
            categoryId = "logic",
            difficulty = Difficulty.MEDIUM,
            questionText = "Look at this statement: 'No reptiles have fur.' Which conclusion is valid?",
            answerOptions = listOf("If an animal has fur, it is not a reptile", "All non-reptiles have fur", "Reptiles have scales", "Snakes have fur"),
            correctAnswer = "If an animal has fur, it is not a reptile",
            explanation = "This is the contrapositive: if no reptiles have fur, any furry animal cannot be a reptile.",
            points = 150,
            timeLimit = 15
        ),
        Question(
            id = "logic_08",
            categoryId = "logic",
            difficulty = Difficulty.MEDIUM,
            questionText = "A bat and ball cost $1.10 together. The bat costs $1.00 more than the ball. How much is the ball?",
            answerOptions = listOf("$0.10", "$0.05", "$0.15", "$0.01"),
            correctAnswer = "$0.05",
            explanation = "If ball = $0.05, then bat = $1.05. Together: $1.05 + $0.05 = $1.10.",
            points = 150,
            timeLimit = 15
        ),
        Question(
            id = "logic_09",
            categoryId = "logic",
            difficulty = Difficulty.MEDIUM,
            questionText = "If it takes 5 machines 5 minutes to make 5 widgets, how long would it take 100 machines to make 100 widgets?",
            answerOptions = listOf("100 minutes", "5 minutes", "20 minutes", "1 minute"),
            correctAnswer = "5 minutes",
            explanation = "Each individual machine takes 5 minutes to make 1 widget. Thus, 100 machines will produce 100 widgets in 5 minutes.",
            points = 150,
            timeLimit = 15
        ),
        Question(
            id = "logic_10",
            categoryId = "logic",
            difficulty = Difficulty.MEDIUM,
            questionText = "Tom is older than Jerry. Spike is older than Tom. Jerry is older than Tyke. Who is the second oldest?",
            answerOptions = listOf("Spike", "Tom", "Jerry", "Tyke"),
            correctAnswer = "Tom",
            explanation = "In order from oldest to youngest: Spike > Tom > Jerry > Tyke. Tom is 2nd oldest.",
            points = 150,
            timeLimit = 15
        ),
        Question(
            id = "logic_11",
            categoryId = "logic",
            difficulty = Difficulty.MEDIUM,
            questionText = "A clock shows 3:15. How many degrees is the angle between the hour hand and minute hand?",
            answerOptions = listOf("0°", "7.5°", "15°", "30°"),
            correctAnswer = "7.5°",
            explanation = "The minute hand is at 15 minutes (90°). In 15 minutes, the hour hand has moved 15 × 0.5° = 7.5° past the 3.",
            points = 150,
            timeLimit = 18
        ),
        Question(
            id = "logic_12",
            categoryId = "logic",
            difficulty = Difficulty.HARD,
            questionText = "Three boxes are labeled Apples, Oranges, and Mixed. ALL labels are wrong. You pick ONE fruit from Mixed and get an Apple. What is in the box labeled Apples?",
            answerOptions = listOf("Apples", "Oranges", "Mixed", "Cannot determine"),
            correctAnswer = "Oranges",
            explanation = "The box labeled Mixed must be all Apples. Since all labels are wrong, the box labeled Apples cannot be Apples, and cannot be Mixed (which we found), so it must be Oranges.",
            points = 200,
            timeLimit = 22
        ),
        Question(
            id = "logic_13",
            categoryId = "logic",
            difficulty = Difficulty.HARD,
            questionText = "If yesterday was Tuesday's tomorrow, what day is tomorrow?",
            answerOptions = listOf("Thursday", "Friday", "Wednesday", "Saturday"),
            correctAnswer = "Friday",
            explanation = "Tuesday's tomorrow is Wednesday. If yesterday was Wednesday, today is Thursday, so tomorrow is Friday.",
            points = 200,
            timeLimit = 18
        ),
        Question(
            id = "logic_14",
            categoryId = "logic",
            difficulty = Difficulty.HARD,
            questionText = "In a tournament, every player plays every other player once. If there are 6 players, how many matches are played?",
            answerOptions = listOf("12", "15", "30", "36"),
            correctAnswer = "15",
            explanation = "Combinations: (6 × 5) ÷ 2 = 15 matches.",
            points = 200,
            timeLimit = 18
        ),
        Question(
            id = "logic_15",
            categoryId = "logic",
            difficulty = Difficulty.HARD,
            questionText = "You have 8 balls that look identical. One is slightly heavier. Using a balance scale, what is the minimum weighings needed to guarantee finding the heavy ball?",
            answerOptions = listOf("1", "2", "3", "4"),
            correctAnswer = "2",
            explanation = "Weigh 3 against 3 (leaving 2). If balanced, weigh the remaining 2. If unbalanced, weigh 1 against 1 of the heavy group.",
            points = 200,
            timeLimit = 20
        ),
        Question(
            id = "logic_16",
            categoryId = "logic",
            difficulty = Difficulty.HARD,
            questionText = "Which statement is true if: 'Only brave people are astronauts, and John is an astronaut'?",
            answerOptions = listOf("John is brave", "All brave people are astronauts", "John might not be brave", "No brave people are astronauts"),
            correctAnswer = "John is brave",
            explanation = "Since being brave is a necessary condition for being an astronaut, John must be brave.",
            points = 200,
            timeLimit = 15
        ),
        Question(
            id = "logic_17",
            categoryId = "logic",
            difficulty = Difficulty.EXTREME,
            questionText = "A knight always tells the truth, and a knave always lies. Person A says: 'Both of us are knaves.' What are they?",
            answerOptions = listOf("A is a knave, B is a knight", "Both are knaves", "Both are knights", "A is a knight, B is a knave"),
            correctAnswer = "A is a knave, B is a knight",
            explanation = "If A were a knight, the statement would have to be true, meaning A is a knave (a contradiction). So A is a knave, making the conjunction false; since A is a knave, B must be a knight.",
            points = 300,
            timeLimit = 22
        ),
        Question(
            id = "logic_18",
            categoryId = "logic",
            difficulty = Difficulty.EXTREME,
            questionText = "Two fathers and two sons go fishing. Each catches one fish. They bring home exactly 3 fish. Why?",
            answerOptions = listOf("One fish was lost", "They are grandfather, father, and son", "One did not catch a fish", "They shared a fish"),
            correctAnswer = "They are grandfather, father, and son",
            explanation = "The group consists of 3 people: the grandfather (father to the father), the father (both a father and a son), and the son.",
            points = 300,
            timeLimit = 15
        ),
        Question(
            id = "logic_19",
            categoryId = "logic",
            difficulty = Difficulty.EXTREME,
            questionText = "If all Zips are Zaps, and no Zaps are Zops, then:",
            answerOptions = listOf("No Zips are Zops", "All Zips are Zops", "Some Zips are Zops", "All Zops are Zips"),
            correctAnswer = "No Zips are Zops",
            explanation = "Since Zips are entirely contained within Zaps, and Zaps has zero overlap with Zops, Zips cannot overlap with Zops.",
            points = 300,
            timeLimit = 18
        ),
        Question(
            id = "logic_20",
            categoryId = "logic",
            difficulty = Difficulty.EXTREME,
            questionText = "A lily pad doubles in size every day. If it covers a pond in 48 days, how many days did it take to cover half the pond?",
            answerOptions = listOf("24 days", "47 days", "12 days", "36 days"),
            correctAnswer = "47 days",
            explanation = "Since it doubles every day, the day right before day 48 (day 47) it covered exactly half of the pond.",
            points = 300,
            timeLimit = 15
        )
    )

    val wordsQuestions: List<Question> = listOf(
        Question(
            id = "words_01",
            categoryId = "words",
            difficulty = Difficulty.EASY,
            questionText = "Which word is an antonym for 'ANCIENT'?",
            answerOptions = listOf("Antique", "Modern", "Historic", "Aged"),
            correctAnswer = "Modern",
            explanation = "'Modern' refers to the present or recent times, which is opposite to ancient.",
            points = 100,
            timeLimit = 10
        ),
        Question(
            id = "words_02",
            categoryId = "words",
            difficulty = Difficulty.EASY,
            questionText = "What word is an anagram of 'LISTEN'?",
            answerOptions = listOf("SILENT", "INLETS", "TINSEL", "All of these"),
            correctAnswer = "All of these",
            explanation = "SILENT, INLETS, and TINSEL all use the exact letters L-I-S-T-E-N.",
            points = 100,
            timeLimit = 12
        ),
        Question(
            id = "words_03",
            categoryId = "words",
            difficulty = Difficulty.EASY,
            questionText = "Which word is spelled correctly?",
            answerOptions = listOf("Accommodate", "Acommodate", "Accomodate", "Acomodate"),
            correctAnswer = "Accommodate",
            explanation = "'Accommodate' has two c's and two m's.",
            points = 100,
            timeLimit = 10
        ),
        Question(
            id = "words_04",
            categoryId = "words",
            difficulty = Difficulty.EASY,
            questionText = "Choose the synonym for 'CANDID':",
            answerOptions = listOf("Frank", "Secretive", "Shy", "Deceptive"),
            correctAnswer = "Frank",
            explanation = "Candid means straightforward, truthful, or frank.",
            points = 100,
            timeLimit = 12
        ),
        Question(
            id = "words_05",
            categoryId = "words",
            difficulty = Difficulty.EASY,
            questionText = "Which word means a word formed from the first letters of other words?",
            answerOptions = listOf("Acronym", "Synonym", "Antonym", "Homophone"),
            correctAnswer = "Acronym",
            explanation = "An acronym is formed from initials, like NASA or UNESCO.",
            points = 100,
            timeLimit = 10
        ),
        Question(
            id = "words_06",
            categoryId = "words",
            difficulty = Difficulty.MEDIUM,
            questionText = "What does the word 'EPHEMERAL' mean?",
            answerOptions = listOf("Short-lived", "Eternal", "Heavy", "Fragile"),
            correctAnswer = "Short-lived",
            explanation = "Ephemeral means lasting for a very short time.",
            points = 150,
            timeLimit = 12
        ),
        Question(
            id = "words_07",
            categoryId = "words",
            difficulty = Difficulty.MEDIUM,
            questionText = "Unscramble: 'E L P P A'",
            answerOptions = listOf("APPLE", "PALE", "APEL", "LEAP"),
            correctAnswer = "APPLE",
            explanation = "The letters E-L-P-P-A spell APPLE.",
            points = 150,
            timeLimit = 10
        ),
        Question(
            id = "words_08",
            categoryId = "words",
            difficulty = Difficulty.MEDIUM,
            questionText = "Which of the following is a palindrome?",
            answerOptions = listOf("RACECAR", "BICYCLE", "RUNNER", "VEHICLE"),
            correctAnswer = "RACECAR",
            explanation = "A palindrome reads the same backwards and forwards: R-A-C-E-C-A-R.",
            points = 150,
            timeLimit = 10
        ),
        Question(
            id = "words_09",
            categoryId = "words",
            difficulty = Difficulty.MEDIUM,
            questionText = "Identify the antonym of 'BENEVOLENT':",
            answerOptions = listOf("Malevolent", "Generous", "Kind", "Altruistic"),
            correctAnswer = "Malevolent",
            explanation = "Benevolent means wishing good; malevolent means wishing evil.",
            points = 150,
            timeLimit = 12
        ),
        Question(
            id = "words_10",
            categoryId = "words",
            difficulty = Difficulty.MEDIUM,
            questionText = "What is the prefix meaning 'against' or 'opposite'?",
            answerOptions = listOf("Anti-", "Pro-", "Sub-", "Inter-"),
            correctAnswer = "Anti-",
            explanation = "'Anti-' is a Greek prefix signifying opposed to or against.",
            points = 150,
            timeLimit = 10
        ),
        Question(
            id = "words_11",
            categoryId = "words",
            difficulty = Difficulty.MEDIUM,
            questionText = "Which word describes words that sound alike but have different meanings and spellings?",
            answerOptions = listOf("Homophones", "Synonyms", "Hypernyms", "Pseudonyms"),
            correctAnswer = "Homophones",
            explanation = "Homophones sound the same (e.g., hear/here, knight/night).",
            points = 150,
            timeLimit = 12
        ),
        Question(
            id = "words_12",
            categoryId = "words",
            difficulty = Difficulty.HARD,
            questionText = "Choose the word that means 'to make something less severe or painful':",
            answerOptions = listOf("Mitigate", "Aggravate", "Exacerbate", "Prolong"),
            correctAnswer = "Mitigate",
            explanation = "To mitigate is to lessen gravity, damage, or pain.",
            points = 200,
            timeLimit = 15
        ),
        Question(
            id = "words_13",
            categoryId = "words",
            difficulty = Difficulty.HARD,
            questionText = "What is the meaning of 'UBIQUITOUS'?",
            answerOptions = listOf("Found everywhere", "Extremely rare", "Dangerous", "Loud"),
            correctAnswer = "Found everywhere",
            explanation = "Ubiquitous means present, appearing, or found everywhere.",
            points = 200,
            timeLimit = 12
        ),
        Question(
            id = "words_14",
            categoryId = "words",
            difficulty = Difficulty.HARD,
            questionText = "Select the pair that expresses an analogous relationship: BIRD : AVIARY ::",
            answerOptions = listOf("Fish : Aquarium", "Dog : Leash", "Car : Highway", "Book : Author"),
            correctAnswer = "Fish : Aquarium",
            explanation = "A bird is kept in an aviary; a fish is kept in an aquarium.",
            points = 200,
            timeLimit = 15
        ),
        Question(
            id = "words_15",
            categoryId = "words",
            difficulty = Difficulty.HARD,
            questionText = "Which word is spelled correctly?",
            answerOptions = listOf("Mischievous", "Mischevious", "Mischievious", "Mischevous"),
            correctAnswer = "Mischievous",
            explanation = "The standard spelling is 'mischievous' (3 syllables).",
            points = 200,
            timeLimit = 12
        ),
        Question(
            id = "words_16",
            categoryId = "words",
            difficulty = Difficulty.HARD,
            questionText = "What does the word 'SURREPTITIOUS' mean?",
            answerOptions = listOf("Secret or clandestine", "Open and proud", "Fast-moving", "Repetitive"),
            correctAnswer = "Secret or clandestine",
            explanation = "Surreptitious means kept secret, especially because it would not be approved of.",
            points = 200,
            timeLimit = 15
        ),
        Question(
            id = "words_17",
            categoryId = "words",
            difficulty = Difficulty.EXTREME,
            questionText = "What is the definition of 'SESQUIPEDALIAN'?",
            answerOptions = listOf("Using very long words", "Having six feet", "A 15-year period", "Walking slowly"),
            correctAnswer = "Using very long words",
            explanation = "Sesquipedalian literally means 'a foot and a half long' and refers to lengthy words.",
            points = 300,
            timeLimit = 15
        ),
        Question(
            id = "words_18",
            categoryId = "words",
            difficulty = Difficulty.EXTREME,
            questionText = "Which word means 'appeased or pacified'?",
            answerOptions = listOf("Placated", "Vindicated", "Instigated", "Alienated"),
            correctAnswer = "Placated",
            explanation = "To placate means to make someone less angry or hostile.",
            points = 300,
            timeLimit = 15
        ),
        Question(
            id = "words_19",
            categoryId = "words",
            difficulty = Difficulty.EXTREME,
            questionText = "An oxymoron is a figure of speech that:",
            answerOptions = listOf("Combines contradictory terms", "Exaggerates for effect", "Compares two things using 'like'", "Substitutes a gentle term"),
            correctAnswer = "Combines contradictory terms",
            explanation = "An oxymoron juxtaposes contrasting concepts (e.g. 'deafening silence').",
            points = 300,
            timeLimit = 15
        ),
        Question(
            id = "words_20",
            categoryId = "words",
            difficulty = Difficulty.EXTREME,
            questionText = "What is the origin of the word 'CLICHÉ'?",
            answerOptions = listOf("A printing plate sound in French", "A Latin proverb", "A Greek mythological character", "An Italian poem"),
            correctAnswer = "A printing plate sound in French",
            explanation = "It comes from the French 'clicher', an onomatopoeia for the sound of a stereotype printing plate casting.",
            points = 300,
            timeLimit = 18
        )
    )

    val knowledgeQuestions: List<Question> = listOf(
        Question(
            id = "gk_01",
            categoryId = "knowledge",
            difficulty = Difficulty.EASY,
            questionText = "What is the capital city of France?",
            answerOptions = listOf("Berlin", "Madrid", "Paris", "Rome"),
            correctAnswer = "Paris",
            explanation = "Paris is the capital and most populous city of France.",
            points = 100,
            timeLimit = 10
        ),
        Question(
            id = "gk_02",
            categoryId = "knowledge",
            difficulty = Difficulty.EASY,
            questionText = "Which is the largest ocean on Earth?",
            answerOptions = listOf("Atlantic", "Indian", "Arctic", "Pacific"),
            correctAnswer = "Pacific",
            explanation = "The Pacific Ocean covers more than 30% of Earth's surface.",
            points = 100,
            timeLimit = 10
        ),
        Question(
            id = "gk_03",
            categoryId = "knowledge",
            difficulty = Difficulty.EASY,
            questionText = "How many continents are there on Earth?",
            answerOptions = listOf("5", "6", "7", "8"),
            correctAnswer = "7",
            explanation = "Asia, Africa, North America, South America, Antarctica, Europe, Australia.",
            points = 100,
            timeLimit = 10
        ),
        Question(
            id = "gk_04",
            categoryId = "knowledge",
            difficulty = Difficulty.EASY,
            questionText = "Who painted the Mona Lisa?",
            answerOptions = listOf("Vincent van Gogh", "Pablo Picasso", "Leonardo da Vinci", "Claude Monet"),
            correctAnswer = "Leonardo da Vinci",
            explanation = "Leonardo da Vinci painted the Mona Lisa in the early 16th century.",
            points = 100,
            timeLimit = 10
        ),
        Question(
            id = "gk_05",
            categoryId = "knowledge",
            difficulty = Difficulty.EASY,
            questionText = "What is the currency of Japan?",
            answerOptions = listOf("Yen", "Won", "Yuan", "Ringgit"),
            correctAnswer = "Yen",
            explanation = "The Japanese Yen (¥) is the official currency of Japan.",
            points = 100,
            timeLimit = 10
        ),
        Question(
            id = "gk_06",
            categoryId = "knowledge",
            difficulty = Difficulty.MEDIUM,
            questionText = "In which year did the Apollo 11 moon landing occur?",
            answerOptions = listOf("1967", "1969", "1971", "1973"),
            correctAnswer = "1969",
            explanation = "Neil Armstrong and Buzz Aldrin landed on the Moon on July 20, 1969.",
            points = 150,
            timeLimit = 12
        ),
        Question(
            id = "gk_07",
            categoryId = "knowledge",
            difficulty = Difficulty.MEDIUM,
            questionText = "Which country has the largest land area in the world?",
            answerOptions = listOf("Canada", "China", "United States", "Russia"),
            correctAnswer = "Russia",
            explanation = "Russia spans over 17 million square kilometers across two continents.",
            points = 150,
            timeLimit = 10
        ),
        Question(
            id = "gk_08",
            categoryId = "knowledge",
            difficulty = Difficulty.MEDIUM,
            questionText = "What is the longest river in the world?",
            answerOptions = listOf("Amazon", "Nile", "Yangtze", "Mississippi"),
            correctAnswer = "Nile",
            explanation = "The Nile River is traditionally recognized as the longest river at approx 6,650 km.",
            points = 150,
            timeLimit = 12
        ),
        Question(
            id = "gk_09",
            categoryId = "knowledge",
            difficulty = Difficulty.MEDIUM,
            questionText = "Which ancient civilization built Machu Picchu?",
            answerOptions = listOf("Aztec", "Maya", "Inca", "Olmec"),
            correctAnswer = "Inca",
            explanation = "Machu Picchu was built by the Inca Empire in 15th-century Peru.",
            points = 150,
            timeLimit = 12
        ),
        Question(
            id = "gk_10",
            categoryId = "knowledge",
            difficulty = Difficulty.MEDIUM,
            questionText = "What is the largest desert in the world?",
            answerOptions = listOf("Sahara", "Gobi", "Antarctic Desert", "Arabian Desert"),
            correctAnswer = "Antarctic Desert",
            explanation = "Antarctica is a polar desert and the largest desert on Earth by surface area.",
            points = 150,
            timeLimit = 12
        ),
        Question(
            id = "gk_11",
            categoryId = "knowledge",
            difficulty = Difficulty.MEDIUM,
            questionText = "How many keys are on a standard acoustic piano?",
            answerOptions = listOf("76", "84", "88", "92"),
            correctAnswer = "88",
            explanation = "A standard full piano keyboard contains 52 white keys and 36 black keys (88 total).",
            points = 150,
            timeLimit = 10
        ),
        Question(
            id = "gk_12",
            categoryId = "knowledge",
            difficulty = Difficulty.HARD,
            questionText = "Which country has the longest coastline in the world?",
            answerOptions = listOf("Russia", "Australia", "Canada", "Norway"),
            correctAnswer = "Canada",
            explanation = "Canada's coastline is over 202,080 km long, far exceeding any other nation.",
            points = 200,
            timeLimit = 15
        ),
        Question(
            id = "gk_13",
            categoryId = "knowledge",
            difficulty = Difficulty.HARD,
            questionText = "In what year did the Berlin Wall fall?",
            answerOptions = listOf("1987", "1989", "1991", "1993"),
            correctAnswer = "1989",
            explanation = "The Berlin Wall fell on November 9, 1989.",
            points = 200,
            timeLimit = 12
        ),
        Question(
            id = "gk_14",
            categoryId = "knowledge",
            difficulty = Difficulty.HARD,
            questionText = "Which country is home to the ancient city of Petra?",
            answerOptions = listOf("Jordan", "Egypt", "Lebanon", "Syria"),
            correctAnswer = "Jordan",
            explanation = "Petra is a historic rock-carved city in southern Jordan.",
            points = 200,
            timeLimit = 12
        ),
        Question(
            id = "gk_15",
            categoryId = "knowledge",
            difficulty = Difficulty.HARD,
            questionText = "What is the highest mountain peak in North America?",
            answerOptions = listOf("Mount Whitney", "Denali (Mount McKinley)", "Mount Logan", "Mount Rainier"),
            correctAnswer = "Denali (Mount McKinley)",
            explanation = "Denali in Alaska rises to 20,310 feet (6,190 m) above sea level.",
            points = 200,
            timeLimit = 12
        ),
        Question(
            id = "gk_16",
            categoryId = "knowledge",
            difficulty = Difficulty.HARD,
            questionText = "Who was the first female Prime Minister of the United Kingdom?",
            answerOptions = listOf("Theresa May", "Margaret Thatcher", "Queen Elizabeth II", "Indira Gandhi"),
            correctAnswer = "Margaret Thatcher",
            explanation = "Margaret Thatcher served as British Prime Minister from 1979 to 1990.",
            points = 200,
            timeLimit = 12
        ),
        Question(
            id = "gk_17",
            categoryId = "knowledge",
            difficulty = Difficulty.EXTREME,
            questionText = "Which African nation was never colonized by a European power?",
            answerOptions = listOf("Ethiopia", "Kenya", "Nigeria", "Ghana"),
            correctAnswer = "Ethiopia",
            explanation = "Ethiopia successfully defended its sovereignty at the Battle of Adwa in 1896.",
            points = 300,
            timeLimit = 15
        ),
        Question(
            id = "gk_18",
            categoryId = "knowledge",
            difficulty = Difficulty.EXTREME,
            questionText = "What is the rarest blood type in the human ABO/Rh system?",
            answerOptions = listOf("AB Negative", "O Negative", "B Negative", "A Negative"),
            correctAnswer = "AB Negative",
            explanation = "AB Negative is found in less than 1% of the global population.",
            points = 300,
            timeLimit = 15
        ),
        Question(
            id = "gk_19",
            categoryId = "knowledge",
            difficulty = Difficulty.EXTREME,
            questionText = "Which treaty officially ended the Thirty Years' War in 1648?",
            answerOptions = listOf("Peace of Westphalia", "Treaty of Utrecht", "Treaty of Versailles", "Treaty of Tordesillas"),
            correctAnswer = "Peace of Westphalia",
            explanation = "The Peace of Westphalia (1648) established principles of state sovereignty in Europe.",
            points = 300,
            timeLimit = 15
        ),
        Question(
            id = "gk_20",
            categoryId = "knowledge",
            difficulty = Difficulty.EXTREME,
            questionText = "What is the deepest known point in Earth's oceans?",
            answerOptions = listOf("Challenger Deep", "Puerto Rico Trench", "Java Trench", "Milwaukee Deep"),
            correctAnswer = "Challenger Deep",
            explanation = "Challenger Deep in the Mariana Trench reaches approximately 10,928 meters depth.",
            points = 300,
            timeLimit = 15
        )
    )

    val scienceQuestions: List<Question> = listOf(
        Question(
            id = "sci_01",
            categoryId = "science",
            difficulty = Difficulty.EASY,
            questionText = "What is the chemical symbol for Water?",
            answerOptions = listOf("H2O", "CO2", "NaCl", "O2"),
            correctAnswer = "H2O",
            explanation = "Water consists of two hydrogen atoms bonded to one oxygen atom.",
            points = 100,
            timeLimit = 10
        ),
        Question(
            id = "sci_02",
            categoryId = "science",
            difficulty = Difficulty.EASY,
            questionText = "Which planet is closest to the Sun?",
            answerOptions = listOf("Venus", "Mercury", "Mars", "Earth"),
            correctAnswer = "Mercury",
            explanation = "Mercury orbits closest to the Sun at an average of 58 million km.",
            points = 100,
            timeLimit = 10
        ),
        Question(
            id = "sci_03",
            categoryId = "science",
            difficulty = Difficulty.EASY,
            questionText = "What process do plants use to convert sunlight into food?",
            answerOptions = listOf("Photosynthesis", "Respiration", "Fermentation", "Digestion"),
            correctAnswer = "Photosynthesis",
            explanation = "Photosynthesis converts carbon dioxide and water into glucose using sunlight.",
            points = 100,
            timeLimit = 10
        ),
        Question(
            id = "sci_04",
            categoryId = "science",
            difficulty = Difficulty.EASY,
            questionText = "What force pulls objects toward the center of the Earth?",
            answerOptions = listOf("Magnetism", "Friction", "Gravity", "Inertia"),
            correctAnswer = "Gravity",
            explanation = "Gravitational attraction pulls mass toward the Earth's center.",
            points = 100,
            timeLimit = 10
        ),
        Question(
            id = "sci_05",
            categoryId = "science",
            difficulty = Difficulty.EASY,
            questionText = "What gas do humans breathe out during exhalation?",
            answerOptions = listOf("Carbon Dioxide", "Nitrogen", "Methane", "Hydrogen"),
            correctAnswer = "Carbon Dioxide",
            explanation = "Cellular respiration produces carbon dioxide (CO2) which is exhaled.",
            points = 100,
            timeLimit = 10
        ),
        Question(
            id = "sci_06",
            categoryId = "science",
            difficulty = Difficulty.MEDIUM,
            questionText = "What is the chemical symbol for Gold?",
            answerOptions = listOf("Ag", "Au", "Fe", "Cu"),
            correctAnswer = "Au",
            explanation = "'Au' comes from the Latin word 'Aurum', meaning shining dawn.",
            points = 150,
            timeLimit = 10
        ),
        Question(
            id = "sci_07",
            categoryId = "science",
            difficulty = Difficulty.MEDIUM,
            questionText = "Which organelle is called the 'powerhouse of the cell'?",
            answerOptions = listOf("Nucleus", "Ribosome", "Mitochondria", "Golgi apparatus"),
            correctAnswer = "Mitochondria",
            explanation = "Mitochondria generate most of the cell's ATP chemical energy.",
            points = 150,
            timeLimit = 10
        ),
        Question(
            id = "sci_08",
            categoryId = "science",
            difficulty = Difficulty.MEDIUM,
            questionText = "What is the most abundant gas in Earth's atmosphere?",
            answerOptions = listOf("Oxygen", "Nitrogen", "Carbon Dioxide", "Argon"),
            correctAnswer = "Nitrogen",
            explanation = "Nitrogen makes up approximately 78% of Earth's atmosphere.",
            points = 150,
            timeLimit = 12
        ),
        Question(
            id = "sci_09",
            categoryId = "science",
            difficulty = Difficulty.MEDIUM,
            questionText = "At what Celsius temperature does water freeze at standard pressure?",
            answerOptions = listOf("0°C", "32°C", "-10°C", "100°C"),
            correctAnswer = "0°C",
            explanation = "Water transitions to ice at 0°C (32°F).",
            points = 150,
            timeLimit = 10
        ),
        Question(
            id = "sci_10",
            categoryId = "science",
            difficulty = Difficulty.MEDIUM,
            questionText = "What particle in an atom carries a negative electric charge?",
            answerOptions = listOf("Proton", "Neutron", "Electron", "Photon"),
            correctAnswer = "Electron",
            explanation = "Electrons orbit the nucleus and carry a -1 fundamental charge.",
            points = 150,
            timeLimit = 10
        ),
        Question(
            id = "sci_11",
            categoryId = "science",
            difficulty = Difficulty.MEDIUM,
            questionText = "What is the speed of light in vacuum approximately?",
            answerOptions = listOf("300,000 km/s", "150,000 km/s", "1,000 km/s", "30,000 km/s"),
            correctAnswer = "300,000 km/s",
            explanation = "Light travels at approximately 299,792 kilometers per second in vacuum.",
            points = 150,
            timeLimit = 12
        ),
        Question(
            id = "sci_12",
            categoryId = "science",
            difficulty = Difficulty.HARD,
            questionText = "What is the pH value of pure distilled water at 25°C?",
            answerOptions = listOf("5", "7", "9", "1"),
            correctAnswer = "7",
            explanation = "A pH of 7 represents neutral acidity at room temperature.",
            points = 200,
            timeLimit = 12
        ),
        Question(
            id = "sci_13",
            categoryId = "science",
            difficulty = Difficulty.HARD,
            questionText = "Which blood cells are responsible for transporting oxygen?",
            answerOptions = listOf("Erythrocytes (Red)", "Leukocytes (White)", "Thrombocytes (Platelets)", "Lymphocytes"),
            correctAnswer = "Erythrocytes (Red)",
            explanation = "Red blood cells contain hemoglobin that binds and delivers oxygen.",
            points = 200,
            timeLimit = 12
        ),
        Question(
            id = "sci_14",
            categoryId = "science",
            difficulty = Difficulty.HARD,
            questionText = "What is the hardest known natural mineral on Earth?",
            answerOptions = listOf("Quartz", "Corundum", "Diamond", "Topaz"),
            correctAnswer = "Diamond",
            explanation = "Diamond ranks at the top rating of 10 on the Mohs hardness scale.",
            points = 200,
            timeLimit = 10
        ),
        Question(
            id = "sci_15",
            categoryId = "science",
            difficulty = Difficulty.HARD,
            questionText = "What is the primary constituent of the Sun's mass?",
            answerOptions = listOf("Helium", "Hydrogen", "Carbon", "Oxygen"),
            correctAnswer = "Hydrogen",
            explanation = "Hydrogen accounts for about 73% of the Sun's mass.",
            points = 200,
            timeLimit = 12
        ),
        Question(
            id = "sci_16",
            categoryId = "science",
            difficulty = Difficulty.HARD,
            questionText = "Which law states that energy cannot be created or destroyed?",
            answerOptions = listOf("First Law of Thermodynamics", "Second Law of Motion", "Coulomb's Law", "Hubble's Law"),
            correctAnswer = "First Law of Thermodynamics",
            explanation = "The Law of Conservation of Energy (First Law) states energy only changes forms.",
            points = 200,
            timeLimit = 15
        ),
        Question(
            id = "sci_17",
            categoryId = "science",
            difficulty = Difficulty.EXTREME,
            questionText = "What phenomenon describes light bending when passing around a massive object?",
            answerOptions = listOf("Gravitational Lensing", "Photoelectric Effect", "Compton Scattering", "Cherenkov Radiation"),
            correctAnswer = "Gravitational Lensing",
            explanation = "Predicted by Einstein's General Relativity, spacetime curvature bends light paths.",
            points = 300,
            timeLimit = 15
        ),
        Question(
            id = "sci_18",
            categoryId = "science",
            difficulty = Difficulty.EXTREME,
            questionText = "Which subatomic particles make up protons and neutrons?",
            answerOptions = listOf("Quarks", "Leptons", "Muons", "Positrons"),
            correctAnswer = "Quarks",
            explanation = "A proton has 2 up and 1 down quark; a neutron has 1 up and 2 down quarks.",
            points = 300,
            timeLimit = 15
        ),
        Question(
            id = "sci_19",
            categoryId = "science",
            difficulty = Difficulty.EXTREME,
            questionText = "What is absolute zero temperature in Kelvin?",
            answerOptions = listOf("0 K", "-273.15 K", "100 K", "373.15 K"),
            correctAnswer = "0 K",
            explanation = "0 Kelvin (-273.15°C) is the lowest thermodynamic temperature limit.",
            points = 300,
            timeLimit = 12
        ),
        Question(
            id = "sci_20",
            categoryId = "science",
            difficulty = Difficulty.EXTREME,
            questionText = "What is the role of telomeres in human DNA?",
            answerOptions = listOf("Protect chromosome ends from degradation", "Synthesize proteins", "Store RNA transcripts", "Trigger cytokinesis"),
            correctAnswer = "Protect chromosome ends from degradation",
            explanation = "Telomeres cap chromosome ends to prevent loss of vital genetic sequences during replication.",
            points = 300,
            timeLimit = 15
        )
    )

    val patternQuestions: List<Question> = listOf(
        Question(
            id = "pat_01",
            categoryId = "patterns",
            difficulty = Difficulty.EASY,
            questionText = "If Red + Blue = Purple, and Blue + Yellow = Green, then Red + Yellow = ?",
            answerOptions = listOf("Orange", "Brown", "Violet", "Teal"),
            correctAnswer = "Orange",
            explanation = "Mixing primary colors red and yellow produces secondary color orange.",
            points = 100,
            timeLimit = 10
        ),
        Question(
            id = "pat_02",
            categoryId = "patterns",
            difficulty = Difficulty.EASY,
            questionText = "Pattern: Up, Right, Down, Left, Up, ___",
            answerOptions = listOf("Down", "Left", "Right", "Up"),
            correctAnswer = "Right",
            explanation = "This is a clockwise rotation: Up -> Right -> Down -> Left -> Up -> Right.",
            points = 100,
            timeLimit = 10
        ),
        Question(
            id = "pat_03",
            categoryId = "patterns",
            difficulty = Difficulty.EASY,
            questionText = "Letter progression: A, C, E, G, ___",
            answerOptions = listOf("H", "I", "J", "K"),
            correctAnswer = "I",
            explanation = "Skipping one letter each time (+2): G (+2) = I.",
            points = 100,
            timeLimit = 10
        ),
        Question(
            id = "pat_04",
            categoryId = "patterns",
            difficulty = Difficulty.EASY,
            questionText = "Find the missing term: △ (3 sides), ▢ (4 sides), ⬠ (5 sides), ___",
            answerOptions = listOf("⬡ (6 sides)", "◯ (0 sides)", "⬨ (4 sides)", "⬘ (2 sides)"),
            correctAnswer = "⬡ (6 sides)",
            explanation = "Each shape adds 1 side: triangle (3), square (4), pentagon (5), hexagon (6).",
            points = 100,
            timeLimit = 10
        ),
        Question(
            id = "pat_05",
            categoryId = "patterns",
            difficulty = Difficulty.EASY,
            questionText = "Pattern: Morning, Noon, Evening, Night, Morning, ___",
            answerOptions = listOf("Noon", "Evening", "Dusk", "Midnight"),
            correctAnswer = "Noon",
            explanation = "Daily cycle continues from Morning to Noon.",
            points = 100,
            timeLimit = 10
        ),
        Question(
            id = "pat_06",
            categoryId = "patterns",
            difficulty = Difficulty.MEDIUM,
            questionText = "Pattern: 1A, 2B, 4C, 8D, ___",
            answerOptions = listOf("16E", "12E", "16F", "10D"),
            correctAnswer = "16E",
            explanation = "The number doubles (1, 2, 4, 8, 16) while letters advance alphabetically (A, B, C, D, E).",
            points = 150,
            timeLimit = 15
        ),
        Question(
            id = "pat_07",
            categoryId = "patterns",
            difficulty = Difficulty.MEDIUM,
            questionText = "Letter pattern: Z, X, V, T, ___",
            answerOptions = listOf("S", "R", "Q", "P"),
            correctAnswer = "R",
            explanation = "Moving backward through the alphabet by 2 letters: T (-2) = R.",
            points = 150,
            timeLimit = 12
        ),
        Question(
            id = "pat_08",
            categoryId = "patterns",
            difficulty = Difficulty.MEDIUM,
            questionText = "Look at: AB, CD, EF, GH, ___",
            answerOptions = listOf("IJ", "JK", "HI", "IK"),
            correctAnswer = "IJ",
            explanation = "Consecutive alphabet pairs: next pair is I and J.",
            points = 150,
            timeLimit = 10
        ),
        Question(
            id = "pat_09",
            categoryId = "patterns",
            difficulty = Difficulty.MEDIUM,
            questionText = "Which symbol completes: ☀️ (Day), 🌙 (Night), ☀️ (Day), ___",
            answerOptions = listOf("⭐", "🌙", "☁️", "⚡"),
            correctAnswer = "🌙",
            explanation = "Alternating binary pattern: Sun, Moon, Sun, Moon.",
            points = 150,
            timeLimit = 10
        ),
        Question(
            id = "pat_10",
            categoryId = "patterns",
            difficulty = Difficulty.MEDIUM,
            questionText = "Pattern: O, T, T, F, F, S, S, E, ___",
            answerOptions = listOf("N", "T", "O", "Z"),
            correctAnswer = "N",
            explanation = "First letters of numbers: One, Two, Three, Four, Five, Six, Seven, Eight, Nine (N).",
            points = 150,
            timeLimit = 18
        ),
        Question(
            id = "pat_11",
            categoryId = "patterns",
            difficulty = Difficulty.MEDIUM,
            questionText = "Sequence: A1, B2, D4, G7, ___",
            answerOptions = listOf("K11", "I9", "J10", "H8"),
            correctAnswer = "K11",
            explanation = "Increments increase: +1, +2, +3, next is +4: G (7) + 4 = K (11).",
            points = 150,
            timeLimit = 15
        ),
        Question(
            id = "pat_12",
            categoryId = "patterns",
            difficulty = Difficulty.HARD,
            questionText = "Pattern: J, F, M, A, M, J, J, A, S, O, N, ___",
            answerOptions = listOf("D", "M", "J", "F"),
            correctAnswer = "D",
            explanation = "First letters of the months: January through December (D).",
            points = 200,
            timeLimit = 15
        ),
        Question(
            id = "pat_13",
            categoryId = "patterns",
            difficulty = Difficulty.HARD,
            questionText = "Pattern: 1Z, 3X, 6U, 10Q, ___",
            answerOptions = listOf("15L", "14M", "15K", "16L"),
            correctAnswer = "15L",
            explanation = "Numbers: +2, +3, +4, next is +5 (10 + 5 = 15). Letters step back: -2, -3, -4, next is -5: Q (17) - 5 = L (12).",
            points = 200,
            timeLimit = 20
        ),
        Question(
            id = "pat_14",
            categoryId = "patterns",
            difficulty = Difficulty.HARD,
            questionText = "Pattern: Red, Green, Blue, Cyan, Magenta, ___",
            answerOptions = listOf("Yellow", "Purple", "White", "Black"),
            correctAnswer = "Yellow",
            explanation = "RGB primary colors followed by secondary colors CMY: Cyan, Magenta, Yellow.",
            points = 200,
            timeLimit = 15
        ),
        Question(
            id = "pat_15",
            categoryId = "patterns",
            difficulty = Difficulty.HARD,
            questionText = "Which letter is missing: B, D, G, K, P, ___?",
            answerOptions = listOf("U", "V", "W", "X"),
            correctAnswer = "V",
            explanation = "Gaps increase: B(+2)D(+3)G(+4)K(+5)P(+6) = V.",
            points = 200,
            timeLimit = 18
        ),
        Question(
            id = "pat_16",
            categoryId = "patterns",
            difficulty = Difficulty.HARD,
            questionText = "Pattern: M, T, W, T, F, S, ___",
            answerOptions = listOf("S", "M", "T", "W"),
            correctAnswer = "S",
            explanation = "Days of the week initials: Monday, Tuesday, Wednesday, Thursday, Friday, Saturday, Sunday (S).",
            points = 200,
            timeLimit = 12
        ),
        Question(
            id = "pat_17",
            categoryId = "patterns",
            difficulty = Difficulty.EXTREME,
            questionText = "Pattern: 2A, 4D, 8I, 16P, ___",
            answerOptions = listOf("32Y", "32X", "30Z", "24W"),
            correctAnswer = "32Y",
            explanation = "Numbers double (2, 4, 8, 16, 32). Letters correspond to squares: 1^2=A, 2^2=D(4), 3^2=I(9), 4^2=P(16), 5^2=Y(25).",
            points = 300,
            timeLimit = 20
        ),
        Question(
            id = "pat_18",
            categoryId = "patterns",
            difficulty = Difficulty.EXTREME,
            questionText = "Look at: 1, 11, 21, 1211, 111221, ___",
            answerOptions = listOf("312211", "13112221", "112211", "211211"),
            correctAnswer = "312211",
            explanation = "Look-and-say sequence: 111221 has 'three 1s, two 2s, one 1' -> 312211.",
            points = 300,
            timeLimit = 22
        ),
        Question(
            id = "pat_19",
            categoryId = "patterns",
            difficulty = Difficulty.EXTREME,
            questionText = "What comes next: 0, 1, 1, 2, 4, 7, 13, 24, ___?",
            answerOptions = listOf("44", "41", "37", "48"),
            correctAnswer = "44",
            explanation = "Tribonacci sequence: sum of previous 3 numbers: 7 + 13 + 24 = 44.",
            points = 300,
            timeLimit = 20
        ),
        Question(
            id = "pat_20",
            categoryId = "patterns",
            difficulty = Difficulty.EXTREME,
            questionText = "Pattern: H, He, Li, Be, B, C, N, O, F, ___",
            answerOptions = listOf("Ne", "Na", "Mg", "Al"),
            correctAnswer = "Ne",
            explanation = "Periodic table elements by atomic number: 1(H) to 9(F), next is 10(Ne Neon).",
            points = 300,
            timeLimit = 15
        )
    )

    val speedQuestions: List<Question> = listOf(
        Question(
            id = "spd_01",
            categoryId = "speed",
            difficulty = Difficulty.EASY,
            questionText = "7 + 8 = ?",
            answerOptions = listOf("14", "15", "16", "17"),
            correctAnswer = "15",
            explanation = "7 + 8 = 15.",
            points = 100,
            timeLimit = 8
        ),
        Question(
            id = "spd_02",
            categoryId = "speed",
            difficulty = Difficulty.EASY,
            questionText = "12 - 5 = ?",
            answerOptions = listOf("6", "7", "8", "9"),
            correctAnswer = "7",
            explanation = "12 - 5 = 7.",
            points = 100,
            timeLimit = 8
        ),
        Question(
            id = "spd_03",
            categoryId = "speed",
            difficulty = Difficulty.EASY,
            questionText = "6 × 6 = ?",
            answerOptions = listOf("32", "34", "36", "38"),
            correctAnswer = "36",
            explanation = "6 × 6 = 36.",
            points = 100,
            timeLimit = 8
        ),
        Question(
            id = "spd_04",
            categoryId = "speed",
            difficulty = Difficulty.EASY,
            questionText = "True or False: A triangle has 3 sides.",
            answerOptions = listOf("True", "False"),
            correctAnswer = "True",
            explanation = "By definition a triangle has 3 sides.",
            points = 100,
            timeLimit = 6
        ),
        Question(
            id = "spd_05",
            categoryId = "speed",
            difficulty = Difficulty.EASY,
            questionText = "50 ÷ 5 = ?",
            answerOptions = listOf("5", "10", "15", "20"),
            correctAnswer = "10",
            explanation = "50 ÷ 5 = 10.",
            points = 100,
            timeLimit = 6
        ),
        Question(
            id = "spd_06",
            categoryId = "speed",
            difficulty = Difficulty.MEDIUM,
            questionText = "99 - 43 = ?",
            answerOptions = listOf("54", "56", "58", "52"),
            correctAnswer = "56",
            explanation = "99 - 43 = 56.",
            points = 150,
            timeLimit = 8
        ),
        Question(
            id = "spd_07",
            categoryId = "speed",
            difficulty = Difficulty.MEDIUM,
            questionText = "15 × 3 = ?",
            answerOptions = listOf("35", "40", "45", "50"),
            correctAnswer = "45",
            explanation = "15 × 3 = 45.",
            points = 150,
            timeLimit = 8
        ),
        Question(
            id = "spd_08",
            categoryId = "speed",
            difficulty = Difficulty.MEDIUM,
            questionText = "True or False: Spiders are insects.",
            answerOptions = listOf("True", "False"),
            correctAnswer = "False",
            explanation = "Spiders are arachnids (8 legs); insects have 6 legs.",
            points = 150,
            timeLimit = 6
        ),
        Question(
            id = "spd_09",
            categoryId = "speed",
            difficulty = Difficulty.MEDIUM,
            questionText = "Which number is greater: 0.65 or 0.605?",
            answerOptions = listOf("0.65", "0.605", "They are equal"),
            correctAnswer = "0.65",
            explanation = "0.650 is greater than 0.605.",
            points = 150,
            timeLimit = 8
        ),
        Question(
            id = "spd_10",
            categoryId = "speed",
            difficulty = Difficulty.MEDIUM,
            questionText = "81 ÷ 9 = ?",
            answerOptions = listOf("7", "8", "9", "10"),
            correctAnswer = "9",
            explanation = "9 × 9 = 81.",
            points = 150,
            timeLimit = 6
        ),
        Question(
            id = "spd_11",
            categoryId = "speed",
            difficulty = Difficulty.MEDIUM,
            questionText = "Double 36:",
            answerOptions = listOf("68", "72", "74", "76"),
            correctAnswer = "72",
            explanation = "36 × 2 = 72.",
            points = 150,
            timeLimit = 6
        ),
        Question(
            id = "spd_12",
            categoryId = "speed",
            difficulty = Difficulty.HARD,
            questionText = "14 × 7 = ?",
            answerOptions = listOf("94", "96", "98", "102"),
            correctAnswer = "98",
            explanation = "10 × 7 = 70, 4 × 7 = 28. 70 + 28 = 98.",
            points = 200,
            timeLimit = 8
        ),
        Question(
            id = "spd_13",
            categoryId = "speed",
            difficulty = Difficulty.HARD,
            questionText = "True or False: 2 is the only even prime number.",
            answerOptions = listOf("True", "False"),
            correctAnswer = "True",
            explanation = "All other even numbers are divisible by 2.",
            points = 200,
            timeLimit = 6
        ),
        Question(
            id = "spd_14",
            categoryId = "speed",
            difficulty = Difficulty.HARD,
            questionText = "Half of 250 is:",
            answerOptions = listOf("120", "125", "130", "135"),
            correctAnswer = "125",
            explanation = "250 ÷ 2 = 125.",
            points = 200,
            timeLimit = 6
        ),
        Question(
            id = "spd_15",
            categoryId = "speed",
            difficulty = Difficulty.HARD,
            questionText = "What is 4 cubed (4^3)?",
            answerOptions = listOf("16", "32", "64", "128"),
            correctAnswer = "64",
            explanation = "4 × 4 × 4 = 64.",
            points = 200,
            timeLimit = 6
        ),
        Question(
            id = "spd_16",
            categoryId = "speed",
            difficulty = Difficulty.HARD,
            questionText = "125 + 375 = ?",
            answerOptions = listOf("450", "490", "500", "510"),
            correctAnswer = "500",
            explanation = "125 + 375 = 500.",
            points = 200,
            timeLimit = 6
        ),
        Question(
            id = "spd_17",
            categoryId = "speed",
            difficulty = Difficulty.EXTREME,
            questionText = "19 × 6 = ?",
            answerOptions = listOf("108", "114", "116", "124"),
            correctAnswer = "114",
            explanation = "20 × 6 - 6 = 120 - 6 = 114.",
            points = 300,
            timeLimit = 7
        ),
        Question(
            id = "spd_18",
            categoryId = "speed",
            difficulty = Difficulty.EXTREME,
            questionText = "Square root of 289:",
            answerOptions = listOf("15", "17", "19", "21"),
            correctAnswer = "17",
            explanation = "17 × 17 = 289.",
            points = 300,
            timeLimit = 7
        ),
        Question(
            id = "spd_19",
            categoryId = "speed",
            difficulty = Difficulty.EXTREME,
            questionText = "35% of 200 = ?",
            answerOptions = listOf("65", "70", "75", "80"),
            correctAnswer = "70",
            explanation = "35 × 2 = 70.",
            points = 300,
            timeLimit = 6
        ),
        Question(
            id = "spd_20",
            categoryId = "speed",
            difficulty = Difficulty.EXTREME,
            questionText = "480 ÷ 16 = ?",
            answerOptions = listOf("25", "30", "35", "40"),
            correctAnswer = "30",
            explanation = "16 × 3 = 48, so 480 ÷ 16 = 30.",
            points = 300,
            timeLimit = 6
        )
    )

    val geographyQuestions: List<Question> get() = DedicatedCategoryQuestionPools.geographyQuestions
    val memoryQuestions: List<Question> get() = DedicatedCategoryQuestionPools.memoryQuestions
    val riddleQuestions: List<Question> get() = DedicatedCategoryQuestionPools.riddleQuestions
    val technologyQuestions: List<Question> get() = DedicatedCategoryQuestionPools.technologyQuestions

    val allQuestions: List<Question> by lazy {
        mathQuestions + numbersQuestions + logicQuestions + wordsQuestions +
                knowledgeQuestions + scienceQuestions + patternQuestions + speedQuestions +
                geographyQuestions + memoryQuestions + riddleQuestions + technologyQuestions
    }
}
