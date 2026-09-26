package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ads.AdMobManager
import com.example.data.GameRepository
import com.example.data.social.FriendChallengeRecord
import com.example.model.Difficulty
import com.example.model.GameCategory
import com.example.model.GameModeType
import com.example.model.GameResult
import com.example.ui.navigation.BrainBattleBottomBar
import com.example.ui.navigation.Screen
import com.example.ui.screens.*
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Google Mobile Ads SDK (DEVELOPMENT / TEST ADS MODE)
        AdMobManager.initialize(this)

        setContent {
            MyApplicationTheme {
                val context = LocalContext.current
                val coroutineScope = rememberCoroutineScope()
                val repository = remember { GameRepository(context.applicationContext) }

                var currentScreen by remember { mutableStateOf<Screen>(Screen.Splash) }
                var activeGameMode by remember { mutableStateOf(GameModeType.CATEGORY_BATTLE) }
                var activeCategory by remember { mutableStateOf(GameCategory.MATH) }
                var activeDifficulty by remember { mutableStateOf(Difficulty.MEDIUM) }
                var currentResult by remember { mutableStateOf<GameResult?>(null) }
                var activeChallengeRecord by remember { mutableStateOf<FriendChallengeRecord?>(null) }

                val userProfile by repository.userProfile.collectAsState()
                LaunchedEffect(userProfile.isPremium) {
                    AdMobManager.isUserPremium = userProfile.isPremium
                }

                // Deep linking handler for Friend Challenge invites
                LaunchedEffect(intent?.data) {
                    val dataUri = intent?.data
                    if (dataUri != null) {
                        val path = dataUri.path ?: ""
                        val code = when {
                            dataUri.scheme == "brainbattle" && dataUri.host == "challenge" -> path.trim('/')
                            dataUri.host == "brainbattle.game" && path.startsWith("/challenge/") -> path.removePrefix("/challenge/")
                            else -> ""
                        }
                        if (code.isNotBlank()) {
                            val challenge = repository.socialRepository.getChallengeByCode(code)
                            if (challenge != null) {
                                repository.socialRepository.acceptChallenge(challenge.challengeId)
                                activeChallengeRecord = challenge
                                activeGameMode = GameModeType.FRIEND_CHALLENGE
                                activeCategory = GameCategory.values().firstOrNull { it.id == challenge.category } ?: GameCategory.MATH
                                activeDifficulty = try { Difficulty.valueOf(challenge.difficulty) } catch (e: Exception) { Difficulty.MEDIUM }
                                currentScreen = Screen.Game
                            }
                        }
                    }
                }

                // System Back Handling for Top-level tabs
                BackHandler(
                    enabled = currentScreen != Screen.Home &&
                              currentScreen != Screen.Splash &&
                              currentScreen != Screen.Onboarding &&
                              currentScreen != Screen.Auth
                ) {
                    when (currentScreen) {
                        Screen.Play, Screen.Leaderboard, Screen.Social, Screen.Achievements, Screen.Profile -> {
                            currentScreen = Screen.Home
                        }
                        is Screen.MatchResult -> {
                            currentScreen = Screen.Social
                        }
                        Screen.CategorySelection -> {
                            currentScreen = Screen.Play
                        }
                        Screen.DifficultySelection -> {
                            currentScreen = Screen.CategorySelection
                        }
                        Screen.Results -> {
                            currentScreen = Screen.Home
                        }
                        Screen.Notifications -> {
                            currentScreen = Screen.Home
                        }
                        Screen.Settings -> {
                            currentScreen = Screen.Profile
                        }
                        Screen.FriendChallenge -> {
                            currentScreen = Screen.Play
                        }
                        Screen.Tournament -> {
                            currentScreen = Screen.Play
                        }
                        Screen.Statistics -> {
                            currentScreen = Screen.Home
                        }
                        else -> {
                            currentScreen = Screen.Home
                        }
                    }
                }

                Scaffold(
                    containerColor = BackgroundDark,
                    bottomBar = {
                        BrainBattleBottomBar(
                            currentScreen = currentScreen,
                            onTabSelected = { tab ->
                                currentScreen = tab.screen
                            }
                        )
                    },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (currentScreen) {
                            Screen.Splash -> {
                                SplashScreen(
                                    onSplashFinished = {
                                        val isAuthenticated = repository.authService?.isAuthenticated == true &&
                                                repository.userProfile.value.uid.isNotBlank() &&
                                                repository.userProfile.value.username != "Guest"
                                        val hasSeenOnboarding = repository.localRepo?.hasCompletedOnboarding() == true
                                        currentScreen = when {
                                            isAuthenticated -> Screen.Home
                                            hasSeenOnboarding -> Screen.Auth
                                            else -> Screen.Onboarding
                                        }
                                    }
                                )
                            }
                            Screen.Onboarding -> {
                                OnboardingScreen(
                                    onComplete = {
                                        repository.localRepo?.setOnboardingCompleted(true)
                                        currentScreen = Screen.Auth
                                    }
                                )
                            }
                            Screen.Auth -> {
                                AuthScreen(
                                    authService = repository.authService,
                                    onAuthSuccess = { username ->
                                        repository.localRepo?.setOnboardingCompleted(true)
                                        currentScreen = Screen.Home
                                    }
                                )
                            }
                            Screen.Home -> {
                                HomeScreen(
                                    repository = repository,
                                    onPlayDailyChallenge = {
                                        activeGameMode = GameModeType.DAILY_CHALLENGE
                                        activeCategory = GameCategory.DAILY
                                        activeDifficulty = Difficulty.MEDIUM
                                        currentScreen = Screen.Game
                                    },
                                    onQuickPlaySelected = { category ->
                                        activeGameMode = GameModeType.CATEGORY_BATTLE
                                        activeCategory = category
                                        currentScreen = Screen.DifficultySelection
                                    },
                                    onNotificationsClick = {
                                        currentScreen = Screen.Notifications
                                    },
                                    onProfileClick = {
                                        currentScreen = Screen.Profile
                                    },
                                    onViewAllAchievements = {
                                        currentScreen = Screen.Achievements
                                    },
                                    onNavigateToStatistics = {
                                        currentScreen = Screen.Statistics
                                    },
                                    onStartPractice = { cat, diff ->
                                        activeGameMode = GameModeType.TARGETED_PRACTICE
                                        activeCategory = cat
                                        activeDifficulty = diff
                                        currentScreen = Screen.Game
                                    }
                                )
                            }
                            Screen.Play -> {
                                PlayScreen(
                                    onModeSelected = { mode ->
                                        activeGameMode = mode
                                        when (mode) {
                                            GameModeType.DAILY_CHALLENGE -> {
                                                activeCategory = GameCategory.DAILY
                                                activeDifficulty = Difficulty.MEDIUM
                                                currentScreen = Screen.Game
                                            }
                                            GameModeType.SIXTY_SECOND_RUSH -> {
                                                activeCategory = GameCategory.SPEED
                                                activeDifficulty = Difficulty.HARD
                                                currentScreen = Screen.Game
                                            }
                                            GameModeType.ENDLESS_MODE -> {
                                                activeCategory = GameCategory.LOGIC
                                                activeDifficulty = Difficulty.MEDIUM
                                                currentScreen = Screen.Game
                                            }
                                            GameModeType.CATEGORY_BATTLE -> {
                                                currentScreen = Screen.CategorySelection
                                            }
                                            GameModeType.FRIEND_CHALLENGE -> {
                                                currentScreen = Screen.FriendChallenge
                                            }
                                            GameModeType.TOURNAMENTS -> {
                                                currentScreen = Screen.Tournament
                                            }
                                            GameModeType.TARGETED_PRACTICE -> {
                                                currentScreen = Screen.Statistics
                                            }
                                        }
                                    }
                                )
                            }
                            Screen.CategorySelection -> {
                                CategoryScreen(
                                    onCategorySelected = { category ->
                                        activeCategory = category
                                        currentScreen = Screen.DifficultySelection
                                    },
                                    onBack = {
                                        currentScreen = Screen.Play
                                    }
                                )
                            }
                            Screen.DifficultySelection -> {
                                DifficultyScreen(
                                    category = activeCategory,
                                    onDifficultySelected = { difficulty ->
                                        activeDifficulty = difficulty
                                        currentScreen = Screen.Game
                                    },
                                    onBack = {
                                        currentScreen = Screen.CategorySelection
                                    }
                                )
                            }
                            Screen.Game -> {
                                GameScreen(
                                    category = activeCategory,
                                    difficulty = activeDifficulty,
                                    gameMode = activeGameMode,
                                    activeChallenge = activeChallengeRecord,
                                    repository = repository,
                                    onGameFinished = { result ->
                                        currentResult = result
                                        currentScreen = Screen.Results
                                    },
                                    onExitGame = {
                                        activeChallengeRecord = null
                                        currentScreen = Screen.Home
                                    }
                                )
                            }
                            Screen.Results -> {
                                val result = currentResult ?: repository.lastGameResult.value ?: GameResult(
                                    score = 870,
                                    correctCount = 8,
                                    totalQuestions = 10,
                                    accuracyPercentage = 80,
                                    timeSpentSeconds = 54,
                                    xpEarned = 180,
                                    currentStreakDays = 7,
                                    categoryTitle = activeCategory.title
                                )
                                val currentChallenge = activeChallengeRecord
                                GameResultScreen(
                                    result = result,
                                    onPlayAgain = {
                                        AdMobManager.showInterstitial(this@MainActivity) {
                                            currentScreen = Screen.Game
                                        }
                                    },
                                    onNextChallenge = {
                                        AdMobManager.showInterstitial(this@MainActivity) {
                                            val allCats = GameCategory.values()
                                            val nextIndex = (activeCategory.ordinal + 1) % allCats.size
                                            activeCategory = allCats[nextIndex]
                                            activeChallengeRecord = null
                                            currentScreen = Screen.DifficultySelection
                                        }
                                    },
                                    onBackToHome = {
                                        AdMobManager.showInterstitial(this@MainActivity) {
                                            activeChallengeRecord = null
                                            currentScreen = Screen.Home
                                        }
                                    },
                                    onViewMatchResult = if (currentChallenge != null) {
                                        {
                                            currentScreen = Screen.MatchResult(currentChallenge.challengeId)
                                        }
                                    } else null,
                                    onRewardEarnedDoubleXp = { bonusXp ->
                                        repository.awardXp(bonusXp)
                                    }
                                )
                            }
                            Screen.Leaderboard -> {
                                LeaderboardScreen(
                                    repository = repository,
                                    onNavigateToProfile = {
                                        currentScreen = Screen.Profile
                                    }
                                )
                            }
                            Screen.Social -> {
                                SocialScreen(
                                    repository = repository,
                                    onStartChallengeGame = { challenge ->
                                        activeChallengeRecord = challenge
                                        activeGameMode = GameModeType.FRIEND_CHALLENGE
                                        activeCategory = GameCategory.values().firstOrNull { it.id == challenge.category } ?: GameCategory.MATH
                                        activeDifficulty = try { Difficulty.valueOf(challenge.difficulty) } catch (e: Exception) { Difficulty.MEDIUM }
                                        currentScreen = Screen.Game
                                    },
                                    onViewMatchResult = { challengeId ->
                                        currentScreen = Screen.MatchResult(challengeId)
                                    }
                                )
                            }
                            is Screen.MatchResult -> {
                                val matchScreen = currentScreen as Screen.MatchResult
                                MatchResultScreen(
                                    challengeId = matchScreen.challengeId,
                                    repository = repository,
                                    onRematch = { rematch ->
                                        activeChallengeRecord = rematch
                                        activeGameMode = GameModeType.FRIEND_CHALLENGE
                                        activeCategory = GameCategory.values().firstOrNull { it.id == rematch.category } ?: GameCategory.MATH
                                        activeDifficulty = try { Difficulty.valueOf(rematch.difficulty) } catch (e: Exception) { Difficulty.MEDIUM }
                                        currentScreen = Screen.Game
                                    },
                                    onBack = {
                                        currentScreen = Screen.Social
                                    }
                                )
                            }
                            Screen.Achievements -> {
                                AchievementsScreen(repository = repository)
                            }
                            Screen.Profile -> {
                                ProfileScreen(
                                    repository = repository,
                                    onNavigateToSettings = {
                                        currentScreen = Screen.Settings
                                    },
                                    onNavigateToAchievements = {
                                        currentScreen = Screen.Achievements
                                    },
                                    onNavigateToStatistics = {
                                        currentScreen = Screen.Statistics
                                    }
                                )
                            }
                            Screen.Statistics -> {
                                StatisticsScreen(
                                    repository = repository,
                                    onNavigateBack = {
                                        currentScreen = Screen.Home
                                    },
                                    onStartPractice = { cat, diff ->
                                        activeGameMode = GameModeType.TARGETED_PRACTICE
                                        activeCategory = cat
                                        activeDifficulty = diff
                                        currentScreen = Screen.Game
                                    }
                                )
                            }
                            Screen.Settings -> {
                                SettingsScreen(
                                    repository = repository,
                                    onBack = {
                                        currentScreen = Screen.Profile
                                    },
                                    onLogout = {
                                        currentScreen = Screen.Auth
                                    }
                                )
                            }
                            Screen.FriendChallenge -> {
                                FriendChallengeScreen(
                                    repository = repository,
                                    onPlayChallenge = { code ->
                                        coroutineScope.launch {
                                            val challenge = repository.socialRepository.getChallengeByCode(code)
                                            if (challenge != null) {
                                                repository.socialRepository.acceptChallenge(challenge.challengeId)
                                                activeChallengeRecord = challenge
                                                activeGameMode = GameModeType.FRIEND_CHALLENGE
                                                activeCategory = GameCategory.values().firstOrNull { it.id == challenge.category } ?: GameCategory.MATH
                                                activeDifficulty = try { Difficulty.valueOf(challenge.difficulty) } catch (e: Exception) { Difficulty.MEDIUM }
                                                currentScreen = Screen.Game
                                            } else {
                                                activeCategory = GameCategory.MATH
                                                activeDifficulty = Difficulty.HARD
                                                activeGameMode = GameModeType.FRIEND_CHALLENGE
                                                currentScreen = Screen.Game
                                            }
                                        }
                                    },
                                    onBack = {
                                        currentScreen = Screen.Play
                                    }
                                )
                            }
                            Screen.Tournament -> {
                                TournamentScreen(
                                    repository = repository,
                                    onJoinTournament = {
                                        activeCategory = GameCategory.SPEED
                                        activeDifficulty = Difficulty.HARD
                                        currentScreen = Screen.Game
                                    },
                                    onBack = {
                                        currentScreen = Screen.Play
                                    }
                                )
                            }
                            Screen.Notifications -> {
                                NotificationsScreen(
                                    repository = repository,
                                    onNavigateToSocial = {
                                        currentScreen = Screen.Social
                                    },
                                    onViewMatchResult = { matchId ->
                                        currentScreen = Screen.MatchResult(matchId)
                                    },
                                    onBack = {
                                        currentScreen = Screen.Home
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
