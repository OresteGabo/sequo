package dev.orestegabo.sequo.core.auth

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class AuthRepository(
    private val authApiClient: AuthApiClient,
    private val sessionStore: AuthSessionStore,
    private val secureSessionCache: SecureSessionCache,
    private val logger: AuthDebugLogger = AuthDebugLogger,
) {
    private val sessionMutation = Mutex()

    suspend fun localFixtureAccounts(): List<LocalFixtureAccount> = authApiClient.localFixtureAccounts()

    suspend fun loginWithLocalFixture(accountKey: String): AuthSession = sessionMutation.withLock {
        authApiClient.loginWithLocalFixture(accountKey).also { saveSession(it) }
    }

    suspend fun loginWithGoogle(idToken: String): AuthSession = sessionMutation.withLock {
        if (idToken.isBlank()) {
            throw GoogleIdTokenMissingException()
        }

        logger.logGoogleIdToken(idToken)
        val response = authApiClient.loginWithGoogle(idToken)
        logger.logBackendStatus(response.statusCode)
        saveSession(response.session)
        response.session
    }

    suspend fun loginWithFacebook(accessToken: String): AuthSession = sessionMutation.withLock {
        if (accessToken.isBlank()) {
            throw FacebookAccessTokenMissingException()
        }

        val response = authApiClient.loginWithFacebook(accessToken)
        logger.logBackendStatus(response.statusCode, "Facebook")
        saveSession(response.session)
        response.session
    }

    suspend fun signUpWithEmail(email: String, password: String, name: String?): AuthSession = sessionMutation.withLock {
        val response = authApiClient.signUpWithEmail(email = email, password = password, name = name)
        saveSession(response.session)
        response.session
    }

    suspend fun loginWithEmail(email: String, password: String): AuthSession = sessionMutation.withLock {
        val response = authApiClient.loginWithEmail(email = email, password = password)
        saveSession(response.session)
        response.session
    }

    internal suspend fun savedAddresses(): List<SavedDeliveryAddress> {
        val session = checkNotNull(sessionStore.get())
        return authApiClient.savedAddresses(session.accessToken)
    }

    suspend fun currentUser(): CurrentUser? {
        val session = sessionStore.get() ?: return null
        return authApiClient.currentUser(session.accessToken)
    }

    suspend fun restoreSavedSession(): CurrentUser? = sessionMutation.withLock {
        val session = sessionStore.get() ?: return@withLock null

        runCatching {
            authApiClient.currentUser(session.accessToken)
        }.getOrElse { firstError ->
            if ((firstError as? AuthApiException)?.statusCode != 401) throw firstError

            val refreshedSession = authApiClient.refreshSession(session.refreshToken)
            saveSession(refreshedSession)
            authApiClient.currentUser(refreshedSession.accessToken)
        }.also { user ->
            sessionStore.saveRememberedUser(user)
        }
    }

    suspend fun getRememberedUser(): CurrentUser? =
        sessionStore.getRememberedUser()

    suspend fun rememberUser(user: CurrentUser) {
        sessionStore.saveRememberedUser(user)
    }

    suspend fun getSavedSession(): AuthSession? =
        sessionStore.get()

    suspend fun hasCachedRefreshToken(): Boolean =
        secureSessionCache.hasCachedRefreshToken()

    suspend fun migrateSavedSessionToSecureCache() {
        val session = sessionStore.get() ?: return
        secureSessionCache.cache(session)
    }

    suspend fun unlockCachedSession(promptConfig: BiometricPromptConfig = BiometricPromptConfig()): SecureSessionUnlockResult =
        secureSessionCache.unlockCachedSession(promptConfig)

    suspend fun logout() = sessionMutation.withLock {
        val session = sessionStore.get()
        if (session != null) {
            runCatching {
                authApiClient.logout(session.refreshToken)
            }
        }
        sessionStore.clear()
        secureSessionCache.clearSession()
    }

    fun close() {
        authApiClient.close()
    }

    private suspend fun saveSession(session: AuthSession) {
        sessionStore.save(session)
        secureSessionCache.cache(session)
    }
}

class GoogleIdTokenMissingException : Exception(
    AuthUserMessage.GoogleSignInTemporarilyUnavailable.name,
)

class FacebookAccessTokenMissingException : Exception(
    AuthUserMessage.SocialSignInUnavailable.name,
)

object AuthDebugLogger {
    fun logGoogleIdToken(idToken: String) {
        println("Google sign-in idToken present=${idToken.isNotBlank()} length=${idToken.length}")
    }

    fun logBackendStatus(statusCode: Int, provider: String = "Google") {
        println("$provider backend login status=$statusCode")
    }
}
