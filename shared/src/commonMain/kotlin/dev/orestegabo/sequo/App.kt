package dev.orestegabo.sequo

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import dev.orestegabo.sequo.core.auth.AuthApiClient
import dev.orestegabo.sequo.core.auth.AuthRepository
import dev.orestegabo.sequo.core.auth.GoogleSignInResult
import dev.orestegabo.sequo.core.auth.rememberAuthSessionStore
import dev.orestegabo.sequo.feature.auth.AuthScreen
import dev.orestegabo.sequo.feature.legal.LegalScreen
import dev.orestegabo.sequo.feature.settings.AppLanguage
import dev.orestegabo.sequo.feature.splash.SplashScreen
import dev.orestegabo.sequo.theme.SequoTheme
import dev.orestegabo.sequo.ui.app.SequoShell
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@Composable
@Preview
fun App(
    onGoogleSignIn: suspend () -> GoogleSignInResult = {
        GoogleSignInResult.Failure("Google sign-in is not configured on this platform yet.")
    },
) {
    SequoTheme {
        SequoApp(onGoogleSignIn = onGoogleSignIn)
    }
}

@Composable
private fun SequoApp(
    onGoogleSignIn: suspend () -> GoogleSignInResult,
) {
    var showSplash by rememberSaveable { mutableStateOf(true) }
    var isAuthenticated by rememberSaveable { mutableStateOf(false) }
    var showLegalScreen by rememberSaveable { mutableStateOf(false) }
    var language by rememberSaveable { mutableStateOf(AppLanguage.English) }
    var authErrorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var googleSignInInProgress by rememberSaveable { mutableStateOf(false) }
    val authSessionStore = rememberAuthSessionStore()
    val authRepository = remember(authSessionStore) {
        AuthRepository(
            authApiClient = AuthApiClient(),
            sessionStore = authSessionStore,
        )
    }
    val scope = rememberCoroutineScope()

    DisposableEffect(authRepository) {
        onDispose { authRepository.close() }
    }

    LaunchedEffect(authRepository) {
        isAuthenticated = authRepository.getSavedSession() != null
    }

    if (showSplash) {
        SplashScreen(onTimeout = { showSplash = false })
        return
    }

    if (!isAuthenticated) {
        if (showLegalScreen) {
            LegalScreen(onBack = { showLegalScreen = false })
        } else {
            AuthScreen(
                language = language,
                onLanguageChange = { language = it },
                onLogin = { isAuthenticated = true },
                googleSignInInProgress = googleSignInInProgress,
                onGoogleLogin = {
                    if (googleSignInInProgress) return@AuthScreen
                    scope.launch {
                        googleSignInInProgress = true
                        authErrorMessage = null
                        try {
                            val result = runCatching {
                                onGoogleSignIn()
                            }.getOrElse { error ->
                                if (error is CancellationException) throw error
                                GoogleSignInResult.Failure(error.message ?: "Google sign-in failed.")
                            }

                            when (result) {
                                is GoogleSignInResult.Success -> {
                                    runCatching {
                                        authRepository.loginWithGoogle(result.idToken)
                                    }.onSuccess {
                                        authErrorMessage = null
                                        isAuthenticated = true
                                    }.onFailure { error ->
                                        if (error is CancellationException) throw error
                                        authErrorMessage = error.message ?: "Backend Google login failed."
                                    }
                                }
                                GoogleSignInResult.Cancelled -> Unit
                                is GoogleSignInResult.Failure -> {
                                    authErrorMessage = result.message
                                }
                            }
                        } finally {
                            googleSignInInProgress = false
                        }
                    }
                },
                onAppleLogin = { isAuthenticated = true },
                onPrivacyTermsClick = { showLegalScreen = true },
            )
        }
        authErrorMessage?.let { message ->
            AlertDialog(
                onDismissRequest = { authErrorMessage = null },
                title = { Text("Google sign-in") },
                text = { Text(message) },
                confirmButton = {
                    TextButton(onClick = { authErrorMessage = null }) {
                        Text("OK")
                    }
                },
            )
        }
        return
    }

    SequoShell()
}
