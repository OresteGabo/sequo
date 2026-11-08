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
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CheckoutHistoryClientTest {
    private val pending = """
        {"checkoutId":"checkout-fixture","status":"AWAITING_WEBHOOK","totalCfa":2400,
         "createdAt":"2026-11-08T10:00:00Z","destinationLabel":null,
         "lines":[{"productName":"Fixture product","quantity":2}]}
    """.trimIndent()
    private val completed = """
        {"order":{"id":"order-fixture","checkoutId":"checkout-fixture","orderStatus":"ACCEPTED_FOR_FULFILLMENT",
         "totalCfa":2400,"createdAt":"2026-11-08T10:00:00Z"},
         "lines":[{"productName":"Fixture product","quantity":2}]}
    """.trimIndent()

    @Test
    fun readsPersistedPendingFailedCancelledAndConfirmedHistoryWithLegacyNullFields() = runBlocking<Unit> {
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        var checkoutBody = "[$pending]"
        var orderBody = "[]"
        val paths = mutableListOf<String>()
        server.createContext("/api/orders") { exchange ->
            assertEquals("GET", exchange.requestMethod)
            assertEquals("Bearer customer-fixture", exchange.requestHeaders.getFirst("Authorization"))
            assertNull(exchange.requestURI.rawQuery, "The server derives ownership from the authenticated user.")
            paths += exchange.requestURI.path
            val body = if (exchange.requestURI.path.endsWith("/checkouts")) checkoutBody else orderBody
            val bytes = body.toByteArray()
            exchange.responseHeaders.set("Content-Type", "application/json")
            exchange.sendResponseHeaders(200, bytes.size.toLong())
            exchange.responseBody.use { it.write(bytes) }
        }
        server.start()
        val client = CartApiClient(createSequoHttpClient("http://127.0.0.1:${server.address.port}"))
        try {
            val waiting = client.checkouts("customer-fixture").single()
            assertEquals("AWAITING_WEBHOOK", waiting.status)
            assertNull(waiting.orderId)
            assertNull(waiting.destinationLabel)
            assertEquals(2, waiting.lines.single().quantity)
            assertTrue(client.orders("customer-fixture").isEmpty())
            for (status in listOf("FAILED", "CANCELLED")) {
                checkoutBody = "[${pending.replace("AWAITING_WEBHOOK", status)}]"
                assertEquals(status, client.checkouts("customer-fixture").single().status)
                assertTrue(client.orders("customer-fixture").isEmpty())
            }
            checkoutBody = "[${pending.replace("AWAITING_WEBHOOK", "COMPLETED")}]"
            orderBody = "[$completed]"
            val confirmedCheckout = client.checkouts("customer-fixture").single()
            val order = client.orders("customer-fixture").single()
            assertEquals("COMPLETED", confirmedCheckout.status)
            assertEquals(confirmedCheckout.checkoutId, order.order.checkoutId)
            assertEquals("ACCEPTED_FOR_FULFILLMENT", order.order.orderStatus)
            assertTrue(order.deliveries.isEmpty(), "Legacy order reads need not invent a delivery record.")
            assertEquals(2400, order.order.totalCfa)
            orderBody = "[${completed.replace("ACCEPTED_FOR_FULFILLMENT", "DELIVERED")}]"
            assertEquals("DELIVERED", client.orders("customer-fixture").single().order.orderStatus)
            assertTrue(paths.all { it == "/api/orders/checkouts" || it == "/api/orders" })
        } finally { client.close(); server.stop(0) }
    }

    @Test
    fun deniedOrUnavailableRefreshDoesNotBecomeCachedOrSyntheticSuccess() = runBlocking<Unit> {
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        var failingPath = "none"
        var status = 503
        var orderBody = "[$completed]"
        server.createContext("/api/orders") { exchange ->
            assertEquals("Bearer customer-fixture", exchange.requestHeaders.getFirst("Authorization"))
            val code = if (exchange.requestURI.path == failingPath) status else 200
            val body = if (code != 200) """{"message":"Fixture read unavailable"}""" else
                if (exchange.requestURI.path.endsWith("/checkouts")) "[$pending]" else orderBody
            val bytes = body.toByteArray()
            exchange.responseHeaders.set("Content-Type", "application/json")
            exchange.sendResponseHeaders(code, bytes.size.toLong())
            exchange.responseBody.use { it.write(bytes) }
        }
        server.start()
        val client = CartApiClient(createSequoHttpClient("http://127.0.0.1:${server.address.port}"))
        try {
            assertEquals("ACCEPTED_FOR_FULFILLMENT", client.orders("customer-fixture").single().order.orderStatus)
            for (path in listOf("/api/orders/checkouts", "/api/orders")) {
                failingPath = path
                for (code in listOf(401, 403, 503)) {
                    status = code
                    val error = assertFailsWith<SequoApiException> {
                        // This is the normal two-read refresh order, including a failed second read.
                        client.checkouts("customer-fixture")
                        client.orders("customer-fixture")
                    }
                    assertEquals(code, error.statusCode)
                }
            }
            failingPath = "none"
            orderBody = "[]"
            assertEquals("AWAITING_WEBHOOK", client.checkouts("customer-fixture").single().status)
            assertTrue(client.orders("customer-fixture").isEmpty())
        } finally { client.close(); server.stop(0) }
    }
}
