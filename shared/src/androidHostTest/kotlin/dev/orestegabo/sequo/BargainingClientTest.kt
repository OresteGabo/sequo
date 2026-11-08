package dev.orestegabo.sequo

import com.sun.net.httpserver.HttpServer
import dev.orestegabo.sequo.core.bargaining.NetworkBargainingRepository
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

class BargainingClientTest {
    private val open = """{"id":"thread-1","productId":"product-1","status":"OPEN","catalogPriceCfa":1000,
        "bargainingEnabled":true,"customerAttemptsUsed":1,"maxCustomerAttempts":3,"nextActor":"MERCHANT",
        "offers":[{"id":"customer-offer","actorType":"CUSTOMER","offerType":"OFFER","amountCfa":800}]}"""

    @Test
    fun restoresServerRepliesPreservesRetryKeysAndDoesNotConfirmRejectedActions() = runBlocking<Unit> {
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        var state = open
        var reject = false
        val requests = mutableListOf<String>()
        server.createContext("/api/bargaining/threads") { exchange ->
            assertEquals("Bearer customer-fixture", exchange.requestHeaders.getFirst("Authorization"))
            if (exchange.requestMethod == "POST") requests += exchange.requestBody.readBytes().decodeToString()
            val body = if (reject) """{"code":"stale_offer","message":"Refresh the current offer."}""" else if (exchange.requestMethod == "GET") "[$state]" else state
            val bytes = body.toByteArray()
            exchange.responseHeaders.set("Content-Type", "application/json")
            exchange.sendResponseHeaders(if (reject) 409 else 200, bytes.size.toLong())
            exchange.responseBody.use { it.write(bytes) }
        }
        server.start()
        val repository = NetworkBargainingRepository({ "customer-fixture" }, createSequoHttpClient("http://127.0.0.1:${server.address.port}"))
        try {
            repeat(2) {
                val pending = repository.offer("product-1", null, 800, "stable-create")
                assertEquals(2, pending.attemptsRemaining)
                assertNull(pending.acceptedPriceCfa)
                assertTrue(!pending.accepted)
            }
            val create = Json.parseToJsonElement("""{"productId":"product-1","amountCfa":800,"idempotencyKey":"stable-create"}""")
            assertEquals(listOf(create, create), requests.map(Json::parseToJsonElement))
            state = """{"id":"thread-1","productId":"product-1","status":"OPEN","nextActor":"CUSTOMER","customerAttemptsUsed":1,"maxCustomerAttempts":3,
                "offers":[{"id":"counter-1","actorType":"MERCHANT","offerType":"COUNTER_OFFER","amountCfa":850}]}"""
            val refreshed = repository.threads("product-1").single()
            assertEquals("counter-1", refreshed.pendingMerchantOffer?.id)
            reject = true
            assertEquals(409, assertFailsWith<SequoApiException> { repository.respond(refreshed.id, "counter-1", true, "stable-accept") }.statusCode)
            assertTrue(!refreshed.accepted)
            reject = false
            state = """{"id":"thread-1","productId":"product-1","status":"ACCEPTED","acceptedPriceCfa":850,"expiresAt":"2026-11-09T10:00:00Z","offers":[]}"""
            val accepted = repository.respond(refreshed.id, "counter-1", true, "stable-accept")
            assertTrue(accepted.accepted)
            assertEquals(850, accepted.acceptedPriceCfa)
            assertEquals("2026-11-09T10:00:00Z", repository.threads("product-1").single().expiresAt)
            val acceptance = Json.parseToJsonElement("""{"offerType":"ACCEPT","expectedOfferId":"counter-1","idempotencyKey":"stable-accept"}""")
            assertEquals(listOf(acceptance, acceptance), requests.drop(2).map(Json::parseToJsonElement))
        } finally { repository.close(); server.stop(0) }
    }

    @Test
    fun negotiatedCartReferenceSurvivesQuantityEditsAndKeepsServerTotalsAuthoritative() = runBlocking<Unit> {
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        val bodies = mutableListOf<String>()
        val snapshot = """{"items":[{"id":"negotiated-line","product":{"id":"product-1","name":"Fixture item"},"quantity":2,"selections":[],"lineTotalCfa":1700,"available":true,
            "bargainingThreadId":"thread-1","negotiatedUnitPriceCfa":850,"negotiatedPriceExpiresAt":"2026-11-09T10:00:00Z"},
            {"id":"catalog-line","product":{"id":"product-1","name":"Fixture item"},"quantity":1,"selections":[],"lineTotalCfa":1000,"available":true}],"totalItems":3,"subtotalCfa":2700}"""
        server.createContext("/api/cart") { exchange ->
            assertEquals("Bearer customer-fixture", exchange.requestHeaders.getFirst("Authorization"))
            if (exchange.requestMethod == "PUT") bodies += exchange.requestBody.readBytes().decodeToString()
            val body = if (exchange.requestURI.path.endsWith("/quote")) """{"addressId":"address-1","revision":"server-revision","productTotalCfa":2700,"optionTotalCfa":0,"serviceFeeCfa":200,"deliveryFeeCfa":400,"totalCfa":3300}""" else snapshot
            val bytes = body.toByteArray(); exchange.responseHeaders.set("Content-Type", "application/json")
            exchange.sendResponseHeaders(200, bytes.size.toLong()); exchange.responseBody.use { it.write(bytes) }
        }
        server.start()
        val client = CartApiClient(createSequoHttpClient("http://127.0.0.1:${server.address.port}"))
        try {
            val saved = client.put("customer-fixture", "product-1", 2, emptyList(), "thread-1")
            val negotiated = requireNotNull(saved.findLine("product-1", emptyList(), "thread-1"))
            assertEquals("negotiated-line", negotiated.id)
            assertEquals("catalog-line", saved.findLine("product-1", emptyList())?.id)
            assertEquals(850, negotiated.negotiatedUnitPriceCfa)
            client.put("customer-fixture", "product-1", 3, negotiated.selections, negotiated.bargainingThreadId)
            for (body in bodies) {
                val request = Json.parseToJsonElement(body).toString()
                assertTrue(request.contains("\"bargainingThreadId\":\"thread-1\""))
                assertTrue(!request.contains("850"))
            }
            assertEquals(3300, client.quote("customer-fixture", "address-1").totalCfa)
        } finally { client.close(); server.stop(0) }
    }
}
