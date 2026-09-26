package com.example.data.auth

import android.content.Context
import android.util.Log
import com.example.data.local.LocalStorageRepository
import com.example.model.UserProfile
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
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

    val userStore = PersistentUserAccountStore(context)
    private val localRepo = LocalStorageRepository(context)
    private val prefs = context.getSharedPreferences("brain_battle_auth", Context.MODE_PRIVATE)

    private val _isFirebaseConfigured = try {
        val apps = FirebaseApp.getApps(context)
        if (apps.isNotEmpty()) {
            val app = apps.first()
            val opts = app.options
            !opts.apiKey.isNullOrBlank() && !opts.applicationId.isNullOrBlank() && opts.apiKey != "dummy"
        } else {
            false
        }
    } catch (e: Exception) {
        Log.w(tag, "Firebase initialization check: ${e.message}")
        false
    }

    override val isFirebaseConfigured: Boolean get() = _isFirebaseConfigured

    private val auth: FirebaseAuth? = if (_isFirebaseConfigured) {
        try {
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            Log.e(tag, "Failed to get FirebaseAuth instance", e)
            null
        }
    } else null

    private val firestore: FirebaseFirestore? = if (_isFirebaseConfigured) {
        try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.e(tag, "Failed to get FirebaseFirestore instance", e)
            null
        }
    } else null

    private val _currentUser = MutableStateFlow<AuthUser?>(null)
    override val currentUser: StateFlow<AuthUser?> = _currentUser.asStateFlow()

    private val _authState = MutableStateFlow<AuthState>(AuthState.AUTHENTICATING)
    override val authState: StateFlow<AuthState> = _authState.asStateFlow()

    override val isAuthenticated: Boolean
        get() = _currentUser.value != null

    init {
        seedDefaultAccounts()
        restoreSession()
    }

    private fun seedDefaultAccounts() {
        userStore.seedDefaultAccounts()
        val demoUid = PersistentUserAccountStore.generateDeterministicUid(PersistentUserAccountStore.DEMO_EMAIL)
        if (!localRepo.hasProfileData(demoUid)) {
            val demoProfile = UserProfile(
                uid = demoUid,
                playerId = "BB-987654",
                username = PersistentUserAccountStore.DEMO_USERNAME,
                displayName = PersistentUserAccountStore.DEMO_DISPLAY_NAME,
                email = PersistentUserAccountStore.DEMO_EMAIL,
                avatarEmoji = "👑",
                level = 5,
                currentXp = 320,
                nextLevelXp = 500,
                totalXp = 1800,
                gamesPlayed = 30,
                totalScore = 21500,
                bestScore = 980,
                currentStreak = 6,
                bestStreak = 14,
                selectedTitle = "Pioneer",
                unlockedTitles = listOf("Pioneer", "Rookie", "Grandmaster"),
                emailVerified = true,
                role = PersistentUserAccountStore.DEMO_ROLE
            )
            localRepo.saveProfile(demoProfile)
        }
    }

    private fun restoreSession() {
        if (_isFirebaseConfigured && auth != null) {
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
        } else {
            // Restore persistent non-volatile local session
            val activeUid = localRepo.getActiveSessionUid()
            if (!activeUid.isNullOrBlank()) {
                val stored = userStore.getUserByUid(activeUid)
                if (stored != null) {
                    val isGoogle = stored.authProvider.equals("GOOGLE", ignoreCase = true)
                    val authUser = AuthUser(
                        uid = stored.uid,
                        email = stored.email,
                        username = stored.username,
                        displayName = stored.displayName,
                        photoUrl = stored.photoUrl,
                        isEmailVerified = stored.isEmailVerified,
                        providerId = if (isGoogle) "google.com" else "local",
                        role = stored.role,
                        authType = if (isGoogle) "GOOGLE_OAUTH" else "PASSWORD"
                    )
                    _currentUser.value = authUser
                    _authState.value = AuthState.AUTHENTICATED
                } else {
                    localRepo.setActiveSessionUid(null)
                    _currentUser.value = null
                    _authState.value = AuthState.UNAUTHENTICATED
                }
            } else {
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

        if (_isFirebaseConfigured && firestore != null) {
            return try {
                val doc = firestore.collection("usernames")
                    .document(trimmed.lowercase())
                    .get()
                    .await()
                if (doc.exists()) {
                    val ownerUid = doc.getString("uid")
                    if (ownerUid != null && ownerUid == auth?.currentUser?.uid) {
                        UsernameValidationStatus.VALID
                    } else {
                        UsernameValidationStatus.TAKEN
                    }
                } else {
                    UsernameValidationStatus.VALID
                }
            } catch (e: Exception) {
                Log.w(tag, "Firestore username check error: ${e.message}")
                UsernameValidationStatus.VALID
            }
        } else {
            val currentUid = _currentUser.value?.uid
            if (userStore.isUsernameTaken(trimmed, currentUid)) {
                return UsernameValidationStatus.TAKEN
            }
            return UsernameValidationStatus.VALID
        }
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
            return@withContext AuthResult.Error("Please enter a valid email address.")
        }

        if (password.length < 6) {
            _authState.value = AuthState.AUTH_ERROR
            return@withContext AuthResult.Error("Password must be at least 6 characters.")
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

        // Duplicate email validation: check if normalized email already exists in persistent database
        val existing = userStore.getUserByEmail(normalizedEmail)
        if (existing != null) {
            _authState.value = AuthState.AUTH_ERROR
            return@withContext if (existing.authProvider.equals("GOOGLE", ignoreCase = true)) {
                AuthResult.Error("An account with this email is linked with Google Sign-In. Please sign in using 'Continue with Google'.")
            } else {
                AuthResult.Error("An account with this email already exists. Please sign in.")
            }
        }

        if (_isFirebaseConfigured && auth != null) {
            try {
                val result = auth.createUserWithEmailAndPassword(normalizedEmail, password).await()
                val firebaseUser = result.user ?: return@withContext AuthResult.Error("Failed to create authentication user")

                // Update display name
                try {
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(trimmedUsername)
                        .build()
                    firebaseUser.updateProfile(profileUpdates).await()
                } catch (e: Exception) {
                    Log.w(tag, "Failed to set display name: ${e.message}")
                }

                // Send real email verification
                try {
                    firebaseUser.sendEmailVerification().await()
                } catch (e: Exception) {
                    Log.w(tag, "Send email verification warning: ${e.message}")
                }

                // Store in userStore as local persistent cache as well
                val stored = StoredUserAccount(
                    uid = firebaseUser.uid,
                    email = normalizedEmail,
                    passwordHash = PersistentUserAccountStore.hashSha256(password),
                    username = trimmedUsername,
                    displayName = trimmedUsername,
                    role = "CUSTOMER / PIONEER",
                    isEmailVerified = firebaseUser.isEmailVerified,
                    createdAt = System.currentTimeMillis(),
                    lastLoginAt = System.currentTimeMillis()
                )
                userStore.saveUser(stored)

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
            } catch (e: FirebaseAuthWeakPasswordException) {
                _authState.value = AuthState.AUTH_ERROR
                AuthResult.Error("Password is too weak. Please use at least 6 characters.")
            } catch (e: FirebaseAuthUserCollisionException) {
                _authState.value = AuthState.AUTH_ERROR
                AuthResult.Error("An account with this email already exists. Please sign in.")
            } catch (e: FirebaseAuthInvalidCredentialsException) {
                _authState.value = AuthState.AUTH_ERROR
                AuthResult.Error("Invalid email format.")
            } catch (e: Exception) {
                Log.e(tag, "Registration error", e)
                _authState.value = AuthState.AUTH_ERROR
                AuthResult.Error(e.localizedMessage ?: "Registration failed. Please check network connectivity.")
            }
        } else {
            // Persistent non-volatile local account
            if (userStore.isEmailRegistered(normalizedEmail)) {
                _authState.value = AuthState.AUTH_ERROR
                return@withContext AuthResult.Error("An account with this email already exists. Please sign in.")
            }

            val stableUid = PersistentUserAccountStore.generateDeterministicUid(normalizedEmail)
            val passHash = PersistentUserAccountStore.hashSha256(password)

            val newAccount = StoredUserAccount(
                uid = stableUid,
                email = normalizedEmail,
                passwordHash = passHash,
                username = trimmedUsername,
                displayName = trimmedUsername,
                role = "CUSTOMER / PIONEER",
                isEmailVerified = false,
                createdAt = System.currentTimeMillis(),
                lastLoginAt = System.currentTimeMillis()
            )
            userStore.saveUser(newAccount)

            // Initialize local profile
            val newProfile = UserProfile(
                uid = stableUid,
                playerId = "BB-${(100000..999999).random()}",
                username = trimmedUsername,
                displayName = trimmedUsername,
                email = normalizedEmail,
                role = "CUSTOMER / PIONEER",
                emailVerified = false,
                createdAt = System.currentTimeMillis(),
                lastLoginAt = System.currentTimeMillis()
            )
            localRepo.saveProfile(newProfile)

            localRepo.setActiveSessionUid(stableUid)

            val authUser = AuthUser(
                uid = stableUid,
                email = normalizedEmail,
                username = trimmedUsername,
                displayName = trimmedUsername,
                isEmailVerified = false,
                providerId = "local",
                role = "CUSTOMER / PIONEER"
            )
            _currentUser.value = authUser
            _authState.value = AuthState.AUTHENTICATED
            AuthResult.Success(authUser)
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
            return@withContext AuthResult.Error("Please enter a valid email address.")
        }

        if (_isFirebaseConfigured && auth != null) {
            try {
                val result = auth.signInWithEmailAndPassword(normalizedEmail, password).await()
                val firebaseUser = result.user ?: run {
                    _authState.value = AuthState.AUTH_ERROR
                    return@withContext AuthResult.Error("User record not found")
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
            } catch (e: FirebaseAuthInvalidCredentialsException) {
                _authState.value = AuthState.AUTH_ERROR
                AuthResult.Error("Invalid email or password. Please try again.")
            } catch (e: Exception) {
                // If Firebase fails (e.g. offline mode or local demo account), check userStore
                val localAcc = userStore.getUserByEmail(normalizedEmail)
                if (localAcc != null && userStore.verifyPassword(localAcc, password)) {
                    localRepo.setActiveSessionUid(localAcc.uid)
                    userStore.updateLastLogin(localAcc.uid)
                    val authUser = AuthUser(
                        uid = localAcc.uid,
                        email = localAcc.email,
                        username = localAcc.username,
                        displayName = localAcc.displayName,
                        isEmailVerified = localAcc.isEmailVerified,
                        providerId = "local",
                        role = localAcc.role
                    )
                    _currentUser.value = authUser
                    _authState.value = AuthState.AUTHENTICATED
                    return@withContext AuthResult.Success(authUser)
                }
                Log.e(tag, "Login error", e)
                _authState.value = AuthState.AUTH_ERROR
                AuthResult.Error(e.localizedMessage ?: "Login failed. Please check your network connection.")
            }
        } else {
            // Persistent non-volatile local login
            val account = userStore.getUserByEmail(normalizedEmail)
            if (account == null) {
                _authState.value = AuthState.AUTH_ERROR
                return@withContext AuthResult.Error("No account found for this email. Please check your email or create an account.")
            }

            if (account.authProvider.equals("GOOGLE", ignoreCase = true)) {
                _authState.value = AuthState.AUTH_ERROR
                return@withContext AuthResult.Error("This account was created with Google Sign-In. Please tap 'Continue with Google' to sign in.")
            }

            if (!userStore.verifyPassword(account, password)) {
                _authState.value = AuthState.AUTH_ERROR
                return@withContext AuthResult.Error("Incorrect password. Please try again.")
            }

            userStore.updateLastLogin(account.uid)
            localRepo.setActiveSessionUid(account.uid)

            // Ensure profile exists in localRepo
            if (!localRepo.hasProfileData(account.uid)) {
                localRepo.saveProfile(
                    UserProfile(
                        uid = account.uid,
                        playerId = "BB-${(100000..999999).random()}",
                        username = account.username,
                        displayName = account.displayName,
                        email = account.email,
                        photoUrl = account.photoUrl,
                        role = account.role,
                        emailVerified = account.isEmailVerified,
                        lastLoginAt = System.currentTimeMillis()
                    )
                )
            }

            val authUser = AuthUser(
                uid = account.uid,
                email = account.email,
                username = account.username,
                displayName = account.displayName,
                photoUrl = account.photoUrl,
                isEmailVerified = account.isEmailVerified,
                providerId = "local",
                role = account.role,
                authType = "PASSWORD"
            )
            _currentUser.value = authUser
            _authState.value = AuthState.AUTHENTICATED
            AuthResult.Success(authUser)
        }
    }

    override suspend fun signInWithGoogle(idToken: String): AuthResult<AuthUser> = withContext(Dispatchers.IO) {
        _authState.value = AuthState.AUTHENTICATING
        if (idToken.isBlank()) {
            _authState.value = AuthState.AUTH_ERROR
            return@withContext AuthResult.Error("Google ID token was empty")
        }

        if (_isFirebaseConfigured && auth != null) {
            try {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                val result = auth.signInWithCredential(credential).await()
                val firebaseUser = result.user ?: run {
                    _authState.value = AuthState.AUTH_ERROR
                    return@withContext AuthResult.Error("Google Sign-In failed to produce user record")
                }

                val normalizedEmail = PersistentUserAccountStore.normalizeEmail(firebaseUser.email ?: "")

                // DUPLICATE EMAIL CHECK: Check if email already registered via standard password auth
                val localExisting = userStore.getUserByEmail(normalizedEmail)
                if (localExisting != null && localExisting.authProvider.equals("PASSWORD", ignoreCase = true)) {
                    auth.signOut()
                    _authState.value = AuthState.AUTH_ERROR
                    return@withContext AuthResult.Error(
                        "This email address is already in use. Please sign in using your email and password, or link your Google account in settings."
                    )
                }

                val displayName = firebaseUser.displayName ?: firebaseUser.email?.substringBefore("@") ?: "Player"
                val username = prefs.getString("user_username_${firebaseUser.uid}", null)
                    ?: displayName.filter { it.isLetterOrDigit() || it == '_' }.ifBlank { "Player" }
                val photoUrl = firebaseUser.photoUrl?.toString() ?: "https://lh3.googleusercontent.com/a/default-user"

                val stored = StoredUserAccount(
                    uid = firebaseUser.uid,
                    email = normalizedEmail,
                    passwordHash = "OAUTH_GOOGLE_MANAGED",
                    username = username,
                    displayName = displayName,
                    role = "CUSTOMER / PIONEER",
                    isEmailVerified = firebaseUser.isEmailVerified,
                    createdAt = System.currentTimeMillis(),
                    lastLoginAt = System.currentTimeMillis(),
                    authProvider = "GOOGLE",
                    photoUrl = photoUrl
                )
                userStore.saveUser(stored)

                localRepo.setActiveSessionUid(firebaseUser.uid)
                val existingProfile = localRepo.loadProfile(firebaseUser.uid)
                localRepo.saveProfile(
                    existingProfile.copy(
                        uid = firebaseUser.uid,
                        playerId = if (existingProfile.playerId.isNotBlank()) existingProfile.playerId else "BB-${(100000..999999).random()}",
                        email = normalizedEmail,
                        username = username,
                        displayName = displayName,
                        photoUrl = photoUrl,
                        provider = "google.com",
                        emailVerified = true,
                        lastLoginAt = System.currentTimeMillis()
                    )
                )

                val authUser = AuthUser(
                    uid = firebaseUser.uid,
                    email = normalizedEmail,
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
            } catch (e: Exception) {
                Log.e(tag, "Google Sign-In error", e)
                _authState.value = AuthState.AUTH_ERROR
                AuthResult.Error(e.localizedMessage ?: "Google Sign-In failed.")
            }
        } else {
            _authState.value = AuthState.AUTH_ERROR
            AuthResult.Error("Firebase Authentication is not configured for Google Sign-In.")
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
            return@withContext AuthResult.Error("Please enter a valid Google email address.")
        }

        // 1. Check if user already exists
        val existing = userStore.getUserByEmail(normalizedEmail)
        if (existing != null) {
            // IF THE EMAIL ALREADY EXISTS UNDER EMAIL/PASSWORD AUTH:
            if (existing.authProvider.equals("PASSWORD", ignoreCase = true)) {
                _authState.value = AuthState.AUTH_ERROR
                return@withContext AuthResult.Error(
                    "This email address is already in use. Please sign in using your email and password, or link your Google account in settings."
                )
            }

            // IF THE EMAIL ALREADY EXISTS UNDER GOOGLE AUTH:
            // Automatically log the user into their existing Google-linked account profile seamlessly without throwing an error.
            userStore.updateLastLogin(existing.uid)
            localRepo.setActiveSessionUid(existing.uid)

            val existingProfile = localRepo.loadProfile(existing.uid)
            val updatedPhoto = existing.photoUrl ?: photoUrl ?: existingProfile.photoUrl
            localRepo.saveProfile(
                existingProfile.copy(
                    lastLoginAt = System.currentTimeMillis(),
                    provider = "google.com",
                    photoUrl = updatedPhoto
                )
            )

            val authUser = AuthUser(
                uid = existing.uid,
                email = existing.email,
                username = existing.username,
                displayName = existing.displayName.ifBlank { displayName },
                photoUrl = updatedPhoto,
                isEmailVerified = true,
                providerId = "google.com",
                role = existing.role,
                authType = "GOOGLE_OAUTH"
            )
            _currentUser.value = authUser
            _authState.value = AuthState.AUTHENTICATED
            return@withContext AuthResult.Success(authUser)
        }

        // 2. NEW USER (Sign-Up):
        // Auto-create account record with Google profile photo, display name, and authProvider = "GOOGLE", then auto-login.
        val stableUid = PersistentUserAccountStore.generateDeterministicUid(normalizedEmail)
        val cleanDisplayName = displayName.trim().ifBlank {
            normalizedEmail.substringBefore("@").replaceFirstChar { it.uppercase() }
        }

        val baseCandidate = cleanDisplayName.filter { it.isLetterOrDigit() || it == '_' }.ifBlank { "Player" }
        var uniqueUsername = baseCandidate
        var counter = 1
        while (userStore.isUsernameTaken(uniqueUsername)) {
            uniqueUsername = "${baseCandidate}_$counter"
            counter++
        }

        val effectivePhotoUrl = photoUrl ?: "https://lh3.googleusercontent.com/a/default-user"

        val newAccount = StoredUserAccount(
            uid = stableUid,
            email = normalizedEmail,
            passwordHash = "OAUTH_GOOGLE_MANAGED",
            username = uniqueUsername,
            displayName = cleanDisplayName,
            role = "CUSTOMER / PIONEER",
            isEmailVerified = true,
            createdAt = System.currentTimeMillis(),
            lastLoginAt = System.currentTimeMillis(),
            authProvider = "GOOGLE",
            photoUrl = effectivePhotoUrl
        )
        userStore.saveUser(newAccount)

        val newProfile = UserProfile(
            uid = stableUid,
            playerId = "BB-${(100000..999999).random()}",
            username = uniqueUsername,
            displayName = cleanDisplayName,
            email = normalizedEmail,
            photoUrl = effectivePhotoUrl,
            avatarEmoji = "🧠",
            role = "CUSTOMER / PIONEER",
            emailVerified = true,
            provider = "google.com",
            createdAt = System.currentTimeMillis(),
            lastLoginAt = System.currentTimeMillis()
        )
        localRepo.saveProfile(newProfile)
        localRepo.setActiveSessionUid(stableUid)

        val authUser = AuthUser(
            uid = stableUid,
            email = normalizedEmail,
            username = uniqueUsername,
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
        if (_isFirebaseConfigured && auth != null) {
            val user = auth.currentUser
            if (user == null) {
                return@withContext AuthResult.Error("No active user session to verify.")
            }
            try {
                user.sendEmailVerification().await()
                AuthResult.Success(Unit)
            } catch (e: Exception) {
                Log.e(tag, "Error sending verification email", e)
                AuthResult.Error(e.localizedMessage ?: "Failed to dispatch verification email.")
            }
        } else {
            val currentUid = _currentUser.value?.uid
            if (currentUid != null) {
                userStore.updateEmailVerified(currentUid, true)
                _currentUser.value = _currentUser.value?.copy(isEmailVerified = true)
                AuthResult.Success(Unit)
            } else {
                AuthResult.Error("No active session.")
            }
        }
    }

    override suspend fun reloadUser(): AuthResult<Boolean> = withContext(Dispatchers.IO) {
        if (_isFirebaseConfigured && auth != null) {
            val user = auth.currentUser
            if (user == null) {
                return@withContext AuthResult.Error("No active user session.")
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
                Log.e(tag, "Error reloading user", e)
                AuthResult.Error(e.localizedMessage ?: "Failed to refresh user state.")
            }
        } else {
            val currentUid = _currentUser.value?.uid
            val verified = if (currentUid != null) {
                userStore.getUserByUid(currentUid)?.isEmailVerified ?: false
            } else false
            AuthResult.Success(verified)
        }
    }

    override suspend fun sendPasswordResetEmail(email: String): AuthResult<Unit> = withContext(Dispatchers.IO) {
        val normalized = PersistentUserAccountStore.normalizeEmail(email)
        if (normalized.isEmpty() || !normalized.contains("@")) {
            return@withContext AuthResult.Error("Please enter a valid email address.")
        }
        if (_isFirebaseConfigured && auth != null) {
            try {
                auth.sendPasswordResetEmail(normalized).await()
                AuthResult.Success(Unit)
            } catch (e: Exception) {
                Log.e(tag, "Password reset error", e)
                AuthResult.Error(e.localizedMessage ?: "Failed to send reset email. Verify your email address.")
            }
        } else {
            val account = userStore.getUserByEmail(normalized)
            if (account == null) {
                AuthResult.Error("No registered Brain Battle account was found for $normalized.")
            } else {
                AuthResult.Success(Unit)
            }
        }
    }

    override suspend fun signOut(): AuthResult<Unit> = withContext(Dispatchers.IO) {
        try {
            if (_isFirebaseConfigured) {
                auth?.signOut()
            }
            // ONLY remove the active session token on logout.
            // DO NOT clear or reset the registered users database/array!
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
            val user = if (_isFirebaseConfigured) auth?.currentUser else null
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
            Log.e(tag, "Account deletion error", e)
            AuthResult.Error(e.localizedMessage ?: "Failed to delete account. You may need to sign in again first.")
        }
    }

    override fun getConfigurationStatus(): AuthConfigurationStatus {
        val clientId = getGoogleWebClientId()
        val missing = mutableListOf<String>()
        if (auth == null) {
            missing.add("Firebase google-services.json not installed or initialized.")
        }
        if (clientId.isNullOrBlank()) {
            missing.add("Google Web Client ID (GOOGLE_WEB_CLIENT_ID) not configured.")
        }
        return AuthConfigurationStatus(
            isFirebaseInitialized = _isFirebaseConfigured && auth != null,
            isGoogleSignInConfigured = _isFirebaseConfigured && auth != null && !clientId.isNullOrBlank(),
            isEmailVerificationActive = _isFirebaseConfigured && auth != null,
            activeProviderMode = if (_isFirebaseConfigured && auth != null) "Firebase Cloud Authentication" else "Non-Volatile SQLite Scoped Persistence",
            googleWebClientId = clientId,
            notes = missing
        )
    }

    override fun getGoogleWebClientId(): String? {
        val fromEnv = System.getenv("GOOGLE_WEB_CLIENT_ID")
        if (!fromEnv.isNullOrBlank()) return fromEnv
        return prefs.getString("google_web_client_id", null)
    }

    override fun getRegisteredGoogleAccounts(): List<StoredUserAccount> {
        return userStore.getGoogleAccounts()
    }
}
