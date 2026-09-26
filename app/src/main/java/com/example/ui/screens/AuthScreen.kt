package com.example.ui.screens

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.example.R
import com.example.data.auth.AuthResult
import com.example.data.auth.AuthService
import com.example.data.auth.UsernameValidationStatus
import com.example.ui.components.PrimaryButton
import com.example.ui.theme.*
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(
    authService: AuthService?,
    onAuthSuccess: (username: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var isSignUp by remember { mutableStateOf(false) }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    // Email verification state
    var showVerificationDialog by remember { mutableStateOf(false) }
    var registeredEmail by remember { mutableStateOf("") }
    var isCheckingVerification by remember { mutableStateOf(false) }
    var verificationStatusMessage by remember { mutableStateOf<String?>(null) }

    // Password reset state
    var forgotPasswordOpen by remember { mutableStateOf(false) }
    var resetEmail by remember { mutableStateOf("") }
    var resetEmailSent by remember { mutableStateOf(false) }
    var resetError by remember { mutableStateOf<String?>(null) }
    var isResetLoading by remember { mutableStateOf(false) }

    val configStatus = remember(authService) {
        authService?.getConfigurationStatus()
    }

    val scrollState = rememberScrollState()

    Scaffold(
        containerColor = BackgroundDark,
        modifier = modifier.testTag("auth_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Brain Battle Header Logo
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = CardSurface,
                border = BorderStroke(1.5.dp, CardSurfaceBorder),
                modifier = Modifier.size(76.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_brain_battle_logo),
                    contentDescription = "Brain Battle Logo",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(20.dp)),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "BRAIN BATTLE",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )

            Text(
                text = if (isSignUp) "Create your permanent player account" else "Sign in to access your cloud profile & rank",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Auth Backend Status Badge
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (authService?.isFirebaseConfigured == true) NeonGreen.copy(alpha = 0.12f) else CardSurface,
                border = BorderStroke(1.dp, if (authService?.isFirebaseConfigured == true) NeonGreen else CardSurfaceBorder)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (authService?.isFirebaseConfigured == true) "🟢 Firebase Cloud Auth Active" else "🛡️ Persistent Scoped Account Mode",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (authService?.isFirebaseConfigured == true) NeonGreen else TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Error display if any
            if (errorMessage != null) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = NeonRed.copy(alpha = 0.15f),
                    border = BorderStroke(1.5.dp, NeonRed),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .testTag("auth_error_banner")
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "⚠️", fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = errorMessage ?: "",
                            color = NeonRed,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Success display if any
            if (successMessage != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = NeonGreen.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, NeonGreen),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Text(
                        text = successMessage ?: "",
                        color = NeonGreen,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(12.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Input Fields Card
            Card(
                colors = CardDefaults.cardColors(containerColor = CardSurface),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, CardSurfaceBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    // Sign Up: Unique Username Field
                    if (isSignUp) {
                        OutlinedTextField(
                            value = username,
                            onValueChange = {
                                username = it.filter { char -> char.isLetterOrDigit() || char == '_' }
                                errorMessage = null
                            },
                            label = { Text("Player Username", color = TextSecondary) },
                            singleLine = true,
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = "Username", tint = NeonCyan)
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = CardSurfaceBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                cursorColor = NeonCyan
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_username_field")
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    // Email Field
                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            errorMessage = null
                        },
                        label = { Text("Email Address", color = TextSecondary) },
                        placeholder = { Text("name@example.com", color = TextMuted) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        leadingIcon = {
                            Icon(Icons.Default.Email, contentDescription = "Email", tint = NeonCyan)
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = CardSurfaceBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            cursorColor = NeonCyan
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_email_field")
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Password Field
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            errorMessage = null
                        },
                        label = { Text("Password", color = TextSecondary) },
                        placeholder = { Text("••••••••", color = TextMuted) },
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = "Password", tint = NeonCyan)
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                    tint = TextSecondary
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = CardSurfaceBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            cursorColor = NeonCyan
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_password_field")
                    )

                    // Sign Up: Confirm Password
                    if (isSignUp) {
                        Spacer(modifier = Modifier.height(14.dp))
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = {
                                confirmPassword = it
                                errorMessage = null
                            },
                            label = { Text("Confirm Password", color = TextSecondary) },
                            placeholder = { Text("••••••••", color = TextMuted) },
                            singleLine = true,
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = "Confirm Password", tint = NeonCyan)
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = CardSurfaceBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                cursorColor = NeonCyan
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_confirm_password_field")
                        )
                    }

                    // Forgot Password link for Sign In
                    if (!isSignUp) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                            Text(
                                text = "Forgot Password?",
                                color = NeonCyan,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clickable {
                                        resetEmail = email.trim().lowercase()
                                        resetEmailSent = false
                                        resetError = null
                                        forgotPasswordOpen = true
                                    }
                                    .padding(4.dp)
                                    .testTag("auth_forgot_password_link")
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Primary Action Button (Sign In / Register)
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = NeonCyan, modifier = Modifier.size(32.dp))
                }
            } else {
                PrimaryButton(
                    text = if (isSignUp) "CREATE ACCOUNT" else "SIGN IN",
                    onClick = {
                        errorMessage = null
                        successMessage = null

                        val normalizedEmail = email.trim().lowercase()
                        val cleanUsername = username.trim()

                        if (isSignUp) {
                            if (cleanUsername.isBlank()) {
                                errorMessage = "Please choose a username"
                                return@PrimaryButton
                            }
                            if (normalizedEmail.isBlank()) {
                                errorMessage = "Please enter your email"
                                return@PrimaryButton
                            }
                            if (password.length < 6) {
                                errorMessage = "Password must be at least 6 characters"
                                return@PrimaryButton
                            }
                            if (password != confirmPassword) {
                                errorMessage = "Passwords do not match"
                                return@PrimaryButton
                            }

                            isLoading = true
                            coroutineScope.launch {
                                val result = authService?.registerWithEmail(
                                    email = normalizedEmail,
                                    password = password,
                                    username = cleanUsername
                                )
                                isLoading = false
                                when (result) {
                                    is AuthResult.Success -> {
                                        registeredEmail = normalizedEmail
                                        if (authService.isFirebaseConfigured && !result.data.isEmailVerified) {
                                            showVerificationDialog = true
                                        } else {
                                            onAuthSuccess(result.data.username)
                                        }
                                    }
                                    is AuthResult.Error -> {
                                        errorMessage = result.message
                                    }
                                    null -> {
                                        onAuthSuccess(cleanUsername)
                                    }
                                }
                            }
                        } else {
                            if (normalizedEmail.isBlank() || password.isBlank()) {
                                errorMessage = "Please enter your email and password"
                                return@PrimaryButton
                            }

                            isLoading = true
                            coroutineScope.launch {
                                val result = authService?.loginWithEmail(
                                    email = normalizedEmail,
                                    password = password
                                )
                                isLoading = false
                                when (result) {
                                    is AuthResult.Success -> {
                                        onAuthSuccess(result.data.username)
                                    }
                                    is AuthResult.Error -> {
                                        errorMessage = result.message
                                    }
                                    null -> {
                                        val fallback = normalizedEmail.substringBefore("@").replaceFirstChar { it.uppercase() }
                                        onAuthSuccess(if (fallback.isNotBlank()) fallback else "Player")
                                    }
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "auth_submit_button"
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // CONTINUE WITH GOOGLE
            OutlinedButton(
                onClick = {
                    errorMessage = null
                    val activity = context as? Activity
                    if (activity == null) {
                        errorMessage = "Activity context is required for Google Sign-In"
                        return@OutlinedButton
                    }

                    val clientId = authService?.getGoogleWebClientId()?.takeIf { it.isNotBlank() }
                        ?: "619659849137-webclient.apps.googleusercontent.com"

                    coroutineScope.launch {
                        try {
                            isLoading = true
                            val credentialManager = CredentialManager.create(context)
                            val googleIdOption = GetGoogleIdOption.Builder()
                                .setFilterByAuthorizedAccounts(false)
                                .setServerClientId(clientId)
                                .setAutoSelectEnabled(false)
                                .build()

                            val request = GetCredentialRequest.Builder()
                                .addCredentialOption(googleIdOption)
                                .build()

                            val result = credentialManager.getCredential(activity, request)
                            val credential = result.credential
                            if (credential is androidx.credentials.CustomCredential &&
                                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                            ) {
                                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                                val idToken = googleIdTokenCredential.idToken

                                val authResult = authService?.signInWithGoogle(idToken)
                                isLoading = false
                                when (authResult) {
                                    is AuthResult.Success -> {
                                        onAuthSuccess(authResult.data.username)
                                    }
                                    is AuthResult.Error -> {
                                        errorMessage = authResult.message
                                    }
                                    null -> {
                                        errorMessage = "Authentication service unavailable"
                                    }
                                }
                            } else {
                                isLoading = false
                                errorMessage = "Unexpected credential returned from Google Sign-In"
                            }
                        } catch (e: GetCredentialCancellationException) {
                            isLoading = false
                            // User dismissed native Google account picker; no error display
                        } catch (e: Exception) {
                            isLoading = false
                            val msg = e.localizedMessage ?: "Google Sign-In failed"
                            errorMessage = when {
                                msg.contains("16") || msg.contains("12500") || msg.contains("Cannot find a matching credential") ->
                                    "Google Play Services: Unable to sign in with Google on this device. Please check Google Account settings."
                                else -> msg
                            }
                        }
                    }
                },
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, CardSurfaceBorder),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = CardSurface,
                    contentColor = TextPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("auth_google_button")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🌐", fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "CONTINUE WITH GOOGLE",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Switch between Sign In / Sign Up
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (isSignUp) "Already have an account? " else "Don't have an account? ",
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = if (isSignUp) "Sign In" else "Create Account",
                    color = NeonCyan,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable {
                            isSignUp = !isSignUp
                            errorMessage = null
                            successMessage = null
                        }
                        .padding(4.dp)
                        .testTag("auth_toggle_mode_link")
                )
            }
        }
    }

    // Email Verification Dialog
    if (showVerificationDialog) {
        AlertDialog(
            onDismissRequest = { /* Require explicit action */ },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "✉️", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("VERIFY YOUR EMAIL", fontWeight = FontWeight.Bold, color = TextPrimary)
                }
            },
            text = {
                Column {
                    Text(
                        text = "A verification link was dispatched to:",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = registeredEmail.ifBlank { email },
                        color = NeonCyan,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Please click the link in your email to activate and secure your Brain Battle profile.",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodySmall
                    )

                    if (verificationStatusMessage != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = verificationStatusMessage ?: "",
                            color = if (verificationStatusMessage?.contains("verified", ignoreCase = true) == true) NeonGreen else NeonAmber,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                isCheckingVerification = true
                                val result = authService?.reloadUser()
                                isCheckingVerification = false
                                when (result) {
                                    is AuthResult.Success -> {
                                        if (result.data) {
                                            verificationStatusMessage = "Email verified successfully!"
                                            showVerificationDialog = false
                                            onAuthSuccess(username.ifBlank { "Player" })
                                        } else {
                                            verificationStatusMessage = "Not verified yet. Check your inbox and click the link."
                                        }
                                    }
                                    is AuthResult.Error -> {
                                        verificationStatusMessage = result.message
                                    }
                                    null -> {
                                        showVerificationDialog = false
                                        onAuthSuccess(username.ifBlank { "Player" })
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("check_verification_button")
                    ) {
                        if (isCheckingVerification) {
                            CircularProgressIndicator(color = BackgroundDark, modifier = Modifier.size(18.dp))
                        } else {
                            Text("I HAVE VERIFIED", color = BackgroundDark, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(
                            onClick = {
                                coroutineScope.launch {
                                    val res = authService?.sendEmailVerification()
                                    if (res is AuthResult.Success) {
                                        verificationStatusMessage = "A fresh verification email was sent!"
                                    } else if (res is AuthResult.Error) {
                                        verificationStatusMessage = res.message
                                    }
                                }
                            },
                            modifier = Modifier.testTag("resend_verification_button")
                        ) {
                            Text("RESEND LINK", color = NeonCyan, fontSize = 12.sp)
                        }

                        TextButton(
                            onClick = {
                                showVerificationDialog = false
                                onAuthSuccess(username.ifBlank { "Player" })
                            },
                            modifier = Modifier.testTag("skip_verification_button")
                        ) {
                            Text("CONTINUE ANYWAY", color = TextSecondary, fontSize = 12.sp)
                        }
                    }
                }
            },
            containerColor = CardSurface,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.testTag("verification_dialog")
        )
    }



    // Password Reset Dialog
    if (forgotPasswordOpen) {
        AlertDialog(
            onDismissRequest = { forgotPasswordOpen = false },
            title = {
                Text("Reset Password", fontWeight = FontWeight.Bold, color = TextPrimary)
            },
            text = {
                Column {
                    if (resetEmailSent) {
                        Text(
                            text = "A password reset link has been dispatched to $resetEmail. Please check your inbox (and spam folder) to choose a new password.",
                            color = NeonGreen,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        Text(
                            text = "Enter your registered email address and we'll send you instructions to reset your password.",
                            color = TextSecondary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        if (resetError != null) {
                            Text(
                                text = resetError ?: "",
                                color = NeonRed,
                                style = MaterialTheme.typography.bodySmall
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        OutlinedTextField(
                            value = resetEmail,
                            onValueChange = {
                                resetEmail = it
                                resetError = null
                            },
                            label = { Text("Email Address", color = TextSecondary) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = CardSurfaceBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_reset_email_field")
                        )
                    }
                }
            },
            confirmButton = {
                if (resetEmailSent) {
                    Button(
                        onClick = { forgotPasswordOpen = false },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("auth_reset_done_button")
                    ) {
                        Text("DONE", color = BackgroundDark, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = {
                            if (resetEmail.isBlank() || !resetEmail.contains("@")) {
                                resetError = "Please enter a valid email address"
                                return@Button
                            }
                            isResetLoading = true
                            coroutineScope.launch {
                                val result = authService?.sendPasswordResetEmail(resetEmail.trim().lowercase())
                                isResetLoading = false
                                when (result) {
                                    is AuthResult.Success -> {
                                        resetEmailSent = true
                                    }
                                    is AuthResult.Error -> {
                                        resetError = result.message
                                    }
                                    null -> {
                                        resetEmailSent = true
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("auth_send_reset_button")
                    ) {
                        if (isResetLoading) {
                            CircularProgressIndicator(color = BackgroundDark, modifier = Modifier.size(18.dp))
                        } else {
                            Text("SEND RESET LINK", color = BackgroundDark, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            dismissButton = {
                if (!resetEmailSent) {
                    TextButton(
                        onClick = { forgotPasswordOpen = false },
                        modifier = Modifier.testTag("auth_cancel_reset_button")
                    ) {
                        Text("CANCEL", color = TextSecondary)
                    }
                }
            },
            containerColor = CardSurface,
            shape = RoundedCornerShape(20.dp)
        )
    }
}
