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

class BenefitsClientTest {
    @Test
    fun readsEffectiveServerBenefitsWithoutPromotingRawActiveOrHidingReadFailures() = runBlocking<Unit> {
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        var status = "PENDING_VERIFICATION"
        var unavailable = false
        server.createContext("/api/subscriptions/benefits") { exchange ->
            assertEquals("GET", exchange.requestMethod)
            assertEquals("Bearer customer-fixture", exchange.requestHeaders.getFirst("Authorization"))
            val active = status == "ACTIVE"
            val body = if (unavailable) """{"code":"unavailable","message":"Refresh later."}""" else
                """{"subscriptionActive":$active,"subscriptionStatus":"ACTIVE","planName":"Approved local fixture plan","monthlyPriceCfa":2500,
                "deliveryDiscountPercent":${if (active) 7 else 0},"loyaltyEnabled":$active,
                "availableReferralCreditCfa":${if (active) 230 else 0},"currency":"XOF","status":"$status"}"""
            val bytes = body.toByteArray(); exchange.responseHeaders.set("Content-Type", "application/json")
            exchange.sendResponseHeaders(if (unavailable) 503 else 200, bytes.size.toLong()); exchange.responseBody.use { it.write(bytes) }
        }
        server.start()
        val client = CartApiClient(createSequoHttpClient("http://127.0.0.1:${server.address.port}"))
        try {
            val pending = client.benefits("customer-fixture")
            assertEquals("ACTIVE", pending.subscriptionStatus)
            assertEquals("PENDING_VERIFICATION", pending.status)
            assertTrue(!pending.subscriptionActive)
            assertEquals(0, pending.deliveryDiscountPercent)
            assertEquals(0, pending.availableReferralCreditCfa)
            status = "ACTIVE"
            val verified = client.benefits("customer-fixture")
            assertTrue(verified.subscriptionActive)
            assertEquals(7, verified.deliveryDiscountPercent)
            assertEquals(230, verified.availableReferralCreditCfa)
            assertEquals("Approved local fixture plan", verified.planName)
            status = "EXPIRED"
            assertTrue(!client.benefits("customer-fixture").subscriptionActive)
            status = "NO_APPROVED_SUBSCRIPTION"
            assertEquals(0, client.benefits("customer-fixture").deliveryDiscountPercent)
            status = "UNKNOWN_POLICY"
            assertFailsWith<IllegalArgumentException> { client.benefits("customer-fixture") }
            unavailable = true
            assertEquals(503, assertFailsWith<SequoApiException> { client.benefits("customer-fixture") }.statusCode)
        } finally { client.close(); server.stop(0) }
    }

    @Test
    fun quoteBreakdownAndCheckoutUseServerBenefitsAndSupportLegacyQuoteResponses() = runBlocking<Unit> {
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        var legacy = false
        var checkoutBody: String? = null
        server.createContext("/api/cart/quote") { exchange ->
            assertEquals("Bearer customer-fixture", exchange.requestHeaders.getFirst("Authorization"))
            assertEquals("addressId=address-1", exchange.requestURI.rawQuery)
            val body = if (legacy) """{"addressId":"address-1","revision":"legacy-revision","productTotalCfa":2000,"optionTotalCfa":0,"serviceFeeCfa":0,"deliveryFeeCfa":400,"totalCfa":2400}""" else
                """{"addressId":"address-1","revision":"server-benefits-revision","productTotalCfa":2000,"optionTotalCfa":0,"serviceFeeCfa":0,
                "deliveryBaseFeeCfa":800,"subscriptionDiscountCfa":56,"loyaltyDiscountCfa":24,"referralCreditAppliedCfa":180,"deliveryFeeCfa":540,"totalCfa":2540}"""
            val bytes = body.toByteArray(); exchange.responseHeaders.set("Content-Type", "application/json")
            exchange.sendResponseHeaders(200, bytes.size.toLong()); exchange.responseBody.use { it.write(bytes) }
        }
        server.createContext("/api/orders/checkouts") { exchange ->
            assertEquals("Bearer customer-fixture", exchange.requestHeaders.getFirst("Authorization"))
            checkoutBody = exchange.requestBody.readBytes().decodeToString()
            val bytes = """{"checkoutId":"checkout-1","status":"AWAITING_WEBHOOK","totalCfa":2540,"createdAt":"2026-11-08T10:00:00Z","lines":[]}""".toByteArray()
            exchange.responseHeaders.set("Content-Type", "application/json")
            exchange.sendResponseHeaders(202, bytes.size.toLong()); exchange.responseBody.use { it.write(bytes) }
        }
        server.start()
        val client = CartApiClient(createSequoHttpClient("http://127.0.0.1:${server.address.port}"))
        try {
            val quote = client.quote("customer-fixture", "address-1")
            assertEquals(800, quote.deliveryBaseFeeCfa)
            assertEquals(56, quote.subscriptionDiscountCfa)
            assertEquals(24, quote.loyaltyDiscountCfa)
            assertEquals(180, quote.referralCreditAppliedCfa)
            assertEquals(540, quote.deliveryFeeCfa)
            assertEquals(2540, quote.totalCfa)
            assertEquals(2540, client.checkout("customer-fixture", quote, "yas_togo").totalCfa)
            assertEquals(Json.parseToJsonElement("""{"addressId":"address-1","quoteRevision":"server-benefits-revision","paymentProvider":"yas_togo"}"""),
                Json.parseToJsonElement(requireNotNull(checkoutBody)))
            legacy = true
            val old = client.quote("customer-fixture", "address-1")
            assertNull(old.deliveryBaseFeeCfa)
            assertEquals(0, old.subscriptionDiscountCfa)
            assertEquals(0, old.loyaltyDiscountCfa)
            assertEquals(0, old.referralCreditAppliedCfa)
            assertEquals(2400, old.totalCfa)
        } finally { client.close(); server.stop(0) }
    }
}
