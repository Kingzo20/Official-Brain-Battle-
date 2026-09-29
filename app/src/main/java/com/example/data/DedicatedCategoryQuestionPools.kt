package com.example.data

import com.example.model.Difficulty
import com.example.model.Question

/**
 * Dedicated category-specific question pools for:
 * - Geography: Countries, locations, capitals, landmarks
 * - Memory: Memory-based question and recall gameplay
 * - Riddles: Brain teasers and riddles
 * - Technology: Tech, computing, and modern science
 */
object DedicatedCategoryQuestionPools {

    val geographyQuestions: List<Question> = listOf(
        Question(
            id = "geo_01",
            categoryId = "geography",
            difficulty = Difficulty.EASY,
            questionText = "What is the capital city of France?",
            answerOptions = listOf("Marseille", "Lyon", "Paris", "Nice"),
            correctAnswer = "Paris",
            explanation = "Paris has been the capital and cultural epicenter of France since medieval times.",
            points = 100,
            timeLimit = 15
        ),
        Question(
            id = "geo_02",
            categoryId = "geography",
            difficulty = Difficulty.EASY,
            questionText = "Which is the largest continent on Earth by both land area and population?",
            answerOptions = listOf("Africa", "Asia", "North America", "Europe"),
            correctAnswer = "Asia",
            explanation = "Asia covers approximately 30% of Earth's total land area and is home to over 4.7 billion people.",
            points = 100,
            timeLimit = 15
        ),
        Question(
            id = "geo_03",
            categoryId = "geography",
            difficulty = Difficulty.EASY,
            questionText = "In which country can you visit the ancient Colosseum?",
            answerOptions = listOf("Greece", "Spain", "Italy", "Turkey"),
            correctAnswer = "Italy",
            explanation = "The Colosseum, an iconic Roman amphitheater, is situated in the center of Rome, Italy.",
            points = 100,
            timeLimit = 15
        ),
        Question(
            id = "geo_04",
            categoryId = "geography",
            difficulty = Difficulty.EASY,
            questionText = "What is the longest river in the world?",
            answerOptions = listOf("Amazon River", "Nile River", "Yangtze River", "Mississippi River"),
            correctAnswer = "Nile River",
            explanation = "The Nile River spans roughly 6,650 kilometers (4,132 miles) through northeastern Africa.",
            points = 100,
            timeLimit = 15
        ),
        Question(
            id = "geo_05",
            categoryId = "geography",
            difficulty = Difficulty.EASY,
            questionText = "Which ocean lies between the Americas and Europe/Africa?",
            answerOptions = listOf("Pacific Ocean", "Indian Ocean", "Atlantic Ocean", "Arctic Ocean"),
            correctAnswer = "Atlantic Ocean",
            explanation = "The Atlantic Ocean separates North and South America to the west from Europe and Africa to the east.",
            points = 100,
            timeLimit = 15
        ),
        Question(
            id = "geo_06",
            categoryId = "geography",
            difficulty = Difficulty.MEDIUM,
            questionText = "What is the official capital city of Australia?",
            answerOptions = listOf("Sydney", "Melbourne", "Canberra", "Brisbane"),
            correctAnswer = "Canberra",
            explanation = "Canberra was chosen as Australia's capital in 1908 as a compromise between Sydney and Melbourne.",
            points = 150,
            timeLimit = 15
        ),
        Question(
            id = "geo_07",
            categoryId = "geography",
            difficulty = Difficulty.MEDIUM,
            questionText = "Which mountain range separates the continents of Europe and Asia?",
            answerOptions = listOf("The Alps", "The Andes", "The Ural Mountains", "The Rockies"),
            correctAnswer = "The Ural Mountains",
            explanation = "The Ural Mountains in western Russia form the conventional geographic boundary between Europe and Asia.",
            points = 150,
            timeLimit = 15
        ),
        Question(
            id = "geo_08",
            categoryId = "geography",
            difficulty = Difficulty.MEDIUM,
            questionText = "Which country has the longest coastline in the world?",
            answerOptions = listOf("Russia", "Canada", "Australia", "Indonesia"),
            correctAnswer = "Canada",
            explanation = "Canada's coastline stretches across 243,042 kilometers, including its vast Arctic islands.",
            points = 150,
            timeLimit = 15
        ),
        Question(
            id = "geo_09",
            categoryId = "geography",
            difficulty = Difficulty.MEDIUM,
            questionText = "In which modern country was the ancient city of Machu Picchu built?",
            answerOptions = listOf("Chile", "Bolivia", "Peru", "Ecuador"),
            correctAnswer = "Peru",
            explanation = "Machu Picchu is a 15th-century Inca citadel situated on an Andean mountain ridge in Peru.",
            points = 150,
            timeLimit = 15
        ),
        Question(
            id = "geo_10",
            categoryId = "geography",
            difficulty = Difficulty.MEDIUM,
            questionText = "Which African nation has three designated capital cities (Pretoria, Cape Town, Bloemfontein)?",
            answerOptions = listOf("Nigeria", "South Africa", "Kenya", "Egypt"),
            correctAnswer = "South Africa",
            explanation = "South Africa splits its government branches across Pretoria (executive), Cape Town (legislative), and Bloemfontein (judicial).",
            points = 150,
            timeLimit = 15
        ),
        Question(
            id = "geo_11",
            categoryId = "geography",
            difficulty = Difficulty.HARD,
            questionText = "What is the deepest known natural ocean trench on Earth?",
            answerOptions = listOf("Puerto Rico Trench", "Java Trench", "Mariana Trench", "Tonga Trench"),
            correctAnswer = "Mariana Trench",
            explanation = "The Mariana Trench reaches a maximum depth of approximately 10,994 meters (Challenger Deep) in the western Pacific.",
            points = 200,
            timeLimit = 12
        ),
        Question(
            id = "geo_12",
            categoryId = "geography",
            difficulty = Difficulty.HARD,
            questionText = "Which country contains the geographic point known as the driest place on Earth (Atacama Desert)?",
            answerOptions = listOf("Argentina", "Chile", "Namibia", "Egypt"),
            correctAnswer = "Chile",
            explanation = "The Atacama Desert in northern Chile receives virtually zero precipitation in some monitoring zones.",
            points = 200,
            timeLimit = 12
        ),
        Question(
            id = "geo_13",
            categoryId = "geography",
            difficulty = Difficulty.HARD,
            questionText = "What strait connects the Mediterranean Sea directly with the Atlantic Ocean?",
            answerOptions = listOf("Strait of Hormuz", "Strait of Malacca", "Strait of Gibraltar", "Bosphorus Strait"),
            correctAnswer = "Strait of Gibraltar",
            explanation = "The Strait of Gibraltar is a 13-kilometer-wide narrow waterway separating Spain and Morocco.",
            points = 200,
            timeLimit = 12
        ),
        Question(
            id = "geo_14",
            categoryId = "geography",
            difficulty = Difficulty.HARD,
            questionText = "Which nation is known as the only country in the world situated entirely above 1,000 meters elevation?",
            answerOptions = listOf("Lesotho", "Bhutan", "Nepal", "Switzerland"),
            correctAnswer = "Lesotho",
            explanation = "Lesotho, entirely landlocked inside South Africa, has its lowest point at 1,400 meters above sea level.",
            points = 200,
            timeLimit = 12
        ),
        Question(
            id = "geo_15",
            categoryId = "geography",
            difficulty = Difficulty.HARD,
            questionText = "What is the world's largest landlocked country by area?",
            answerOptions = listOf("Mongolia", "Kazakhstan", "Chad", "Bolivia"),
            correctAnswer = "Kazakhstan",
            explanation = "Kazakhstan spans 2.72 million square kilometers, making it the largest landlocked sovereign state on Earth.",
            points = 200,
            timeLimit = 12
        )
    )

