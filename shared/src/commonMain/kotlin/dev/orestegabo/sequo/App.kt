package dev.orestegabo.sequo

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import dev.orestegabo.sequo.core.auth.CurrentUser
import dev.orestegabo.sequo.core.auth.GoogleSignInResult
import dev.orestegabo.sequo.core.auth.SecureSessionCache
import dev.orestegabo.sequo.core.auth.SecureSessionUnlockResult
import dev.orestegabo.sequo.core.auth.rememberAuthDeviceId
import dev.orestegabo.sequo.core.auth.rememberAuthSessionStore
import dev.orestegabo.sequo.core.auth.rememberBiometricAuthenticator
import dev.orestegabo.sequo.core.auth.rememberSecureTokenStorage
import dev.orestegabo.sequo.feature.auth.AuthScreen
import dev.orestegabo.sequo.feature.legal.LegalInitialTab
import dev.orestegabo.sequo.feature.legal.LegalScreen
import dev.orestegabo.sequo.feature.settings.AppLanguage
import dev.orestegabo.sequo.feature.settings.AppThemePreference
import dev.orestegabo.sequo.feature.settings.LocalAppLanguage
import dev.orestegabo.sequo.feature.settings.appText
import dev.orestegabo.sequo.feature.settings.rememberAppPreferencesStore
import dev.orestegabo.sequo.feature.splash.SplashScreen
import dev.orestegabo.sequo.theme.SequoTheme
import dev.orestegabo.sequo.ui.app.SequoShell
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import sequo.shared.generated.resources.*

@Composable
@Preview
fun App(
    onGoogleSignIn: suspend () -> GoogleSignInResult = {
        GoogleSignInResult.Failure("Google sign-in is not configured on this platform yet.")
    },
    onGoogleSignOut: suspend () -> Unit = {},
    onHomeEntered: (AppLanguage) -> Unit = {},
    openNotificationsRequest: Int = 0,
) {
    val appPreferencesStore = rememberAppPreferencesStore()
    val appScope = rememberCoroutineScope()
    var language by rememberSaveable { mutableStateOf(AppLanguage.English) }
    var themePreference by rememberSaveable { mutableStateOf(AppThemePreference.System) }
    val systemDarkTheme = isSystemInDarkTheme()
    val darkTheme = when (themePreference) {
        AppThemePreference.System -> systemDarkTheme
        AppThemePreference.Light -> false
        AppThemePreference.Dark -> true
    }

    LaunchedEffect(appPreferencesStore) {
        appPreferencesStore.getLanguage()?.let { language = it }
        appPreferencesStore.getThemePreference()?.let { themePreference = it }
    }

    SequoTheme(darkTheme = darkTheme) {
        SequoApp(
            language = language,
            onLanguageChange = { selectedLanguage ->
                language = selectedLanguage
                appScope.launch { appPreferencesStore.saveLanguage(selectedLanguage) }
            },
            themePreference = themePreference,
            onThemePreferenceChange = { selectedTheme ->
                themePreference = selectedTheme
                appScope.launch { appPreferencesStore.saveThemePreference(selectedTheme) }
            },
            onGoogleSignIn = onGoogleSignIn,
            onGoogleSignOut = onGoogleSignOut,
            onHomeEntered = onHomeEntered,
            openNotificationsRequest = openNotificationsRequest,
        )
    }
}

