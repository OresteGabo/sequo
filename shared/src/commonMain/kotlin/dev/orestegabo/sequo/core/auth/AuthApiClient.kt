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
    private val baseUrl: String = NetworkConfig.ProductionBaseUrl,
    private val httpClient: HttpClient = createSequoHttpClient(baseUrl),
    private val deviceIdProvider: () -> String,
) {
    suspend fun loginWithGoogle(idToken: String): AuthApiResponse {
        return loginWithSocialToken(SocialLoginProvider.Google, idToken)
    }

    suspend fun loginWithFacebook(accessToken: String): AuthApiResponse {
        return loginWithSocialToken(SocialLoginProvider.Facebook, accessToken)
    }

    private suspend fun loginWithSocialToken(provider: SocialLoginProvider, token: String): AuthApiResponse {
        val response = try {
            httpClient.post("${baseUrl.trimEnd('/')}/api/auth/social/${provider.wireName}") {
                headers.append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(
                    SocialLoginRequest(
                        token = token,
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
                safeMessage = socialLoginFailureMessage(provider, error.statusCode, error.responseBody),
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
                safeMessage = emailAuthFailureMessage(error.statusCode, error.responseBody),
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
                safeMessage = emailAuthFailureMessage(error.statusCode, error.responseBody),
                cause = error,
            )
        }
        return response.toAuthApiResponse()
    }

    suspend fun currentUser(accessToken: String): CurrentUser {
        return try {
            httpClient.get("${baseUrl.trimEnd('/')}/api/auth/me") {
                bearerAuth(accessToken)
            }.body()
        } catch (error: SequoApiException) {
            throw AuthApiException(
                statusCode = error.statusCode,
                safeMessage = "Could not load the authenticated user profile.",
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
                safeMessage = "Could not restore the saved session.",
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
                safeMessage = socialLoginFailureMessage(provider, statusCode, bodyAsText()),
            )
        }

        return AuthApiResponse(
            statusCode = statusCode,
            session = body(),
        )
    }
}

private fun emailAuthFailureMessage(statusCode: Int, responseBody: String?): String {
    val error = responseBody
        ?.takeIf { it.isNotBlank() }
        ?.let { body -> runCatching { defaultNetworkJson.decodeFromString<AuthErrorResponse>(body) }.getOrNull() }

    return when (statusCode) {
        400 -> error?.message ?: "Check the email and password format."
        401 -> "Email or password is incorrect."
        409 -> error?.message ?: "This account must be opened with another sign-in method."
        429 -> error?.message ?: "Too many attempts. Please wait before trying again."
        else -> error?.message ?: "Authentication failed with HTTP $statusCode."
    }
}

private fun socialLoginFailureMessage(provider: SocialLoginProvider, statusCode: Int, responseBody: String?): String {
    if (provider != SocialLoginProvider.Google) {
        val error = responseBody
            ?.takeIf { it.isNotBlank() }
            ?.let { body -> runCatching { defaultNetworkJson.decodeFromString<AuthErrorResponse>(body) }.getOrNull() }
        return when (statusCode) {
            401 -> error?.message ?: "${provider.displayName} sign-in could not be completed."
            409 -> error?.message ?: "This account must be opened with another sign-in method."
            429 -> error?.message ?: "Too many attempts. Please wait before trying again."
            else -> error?.message ?: "${provider.displayName} login failed with HTTP $statusCode."
        }
    }

    val googleError = responseBody
        ?.takeIf { it.isNotBlank() }
        ?.let { body ->
            runCatching { defaultNetworkJson.decodeFromString<GoogleLoginErrorResponse>(body) }.getOrNull()
        }

    return when (googleError?.reason ?: googleError?.code ?: googleError?.error) {
        "forbidden", "access_denied" -> {
            "The Google request was blocked by the API gateway (HTTP $statusCode). Please try again."
        }
        "invalid_audience" -> {
            "Google sign-in reached Sequo, but the backend rejected this app's Google client ID. " +
                "Use the Web/server client ID in the Android app and allow that same client ID on the backend."
        }
        "missing_allowed_audience" -> {
            "Google sign-in reached Sequo, but backend Google client IDs are not configured."
        }
        "expired_token" -> "Google returned an expired sign-in token. Please try again."
        "missing_email" -> "Google did not share an email address for this account."
        "unverified_email" -> "Google says this account email is not verified."
        "invalid_issuer", "invalid_signature", "malformed_token", "invalid_token" -> {
            "Google sign-in reached Sequo, but the backend rejected the Google token (${googleError?.reason ?: googleError?.error})."
        }
        "account_link_required" -> {
            googleError?.message ?: "This email is already linked to another sign-in method."
        }
        else -> when {
            statusCode == 403 -> "The Google request was blocked by the API gateway (HTTP 403). Please try again."
            else -> googleError?.message ?: "Backend Google login failed with HTTP $statusCode."
        }
    }
}

data class AuthApiResponse(
    val statusCode: Int,
    val session: AuthSession,
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
) {
    val displayName: String
        get() = backendDisplayName?.takeIf { it.isNotBlank() }
            ?: name?.takeIf { it.isNotBlank() }
            ?: email?.substringBefore("@")?.replaceFirstChar { it.uppercase() }
            ?: "Sequo user"
}

class AuthApiException(
    val statusCode: Int,
    val safeMessage: String,
    override val cause: Throwable? = null,
) : Exception(safeMessage, cause)

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
    Facebook;

    val wireName: String
        get() = when (this) {
            Google -> "GOOGLE"
            Facebook -> "FACEBOOK"
        }

    val displayName: String
        get() = when (this) {
            Google -> "Google"
            Facebook -> "Facebook"
        }
}

@Serializable
private enum class AppSource {
    @SerialName("SEQUO_APP")
    SequoApp,
}
