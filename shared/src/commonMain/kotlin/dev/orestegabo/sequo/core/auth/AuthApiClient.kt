package dev.orestegabo.sequo.core.auth

import dev.orestegabo.sequo.core.network.NetworkConfig
import dev.orestegabo.sequo.core.network.SequoApiException
import dev.orestegabo.sequo.core.network.createSequoHttpClient
import dev.orestegabo.sequo.core.network.defaultNetworkJson
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.SerialName

class AuthApiClient(
    private val baseUrl: String = NetworkConfig.BaseUrl,
    private val httpClient: HttpClient = createSequoHttpClient(baseUrl),
    private val deviceIdProvider: () -> String,
    private val environment: String = NetworkConfig.Environment,
) {
    suspend fun loginWithDemo(accessCode: String): AuthSession {
        check(environment == "DEMO") { "Demo sign-in is unavailable in this build." }
        return httpClient.post("${baseUrl.trimEnd('/')}/api/auth/demo/session") {
            setBody(DemoSessionRequest(accessCode.trim(), deviceIdProvider()))
        }.body()
    }

    suspend fun localFixtureAccounts(): List<LocalFixtureAccount> {
        if (!allowsLocalFixtureAccess(environment, baseUrl)) return emptyList()
        val access = httpClient.get("${baseUrl.trimEnd('/')}/api/auth/development-access") {
            url.parameters.append("appSource", "SEQUO_APP")
        }.body<LocalFixtureAccess>()
        return if (access.enabled && access.mode == "LOCAL_FIXTURE") access.accounts else emptyList()
    }

    suspend fun loginWithLocalFixture(accountKey: String): AuthSession {
        check(allowsLocalFixtureAccess(environment, baseUrl)) { "Local practice access is unavailable." }
        check(localFixtureAccounts().any { it.key == accountKey }) { "Practice account is unavailable." }
        return httpClient.post("${baseUrl.trimEnd('/')}/api/auth/development-session") {
            headers.append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(LocalFixtureSessionRequest(appSource = "SEQUO_APP", accountKey = accountKey, deviceId = deviceIdProvider()))
        }.body()
    }

    suspend fun loginWithGoogle(idToken: String): AuthApiResponse {
        return loginWithSocialToken(SocialLoginProvider.Google, idToken)
    }

    suspend fun loginWithFacebook(accessToken: String): AuthApiResponse {
        return loginWithSocialToken(SocialLoginProvider.Facebook, accessToken)
    }

    suspend fun appleSignInChallenge(): AppleSignInChallenge =
        httpClient.post("${baseUrl.trimEnd('/')}/api/auth/apple/challenge") {
            headers.append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(DeviceRequest(deviceIdProvider(), AppSource.SequoApp))
        }.body()

    suspend fun loginWithApple(idToken: String, challengeId: String): AuthApiResponse =
        loginWithSocialToken(SocialLoginProvider.Apple, idToken, challengeId)

    private suspend fun loginWithSocialToken(provider: SocialLoginProvider, token: String, challengeId: String? = null): AuthApiResponse {
        val response = try {
            httpClient.post("${baseUrl.trimEnd('/')}/api/auth/social/${provider.wireName}") {
                headers.append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(
                    SocialLoginRequest(
                        token = token,
                        challengeId = challengeId,
                        device = DeviceRequest(
                            deviceId = deviceIdProvider(),
                            appSource = AppSource.SequoApp,
                        ),
                    ),
                )
            }
        } catch (error: SequoApiException) {
            throw AuthApiException(
                statusCode = error.statusCode,
                userMessage = socialLoginFailureMessage(provider, error.statusCode, error.responseBody),
                cause = error,
            )
        }
        return response.toAuthApiResponse(provider)
    }

    suspend fun signUpWithEmail(email: String, password: String, name: String?): AuthApiResponse {
        val response = try {
            httpClient.post("${baseUrl.trimEnd('/')}/api/auth/signup") {
                headers.append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(EmailSignUpRequest(email = email, password = password, name = name?.takeIf { it.isNotBlank() }))
            }
        } catch (error: SequoApiException) {
            throw AuthApiException(
                statusCode = error.statusCode,
                userMessage = emailAuthFailureMessage(error.statusCode, error.responseBody),
                cause = error,
            )
        }
        return response.toAuthApiResponse()
    }

    suspend fun loginWithEmail(email: String, password: String): AuthApiResponse {
        val response = try {
            httpClient.post("${baseUrl.trimEnd('/')}/api/auth/login") {
                headers.append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(EmailLoginRequest(email = email, password = password))
            }
        } catch (error: SequoApiException) {
            throw AuthApiException(
                statusCode = error.statusCode,
                userMessage = emailAuthFailureMessage(error.statusCode, error.responseBody),
                cause = error,
            )
        }
        return response.toAuthApiResponse()
    }

    internal suspend fun savedAddresses(accessToken: String): List<SavedDeliveryAddress> =
        httpClient.get("${baseUrl.trimEnd('/')}/api/customer/addresses") {
            bearerAuth(accessToken)
        }.body()

    suspend fun currentUser(accessToken: String): CurrentUser {
        return try {
            httpClient.get("${baseUrl.trimEnd('/')}/api/auth/me") {
                bearerAuth(accessToken)
            }.body()
        } catch (error: SequoApiException) {
            throw AuthApiException(
                statusCode = error.statusCode,
                userMessage = AuthUserMessage.ProfileLoadFailed,
                cause = error,
            )
        }
    }

    suspend fun refreshSession(refreshToken: String): AuthSession {
        return try {
            httpClient.post("${baseUrl.trimEnd('/')}/api/auth/refresh") {
                headers.append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(RefreshRequest(refreshToken = refreshToken, deviceId = deviceIdProvider()))
            }.body()
        } catch (error: SequoApiException) {
            throw AuthApiException(
                statusCode = error.statusCode,
                userMessage = AuthUserMessage.SessionRestoreFailed,
                cause = error,
            )
        }
    }

    suspend fun logout(refreshToken: String) {
        try {
            httpClient.post("${baseUrl.trimEnd('/')}/api/auth/logout") {
                headers.append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(LogoutRequest(refreshToken))
            }
        } catch (_: SequoApiException) {
            // Local logout must still complete when the network is unavailable.
        }
    }

    fun close() {
        httpClient.close()
    }

    private suspend fun HttpResponse.toAuthApiResponse(provider: SocialLoginProvider = SocialLoginProvider.Google): AuthApiResponse {
        val statusCode = status.value
        if (statusCode !in 200..299) {
            throw AuthApiException(
                statusCode = statusCode,
                userMessage = socialLoginFailureMessage(provider, statusCode, bodyAsText()),
            )
        }

        return AuthApiResponse(
            statusCode = statusCode,
            session = body(),
        )
    }
}

