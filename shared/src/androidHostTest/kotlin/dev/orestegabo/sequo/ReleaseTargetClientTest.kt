package dev.orestegabo.sequo

import dev.orestegabo.sequo.core.auth.AuthApiClient
import dev.orestegabo.sequo.core.auth.AuthApiException
import dev.orestegabo.sequo.core.network.ApiHealthClient
import dev.orestegabo.sequo.core.network.NetworkConfig
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import dev.orestegabo.sequo.core.auth.AdminLoginApprovalApiClient

/** The transport is mocked: this regression must never contact a configured deployment. */
class ReleaseTargetClientTest {
    @Test
    fun defaultAuthAndOperationalClientsUseTheSameGeneratedOrigin() = runBlocking<Unit> {
        val requests = mutableListOf<Url>()
        val client = HttpClient(MockEngine { request ->
            requests += request.url
            when (request.url.encodedPath) {
                "/api/auth/login" -> respond("{}", HttpStatusCode.Unauthorized,
                    headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()))
                "/actuator/health" -> respond("{\"status\":\"UP\"}", HttpStatusCode.OK,
                    headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()))
                else -> respond("[]", HttpStatusCode.OK,
                    headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()))
            }
        }) {
            install(ContentNegotiation) { json() }
        }
        try {
            assertFailsWith<AuthApiException> {
                AuthApiClient(httpClient = client, deviceIdProvider = { "fixture-device" })
                    .loginWithEmail("release@example.test", "fixture-only")
            }
            assertTrue(ApiHealthClient(httpClient = client).checkHealth().isHealthyResponse)
            assertTrue(AdminLoginApprovalApiClient(accessTokenProvider = { "fixture-only" }, httpClient = client)
                .getPendingLoginRequests().getOrThrow().isEmpty())
            assertEquals(listOf("/api/auth/login", "/actuator/health", "/api/admin-auth/mobile/login-requests"), requests.map { it.encodedPath })
            val target = Url(NetworkConfig.BaseUrl)
            requests.forEach {
                assertEquals(target.protocol, it.protocol)
                assertEquals(target.host, it.host)
                assertEquals(target.port, it.port)
            }
            assertTrue(NetworkConfig.ReleaseId.isNotBlank())
            assertTrue(NetworkConfig.Environment in setOf("PRODUCTION", "STAGING", "LOCAL_FIXTURE"))
        } finally {
            client.close()
        }
    }
}
