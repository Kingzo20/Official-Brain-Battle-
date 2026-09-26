package com.example.data.auth

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.LocalStorageRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class AccountPersistenceTest {

    private lateinit var context: Context
    private lateinit var localRepo: LocalStorageRepository
    private lateinit var authService: FirebaseAuthServiceImpl

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        localRepo = LocalStorageRepository(context)
        authService = FirebaseAuthServiceImpl(context)
    }

    @Test
    fun testDefaultDemoAccountIsPrePopulated() = runBlocking {
        // Verify default demo account exists out of the box
        val demoEmail = "kingzotalker@gmail.com"
        val demoPass = "kingzley@1A"

        val account = authService.userStore.getUserByEmail(demoEmail)
        assertNotNull("Demo account must be pre-populated", account)
        assertEquals("kingzotalker@gmail.com", account?.email)
        assertEquals("Kingzo", account?.username)
        assertEquals("CUSTOMER / PIONEER", account?.role)
        assertTrue("Password must verify correctly", authService.userStore.verifyPassword(account!!, demoPass))

        // Verify login succeeds with demo account
        val loginResult = authService.loginWithEmail(demoEmail, demoPass)
        assertTrue("Login with demo account should succeed", loginResult is AuthResult.Success)
        val user = (loginResult as AuthResult.Success).data
        assertEquals("kingzotalker@gmail.com", user.email)
        assertEquals("CUSTOMER / PIONEER", user.role)
        assertEquals(user.uid, localRepo.getActiveSessionUid())
    }

    @Test
    fun testEmailNormalizationOnLoginAndRegistration() = runBlocking {
        // Demo account login with mixed case and leading/trailing whitespace
        val loginResult = authService.loginWithEmail("   KiNgZoTaLkEr@gMaIL.CoM   ", "kingzley@1A")
        assertTrue("Login with unnormalized email must succeed after normalization", loginResult is AuthResult.Success)

        // Register new account with mixed case and whitespace
        val regEmail = "   PlayerAlpha@BrainBattle.io   "
        val regPass = "battlePass#1"
        val regUser = "PlayerAlpha"

        val regResult = authService.registerWithEmail(regEmail, regPass, regUser)
        assertTrue("Registration should succeed", regResult is AuthResult.Success)
        val registered = (regResult as AuthResult.Success).data
        assertEquals("playeralpha@brainbattle.io", registered.email)
        assertEquals("PlayerAlpha", registered.username)
        assertEquals("CUSTOMER / PIONEER", registered.role)

        // Query by exact lowercase normalized email
        val stored = authService.userStore.getUserByEmail("playeralpha@brainbattle.io")
        assertNotNull("Stored user must be queryable by normalized email", stored)
        assertEquals("playeralpha@brainbattle.io", stored?.email)
    }

    @Test
    fun testLogoutDoesNotWipeUserDirectory() = runBlocking {
        val email = "persistent.user@domain.com"
        val pass = "myPassword99"
        val username = "PersistentUser"

        // 1. Register new user
        val regResult = authService.registerWithEmail(email, pass, username)
        assertTrue(regResult is AuthResult.Success)
        val uid = (regResult as AuthResult.Success).data.uid

        assertEquals(uid, localRepo.getActiveSessionUid())
        assertTrue(authService.isAuthenticated)

        // 2. Log out
        val logoutResult = authService.signOut()
        assertTrue(logoutResult is AuthResult.Success)

        // 3. Verify session token is removed
        assertNull("Active session UID must be removed on logout", localRepo.getActiveSessionUid())
        assertFalse("Service must report unauthenticated", authService.isAuthenticated)
        assertNull("Current user in state must be null", authService.currentUser.value)

        // 4. Verify user record STILL exists in database
        val userAfterLogout = authService.userStore.getUserByEmail(email)
        assertNotNull("User record must NOT be deleted upon logout", userAfterLogout)
        assertEquals(email, userAfterLogout?.email)
        assertEquals(username, userAfterLogout?.username)

        // 5. Verify user can immediately log back in
        val reloginResult = authService.loginWithEmail(email, pass)
        assertTrue("Re-login after logout must succeed", reloginResult is AuthResult.Success)
        assertEquals(uid, (reloginResult as AuthResult.Success).data.uid)
    }

    @Test
    fun testAccountPersistsAcrossAppReload() = runBlocking {
        val email = "survivor@reloadtest.org"
        val pass = "reloadPass!7"
        val username = "ReloadHero"

        // 1. Register account in first instance
        val regResult = authService.registerWithEmail(email, pass, username)
        assertTrue(regResult is AuthResult.Success)

        // 2. User logs out
        authService.signOut()

        // 3. Simulate app reload: create a new AuthService instance pointing to same storage
        val reloadedAuthService = FirebaseAuthServiceImpl(context)

        // 4. Verify session is not automatically active after logout
        assertFalse(reloadedAuthService.isAuthenticated)

        // 5. Log in with the registered credentials
        val loginResult = reloadedAuthService.loginWithEmail(email, pass)
        assertTrue("Login after simulated app reload must succeed without throwing 'No account found'", loginResult is AuthResult.Success)
        val loggedInUser = (loginResult as AuthResult.Success).data
        assertEquals("survivor@reloadtest.org", loggedInUser.email)
        assertEquals("ReloadHero", loggedInUser.username)
        assertEquals("CUSTOMER / PIONEER", loggedInUser.role)
    }

    @Test
    fun testAuthenticationErrorsAreAccurate() = runBlocking {
        // Unknown email
        val unknownResult = authService.loginWithEmail("nonexistent.user@random.org", "anyPass123")
        assertTrue(unknownResult is AuthResult.Error)
        val errorMsg = (unknownResult as AuthResult.Error).message
        assertTrue(errorMsg.contains("No account found for this email", ignoreCase = true))

        // Incorrect password for valid account
        val badPassResult = authService.loginWithEmail("kingzotalker@gmail.com", "wrongPasswordHere")
        assertTrue(badPassResult is AuthResult.Error)
        val passErrorMsg = (badPassResult as AuthResult.Error).message
        assertTrue(passErrorMsg.contains("Incorrect password", ignoreCase = true))

        // Duplicate registration
        val duplicateResult = authService.registerWithEmail("kingzotalker@gmail.com", "somePassword", "DuplicateUser")
        assertTrue(duplicateResult is AuthResult.Error)
        val dupMsg = (duplicateResult as AuthResult.Error).message
        assertTrue(dupMsg.contains("already exists", ignoreCase = true))
    }

    @Test
    fun testGoogleSignInOnExistingPasswordAccountShowsConflictWarning() = runBlocking {
        // kingzotalker@gmail.com is seeded with PASSWORD auth
        val result = authService.signInOrSignUpWithGoogleAccount(
            email = "kingzotalker@gmail.com",
            displayName = "Kingsley"
        )

        // 1. Must return error
        assertTrue("Attempting Google Sign-In on password account must return Error", result is AuthResult.Error)
        val errorMsg = (result as AuthResult.Error).message

        // 2. Must match the exact required conflict warning message
        val expectedMessage = "This email address is already in use. Please sign in using your email and password, or link your Google account in settings."
        assertEquals(expectedMessage, errorMsg)

        // 3. Verify no duplicate account record was created; original password auth preserved
        val stored = authService.userStore.getUserByEmail("kingzotalker@gmail.com")
        assertNotNull(stored)
        assertEquals("PASSWORD", stored?.authProvider)
        assertEquals("Kingzo", stored?.username)
    }

    @Test
    fun testGoogleSignUpNewUserCreatesAccountAndEstablishesSession() = runBlocking {
        val googleEmail = "   PioneerGamer@Gmail.Com   "
        val displayName = "Pioneer Gamer"
        val photoUrl = "https://lh3.googleusercontent.com/a/pioneer-photo"

        // 1. Perform Google Sign-Up for a new user
        val result = authService.signInOrSignUpWithGoogleAccount(
            email = googleEmail,
            displayName = displayName,
            photoUrl = photoUrl
        )

        assertTrue("Google sign up with new email must succeed", result is AuthResult.Success)
        val user = (result as AuthResult.Success).data

        // 2. Verify normalized metadata mapping
        assertEquals("pioneergamer@gmail.com", user.email)
        assertEquals("Pioneer Gamer", user.displayName)
        assertEquals(photoUrl, user.photoUrl)
        assertEquals("GOOGLE_OAUTH", user.authType)
        assertTrue(user.isEmailVerified)

        // 3. Verify session is actively established
        assertEquals(user.uid, localRepo.getActiveSessionUid())
        assertTrue(authService.isAuthenticated)

        // 4. Verify stored account in database
        val storedAccount = authService.userStore.getUserByEmail("pioneergamer@gmail.com")
        assertNotNull(storedAccount)
        assertEquals("GOOGLE", storedAccount?.authProvider)
        assertEquals(photoUrl, storedAccount?.photoUrl)
    }

    @Test
    fun testGoogleSignInExistingGoogleUserInstantLogin() = runBlocking {
        val googleEmail = "alex.cloud@gmail.com"
        val displayName = "Alex Cloud"

        // 1. Create Google user
        val createResult = authService.signInOrSignUpWithGoogleAccount(googleEmail, displayName)
        assertTrue(createResult is AuthResult.Success)
        val initialUser = (createResult as AuthResult.Success).data

        // 2. User logs out
        val logoutResult = authService.signOut()
        assertTrue(logoutResult is AuthResult.Success)
        assertNull(localRepo.getActiveSessionUid())
        assertFalse(authService.isAuthenticated)

        // 3. Sign in again with same Google account -> Instant Login without error
        val reloginResult = authService.signInOrSignUpWithGoogleAccount(googleEmail, displayName)
        assertTrue("Subsequent Google Sign-In with same email must log in seamlessly", reloginResult is AuthResult.Success)
        val reloggedUser = (reloginResult as AuthResult.Success).data
        assertEquals(initialUser.uid, reloggedUser.uid)
        assertEquals(googleEmail, reloggedUser.email)
        assertEquals(reloggedUser.uid, localRepo.getActiveSessionUid())
        assertTrue(authService.isAuthenticated)
    }

    @Test
    fun testEmailPasswordRegistrationOnGoogleAccountRejectsConflict() = runBlocking {
        val googleEmail = "charlie.oauth@gmail.com"
        authService.signInOrSignUpWithGoogleAccount(googleEmail, "Charlie OAuth")
        authService.signOut()

        // Attempting email/password registration with the same email must reject duplicate
        val regResult = authService.registerWithEmail(googleEmail, "somePassword123", "CharlieNew")
        assertTrue("Email registration on existing Google account must fail", regResult is AuthResult.Error)
        val msg = (regResult as AuthResult.Error).message
        assertTrue(msg.contains("Google", ignoreCase = true))
    }

    @Test
    fun testGoogleAccountPersistsAcrossReload() = runBlocking {
        val googleEmail = "persistent.google@gmail.com"
        val displayName = "Persistent Google User"
        val photoUrl = "https://lh3.googleusercontent.com/avatar1"

        // 1. Create account
        authService.signInOrSignUpWithGoogleAccount(googleEmail, displayName, photoUrl)
        authService.signOut()

        // 2. Simulate fresh app reload
        val reloadedService = FirebaseAuthServiceImpl(context)

        // 3. Account must still exist with GOOGLE provider in persistent store
        val stored = reloadedService.userStore.getUserByEmail(googleEmail)
        assertNotNull("Google user must persist across simulated reload", stored)
        assertEquals("GOOGLE", stored?.authProvider)
        assertEquals(photoUrl, stored?.photoUrl)

        // 4. Instant Google login succeeds on reloaded service
        val loginResult = reloadedService.signInOrSignUpWithGoogleAccount(googleEmail, displayName)
        assertTrue("Google sign in on reloaded service must succeed", loginResult is AuthResult.Success)
        assertEquals(stored?.uid, (loginResult as AuthResult.Success).data.uid)
    }
}