@Composable
private fun SequoApp(
    language: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit,
    themePreference: AppThemePreference,
    onThemePreferenceChange: (AppThemePreference) -> Unit,
    onGoogleSignIn: suspend () -> GoogleSignInResult,
    onGoogleSignOut: suspend () -> Unit,
    onHomeEntered: (AppLanguage) -> Unit,
    openNotificationsRequest: Int,
) {
    var showSplash by rememberSaveable { mutableStateOf(shouldShowInAppSplash) }
    var isAuthenticated by rememberSaveable { mutableStateOf(false) }
    var guestMode by rememberSaveable { mutableStateOf(false) }
    var legalScreenTab by rememberSaveable { mutableStateOf<LegalInitialTab?>(null) }
    var authErrorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var googleSignInInProgress by rememberSaveable { mutableStateOf(false) }
    var emailAuthInProgress by rememberSaveable { mutableStateOf(false) }
    var sessionRestoreChecked by rememberSaveable { mutableStateOf(false) }
    var currentUser by remember { mutableStateOf<CurrentUser?>(null) }
    var rememberedUser by remember { mutableStateOf<CurrentUser?>(null) }
    val authSessionStore = rememberAuthSessionStore()
    val secureTokenStorage = rememberSecureTokenStorage()
    val biometricAuthenticator = rememberBiometricAuthenticator()
    val authDeviceId = rememberAuthDeviceId()
    val secureSessionCache = remember(secureTokenStorage, biometricAuthenticator) {
        SecureSessionCache(
            tokenStorage = secureTokenStorage,
            biometricAuthenticator = biometricAuthenticator,
        )
    }
    val authRepository = remember(authSessionStore, secureSessionCache, authDeviceId) {
        AuthRepository(
            authApiClient = AuthApiClient(deviceIdProvider = { authDeviceId }),
            sessionStore = authSessionStore,
            secureSessionCache = secureSessionCache,
        )
    }
    val scope = rememberCoroutineScope()

    DisposableEffect(authRepository) {
        onDispose { authRepository.close() }
    }

    LaunchedEffect(authRepository) {
        rememberedUser = authRepository.getRememberedUser()

        if (authRepository.getSavedSession() != null && !authRepository.hasCachedRefreshToken()) {
            authRepository.migrateSavedSessionToSecureCache()
        }

        val restoredUser = runCatching { authRepository.restoreSavedSession() }.getOrNull()
        if (restoredUser != null) {
            currentUser = restoredUser
            rememberedUser = restoredUser
            isAuthenticated = true
            guestMode = false
            authErrorMessage = null
            sessionRestoreChecked = true
            return@LaunchedEffect
        }

        if (authRepository.hasCachedRefreshToken()) {
            when (val unlockResult = authRepository.unlockCachedSession()) {
                is SecureSessionUnlockResult.Unlocked -> {
                    val user = runCatching { authRepository.restoreSavedSession() }.getOrNull()
                    currentUser = user
                    user?.let {
                        rememberedUser = it
                        authRepository.rememberUser(it)
                    }
                    isAuthenticated = true
                    guestMode = false
                    authErrorMessage = null
                }
                SecureSessionUnlockResult.Cancelled,
                SecureSessionUnlockResult.NoCachedSession,
                -> {
                    currentUser = null
                    isAuthenticated = false
                    guestMode = false
                }
                is SecureSessionUnlockResult.Unavailable -> {
                    currentUser = null
                    isAuthenticated = false
                    guestMode = false
                    authErrorMessage = "Biometric unlock is not available on this device. Sign in again to continue."
                }
                is SecureSessionUnlockResult.Failed -> {
                    currentUser = null
                    isAuthenticated = false
                    guestMode = false
                    authErrorMessage = unlockResult.message ?: "Biometric unlock failed. Sign in again to continue."
                }
            }
        }
        sessionRestoreChecked = true
    }

    if (showSplash) {
        SplashScreen(onTimeout = { showSplash = false })
        return
    }

    if (!sessionRestoreChecked) {
        SplashScreen()
        return
    }

    legalScreenTab?.let { tab ->
        LegalScreen(
            initialTab = tab,
            onBack = { legalScreenTab = null },
        )
        return
    }

    if (!isAuthenticated && !guestMode) {
        CompositionLocalProvider(LocalAppLanguage provides language) {
            AuthScreen(
                language = language,
                onLanguageChange = onLanguageChange,
                emailAuthInProgress = emailAuthInProgress,
                rememberedUser = rememberedUser,
                onContinueRememberedUser = {
                    if (emailAuthInProgress || googleSignInInProgress) return@AuthScreen
                    scope.launch {
                        emailAuthInProgress = true
                        authErrorMessage = null
                        try {
                            runCatching {
                                authRepository.restoreSavedSession()
                            }.onSuccess { user ->
                                if (user != null) {
                                    currentUser = user
                                    rememberedUser = user
                                    isAuthenticated = true
                                    guestMode = false
                                } else {
                                    authErrorMessage = "Please sign in again to continue."
                                }
                            }.onFailure { error ->
                                if (error is CancellationException) throw error
                                authErrorMessage = error.message ?: "Please sign in again to continue."
                            }
                        } finally {
                            emailAuthInProgress = false
                        }
                    }
                },
                onEmailLogin = { email, password ->
                    if (emailAuthInProgress) return@AuthScreen
                    scope.launch {
                        emailAuthInProgress = true
                        authErrorMessage = null
                        try {
                            runCatching {
                                authRepository.loginWithEmail(email, password)
                                authRepository.currentUser()
                            }.onSuccess { user ->
                                currentUser = user
                                user?.let {
                                    rememberedUser = it
                                    authRepository.rememberUser(it)
                                }
                                isAuthenticated = user != null
                                guestMode = false
                            }.onFailure { error ->
                                if (error is CancellationException) throw error
                                authErrorMessage = error.message ?: "Email sign-in failed."
                            }
                        } finally {
                            emailAuthInProgress = false
                        }
                    }
                },
                onEmailSignUp = { email, password, name ->
                    if (emailAuthInProgress) return@AuthScreen
                    scope.launch {
                        emailAuthInProgress = true
                        authErrorMessage = null
                        try {
                            runCatching {
                                authRepository.signUpWithEmail(email, password, name)
                                authRepository.currentUser()
                            }.onSuccess { user ->
                                currentUser = user
                                user?.let {
                                    rememberedUser = it
                                    authRepository.rememberUser(it)
                                }
                                isAuthenticated = user != null
                                guestMode = false
                            }.onFailure { error ->
                                if (error is CancellationException) throw error
                                authErrorMessage = error.message ?: "Email sign-up failed."
                            }
                        } finally {
                            emailAuthInProgress = false
                        }
                    }
                },
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
                                        authRepository.currentUser()
                                    }.onSuccess {
                                        authErrorMessage = null
                                        currentUser = it
                                        it?.let { user ->
                                            rememberedUser = user
                                            authRepository.rememberUser(user)
                                        }
                                        isAuthenticated = it != null
                                        guestMode = false
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
                onAppleLogin = {
                    authErrorMessage = "Apple sign-in needs native identity token wiring before it can complete."
                },
                onFacebookLogin = {
                    authErrorMessage = "Facebook sign-in needs native provider token wiring before it can complete."
                },
                onWhatsAppLogin = {
                    authErrorMessage = "WhatsApp sign-in needs the OTP flow wired into this compact launcher."
                },
                onPasskeyLogin = {
                    authErrorMessage = "Passkey sign-in needs native passkey assertion wiring before it can complete."
                },
                onSequoLogin = {
                    authErrorMessage = "Login by Sequo needs cross-device approval wiring before it can complete."
                },
                onSkipAuth = {
                    currentUser = null
                    isAuthenticated = false
                    guestMode = true
                    authErrorMessage = null
                },
                onPrivacyTermsClick = { legalScreenTab = LegalInitialTab.Privacy },
            )
            authErrorMessage?.let { message ->
                AlertDialog(
                    onDismissRequest = { authErrorMessage = null },
                    title = { Text(appText(Res.string.auth_dialog_title)) },
                    text = { Text(message) },
                    confirmButton = {
                        TextButton(onClick = { authErrorMessage = null }) {
                            Text(appText(Res.string.common_ok))
                        }
                    },
                )
            }
        }
        return
    }

    CompositionLocalProvider(LocalAppLanguage provides language) {
        SequoShell(
            currentUser = currentUser,
            isGuest = guestMode,
            language = language,
            onLanguageChange = onLanguageChange,
            themePreference = themePreference,
            onThemePreferenceChange = onThemePreferenceChange,
            onOpenLegal = { legalScreenTab = it },
            onHomeEntered = { onHomeEntered(language) },
            openNotificationsRequest = openNotificationsRequest,
            onSignInRequested = {
                guestMode = false
                isAuthenticated = false
                currentUser = null
            },
            onLogout = {
                scope.launch {
                    authRepository.logout()
                    onGoogleSignOut()
                    currentUser = null
                    isAuthenticated = false
                    guestMode = false
                }
            },
        )
    }
}

internal expect val shouldShowInAppSplash: Boolean
