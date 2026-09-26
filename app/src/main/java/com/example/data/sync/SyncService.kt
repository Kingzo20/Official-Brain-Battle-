package com.example.data.sync

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import com.example.data.auth.AuthService
import com.example.data.firestore.FirestoreRepository
import com.example.data.local.LocalStorageRepository
import com.example.model.SyncState
import com.example.model.UserProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SyncService(
    private val context: Context,
    private val authService: AuthService,
    private val firestoreRepo: FirestoreRepository,
    private val localRepo: LocalStorageRepository,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    private val tag = "SyncService"

    private val _isOnline = MutableStateFlow(checkInitialConnectivity())
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private val _syncState = MutableStateFlow(if (_isOnline.value) SyncState.IDLE else SyncState.OFFLINE)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    init {
        registerNetworkCallback()
    }

    private fun checkInitialConnectivity(): Boolean {
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            val network = cm?.activeNetwork ?: return false
            val caps = cm.getNetworkCapabilities(network) ?: return false
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } catch (e: Exception) {
            false
        }
    }

    private fun registerNetworkCallback() {
        try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()

            cm.registerNetworkCallback(request, object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    _isOnline.value = true
                    coroutineScope.launch {
                        _syncState.value = SyncState.IDLE
                        syncNow()
                    }
                }

                override fun onLost(network: Network) {
                    _isOnline.value = false
                    _syncState.value = SyncState.OFFLINE
                }
            })
        } catch (e: Exception) {
            Log.w(tag, "Failed to register network callback: ${e.message}")
        }
    }

    suspend fun syncNow(): Boolean = withContext(Dispatchers.IO) {
        val user = authService.currentUser.value
        if (user == null) {
            _syncState.value = SyncState.IDLE
            return@withContext false
        }

        if (!_isOnline.value) {
            _syncState.value = SyncState.OFFLINE
            return@withContext false
        }

        if (!firestoreRepo.isAvailable) {
            _syncState.value = SyncState.IDLE
            return@withContext false
        }

        _syncState.value = SyncState.SYNCING

        try {
            // 1. Process pending offline sync queue
            val pending = localRepo.getPendingSyncQueue()
            val successfulIds = mutableSetOf<String>()

            for (record in pending) {
                val success = firestoreRepo.saveGameHistory(user.uid, record)
                if (success) {
                    successfulIds.add(record.gameId)
                }
            }

            if (successfulIds.isNotEmpty()) {
                localRepo.removePendingSync(successfulIds)
            }

            // 2. Fetch and merge cloud profile
            val cloudProfile = firestoreRepo.getUserProfile(user.uid)
            val localProfile = localRepo.loadProfile()

            if (cloudProfile != null) {
                // Merge strategy:
                // Non-overwriting conflict resolution
                val mergedTotalXp = maxOf(localProfile.totalXp, cloudProfile.totalXp)
                val mergedBestScore = maxOf(localProfile.bestScore, cloudProfile.bestScore)
                val mergedStreak = maxOf(localProfile.currentStreak, cloudProfile.currentStreak)
                val mergedGames = maxOf(localProfile.gamesPlayed, cloudProfile.gamesPlayed)

                val mergedProfile = localProfile.copy(
                    uid = user.uid,
                    username = if (cloudProfile.username.isNotBlank()) cloudProfile.username else localProfile.username,
                    displayName = if (cloudProfile.displayName.isNotBlank()) cloudProfile.displayName else localProfile.displayName,
                    email = if (user.email.isNotBlank()) user.email else cloudProfile.email,
                    avatarEmoji = if (cloudProfile.avatarEmoji.isNotBlank()) cloudProfile.avatarEmoji else localProfile.avatarEmoji,
                    totalXp = mergedTotalXp,
                    bestScore = mergedBestScore,
                    currentStreak = mergedStreak,
                    gamesPlayed = mergedGames,
                    lastDailyCompletedDate = cloudProfile.lastDailyCompletedDate ?: localProfile.lastDailyCompletedDate,
                    dailyChallengeCompleted = cloudProfile.dailyChallengeCompleted || localProfile.dailyChallengeCompleted,
                    updatedAt = System.currentTimeMillis()
                )

                localRepo.saveProfile(mergedProfile)
                firestoreRepo.saveUserProfile(mergedProfile)
            } else {
                // Initial upload of local profile to Firestore
                val initialProfile = localProfile.copy(
                    uid = user.uid,
                    email = user.email,
                    username = user.username,
                    displayName = user.displayName.ifBlank { user.username }
                )
                firestoreRepo.saveUserProfile(initialProfile)
                localRepo.saveProfile(initialProfile)
            }

            // 3. Sync settings
            val settings = localRepo.loadSettings()
            firestoreRepo.saveSettings(user.uid, settings)

            _syncState.value = SyncState.SYNC_COMPLETED
            coroutineScope.launch {
                delay(2000)
                if (_syncState.value == SyncState.SYNC_COMPLETED) {
                    _syncState.value = SyncState.IDLE
                }
            }
            true
        } catch (e: Exception) {
            Log.e(tag, "Sync error: ${e.message}", e)
            _syncState.value = SyncState.ERROR
            false
        }
    }

    fun setSavingState() {
        _syncState.value = SyncState.SAVING
    }

    fun setIdleState() {
        _syncState.value = if (_isOnline.value) SyncState.IDLE else SyncState.OFFLINE
    }
}
