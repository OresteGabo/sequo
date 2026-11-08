package dev.orestegabo.sequo

import com.sun.net.httpserver.HttpServer
import dev.orestegabo.sequo.core.catalog.CartApiClient
import dev.orestegabo.sequo.core.network.SequoApiException
import dev.orestegabo.sequo.core.network.createSequoHttpClient
import java.net.InetSocketAddress
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PaymentProviderClientTest {
    @Test
    fun persistedLiveReadinessAndConfiguredPrerequisitesDoNotMeanCheckoutConnected() = runBlocking<Unit> {
        fixture("""
            [{"providerId":"yas_togo","displayName":"Mixx by Yas","countryCode":"TG",
              "readiness":"PLANNED","configuredReadiness":"LIVE_READY","customerCheckoutEnabled":false,
              "refundEnabled":false,"courierPayoutEnabled":false,"apiConfigured":true,"checkoutConnected":false,
              "integrationStatus":"NOT_CONNECTED","configurationIssues":[]},
             {"providerId":"moov_africa","displayName":"Flooz","countryCode":"TG",
              "readiness":"LIVE_READY","customerCheckoutEnabled":true,"refundEnabled":true,
              "courierPayoutEnabled":true,"apiConfigured":true}]
        """.trimIndent()) { client ->
            val providers = client.paymentProviders()
            assertEquals(listOf("yas_togo", "moov_africa"), providers.map { it.providerId })
            assertTrue(providers.all { it.apiConfigured })
            assertTrue(providers.none { it.checkoutConnected || it.paymentAvailable })
        }
    }

    @Test
    fun legacyAndContradictoryReadFieldsStayUnavailableUnlessBothExplicitChecksSucceed() = runBlocking<Unit> {
        fixture("""
            [{"providerId":"legacy","readiness":"LIVE_READY","customerCheckoutEnabled":true},
             {"providerId":"configured-only","apiConfigured":true},
             {"providerId":"connected-only","checkoutConnected":true},
             {"providerId":"explicit-connected-fixture","apiConfigured":true,"checkoutConnected":true}]
        """.trimIndent()) { client ->
            val providers = client.paymentProviders()
            assertFalse(providers[0].apiConfigured)
            assertFalse(providers[0].checkoutConnected)
            assertTrue(providers.take(3).none { it.paymentAvailable })
            assertTrue(providers.last().paymentAvailable)
        }
    }

    @Test
    fun failedRefreshPropagatesAndLaterEmptyReadDoesNotReuseEarlierAvailableRecords() = runBlocking<Unit> {
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        var status = 200
        var body = """[{"providerId":"fixture","apiConfigured":true,"checkoutConnected":true}]"""
        server.createContext("/api/payments/providers") { exchange ->
            assertEquals("GET", exchange.requestMethod)
            val bytes = body.toByteArray()
            exchange.responseHeaders.set("Content-Type", "application/json")
            exchange.sendResponseHeaders(status, bytes.size.toLong())
            exchange.responseBody.use { it.write(bytes) }
        }
        server.start()
        val client = CartApiClient(createSequoHttpClient("http://127.0.0.1:${server.address.port}"))
        try {
            assertTrue(client.paymentProviders().single().paymentAvailable)
            status = 503
            body = """{"message":"Fixture provider status unavailable"}"""
            assertEquals(503, assertFailsWith<SequoApiException> { client.paymentProviders() }.statusCode)
            status = 200
            body = "[]"
            assertTrue(client.paymentProviders().isEmpty())
        } finally { client.close(); server.stop(0) }
    }

    private suspend fun fixture(body: String, block: suspend (CartApiClient) -> Unit) {
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        server.createContext("/api/payments/providers") { exchange ->
            assertEquals("GET", exchange.requestMethod)
            val bytes = body.toByteArray()
            exchange.responseHeaders.set("Content-Type", "application/json")
            exchange.sendResponseHeaders(200, bytes.size.toLong())
            exchange.responseBody.use { it.write(bytes) }
        }
        server.start()
        val client = CartApiClient(createSequoHttpClient("http://127.0.0.1:${server.address.port}"))
        try { block(client) } finally { client.close(); server.stop(0) }
    }
}
