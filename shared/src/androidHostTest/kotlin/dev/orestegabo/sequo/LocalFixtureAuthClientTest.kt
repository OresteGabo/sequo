package dev.orestegabo.sequo

import dev.orestegabo.sequo.core.auth.*
import dev.orestegabo.sequo.core.network.defaultNetworkJson
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.*
import io.ktor.http.content.TextContent
import kotlinx.serialization.json.*
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.*
import kotlin.test.*

class LocalFixtureAuthClientTest {
    @Test
    fun practiceAccessRequiresLocalBuildAndExactLocalOrigin() {
        for (target in listOf("http://127.0.0.1:55840", "http://10.0.2.2:55840", "http://localhost:55840")) {
            assertTrue(allowsLocalFixtureAccess("LOCAL_FIXTURE", target))
            assertFalse(allowsLocalFixtureAccess("PRODUCTION", target))
        }
        for (target in listOf("https://api.sequoservice.com", "http://127.0.0.1.evil.test", "http://user@127.0.0.1", "http://127.0.0.1/path", "http://127.0.0.1?x=y"))
            assertFalse(allowsLocalFixtureAccess("LOCAL_FIXTURE", target))
    }

    @Test
    fun productionNeverCallsPracticeEndpointsAndUnknownAccountsCannotBeSubmitted() = runBlocking<Unit> {
        var calls = 0
        val http = HttpClient(MockEngine { calls++; respond("""{"enabled":true,"mode":"LOCAL_FIXTURE","accounts":[{"key":"ama","label":"[TEST] Ama"}]}""", headers = headersOf(HttpHeaders.ContentType, "application/json")) }) {
            install(ContentNegotiation) { json(defaultNetworkJson) }
        }
        try {
            val production = AuthApiClient("https://api.sequoservice.com", http, { "device" }, "PRODUCTION")
            assertEquals(emptyList(), production.localFixtureAccounts())
            assertFailsWith<IllegalStateException> { production.loginWithLocalFixture("ama") }
            assertEquals(0, calls)
            val local = AuthApiClient("http://127.0.0.1:55840", http, { "device" }, "LOCAL_FIXTURE")
            assertFailsWith<IllegalStateException> { local.loginWithLocalFixture("arbitrary-user") }
            assertEquals(1, calls)
        } finally { http.close() }
    }

    @Test
    fun practiceSessionSendsExplicitFamilyAndDeviceAndAcceptsNormalTokenResponse() = runBlocking<Unit> {
        var posted = false
        val http = HttpClient(MockEngine { request ->
            if (request.url.encodedPath.endsWith("development-access")) {
                assertEquals("SEQUO_APP", request.url.parameters["appSource"])
                respond("""{"enabled":true,"mode":"LOCAL_FIXTURE","accounts":[{"key":"ama","label":"[TEST] Ama"}]}""", headers=headersOf(HttpHeaders.ContentType,"application/json"))
            } else {
                assertEquals("/api/auth/development-session", request.url.encodedPath)
                val body = Json.parseToJsonElement((request.body as TextContent).text).jsonObject
                assertEquals("SEQUO_APP", body["appSource"]?.jsonPrimitive?.content)
                assertEquals("ama", body["accountKey"]?.jsonPrimitive?.content)
                assertEquals("review-device", body["deviceId"]?.jsonPrimitive?.content)
                posted = true
                respond("""{"accessToken":"test-access","refreshToken":"test-refresh","expiresIn":3600,"user":{"id":"ama"}}""", headers=headersOf(HttpHeaders.ContentType,"application/json"))
            }
        }) { install(ContentNegotiation) { json(defaultNetworkJson) } }
        try {
            val session = AuthApiClient("http://127.0.0.1:55840", http, { "review-device" }, "LOCAL_FIXTURE").loginWithLocalFixture("ama")
            assertTrue(posted)
            assertEquals("test-access", session.accessToken)
        } finally { http.close() }
    }

    @Test
    fun concurrentRestoresRotateRefreshTokenOnceAndLogoutCannotResurrectIt() = runBlocking<Unit> {
        var refreshes = 0
        val store = object : AuthSessionStore {
            var session: AuthSession? = AuthSession("expired", "refresh-one", 0)
            override suspend fun save(session: AuthSession) { this.session = session }
            override suspend fun get() = session
            override suspend fun saveRememberedUser(user: CurrentUser) = Unit
            override suspend fun getRememberedUser(): CurrentUser? = null
            override suspend fun clear() { session = null }
        }
        val secure = object : SecureTokenStorage {
            var token: String? = null
            override suspend fun saveRefreshToken(token: String) { this.token = token }
            override suspend fun getRefreshToken() = token
            override suspend fun clearSession() { token = null }
        }
        val biometric = object : BiometricAuthenticator {
            override val availability = BiometricAvailability.NotSupported
            override suspend fun authenticate(config: BiometricPromptConfig) = BiometricAuthResult.Cancelled
        }
        val http = HttpClient(MockEngine { request ->
            when (request.url.encodedPath) {
                "/api/auth/refresh" -> { refreshes++; delay(25); respond("""{"accessToken":"fresh","refreshToken":"refresh-two","expiresIn":3600}""", headers=headersOf(HttpHeaders.ContentType,"application/json")) }
                "/api/auth/me" -> if (request.headers[HttpHeaders.Authorization] == "Bearer expired") throw AuthApiException(401, AuthUserMessage.SessionRestoreFailed) else respond("""{"id":"ama","email":"ama@fixture.invalid","name":"[TEST] Ama","provider":"EMAIL","status":"ACTIVE","roles":["CUSTOMER"]}""", headers=headersOf(HttpHeaders.ContentType,"application/json"))
                else -> respond("{}", headers=headersOf(HttpHeaders.ContentType,"application/json"))
            }
        }) { install(ContentNegotiation) { json(defaultNetworkJson) } }
        val repository = AuthRepository(AuthApiClient("http://127.0.0.1", http, {"device"}, "LOCAL_FIXTURE"), store, SecureSessionCache(secure, biometric))
        try {
            val results = listOf(async { repository.restoreSavedSession() }, async { repository.restoreSavedSession() })
            results.awaitAll()
            assertEquals(1, refreshes)
            repository.logout()
            assertNull(store.get())
            assertNull(secure.getRefreshToken())
        } finally { http.close() }
    }
}