    val memoryQuestions: List<Question> = listOf(
        Question(
            id = "mem_01",
            categoryId = "memory",
            difficulty = Difficulty.EASY,
            questionText = "Remember this sequence: [BLUE, RED, YELLOW, GREEN]. What was the THIRD color?",
            answerOptions = listOf("Blue", "Red", "Yellow", "Green"),
            correctAnswer = "Yellow",
            explanation = "In the sequence [BLUE, RED, YELLOW, GREEN], the third position is Yellow.",
            points = 100,
            timeLimit = 15
        ),
        Question(
            id = "mem_02",
            categoryId = "memory",
            difficulty = Difficulty.EASY,
            questionText = "A telephone number was read as: 5-8-2-9-1. Which digit was between 2 and 1?",
            answerOptions = listOf("5", "8", "9", "2"),
            correctAnswer = "9",
            explanation = "The sequence is 5, 8, 2, 9, 1. The digit between 2 and 1 is 9.",
            points = 100,
            timeLimit = 15
        ),
        Question(
            id = "mem_03",
            categoryId = "memory",
            difficulty = Difficulty.EASY,
            questionText = "Three animals entered the barn: Cat first, Goat second, Owl third. Which animal arrived LAST?",
            answerOptions = listOf("Cat", "Goat", "Owl", "Horse"),
            correctAnswer = "Owl",
            explanation = "Cat (1st), Goat (2nd), Owl (3rd and last).",
            points = 100,
            timeLimit = 15
        ),
        Question(
            id = "mem_04",
            categoryId = "memory",
            difficulty = Difficulty.EASY,
            questionText = "Remember the code: 'ALPHA-7'. Which letter was paired with number 7?",
            answerOptions = listOf("BETA", "OMEGA", "ALPHA", "DELTA"),
            correctAnswer = "ALPHA",
            explanation = "The code clearly stated 'ALPHA-7'.",
            points = 100,
            timeLimit = 15
        ),
        Question(
            id = "mem_05",
            categoryId = "memory",
            difficulty = Difficulty.EASY,
            questionText = "Items in the basket: 2 Apples, 3 Oranges, 1 Banana. How many Oranges were stored?",
            answerOptions = listOf("1", "2", "3", "4"),
            correctAnswer = "3",
            explanation = "There were 3 Oranges in the memory set.",
            points = 100,
            timeLimit = 15
        ),
        Question(
            id = "mem_06",
            categoryId = "memory",
            difficulty = Difficulty.MEDIUM,
            questionText = "Observe this card sequence: King of Hearts, 7 of Spades, Ace of Diamonds, 4 of Clubs. Which card followed the 7 of Spades?",
            answerOptions = listOf("King of Hearts", "Ace of Diamonds", "4 of Clubs", "Queen of Spades"),
            correctAnswer = "Ace of Diamonds",
            explanation = "The sequence is: King of Hearts -> 7 of Spades -> Ace of Diamonds -> 4 of Clubs.",
            points = 150,
            timeLimit = 15
        ),
        Question(
            id = "mem_07",
            categoryId = "memory",
            difficulty = Difficulty.MEDIUM,
            questionText = "Flight paths recorded: Gate 14 (Tokyo), Gate 22 (London), Gate 07 (New York), Gate 31 (Sydney). Which city boarded at Gate 22?",
            answerOptions = listOf("Tokyo", "London", "New York", "Sydney"),
            correctAnswer = "London",
            explanation = "Gate 22 was matched with London.",
            points = 150,
            timeLimit = 15
        ),
        Question(
            id = "mem_08",
            categoryId = "memory",
            difficulty = Difficulty.MEDIUM,
            questionText = "Memorize the 6-digit pin: 4 - 9 - 1 - 7 - 3 - 8. What is the sum of the FIRST and LAST digits?",
            answerOptions = listOf("10", "11", "12", "13"),
            correctAnswer = "12",
            explanation = "First digit is 4, last digit is 8. 4 + 8 = 12.",
            points = 150,
            timeLimit = 15
        ),
        Question(
            id = "mem_09",
            categoryId = "memory",
            difficulty = Difficulty.MEDIUM,
            questionText = "Four runners crossed in order: Maya (1st), Liam (2nd), Sophia (3rd), Noah (4th). Who finished immediately ahead of Sophia?",
            answerOptions = listOf("Maya", "Liam", "Noah", "Ethan"),
            correctAnswer = "Liam",
            explanation = "Liam was 2nd, immediately ahead of 3rd-place Sophia.",
            points = 150,
            timeLimit = 15
        ),
        Question(
            id = "mem_10",
            categoryId = "memory",
            difficulty = Difficulty.MEDIUM,
            questionText = "Storage locker contents: Box A has Silver coins, Box B has Gold rings, Box C has Emeralds. What is inside Box B?",
            answerOptions = listOf("Silver coins", "Gold rings", "Emeralds", "Diamonds"),
            correctAnswer = "Gold rings",
            explanation = "Box B was designated to hold Gold rings.",
            points = 150,
            timeLimit = 15
        ),
        Question(
            id = "mem_11",
            categoryId = "memory",
            difficulty = Difficulty.HARD,
            questionText = "Recall this reverse sequence: 9 -> 4 -> 7 -> 2 -> 6. If repeated BACKWARDS from end to start, what is the 3rd number spoken?",
            answerOptions = listOf("6", "2", "7", "4"),
            correctAnswer = "7",
            explanation = "Backwards order: 6 (1st), 2 (2nd), 7 (3rd), 4 (4th), 9 (5th).",
            points = 200,
            timeLimit = 12
        ),
        Question(
            id = "mem_12",
            categoryId = "memory",
            difficulty = Difficulty.HARD,
            questionText = "Color grid: Top-Left=Green, Top-Right=Purple, Bottom-Left=Yellow, Bottom-Right=Cyan. Which color sat DIAGONALLY opposite to Green?",
            answerOptions = listOf("Purple", "Yellow", "Cyan", "Red"),
            correctAnswer = "Cyan",
            explanation = "The diagonal opposite of Top-Left (Green) is Bottom-Right (Cyan).",
            points = 200,
            timeLimit = 12
        ),
        Question(
            id = "mem_13",
            categoryId = "memory",
            difficulty = Difficulty.HARD,
            questionText = "Audit trail items: [T-302: Pass], [T-303: Fail], [T-304: Pass], [T-305: Pass], [T-306: Fail]. How many items Passed in total?",
            answerOptions = listOf("2", "3", "4", "1"),
            correctAnswer = "3",
            explanation = "Items T-302, T-304, and T-305 Passed (3 items).",
            points = 200,
            timeLimit = 12
        ),
        Question(
            id = "mem_14",
            categoryId = "memory",
            difficulty = Difficulty.HARD,
            questionText = "A musical note progression played: C - E - G - B - D. Which note occurred between E and B?",
            answerOptions = listOf("C", "G", "D", "F"),
            correctAnswer = "G",
            explanation = "The note between E and B is G.",
            points = 200,
            timeLimit = 12
        ),
        Question(
            id = "mem_15",
            categoryId = "memory",
            difficulty = Difficulty.HARD,
            questionText = "Three security lights blinked in pattern: Red blinked 4 times, Amber blinked 2 times, Blue blinked 5 times. Total flashes count?",
            answerOptions = listOf("10", "11", "12", "9"),
            correctAnswer = "11",
            explanation = "4 + 2 + 5 = 11 total flashes.",
            points = 200,
            timeLimit = 12
        )
    )

