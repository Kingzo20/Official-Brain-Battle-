package com.example.data.auth

import android.content.Context
import android.util.Log
import com.example.data.local.LocalStorageRepository
import com.example.model.UserProfile
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FirebaseAuthServiceImpl(
    private val context: Context,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) : AuthService {

    private val tag = "FirebaseAuthService"

    companion object {
        const val FIREBASE_PROJECT_ID = "brain-battle-21d8b"
        const val FIREBASE_APPLICATION_ID = "1:906231122938:android:7d6a9c9693d19199113243"
        const val FIREBASE_API_KEY = "AIzaSyCVCcRkbrJxsUa5Q0uMQqUPCjXMrFYqp1c"
        const val FIREBASE_GCM_SENDER_ID = "906231122938"
        const val FIREBASE_STORAGE_BUCKET = "brain-battle-21d8b.firebasestorage.app"
    }

    val userStore = PersistentUserAccountStore(context)
    private val localRepo = LocalStorageRepository(context)
    private val prefs = context.getSharedPreferences("brain_battle_auth", Context.MODE_PRIVATE)

    private val auth: FirebaseAuth = try {
        val appContext = context.applicationContext ?: context
        if (FirebaseApp.getApps(appContext).isEmpty()) {
            val options = FirebaseOptions.Builder()
                .setApplicationId(FIREBASE_APPLICATION_ID)
                .setApiKey(FIREBASE_API_KEY)
                .setProjectId(FIREBASE_PROJECT_ID)
                .setGcmSenderId(FIREBASE_GCM_SENDER_ID)
                .setStorageBucket(FIREBASE_STORAGE_BUCKET)
                .build()
            FirebaseApp.initializeApp(appContext, options)
        }
        FirebaseAuth.getInstance()
    } catch (e: Exception) {
        Log.e(tag, "Error getting FirebaseAuth instance", e)
        FirebaseAuth.getInstance()
    }

    private val firestore: FirebaseFirestore? = try {
        FirebaseFirestore.getInstance()
    } catch (e: Exception) {
        Log.e(tag, "Error getting FirebaseFirestore instance", e)
        null
    }

    override val isFirebaseConfigured: Boolean = true

    private val _currentUser = MutableStateFlow<AuthUser?>(null)
    override val currentUser: StateFlow<AuthUser?> = _currentUser.asStateFlow()

    private val _authState = MutableStateFlow<AuthState>(AuthState.AUTHENTICATING)
    override val authState: StateFlow<AuthState> = _authState.asStateFlow()

    override val isAuthenticated: Boolean
        get() = _currentUser.value != null

    init {
        // Clear out any old mock user cache or false local collisions
        userStore.clearAllAccounts()
        restoreSession()
    }

    private fun restoreSession() {
        auth.addAuthStateListener { firebaseAuth ->
            val fbUser = firebaseAuth.currentUser
            if (fbUser != null) {
                val savedUsername = prefs.getString("user_username_${fbUser.uid}", null)
                    ?: fbUser.displayName?.ifBlank { null }
                    ?: fbUser.email?.substringBefore("@")
                    ?: "Player"

                val isGoogle = fbUser.providerData.any { it.providerId == "google.com" }
                val authUser = AuthUser(
                    uid = fbUser.uid,
                    email = PersistentUserAccountStore.normalizeEmail(fbUser.email ?: ""),
                    username = savedUsername,
                    displayName = fbUser.displayName ?: savedUsername,
                    photoUrl = fbUser.photoUrl?.toString(),
                    isAnonymous = fbUser.isAnonymous,
                    isEmailVerified = fbUser.isEmailVerified,
                    providerId = fbUser.providerData.firstOrNull()?.providerId ?: "firebase",
                    role = "CUSTOMER / PIONEER",
                    authType = if (isGoogle) "GOOGLE_OAUTH" else "PASSWORD"
                )
                localRepo.setActiveSessionUid(fbUser.uid)
                _currentUser.value = authUser
                _authState.value = AuthState.AUTHENTICATED
            } else {
                localRepo.setActiveSessionUid(null)
                _currentUser.value = null
                _authState.value = AuthState.UNAUTHENTICATED
            }
        }
    }

    override suspend fun checkUsernameAvailability(username: String): UsernameValidationStatus {
        val trimmed = username.trim()
        if (trimmed.isEmpty()) return UsernameValidationStatus.EMPTY
        if (trimmed.length < 3) return UsernameValidationStatus.TOO_SHORT
        if (trimmed.length > 20) return UsernameValidationStatus.TOO_LONG
        val validChars = Regex("^[a-zA-Z0-9_]+$")
        if (!validChars.matches(trimmed)) return UsernameValidationStatus.INVALID_CHARACTERS

        if (firestore != null) {
            return try {
                val doc = firestore.collection("usernames")
                    .document(trimmed.lowercase())
                    .get()
                    .await()
                if (doc.exists()) {
                    val ownerUid = doc.getString("uid")
                    if (ownerUid != null && ownerUid == auth.currentUser?.uid) {
                        UsernameValidationStatus.VALID
                    } else {
                        UsernameValidationStatus.TAKEN
                    }
                } else {
                    UsernameValidationStatus.VALID
                }
            } catch (e: Exception) {
                Log.w(tag, "Firestore username check warning: ${e.message}")
                UsernameValidationStatus.VALID
            }
        }
        return UsernameValidationStatus.VALID
    }

    override suspend fun registerWithEmail(
        email: String,
        password: String,
        username: String
    ): AuthResult<AuthUser> = withContext(Dispatchers.IO) {
        _authState.value = AuthState.AUTHENTICATING
        val normalizedEmail = PersistentUserAccountStore.normalizeEmail(email)
        val trimmedUsername = username.trim()

        if (normalizedEmail.isEmpty() || !normalizedEmail.contains("@")) {
            _authState.value = AuthState.AUTH_ERROR
            return@withContext AuthResult.Error("[ERROR_INVALID_EMAIL] Please enter a valid email address.")
        }

        if (password.length < 6) {
            _authState.value = AuthState.AUTH_ERROR
            return@withContext AuthResult.Error("[ERROR_WEAK_PASSWORD] Password must be at least 6 characters.")
        }

        val usernameStatus = checkUsernameAvailability(trimmedUsername)
        if (usernameStatus != UsernameValidationStatus.VALID) {
            _authState.value = AuthState.AUTH_ERROR
            val msg = when (usernameStatus) {
                UsernameValidationStatus.EMPTY -> "Username cannot be empty"
                UsernameValidationStatus.TOO_SHORT -> "Username must be at least 3 characters"
                UsernameValidationStatus.TOO_LONG -> "Username cannot exceed 20 characters"
                UsernameValidationStatus.INVALID_CHARACTERS -> "Username can only contain letters, numbers, and underscores"
                UsernameValidationStatus.TAKEN -> "Username '$trimmedUsername' is already taken. Please choose another."
                UsernameValidationStatus.VALID -> ""
            }
            return@withContext AuthResult.Error(msg)
        }

        // DIRECT LIVE FIREBASE REGISTRATION (No local interceptors or mock blockers)
        try {
            Log.d(tag, "Calling FirebaseAuth.createUserWithEmailAndPassword for $normalizedEmail on project $FIREBASE_PROJECT_ID")
            val result = auth.createUserWithEmailAndPassword(normalizedEmail, password).await()
            val firebaseUser = result.user ?: run {
                _authState.value = AuthState.AUTH_ERROR
                return@withContext AuthResult.Error("[AUTH_ERROR] Firebase failed to create user record.")
            }

            // Update display name
            try {
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(trimmedUsername)
                    .build()
                firebaseUser.updateProfile(profileUpdates).await()
            } catch (e: Exception) {
                Log.w(tag, "Failed to set display name on FirebaseUser: ${e.message}")
            }

            // Send verification email
            try {
                firebaseUser.sendEmailVerification().await()
            } catch (e: Exception) {
                Log.w(tag, "Send email verification warning: ${e.message}")
            }

            // Save preferences
            prefs.edit().putString("user_username_${firebaseUser.uid}", trimmedUsername).apply()
            localRepo.setActiveSessionUid(firebaseUser.uid)

            val authUser = AuthUser(
                uid = firebaseUser.uid,
                email = firebaseUser.email ?: normalizedEmail,
                username = trimmedUsername,
                displayName = trimmedUsername,
                isEmailVerified = firebaseUser.isEmailVerified,
                providerId = "password",
                role = "CUSTOMER / PIONEER"
            )
            _currentUser.value = authUser
            _authState.value = AuthState.AUTHENTICATED
            AuthResult.Success(authUser)
        } catch (e: FirebaseAuthException) {
            val errorCode = e.errorCode
            val errorMsg = e.localizedMessage ?: e.message ?: "Registration failed."
            Log.e(tag, "Firebase register exception: [$errorCode] $errorMsg", e)
            _authState.value = AuthState.AUTH_ERROR
            val displayMessage = when (errorCode) {
                "ERROR_EMAIL_ALREADY_IN_USE" -> "[$errorCode] An account with this email already exists in Firebase. Please sign in."
                "ERROR_WEAK_PASSWORD" -> "[$errorCode] Password is too weak. Please use at least 6 characters."
                "ERROR_INVALID_EMAIL" -> "[$errorCode] The email address is badly formatted."
                "ERROR_OPERATION_NOT_ALLOWED" -> "[$errorCode] Email/Password sign-up is disabled in Firebase Console."
                else -> "[$errorCode] $errorMsg"
            }
            AuthResult.Error(displayMessage)
        } catch (e: Exception) {
            val errorCode = (e as? FirebaseAuthException)?.errorCode ?: "REGISTRATION_ERROR"
            val errorMsg = e.localizedMessage ?: e.message ?: "Registration failed. Check network connection."
            Log.e(tag, "Registration error: [$errorCode] $errorMsg", e)
            _authState.value = AuthState.AUTH_ERROR
            AuthResult.Error("[$errorCode] $errorMsg")
        }
    }

    override suspend fun loginWithEmail(
        email: String,
        password: String
    ): AuthResult<AuthUser> = withContext(Dispatchers.IO) {
        _authState.value = AuthState.AUTHENTICATING
        val normalizedEmail = PersistentUserAccountStore.normalizeEmail(email)

        if (normalizedEmail.isEmpty() || !normalizedEmail.contains("@")) {
            _authState.value = AuthState.AUTH_ERROR
            return@withContext AuthResult.Error("[ERROR_INVALID_EMAIL] Please enter a valid email address.")
        }

        if (password.isEmpty()) {
            _authState.value = AuthState.AUTH_ERROR
            return@withContext AuthResult.Error("[ERROR_WRONG_PASSWORD] Password cannot be empty.")
        }

        // DIRECT LIVE FIREBASE LOGIN (No local interceptors or mock passwords)
        try {
            Log.d(tag, "Calling FirebaseAuth.signInWithEmailAndPassword for $normalizedEmail on project $FIREBASE_PROJECT_ID")
            val result = auth.signInWithEmailAndPassword(normalizedEmail, password).await()
            val firebaseUser = result.user ?: run {
                _authState.value = AuthState.AUTH_ERROR
                return@withContext AuthResult.Error("[AUTH_ERROR] Firebase user record not found.")
            }

            val savedUsername = prefs.getString("user_username_${firebaseUser.uid}", null)
                ?: firebaseUser.displayName?.ifBlank { null }
                ?: firebaseUser.email?.substringBefore("@")
                ?: "Player"

            localRepo.setActiveSessionUid(firebaseUser.uid)

            val authUser = AuthUser(
                uid = firebaseUser.uid,
                email = firebaseUser.email ?: normalizedEmail,
                username = savedUsername,
                displayName = firebaseUser.displayName ?: savedUsername,
                photoUrl = firebaseUser.photoUrl?.toString(),
                isEmailVerified = firebaseUser.isEmailVerified,
                providerId = "password",
                role = "CUSTOMER / PIONEER"
            )
            _currentUser.value = authUser
            _authState.value = AuthState.AUTHENTICATED
            AuthResult.Success(authUser)
        } catch (e: FirebaseAuthException) {
            val errorCode = e.errorCode
            val errorMsg = e.localizedMessage ?: e.message ?: "Sign-in failed."
            Log.e(tag, "Firebase login exception: [$errorCode] $errorMsg", e)
            _authState.value = AuthState.AUTH_ERROR
            val displayMessage = when (errorCode) {
                "ERROR_USER_NOT_FOUND" -> "[$errorCode] No account found with this email in Firebase. Please sign up."
                "ERROR_WRONG_PASSWORD" -> "[$errorCode] Incorrect password. Please try again."
                "ERROR_INVALID_EMAIL" -> "[$errorCode] The email address is badly formatted."
                "ERROR_USER_DISABLED" -> "[$errorCode] This user account has been disabled in Firebase."
                "ERROR_TOO_MANY_REQUESTS" -> "[$errorCode] Access blocked due to unusual activity. Try again later."
                "ERROR_INVALID_CREDENTIAL" -> "[$errorCode] Invalid credentials. Check your email and password."
                else -> "[$errorCode] $errorMsg"
            }
            AuthResult.Error(displayMessage)
        } catch (e: Exception) {
            val errorCode = (e as? FirebaseAuthException)?.errorCode ?: "LOGIN_ERROR"
            val errorMsg = e.localizedMessage ?: e.message ?: "Login failed. Please check network connection."
            Log.e(tag, "Login error: [$errorCode] $errorMsg", e)
            _authState.value = AuthState.AUTH_ERROR
            AuthResult.Error("[$errorCode] $errorMsg")
        }
    }

    override suspend fun signInWithGoogle(idToken: String): AuthResult<AuthUser> = withContext(Dispatchers.IO) {
        _authState.value = AuthState.AUTHENTICATING
        if (idToken.isBlank()) {
            _authState.value = AuthState.AUTH_ERROR
            return@withContext AuthResult.Error("[ERROR_INVALID_CREDENTIAL] Google ID token was empty")
        }

        try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = auth.signInWithCredential(credential).await()
            val firebaseUser = result.user ?: run {
                _authState.value = AuthState.AUTH_ERROR
                return@withContext AuthResult.Error("[AUTH_ERROR] Google Sign-In failed to produce user record")
            }

            val displayName = firebaseUser.displayName ?: firebaseUser.email?.substringBefore("@") ?: "Player"
            val username = prefs.getString("user_username_${firebaseUser.uid}", null)
                ?: displayName.filter { it.isLetterOrDigit() || it == '_' }.ifBlank { "Player" }
            val photoUrl = firebaseUser.photoUrl?.toString() ?: "https://lh3.googleusercontent.com/a/default-user"

            localRepo.setActiveSessionUid(firebaseUser.uid)

            val authUser = AuthUser(
                uid = firebaseUser.uid,
                email = PersistentUserAccountStore.normalizeEmail(firebaseUser.email ?: ""),
                username = username,
                displayName = displayName,
                photoUrl = photoUrl,
                isEmailVerified = firebaseUser.isEmailVerified,
                providerId = "google.com",
                role = "CUSTOMER / PIONEER",
                authType = "GOOGLE_OAUTH"
            )
            _currentUser.value = authUser
            _authState.value = AuthState.AUTHENTICATED
            AuthResult.Success(authUser)
        } catch (e: FirebaseAuthException) {
            val errorCode = e.errorCode
            val errorMsg = e.localizedMessage ?: e.message ?: "Google Sign-In failed."
            Log.e(tag, "FirebaseAuthException Google Sign-In: [$errorCode] $errorMsg", e)
            _authState.value = AuthState.AUTH_ERROR
            AuthResult.Error("[$errorCode] $errorMsg")
        } catch (e: Exception) {
            val errorCode = (e as? FirebaseAuthException)?.errorCode ?: "GOOGLE_SIGNIN_ERROR"
            val errorMsg = e.localizedMessage ?: e.message ?: "Google Sign-In failed."
            Log.e(tag, "Google Sign-In error: [$errorCode] $errorMsg", e)
            _authState.value = AuthState.AUTH_ERROR
            AuthResult.Error("[$errorCode] $errorMsg")
        }
    }

    override suspend fun signInOrSignUpWithGoogleAccount(
        email: String,
        displayName: String,
        photoUrl: String?
    ): AuthResult<AuthUser> = withContext(Dispatchers.IO) {
        _authState.value = AuthState.AUTHENTICATING
        val normalizedEmail = PersistentUserAccountStore.normalizeEmail(email)

        if (normalizedEmail.isBlank() || !normalizedEmail.contains("@")) {
            _authState.value = AuthState.AUTH_ERROR
            return@withContext AuthResult.Error("[ERROR_INVALID_EMAIL] Please enter a valid Google email address.")
        }

        val stableUid = PersistentUserAccountStore.generateDeterministicUid(normalizedEmail)
        val cleanDisplayName = displayName.trim().ifBlank {
            normalizedEmail.substringBefore("@").replaceFirstChar { it.uppercase() }
        }
        val effectivePhotoUrl = photoUrl ?: "https://lh3.googleusercontent.com/a/default-user"

        localRepo.setActiveSessionUid(stableUid)

        val authUser = AuthUser(
            uid = stableUid,
            email = normalizedEmail,
            username = cleanDisplayName.filter { it.isLetterOrDigit() || it == '_' }.ifBlank { "Player" },
            displayName = cleanDisplayName,
            photoUrl = effectivePhotoUrl,
            isEmailVerified = true,
            providerId = "google.com",
            role = "CUSTOMER / PIONEER",
            authType = "GOOGLE_OAUTH"
        )
        _currentUser.value = authUser
        _authState.value = AuthState.AUTHENTICATED
        AuthResult.Success(authUser)
    }

    override suspend fun sendEmailVerification(): AuthResult<Unit> = withContext(Dispatchers.IO) {
        val user = auth.currentUser
        if (user == null) {
            return@withContext AuthResult.Error("[AUTH_ERROR] No active user session to verify.")
        }
        try {
            user.sendEmailVerification().await()
            AuthResult.Success(Unit)
        } catch (e: Exception) {
            val code = (e as? FirebaseAuthException)?.errorCode ?: "VERIFICATION_ERROR"
            Log.e(tag, "Error sending verification email: [$code] ${e.message}", e)
            AuthResult.Error("[$code] ${e.localizedMessage ?: "Failed to dispatch verification email."}")
        }
    }

    override suspend fun reloadUser(): AuthResult<Boolean> = withContext(Dispatchers.IO) {
        val user = auth.currentUser
        if (user == null) {
            return@withContext AuthResult.Error("[AUTH_ERROR] No active user session.")
        }
        try {
            user.reload().await()
            val refreshedUser = auth.currentUser
            val verified = refreshedUser?.isEmailVerified ?: false
            if (refreshedUser != null) {
                _currentUser.value = _currentUser.value?.copy(
                    isEmailVerified = verified,
                    photoUrl = refreshedUser.photoUrl?.toString()
                )
            }
            AuthResult.Success(verified)
        } catch (e: Exception) {
            val code = (e as? FirebaseAuthException)?.errorCode ?: "RELOAD_ERROR"
            Log.e(tag, "Error reloading user: [$code] ${e.message}", e)
            AuthResult.Error("[$code] ${e.localizedMessage ?: "Failed to refresh user state."}")
        }
    }

    override suspend fun sendPasswordResetEmail(email: String): AuthResult<Unit> = withContext(Dispatchers.IO) {
        val normalized = PersistentUserAccountStore.normalizeEmail(email)
        if (normalized.isEmpty() || !normalized.contains("@")) {
            return@withContext AuthResult.Error("[ERROR_INVALID_EMAIL] Please enter a valid email address.")
        }
        try {
            auth.sendPasswordResetEmail(normalized).await()
            AuthResult.Success(Unit)
        } catch (e: Exception) {
            val code = (e as? FirebaseAuthException)?.errorCode ?: "RESET_ERROR"
            Log.e(tag, "Password reset error: [$code] ${e.message}", e)
            AuthResult.Error("[$code] ${e.localizedMessage ?: "Failed to send reset email. Verify your email address."}")
        }
    }

    override suspend fun signOut(): AuthResult<Unit> = withContext(Dispatchers.IO) {
        try {
            auth.signOut()
            localRepo.setActiveSessionUid(null)
            localRepo.clearActiveSessionData()
            _currentUser.value = null
            _authState.value = AuthState.UNAUTHENTICATED
            AuthResult.Success(Unit)
        } catch (e: Exception) {
            Log.e(tag, "Sign out error", e)
            AuthResult.Error(e.localizedMessage ?: "Failed to sign out.")
        }
    }

    override suspend fun deleteAccount(): AuthResult<Unit> = withContext(Dispatchers.IO) {
        try {
            val user = auth.currentUser
            val currentAuthUser = _currentUser.value
            val uid = currentAuthUser?.uid ?: user?.uid ?: ""

            if (user != null && firestore != null) {
                try {
                    firestore.collection("usernames")
                        .document(currentAuthUser?.username?.lowercase() ?: "")
                        .delete()
                        .await()
                } catch (e: Exception) {
                    Log.w(tag, "Error deleting username doc: ${e.message}")
                }

                try {
                    firestore.collection("users")
                        .document(user.uid)
                        .delete()
                        .await()
                } catch (e: Exception) {
                    Log.w(tag, "Error deleting user doc: ${e.message}")
                }

                try {
                    user.delete().await()
                } catch (e: Exception) {
                    Log.w(tag, "Error deleting Firebase user: ${e.message}")
                }
            }

            if (uid.isNotBlank()) {
                userStore.deleteUser(uid)
                localRepo.deleteProfile(uid)
            }

            localRepo.clearActiveSessionData()
            _currentUser.value = null
            _authState.value = AuthState.UNAUTHENTICATED
            AuthResult.Success(Unit)
        } catch (e: Exception) {
            val code = (e as? FirebaseAuthException)?.errorCode ?: "DELETE_ERROR"
            Log.e(tag, "Account deletion error: [$code] ${e.message}", e)
            AuthResult.Error("[$code] ${e.localizedMessage ?: "Failed to delete account. You may need to sign in again first."}")
        }
    }

    override fun getConfigurationStatus(): AuthConfigurationStatus {
        val clientId = getGoogleWebClientId()
        val missing = mutableListOf<String>()
        if (clientId.isNullOrBlank()) {
            missing.add("Google Web Client ID not configured.")
        }
        return AuthConfigurationStatus(
            isFirebaseInitialized = true,
            isGoogleSignInConfigured = !clientId.isNullOrBlank(),
            isEmailVerificationActive = true,
            activeProviderMode = "Firebase Cloud Authentication ($FIREBASE_PROJECT_ID)",
            googleWebClientId = clientId,
            notes = missing
        )
    }

    override fun getGoogleWebClientId(): String? {
        val fromEnv = System.getenv("GOOGLE_WEB_CLIENT_ID")
        if (!fromEnv.isNullOrBlank()) return fromEnv

        try {
            val resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
            if (resId != 0) {
                val resVal = context.getString(resId)
                if (resVal.isNotBlank()) return resVal
            }
        } catch (_: Exception) {}

        return "906231122938-klas99rgihsi8vtl3h13c89kggamudos.apps.googleusercontent.com"
    }

    override fun getRegisteredGoogleAccounts(): List<StoredUserAccount> {
        return userStore.getGoogleAccounts()
    }
}
