package dev.orestegabo.sequo.core.auth

class AuthRepository(
    private val authApiClient: AuthApiClient,
    private val sessionStore: AuthSessionStore,
    private val logger: AuthDebugLogger = AuthDebugLogger,
) {
    suspend fun loginWithGoogle(idToken: String): AuthSession {
        if (idToken.isBlank()) {
            throw GoogleIdTokenMissingException()
        }

        logger.logGoogleIdToken(idToken)
        val response = authApiClient.loginWithGoogle(idToken)
        logger.logBackendStatus(response.statusCode)
        sessionStore.save(response.session)
        return response.session
    }

    suspend fun signUpWithEmail(email: String, password: String, name: String?): AuthSession {
        val response = authApiClient.signUpWithEmail(email = email, password = password, name = name)
        sessionStore.save(response.session)
        return response.session
    }

    suspend fun loginWithEmail(email: String, password: String): AuthSession {
        val response = authApiClient.loginWithEmail(email = email, password = password)
        sessionStore.save(response.session)
        return response.session
    }

    suspend fun currentUser(): CurrentUser? {
        val session = sessionStore.get() ?: return null
        return authApiClient.currentUser(session.accessToken)
    }

    suspend fun getSavedSession(): AuthSession? =
        sessionStore.get()

    suspend fun logout() {
        sessionStore.clear()
    }

    fun close() {
        authApiClient.close()
    }
}

class GoogleIdTokenMissingException : Exception(
    "Google ID token missing. Check OAuth client configuration.",
)

object AuthDebugLogger {
    fun logGoogleIdToken(idToken: String) {
        println("Google sign-in idToken present=${idToken.isNotBlank()} length=${idToken.length}")
    }

    fun logBackendStatus(statusCode: Int) {
        println("Google backend login status=$statusCode")
    }
}
