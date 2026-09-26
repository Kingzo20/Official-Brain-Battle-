package com.example.data.auth

enum class AuthState {
    AUTHENTICATING,
    AUTHENTICATED,
    UNAUTHENTICATED,
    AUTH_ERROR
}

data class AuthUser(
    val uid: String,
    val email: String,
    val username: String,
    val displayName: String = "",
    val photoUrl: String? = null,
    val isAnonymous: Boolean = false,
    val isEmailVerified: Boolean = false,
    val providerId: String = "password",
    val role: String = "CUSTOMER / PIONEER",
    val authType: String = "PASSWORD"
)

sealed class AuthResult<out T> {
    data class Success<out T>(val data: T) : AuthResult<T>()
    data class Error(val message: String, val code: String? = null) : AuthResult<Nothing>()
}

enum class UsernameValidationStatus {
    VALID,
    TOO_SHORT,
    TOO_LONG,
    INVALID_CHARACTERS,
    TAKEN,
    EMPTY
}

data class AuthConfigurationStatus(
    val isFirebaseInitialized: Boolean,
    val isGoogleSignInConfigured: Boolean,
    val isEmailVerificationActive: Boolean,
    val activeProviderMode: String,
    val googleWebClientId: String? = null,
    val notes: List<String> = emptyList()
)