private fun emailAuthFailureMessage(statusCode: Int, @Suppress("UNUSED_PARAMETER") responseBody: String?): AuthUserMessage {
    return when (statusCode) {
        400 -> AuthUserMessage.EmailFormatInvalid
        401 -> AuthUserMessage.EmailPasswordIncorrect
        409 -> AuthUserMessage.AccountUsesAnotherMethod
        429 -> AuthUserMessage.TooManyAttempts
        else -> AuthUserMessage.AuthenticationUnavailable
    }
}

private fun socialLoginFailureMessage(provider: SocialLoginProvider, statusCode: Int, responseBody: String?): AuthUserMessage {
    if (provider != SocialLoginProvider.Google) {
        return when (statusCode) {
            401 -> AuthUserMessage.SocialSignInFailed
            409 -> AuthUserMessage.AccountUsesAnotherMethod
            429 -> AuthUserMessage.TooManyAttempts
            else -> AuthUserMessage.SocialSignInUnavailable
        }
    }

    val googleError = responseBody
        ?.takeIf { it.isNotBlank() }
        ?.let { body ->
            runCatching { defaultNetworkJson.decodeFromString<GoogleLoginErrorResponse>(body) }.getOrNull()
        }

    return when (googleError?.reason ?: googleError?.code ?: googleError?.error) {
        "forbidden", "access_denied" -> {
            AuthUserMessage.GoogleSignInUnavailable
        }
        "invalid_audience" -> {
            AuthUserMessage.GoogleSignInTemporarilyUnavailable
        }
        "missing_allowed_audience" -> {
            AuthUserMessage.GoogleSignInTemporarilyUnavailable
        }
        "expired_token" -> AuthUserMessage.GoogleSignInExpired
        "missing_email" -> AuthUserMessage.GoogleMissingEmail
        "unverified_email" -> AuthUserMessage.GoogleEmailUnverified
        "invalid_issuer", "invalid_signature", "malformed_token", "invalid_token" -> {
            AuthUserMessage.GoogleSignInCouldNotVerify
        }
        "account_link_required" -> {
            AuthUserMessage.AccountUsesAnotherMethod
        }
        else -> when {
            statusCode == 403 -> AuthUserMessage.GoogleSignInUnavailable
            else -> AuthUserMessage.GoogleSignInUnavailable
        }
    }
}

