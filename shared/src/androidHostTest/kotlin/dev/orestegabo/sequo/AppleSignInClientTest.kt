package dev.orestegabo.sequo

import dev.orestegabo.sequo.core.auth.AuthApiClient
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals

class AppleSignInClientTest {
    @Test fun sendsChallengeAndIdentityTokenToAppleEndpointWithDeviceBinding() = runBlocking {
        val calls = mutableListOf<Pair<String, String>>()
        val client = HttpClient(MockEngine { request ->
            calls += request.url.encodedPath to (request.body as TextContent).text
            val body = if (request.url.encodedPath.endsWith("/challenge")) {
                """{"challengeId":"challenge-test","nonce":"server-nonce"}"""
            } else {
                """{"accessToken":"access","refreshToken":"refresh","expiresIn":3600}"""
            }
            respond(body, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()))
        }) { install(ContentNegotiation) { json() } }
        try {
            val api = AuthApiClient(baseUrl = "https://example.test", httpClient = client, deviceIdProvider = { "device-test" })
            val challenge = api.appleSignInChallenge()
            assertEquals("server-nonce", challenge.nonce)
            assertEquals("access", api.loginWithApple("apple-id-token", challenge.challengeId).session.accessToken)
            assertEquals(listOf("/api/auth/apple/challenge", "/api/auth/social/APPLE"), calls.map { it.first })
            val payload = Json.parseToJsonElement(calls.last().second).jsonObject
            assertEquals("apple-id-token", payload.getValue("token").jsonPrimitive.content)
            assertEquals("challenge-test", payload.getValue("challengeId").jsonPrimitive.content)
            assertEquals("device-test", payload.getValue("device").jsonObject.getValue("deviceId").jsonPrimitive.content)
            assertEquals("SEQUO_APP", payload.getValue("device").jsonObject.getValue("appSource").jsonPrimitive.content)
        } finally { client.close() }
    }
}