    val riddleQuestions: List<Question> = listOf(
        Question(
            id = "rid_01",
            categoryId = "riddles",
            difficulty = Difficulty.EASY,
            questionText = "What has hands and a face, but cannot hold anything or smile?",
            answerOptions = listOf("A doll", "A mirror", "A clock", "A coin"),
            correctAnswer = "A clock",
            explanation = "A clock has hour/minute hands and a dial face, but no physical touch or emotion.",
            points = 100,
            timeLimit = 15
        ),
        Question(
            id = "rid_02",
            categoryId = "riddles",
            difficulty = Difficulty.EASY,
            questionText = "The more of this you take, the more you leave behind. What are they?",
            answerOptions = listOf("Breaths", "Footsteps", "Memories", "Photographs"),
            correctAnswer = "Footsteps",
            explanation = "As you walk and take steps forward, you leave footsteps behind you in your path.",
            points = 100,
            timeLimit = 15
        ),
        Question(
            id = "rid_03",
            categoryId = "riddles",
            difficulty = Difficulty.EASY,
            questionText = "What gets wetter the more it dries?",
            answerOptions = listOf("A sponge", "A towel", "A cloud", "A river"),
            correctAnswer = "A towel",
            explanation = "As a towel dries your body or dishes, it absorbs the moisture and becomes wetter.",
            points = 100,
            timeLimit = 15
        ),
        Question(
            id = "rid_04",
            categoryId = "riddles",
            difficulty = Difficulty.EASY,
            questionText = "What belongs to you, but everyone else uses it much more than you do?",
            answerOptions = listOf("Your phone", "Your money", "Your name", "Your car"),
            correctAnswer = "Your name",
            explanation = "Your name is your identity, but others say it whenever they address you.",
            points = 100,
            timeLimit = 15
        ),
        Question(
            id = "rid_05",
            categoryId = "riddles",
            difficulty = Difficulty.EASY,
            questionText = "What has many keys but can never open a single locked door?",
            answerOptions = listOf("A keychain", "A piano", "A map", "A vault"),
            correctAnswer = "A piano",
            explanation = "A musical piano has 88 musical keys, but none are designed to unlock doors.",
            points = 100,
            timeLimit = 15
        ),
        Question(
            id = "rid_06",
            categoryId = "riddles",
            difficulty = Difficulty.MEDIUM,
            questionText = "I speak without a mouth and hear without ears. I have no body, but I come alive with wind. What am I?",
            answerOptions = listOf("A shadow", "An echo", "A kite", "A whistle"),
            correctAnswer = "An echo",
            explanation = "An echo reflects sound waves across distances without a physical body.",
            points = 150,
            timeLimit = 15
        ),
        Question(
            id = "rid_07",
            categoryId = "riddles",
            difficulty = Difficulty.MEDIUM,
            questionText = "I have cities, but no houses. I have mountains, but no trees. I have water, but no fish. What am I?",
            answerOptions = listOf("A photograph", "A map", "A desert", "A dream"),
            correctAnswer = "A map",
            explanation = "A geographical map illustrates cities, rivers, and mountains as cartographic symbols.",
            points = 150,
            timeLimit = 15
        ),
        Question(
            id = "rid_08",
            categoryId = "riddles",
            difficulty = Difficulty.MEDIUM,
            questionText = "What can fill an entire room without taking up any physical space?",
            answerOptions = listOf("Air", "Light", "Smoke", "Dust"),
            correctAnswer = "Light",
            explanation = "Light from a single bulb fills the entire volume of a room without occupying mass.",
            points = 150,
            timeLimit = 15
        ),
        Question(
            id = "rid_09",
            categoryId = "riddles",
            difficulty = Difficulty.MEDIUM,
            questionText = "If you have me, you want to share me. If you share me, you haven't kept me. What am I?",
            answerOptions = listOf("A secret", "A joke", "Money", "Knowledge"),
            correctAnswer = "A secret",
            explanation = "Once a secret is shared with others, it is no longer kept a secret.",
            points = 150,
            timeLimit = 15
        ),
        Question(
            id = "rid_10",
            categoryId = "riddles",
            difficulty = Difficulty.MEDIUM,
            questionText = "I am tall when I am young, and I am short when I am old. What am I?",
            answerOptions = listOf("A tree", "A candle", "A pencil", "A human"),
            correctAnswer = "A candle",
            explanation = "A wax candle melts down and shrinks in height as it burns over time.",
            points = 150,
            timeLimit = 15
        ),
        Question(
            id = "rid_11",
            categoryId = "riddles",
            difficulty = Difficulty.HARD,
            questionText = "Forward I am heavy, but backward I am not. What am I?",
            answerOptions = listOf("A ton", "Lead", "An anchor", "A stone"),
            correctAnswer = "A ton",
            explanation = "Forward, the word is 'ton' (heavy weight). Backward, it spells 'not'.",
            points = 200,
            timeLimit = 12
        ),
        Question(
            id = "rid_12",
            categoryId = "riddles",
            difficulty = Difficulty.HARD,
            questionText = "What English word retains the same pronunciation even after removing four of its five letters?",
            answerOptions = listOf("Queue", "Empty", "Bough", "Peace"),
            correctAnswer = "Queue",
            explanation = "'Queue' is pronounced exactly like the letter 'Q'. Removing 'u-e-u-e' leaves 'Q', pronounced identical.",
            points = 200,
            timeLimit = 12
        ),
        Question(
            id = "rid_13",
            categoryId = "riddles",
            difficulty = Difficulty.HARD,
            questionText = "A man pushes his car to a hotel and tells the owner he is bankrupt. Why?",
            answerOptions = listOf(
                "His engine broke down",
                "He was playing Monopoly",
                "He ran out of gas",
                "He lost a poker bet"
            ),
            correctAnswer = "He was playing Monopoly",
            explanation = "He landed his car token on an opponent's hotel property in the board game Monopoly.",
            points = 200,
            timeLimit = 12
        ),
        Question(
            id = "rid_14",
            categoryId = "riddles",
            difficulty = Difficulty.HARD,
            questionText = "I am not alive, but I grow; I don't have lungs, but I need air; I don't have a mouth, but water kills me. What am I?",
            answerOptions = listOf("Fire", "Rust", "Ice", "Salt"),
            correctAnswer = "Fire",
            explanation = "Fire consumes fuel and oxygen (air) to grow, and water extinguishes it.",
            points = 200,
            timeLimit = 12
        ),
        Question(
            id = "rid_15",
            categoryId = "riddles",
            difficulty = Difficulty.HARD,
            questionText = "Two fathers and two sons go fishing together. Each catches one fish, yet only three fish are caught in total. How is this possible?",
            answerOptions = listOf(
                "One fish was thrown back",
                "They are grandfather, father, and son",
                "One person didn't fish",
                "One fish got away"
            ),
            correctAnswer = "They are grandfather, father, and son",
            explanation = "There are only 3 people: the grandfather (a father), his son (both a father and a son), and his grandson (a son).",
            points = 200,
            timeLimit = 12
        )
    )

