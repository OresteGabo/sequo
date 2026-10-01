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
) {
    suspend fun loginWithGoogle(idToken: String): AuthApiResponse {
        val response = try {
            httpClient.post("${baseUrl.trimEnd('/')}/api/auth/login/social") {
                headers.append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(
                    SocialLoginRequest(
                        provider = SocialLoginProvider.Google,
                        token = idToken,
                    ),
                )
            }
        } catch (error: SequoApiException) {
            throw AuthApiException(
                statusCode = error.statusCode,
                safeMessage = googleLoginFailureMessage(error.statusCode, error.responseBody),
                cause = error,
            )
        }
        return response.toAuthApiResponse()
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

    fun close() {
        httpClient.close()
    }

    private suspend fun HttpResponse.toAuthApiResponse(): AuthApiResponse {
        val statusCode = status.value
        if (statusCode !in 200..299) {
            throw AuthApiException(
                statusCode = statusCode,
                safeMessage = googleLoginFailureMessage(statusCode, bodyAsText()),
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

private fun googleLoginFailureMessage(statusCode: Int, responseBody: String?): String {
    val googleError = responseBody
        ?.takeIf { it.isNotBlank() }
        ?.let { body ->
            runCatching { defaultNetworkJson.decodeFromString<GoogleLoginErrorResponse>(body) }.getOrNull()
        }

    return when (googleError?.reason ?: googleError?.code ?: googleError?.error) {
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
        else -> googleError?.message ?: "Backend Google login failed with HTTP $statusCode."
    }
}

data class AuthApiResponse(
    val statusCode: Int,
    val session: AuthSession,
)

@Serializable
data class CurrentUser(
    val id: String,
    val email: String,
    val name: String? = null,
    val provider: String,
    val status: String,
) {
    val displayName: String
        get() = name?.takeIf { it.isNotBlank() } ?: email.substringBefore("@").replaceFirstChar { it.uppercase() }
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
    val provider: SocialLoginProvider,
    val token: String,
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
}
