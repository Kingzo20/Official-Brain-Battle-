package com.example.data.auth

import kotlinx.coroutines.flow.StateFlow

interface AuthService {
    val currentUser: StateFlow<AuthUser?>
    val authState: StateFlow<AuthState>
    val isFirebaseConfigured: Boolean
    val isAuthenticated: Boolean

    suspend fun registerWithEmail(
        email: String,
        password: String,
        username: String
    ): AuthResult<AuthUser>

    suspend fun loginWithEmail(
        email: String,
        password: String
    ): AuthResult<AuthUser>

    suspend fun signInWithGoogle(idToken: String): AuthResult<AuthUser>

    suspend fun signInOrSignUpWithGoogleAccount(
        email: String,
        displayName: String,
        photoUrl: String? = null
    ): AuthResult<AuthUser>

    suspend fun sendEmailVerification(): AuthResult<Unit>

    suspend fun reloadUser(): AuthResult<Boolean>

    suspend fun sendPasswordResetEmail(email: String): AuthResult<Unit>

    suspend fun signOut(): AuthResult<Unit>

    suspend fun deleteAccount(): AuthResult<Unit>

    suspend fun checkUsernameAvailability(username: String): UsernameValidationStatus

    fun getConfigurationStatus(): AuthConfigurationStatus

    fun getGoogleWebClientId(): String?

    fun getRegisteredGoogleAccounts(): List<StoredUserAccount>
}
