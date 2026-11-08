package dev.orestegabo.sequo

import com.sun.net.httpserver.HttpServer
import dev.orestegabo.sequo.core.catalog.CartApiClient
import dev.orestegabo.sequo.core.network.SequoApiException
import dev.orestegabo.sequo.core.network.createSequoHttpClient
import java.net.InetSocketAddress
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ReturnClientTest {
    @Test
    fun eligibilityDeadlineAndPendingPickupInstructionsAreServerOwnedAndErrorsDoNotBecomeEligibility() = runBlocking<Unit> {
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        var allowed = true
        var pickupPending = false
        var denied = false
        server.createContext("/api/returns/orders/order-1/eligibility") { exchange ->
            assertEquals("Bearer customer-fixture", exchange.requestHeaders.getFirst("Authorization"))
            assertEquals("GET", exchange.requestMethod)
            val code = if (allowed) "null" else "\"return_window_expired\""
            val method = if (pickupPending) "CUSTOMER_MANAGED_PENDING" else "RELAY"
            val body = if (denied) """{"code":"forbidden","message":"Order access denied."}""" else
                """{"orderId":"order-1","eligible":$allowed,"code":$code,"deliveredAt":"2020-01-01T00:00:00Z",
                "returnWindowEndsAt":"2020-01-04T00:00:00Z","dropoffMethod":"$method","instructions":"Follow the server return instructions.",
                "relayPoints":[{"id":"relay-1","name":"Fixture relay","city":"Lomé","latitude":6.1,"longitude":1.2}]}"""
            val bytes = body.toByteArray(); exchange.responseHeaders.set("Content-Type", "application/json")
            exchange.sendResponseHeaders(if (denied) 403 else 200, bytes.size.toLong()); exchange.responseBody.use { it.write(bytes) }
        }
        server.start()
        val client = CartApiClient(createSequoHttpClient("http://127.0.0.1:${server.address.port}"))
        try {
            val eligible = client.returnEligibility("customer-fixture", "order-1")
            assertTrue(eligible.eligible)
            assertEquals("2020-01-04T00:00:00Z", eligible.returnWindowEndsAt)
            assertEquals("Fixture relay", eligible.relayPoints.single().name)
            allowed = false
            val expired = client.returnEligibility("customer-fixture", "order-1")
            assertTrue(!expired.eligible)
            assertEquals("return_window_expired", expired.code)
            allowed = true; pickupPending = true
            val pending = client.returnEligibility("customer-fixture", "order-1")
            assertTrue(pending.eligible)
            assertEquals("CUSTOMER_MANAGED_PENDING", pending.dropoffMethod)
            assertEquals("Follow the server return instructions.", pending.instructions)
            denied = true
            assertEquals(403, assertFailsWith<SequoApiException> { client.returnEligibility("customer-fixture", "order-1") }.statusCode)
        } finally { client.close(); server.stop(0) }
    }

    @Test
    fun uncertainRequestRetainsMinimalRetryBodyAndReadsTheCanonicalReturnAndActualDropoffState() = runBlocking<Unit> {
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        val bodies = mutableListOf<String>()
        var droppedOff = false
        var inspected = false
        val canonical = """{"id":"canonical-return","orderId":"order-1","status":"AwaitingRelayDropoff","reason":"Package damaged",
            "requestedAt":"2026-11-08T10:00:00Z","dropoffMethod":"RELAY","instructions":"Bring the item to Fixture relay.",
            "returnPin":"654321","relayPoints":[{"id":"relay-1","name":"Fixture relay"}]}"""
        server.createContext("/api/returns") { exchange ->
            assertEquals("Bearer customer-fixture", exchange.requestHeaders.getFirst("Authorization"))
            if (exchange.requestMethod == "POST") bodies += exchange.requestBody.readBytes().decodeToString()
            val unavailable = exchange.requestMethod == "POST" && bodies.size == 1
            val actual = if (droppedOff) canonical.replace("AwaitingRelayDropoff", "DroppedAtRelay")
                .replace("\"returnPin\":\"654321\"", "\"returnPin\":null,\"droppedAt\":\"2026-11-08T12:00:00Z\"") else canonical
            val latest = if (inspected) """{"id":"canonical-return","orderId":"order-1","status":"RefundPending","reason":"Package damaged",
                "requestedAt":"2026-11-08T10:00:00Z","dropoffMethod":"ALREADY_DEPOSITED","instructions":"Refund instruction recorded.",
                "collectedBySequoAt":"2026-11-08T13:00:00Z","receivedBySequoAt":"2026-11-08T14:00:00Z",
                "inspectionDecision":"ACCEPTED","inspectedAt":"2026-11-08T15:00:00Z","refundDeadlineState":"UNCONFIGURED","refundDueAt":null,
                "refundInstruction":{"id":"instruction-1","amountCfa":2000,"currency":"XOF","requestedAt":"2026-11-08T15:00:00Z","status":"REQUESTED","paymentStatus":"NOT_SUBMITTED"}}""" else actual
            val body = if (unavailable) """{"code":"unavailable","message":"Unable to confirm."}""" else if (exchange.requestMethod == "GET") "[$latest]" else latest
            val bytes = body.toByteArray(); exchange.responseHeaders.set("Content-Type", "application/json")
            exchange.sendResponseHeaders(if (unavailable) 503 else 200, bytes.size.toLong()); exchange.responseBody.use { it.write(bytes) }
        }
        server.start()
        val client = CartApiClient(createSequoHttpClient("http://127.0.0.1:${server.address.port}"))
        try {
            assertEquals(503, assertFailsWith<SequoApiException> {
                client.requestReturn("customer-fixture", "order-1", "stable-proposed-return", "Package damaged")
            }.statusCode)
            val saved = client.requestReturn("customer-fixture", "order-1", "stable-proposed-return", "Package damaged")
            assertEquals("canonical-return", saved.id)
            assertEquals("AwaitingRelayDropoff", saved.status)
            assertEquals("654321", saved.returnPin)
            val expected = Json.parseToJsonElement("""{"orderId":"order-1","returnId":"stable-proposed-return","reason":"Package damaged"}""")
            assertEquals(listOf(expected, expected), bodies.map(Json::parseToJsonElement))
            assertEquals(saved, client.returns("customer-fixture", "order-1").single())
            droppedOff = true
            val reloaded = client.returns("customer-fixture", "order-1").single()
            assertEquals(saved.id, reloaded.id)
            assertEquals("DroppedAtRelay", reloaded.status)
            assertEquals("2026-11-08T12:00:00Z", reloaded.droppedAt)
            assertNull(reloaded.returnPin)
            inspected = true
            val instruction = client.returns("customer-fixture", "order-1").single()
            assertEquals("2026-11-08T13:00:00Z", instruction.collectedBySequoAt)
            assertEquals("ACCEPTED", instruction.inspectionDecision)
            assertEquals("2026-11-08T15:00:00Z", instruction.inspectedAt)
            assertEquals("REQUESTED", instruction.refundInstruction?.status)
            assertEquals("NOT_SUBMITTED", instruction.refundInstruction?.paymentStatus)
            assertEquals(2000, instruction.refundInstruction?.amountCfa)
            assertEquals("UNCONFIGURED", instruction.refundDeadlineState)
            assertNull(instruction.refundDueAt)
            assertNull(instruction.returnPin)
        } finally { client.close(); server.stop(0) }
    }
}