    val technologyQuestions: List<Question> = listOf(
        Question(
            id = "tech_01",
            categoryId = "technology",
            difficulty = Difficulty.EASY,
            questionText = "What does the abbreviation 'CPU' stand for in computer hardware?",
            answerOptions = listOf(
                "Central Processing Unit",
                "Computer Personal Utility",
                "Central Power User",
                "Control Protocol Unit"
            ),
            correctAnswer = "Central Processing Unit",
            explanation = "The CPU (Central Processing Unit) is often referred to as the brain of the computer.",
            points = 100,
            timeLimit = 15
        ),
        Question(
            id = "tech_02",
            categoryId = "technology",
            difficulty = Difficulty.EASY,
            questionText = "Which company originally created the Android mobile operating system before acquisition?",
            answerOptions = listOf("Apple", "Android Inc.", "Microsoft", "Samsung"),
            correctAnswer = "Android Inc.",
            explanation = "Android Inc. was founded in 2003 by Andy Rubin, Rich Miner, Nick Sears, and Chris White before Google acquired it in 2005.",
            points = 100,
            timeLimit = 15
        ),
        Question(
            id = "tech_03",
            categoryId = "technology",
            difficulty = Difficulty.EASY,
            questionText = "What is the primary language used to structure content on the World Wide Web?",
            answerOptions = listOf("HTML", "Python", "SQL", "C++"),
            correctAnswer = "HTML",
            explanation = "HTML (HyperText Markup Language) is the standard markup language for documents designed to be displayed in a web browser.",
            points = 100,
            timeLimit = 15
        ),
        Question(
            id = "tech_04",
            categoryId = "technology",
            difficulty = Difficulty.EASY,
            questionText = "What type of computer memory loses its stored data when power is turned off (volatile)?",
            answerOptions = listOf("ROM", "Hard Drive", "RAM", "Flash SSD"),
            correctAnswer = "RAM",
            explanation = "RAM (Random Access Memory) is volatile working memory that clears when powered down.",
            points = 100,
            timeLimit = 15
        ),
        Question(
            id = "tech_05",
            categoryId = "technology",
            difficulty = Difficulty.EASY,
            questionText = "What does 'URL' stand for in web browsing?",
            answerOptions = listOf(
                "Uniform Resource Locator",
                "Universal Real Link",
                "United Routing Layer",
                "User Reference Log"
            ),
            correctAnswer = "Uniform Resource Locator",
            explanation = "A URL (Uniform Resource Locator) specifies the location of a web resource on a computer network.",
            points = 100,
            timeLimit = 15
        ),
        Question(
            id = "tech_06",
            categoryId = "technology",
            difficulty = Difficulty.MEDIUM,
            questionText = "In binary computation, how many individual bits make up one standard byte?",
            answerOptions = listOf("4 bits", "8 bits", "16 bits", "32 bits"),
            correctAnswer = "8 bits",
            explanation = "A byte is a unit of digital information that most commonly consists of eight bits.",
            points = 150,
            timeLimit = 15
        ),
        Question(
            id = "tech_07",
            categoryId = "technology",
            difficulty = Difficulty.MEDIUM,
            questionText = "Which networking protocol encrypts web traffic using TLS/SSL on port 443?",
            answerOptions = listOf("HTTP", "FTP", "HTTPS", "SMTP"),
            correctAnswer = "HTTPS",
            explanation = "HTTPS (Hypertext Transfer Protocol Secure) provides authenticated and encrypted communication over TLS.",
            points = 150,
            timeLimit = 15
        ),
        Question(
            id = "tech_08",
            categoryId = "technology",
            difficulty = Difficulty.MEDIUM,
            questionText = "Who is widely considered the world's first computer programmer for writing an algorithm for the Analytical Engine?",
            answerOptions = listOf("Alan Turing", "Ada Lovelace", "Charles Babbage", "Grace Hopper"),
            correctAnswer = "Ada Lovelace",
            explanation = "Ada Lovelace published the first algorithm intended to be carried out by Charles Babbage's mechanical computer in 1843.",
            points = 150,
            timeLimit = 15
        ),
        Question(
            id = "tech_09",
            categoryId = "technology",
            difficulty = Difficulty.MEDIUM,
            questionText = "What does 'SSD' stand for in modern storage technology?",
            answerOptions = listOf(
                "Solid State Drive",
                "Silicon Storage Disk",
                "Secure System Data",
                "Serial Synchronous Drive"
            ),
            correctAnswer = "Solid State Drive",
            explanation = "An SSD (Solid State Drive) uses flash memory integrated circuits without moving mechanical parts.",
            points = 150,
            timeLimit = 15
        ),
        Question(
            id = "tech_10",
            categoryId = "technology",
            difficulty = Difficulty.MEDIUM,
            questionText = "What open-source operating system kernel was created by Linus Torvalds in 1991?",
            answerOptions = listOf("Unix", "Linux", "MINIX", "BSD"),
            correctAnswer = "Linux",
            explanation = "Linus Torvalds released the Linux kernel as free, open-source software in September 1991.",
            points = 150,
            timeLimit = 15
        ),
        Question(
            id = "tech_11",
            categoryId = "technology",
            difficulty = Difficulty.HARD,
            questionText = "What is the average time complexity of searching for an element in a balanced Binary Search Tree (BST)?",
            answerOptions = listOf("O(1)", "O(log n)", "O(n)", "O(n log n)"),
            correctAnswer = "O(log n)",
            explanation = "A balanced BST halves the remaining search space with each comparison, yielding O(log n) time complexity.",
            points = 200,
            timeLimit = 12
        ),
        Question(
            id = "tech_12",
            categoryId = "technology",
            difficulty = Difficulty.HARD,
            questionText = "Which consensus mechanism does the Bitcoin blockchain network use to validate transaction blocks?",
            answerOptions = listOf("Proof of Stake (PoS)", "Proof of Work (PoW)", "Proof of Authority", "Delegated Byzantine"),
            correctAnswer = "Proof of Work (PoW)",
            explanation = "Bitcoin relies on Proof of Work (PoW) SHA-256 cryptographic mining to maintain distributed consensus.",
            points = 200,
            timeLimit = 12
        ),
        Question(
            id = "tech_13",
            categoryId = "technology",
            difficulty = Difficulty.HARD,
            questionText = "In neural networks and deep learning, what does 'backpropagation' primarily calculate?",
            answerOptions = listOf(
                "Training dataset size",
                "Gradients of the loss function with respect to weights",
                "Matrix determinant of inputs",
                "Hyperparameter learning rate"
            ),
            correctAnswer = "Gradients of the loss function with respect to weights",
            explanation = "Backpropagation calculates the gradient of the loss function using the calculus chain rule backwards from output to input.",
            points = 200,
            timeLimit = 12
        ),
        Question(
            id = "tech_14",
            categoryId = "technology",
            difficulty = Difficulty.HARD,
            questionText = "Which port number is the default for SSH (Secure Shell) remote terminal access?",
            answerOptions = listOf("Port 21", "Port 22", "Port 80", "Port 3389"),
            correctAnswer = "Port 22",
            explanation = "SSH runs on TCP port 22 by international standard definition.",
            points = 200,
            timeLimit = 12
        ),
        Question(
            id = "tech_15",
            categoryId = "technology",
            difficulty = Difficulty.HARD,
            questionText = "In cryptography, what does the 'RSA' encryption algorithm rely on as its one-way mathematical hardness assumption?",
            answerOptions = listOf(
                "Discrete logarithm problem",
                "Prime factorization of large composite integers",
                "Elliptic curve point multiplication",
                "Lattice shortest vector problem"
            ),
            correctAnswer = "Prime factorization of large composite integers",
            explanation = "RSA security is based on the practical difficulty of factoring the product of two large prime numbers.",
            points = 200,
            timeLimit = 12
        )
    )
}
