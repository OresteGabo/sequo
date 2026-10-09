package dev.orestegabo.sequo.core.auth

class AuthRepository(
    private val authApiClient: AuthApiClient,
    private val sessionStore: AuthSessionStore,
    private val secureSessionCache: SecureSessionCache,
    private val logger: AuthDebugLogger = AuthDebugLogger,
) {
    suspend fun loginWithGoogle(idToken: String): AuthSession {
        if (idToken.isBlank()) {
            throw GoogleIdTokenMissingException()
        }

        logger.logGoogleIdToken(idToken)
        val response = authApiClient.loginWithGoogle(idToken)
        logger.logBackendStatus(response.statusCode)
        saveSession(response.session)
        return response.session
    }

    suspend fun loginWithFacebook(accessToken: String): AuthSession {
        if (accessToken.isBlank()) {
            throw FacebookAccessTokenMissingException()
        }

        val response = authApiClient.loginWithFacebook(accessToken)
        logger.logBackendStatus(response.statusCode, "Facebook")
        saveSession(response.session)
        return response.session
    }

    suspend fun signUpWithEmail(email: String, password: String, name: String?): AuthSession {
        val response = authApiClient.signUpWithEmail(email = email, password = password, name = name)
        saveSession(response.session)
        return response.session
    }

    suspend fun loginWithEmail(email: String, password: String): AuthSession {
        val response = authApiClient.loginWithEmail(email = email, password = password)
        saveSession(response.session)
        return response.session
    }

    suspend fun currentUser(): CurrentUser? {
        val session = sessionStore.get() ?: return null
        return authApiClient.currentUser(session.accessToken)
    }

    suspend fun restoreSavedSession(): CurrentUser? {
        val session = sessionStore.get() ?: return null

        return runCatching {
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

    suspend fun unlockCachedSession(): SecureSessionUnlockResult =
        secureSessionCache.unlockCachedSession(
            BiometricPromptConfig(
                title = "Unlock Sequo",
                subtitle = "Use biometrics to restore your saved session.",
            ),
        )

    suspend fun logout() {
        sessionStore.get()?.let { session ->
            authApiClient.logout(session.refreshToken)
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
    "Google sign-in is temporarily unavailable. Please use another sign-in option or try again later.",
)

class FacebookAccessTokenMissingException : Exception(
    "Facebook sign-in is temporarily unavailable. Please use another sign-in option or try again later.",
)

object AuthDebugLogger {
    fun logGoogleIdToken(idToken: String) {
        println("Google sign-in idToken present=${idToken.isNotBlank()} length=${idToken.length}")
    }

    fun logBackendStatus(statusCode: Int, provider: String = "Google") {
        println("$provider backend login status=$statusCode")
    }
}