data class AuthApiResponse(
    val statusCode: Int,
    val session: AuthSession,
)

@Serializable
internal data class SavedDeliveryAddress(
    val id: String = "",
    val label: String,
    val city: String,
    val area: String? = null,
    val streetHint: String? = null,
)

@Serializable
data class CurrentUser(
    val id: String,
    val email: String? = null,
    val name: String? = null,
    @SerialName("displayName")
    val backendDisplayName: String? = null,
    val provider: String,
    val status: String,
    val publicReference: String? = null,
) {
    val displayName: String
        get() = backendDisplayName?.takeIf { it.isNotBlank() }
            ?: name?.takeIf { it.isNotBlank() }
            ?: email?.substringBefore("@")?.replaceFirstChar { it.uppercase() }
            ?: "Sequo user"
}

class AuthApiException(
    val statusCode: Int,
    val userMessage: AuthUserMessage,
    override val cause: Throwable? = null,
) : Exception(userMessage.name, cause)

enum class AuthUserMessage {
    EmailFormatInvalid,
    EmailPasswordIncorrect,
    AccountUsesAnotherMethod,
    TooManyAttempts,
    AuthenticationUnavailable,
    SocialSignInFailed,
    SocialSignInUnavailable,
    GoogleSignInUnavailable,
    GoogleSignInTemporarilyUnavailable,
    GoogleSignInExpired,
    GoogleMissingEmail,
    GoogleEmailUnverified,
    GoogleSignInCouldNotVerify,
    ProfileLoadFailed,
    SessionRestoreFailed,
}

@Serializable
data class AuthSession(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long,
)

@Serializable
private data class SocialLoginRequest(
    val token: String,
    val device: DeviceRequest,
    val challengeId: String? = null,
)

@Serializable
private data class DeviceRequest(
    val deviceId: String,
    val appSource: AppSource,
)

@Serializable
private data class EmailSignUpRequest(
    val email: String,
    val password: String,
    val name: String? = null,
)

@Serializable
private data class EmailLoginRequest(
    val email: String,
    val password: String,
)

@Serializable
private data class RefreshRequest(
    val refreshToken: String,
    val deviceId: String,
)

@Serializable
private data class LogoutRequest(
    val refreshToken: String,
)

@Serializable
private data class AuthErrorResponse(
    val code: String? = null,
    val message: String? = null,
    val requiredProvider: String? = null,
)

@Serializable
private data class GoogleLoginErrorResponse(
    val error: String? = null,
    val reason: String? = null,
    val code: String? = null,
    val message: String? = null,
)

@Serializable
private enum class SocialLoginProvider {
    @SerialName("GOOGLE")
    Google,

    @SerialName("FACEBOOK")
    Facebook,

    @SerialName("APPLE")
    Apple;

    val wireName: String
        get() = when (this) {
            Google -> "GOOGLE"
            Facebook -> "FACEBOOK"
            Apple -> "APPLE"
        }

    val displayName: String
        get() = when (this) {
            Google -> "Google"
            Facebook -> "Facebook"
            Apple -> "Apple"
        }
}

@Serializable
private enum class AppSource {
    @SerialName("SEQUO_APP")
    SequoApp,
}

@Serializable
data class LocalFixtureAccount(val key: String, val label: String)

@Serializable
private data class LocalFixtureAccess(val enabled: Boolean = false, val mode: String? = null, val accounts: List<LocalFixtureAccount> = emptyList())

@Serializable
private data class LocalFixtureSessionRequest(val appSource: String, val accountKey: String, val deviceId: String)

internal fun allowsLocalFixtureAccess(environment: String, baseUrl: String): Boolean = runCatching {
    val url = io.ktor.http.Url(baseUrl)
    environment == "LOCAL_FIXTURE" && url.host.lowercase() in setOf("127.0.0.1", "localhost", "10.0.2.2", "::1", "[::1]") &&
        url.protocol.name in setOf("http", "https") && url.user.isNullOrEmpty() && url.password.isNullOrEmpty() &&
        url.encodedPath in setOf("", "/") && url.parameters.isEmpty() && url.fragment.isEmpty()
}.getOrDefault(false)

@Serializable
private data class DemoSessionRequest(val accessCode: String, val deviceId: String)
